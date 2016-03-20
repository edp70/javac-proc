package edp.javac.compiler;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class SourceCode extends SimpleJavaFileObject {
    // pseudo-ctor (checked exceptions from super ctor, blegh)
    public static SourceCode create(final String name, final String source) {
        try { return new SourceCode(name, source); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    //

    private final String source;

    public SourceCode(final String name, final String source) throws Exception {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
        return source;
    }
}
