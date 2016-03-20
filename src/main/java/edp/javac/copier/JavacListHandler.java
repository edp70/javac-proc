package edp.javac.copier;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.sun.tools.javac.util.List;

import edp.copier.core.api.FieldCopier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

/** Copier handler for javac's List class. */
public class JavacListHandler implements Handler {
    // singleton
    private static final JavacListHandler INSTANCE = new JavacListHandler();
    public static JavacListHandler getInstance() { return INSTANCE; }
    private JavacListHandler() {}

    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof List
            ? copyList(t, env.getStore(), env.getFieldCopier())
            : null;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public <T extends @NonNull Object> T copyList(final T t, final Store store, final FieldCopier fieldCopier) {
        final List list = (List) t;
        return list == List.nil()
            ? store.put(t, t) // empty list is a singleton
            : fieldCopier.copy(t, store.put(t, (T) List.of(list.head)));
    }
}
