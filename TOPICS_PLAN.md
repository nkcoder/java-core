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

| Sub-package | Status | Examples                     | Concepts                                  |
|-------------|--------|------------------------------|-------------------------------------------|
| preview/    | [x]    | StructuredConcurrencyExample | StructuredTaskScope, Joiner (5th preview) |

**Medium:**

| Sub-package             | Status | Examples                                                      | Concepts                                   |
|-------------------------|--------|---------------------------------------------------------------|--------------------------------------------|
| executors/              | [x]    | ExecutorServiceExample, CompletableFutureExample              | ExecutorService, async programming         |
| atomic/                 | [x]    | AtomicIntegerExample, AtomicReferenceExample                  | Atomic classes, CAS operations             |
| concurrent_collections/ | [x]    | ConcurrentHashMapExample, CopyOnWriteListExample              | Thread-safe collections                    |

**Foundational (Learn for interviews & legacy code, prefer modern alternatives for new code):**

| Sub-package      | Status | Examples                                                | Java 25 Recommendation                             |
|------------------|--------|---------------------------------------------------------|----------------------------------------------------|
| thread/          | [x]    | ThreadExample, DaemonThreadExample                      | ⚠️ Use ExecutorService or virtual threads          |
| synchronization/ | [x]    | SynchronizedExample, VolatileExample, WaitNotifyExample | ⚠️ Prefer Atomic*, locks, BlockingQueue            |
| locks/           | [x]    | ReentrantLockExample, ReadWriteLockExample              | ✅ Still relevant for advanced locking              |
| utilities/       | [x]    | CountDownLatchExample, SemaphoreExample                 | ✅ Semaphore useful; Latch → structured concurrency |

**Why learn foundational concurrency?**
- Interview questions still focus heavily on these concepts
- Legacy codebases use them extensively
- Understanding primitives helps debug modern abstractions
- Modern APIs (virtual threads, structured concurrency) build on these

**Rule of thumb for new Java 25 code:**

```
1st choice: Virtual threads + simple blocking code
2nd choice: ConcurrentHashMap, Atomic*, CompletableFuture
3rd choice: ReentrantLock, Semaphore (when needed)
Last resort: Raw threads, synchronized, wait/notify
```

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

All pattern matching features below are **finalized and production-ready** in Java 25.

| Status | Example                  | Concepts                                           | Java Version |
|--------|--------------------------|----------------------------------------------------|--------------|
| [x]    | InstanceofPatternExample | Pattern matching for instanceof, flow scoping      | Java 16+     |
| [x]    | SwitchExpressionExample  | Switch expressions, arrow syntax, yield            | Java 14+     |
| [x]    | SwitchPatternExample     | Type patterns in switch, null handling, dominance  | Java 21+     |
| [x]    | RecordPatternExample     | Deconstructing records, nested patterns, var       | Java 21+     |
| [x]    | GuardedPatternExample    | when clauses, guard ordering, complex conditions   | Java 21+     |
| [x]    | ExhaustiveSwitchExample  | Sealed types + switch = compiler-verified coverage | Java 17/21+  |

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

Core language feature, unchanged since Java 5. Essential for type-safe programming.

| Status | Example               | Concepts                                             |
|--------|-----------------------|------------------------------------------------------|
| [x]    | GenericsBasicsExample | Generic classes, interfaces, diamond operator        |
| [x]    | WildcardsExample      | ?, extends, super - PECS principle, wildcard capture |
| [x]    | TypeErasureExample    | How erasure works, limitations, workarounds          |
| [x]    | BoundedTypesExample   | Upper/multiple bounds, recursive bounds, Comparable  |
| [x]    | GenericMethodsExample | Static generic methods, type inference, varargs      |

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

| Topic       | Status      | Progress  |
|-------------|-------------|-----------|
| concurrency | Complete    | 18/18     |
| collections | Complete    | 6/6       |
| streams     | Complete    | 4/4       |
| fp          | Complete    | 6/6       |
| oop         | Complete    | 6/6       |
| pattern     | Complete    | 6/6       |
| strings     | Not Started | 0/5       |
| generics    | Complete    | 5/5       |
| exceptions  | Not Started | 0/4       |
| io          | Not Started | 0/3       |
| **Total**   |             | **51/63** |
