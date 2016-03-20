package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

import edp.copier.core.api.Copier;
import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

/** Copies java.util.List as java.util.ArrayList. */
public class ListHandler implements Handler {
    // singleton
    private static final ListHandler INSTANCE = new ListHandler();
    public static ListHandler getInstance() { return INSTANCE; }
    private ListHandler() {}
    
    @SuppressWarnings("unchecked")
    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof List
            ? (T) copyList((List) t, env.getStore(), env.getCopier())
            : null;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private List copyList(final List t, final Store store, final Copier copier) {
        final List ans = store.put(t, new ArrayList(t.size()));
        for (final Object o: t)
            ans.add(o != null ? copier.copy(o) : null);
        return ans;
    }
}
