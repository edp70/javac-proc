package edp.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

// XXX why not extend PrintStream?
public class StringPrintStream {
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final PrintStream ps = new PrintStream(baos);

    public PrintStream getPrintStream() {
        return ps;
    }

    public String toString() {
        return new String(baos.toByteArray());
    }

    public void close() {
        try {
            try { ps.close(); }
            finally { baos.close(); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
