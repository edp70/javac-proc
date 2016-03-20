package edp.javac.copier;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Set;

import edp.copier.core.impl.DeepCopier;
import edp.copier.core.impl.handler.*;

public class JavacCopier extends DeepCopier {
    // adds JavacListHandler
    private JavacCopier() {
        super(Arrays.asList(RefHandler.getInstance(),
                            PrimitiveHandler.getInstance(),
                            ArrayHandler.getInstance(),
                            CloneableHandler.getInstance(),
                            JavacListHandler.getInstance(),
                            NullaryCtorHandler.getInstance(),
                            ValueCtorHandler.getInstance(),
                            ListHandler.getInstance(),
                            IntegerHandler.getInstance(),
                            LongHandler.getInstance(),
                            CharacterHandler.getInstance()));
    }

    // throw on unhandled class
    private static final DeepCopier DEFAULT = new JavacCopier();

    // reference unhandled class (ie, shallow copy instead of deep copy)
    private static final DeepCopier REFS_UNHANDLED = DEFAULT.withHandler(IdentityHandler.getInstance());

    // refs unhandled classes and prints list of them to System.out
    private static final DeepCopier LOGS_AND_REFS = new JavacCopier() {
        public <T extends @NonNull Object> T copy(final T t) {
            final ClassCollector cc = new ClassCollector();
            final IdentityHandler id = IdentityHandler.getInstance();
            try {
                return withHandler(cc).withHandler(id).copy(t);
            }
            finally {
                final Set<Class<?>> set = cc.getClasses();
                if (!set.isEmpty())
                    System.out.println("[JavacCopier] UNHANDLED CLASSES: " + set);
            }
        }
    };

    public static final DeepCopier getThrowsOnUnhandled() { return DEFAULT; }
    public static final DeepCopier getRefsUnhandled() { return REFS_UNHANDLED; }
    public static final DeepCopier getLogsAndRefsUnhandled() { return LOGS_AND_REFS; }
}
