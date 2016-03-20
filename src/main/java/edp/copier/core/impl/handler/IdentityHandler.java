package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import edp.copier.core.api.Handler;

/** Just returns 't'. */
public class IdentityHandler implements Handler {
    // singleton
    private static final IdentityHandler INSTANCE = new IdentityHandler();
    public static IdentityHandler getInstance() { return INSTANCE; }
    private IdentityHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t;
    }
}
