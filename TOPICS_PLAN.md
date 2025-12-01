# Java Core Topics Plan (Topic-Based Structure)

Reorganized by interview topics instead of Java versions.

## Package Structure

```
src/main/java/org/nkcoder/
├── concurrency/        # Multithreading & Concurrency
├── collections/        # Collections Framework
├── streams/            # Streams & Functional Programming
├── oop/                # OOP & Modern Classes (Records, Sealed)
├── pattern/            # Pattern Matching
├── strings/            # String API
├── generics/           # Generics
├── exceptions/         # Exception Handling
└── io/                 # I/O & Networking
```

---

## Topic Details

### 1. concurrency/ - Multithreading & Concurrency

| Sub-package             | Status | Examples                                                      | Concepts                                   |
|-------------------------|--------|---------------------------------------------------------------|--------------------------------------------|
| thread/                 | [x]    | ThreadExample, DaemonThreadExample                            | Thread creation, lifecycle, daemon threads |
| synchronization/        | [x]    | SynchronizedExample, VolatileExample, WaitNotifyExample       | synchronized, volatile, wait/notify        |
| locks/                  | [x]    | ReentrantLockDemo, ReadWriteLockExample, LockSupportExample   | ReentrantLock, ReadWriteLock, Conditions   |
| atomic/                 | [x]    | AtomicIntegerExample, AtomicReferenceExample                  | Atomic classes, CAS operations             |
| executors/              | [x]    | FixThreadPool, ScheduledThreadPool, ThreadPoolExecutor        | ExecutorService, thread pools              |
| concurrent_collections/ | [x]    | ConcurrentHashMapExample, CopyOnWriteListExample              | Thread-safe collections                    |
| utilities/              | [x]    | CountDownLatchExample, CyclicBarrierExample, SemaphoreExample | Synchronization utilities                  |
| unsafe/                 | [x]    | ArrayListUnSafe, HashMapUnSafe                                | Thread-safety issues demonstration         |
| virtual/                | [ ]    | VirtualThreadExample, VirtualVsPlatformExample                | Virtual threads (Java 21)                  |
| structured/             | [ ]    | StructuredConcurrencyExample                                  | Structured concurrency (Java 21)           |

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

### 3. streams/ - Streams & Functional Programming

| Status | Example                    | Concepts                                            |
|--------|----------------------------|-----------------------------------------------------|
| [ ]    | StreamBasicsExample        | Creating streams, intermediate/terminal operations  |
| [ ]    | StreamCollectorsExample    | Collectors, groupingBy, partitioningBy, toMap       |
| [ ]    | ParallelStreamExample      | Parallel streams, when to use, pitfalls             |
| [ ]    | OptionalExample            | Optional creation, chaining, orElse vs orElseGet    |
| [ ]    | FunctionalInterfaceExample | Function, Predicate, Consumer, Supplier, custom     |
| [ ]    | LambdaExample              | Lambda syntax, method references, effectively final |
| [ ]    | StreamAdvancedExample      | flatMap, reduce, takeWhile, dropWhile               |

---

### 4. oop/ - OOP & Modern Classes

| Status | Example                   | Concepts                                            |
|--------|---------------------------|-----------------------------------------------------|
| [ ]    | RecordExample             | Record syntax, compact constructors, when to use    |
| [ ]    | RecordAdvancedExample     | Record with validation, static methods, interfaces  |
| [ ]    | SealedClassExample        | sealed, permits, non-sealed keywords                |
| [ ]    | SealedWithRecordsExample  | Algebraic data types pattern                        |
| [ ]    | InterfaceEvolutionExample | Default methods, static methods, private methods    |
| [ ]    | InheritanceExample        | Extends vs implements, composition over inheritance |

---

### 5. pattern/ - Pattern Matching

| Status | Example                  | Concepts                                     |
|--------|--------------------------|----------------------------------------------|
| [ ]    | InstanceofPatternExample | Pattern matching for instanceof (Java 14+)   |
| [ ]    | SwitchExpressionExample  | Switch expressions, arrow syntax, yield      |
| [ ]    | SwitchPatternExample     | Pattern matching for switch (Java 17+)       |
| [ ]    | RecordPatternExample     | Deconstructing records in patterns (Java 21) |
| [ ]    | GuardedPatternExample    | when clauses in switch patterns              |
| [ ]    | ExhaustiveSwitchExample  | Exhaustiveness with sealed types             |

---

### 6. strings/ - String API

| Status | Example                   | Concepts                                            |
|--------|---------------------------|-----------------------------------------------------|
| [ ]    | StringMethodsExample      | isBlank, lines, strip, repeat, indent (Java 11+)    |
| [ ]    | TextBlockExample          | Multi-line strings, escaping, formatting (Java 15+) |
| [ ]    | StringImmutabilityExample | Why strings are immutable, string pool              |
| [ ]    | StringBuilderExample      | StringBuilder vs StringBuffer, when to use          |
| [ ]    | StringFormattingExample   | String.format, formatted(), printf                  |

---

### 7. generics/ - Generics

| Status | Example               | Concepts                                    |
|--------|-----------------------|---------------------------------------------|
| [ ]    | GenericsBasicsExample | Generic classes, methods, type parameters   |
| [ ]    | WildcardsExample      | ?, extends, super - PECS principle          |
| [ ]    | TypeErasureExample    | How erasure works, limitations, workarounds |
| [ ]    | BoundedTypesExample   | Upper/lower bounds, multiple bounds         |
| [ ]    | GenericMethodsExample | Static generic methods, type inference      |

---

### 8. exceptions/ - Exception Handling

| Status | Example                   | Concepts                                                       |
|--------|---------------------------|----------------------------------------------------------------|
| [ ]    | TryWithResourcesExample   | AutoCloseable, resource management (Java 7+, enhanced Java 9+) |
| [ ]    | CheckedVsUncheckedExample | When to use which, best practices                              |
| [ ]    | ExceptionChainingExample  | Cause, suppressed exceptions                                   |
| [ ]    | CustomExceptionExample    | Creating custom exceptions, best practices                     |

---

### 9. io/ - I/O & Networking

| Status | Example                   | Concepts                               |
|--------|---------------------------|----------------------------------------|
| [ ]    | FilesExample              | Files API, Path, reading/writing files |
| [ ]    | HttpClientExample         | HTTP Client API (Java 11+), sync/async |
| [ ]    | HttpClientAdvancedExample | HTTP/2, WebSocket, authentication      |

---

## Progress Summary

| Topic        | Status      | Progress |
|--------------|-------------|----------|
| concurrency  | In Progress | 8/10     |
| collections  | Complete    | 6/6      |
| streams      | Not Started | 0/7      |
| oop          | Not Started | 0/6      |
| pattern      | Not Started | 0/6      |
| strings      | Not Started | 0/5      |
| generics     | Not Started | 0/5      |
| exceptions   | Not Started | 0/4      |
| io           | Not Started | 0/3      |
| **Total**    |             | **14/52**|
