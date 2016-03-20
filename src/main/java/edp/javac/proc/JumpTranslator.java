package edp.javac.proc;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

// unsupported API
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/** Translates jump (break and continue) statements within a try/catch
    which attempt to transfer control outside the try/catch, to "break
    <generated-label>" statements, in order to route control through a
    copy of the try/catch's finally block. */
public class JumpTranslator {
    interface NameGenerator { Name genName(String prefix); }
    private final NameGenerator NG;
    private final TreeMaker M;

    public JumpTranslator(final Names N, final UniqueNameGenerator G, final TreeMaker M) {
        this.NG = new NameGenerator() {
            public Name genName(final String prefix) {
                return N.fromString(G.genName(prefix));
            }
        };
        this.M = M;
    }

    public List<Jump<?>> run(final JCTry t) {
        final Env env = new Env(M, NG);
        new UnlabeledBreaks(env).translate(t);
        new UnlabeledContinues(env).translate(t);
        new LabeledBreaks(env).translate(t);
        new LabeledContinues(env).translate(t);
        return env.getResult();
    }

    private static class Env {
        private final TreeMaker M;
        private final NameGenerator G;
        private final AtomicInteger counter = new AtomicInteger();
        private final List<Jump<?>> jumps = new ArrayList<>();
        private Env(final TreeMaker M, NameGenerator G) {
            this.M = M;
            this.G = G;
        }
        private List<Jump<?>> getResult() {
            return jumps;
        }
        private int getIndex() {
            return counter.get();
        }
        private Name getTarget(final int idx) {
            return jumps.get(idx).getTarget();
        }
        private Name createNewTargetName() {
            final int idx = counter.getAndIncrement();
            return G.genName("J" + idx + "$");
        }
        private Name add(final Jump<?> jump) {
            jumps.add(jump);
            return jump.getTarget();
        }
        private JCBreak createBreak(final Name label) {
            return M.Break(label);
        }
        /* TODO preserve old source position?
        private JCBreak createBreak(final int pos, final Name label) {
            return M.at(pos).Break(label);
        }
        */
    }

    // S is really "JCBreak | JCContinue"
    private abstract class Translator<S extends JCStatement> extends TreeTranslator {
        protected final Env env;
        public Translator(final Env env) { this.env = env; }

        // don't recurse into class or lambda defs, because control
        // does not flow into (and jumps cannot escape) from them.
        @Override public void visitClassDef(final JCClassDecl tree) { result = tree; }
        @Override public void visitLambda(final JCLambda tree) { result = tree; }

        // wrap 's' as Jump, with the given (new) target
        protected abstract Jump<S> wrap(final S s, final Name target);

        protected Name createNewTarget(final S s) {
            return env.add(wrap(s, env.createNewTargetName()));
        }
    }

    private abstract class Labeled<S extends JCStatement> extends Translator<S> {
        public Labeled(final Env env) { super(env); }

        private final Set<Name> ignore = new HashSet<>();
        private final Map<Name,Integer> targetMap = new HashMap<>();

        // ignore labels within try/catch, because jumps to them can
        // only transfer control *within* the try/catch, not outside
        // it, and are therefore irrelevant (because control does not
        // need to pass through finally block).
        //
        // eg, no need to translate "break FOO" here:
        //
        // try {
        //     FOO: {
        //         break FOO;
        //     }
        //     ...
        @Override
        public void visitLabelled(final JCLabeledStatement s) {
            ignore.add(s.label);
            super.visitLabelled(s);
        }

        protected JCStatement process(final S s, final Name label) {
            return label != null && !ignore.contains(label)
                ? env.createBreak(getOrCreateNewTarget(s, label))
                : s;
        }

        private Name getOrCreateNewTarget(final S s, final Name label) {
            final Integer ans = targetMap.get(label);
            return ans != null ? env.getTarget(ans) : createNewTarget(s, label);
        }

        private Name createNewTarget(final S s, final Name label) {
            targetMap.put(label, env.getIndex());
            return createNewTarget(s);
        }
    }

    private class LabeledBreaks extends Labeled<JCBreak> {
        private LabeledBreaks(final Env env) { super(env); }
        @Override public Jump.Break wrap(final JCBreak j, final Name t) { return new Jump.Break(j, t); }

        @Override
        public void visitBreak(final JCBreak j) {
            result = process(j, j.label);
        }
    }

    private class LabeledContinues extends Labeled<JCContinue> {
        private LabeledContinues(final Env env) { super(env); }
        @Override public Jump.Continue wrap(final JCContinue j, final Name t) { return new Jump.Continue(j, t); }

        @Override
        public void visitContinue(final JCContinue j) {
            result = process(j, j.label);
        }
    }

    private abstract class Unlabeled<S extends JCStatement> extends Translator<S> {
        private Unlabeled(final Env env) { super(env); }

        private @MonotonicNonNull Name newTarget;

        protected JCBreak process(final S s) {
            return env.createBreak(getOrCreateNewTarget(s));
        }

        private Name getOrCreateNewTarget(final S s) {
            return newTarget != null ? newTarget
                : (newTarget = createNewTarget(s));
        }

        // Don't recurse into loops (because unlabeled break/continue
        // applies to enclosing loop).
        @Override public void visitDoLoop(final JCDoWhileLoop tree) { result = tree; }
        @Override public void visitWhileLoop(final JCWhileLoop tree) { result = tree; }
        @Override public void visitForLoop(final JCForLoop tree) { result = tree; }
        @Override public void visitForeachLoop(final JCEnhancedForLoop tree) { result = tree; }
    }

    private class UnlabeledBreaks extends Unlabeled<JCBreak> {
        private UnlabeledBreaks(final Env env) { super(env); }
        @Override public Jump.Break wrap(final JCBreak j, final Name t) { return new Jump.Break(j, t); }

        @Override
        public void visitBreak(final JCBreak s) {
            result = s.label == null ? process(s) : s;
        }

        // Unlabeled break (unlike unlabeled continue) applies to
        // enclosing switch, so don't recurse into switch here.
        @Override public void visitSwitch(final JCSwitch tree) { result = tree; }
    }

    private class UnlabeledContinues extends Unlabeled<JCContinue> {
        private UnlabeledContinues(final Env env) { super(env); }
        @Override public Jump.Continue wrap(final JCContinue j, final Name t) { return new Jump.Continue(j, t); }

        @Override
        public void visitContinue(final JCContinue s) {
            result = s.label == null ? process(s) : s;
        }
    }
}
