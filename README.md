# Java Core

A Java learning repository covering core topics from Java 8 to Java 21. Organized by topics with self-contained, runnable examples.

## Topics

| Topic | Description | Progress |
|-------|-------------|----------|
| [concurrency](#concurrency) | Multithreading & Concurrency | 8/10 |
| [collections](#collections) | Collections Framework | 6/6 |
| [streams](#streams) | Streams & Functional Programming | 7/7 |
| oop | OOP & Modern Classes (Records, Sealed) | 0/6 |
| pattern | Pattern Matching | 0/6 |
| strings | String API | 0/5 |
| generics | Generics | 0/5 |
| exceptions | Exception Handling | 0/4 |
| io | I/O & Networking | 0/3 |

## Structure

```
src/main/java/org/nkcoder/
├── concurrency/        # Multithreading & Concurrency
├── collections/        # Collections Framework
├── streams/            # Streams & Functional Programming
├── oop/                # OOP & Modern Classes
├── pattern/            # Pattern Matching
├── strings/            # String API
├── generics/           # Generics
├── exceptions/         # Exception Handling
└── io/                 # I/O & Networking
```

---

## Concurrency

Thread-safe programming from basics to advanced patterns.

| Package | Examples | Concepts |
|---------|----------|----------|
| `thread/` | ThreadExample, DaemonThreadExample | Thread creation, lifecycle, daemon threads |
| `synchronization/` | SynchronizedExample, VolatileExample | synchronized, volatile, wait/notify |
| `locks/` | ReentrantLockDemo, ReadWriteLockExample | ReentrantLock, ReadWriteLock, Conditions |
| `atomic/` | AtomicIntegerExample, AtomicReferenceExample | Atomic classes, CAS operations |
| `executors/` | FixThreadPool, ScheduledThreadPool | ExecutorService, thread pools |
| `concurrent_collections/` | CopyOnWriteListExample | Thread-safe collections |
| `utilities/` | CountDownLatchExample, CyclicBarrierExample | Synchronization utilities |
| `unsafe/` | ArrayListUnSafe, HashMapUnSafe | Thread-safety issues demonstration |

---

## Collections

Java Collections Framework with Java 21 features.

| Example | Concepts |
|---------|----------|
| ListExample | ArrayList, LinkedList, List.of(), List.copyOf() |
| SetExample | HashSet, TreeSet, LinkedHashSet, Set.of() |
| MapExample | HashMap, TreeMap, LinkedHashMap, Map.of() |
| QueueExample | Queue, Deque, PriorityQueue |
| SequencedCollectionExample | SequencedCollection, reversed(), getFirst/Last() |
| ImmutableCollectionsExample | Unmodifiable collections, defensive copies |

---

## Streams

Streams API and functional programming.

| Example | Concepts |
|---------|----------|
| StreamBasicsExample | Creating streams, intermediate/terminal operations |
| StreamCollectorsExample | Collectors, groupingBy, partitioningBy, toMap |
| ParallelStreamExample | Parallel streams, when to use, pitfalls |
| OptionalExample | Optional creation, chaining, orElse vs orElseGet |
| FunctionalInterfaceExample | Function, Predicate, Consumer, Supplier |
| LambdaExample | Lambda syntax, method references, effectively final |
| StreamAdvancedExample | flatMap, reduce, takeWhile, dropWhile |

---

## Running Examples

Each class has a `main()` method:

```bash
./gradlew classes
java -cp build/classes/java/main org.nkcoder.concurrency.thread.ThreadExample
java -cp build/classes/java/main org.nkcoder.collections.ListExample
java -cp build/classes/java/main org.nkcoder.streams.StreamBasicsExample
```

## Build

```bash
./gradlew build    # Build the project
./gradlew test     # Run tests
./gradlew clean    # Clean build artifacts
```

## Requirements

- Java 21+
- Gradle 8+
