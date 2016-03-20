package edp.javac.proc;

import java.util.HashSet;
import java.util.Set;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;

public class UniqueNameGenerator {
    private final Set<String> names;

    public UniqueNameGenerator(final Tree tree) {
        final Set<String> tmp = new NameCollector().scan(tree, new HashSet<String>());
        if (tmp == null) throw new RuntimeException("unexpected null from scan"); // stub file (TreeScanner)? PolyNull??
        this.names = tmp;
    }

    /** Generates a unique name of form <prefix><n> for some
        non-negative integer <n>. */
    public String genName(final String prefix) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final String ans = prefix + i;
            if (names.add(ans))
                return ans;
        }
        throw new RuntimeException("failed to generate unique name with prefix: " + prefix);
    }

    static class NameCollector extends TreeScanner<Set<String>,Set<String>> {
        @Override
        public Set<String> visitIdentifier(final IdentifierTree node, final Set<String> ans) {
            ans.add(String.valueOf(node.getName()));
            return ans;
        }
        @Override
        public Set<String> visitLabeledStatement(final LabeledStatementTree node, final Set<String> ans) {
            ans.add(String.valueOf(node.getLabel()));
            return ans;
        }
        @Override
        public Set<String> reduce(final Set<String> a, final Set<String> b) {
            return a != null ? a : b;
        }
    }
}
