# Java Core

A Java learning repository covering core topics from Java 8 to Java 21. Each example is self-contained with detailed comments explaining the concepts.

## Structure

Examples are organized by Java version and topic:

```
src/main/java/org/nkcoder/
j8/          # Java 8 features
  concurrency/   # Threading, locks, atomic classes, concurrent collections
j11/         # Java 11 features
j17/         # Java 17 features
j21/         # Java 21 features
```

## Running Examples

Each class has a `main()` method:

```bash
./gradlew classes
java -cp build/classes/java/main org.nkcoder.j8.concurrency.thread.ThreadExample
```

## Build

```bash
./gradlew build    # Build the project
./gradlew test     # Run tests
./gradlew clean    # Clean build artifacts
```
