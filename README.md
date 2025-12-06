# Java Core

A comprehensive Java learning repository covering core topics for Java 25. Organized by topics with self-contained, runnable examples designed for interview preparation.

## Topics

| Topic                         | Description                  | Progress  |
|-------------------------------|------------------------------|-----------|
| [concurrency](#concurrency)   | Multithreading & Concurrency | 18/18     |
| [collections](#collections)   | Collections Framework        | 6/6       |
| [streams](#streams)           | Stream API                   | 4/4       |
| [fp](#functional-programming) | Functional Programming       | 6/6       |
| [oop](#oop--modern-classes)   | OOP & Modern Classes         | 6/6       |
| [pattern](#pattern-matching)  | Pattern Matching             | 6/6       |
| [strings](#strings)           | String API                   | 5/5       |
| [generics](#generics)         | Generics                     | 5/5       |
| [exceptions](#exceptions)     | Exception Handling           | 4/4       |
| [io](#io--networking)         | I/O & Networking             | 4/4       |
| **Total**                     |                              | **64/64** |

## Structure

```
src/main/java/org/nkcoder/
├── concurrency/        # Multithreading & Concurrency
├── collections/        # Collections Framework
├── streams/            # Stream API
├── fp/                 # Functional Programming
├── oop/                # OOP & Modern Classes
├── pattern/            # Pattern Matching
├── strings/            # String API
├── generics/           # Generics
├── exceptions/         # Exception Handling
└── io/                 # I/O & Networking
```

---

## Concurrency

Thread-safe programming from basics to modern virtual threads.

### Modern (Java 21+)

| Package    | Examples                     | Concepts                                 |
|------------|------------------------------|------------------------------------------|
| `virtual/` | VirtualThreadExample         | Virtual threads, lightweight concurrency |
| `scoped/`  | ScopedValueExample           | ScopedValue (replaces ThreadLocal)       |
| `preview/` | StructuredConcurrencyExample | StructuredTaskScope (preview)            |

### Recommended

| Package                   | Examples                                         | Concepts                           |
|---------------------------|--------------------------------------------------|------------------------------------|
| `executors/`              | ExecutorServiceExample, CompletableFutureExample | ExecutorService, async programming |
| `atomic/`                 | AtomicIntegerExample, AtomicReferenceExample     | Atomic classes, CAS operations     |
| `concurrent_collections/` | ConcurrentHashMapExample, CopyOnWriteListExample | Thread-safe collections            |

### Foundational

| Package            | Examples                                                | Concepts                     |
|--------------------|---------------------------------------------------------|------------------------------|
| `thread/`          | ThreadExample, DaemonThreadExample                      | Thread creation, lifecycle   |
| `synchronization/` | SynchronizedExample, VolatileExample, WaitNotifyExample | synchronized, volatile       |
| `locks/`           | ReentrantLockExample, ReadWriteLockExample              | ReentrantLock, ReadWriteLock |
| `utilities/`       | CountDownLatchExample, SemaphoreExample                 | Synchronization utilities    |

---

## Collections

Java Collections Framework with Java 21 features.

| Example                     | Concepts                                         |
|-----------------------------|--------------------------------------------------|
| ListExample                 | ArrayList, LinkedList, List.of(), List.copyOf()  |
| SetExample                  | HashSet, TreeSet, LinkedHashSet, Set.of()        |
| MapExample                  | HashMap, TreeMap, LinkedHashMap, Map.of()        |
| QueueExample                | Queue, Deque, PriorityQueue                      |
| SequencedCollectionExample  | SequencedCollection, reversed(), getFirst/Last() |
| ImmutableCollectionsExample | Unmodifiable collections, defensive copies       |

---

## Streams

Stream API for data processing pipelines.

| Example                 | Concepts                                           |
|-------------------------|----------------------------------------------------|
| StreamBasicsExample     | Creating streams, intermediate/terminal operations |
| StreamCollectorsExample | Collectors, groupingBy, partitioningBy, toMap      |
| StreamAdvancedExample   | flatMap, reduce, takeWhile, dropWhile              |
| ParallelStreamExample   | Parallel streams, when to use, pitfalls            |

---

## Functional Programming

Lambdas, functional interfaces, and FP patterns.

| Example                    | Concepts                                            |
|----------------------------|-----------------------------------------------------|
| LambdaExample              | Lambda syntax, method references, effectively final |
| FunctionalInterfaceExample | Function, Predicate, Consumer, Supplier             |
| OptionalExample            | Optional creation, chaining, orElse vs orElseGet    |
| CurryingExample            | Currying, partial application, function factories   |
| MemoizationExample         | Caching pure functions, lazy computation            |
| MonadPatternsExample       | Try monad, Validation, railway-oriented programming |

---

## OOP & Modern Classes

Records, sealed classes, and modern OOP patterns.

| Example                   | Concepts                               |
|---------------------------|----------------------------------------|
| RecordExample             | Record syntax, compact constructors    |
| RecordAdvancedExample     | Validation, static methods, interfaces |
| SealedClassExample        | sealed, permits, non-sealed            |
| SealedWithRecordsExample  | Algebraic data types pattern           |
| InterfaceEvolutionExample | Default, static, private methods       |
| InheritanceExample        | Composition over inheritance           |

---

## Pattern Matching

Modern pattern matching (all finalized in Java 21+).

| Example                  | Concepts                                |
|--------------------------|-----------------------------------------|
| InstanceofPatternExample | Pattern matching for instanceof         |
| SwitchExpressionExample  | Switch expressions, arrow syntax, yield |
| SwitchPatternExample     | Type patterns in switch, null handling  |
| RecordPatternExample     | Deconstructing records, nested patterns |
| GuardedPatternExample    | when clauses, guard conditions          |
| ExhaustiveSwitchExample  | Sealed types + switch = exhaustiveness  |

---

## Strings

String API with modern methods (Java 11+).

| Example                 | Concepts                                 |
|-------------------------|------------------------------------------|
| StringBasicsExample     | Immutability, string pool, intern()      |
| StringMethodsExample    | isBlank, strip, lines, repeat, indent    |
| TextBlockExample        | Multi-line strings, indentation, escapes |
| StringBuilderExample    | Mutable strings, StringJoiner            |
| StringFormattingExample | format(), formatted(), printf, locales   |

---

## Generics

Type-safe programming with generics.

| Example               | Concepts                                      |
|-----------------------|-----------------------------------------------|
| GenericsBasicsExample | Generic classes, interfaces, diamond operator |
| WildcardsExample      | ?, extends, super - PECS principle            |
| TypeErasureExample    | How erasure works, limitations                |
| BoundedTypesExample   | Upper/multiple bounds, Comparable             |
| GenericMethodsExample | Static generic methods, type inference        |

---

## Exceptions

Exception handling patterns.

| Example                   | Concepts                             |
|---------------------------|--------------------------------------|
| TryWithResourcesExample   | AutoCloseable, suppressed exceptions |
| CheckedVsUncheckedExample | When to use which, best practices    |
| ExceptionChainingExample  | Cause, suppressed, stack traces      |
| CustomExceptionExample    | Hierarchies, rich context            |

---

## I/O & Networking

Modern file and network I/O (Java 11+).

| Example                    | Concepts                               |
|----------------------------|----------------------------------------|
| PathAndFilesExample        | Path API, Files read/write, attributes |
| DirectoryOperationsExample | walk, list, find, DirectoryStream      |
| HttpClientExample          | HTTP Client API, sync/async requests   |
| HttpClientAdvancedExample  | HTTP/2, authentication, cookies        |

---

## Running Examples

Each class has a `main()` method:

```bash
./gradlew classes
java -cp build/classes/java/main org.nkcoder.concurrency.virtual.VirtualThreadExample
java -cp build/classes/java/main org.nkcoder.collections.ListExample
java -cp build/classes/java/main org.nkcoder.pattern.SwitchPatternExample
```

For preview features:
```bash
java --enable-preview -cp build/classes/java/main org.nkcoder.concurrency.preview.StructuredConcurrencyExample
```

## Build

```bash
./gradlew build    # Build the project
./gradlew test     # Run tests
./gradlew clean    # Clean build artifacts
```

## Requirements

- Java 25+
- Gradle 8+
