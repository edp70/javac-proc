package edp.javac.proc;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.util.TreeScanner;

public class ReturnScanner {
    // singleton
    private static final ReturnScanner INSTANCE = new ReturnScanner();
    public static ReturnScanner getInstance() { return INSTANCE; }
    private ReturnScanner() {}

    /** Returns whether given method does a non-void return.

        NB this is not the same as whether the method has a declared
        return type of "void".

        For example, returns true for the following methods:

        <code>
        int m() { return 0; }
        String m() { return null; }
        <T> T m(T t) { return t; }
        </code>

        Returns false for the following methods:

        <code>
        void m() {}
        C() {} // ctor
        int m() { throw new Error(); }
        </code>
    */
    public boolean doesNonVoidReturn(final MethodTree t) {
        try {
            SCANNER.scan(t.getBody(), null);
        }
        catch (Result ans) {
            return ans.value;
        }
        return false; // no return stmt
    }

    private static class Result extends RuntimeException {
        final boolean value;
        private Result(final boolean value) { this.value = value; }
    }

    private static final Result NON_VOID = new Result(true);
    private static final Result VOID = new Result(false);

    private static final TreeScanner<Void,Void> SCANNER =
        new TreeScanner<Void,Void>() {
            @Override
            public Void visitReturn(final ReturnTree t, final Void p) {
                throw t.getExpression() != null ? NON_VOID : VOID;
            }
            // don't recurse into other method defs, lambda, etc.
            @Override public Void visitClass(final ClassTree t, final Void p) { return null; }
            @Override public Void visitLambdaExpression(final LambdaExpressionTree t, final Void p) { return null; }
            @Override public Void visitMethod(final MethodTree t, final Void p) { return null; }
            @Override public Void visitNewClass(final NewClassTree t, final Void p) { return null; }
        };
}
