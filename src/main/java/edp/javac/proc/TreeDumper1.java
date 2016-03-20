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
public class TreeDumper1 extends AbstractProcessor1 {
    @Override
    public void process(final JCCompilationUnit top, final JCTree tree) {
        System.out.println("[TreeDumper1] JCTree:");
        JavacPrinter.getDefault().print(tree);
    }
}
