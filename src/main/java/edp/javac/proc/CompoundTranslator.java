package edp.javac.proc;

// unsupported API
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree;

public class CompoundTranslator extends TreeTranslator {
    private final TreeTranslator[] ts;
    /* XXX avoiding Checker varargs bug...
    public CompoundTranslator(final TreeTranslator... ts) {
        this.ts = ts; // XXX ref to mutable
    }
    */
    public CompoundTranslator(final TreeTranslator t1, final TreeTranslator t2) {
        this.ts = new TreeTranslator[] { t1, t2 };
    }

    public <T extends JCTree> T translate(final T tree) {
        T ans = tree;
        for (final TreeTranslator t: ts)
            ans = t.translate(ans);
        return ans;
    }
}
