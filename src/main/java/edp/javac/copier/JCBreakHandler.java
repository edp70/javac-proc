package edp.javac.copier;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

// unsupported API
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.util.Name;

import edp.copier.core.api.FieldCopier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

/** Copier handler for javac's JCBreak class. Special-cases the
    'target' field, because it should be copied by reference. */
public class JCBreakHandler implements Handler {
    // singleton
    private static final JCBreakHandler INSTANCE = new JCBreakHandler();
    public static JCBreakHandler getInstance() { return INSTANCE; }
    private JCBreakHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof JCBreak
            ? copyBreak(t, env)
            : null;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public <T extends @NonNull Object> T copyBreak(final T t, final Handler.Env env) {
        final JCBreak orig = (JCBreak) t;

        final Name label = orig.label != null ? env.copy(orig.label) : null;
        final JCTree target = orig.target != null ? refTarget(orig.target, env) : null;

        final JCBreak ans = (JCBreak) orig.clone();
        // XXX why does Checker think these fields are NonNull?
        if (label != null)
        ans.label = label;
        if (target != null)
        ans.target = target;

        return (T) env.getStore().put(orig, ans);
    }

    private JCTree refTarget(final JCTree target, final Env env) {
        // if we've already copied the target, return a reference to
        // the copy.
        {
            final JCTree ans = env.getStore().get(target);
            if (ans != null) return ans;
        }

        // otherwise, just copy the reference
        return target;
    }
}
