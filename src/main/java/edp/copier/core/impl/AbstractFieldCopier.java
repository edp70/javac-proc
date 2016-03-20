package edp.copier.core.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.List;

import edp.copier.core.api.FieldCopier;

public abstract class AbstractFieldCopier implements FieldCopier {
    private static boolean isStaticFinal(final Field f) {
        return Modifier.isStatic(f.getModifiers())
            && Modifier.isFinal(f.getModifiers());
    }

    private Iterable<Field> addFields(final @Nullable Class<?> c, final List<Field> ans) {
        if (c == null) return ans;
        for (final Field f: c.getDeclaredFields())
            if (!isStaticFinal(f))
                ans.add(f);
        return addFields(c.getSuperclass(), ans);
    }

    // subclass can override
    public Iterable<Field> getFields(final Class<?> c) {
        return addFields(c, new ArrayList<Field>());
    }

    // subclass can (should, really) override
    public <T> T copy(final T t) {
        return t;
    }

    // FieldCopier interface (subclass can override)
    public <T extends @NonNull Object> T copy(final T src, final T tgt) {
        // NB src.getClass() might not be the same as tgt.getClass()...
        // should we at least try to DTRT if they are related? ie, copy
        // their common subset of fields...
        for (final Field f: getFields(src.getClass()))
            if (!isStaticFinal(f))
                setField(f, tgt, copy(getField(f, src)));
        return tgt;
    }

    // util (rethrow checked exceptions)

    private static void setField(final Field f, final Object o, final @Nullable Object value) {
        try {
            f.setAccessible(true);
            f.set(o, value);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static @Nullable Object getField(final Field f, final Object o) {
        try {
            f.setAccessible(true);
            return f.get(o);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
