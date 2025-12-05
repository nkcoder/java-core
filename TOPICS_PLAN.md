# Java Core Topics Plan (Topic-Based Structure)

Reorganized by interview topics instead of Java versions.

## Package Structure

```
src/main/java/org/nkcoder/
├── concurrency/        # Multithreading & Concurrency
├── collections/        # Collections Framework
├── streams/            # Stream API
├── fp/                 # Functional Programming
├── oop/                # OOP & Modern Classes (Records, Sealed)
├── pattern/            # Pattern Matching
├── strings/            # String API
├── generics/           # Generics
├── exceptions/         # Exception Handling
└── io/                 # I/O & Networking
```

---

## Topic Details

### 1. concurrency/ - Multithreading & Concurrency (Java 25)

**Stable (Production-Ready):**

| Sub-package  | Status | Examples                      | Concepts                                 |
|--------------|--------|-------------------------------|------------------------------------------|
| virtual/     | [x]    | VirtualThreadExample          | Virtual threads, I/O-bound concurrency   |
| scoped/      | [x]    | ScopedValueExample            | ScopedValue (replaces ThreadLocal)       |

**Preview (Experimental - API may change):**

| Sub-package  | Status | Examples                      | Concepts                                 |
|--------------|--------|-------------------------------|------------------------------------------|
| preview/     | [x]    | StructuredConcurrencyExample  | StructuredTaskScope, Joiner (5th preview)|

**Interview Priority (Completed):**

| Sub-package             | Status | Examples                                                      | Concepts                                   |
|-------------------------|--------|---------------------------------------------------------------|--------------------------------------------|
| executors/              | [x]    | ExecutorServiceExample, CompletableFutureExample              | ExecutorService, async programming         |
| atomic/                 | [x]    | AtomicIntegerExample, AtomicReferenceExample                  | Atomic classes, CAS operations             |
| concurrent_collections/ | [x]    | ConcurrentHashMapExample, CopyOnWriteListExample              | Thread-safe collections                    |

**Foundational (To Add Later):**

| Sub-package             | Status | Examples                                                      | Concepts                                   |
|-------------------------|--------|---------------------------------------------------------------|--------------------------------------------|
| thread/                 | [ ]    | ThreadExample, DaemonThreadExample                            | Thread creation, lifecycle, daemon threads |
| synchronization/        | [ ]    | SynchronizedExample, VolatileExample, WaitNotifyExample       | synchronized, volatile, wait/notify        |
| locks/                  | [ ]    | ReentrantLockDemo, ReadWriteLockExample                       | ReentrantLock, ReadWriteLock               |
| utilities/              | [ ]    | CountDownLatchExample, SemaphoreExample                       | Synchronization utilities                  |

---

### 2. collections/ - Collections Framework

| Status | Example                     | Concepts                                                   |
|--------|-----------------------------|------------------------------------------------------------|
| [x]    | ListExample                 | ArrayList, LinkedList, List.of(), List.copyOf()            |
| [x]    | SetExample                  | HashSet, TreeSet, LinkedHashSet, Set.of()                  |
| [x]    | MapExample                  | HashMap, TreeMap, LinkedHashMap, Map.of(), Map.entry()     |
| [x]    | QueueExample                | Queue, Deque, PriorityQueue                                |
| [x]    | SequencedCollectionExample  | SequencedCollection, reversed(), getFirst/Last() (Java 21) |
| [x]    | ImmutableCollectionsExample | Unmodifiable collections, defensive copies                 |

---

### 3. streams/ - Stream API

| Status | Example                 | Concepts                                           |
|--------|-------------------------|----------------------------------------------------|
| [x]    | StreamBasicsExample     | Creating streams, intermediate/terminal operations |
| [x]    | StreamCollectorsExample | Collectors, groupingBy, partitioningBy, toMap      |
| [x]    | ParallelStreamExample   | Parallel streams, when to use, pitfalls            |
| [x]    | StreamAdvancedExample   | flatMap, reduce, takeWhile, dropWhile              |

---

### 4. fp/ - Functional Programming

