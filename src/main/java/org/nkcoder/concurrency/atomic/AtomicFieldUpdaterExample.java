package org.nkcoder.concurrency.atomic;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * AtomicFieldUpdater Example
 *
 * AtomicFieldUpdater allows atomic operations on volatile fields of existing classes
 * without modifying the class to use Atomic* types directly.
 *
 * Key characteristics:
 * 1. Operates on volatile fields via reflection
 * 2. Zero memory overhead per instance (no extra object wrapping)
 * 3. Useful when you have many instances and don't want overhead of AtomicInteger per instance
 * 4. Field must be volatile and accessible
 * 5. Type-safe through generics
 *
 * When to use AtomicFieldUpdater vs AtomicInteger:
 *
 * Use AtomicInteger/AtomicReference:
 * ✓ Default choice for new code
 * ✓ Cleaner API, easier to use
 * ✓ Type-safe at compile time
 * ✓ When you have few instances
 *
 * Use AtomicFieldUpdater:
 * ✓ Memory optimization for classes with many instances
 * ✓ Retrofitting existing classes (can't change field types)
 * ✓ When 8-16 bytes per instance matters (e.g., millions of objects)
 * ✓ Performance-critical code with memory constraints
 *
 * Requirements for field:
 * - Must be volatile
 * - Must not be static
 * - Must not be final
 * - Must be accessible (consider package/module visibility)
 *
 * Memory comparison:
 * - AtomicInteger: 16 bytes object overhead + 4 bytes int = 20-24 bytes per instance
 * - volatile int + shared updater: 4 bytes per instance, one updater shared
 * - For 1 million objects: saves ~16-20 MB
 *
 * Performance:
 * - Slightly slower than AtomicInteger (reflection overhead)
 * - Same CAS semantics under the hood
 * - Negligible for most applications
 *
 * Java 9+ VarHandle alternative:
 * VarHandle is the modern replacement with better performance:
 * - No reflection overhead
 * - More flexible operations
 * - Better JIT optimization
 * - Consider migrating to VarHandle for Java 9+
 *
 * This example demonstrates:
 * - Updating a volatile field atomically using field updater
 * - Comparing with AtomicInteger to verify correctness
 * - Both should have same final value (proves atomicity)
 * - Memory-efficient alternative for classes with many instances
 */
public class AtomicFieldUpdaterExample {

  public static class Student {
    int id;
    // Field MUST be volatile for AtomicFieldUpdater to work
    // This ensures visibility and enables CAS operations
    volatile int score;
  }

  public static void main(String[] args) throws InterruptedException {
    // Use virtual threads for better resource efficiency (Java 21+)
    // For older Java versions, use: Executors.newCachedThreadPool()
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    // Regular AtomicInteger for comparison
    AtomicInteger count = new AtomicInteger(0);

    // Create field updater for Student.score field
    // This updater can be shared across all Student instances
    // Parameters: target class, field name
    AtomicIntegerFieldUpdater<Student> updaterCount =
        AtomicIntegerFieldUpdater.newUpdater(Student.class, "score");

    Random random = new Random();

    Student student = new Student();

    // Submit 1000 tasks that randomly increment both counters
    for (int i = 0; i < 1000; i++) {
      executorService.submit(() -> {
        // 50% chance to increment
        if (random.nextDouble() > 0.5) {
          // Increment AtomicInteger
          count.incrementAndGet();
          // Increment volatile field using updater (same atomicity guarantees)
          updaterCount.incrementAndGet(student);
        }
      });
    }

    executorService.shutdown();
    boolean terminated = executorService.awaitTermination(3, TimeUnit.SECONDS);

    // count and updaterCount should always have the same value
    // This proves that field updater provides same atomicity as AtomicInteger
    System.out.println(
        "count: "
            + count.get()
            + ", updateCount: "
            + updaterCount.get(student)
            + ", terminated: "
            + terminated);

    // Expected: Both values are identical, demonstrating atomic field updates
  }
}
