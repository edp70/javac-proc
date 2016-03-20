package edp.javac.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public class OutputManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Loader loader;

    protected OutputManager(final JavaFileManager fileManager,
                            final Loader loader) {
        super(fileManager);
        this.loader = loader;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(final JavaFileManager.Location location,
                                               final String className,
                                               final JavaFileObject.Kind kind,
                                               final FileObject sibling) {
        final CompiledCode code = CompiledCode.create(className);
        loader.setCode(code);
        return code;
    }

    @Override
    public ClassLoader getClassLoader(final JavaFileManager.Location location) {
        return loader;
    }
}
