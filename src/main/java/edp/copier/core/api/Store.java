package edp.copier.core.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Store {
    /** Adds mapping from 'src' to 'tgt' to this store. Returns 'tgt'. */
    <T extends @NonNull Object> T put(T src, T tgt);

    /** Returns mapping for 't', null if none. */
    <T extends @NonNull Object> @Nullable T get(T t);
}
