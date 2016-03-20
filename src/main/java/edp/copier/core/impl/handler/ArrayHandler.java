package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;

import edp.copier.core.api.Copier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class ArrayHandler implements Handler {
    // singleton
    private static final ArrayHandler INSTANCE = new ArrayHandler();
    public static ArrayHandler getInstance() { return INSTANCE; }
    private ArrayHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t.getClass().isArray()
            ? copyArray(t, env.getStore(), env.getCopier())
            : null;
    }

    private <T extends @NonNull Object> T copyArray(final T t, final Store store, final Copier copier) {
        final Class<?> c = t.getClass().getComponentType();
        if (c == null) throw new RuntimeException("unexpected null component type");
        final int n = Array.getLength(t);
        final T ans = store.put(t, newArray(t, c, n));
        for (int i = 0; i < n; i++) {
            final Object o = Array.get(t, i);
            if (o != null)
                Array.set(ans, i, copier.copy(o));
        }
        return ans;
    }

    @SuppressWarnings("unchecked")
    private <T extends @NonNull Object> T newArray(final T t, final Class<?> c, final int n) {
        assert c == t.getClass().getComponentType();
        return (T) Array.newInstance(c, n);
    }
}
