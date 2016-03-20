package edp.javac.printer;

// unsupported API
import com.sun.tools.javac.util.Names;

// local
import edp.misc.ObjectPrinter;

public class JavacPrinter {
    public static final ObjectPrinter DEFAULT = ObjectPrinter.getDefault()
        .withMaxDepth(20)
        .eliding(Names.class);

    public static ObjectPrinter getDefault() { return DEFAULT; }
}
