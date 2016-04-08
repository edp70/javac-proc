# javac-proc

javac processors

## Motivation

[Checker Framework](http://types.cs.washington.edu/checker-framework/)
is awesome. But it has some difficult-to-fix issues with code which
uses 'try/finally'.

This project started as an attempt to write a "finally desugarer", ie
a processor which transforms Java code with finally blocks to
equivalent code without finally blocks, thus allowing Checker to do
its job.

## Intended use

Normally one runs Checker something like this:

    javac -processor org.checkerframework.checker.nullness.NullnessChecker ...

Currently, "finally desugaring" can be run before Checker like this:

    javac -processor \
        edp.javac.proc.ReturnViaBreak1,\
        edp.javac.proc.FinallyRemover2,\
        org.checkerframework.checker.nullness.NullnessChecker ...

This project itself can be compiled that way by running `ant && ant -f
build3.xml` instead of just `ant`, although currently `build3.xml`
fails due to [a bug](https://github.com/edp70/javac-proc/issues/2).

(TODO elaborate on AST save/restore to preserve identical codegen,
pre- vs post-ANALYZE processing, `if (true)` wrapping. Add links to
Checker issues and discussion.)

Note on processor naming convention: I use a numeric suffix of `1` or
`2` to indicate whether the processor runs (respectively) before or
after javac's "ANALYZE" phase. (Checker runs after.)

## Requirements

 * JDK 8             (tested with 1.8.0_66)
 * Ant               (tested with 1.9.3)
 * JUnit             (tested with 4.10)
 * Checker Framework (tested with 1.9.10)

To build and test:

Create a build.properties file with "checker.home" set to the location
of Checker (defaults to `/opt/checker-1.9`) and with "junit.jar" set to
the location of the junit JAR file (defaults to `/opt/junit-4.10.jar`).

Once those dependencies are in place, running `ant` should compile
everything and run the tests.

For more details, see `TODO.txt`.
