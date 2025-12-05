package org.nkcoder.concurrency.locks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * ReadWriteLock: Allows multiple readers OR single writer.
 *
 * <p><strong>Java 25 Note:</strong> Still useful for read-heavy workloads where reads significantly
 * outnumber writes. For simpler cases, consider {@code ConcurrentHashMap} or {@code Atomic*}.
 * {@code StampedLock} offers optimistic reads for even better read performance.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Multiple threads can hold read lock simultaneously</li>
 *   <li>Write lock is exclusive (no other readers or writers)</li>
 *   <li>Great for read-heavy workloads with occasional writes</li>
 *   <li>StampedLock (Java 8+) offers optimistic reads</li>
 * </ul>
 *
 * <p>Interview tip: Know when ReadWriteLock outperforms synchronized,
 * and understand the writer starvation problem.
 */
public class ReadWriteLockExample {

  public static void main(String[] args) throws Exception {
    basicUsage();
    cacheExample();
    readWriteBehavior();
    stampedLockExample();
    performanceComparison();
    bestPractices();
  }

  static void basicUsage() throws Exception {
    System.out.println("=== Basic Usage ===");

    ReadWriteLock rwLock = new ReentrantReadWriteLock();

    class Data {
      private String value = "initial";

      String read() {
        rwLock.readLock().lock();
        try {
          System.out.println("    " + Thread.currentThread().getName() + " reading: " + value);
          Thread.sleep(50); // Simulate read time
          return value;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return null;
        } finally {
          rwLock.readLock().unlock();
        }
      }

      void write(String newValue) {
        rwLock.writeLock().lock();
        try {
          System.out.println("    " + Thread.currentThread().getName() + " writing: " + newValue);
          Thread.sleep(100); // Simulate write time
          value = newValue;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          rwLock.writeLock().unlock();
        }
      }
    }

    Data data = new Data();

    // Multiple readers can read simultaneously
    Thread r1 = new Thread(() -> data.read(), "Reader-1");
    Thread r2 = new Thread(() -> data.read(), "Reader-2");
    Thread w1 = new Thread(() -> data.write("updated"), "Writer-1");

    r1.start();
    r2.start();
    Thread.sleep(10);
    w1.start();

    r1.join();
    r2.join();
    w1.join();

    System.out.println("""

        ReadWriteLock rules:
        - Multiple readers: OK (concurrent reads)
        - Reader + Writer: BLOCKED (writer waits or readers wait)
        - Multiple writers: BLOCKED (only one writer)

        Compatibility matrix:
        +--------+--------+--------+
        |        | Read   | Write  |
        +--------+--------+--------+
        | Read   | OK     | BLOCKED|
        | Write  | BLOCKED| BLOCKED|
        +--------+--------+--------+
        """);
  }

  static void cacheExample() throws Exception {
    System.out.println("=== Cache Example ===");

    class ThreadSafeCache<K, V> {
      private final Map<K, V> cache = new HashMap<>();
      private final ReadWriteLock lock = new ReentrantReadWriteLock();

      V get(K key) {
        lock.readLock().lock();
        try {
          return cache.get(key);
        } finally {
          lock.readLock().unlock();
        }
      }

      void put(K key, V value) {
        lock.writeLock().lock();
        try {
          cache.put(key, value);
        } finally {
          lock.writeLock().unlock();
        }
      }

      V computeIfAbsent(K key, java.util.function.Function<K, V> mappingFunction) {
        // First try read lock
        lock.readLock().lock();
        try {
          V value = cache.get(key);
          if (value != null) {
            return value;
          }
        } finally {
          lock.readLock().unlock();
        }

        // Need write lock to insert
        lock.writeLock().lock();
        try {
          // Double-check after acquiring write lock
          V value = cache.get(key);
          if (value == null) {
            value = mappingFunction.apply(key);
            cache.put(key, value);
          }
          return value;
        } finally {
          lock.writeLock().unlock();
        }
      }

      int size() {
        lock.readLock().lock();
        try {
          return cache.size();
        } finally {
          lock.readLock().unlock();
        }
      }
    }

    ThreadSafeCache<String, Integer> cache = new ThreadSafeCache<>();

    try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
      // Many reads
      for (int i = 0; i < 100; i++) {
        int key = i % 10;
        executor.submit(() -> cache.computeIfAbsent("key" + key, k -> k.length()));
      }
    }

    System.out.println("  Cache size: " + cache.size());
    System.out.println("  Value for key5: " + cache.get("key5"));

