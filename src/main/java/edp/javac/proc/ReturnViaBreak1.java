package edp.javac.proc;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

// unsupported API
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ReturnViaBreak1 extends AbstractTranslator1 {
    @Override
    public ReturnViaBreak createTranslator(final Context context, final JCCompilationUnit top) {
        return new ReturnViaBreak(context, top);
    }
}
