package org.nkcoder.j8.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * AtomicIntegerArray Example
 *
 * AtomicIntegerArray provides lock-free, thread-safe operations on an array of integers.
 * Each element can be updated atomically using CAS operations.
 *
 * Key characteristics:
 * 1. Thread-safe array with atomic element operations
 * 2. Lock-free using CAS (Compare-And-Swap)
 * 3. Each array element can be updated independently
 * 4. Better than synchronizing on regular int[]
 * 5. Fixed size (set at construction)
 *
 * Common operations:
 * - get(index) / set(index, value): Read/write element
 * - getAndSet(index, value): Atomic swap
 * - incrementAndGet(index) / decrementAndGet(index): Atomic ++/--
 * - addAndGet(index, delta): Atomic addition
 * - compareAndSet(index, expect, update): CAS operation
 *
 * AtomicIntegerArray vs synchronized int[]:
 *
 * AtomicIntegerArray:
 * ✓ Lock-free, non-blocking per element
 * ✓ Better performance with concurrent access to different elements
 * ✓ No lock contention between operations on different indices
 * ✓ Fine-grained atomicity (per element)
 * ✗ Fixed size
 * ✗ Slightly more memory overhead
 *
 * synchronized int[]:
 * ✓ Simpler syntax
 * ✓ Works with any array operations
 * ✗ Coarse-grained locking (whole array or manual per-element)
 * ✗ Lock contention even for different indices
 * ✗ Risk of deadlock with multiple arrays
 *
 * When to use:
 * ✓ Concurrent counters per category/bucket
 * ✓ Histogram data
 * ✓ Parallel aggregation
 * ✓ Lock-free data structures
 * ✗ Not for large arrays (consider regular array with striping)
 *
 * Java 8+ alternative:
 * - LongAdder[]: Array of LongAdder for better high-contention performance
 *
 * This example demonstrates:
 * - 10 threads concurrently incrementing array elements
 * - Each thread increments each array position 100 times
 * - No synchronization needed - AtomicIntegerArray handles thread safety
 * - Expected: Each element should be 1000 (10 threads * 100 increments)
 */
public class AtomicIntegerArrayExample {

  public static void main(String[] args) throws InterruptedException {
    // Create atomic array with 10 elements (all initialized to 0)
    AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(10);

    // Use virtual threads (Java 21+) or fixed thread pool for older versions
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    // Each of 10 threads will increment array elements
    for (int i = 0; i < 10; i++) {
      executorService.submit(() -> {
        // Each thread increments all 10 positions 100 times
        for (int j = 0; j < 100; j++) {
          // Atomically increment element at index (j % 10)
          // No lock needed - operation is atomic per element
          // Different threads can update different indices concurrently
          atomicIntegerArray.incrementAndGet(j % 10);
        }
      });
    }

    // Better synchronization than Thread.sleep()
    executorService.shutdown();
    executorService.awaitTermination(5, TimeUnit.SECONDS);

    // Print array - each element should be 1000 (10 threads * 100)
    // Format: [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
    System.out.println("array: " + atomicIntegerArray);
  }
}
