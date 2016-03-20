package edp.javac.proc;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

// supported API
import com.sun.source.util.Trees;

// unsupported API
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Pair;

/** Processor which runs before javac's "ANALYZE" phase. Basically
    equivalent to javac's AbstractProcessor.

    NB subclass needs to provide the annotations:

    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_8)
*/
public abstract class AbstractProcessor1 extends AbstractProcessor {
    private static final boolean debug = false;

    @Override
    public boolean process(final Set<? extends TypeElement> annos, final RoundEnvironment env) {
        process(env.getRootElements());
        return false;
    }

    protected void process(final Set<? extends Element> es) {
        for (final Element e: es)
            process(e);
    }

    protected void process(final Element e) {
        if (debug) log("process element: " + e);
        process(getTop(e), getTree(e));
    }

    public abstract void process(JCCompilationUnit top, JCTree tree);

    protected JCCompilationUnit getTop(final Element e) {
        final JavacElements elements = JavacElements.instance(getContext());
        final Pair<JCTree, JCCompilationUnit> pair = elements.getTreeAndTopLevel(e, null, null);
        if (pair == null || pair.snd == null)
            throw new RuntimeException("failed to get toplevel CompilationUnit from element " + e);
        return pair.snd;
    }

    protected Context getContext() {
        final JavacProcessingEnvironment javacEnv = (JavacProcessingEnvironment) processingEnv;
        return javacEnv.getContext();
    }

    protected JCTree getTree(final Element e) {
        final Trees trees = Trees.instance(processingEnv);
        return (JCTree) trees.getTree(e);
    }

    protected void log(final Object o) {
        System.out.println("[AbstractProcessor1 " + this + "] " + o);
    }
}
