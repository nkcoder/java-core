# Java Core

A Java learning repository covering core topics from Java 8 to Java 21. Each example is self-contained with detailed comments explaining the concepts.

## Structure

Examples are organized by topic:

```
src/main/java/org/nkcoder/
└── concurrency/               # Multithreading & Concurrency
    ├── thread/                # Thread basics, lifecycle, priority
    ├── synchronization/       # synchronized, volatile, wait/notify
    ├── locks/                 # ReentrantLock, ReadWriteLock
    ├── atomic/                # AtomicInteger, AtomicReference
    ├── executors/             # Thread pools, scheduled executors
    ├── concurrent_collections/# ConcurrentLinkedQueue, CopyOnWriteList
    ├── utilities/             # CountDownLatch, CyclicBarrier, Semaphore
    └── unsafe/                # Thread-safety demonstrations
```

## Running Examples

Each class has a `main()` method:

```bash
./gradlew classes
java -cp build/classes/java/main org.nkcoder.concurrency.thread.ThreadExample
```

## Build

```bash
./gradlew build    # Build the project
./gradlew test     # Run tests
./gradlew clean    # Clean build artifacts
```
