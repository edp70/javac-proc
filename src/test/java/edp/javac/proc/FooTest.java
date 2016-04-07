package edp.javac.proc;

import org.checkerframework.checker.nullness.NullnessChecker;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

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
        new T().test1c();
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
        new T().test1cTransformed();
    }
}
