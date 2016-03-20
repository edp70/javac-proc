package edp.javac.proc;

// supported API
import com.sun.source.tree.Tree;

// unsupported API
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

/** Processor which runs a TreeTranslator after javac's "ANALYZE"
    phase.

    NB subclass needs to provide the annotations:

    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_8)
*/
public abstract class AbstractTranslator2 extends AbstractProcessor2 {
    public abstract TreeTranslator createTranslator(Context context, JCCompilationUnit ast);

    @Override
    public void process(final JCCompilationUnit tree) {
        createTranslator(getContext(), tree).translate(tree);
    }

    private Context getContext() {
        final JavacProcessingEnvironment javacEnv = (JavacProcessingEnvironment) processingEnv;
        return javacEnv.getContext();
    }
}
