package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger: Lock-free thread-safe integer operations.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Uses CAS (Compare-And-Swap) hardware instructions
 *   <li>No locks, no blocking - better performance under contention
 *   <li>Individual operations are atomic, not compound operations
 *   <li>Great for counters, sequence generators, statistics
 * </ul>
 *
 * <p>Interview tip: Understand CAS and why incrementAndGet is atomic but get() followed by set() is not.
 */
public class AtomicIntegerExample {

    static void main(String[] args) throws Exception {
        whyAtomicNeeded();
        basicOperations();
        atomicIncrementDecrement();
        compareAndSet();
        accumulateAndUpdate();
        atomicVsSynchronized();
        bestPractices();
    }

    static void whyAtomicNeeded() throws Exception {
        System.out.println("=== Why Atomic Operations Are Needed ===");

        // Non-atomic increment (BROKEN)
        class BrokenCounter {
            private int count = 0;

            void increment() {
                count++; // NOT atomic! Read-modify-write
            }

            int get() {
                return count;
            }
        }

        // Atomic increment (CORRECT)
        class SafeCounter {
            private final AtomicInteger count = new AtomicInteger(0);

            void increment() {
                count.incrementAndGet(); // Atomic operation
            }

            int get() {
                return count.get();
            }
        }

        int iterations = 10_000;
        int threads = 10;

        // Test broken counter
        BrokenCounter broken = new BrokenCounter();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < iterations; i++) {
                executor.submit(broken::increment);
            }
        }
        System.out.println("  Broken counter: " + broken.get() + " (expected " + iterations + ")");

        // Test safe counter
        SafeCounter safe = new SafeCounter();
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < iterations; i++) {
                executor.submit(safe::increment);
            }
        }
        System.out.println("  Atomic counter: " + safe.get() + " (expected " + iterations + ")");

        System.out.println("""

        Why count++ is not atomic:
        1. Read current value from memory
        2. Add 1 to the value
        3. Write new value back to memory

        Two threads can read the same value, both add 1,
        and both write back - losing one increment!
        """);
    }

    static void basicOperations() {
        System.out.println("=== Basic Operations ===");

        AtomicInteger atomic = new AtomicInteger(10);

        // get() - read current value
        System.out.println("  get(): " + atomic.get());

        // set() - write new value
        atomic.set(20);
        System.out.println("  set(20): " + atomic.get());

        // getAndSet() - atomically set and return old value
        int old = atomic.getAndSet(30);
        System.out.println("  getAndSet(30): old=" + old + ", new=" + atomic.get());

        // lazySet() - eventual write (no memory barrier, faster)
        atomic.lazySet(40);
        System.out.println("  lazySet(40): " + atomic.get());

        System.out.println();
    }

    static void atomicIncrementDecrement() {
        System.out.println("=== Atomic Increment/Decrement ===");

        AtomicInteger counter = new AtomicInteger(0);

        // incrementAndGet() - add 1, return NEW value
        System.out.println("  incrementAndGet(): " + counter.incrementAndGet()); // 1

        // getAndIncrement() - return OLD value, then add 1
        System.out.println("  getAndIncrement(): " + counter.getAndIncrement()); // 1 (now 2)

        // decrementAndGet() - subtract 1, return NEW value
        System.out.println("  decrementAndGet(): " + counter.decrementAndGet()); // 1

        // getAndDecrement() - return OLD value, then subtract 1
        System.out.println("  getAndDecrement(): " + counter.getAndDecrement()); // 1 (now 0)

        // addAndGet() - add N, return NEW value
        System.out.println("  addAndGet(5): " + counter.addAndGet(5)); // 5

        // getAndAdd() - return OLD value, then add N
        System.out.println("  getAndAdd(3): " + counter.getAndAdd(3)); // 5 (now 8)

        System.out.println("  Final value: " + counter.get()); // 8

        System.out.println("""

        Pattern:
        - getAndXxx(): Returns OLD value, then applies operation
        - xxxAndGet(): Applies operation, then returns NEW value

        All operations are atomic (single CAS instruction).
        """);
    }

    static void compareAndSet() {
        System.out.println("=== Compare-And-Set (CAS) ===");

        AtomicInteger atomic = new AtomicInteger(10);

        // compareAndSet(expected, update)
        // Only updates if current value equals expected
        boolean success1 = atomic.compareAndSet(10, 20); // expect 10, set to 20
        System.out.println("  CAS(10, 20): " + success1 + ", value=" + atomic.get());

        boolean success2 = atomic.compareAndSet(10, 30); // expect 10, but it's 20 now
        System.out.println("  CAS(10, 30): " + success2 + ", value=" + atomic.get());

        // CAS loop pattern - retry until success
        System.out.println("\n  CAS loop pattern (multiply by 2):");
        int oldValue, newValue;
        do {
            oldValue = atomic.get();
            newValue = oldValue * 2;
        } while (!atomic.compareAndSet(oldValue, newValue));
        System.out.println("    Result: " + atomic.get());

        System.out.println("""

        CAS is the foundation of lock-free algorithms:
        1. Read current value
        2. Compute new value
        3. CAS: if still same, update; else retry

        This is what incrementAndGet() does internally!
        """);
    }

    static void accumulateAndUpdate() {
        System.out.println("=== Accumulate and Update ===");

        AtomicInteger atomic = new AtomicInteger(10);

        // updateAndGet(function) - apply function atomically
        int result1 = atomic.updateAndGet(x -> x * 2);
        System.out.println("  updateAndGet(x -> x * 2): " + result1); // 20

        // getAndUpdate(function) - return old, then apply
        int result2 = atomic.getAndUpdate(x -> x + 5);
        System.out.println("  getAndUpdate(x -> x + 5): old=" + result2 + ", new=" + atomic.get());

        // accumulateAndGet(value, function) - combine with another value
        int result3 = atomic.accumulateAndGet(3, (current, x) -> current * x);
        System.out.println("  accumulateAndGet(3, (c,x) -> c*x): " + result3); // 25 * 3 = 75

        // getAndAccumulate(value, function)
        int result4 = atomic.getAndAccumulate(2, Integer::sum);
        System.out.println("  getAndAccumulate(2, Integer::sum): old=" + result4); // 75 (now 77)

        System.out.println("  Final value: " + atomic.get());

        System.out.println("""

        These methods handle the CAS loop internally:
        - updateAndGet(): f(current) -> new
        - accumulateAndGet(): f(current, given) -> new

        Cleaner than writing your own CAS loop!
        """);
    }

    static void atomicVsSynchronized() throws Exception {
        System.out.println("=== Atomic vs Synchronized ===");

        int operations = 1_000_000;

        // Synchronized counter
        class SyncCounter {
            private int count = 0;

            synchronized void increment() {
                count++;
            }

            synchronized int get() {
                return count;
            }
        }

        // Atomic counter
        AtomicInteger atomicCounter = new AtomicInteger(0);

        // Benchmark synchronized
        SyncCounter sync = new SyncCounter();
        long start1 = System.nanoTime();
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < operations; i++) {
                executor.submit(sync::increment);
            }
        }
        long syncTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start1);

        // Benchmark atomic
        long start2 = System.nanoTime();
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < operations; i++) {
                executor.submit(atomicCounter::incrementAndGet);
            }
        }
        long atomicTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start2);

        System.out.println("  Synchronized: " + syncTime + "ms, count=" + sync.get());
        System.out.println("  Atomic: " + atomicTime + "ms, count=" + atomicCounter.get());

        System.out.println("""

        +-------------------+-----------------------+-----------------------+
        | Feature           | synchronized          | AtomicInteger         |
        +-------------------+-----------------------+-----------------------+
        | Mechanism         | Lock (monitor)        | CAS (hardware)        |
        | Blocking          | Yes                   | No (spins/retries)    |
        | Compound ops      | Yes (any code block)  | Limited (single op)   |
        | Contention        | Slower (waiting)      | Faster (retrying)     |
        | Memory visibility | Full barrier          | Volatile semantics    |
        +-------------------+-----------------------+-----------------------+

        Use Atomic when:
        - Single variable updates
        - Simple increment/compare operations
        - High contention expected

        Use synchronized when:
        - Multiple variables must update together
        - Complex logic in critical section
        - Need to wait on conditions
        """);
    }

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        DO:
        - Use AtomicInteger for counters, sequence numbers
        - Use updateAndGet/accumulateAndGet for custom operations
        - Prefer atomic classes for single-variable thread safety

        DON'T:
        - Use for compound operations (check-then-act)
        - Assume multiple atomic ops are atomic together:

          // BROKEN - not atomic as a whole!
          if (counter.get() == 0) {
              counter.set(1);
          }

          // CORRECT - single atomic operation
          counter.compareAndSet(0, 1);

        Related classes:
        - AtomicLong, AtomicBoolean - other primitives
        - AtomicReference<T> - for objects
        - AtomicIntegerArray - atomic array operations
        - LongAdder/LongAccumulator - better for high contention

        Performance tip:
        For very high contention counters, LongAdder is faster
        than AtomicLong because it reduces contention by spreading
        updates across multiple cells.
        """);
    }
}
