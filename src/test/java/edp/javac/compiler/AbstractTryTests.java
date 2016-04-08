package edp.javac.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import edp.misc.ExpectedFailure;

public abstract class AbstractTryTests extends junit.framework.TestCase {
    private final Compiler compiler;
    public AbstractTryTests(final Compiler compiler) {
        this.compiler = compiler;
    }

    public static void xfail(final Object... o) {
        System.out.println("XFAIL: " + Arrays.toString(o));
        new ExpectedFailure().printStackTrace();
    }

    // static stuff

    private static final String FOO = "foo";
    private static final String BAR = "bar";

    private static Method getMethod(final Class<?> c, final String name) {
        try { return c.getMethod(name); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static Object newInstance(final Class<?> c) {
        try { return c.newInstance(); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // more util stuff...

    /** returns result of invoking method "foo()" on instance (via
        nullary ctor) of class compiled and loaded by compiler from
        given source. */
    private Object run(final String source) {
        final Class<?> c = compiler.compileAndLoad("Foo", source);
        try {
            return c.getMethod("foo").invoke(c.newInstance());
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    /** verifies that invoking method "foo()" on instance (via nullary
        ctor) of class compiled and loaded by given compiler from
        given source, throws given error. */
    private void _testThrows(final Throwable expected, final String source) {
        final Class<?> c = compiler.compileAndLoad("Foo", source);
        final Method m = getMethod(c, "foo");
        final Object i = newInstance(c);
        try { m.invoke(i); }
        catch (InvocationTargetException e) {
            final Throwable actual = e.getCause();
            // Throwable doesn't override equals...
            assertEquals(expected.getClass(), actual.getClass());
            assertEquals(expected.getMessage(), actual.getMessage());
            return;
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
        fail("did not throw; expected " + expected);
    }

    //

    private void _test(final Object expected, final String source) {
        assertEquals("FAILED SOURCE: " + source, expected, run(source));
    }

    private void _test(final String source) {
        _test("foo", source);
    }

    private void _test(final Throwable e, final String source) {
        _testThrows(e, source);
    }

    public String wrap(final String s) {
        return "public class Foo {\n"
            + "    public static final String FOO = \"foo\";\n"
            + "    public static final String BAR = \"bar\";\n"
            + "    public String foo() {\n" + s + "\n"
            + "    }\n"
            + "}\n";
    }

    public String wrapAnonInner(final String s) {
        return wrap(anonInnerToString(s));
    }

    // uses the given code fragment (method def) 's' as toString()
    // impl for an anonymous inner class, and returns the result of
    // calling it.
    public String anonInnerToString(final String s) {
        return "return new Object() {\n"
            + "    public String toString() {\n" + s + "\n"
            + "    }\n"
            + "}.toString();\n";
    }

    // different ways to wrap code fragments...

    public void _t1(final String s) { _test(wrap(s)); }
    public void _t2(final String s) { _test(wrapAnonInner(s)); }

    public void _t1(final String s, final Throwable e) { _test(e, wrap(s)); }
    public void _t2(final String s, final Throwable e) { _test(e, wrapAnonInner(s)); }

    // main test methods. verifies that the compiled code returns
    // "foo" (in the first case), or throws a given error (in the
    // second case).

    public void _t(final String s) {
        _t1(s);
        _t2(s);
    }

    public void _t(final String s, final Throwable e) {
        _t1(s, e);
        _t2(s, e);
    }

    // actual tests (phew)...

    public void testFoo() {
        _t("return FOO;");
        _t("final String foo = FOO; return foo;");
        _t("try {} finally {} return FOO;");
        _t("try { return FOO; } finally {}");
        _t("try {} finally { return FOO; }");
        _t("try { return FOO; } finally { return FOO; }");
        _t("try { throw new Error(); } finally { return FOO; }");
        _t("try {} finally { throw new Error(FOO); }", new Error(FOO));
    }

    public void testFooTransformed() {
        _t(""
           + "        String r$0;"
           + "        R$1: {"
           + "            {"
           + "                J0$0: try {"
           + "                    {"
           + "                        r$0 = FOO;"
           + "                        break J0$0;"
           + "                    }"
           + "                } catch (final java.lang.Throwable t$0) {"
           + "                }"
           + "                {"
           + "                    {"
           + "                        r$0 = FOO;"
           + "                        break R$1;"
           + "                    }"
           + "                }"
           + "            }"
           + "        }"
           + "        return r$0;"
           );
    }

    public void testNested() {
        final String ETF = "try {} finally {}"; // empty try-finally
        _t("try {} finally { " + ETF + " } return FOO;");
        _t("try { " + ETF + " } finally {} return FOO;");
        _t("try { " + ETF + " return FOO; } finally {}");
        _t("try { " + ETF + " } finally { return FOO; }");
        _t("try { try {} finally { return FOO; } } finally {}");
        _t("try { try { return FOO; } finally {} } finally {}");
    }

    public void testNested2() {
        _t("try {"
           + "    try {"
           + "        return \"x\";"
           + "    }"
           + "    finally {"
           + "        return null;"
           + "    }"
           + "}"
           + "finally {"
           + "    return FOO;"
           + "}");
    }

    public void testNonEmptyFinally() {
        _t("try { return FOO.toString(); } finally { System.out.println(\"yo\"); }");
    }

    public void testNonEmptyFinally2() {
        _t("try { return FOO.toString(); } finally { System.out.println(FOO); }");
    }

    public void testNonEmptyFinally3() {
        _t("try { return FOO.toString(); } finally { System.out.println(FOO.length()); }");
    }

    /** Testcase for https://github.com/edp70/javac-proc/issues/2 */
    public void testNonEmptyFinally4() {
        _t("final String ans = FOO; try { return ans.toString(); } finally { System.out.println(ans.length()); }");
    }

    public void testTryWithReturn() {
        _t("try { return \"x\"; } finally { return FOO; }");
    }

    public void testTryWithThrow() {
        _t("try { throw new Error(); } finally { return FOO; }");
    }

    public void testTryWithBreak() {
        _t("while (true) { try { break; } finally { return FOO; } }");
    }

    public void testLoopWithReturn() {
        // try with loop which returns BAR + finally which returns FOO
        _t("try { while (true) return BAR; } finally { return FOO; }");
        _t("try { do { return BAR; } while (true); } finally { return FOO; }");
        _t("try { for (int i = 0; i < 10; i++) return BAR; } finally { return FOO; }");
        _t("try { for (int i: new int[] {1,2,3}) return BAR; } finally { return FOO; }");

        // try which returns BAR + finally with loop which returns FOO
        _t("try { return BAR; } finally { while (true) return FOO; }");
        _t("try { return BAR; } finally { do { return FOO; } while (true); }");
        _t("try { return BAR; } finally { for (int i = 0; i < 10; i++) return FOO; }");
        _t("try { return BAR; } finally { for (int i: new int[] {1,2,3}) return FOO; }");
    }

    public void testLoopWithBreak() {
        _t("try { while (true) break; } finally { return FOO; }");
        _t("try { do { break; } while (true); } finally { return FOO; }");
        _t("try { for (int i = 0; i < 10;) break; } finally { return FOO; }");
        _t("try { for (int i: new int[] {1,2,3}) break; } finally { return FOO; }");
    }

    private enum Loop {
        // well, except 'switch' is not a loop; it's breakable but not
        // continuable...
        WHILE("while (true) {"),
        DO("do {",
           "} while (true);"),
        FOR("for (int $V = 0; $V <= Integer.MAX_VALUE; $V++) {"),
        FOREACH("for (final int $V: new int[] { 1, 2, 3 }) {"),
        SWITCH("switch ((int) System.currentTimeMillis()) { default: ");

        private final String begin;
        private final String end;
        private Loop(final String begin) { this(begin, "}"); }
        private Loop(final String begin, final String end) { this.begin = begin; this.end = end; }
        public String begin(final String varName) { return begin.replace("$V", varName); }
        public String end() { return end; }
    }

    public void testNestedLoopBreak() {
        for (final Loop L1: Loop.values())
            for (final Loop L2: Loop.values()) {
                _t("String ans = \"\";"
                   + L1.begin("i")
                   + "    try {"
                   +          L2.begin("j")
                   + "            ans = BAR;"
                   + "            break;"
                   +          L2.end()
                   + "        ans = FOO;"
                   + "    }"
                   + "    finally {"
                   + "        break;"
                   + "    }"
                   + L1.end()
                   + "return ans;");
            }
    }

    public void testAnonClass() {
        _t("return new Object() {"
           + "    public String toString() { return FOO; }"
           + "}.toString();");

        _t("return FOO + new Object() {"
           + "    public String toString() { return \"\"; }"
           + "};");

        _t("return new Object() {"
           + "    public String toString() { return \"\"; }"
           + "} + FOO;");
    }

    public void testTryExceptionVsFinallyError() {
        _t("try {"
           + "    try {"
           + "        throw new Exception();"
           + "    }"
           + "    finally {"
           + "        throw new Error(FOO);"
           + "    }"
           + "}"
           + "catch (Error e) {"
           + "    return e.getMessage();"
           + "}");
    }

    public void testClassDeclInMethod() {
        _t("try {"
           + "    class C extends Error {"
           + "        public C(final String s) { super(s); }"
           + "    }"
           + "    try {"
           + "        throw new Exception();"
           + "    }"
           + "    finally {"
           + "        throw new C(FOO);"
           + "    }"
           + "}"
           + "catch (Error e) {"
           + "    return e.getMessage();"
           + "}");
    }

    // other tests...

    public void test1a() {
        _test(1, "public class Foo { public int foo() { return 1; }}");
    }
    public void test1b() {
        _test(1, "public class Foo { public int foo() { try { return 1; } finally {}}}");
    }
    public void test1c() {
        _test(1, "public class Foo { public int foo() { while (true) { try { break; } finally { return 1; }}}}");
    }

    public void test1cTransformed() {
        _test(1, ""
              + "public class Foo {"
              + "    public int foo() {"
              + "        int r$0;"
              + "        R$1: {"
              + "            while (true) {"
              + "                {"
              + "                    J1$0: J0$0: try {"
              + "                        break J1$0;"
              + "                    } catch (final java.lang.Throwable t$0) {"
              + "                    }"
              + "                    {"
              + "                        {"
              + "                            r$0 = 1;"
              + "                            break R$1;"
              + "                        }"
              + "                    }"
              + "                }"
              + "            }"
              + "        }"
              + "        return r$0;"
              + "    }"
              + "}"
              );
    }

    public void testCtorEtc() {
        _test(1, "public class Foo {"
              + "     private int ans = Integer.MIN_VALUE;"
              + "     public Foo() { this(-1); }"
              + "     public Foo(final int x) { ans = x; }"
              + "     public int foo() {" // ans = -1
              + "         try {"
              + "             if (true) while (true) {"
              + "                 try {"
              + "                     break;"
              + "                 }"
              + "                 finally {"
              + "                     return ans += 1;" // ans = 0
              + "                 }"
              + "             }"
              + "             ans = Integer.MAX_VALUE;"
              + "         }"
              + "         finally {"
              + "             return ans += 1;" // ans = 1
              + "         }"
              + "     }"
              + " }");
    }

    public void testInnerClass() {
        _test("public class Foo {"
              + "    private class Bar {"
              + "        private final String ans = \"foo\";"
              + "        public String getFoo() { return ans; }"
              + "    }"
              + "    public String foo() {"
              + "        return new Bar().getFoo();"
              + "    }"
              + "}");
    }

    public void testInnerAndAnonymousClass() {
        _test("public class Foo {"
              + "    private class Bar {"
              + "        private final Object OBJ = new Object() {"
              + "            public String toString() { return \"foo\"; }"
              + "        };"
              + "        public String getFoo() { return OBJ.toString(); }"
              + "    }"
              + "    public String foo() {"
              + "        return new Bar().getFoo();"
              + "    }"
              + "}");
    }

    // I was surprised to learn that javac uses SOURCE POSITION to
    // determine whether something is declared outside an inner class!
    // In the following method from CaptureAnalyzer in Flow.java, it's
    // the "sym.pos < currentTree.getStartPosition()" check:
    //
    //         @SuppressWarnings("fallthrough")
    //         void checkEffectivelyFinal(DiagnosticPosition pos, VarSymbol sym) {
    //             if (currentTree != null &&
    //                     sym.owner.kind == MTH &&
    //                     sym.pos < currentTree.getStartPosition()) {
    //                 switch (currentTree.getTag()) {
    //                     case CLASSDEF:
    //                         if (!allowEffectivelyFinalInInnerClasses) {
    //                             if ((sym.flags() & FINAL) == 0) {
    //                                 reportInnerClsNeedsFinalError(pos, sym);
    //                             }
    //                             break;
    //                         }
    //                     case LAMBDA:
    //                         if ((sym.flags() & (EFFECTIVELY_FINAL | FINAL)) == 0) {
    //                            reportEffectivelyFinalError(pos, sym);
    //                         }
    //                 }
    //             }
    //         }
    //
    // The following test case failed until I adjusted the source
    // position of a generated variable (in ReturnViaBreak.java).
    public void testAnonymousClass() {
        _test("public class Foo {"
              + "    private final Object OBJ = new Object() {"
              + "        public String toString() { return \"foo\"; }"
              + "    };"
              + "    public String foo() {"
              + "        return OBJ.toString();"
              + "    }"
              + "}");
    }

    public void testAnonymousClassTransformed() {
        _test("public class Foo {"
              + "    private final Object OBJ = new Object() {"
              + "        public String toString() {"
              + "            String r$0;"
              + "            R$1: {"
              + "                {"
              + "                    r$0 = \"foo\";"
              + "                    break R$1;"
              + "                }"
              + "            }"
              + "            return r$0;"
              + "        }"
              + "    };"
              + "    public String foo() {"
              + "        String r$1;"
              + "        R$2: {"
              + "            {"
              + "                r$1 = OBJ.toString();"
              + "               break R$2;"
              + "            }"
              + "        }"
              + "        return r$1;"
              + "    }"
              + "}");
    }
}
