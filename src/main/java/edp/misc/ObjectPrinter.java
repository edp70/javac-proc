package edp.misc;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.io.PrintStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: print static class fields ONCE (ProcessingEnv is only static...)
// TODO: print String values, at least the first bit, escaping weird stuff...
//       eg 1000 chars of "x" could be: <6> = (String, len 1000) "xxxxxxx"...
//       along similar lines: <7> = (byte[], len 1000) { 1, 2, 3, 4, ... }
//       heck, what about recursing into Object[]...
// TODO: limit on total number of objects ("length", as opposed to "depth")?
public class ObjectPrinter {
    private static final PrintStream DEFAULT_OUT = System.out;
    private static final int DEFAULT_MAX_DEPTH = 10;
    private static final Set<Class<?>> DEFAULT_ELIDING = SET();

    public static final ObjectPrinter DEFAULT = new ObjectPrinter(DEFAULT_OUT, DEFAULT_MAX_DEPTH, DEFAULT_ELIDING);
    public static ObjectPrinter getDefault() { return DEFAULT; }

    private static <T> Set<T> SET() {
        return Collections.emptySet();
    }
    private static <T> Set<T> SET(final Set<T> set) {
        return new HashSet<>(set);
    }
    private static <T> Set<T> SET(final Set<T> set, final T t) {
        final Set<T> ans = SET(set); ans.add(t); return ans;
    }
    private static <T> Set<T> SET(final Set<T> set, final Set<T> ts) {
        final Set<T> ans = SET(set); ans.addAll(ts); return ans;
    }

    // fields

    private final PrintStream out;
    private final int maxDepth;
    private final Set<Class<?>> eliding; // don't print fields in these classes

    // ctors

    public ObjectPrinter(final int maxDepth) {
        this(System.out, maxDepth);
    }

    public ObjectPrinter(final PrintStream out) {
        this(out, DEFAULT_MAX_DEPTH);
    }

    public ObjectPrinter(final PrintStream out, final int maxDepth) {
        this(out, maxDepth, SET());
    }

    public ObjectPrinter(final PrintStream out, final int maxDepth, final Set<Class<?>> eliding) {
        this.out = out;
        this.maxDepth = maxDepth; // XXX negative? zero?
        this.eliding = SET(eliding);
    }

    // pseudo-ctors

    public ObjectPrinter withOut(final PrintStream out) {
        return new ObjectPrinter(out, maxDepth, eliding);
    }

    public ObjectPrinter withMaxDepth(final int maxDepth) {
        return new ObjectPrinter(out, maxDepth, eliding);
    }

    public ObjectPrinter eliding(final Class<?> c) {
        return new ObjectPrinter(out, maxDepth, SET(eliding, c));
    }

    public ObjectPrinter eliding(final Set<Class<?>> cs) {
        return new ObjectPrinter(out, maxDepth, SET(eliding, cs));
    }

    // API

    public void print(final Object o) {
        new Printer().print(o);
    }

    // impl

    private class Printer {
        private int depth = 0;
        final Map<Object,Integer> seen = new IdentityHashMap<>(); // object -> id
        int id; // incremented for each unique object of reference type

        private void p(final Object o) {
            out.print(o);
        }
        private void pline(final Object o) {
            out.println(o);
        }
        private void indent() {
            for (int i = 0; i < depth; i++)
                p("    ");
        }

        private void printNullable(final @Nullable Object o) {
            if (o != null) {
                print(o);
            } else {
                pline("null");
            }
        }
        private void print(final Object o) {
            final Integer ref = seen.get(o);
            if (ref != null) {
                pline("<" + ref + ">");
                return;
            }
            seen.put(o, ++id);
            final Class<?> c = o.getClass();
            p("<" + id + "> = " + c + " {");
            if (depth >= maxDepth || eliding.contains(c)) {
                pline(" ... }");
                return;
            }
            depth++;
            final boolean fields = printFields(c, o);
            depth--;
            if (fields) indent();
            pline("}");
        }
        // return value is for formatting purposes. returns true iff
        // it printed at least one field (and thus a newline).
        private boolean printFields(final Class<?> c, final Object o) {
            return printClassFields(printSuperFields(c.getSuperclass(), o), c, o);
        }
        private boolean printSuperFields(final @Nullable Class<?> c, final Object o) {
            return c != null ? printFields(c, o) : false;
        }
        private boolean printClassFields(/*mutable*/ boolean ans, final Class<?> c, final Object o) {
            for (final Field f: c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) { // skip static fields...
                    if (!ans) pline("");
                    printField(f, o);
                    ans = true;
                }
            }
            return ans;
        }
        private void printField(final Field f, final Object o) {
            try {
                indent();
                // TODO consider indication of whether the field is in
                // class or superclass (or super^2 etc).
                p(f.getName() + ": ");
                f.setAccessible(true);
                final Object value = f.get(o);
                final String primitiveName = getPrimitiveName(f.getType());
                if (value != null && primitiveName != null)
                    printPrimitive(primitiveName, value);
                else
                    printNullable(value);
            }
            catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }
        private void printPrimitive(final String type, final Object o) {
            pline("(" + type + ") " + o);
        }
        private @Nullable String getPrimitiveName(final Class<?> c) {
            if (c == boolean.class) return "boolean";
            if (c == byte.class) return "byte";
            if (c == char.class) return "char";
            if (c == short.class) return "short";
            if (c == int.class) return "int";
            if (c == long.class) return "long";
            if (c == float.class) return "float";
            if (c == double.class) return "double";
            return null;
        }
    }
}
