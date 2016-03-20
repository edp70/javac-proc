package edp.javac.proc;

// unsupported API
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.Name;

/** For FinallyRemover / JumpTranslator, this class encapsulates the
    original jump (break or continue) statement and its new location,
    ie the target to which the rewritten jump in its old location
    should transfer control.

    The 'jump' and 'target' fields here correspond to "J1" and "#J1"
    as described in FinallyRemover comments. */
public abstract class Jump<J extends JCStatement> {
    protected final J jump;
    protected final Name target;
    protected Jump(final J jump, final Name target) {
        this.jump = jump;
        this.target = target;
    }
    public J getJump() { return jump; }
    public Name getTarget() { return target; }

    public static class Break extends Jump<JCBreak> {
        public Break(final JCBreak s, final Name t) { super(s, t); }
    }

    public static class Continue extends Jump<JCContinue> {
        public Continue(final JCContinue s, final Name t) { super(s, t); }
    }
}
