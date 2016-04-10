package edp.javac.proc;

import org.checkerframework.checker.nullness.NullnessChecker;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

import static edp.javac.compiler.AbstractTryTests.xfail;
import static edp.javac.compiler.AbstractTryTests.xpass;

public class FooTest extends junit.framework.TestCase {
    public void testFoo() {
        class T extends AbstractTryTests {
            T() {
                super(CompilerImpl.getDefault()
                      .withProcessor(TreePrinter1.class)
                      .withProcessor(ReturnViaBreak1.class)
                      .withProcessor(FinallyRemover2.class)
                      .withProcessor(TreePrinter2.class)
                      .withProcessor(TreeDumper2.class)
                      .withProcessor(NullnessChecker.class)
                      );
            }
        }
        try {
        new T().test1c();
        } catch (Throwable xfail) { xfail(xfail); return; }
        xpass();
    }

    public void testBar() {
        class T extends AbstractTryTests {
            T() {
                super(CompilerImpl.getDefault()
                      .withProcessor(TreePrinter2.class)
                      .withProcessor(TreeDumper2.class)
                      .withProcessor(NullnessChecker.class)
                      );
            }
        }
        try {
        new T().test1cTransformed();
        } catch (Throwable xfail) { xfail(xfail); return; }
        xpass();
    }
}
