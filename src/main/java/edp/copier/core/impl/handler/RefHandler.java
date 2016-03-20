package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class RefHandler implements Handler {
    // singleton
    private static final RefHandler INSTANCE = new RefHandler();
    public static RefHandler getInstance() { return INSTANCE; }
    private RefHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return env.getStore().get(t);
    }
}
