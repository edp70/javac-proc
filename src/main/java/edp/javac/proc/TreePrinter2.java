package edp.javac.proc;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

// unsupported API
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TreePrinter2 extends AbstractProcessor2 {
    @Override
    public void process(final JCCompilationUnit top) {
        System.out.println("[TreePrinter2] JCCompilationUnit");
        System.out.println(top);
    }
}
