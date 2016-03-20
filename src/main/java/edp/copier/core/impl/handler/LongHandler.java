package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class LongHandler implements Handler {
    // singleton
    private static final LongHandler INSTANCE = new LongHandler();
    public static LongHandler getInstance() { return INSTANCE; }
    private LongHandler() {}

    @SuppressWarnings("unchecked")
    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof Long
            ? (T) copyLong((Long) t, env.getStore())
            : null;
    }

    private Long copyLong(final Long t, final Store store) {
        return store.put(t, t == Long.valueOf(t) ? t : new Long(t));
    }
}
