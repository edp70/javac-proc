package edp.javac.compiler;

import java.util.Map;

public interface Compiler extends Parser {
    // TODO would be cool: public byte[] compile(Tree classTree);
    public byte[] compile(String className, String source);
    public Class<?> compileAndLoad(String className, String source);

    public Map<String,Class<?>> compileAndLoad2(String className, String source);
}
