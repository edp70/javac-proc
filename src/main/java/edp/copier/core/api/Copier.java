package edp.copier.core.api;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface Copier {
    <T extends @NonNull Object> T copy(T t);
}
