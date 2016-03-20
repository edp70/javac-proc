package edp.javac.proc;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

// supported API
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

// unsupported API
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

/** Processor which runs after javac's "ANALYZE" phase.

    NB subclass needs to provide the annotations:

    @SupportedAnnotationTypes("*")
    @SupportedSourceVersion(SourceVersion.RELEASE_8)
*/
public abstract class AbstractProcessor2 extends AbstractProcessor {
    @Override
    public void init(final ProcessingEnvironment env) {
        super.init(env);
        JavacTask.instance(env).addTaskListener(new Listener());
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annos, final RoundEnvironment env) {
        return false;
    }

    private class Listener implements TaskListener {
        public void started(final TaskEvent e) {
            if (e.getKind() == TaskEvent.Kind.GENERATE) {
                processingOver();
            }
        }
        public void finished(final TaskEvent e) {
            if (e.getKind() == TaskEvent.Kind.ANALYZE) {
                process((JCCompilationUnit) e.getCompilationUnit());
            }
        }
    }

    public abstract void process(JCCompilationUnit u);

    public void processingOver() {}
}
