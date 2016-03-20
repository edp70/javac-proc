package edp.javac.proc;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import javax.lang.model.element.ElementKind;

// supported API
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

// unsupported API
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

// local
import edp.javac.copier.JavacCopier;

/** "Desugars" try/catch/finally to just try/catch.
    NB depends on ReturnViaBreak being done first. */
public class FinallyRemover extends TreeTranslator {
    private final TreeMaker M;
    private final Names N;
    private final Symtab S;
    private final JCCompilationUnit U;
    private final UniqueNameGenerator G;

    public FinallyRemover(final Context c,
                          final JCCompilationUnit U) {
        this(TreeMaker.instance(c),
             Names.instance(c),
             Symtab.instance(c),
             U);
    }

    public FinallyRemover(final TreeMaker M,
                          final Names N,
                          final Symtab S,
                          final JCCompilationUnit U) {
        this.M = M;
        this.N = N;
        this.S = S;
        this.U = U;
        this.G = new UniqueNameGenerator(U);
    }

    // impl

    public void visitTry(final JCTry orig) {
        final JCBlock _finally = orig.finalizer;
        if (_finally != null) {
            orig.finalizer = null;
            orig.finallyCanCompleteNormally = false; // ???
            result = translate(rewrite(orig, _finally));
            return;
        }
        super.visitTry(orig);
    }

    private JCTree rewrite(final JCTry orig, final JCBlock _finally) {
        assert _finally != null;

        // skip empty finally block
        if (no(_finally.stats)) {
            return no(orig.catchers) && no(orig.resources)
                ? orig.body
                : orig;
        }

        // skip empty try block
        if (no(orig.body.stats) && no(orig.resources)) {
            return _finally;
        }

        // Create new try/catch (wrapping original with catch of
        // Throwable for finally)...
        final JCTry _try = makeTry(orig, makeCatch(genVar(orig), _finally));

        // Retarget break/continue within original try/catch...
        final java.util.List<Jump<?>> jumps = new JumpTranslator(N, G, M).run(orig);

        // If there were no (relevant) jumps, add copy of finally
        // block after try/catch and we're done:
        //
        // {
        //     try {
        //         try A catchers
        //     }
        //     catch (java.lang.Throwable #t) {
        //         B
        //         throw #t
        //     }
        //     B
        // }
        if (jumps.isEmpty())
            return canCompleteNormally(_try)
                ? Block(_try, copy(_finally))
                : _try;

        // Otherwise...  Call every "unique" relevant break/continue
        // statement "J1", "J2", etc. Then the translation is:
        //
        // try A catchers
        //
        // =>
        //
        // #Try: {
        //     #J1: {
        //         #J2: {
        //             try {
        //                 try A catchers // with "J1" => "break #J1" etc.
        //             }
        //             catch (java.lang.Throwable #t) {
        //                 B
        //                 throw #t;
        //             }
        //             B
        //             break #Try;
        //         } // #J2
        //         B
        //         J2
        //     } // #J1
        //     B
        //     J1
        // } // #Try
        //
        // JumpTranslator already generated labels and rewrote
        // break/continue statements as "break <label>" within the
        // original try/catch. All we need to do now is generate the
        // rest of the structure.

        final Name tryLabel = genName("Try$");

        // "core" is the middle of the above structure, ie:
        //
        // try {
        //     try A catchers // with "J1" => "break #J1" etc.
        // }
        // catch (java.lang.Throwable #t) {
        //     B
        //     throw #t;
        // }
        // B
        // break #Try;
        final JCBlock core = Block(_try,
                                   copy(_finally),
                                   Break(tryLabel));

        // recursively generate the rest.
        return Label(tryLabel,
                     gen(jumps, 0, _finally, core));
    }

    // #J1: {
    //     ...
    // }
    // B
    // J1
    private JCStatement gen(final java.util.List<Jump<?>> jumps,
                            final int i,
                            final JCBlock _finally,
                            final JCBlock core) {
        return i < jumps.size()
            ? Block(Label(jumps.get(i).getTarget(),
                          gen(jumps, i + 1, _finally, core)),
                    copy(_finally),
                    jumps.get(i).getJump())
            : core;
    }

    private JCTry makeTry(final JCTry orig, final JCCatch _catch) {
        if (no(orig.catchers))
            // try { A } ...
            return Try(orig.resources,
                       orig.body,
                       list(_catch));

        // try { try { A } catchers } ...
        return Try(orig,
                   list(_catch));
    }

    // catch (java.lang.Throwable #t) {
    //     B
    //     throw #t;
    // }
    private JCCatch makeCatch(final VarSymbol t, final JCBlock b) {
        return canCompleteNormally(b)
            ? Catch(t, Block(b, Throw(t)))
            : Catch(t, b);
    }

