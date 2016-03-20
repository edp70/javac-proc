package edp.copier.core.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

import edp.copier.core.api.Store;

public class SimpleStore implements Store {
    // key and value are always the same type, but can be any type
    private final Map<Object,Object> refs = new IdentityHashMap<>();

    public <T extends @NonNull Object> T put(final T src, final T tgt) {
        refs.put(src, tgt);
        return tgt;
    }

    @SuppressWarnings("unchecked")
    public <T extends @NonNull Object> @Nullable T get(final T t) {
        return (@Nullable T) refs.get(t);
    }
}
