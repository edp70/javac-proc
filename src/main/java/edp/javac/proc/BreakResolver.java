package edp.javac.proc;

import java.util.HashMap;
import java.util.Map;

// unsupported API
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Name;

/** Sets JCBreak 'target' field based on 'label'. */
public class BreakResolver {
    // singleton
    private static final BreakResolver INSTANCE = new BreakResolver();
    public static BreakResolver getInstance() { return INSTANCE; }
    private BreakResolver() {}

    // main method

    public void resolve(final JCTree t) {
        new Resolver().scan(t);
    }

    private static class Resolver extends TreeScanner {
        // JLS 14.7 specifies label scoping rules etc. In particular,
        // the scope of a label is only within the labeled statement,
        // and they are "unique" within that scope (no shadowing).
        //
        // Thus, we can use a simple map from label name to labeled
        // statement; lookup should always succeed (per label scope
        // rule) and duplicate key insert should never happen (since
        // there is no shadowing).
        private final Map<Name,JCStatement> map = new HashMap<>();

        @Override
        public void visitLabelled(final JCLabeledStatement t) {
            // Note: javac Attr.java "findJumpTarget" method has a
            // javadoc comment saying "The target of a labelled break
            // or continue is the (non-labelled) statement tree
            // referred to by the label, not the tree representing the
            // labelled statement itself." However, that appears to be
            // true (based on the method impl) only for *continue*
            // statements (where it does "unwrap" labeled statements
            // via "TreeInfo.referencedStatement" method), not for
            // *break*. Since we're only dealing with break
            // statements, we don't need to unwrap, ie we can set
            // target to "t", as opposed to "t.body[.body[...]]".
            final JCStatement prev = map.put(t.label, t);
            if (prev != null)
                throw new AssertionError("duplicate label " + t);
            super.visitLabelled(t);
        }

        @Override
        public void visitBreak(final JCBreak t) {
            if (t.label != null && t.target == null) {
                final JCStatement target = map.get(t.label);
                if (target == null)
                    throw new AssertionError("no target found for " + t);
                t.target = target;
            }
        }

        // don't recurse into other method defs, lambdas, etc.
        @Override public void visitClassDef(final JCClassDecl t) {}
        @Override public void visitLambda(final JCLambda t) {}
        @Override public void visitNewClass(final JCNewClass t) {}
    }
}
