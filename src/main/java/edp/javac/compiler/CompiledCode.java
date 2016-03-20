package edp.javac.compiler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class CompiledCode extends SimpleJavaFileObject {
    // pseudo-ctor (checked exceptions from super ctor, blegh)
    public static CompiledCode create(final String className) {
        try { return new CompiledCode(className); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    //

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public CompiledCode(final String className) throws Exception {
        super(new URI(className), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
        return baos;
    }

    public byte[] getByteCode() {
        return baos.toByteArray();
    }
}
