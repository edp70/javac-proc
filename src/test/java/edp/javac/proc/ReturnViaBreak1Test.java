package edp.javac.proc;

// (main)
import edp.javac.compiler.CompilerImpl;

// (test)
import edp.javac.compiler.AbstractTryTests;

public class ReturnViaBreak1Test extends AbstractTryTests {
    public ReturnViaBreak1Test() {
        super(CompilerImpl.getDefault()
              .withProcessor(ReturnViaBreak1.class));
    }
}
