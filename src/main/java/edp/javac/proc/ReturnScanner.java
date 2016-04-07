package edp.javac.proc;

import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.TryTree;
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

    /** Returns 'return' statements (directly contained within the
        given method, ie not including any other nested methods)
        within a 'try' (or 'catch') which has a 'finally'.

        For example, given the following "foo()" method, this method
        returns a list of just the "return 1" statement. ("return 0"
        is not within a 'try' statement; "return 2" is within a 'try'
        but it does not have a 'finally'; "return 3" is within a 'try'
        which has a 'finally', but is not directly contained within
        the "foo()" method - it is part of the "hashCode()" method of
        an anonymous inner class; finally, "return 4" is within a
        'finally', not a 'try' or 'catch'.)

        <code>
        int foo() {
            if (true) return 0;
            try {
                if (true) try { return 1; } finally {}
                if (true) return 2;
            }
            catch (Error e) {
                return new Object() {
                    public int hashCode() {
                        try { return 3; } finally {}
                    }
                }.hashCode();
            }
            try {} finally { return 4; }
        }
        </code>
    */
    public List<ReturnTree> findReturnViaFinally(final MethodTree t) {
        final List<ReturnTree> ans = new ArrayList<>();

        new TreeScanner<Void,Void>() {
            private boolean add = false;

            @Override
            public Void visitTry(final TryTree t, final Void p) {
                final boolean prevAdd = add;
                if (t.getFinallyBlock() != null) add = true;
                scan(t.getResources(), p);
                scan(t.getBlock(), p);
                scan(t.getCatches(), p);
                add = prevAdd;
                scan(t.getFinallyBlock(), p);
                return null;
            }

            @Override
            public Void visitReturn(final ReturnTree t, final Void p) {
                if (add) ans.add(t);
                return super.visitReturn(t, p);
            }

            // don't recurse into other method defs, lambda, etc.
            @Override public Void visitClass(final ClassTree t, final Void p) { return null; }
            @Override public Void visitLambdaExpression(final LambdaExpressionTree t, final Void p) { return null; }
            @Override public Void visitMethod(final MethodTree t, final Void p) { return null; }
            @Override public Void visitNewClass(final NewClassTree t, final Void p) { return null; }

        }.scan(t.getBody(), null);

        return ans;
    }
}
