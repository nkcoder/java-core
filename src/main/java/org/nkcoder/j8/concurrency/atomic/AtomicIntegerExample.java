package org.nkcoder.j8.concurrency.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger Example
 *
 * AtomicInteger provides lock-free, thread-safe operations on integer values
 * using Compare-And-Swap (CAS) hardware instructions.
 *
 * Key characteristics:
 * 1. Lock-free - No locks, uses CPU CAS instructions
 * 2. Non-blocking - Threads never wait for locks
 * 3. Thread-safe - All operations are atomic
 * 4. Better performance than synchronized for simple operations
 * 5. Provides atomicity + visibility (like volatile + atomicity)
 *
 * Common operations:
 * - get() / set(value): Read/write value
 * - getAndSet(value): Atomic swap
 * - incrementAndGet() / decrementAndGet(): Atomic ++/--
 * - getAndIncrement() / getAndDecrement(): Atomic ++/--, return old value
 * - addAndGet(delta) / getAndAdd(delta): Atomic addition
 * - compareAndSet(expect, update): CAS operation (foundation of all others)
 *
 * How CAS (Compare-And-Swap) works:
 * 1. Read current value
 * 2. Compute new value
 * 3. CAS: If current value hasn't changed, update it atomically
 * 4. If CAS fails (value changed), retry from step 1
 * 5. All atomic operations use CAS internally
 *
 * AtomicInteger vs synchronized:
 *
 * AtomicInteger:
 * ✓ Lock-free, non-blocking
 * ✓ Better performance for simple operations
 * ✓ No thread context switching overhead
 * ✓ Suitable for counters, accumulators
 * ✗ Limited to single-variable operations
 * ✗ CAS retry loop under high contention
 *
 * synchronized:
 * ✓ Works for compound operations on multiple variables
 * ✓ Predictable behavior under contention
 * ✗ Blocking (threads wait for locks)
 * ✗ Context switching overhead
 * ✗ Risk of deadlock
 *
 * When to use:
 * ✓ Counters and accumulators
 * ✓ Sequence generators
 * ✓ Lock-free algorithms
 * ✓ High-contention scenarios (better than synchronized for simple ops)
 * ✗ Not for compound operations (use synchronized or locks)
 *
 * Java 8+ alternatives for aggregation:
 * - LongAdder: Better performance for high-contention counters (splits into cells)
 * - LongAccumulator: Flexible aggregation with custom operations
 *
 * This example demonstrates:
 * - 100 threads concurrently adding to AtomicInteger
 * - No synchronization needed - AtomicInteger handles thread safety
 * - Final value should be 1000 (100 threads * 10 each)
 */
public class AtomicIntegerExample {

  public static void main(String[] args) throws InterruptedException {
    // AtomicInteger initialized to 0
    // Provides atomic operations without explicit synchronization
    AtomicInteger counter = new AtomicInteger(0);

    // Create 100 threads, each adding 10 to the counter
    // No synchronization needed - AtomicInteger is thread-safe
    for (int i = 0; i < 100; i++) {
      new Thread(() -> {
        // addAndGet atomically adds 10 and returns new value
        // Internally uses CAS (Compare-And-Swap) loop until successful
        // Equivalent to: counter = counter + 10 (but thread-safe)
        counter.addAndGet(10);
      }).start();
    }

    // Wait for threads to complete (crude synchronization)
    // Better approach: use CountDownLatch, join(), or ExecutorService.awaitTermination()
    Thread.sleep(200);

    // Should print 1000 (100 threads * 10)
    // Without AtomicInteger, would get race conditions and incorrect result
    System.out.println("counter value: " + counter.get());
  }
}
