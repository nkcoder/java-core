package org.nkcoder.concurrency.locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock: Explicit lock with more features than synchronized.
 *
 * <p><strong>Java 25 Note:</strong> Still recommended when you need tryLock, interruptible locking, fairness, or
 * multiple Conditions. For simpler cases, {@code synchronized} or {@code Atomic*} classes may suffice.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Must explicitly lock() and unlock() (use try-finally)
 *   <li>tryLock() for non-blocking lock attempts
 *   <li>lockInterruptibly() allows interruption while waiting
 *   <li>Condition objects for wait/notify with multiple conditions
 *   <li>Fair mode option (threads acquire in FIFO order)
 * </ul>
 *
 * <p>Interview tip: Know when to use ReentrantLock over synchronized, and always use try-finally pattern.
 */
public class ReentrantLockExample {

    static void main(String[] args) throws Exception {
        basicUsage();
        tryLockExample();
        lockInterruptibly();
        conditionExample();
        fairnessExample();
        reentrantBehavior();
        bestPractices();
    }

    static void basicUsage() throws Exception {
        System.out.println("=== Basic Usage ===");

        ReentrantLock lock = new ReentrantLock();

        class Counter {
            private int count = 0;

            void increment() {
                lock.lock(); // Acquire lock
                try {
                    count++;
                } finally {
                    lock.unlock(); // ALWAYS unlock in finally!
                }
            }

            int getCount() {
                lock.lock();
                try {
                    return count;
                } finally {
                    lock.unlock();
                }
            }
        }

        Counter counter = new Counter();

        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(counter::increment);
            }
        }

        System.out.println("  Counter: " + counter.getCount());

        System.out.println("""

        CRITICAL: Always use try-finally pattern!

        lock.lock();
        try {
            // critical section
        } finally {
            lock.unlock();  // Ensures unlock even if exception
        }

        Without finally: exception leaves lock held = deadlock!
        """);
    }

    static void tryLockExample() throws Exception {
        System.out.println("=== tryLock() - Non-blocking ===");

        ReentrantLock lock = new ReentrantLock();

        // Thread 1 holds lock for a while
        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("    Holder: Got lock, holding for 500ms...");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
                System.out.println("    Holder: Released lock");
            }
        });

        holder.start();
        Thread.sleep(50); // Let holder acquire lock

        // tryLock() - returns immediately
        System.out.println("  tryLock() without timeout:");
        if (lock.tryLock()) {
            try {
                System.out.println("    Acquired lock immediately");
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("    Could not acquire lock (returned false)");
        }

        // tryLock(timeout) - waits up to timeout
        System.out.println("\n  tryLock(200ms) with timeout:");
        if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
            try {
                System.out.println("    Acquired lock within timeout");
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("    Timeout! Could not acquire lock in 200ms");
        }

        // tryLock with longer timeout should succeed
        System.out.println("\n  tryLock(1000ms) with longer timeout:");
        if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
            try {
                System.out.println("    Acquired lock within timeout");
            } finally {
                lock.unlock();
            }
        }

        holder.join();

        System.out.println("""

        tryLock() use cases:
        - Avoid deadlock by backing off
        - Implement timeouts
        - Poll for lock availability
        - Graceful degradation when contended
        """);
    }

    static void lockInterruptibly() throws Exception {
        System.out.println("=== lockInterruptibly() ===");

        ReentrantLock lock = new ReentrantLock();

        // Thread holds lock
        lock.lock();

        Thread waiter = new Thread(() -> {
            try {
                System.out.println("    Waiter: Trying to acquire lock (interruptibly)...");
                lock.lockInterruptibly(); // Can be interrupted while waiting
                try {
                    System.out.println("    Waiter: Got lock!");
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                System.out.println("    Waiter: Interrupted while waiting for lock!");
            }
        });

        waiter.start();
        Thread.sleep(100);

        System.out.println("  Main: Interrupting waiter...");
        waiter.interrupt();

        waiter.join();
        lock.unlock();

        System.out.println("""

        lockInterruptibly() vs lock():
        - lock(): Cannot be interrupted while waiting
        - lockInterruptibly(): Throws InterruptedException if interrupted

        Use lockInterruptibly() when:
        - Thread should be cancellable
        - Need responsive shutdown
        - Implementing timeout with interrupt fallback
        """);
    }

    static void conditionExample() throws Exception {
        System.out.println("=== Condition (Better wait/notify) ===");

        class BoundedBuffer<T> {
            private final Object[] items;
            private int putIndex, takeIndex, count;

            private final ReentrantLock lock = new ReentrantLock();
            private final Condition notEmpty = lock.newCondition();
            private final Condition notFull = lock.newCondition();

            BoundedBuffer(int capacity) {
                items = new Object[capacity];
            }

            void put(T item) throws InterruptedException {
                lock.lock();
                try {
                    while (count == items.length) {
                        System.out.println("      Buffer full, waiting on notFull...");
                        notFull.await(); // Wait specifically for "not full"
                    }
                    items[putIndex] = item;
                    putIndex = (putIndex + 1) % items.length;
                    count++;
                    System.out.println("      Put: " + item + ", count=" + count);
                    notEmpty.signal(); // Signal specifically "not empty"
                } finally {
                    lock.unlock();
                }
            }

            @SuppressWarnings("unchecked")
            T take() throws InterruptedException {
                lock.lock();
                try {
                    while (count == 0) {
                        System.out.println("      Buffer empty, waiting on notEmpty...");
                        notEmpty.await(); // Wait specifically for "not empty"
                    }
                    T item = (T) items[takeIndex];
                    takeIndex = (takeIndex + 1) % items.length;
                    count--;
                    System.out.println("      Take: " + item + ", count=" + count);
                    notFull.signal(); // Signal specifically "not full"
                    return item;
                } finally {
                    lock.unlock();
                }
            }
        }

        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(2);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 4; i++) {
                    buffer.put(i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 4; i++) {
                    buffer.take();
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        System.out.println("""

        Condition advantages over wait/notify:
        - Multiple conditions per lock
        - More precise signaling (signal specific waiters)
        - Clearer code (named conditions)
        - await() methods: await(), awaitNanos(), awaitUntil()
        """);
    }

    static void fairnessExample() throws Exception {
        System.out.println("=== Fair vs Non-Fair Locks ===");

        // Non-fair (default) - better throughput, possible starvation
        ReentrantLock nonFairLock = new ReentrantLock(false);

        // Fair - FIFO order, no starvation, lower throughput
        ReentrantLock fairLock = new ReentrantLock(true);

        System.out.println("  Non-fair lock isFair: " + nonFairLock.isFair());
        System.out.println("  Fair lock isFair: " + fairLock.isFair());

        System.out.println("""

        Non-fair (default):
        - Thread can "barge" ahead of waiting threads
        - Better throughput (less context switching)
        - Risk of starvation for some threads

        Fair:
        - Threads acquire lock in FIFO order
        - No starvation
        - Lower throughput (more overhead)

        When to use fair:
        - Critical that no thread starves
        - Predictable behavior required
        - Debugging synchronization issues

        Default (non-fair) is usually better for performance.
        """);
    }

    static void reentrantBehavior() throws Exception {
        System.out.println("=== Reentrant Behavior ===");

        ReentrantLock lock = new ReentrantLock();

        lock.lock();
        System.out.println("  Hold count after 1st lock: " + lock.getHoldCount());

        lock.lock(); // Same thread can acquire again
        System.out.println("  Hold count after 2nd lock: " + lock.getHoldCount());

        lock.lock();
        System.out.println("  Hold count after 3rd lock: " + lock.getHoldCount());

        System.out.println("  isHeldByCurrentThread: " + lock.isHeldByCurrentThread());

        lock.unlock();
        System.out.println("  Hold count after 1st unlock: " + lock.getHoldCount());

        lock.unlock();
        lock.unlock();
        System.out.println("  Hold count after all unlocks: " + lock.getHoldCount());

        System.out.println("""

        Reentrant means:
        - Same thread can acquire lock multiple times
        - Must unlock same number of times
        - Hold count tracks number of acquisitions
        - Useful for recursive methods, calling helper methods
        """);
    }

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        When to use ReentrantLock over synchronized:
        - Need tryLock() for non-blocking attempts
        - Need lockInterruptibly() for cancellation
        - Need multiple Condition objects
        - Need fair ordering
        - Need to query lock state (isLocked, getHoldCount)

        When synchronized is fine:
        - Simple locking needs
        - No need for advanced features
        - Want simpler syntax (less error-prone)

        DO:
        - ALWAYS use try-finally for unlock()
        - Consider tryLock() for deadlock avoidance
        - Use fair locks only when necessary
        - Prefer lock per data, not lock per method

        DON'T:
        - Forget to unlock (deadlock)
        - Hold lock during I/O or long operations
        - Use fair locks by default (performance)
        - Return from method without unlocking

        Lock comparison:
        +-------------------+-------------+---------------+
        | Feature           | synchronized| ReentrantLock |
        +-------------------+-------------+---------------+
        | Syntax            | Simple      | Verbose       |
        | Try lock          | No          | Yes           |
        | Interruptible     | No          | Yes           |
        | Fairness          | No          | Yes           |
        | Multiple conditions| No         | Yes           |
        | Lock query        | Limited     | Full          |
        +-------------------+-------------+---------------+
        """);
    }
}
