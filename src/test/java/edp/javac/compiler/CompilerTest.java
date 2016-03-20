package edp.javac.compiler;

import java.util.List;
import java.util.Map;

// supported API
import com.sun.source.tree.Tree;

import edp.javac.printer.JavacPrinter;
import edp.misc.StringPrintStream;
import edp.misc.ObjectPrinter;

public class CompilerTest extends junit.framework.TestCase {
    private static final CompilerImpl COMPILER = CompilerImpl.getDefault();
    private static final ObjectPrinter DUMPER = JavacPrinter.getDefault();

    public void testFooCompileLoadRun() throws Exception {
        final String src = "public class Foo { public String foo() { return \"foo!\"; }}";
        final Class<?> c = COMPILER.compileAndLoad("Foo", src);
        assertEquals("foo!", (String) c.getMethod("foo").invoke(c.newInstance()));
    }

    public void testFooBarCompileLoadRun() throws Exception {
        final String src =
            "public class Foo {"
            + "    public static class Bar {"
            + "        public String toString() { return \"bar\"; }"
            + "    }"
            + "    public String toString() { return \"foo\"; }"
            + "}";

        final Map<String,Class<?>> cs = COMPILER.compileAndLoad2("Foo", src);
        assertEquals(2, cs.size());
        final Class<?> fooClass = cs.get("Foo");
        final Class<?> barClass = cs.get("Foo$Bar");
        final Object foo = fooClass.newInstance();
        final Object bar = barClass.newInstance();
        assertEquals("foo", foo.toString());
        assertEquals("bar", bar.toString());
    }

    public void testFooParse() throws Exception {
        final Tree ast;
        {
            final String src = "public class Foo { public String foo() { return \"foo!\"; }}";
            final List<Tree> tmp = COMPILER.parse(src);
            assertEquals(1, tmp.size());
            ast = tmp.get(0);

            // javac toString() impl could change, but ok for now...
            {
                final String x = "\n"
                    + "public class Foo {\n"
                    + "    \n"
                    + "    public String foo() {\n"
                    + "        return \"foo!\";\n"
                    + "    }"
                    + "\n"
                    + "}";
                assertEquals(x, ast.toString());
            }

            // cloned AST should be distinct from original
            //
            // (XXX why do this? possibly left over from early
            // experiments attempting to copy AST via clone; maybe we
            // don't care about this at all any more really.)
            final Tree ast2 = clone(ast);
            assertTrue(ast2 != ast);

            // compare dumps of AST vs cloned AST
            //
            // (XXX again, maybe this doesn't matter.)
            {
                final StringPrintStream s1 = new StringPrintStream();
                try { DUMPER.withOut(s1.getPrintStream()).print(ast); }
                finally { s1.close(); }

                final StringPrintStream s2 = new StringPrintStream();
                try { DUMPER.withOut(s2.getPrintStream()).print(ast2); }
                finally { s2.close(); }

                assertEquals(s1.toString(), s2.toString());
            }
        }

        // javac toString() produces equivalent (but not identical)
        // source code, so we should be able to compile and run it.
        //
        // note: actually, the toString() code may not always be
        // equivalent. i think anonymous inner classes get a
        // constructor with an empty name, which won't compile, for
        // example. (also, imports are missing?? or is that because
        // i'm not printing the whole compilation unit?)
        final String src = ast.toString();
        final Class<?> c = COMPILER.compileAndLoad("Foo", src);
        assertEquals("foo!", (String) c.getMethod("foo").invoke(c.newInstance()));
    }

    // util

    @SuppressWarnings("unchecked")
    static <T> T clone(final T t) {
        try {
            final T ans = (T) t.getClass().getMethod("clone").invoke(t);
            if (ans != null) return ans;
            throw new RuntimeException("unexpected null result from clone");
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
