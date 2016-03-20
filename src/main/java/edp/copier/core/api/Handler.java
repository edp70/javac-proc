package edp.copier.core.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Handler {
    interface Env extends Copier {
        Store getStore();
        FieldCopier getFieldCopier();
        Copier getCopier();
    }

    /** Returns (non-null) copy of <code>t</code>, if able to handle
        copying it; otherwise, returns null to indicate that this
        handler cannot copy it. */
    <T extends @NonNull Object> @Nullable T copy(T t, Env env);
}
