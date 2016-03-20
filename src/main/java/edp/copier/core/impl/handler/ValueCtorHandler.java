package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;

import edp.copier.core.api.FieldCopier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class ValueCtorHandler implements Handler {
    // singleton
    private static final ValueCtorHandler INSTANCE = new ValueCtorHandler();
    public static ValueCtorHandler getInstance() { return INSTANCE; }
    private ValueCtorHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        // new T(t)
        final Constructor<? extends T> ctor = getCtor(t);
        return ctor != null
            ? copyViaCtor(t, ctor, env.getStore(), env.getFieldCopier())
            : null;
    }

    private <T extends @NonNull Object> T copyViaCtor(final T t, final Constructor<? extends T> ctor, final Store store, final FieldCopier fieldCopier) {
        return fieldCopier.copy(t, store.put(t, newInstance(ctor, t)));
    }

    // static util (casts, unchecked exceptions)

    @SuppressWarnings("unchecked")
    private static <T extends @NonNull Object> @Nullable Constructor<? extends T> getCtor(final T t) {
        final Class<? extends T> c = (Class<? extends T>) t.getClass();
        try {
            // note: does not return stuff like 'new Integer(int)'
            return c.getConstructor(c);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static <T extends @NonNull Object> T newInstance(final Constructor<? extends T> c, final T t) {
        try {
            c.setAccessible(true);
            return c.newInstance(t);
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
