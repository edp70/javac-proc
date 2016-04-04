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
        private final Map<Name,JCStatement> map = new HashMap<>();

        @Override
        public void visitLabelled(final JCLabeledStatement t) {
            final JCStatement prev = map.put(t.label, getLabeledStmt(t.body));
            assert prev == null;
            super.visitLabelled(t);
        }

        private JCStatement getLabeledStmt(JCStatement ans) {
            while (ans instanceof JCLabeledStatement)
                ans = ((JCLabeledStatement) ans).body;
            return ans;
        }

        @Override
        public void visitBreak(final JCBreak t) {
            if (t.label != null && t.target == null) {
                final JCStatement target = map.get(t.label);
                if (target == null)
                    throw new RuntimeException("no target found for " + t);
                t.target = target;
            }
        }

        // don't recurse into other method defs, lambda, etc.  XXX
        // what are the scoping rules for labels, anyway? can labels
        // shadow labels in higher scope? (ISTR maybe not??)
        @Override public void visitClassDef(final JCClassDecl t) {}
        @Override public void visitLambda(final JCLambda t) {}
        @Override public void visitNewClass(final JCNewClass t) {}
    }
}
