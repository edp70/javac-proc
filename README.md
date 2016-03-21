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

The intention is for the "finally desugarer" processor to be run
before Checker, something like this:

    javac -processor edp.javac.proc.FinallyRemover,org.checkerframework.checker.nullness.NullnessChecker ...

In reality, it's a little more complicated.

## Requirements

 * JDK 8             (tested with 1.8.0_66)
 * Ant               (tested with 1.9.3)
 * JUnit             (tested with 4.10)
 * Checker Framework (tested with 1.9.10)

To build and test:

Create a build.properties file with "checker.home" set to the location
of Checker (defaults to /opt/checker-1.9) and with "junit.jar" set to
the location of the junit JAR file (default to /opt/junit-4.10.jar).

Once those dependencies are in place, running "ant" should compile
everything and run the tests.

The unit tests are currently failing.

For more details, see TODO.txt.