    System.out.println();
  }

  static void readWriteBehavior() throws Exception {
    System.out.println("=== Read/Write Lock Behavior ===");

    ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Show lock downgrading (write -> read, but not read -> write)
    System.out.println("  Lock downgrading (write -> read):");

    rwLock.writeLock().lock();
    System.out.println("    Acquired write lock");

    rwLock.readLock().lock(); // Can acquire read while holding write
    System.out.println("    Acquired read lock (while holding write)");

    rwLock.writeLock().unlock(); // Release write, still hold read
    System.out.println("    Released write lock, still have read");

    rwLock.readLock().unlock();
    System.out.println("    Released read lock");

    // Lock upgrading is NOT supported (would cause deadlock)
    System.out.println("\n  Lock upgrading (read -> write): NOT SUPPORTED!");
    System.out.println("    Would cause deadlock if multiple readers try to upgrade");

    // Query lock state
    System.out.println("\n  Lock state queries:");
    rwLock.writeLock().lock();
    System.out.println("    Write lock held: " + rwLock.isWriteLocked());
    System.out.println("    Write hold count: " + rwLock.getWriteHoldCount());
    System.out.println("    Read lock count: " + rwLock.getReadLockCount());
    rwLock.writeLock().unlock();

    System.out.println();
  }

  static void stampedLockExample() throws Exception {
    System.out.println("=== StampedLock (Java 8+) ===");

    StampedLock stampedLock = new StampedLock();

    class Point {
      private double x, y;

      void move(double deltaX, double deltaY) {
        long stamp = stampedLock.writeLock();
        try {
          x += deltaX;
          y += deltaY;
        } finally {
          stampedLock.unlockWrite(stamp);
        }
      }

      // Optimistic read - very fast, may need retry
      double distanceFromOrigin() {
        long stamp = stampedLock.tryOptimisticRead(); // No blocking!
        double currentX = x;
        double currentY = y;

        // Check if write occurred during read
        if (!stampedLock.validate(stamp)) {
          // Fallback to pessimistic read
          stamp = stampedLock.readLock();
          try {
            currentX = x;
            currentY = y;
          } finally {
            stampedLock.unlockRead(stamp);
          }
        }

        return Math.sqrt(currentX * currentX + currentY * currentY);
      }

      // Convert read lock to write lock
      void moveIfAt(double expectedX, double expectedY, double newX, double newY) {
        long stamp = stampedLock.readLock();
        try {
          while (x == expectedX && y == expectedY) {
            long writeStamp = stampedLock.tryConvertToWriteLock(stamp);
            if (writeStamp != 0L) {
              stamp = writeStamp;
              x = newX;
              y = newY;
              return;
            } else {
              stampedLock.unlockRead(stamp);
              stamp = stampedLock.writeLock();
            }
          }
        } finally {
          stampedLock.unlock(stamp);
        }
      }
    }

    Point point = new Point();
    point.move(3, 4);
    System.out.println("  Distance from origin: " + point.distanceFromOrigin());

    System.out.println("""

        StampedLock advantages:
        - Optimistic reads (tryOptimisticRead): No lock, just validates
        - Lock conversion (read -> write without releasing)
        - Better performance for read-heavy workloads

        StampedLock caveats:
        - NOT reentrant (unlike ReentrantReadWriteLock)
        - More complex API (stamps must be tracked)
        - No Condition support

        Use StampedLock when:
        - Very read-heavy workload
        - Short read operations
        - Can handle retry logic
        """);
  }

  static void performanceComparison() throws Exception {
    System.out.println("=== Performance: When ReadWriteLock Wins ===");

    int reads = 10000;
    int writes = 100;
    int threads = 8;

    // Simulate read-heavy workload comparison
    System.out.println("""
        Read-heavy workload (10000 reads, 100 writes):

        synchronized: All operations sequential
        - Even reads block each other

        ReadWriteLock: Reads can be concurrent
        - 8 threads can read simultaneously
        - Only writes need exclusive access

        Typical improvement: 2-8x for read-heavy workloads

        When ReadWriteLock does NOT help:
        - Write-heavy workloads (writes block everything)
        - Short operations (lock overhead dominates)
        - Low contention (no threads waiting)
        """);
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        When to use ReadWriteLock:
        - Read-heavy, write-light workloads
        - Read operations are not trivial
        - High contention for shared data
        - Caches, configuration, lookup tables

        When NOT to use:
        - Write-heavy workloads
        - Very short operations
        - Low contention
        - Need simplicity

        DO:
        - Use try-finally for both read and write locks
        - Consider fair mode if writer starvation is an issue
        - Use lock downgrading when appropriate
        - Prefer StampedLock for read-heavy with short reads

        DON'T:
        - Try to upgrade read lock to write lock (deadlock)
        - Hold locks during I/O
        - Use when simpler solutions work

        Writer starvation:
        - Many readers can starve writers
        - Fair mode helps but reduces throughput
        - Design consideration for your use case

        Lock selection guide:
        +---------------------+----------------------------------+
        | Scenario            | Recommended                      |
        +---------------------+----------------------------------+
        | Simple exclusion    | synchronized or ReentrantLock    |
        | Read-heavy, complex | ReentrantReadWriteLock           |
        | Read-heavy, short   | StampedLock optimistic           |
        | High contention     | Consider lock-free (Atomic)      |
        +---------------------+----------------------------------+
        """);
  }
}
