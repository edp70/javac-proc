package edp.javac.proc;

import org.checkerframework.checker.nullness.NullnessChecker;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

public class FullCheckerTest extends AbstractTryTests {
    public FullCheckerTest() {
        super(CompilerImpl
              .getDefault()
              .withProcessor(FinallyRemover1.class)
              .withProcessor(IfTrueRemover2.class)
              .withProcessor(NullnessChecker.class));
    }

    /*
    @Override
    public void test1c() {
        try { super.test1c(); }
        catch (Throwable xfail) { xfail(xfail); }
    }

    @Override
    public void testTryWithBreak() {
        try { super.testTryWithBreak(); }
        catch (Throwable xfail) { xfail(xfail); }
    }
    */
}
