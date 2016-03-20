package edp.javac.proc;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

// unsupported API
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FinallyRemover1 extends AbstractTranslator1 {
    @Override
    public TreeTranslator createTranslator(final Context context, final JCCompilationUnit top) {
        return new CompoundTranslator(new ReturnViaBreak(context, top),
                                      new FinallyRemover(context, top));
    }
}
