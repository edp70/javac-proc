package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;

import edp.copier.core.api.Handler;

/** Just records the class of every object it is asked to
    handle. Doesn't actually "handle" copying them, ie always returns
    null. */
public class ClassCollector implements Handler {
    private final Set<Class<?>> classes = new HashSet<>();

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        classes.add(t.getClass());
        return null;
    }

    public Set<Class<?>> getClasses() {
        return classes; // XXX ref to mutable
    }
}
