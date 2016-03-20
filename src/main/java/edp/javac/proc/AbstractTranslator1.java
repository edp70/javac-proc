package edp.javac.proc;

// unsupported API
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/** Processor which runs a TreeTranslator before javac's "ANALYZE"
    phase.

    NB subclass needs to provide the annotations:

    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_8)
*/
public abstract class AbstractTranslator1 extends AbstractProcessor1 {
    public abstract TreeTranslator createTranslator(Context context, JCCompilationUnit ast);

    @Override
    public void process(final JCCompilationUnit top, final JCTree tree) {
        createTranslator(getContext(), top).translate(tree);
    }
}
