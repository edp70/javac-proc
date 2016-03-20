package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import edp.copier.core.api.Handler;

public class PrimitiveHandler implements Handler {
    // singleton
    private static final PrimitiveHandler INSTANCE = new PrimitiveHandler();
    public static PrimitiveHandler getInstance() { return INSTANCE; }
    private PrimitiveHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t.getClass().isPrimitive()
            ? t
            : null;
    }
}
