package edp.javac.proc;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

public class FinallyRemover2Test extends AbstractTryTests {
    public FinallyRemover2Test() {
        super(CompilerImpl
              .getDefault()
              .withProcessor(ReturnViaBreak1.class)
              .withProcessor(FinallyRemover2.class));
    }
}
