package edp.javac.proc;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

public class FinallyRemover1IfTrueRemover2Test extends AbstractTryTests {
    public FinallyRemover1IfTrueRemover2Test() {
        super(CompilerImpl
              .getDefault()
              .withProcessor(FinallyRemover1.class)
              .withProcessor(IfTrueRemover2.class));
    }
}
