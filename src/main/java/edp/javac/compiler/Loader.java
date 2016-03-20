package edp.javac.compiler;

import java.util.HashMap;
import java.util.Map;

import static edp.misc.ThrowUnless.NON_NULL;

public class Loader extends ClassLoader {
    // maps class name to bytecode
    private final Map<String,CompiledCode> code = new HashMap<>();

    public Loader() {
        this(NON_NULL(getSystemClassLoader(), "getSystemClassLoader() result"));
    }

    public Loader(final ClassLoader parent) {
        super(parent);
    }

    public void setCode(final CompiledCode cc) {
        code.put(cc.getName(), cc);
    }

    public CompiledCode getCode(final String name) {
        return NON_NULL(code.get(name), "code for name " + name);
    }

    public Map<String,Class<?>> loadAll() {
        final Map<String,Class<?>> ans = new HashMap<>();
        try {
            for (final String name: code.keySet())
                ans.put(name, loadClass(name));
        }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
        return ans;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final CompiledCode cc = code.get(name);
        return cc != null ? def(name, cc.getByteCode()) : super.findClass(name);
    }

    private Class<?> def(final String name, final byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
