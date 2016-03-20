package edp.javac.compiler;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

// supported API
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

// unsupported API
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;

// local
import edp.javac.printer.JavacPrinter;

public class CompilerImpl implements Compiler {
    // singleton default
    private static final CompilerImpl DEFAULT = new CompilerImpl(ToolProvider.getSystemJavaCompiler());
    public static CompilerImpl getDefault() { return DEFAULT; }

    private final JavaCompiler javac;
    private final List<Class<? extends Processor>> processors;
    private final boolean debugTrees;
    private final boolean debugTaskStart;
    private final boolean debugTaskFinish;

    public CompilerImpl(final JavaCompiler javac) {
        this.javac = javac;
        this.processors =  Collections.emptyList();
        this.debugTrees = false;
        this.debugTaskStart = false;
        this.debugTaskFinish = false;
    }

    public CompilerImpl(final JavaCompiler javac,
                        final List<Class<? extends Processor>> processors,
                        final boolean debugTrees,
                        final boolean debugTaskStart,
                        final boolean debugTaskFinish) {
        this.javac = javac;
        this.processors = processors; // XXX ref
        this.debugTrees = debugTrees;
        this.debugTaskStart = debugTaskStart;
        this.debugTaskFinish = debugTaskFinish;
    }

    public CompilerImpl withProcessor(final Class<? extends Processor> processor) {
        return new CompilerImpl(javac,
                                append(processors, processor),
                                debugTrees,
                                debugTaskStart,
                                debugTaskFinish);
    }

    public CompilerImpl withDebugTrees() {
        return new CompilerImpl(javac,
                                processors,
                                true,
                                debugTaskStart,
                                debugTaskFinish);
    }

    public CompilerImpl withDebugTasks() {
        return new CompilerImpl(javac,
                                processors,
                                debugTrees,
                                true,
                                true);
    }

    // Parser impl

    public List<Tree> parse(final String source) {
        try {
            return parse("DoesNotSeemToMatter", source);
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<Tree> parse(final String className, final String source) throws Exception {
        final JavacTask task = (JavacTask) getTask(className, source); // XXX JavacTask...
        final List<Tree> ans = new ArrayList<>();
        for (final Tree tree: task.parse())
            ans.add(tree);
        return ans;
    }

    // Compiler impl

    public byte[] compile(final String className, final String source) {
        // XXX TODO FIXME a single "source" can result in multiple
        // output compiled bytecode...  so should the Compiler API
        // return, say, a map from classname to bytecode instead of
        // just byte[]?
        final Loader loader = new Loader();
        run(getTask(loader, className, source));
        return loader.getCode(className).getByteCode();
    }

    public Class<?> compileAndLoad(final String className, final String source) {
        final Loader loader = new Loader();
        run(getTask(loader, className, source));
        try {
            return loader.loadClass(className);
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public Map<String,Class<?>> compileAndLoad2(final String className, final String source) {
        final Loader loader = new Loader();
        run(getTask(loader, className, source));
        return loader.loadAll();
    }

    // run

    private void run(final CompilationTask _task) {
        final BasicJavacTask task = (BasicJavacTask) _task; // XXX
        task.addTaskListener(new Listener(task));
        if (!task.call())
            throw new RuntimeException("compile failed");
    }

    // "task" stuff

    private CompilationTask getTask(final String className,
                                    final String source) {
        return getTask(new Loader(), className, source);
    }

    private CompilationTask getTask(final Loader loader,
                                    final String className,
                                    final String source) {
        return setProcessors(_getTask(loader, className, source));
    }

    private CompilationTask setProcessors(final CompilationTask task) {
        final int n = processors.size();
        if (n > 0) {
            // note: we can't store and inject processor *instances*,
            // because they would be used multiple times, which won't
            // work because they have a certain "lifecycle", eg their
            // "init" method can only be called once.
            task.setProcessors(create(processors));
        }
        return task;
    }
    private static <T> List<T> create(final List<Class<? extends T>> cs) {
        final List<T> ans = new ArrayList<>();
        for (final Class<? extends T> c: cs)
            ans.add(create(c));
        return ans;
    }
    private static <T> T create(final Class<? extends T> c) {
        try { return c.newInstance(); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private CompilationTask _getTask(final Loader loader,
                                     final String name,
                                     final String source) {
        final JavaFileManager fileManager =
            createFileManager(javac, loader);

        final List<JavaFileObject> input =
            Arrays.asList(SourceCode.create(name, source));

        try {
            return javac.getTask(null,
                                 fileManager,
                                 null,
                                 null,
                                 null,
                                 input);
        }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // TaskListener (for debugging)

    private class Listener implements TaskListener {
        private final BasicJavacTask task;
        Listener(final JavacTask task) {
            this.task = (BasicJavacTask) task;
        }
        private Context getContext() {
            return task.getContext();
        }
        public void started(final TaskEvent e) {
            if (debugTaskStart)
                System.out.println("(TaskListener) started: " + e);
            if (debugTrees)
                dump(e);
        }
        public void finished(final TaskEvent e) {
            if (debugTaskFinish)
                System.out.println("(TaskListener) finished: " + e);
            if (debugTrees)
                dump(e);
        }
    }
    private static void dump(final TaskEvent e) {
        final CompilationUnitTree cu = e.getCompilationUnit();
        if (cu != null) {
            System.out.println(cu);
            JavacPrinter.getDefault().print(cu);
        }
    }

    // static stuff

    private static JavaFileManager createFileManager(final JavaCompiler javac,
                                                     final Loader loader) {
        return new OutputManager(javac.getStandardFileManager(null, null, null),
                                 loader);
    }

    private static <T> List<T> append(final List<? extends T> list, final T t) {
        final List<T> ans = new ArrayList<>(list);
        ans.add(t);
        return ans;
    }
}
