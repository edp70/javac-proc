package edp.javac.proc;

import org.checkerframework.checker.nullness.qual.Nullable;

// unsupported API
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.TreeTranslator;

/** Translates "if (true) S" to "S". */
public class IfTrueRemover extends TreeTranslator {
    // singleton
    private static final IfTrueRemover INSTANCE = new IfTrueRemover();
    public static IfTrueRemover getInstance() { return INSTANCE; }
    private IfTrueRemover() {}

    // impl

    public void visitIf(final JCIf _if) {
        final Boolean cond = getBooleanConstantExpressionValue(_if.cond);
        if (Boolean.TRUE.equals(cond) && _if.elsepart == null) {
            result = translate(_if.thenpart);
            return;
        }
        super.visitIf(_if);
    }

    /** @return null if 'x' is not a boolean constant expression (per
        JLS 15.28 [*]), otherwise return its value (true or false).

        XXX for now, only handles literals; returns null for all other
        constant expressions.

        [*] https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.28
    */
    private @Nullable Boolean getBooleanConstantExpressionValue(final JCExpression x) {
        return x instanceof JCParens
            ? getBooleanConstantExpressionValue(((JCParens) x).expr)
            : x instanceof JCLiteral
            ? getBooleanLiteralValue((JCLiteral) x)
            : null;
    }

    /** @return null if 'x' is not a boolean literal, otherwise return
        its value (true or false). */
    private @Nullable Boolean getBooleanLiteralValue(final JCLiteral x) {
        return x.typetag == TypeTag.BOOLEAN
            ? (Boolean) x.getValue()
            : null;
    }
}
