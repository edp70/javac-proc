package edp.javac.proc;

import java.util.ArrayList;
import java.util.Collection;

// supported API
import com.sun.source.tree.Tree;

// unsupported API
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

import edp.javac.copier.JavacCopier;

/** Rewrites method bodies so that there is only one "return"
    statement, at the end. Ie, translate this:

    <code>
    { B }
    </code>

    to the following (for non-void method, where TYPE is the return
    type):

    <code>
    {
        TYPE #r;
        #R: {
            B // with "return expr" => "{ #r = expr; break #R; }"
        }
        return #r;
    }
    </code>

    If the method return type is void, it's a bit simpler:

    <code>
    #R: {
        B // with "return" => "break #R"
    }
    </code>

    Constructors are the same as void methods, but the required
    initial call to "this" or "super" (possibly implicit in source,
    always present in parsed AST, IIUC) needs to be preserved:

    <code>
    this_or_super(expr);
    #R: {
        B // with "return" => "break #R"
    }
    </code>

    Since initializer blocks can't do "return", we don't need to
    handle them. (Don't know why they aren't considered void methods
    which can do return. That would seem reasonable.)

    XXX this handles lambdas, yes? (TODO lambda test cases...)
*/
public class ReturnViaBreak extends TreeTranslator {
    private final TreeMaker M;
    private final Names N;
    private final Types T;
    private final Symtab S;
    private final JCCompilationUnit U;
    private final UniqueNameGenerator G;

    private final String returnVarPrefix = "r$";
    private final String returnLabelPrefix = "R$";

    public ReturnViaBreak(final Context c,
                          final JCCompilationUnit U) {
        this(TreeMaker.instance(c),
             Names.instance(c),
             Types.instance(c),
             Symtab.instance(c),
             U);
    }

    public ReturnViaBreak(final TreeMaker M,
                          final Names N,
                          final Types T,
                          final Symtab S,
                          final JCCompilationUnit U) {
        this.M = M;
        this.N = N;
        this.T = T;
        this.S = S;
        this.U = U;
        this.G = new UniqueNameGenerator(U);
    }

    // main method

    @Override
    public void visitMethodDef(final JCMethodDecl m) {
        m.body = isCtor(m) ? doCtor(m.body)
            : returnsNonVoid(m) ? doNonVoid(m.body, m)
            : doVoid(m.body);
        result = m;
    }

    private JCBlock doCtor(final JCBlock b) {
        // preserve initial "this(expr)" or "super(expr)"...
        final JCStatement init = b.stats.head;
        final List<JCStatement> rest = b.stats.tail;
        if (rest == null || rest.isEmpty())
            return b;

        // return Block(init, doVoid(Block(rest)));
        final JCBlock tmp = Block(rest);
        final JCBlock tmp2 = doVoid(tmp);
        return tmp2 != tmp
            ? Block(init, tmp2.stats)
            : b;
    }

    private JCBlock doVoid(final JCBlock b) {
        // #R: {
        //     B // with "return" => "break #R"
        // }
        final Name returnLabel = genName(returnLabelPrefix);

        final ReturnRewriter rrw = new ReturnRewriter() {
            @Override public JCStatement rewrite(final JCReturn r) {
                return Break(returnLabel);
            }
        };

        final JCBlock b2 = rrw.translate(b);
        return rrw.getCount() > 0
            ? Block(Label(returnLabel, b2))
            : b;
    }

    private JCBlock doNonVoid(final JCBlock b, final JCMethodDecl m) {
        // {
        //     TYPE #r;
        //     #R: {
        //         B // with "return expr" => "{ #r = expr; break #R; }"
        //     }
        //     return #r;
        // }
        final Name returnVar = genName(returnVarPrefix);
        final Name returnLabel = genName(returnLabelPrefix);

        final ReturnRewriter rrw = new ReturnRewriter() {
            @Override public JCStatement rewrite(final JCReturn r) {
                return Block(Assign(returnVar, r.expr),
                             Break(returnLabel));
            }
        };

        final JCBlock b2 = rrw.translate(b);
        if (rrw.getCount() == 0)
            return b;

        final JCVariableDecl returnVarDecl = VarDecl(returnVar, m.restype);
        returnVarDecl.pos = b.pos; // for javac "checkEffectivelyFinal"...

        return Block(returnVarDecl,
                     Label(returnLabel, b2),
                     Return(returnVar));
    }

    private static abstract class ReturnRewriter extends TreeTranslator {
        private int count = 0;
        public int getCount() {
            return count;
        }
        public abstract JCStatement rewrite(final JCReturn r);
        public void visitReturn(final JCReturn r) {
            result = rewrite(r);
            count++;
        }
        // don't recurse into class defs or lambdas (because control
        // doesn't flow between their bodies and enclosing context).
        @Override public void visitClassDef(final JCClassDecl tree) { result = tree; }
        @Override public void visitLambda(final JCLambda tree) { result = tree; }
        @Override public void visitNewClass(final JCNewClass tree) { result = tree; }
    }

    //
    // util (mostly TreeMaker wrappers)
    //

    private Name genName(final String prefix) {
        return N.fromString(G.genName(prefix));
    }

    private JCBlock Block(final List<JCStatement> stmts) {
        return M.Block(0L, stmts);
    }

    private JCBlock Block(final JCStatement s) {
        return Block(List.of(s));
    }

    private JCBlock Block(final JCStatement s, final List<JCStatement> stmts) {
        return Block(stmts.prepend(s));
    }

    private JCBlock Block(final JCStatement s, final JCStatement s2) {
        return Block(List.of(s, s2));
    }

    private JCBlock Block(final JCStatement s, final JCStatement s2, final JCStatement s3) {
        return Block(List.of(s, s2, s3));
    }

    private JCBlock Block(final JCBlock b, final JCStatement s) {
        return Block(List.of(b, s)); // or append 's'...
    }

    private JCBlock Block(final JCStatement s, final JCBlock b) {
        return Block(List.of(s, b)); // or prepend 's'
    }

    private JCLabeledStatement Label(final Name label, final JCStatement s) {
        return M.Labelled(label, s);
    }

    private JCBreak Break(final Name label) {
        return M.Break(label);
    }

    private JCVariableDecl VarDecl(final Name name, final JCExpression type) {
        final JCExpression NO_INIT = null;
        final JCModifiers NO_MODS = M.Modifiers(0L);
        return M.VarDef(NO_MODS, name, type, NO_INIT);
    }

    private JCStatement Assign(final Name v, final JCExpression expr) {
        return M.Exec(M.Assign(M.Ident(v), expr));
    }

    private JCReturn Return(final Name v) {
        return M.Return(M.Ident(v));
    }

    private boolean isCtor(final JCMethodDecl m) {
        if (false) return m.sym.isConstructor(); // m.sym can be null
        return "<init>".equals(m.getName());
    }

    private boolean returnsNonVoid(final JCMethodDecl m) {
        if (false) return m.sym.getReturnType().getTag() == TypeTag.VOID; // m.sym can be null
        return ReturnScanner.getInstance().doesNonVoidReturn(m);
    }

    private final <T extends JCTree> T COPY(final T t) {
        return JavacCopier.getRefsUnhandled().copy(t);
    }
}