    //
    // util (mostly TreeMaker wrappers)
    //

    private static final String GENERATED_PREFIX = "t$";

    private Name genName(final String prefix) {
        return N.fromString(G.genName(prefix));
    }

    private VarSymbol genVar(final JCTree where) {
        // note: marking generated variable as synthetic caused
        // "symbol not found" compile error when running before
        // ANALYZE phase of javac.
        return new VarSymbol(Flags.PARAMETER | Flags.FINAL,
                             genName(GENERATED_PREFIX),
                             S.throwableType,
                             findEnclosingMethod(where).sym);
    }

    private JCMethodDecl findEnclosingMethod(final JCTree t) {
        for (TreePath p = TreePath.getPath(U, t); p != null; p = p.getParentPath()) {
            final Tree node = p.getLeaf();
            if (node instanceof JCMethodDecl) // what about init blocks... (anything else?)
                return (JCMethodDecl) node;
        }
        throw new RuntimeException("failed to find enclosing method for AST node: " + t);
    }

    private boolean canCompleteNormally(final JCStatement stmt) {
        // need Flow for this... but might as well handle some simple
        // cases?!
        if (true) return true; // TODO make this configurable
        return !endsWithReturnOrThrow(stmt);
    }
    private boolean endsWithReturnOrThrow(final JCStatement stmt) {
        return stmt instanceof JCReturn
            || stmt instanceof JCThrow
            || (stmt instanceof JCBlock && blockEndsWithReturnOrThrow((JCBlock) stmt));
    }
    private boolean blockEndsWithReturnOrThrow(final JCBlock block) {
        final JCStatement stmt = block.stats.last();
        return stmt != null ? endsWithReturnOrThrow(stmt) : false;
    }

    // lacking real 'canCompleteNormally', need this when running
    // before javac "ANALYZE" phase...
    private JCStatement IfTrue(final JCStatement stmt) {
        final JCStatement NO_ELSE = null;
        return M.If(M.Literal(Boolean.TRUE), stmt, NO_ELSE);
    }

    private JCBlock Block(final List<JCStatement> stmts) {
        return M.Block(0L, stmts);
    }

    private JCBlock Block(final JCStatement s) {
        return Block(List.of(s));
    }

    private JCBlock Block(final JCStatement s, final JCStatement s2) {
        return Block(List.of(IfTrue(s), s2));
    }

    private JCBlock Block(final JCBlock b, final JCStatement s) {
        return Block(List.of(IfTrue(b), s)); // or append 's'...
    }

    private JCBlock Block(final JCStatement s, final JCBlock b) {
        return Block(List.of(IfTrue(s), b));
    }

    private JCBlock Block(final JCStatement s, final JCBlock b, final JCStatement s2) {
        return Block(List.of(IfTrue(s), IfTrue(b), s2));
    }

    private JCLabeledStatement Label(final Name label, final JCStatement s) {
        return M.Labelled(label, s);
    }

    private JCBreak Break(final Name label, final JCTree target) {
        final JCBreak ans = M.Break(label);
        ans.target = target;
        return ans;
    }

    private JCBreak Break() {
        final Name NO_LABEL = null;
        return M.Break(NO_LABEL);
    }

    private JCBreak Break(final Name label) {
        return M.Break(label);
    }

    private JCBreak Break(final JCTree target) {
        final JCBreak ans = Break();
        ans.target = target;
        return ans;
    }

    private JCTry Try(final @Nullable List<JCTree> resources, final JCBlock body, final List<JCCatch> catchers) {
        final JCBlock NO_FINALLY = null;
        return M.Try(resources, body, catchers, NO_FINALLY);
    }

    private JCTry Try(final JCStatement body, final List<JCCatch> catchers) {
        return body instanceof JCBlock
            ? Try(List.nil(), (JCBlock) body, catchers)
            : Try(List.nil(), Block(body), catchers);
    }

    private JCCatch Catch(final VarSymbol t, final JCBlock block) {
        t.setData(ElementKind.EXCEPTION_PARAMETER);
        return M.Catch(M.VarDef(t, null), block);
    }

    private JCThrow Throw(final VarSymbol t) {
        return M.Throw(M.Ident(t));
    }

    // static util

    private static boolean no(final @Nullable List<?> list) {
        return list == null || list.isEmpty();
    }

    private static List<JCCatch> list(final JCCatch t) {
        return List.of(t);
    }

    private static List<JCStatement> list(final JCStatement s1, final JCStatement s2) {
        return List.of(s1, s2);
    }

    private static <T extends JCTree> T copy(final T t) {
        return JavacCopier.getRefsUnhandled().copy(t);
    }
}
