package edp.copier.core.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edp.copier.core.api.Copier;
import edp.copier.core.api.FieldCopier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;
import edp.copier.core.impl.handler.*;

public class DeepCopier implements Copier {
    // singleton default
    private static final DeepCopier DEFAULT = new DeepCopier();
    public static DeepCopier getDefault() { return DEFAULT; }

    //

    private final List<Handler> handlers;

    private DeepCopier() {
        this(Arrays.asList(RefHandler.getInstance(),
                           PrimitiveHandler.getInstance(),
                           ArrayHandler.getInstance(),
                           CloneableHandler.getInstance(),
                           NullaryCtorHandler.getInstance(),
                           ValueCtorHandler.getInstance(),
                           ListHandler.getInstance(),
                           IntegerHandler.getInstance(),
                           LongHandler.getInstance(),
                           CharacterHandler.getInstance()));
    }

    public DeepCopier(final List<Handler> handlers) {
        this.handlers = handlers; // XXX ref...
    }

    public DeepCopier withHandler(final Handler handler) {
        return new DeepCopier(append(handlers, handler));
    }
    private static <T> List<T> append(final List<T> ts, final T t) {
        final List<T> ans = new ArrayList<>(ts);
        ans.add(t);
        return ans;
    }

    @Override
    public <T extends @NonNull Object> T copy(final T t) {
        return new Copy().copy(t);
    }

    // nested class to manage per-'copy'-invocation state

    private class Copy implements Handler.Env {
        private final Store store = new SimpleStore();
        private final FieldCopier fieldCopier =
            new AbstractFieldCopier() {
                /*
                @Override
                public <T> T copy(final T t) {
                    return t != null
                        ? Copy.this.copy(t)
                        : null;
                }

                // XXX I'm confused why "@Nullable" works / seems
                // necessary on the "copy" method here. Without it
                // (ie, as above), I get the following (confusing!)
                // Checker diagnostic:
                //
                // [...] error: [return.type.incompatible] incompatible types in return.
                //                         ? Copy.this.copy(t)
                //                         ^
                //   found   : T extends @Initialized @Nullable Object
                //   required: T extends @Initialized @Nullable Object
                // 1 error
                */
                @Override
                public <T> @Nullable T copy(final @Nullable T t) {
                    return t != null
                        ? Copy.this.copy(t)
                        : null;
                }
            };

        public Store getStore() { return store; }
        public FieldCopier getFieldCopier() { return fieldCopier; }
        public Copier getCopier() { return this; }

        @Override
        public <T extends @NonNull Object> T copy(final T t) {
            for (final Handler h: handlers) {
                final T ans = h.copy(t, this);
                if (ans != null)
                    return ans;
            }
            throw new RuntimeException("no handler for " + t.getClass());
        }
    }
}
