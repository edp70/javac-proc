package edp.copier.core.api;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface FieldCopier {
    /** Copy fields from <code>src</code> to <code>tgt</code> and
        return <code>tgt</code>. Ie, for some definition of "copy",
        something like this:

        <code>
        for (Field f: src.getClass().getFields())
            f.set(tgt, copy(f.get(src)));
        return tgt;
        </code>
    */
    <T extends @NonNull Object> T copy(T src, T tgt);
}
