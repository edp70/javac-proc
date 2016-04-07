package edp.javac.proc;

import java.util.List;

// supported API
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;

import edp.javac.compiler.CompilerImpl;

public class ReturnScannerTest extends junit.framework.TestCase {
    private static final CompilerImpl COMPILER = CompilerImpl.getDefault();

    private List<Tree> parse(final String src) {
        return COMPILER.parse(src);
    }

    public void testFindReturnViaFinallyExample() {
        final String src = ""
            + "public class Foo {"
            + "    int foo() {"
            + "        if (true) return 0;"
            + "        try {"
            + "            if (true) try { return 1; } finally {}"
            + "            if (true) return 2;"
            + "        }"
            + "        catch (Error e) {"
            + "            return new Object() {"
            + "                public int hashCode() {"
            + "                    try { return 3; } finally {}"
            + "                }"
            + "            }.hashCode();"
            + "        }"
            + "        try {} finally { return 4; }"
            + "    }"
            + "}";

        final List<Tree> parsed = parse(src);
        assertEquals(1, parsed.size()); // single compilation unit...
        final CompilationUnitTree unit = (CompilationUnitTree) parsed.get(0);
        final List<? extends Tree> decls = unit.getTypeDecls();
        assertEquals(1, decls.size()); // single class...
        final ClassTree clss = (ClassTree) decls.get(0);
        final List<? extends Tree> members = clss.getMembers();
        assertEquals(1, members.size()); // single method...
        final MethodTree method = (MethodTree) members.get(0);

        final List<ReturnTree> ans = ReturnScanner.getInstance().findReturnViaFinally(method);
        assertEquals(1, ans.size());
        final ReturnTree r = ans.get(0); // just "return 1" should be returned
        final LiteralTree lit = (LiteralTree) r.getExpression();
        assertEquals((Integer) 1, (Integer) lit.getValue());
    }
}
