package edp.javac.proc;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

// unsupported API
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree;

import edp.javac.printer.JavacPrinter;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TreeDumper2 extends AbstractProcessor2 {
    @Override
    public void process(final JCCompilationUnit top) {
        System.out.println("[TreeDumper2] JCCompilationUnit:");
        JavacPrinter.getDefault().print(top);
    }
}