| Status | Example                    | Concepts                                            |
|--------|----------------------------|-----------------------------------------------------|
| [x]    | LambdaExample              | Lambda syntax, method references, effectively final |
| [x]    | FunctionalInterfaceExample | Function, Predicate, Consumer, Supplier, custom     |
| [x]    | OptionalExample            | Optional creation, chaining, orElse vs orElseGet    |
| [x]    | CurryingExample            | Currying, partial application, function factories   |
| [x]    | MemoizationExample         | Caching pure functions, lazy computation, Fibonacci |
| [x]    | MonadPatternsExample       | Try monad, Validation, railway-oriented programming |

---

### 5. oop/ - OOP & Modern Classes

| Status | Example                   | Concepts                                            |
|--------|---------------------------|-----------------------------------------------------|
| [x]    | RecordExample             | Record syntax, compact constructors, when to use    |
| [x]    | RecordAdvancedExample     | Record with validation, static methods, interfaces  |
| [x]    | SealedClassExample        | sealed, permits, non-sealed keywords                |
| [x]    | SealedWithRecordsExample  | Algebraic data types pattern                        |
| [x]    | InterfaceEvolutionExample | Default methods, static methods, private methods    |
| [x]    | InheritanceExample        | Extends vs implements, composition over inheritance |

---

### 6. pattern/ - Pattern Matching

| Status | Example                  | Concepts                                     |
|--------|--------------------------|----------------------------------------------|
| [ ]    | InstanceofPatternExample | Pattern matching for instanceof (Java 14+)   |
| [ ]    | SwitchExpressionExample  | Switch expressions, arrow syntax, yield      |
| [ ]    | SwitchPatternExample     | Pattern matching for switch (Java 17+)       |
| [ ]    | RecordPatternExample     | Deconstructing records in patterns (Java 21) |
| [ ]    | GuardedPatternExample    | when clauses in switch patterns              |
| [ ]    | ExhaustiveSwitchExample  | Exhaustiveness with sealed types             |

---

### 7. strings/ - String API

| Status | Example                   | Concepts                                            |
|--------|---------------------------|-----------------------------------------------------|
| [ ]    | StringMethodsExample      | isBlank, lines, strip, repeat, indent (Java 11+)    |
| [ ]    | TextBlockExample          | Multi-line strings, escaping, formatting (Java 15+) |
| [ ]    | StringImmutabilityExample | Why strings are immutable, string pool              |
| [ ]    | StringBuilderExample      | StringBuilder vs StringBuffer, when to use          |
| [ ]    | StringFormattingExample   | String.format, formatted(), printf                  |

---

### 8. generics/ - Generics

| Status | Example               | Concepts                                    |
|--------|-----------------------|---------------------------------------------|
| [ ]    | GenericsBasicsExample | Generic classes, methods, type parameters   |
| [ ]    | WildcardsExample      | ?, extends, super - PECS principle          |
| [ ]    | TypeErasureExample    | How erasure works, limitations, workarounds |
| [ ]    | BoundedTypesExample   | Upper/lower bounds, multiple bounds         |
| [ ]    | GenericMethodsExample | Static generic methods, type inference      |

---

### 9. exceptions/ - Exception Handling

| Status | Example                   | Concepts                                                       |
|--------|---------------------------|----------------------------------------------------------------|
| [ ]    | TryWithResourcesExample   | AutoCloseable, resource management (Java 7+, enhanced Java 9+) |
| [ ]    | CheckedVsUncheckedExample | When to use which, best practices                              |
| [ ]    | ExceptionChainingExample  | Cause, suppressed exceptions                                   |
| [ ]    | CustomExceptionExample    | Creating custom exceptions, best practices                     |

---

### 10. io/ - I/O & Networking

| Status | Example                   | Concepts                               |
|--------|---------------------------|----------------------------------------|
| [ ]    | FilesExample              | Files API, Path, reading/writing files |
| [ ]    | HttpClientExample         | HTTP Client API (Java 11+), sync/async |
| [ ]    | HttpClientAdvancedExample | HTTP/2, WebSocket, authentication      |

---

## Progress Summary

| Topic        | Status      | Progress |
|--------------|-------------|----------|
| concurrency  | In Progress | 9/18     |
| collections  | Complete    | 6/6      |
| streams      | Complete    | 4/4      |
| fp           | Complete    | 6/6      |
| oop          | Complete    | 6/6      |
| pattern      | Not Started | 0/6      |
| strings      | Not Started | 0/5      |
| generics     | Not Started | 0/5      |
| exceptions   | Not Started | 0/4      |
| io           | Not Started | 0/3      |
| **Total**    |             | **31/63**|
