package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;

import edp.copier.core.api.FieldCopier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class CloneableHandler implements Handler {
    // singleton
    private static final CloneableHandler INSTANCE = new CloneableHandler();
    public static CloneableHandler getInstance() { return INSTANCE; }
    private CloneableHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof Cloneable
            ? copyCloneable(t, env.getStore(), env.getFieldCopier())
            : null;
    }

    public <T extends @NonNull Object> T copyCloneable(final T t, final Store store, final FieldCopier fieldCopier) {
        return fieldCopier.copy(t, store.put(t, clone(t)));
    }

    @SuppressWarnings("unchecked")
    public <T extends @NonNull Object> T clone(final T t) {
        try {
            final Method m = t.getClass().getMethod("clone");
            m.setAccessible(true);
            final @Nullable T ans = (@Nullable T) m.invoke(t);
            if (ans != null) return ans;
            throw new RuntimeException("unexpected null result from clone");
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
