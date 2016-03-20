package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class IntegerHandler implements Handler {
    // singleton
    private static final IntegerHandler INSTANCE = new IntegerHandler();
    public static IntegerHandler getInstance() { return INSTANCE; }
    private IntegerHandler() {}

    @SuppressWarnings("unchecked")
    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof Integer
            ? (T) copyInteger((Integer) t, env.getStore())
            : null;
    }

    private Integer copyInteger(final Integer t, final Store store) {
        return store.put(t, t == Integer.valueOf(t) ? t : new Integer(t));
    }
}
