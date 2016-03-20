package edp.javac.proc;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

public class FinallyRemover1Test extends AbstractTryTests {
    public FinallyRemover1Test() {
        super(CompilerImpl
              .getDefault()
              .withProcessor(FinallyRemover1.class));
    }
}
