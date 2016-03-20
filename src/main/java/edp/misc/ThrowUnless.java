package edp.misc;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

public class ThrowUnless {
    /** Returns 't' if non-null, otherwise throws IllegalArgumentException. */
    public static <T> @NonNull T NON_NULL(final @Nullable T t, final String description) {
        if (t != null) return t;
        throw new IllegalArgumentException("null " + description);
    }

    /** Returns 't' if non-empty, otherwise throws IllegalArgumentException. */
    public static <T extends Collection<?>> T NON_EMPTY(final T t, final String description) {
        if (!t.isEmpty()) return t;
        throw new IllegalArgumentException("empty " + description);
    }
}
