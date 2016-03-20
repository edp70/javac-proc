package edp.copier.core.impl.handler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import edp.copier.core.api.Handler;
import edp.copier.core.api.Store;

public class CharacterHandler implements Handler {
    // singleton
    private static final CharacterHandler INSTANCE = new CharacterHandler();
    public static CharacterHandler getInstance() { return INSTANCE; }
    private CharacterHandler() {}

    @SuppressWarnings("unchecked")
    public <T extends @NonNull Object> @Nullable T copy(final T t, final Handler.Env env) {
        return t instanceof Character
        ? (T) copyCharacter((Character) t, env.getStore())
            : null;
    }

    private Character copyCharacter(final Character t, final Store store) {
        return store.put(t, t == Character.valueOf(t) ? t : new Character(t));
    }
}
