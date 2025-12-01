package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicReference Example
 *
 * AtomicReference provides atomic operations on object references, enabling lock-free
 * algorithms for managing shared object state.
 *
 * Key characteristics:
 * 1. Atomically updates object references (not primitive values)
 * 2. Lock-free using CAS (Compare-And-Swap)
 * 3. Works with any object type (generics)
 * 4. Provides atomicity + visibility for reference changes
 * 5. Does NOT make the referenced object immutable or thread-safe
 *
 * Important: AtomicReference only makes the REFERENCE atomic, not the object itself
 * - Changing which object the reference points to is atomic
 * - Modifying the referenced object's fields is NOT atomic
 * - Best used with immutable objects
 *
 * Common operations:
 * - get() / set(value): Read/write reference
 * - getAndSet(newValue): Atomic swap, returns old value
 * - compareAndSet(expect, update): CAS operation
 * - updateAndGet(updateFunction): Atomic functional update (Java 8+)
 * - getAndUpdate(updateFunction): Atomic update, returns old value
 *
 * AtomicReference vs synchronized:
 *
 * AtomicReference:
 * ✓ Lock-free, non-blocking
 * ✓ Better performance for reference swapping
 * ✓ Good for immutable object patterns
 * ✗ Only reference is atomic, not object state
 * ✗ CAS retry loop can be complex
 *
 * synchronized:
 * ✓ Can protect both reference and object state
 * ✓ Simpler for compound operations
 * ✗ Blocking, lock contention
 * ✗ Risk of deadlock
 *
 * Common patterns:
 * 1. Immutable object swap (this example with Integer)
 * 2. Copy-on-write collections
 * 3. Lock-free stack/queue implementations
 * 4. Versioned references
 *
 * ABA Problem:
 * - Thread 1 reads A
 * - Thread 2 changes A→B→A
 * - Thread 1's CAS succeeds (thinks nothing changed)
 * - Use AtomicStampedReference to solve this
 *
 * This example demonstrates:
 * - Account balance management with atomic reference
 * - Multiple threads recharging (add money)
 * - One thread spending (subtract money)
 * - CAS ensures only one operation succeeds when balance changes
 * - Using Integer (immutable) makes reference updates safe
 */
public class AtomicReferenceExample {

  public static void main(String[] args) throws InterruptedException {
    // AtomicReference holding an Integer (immutable object)
    // Initial balance: 15
    AtomicReference<Integer> balance = new AtomicReference<>(15);

    // Java 21: Use virtual threads for better resource efficiency
    // For Java 8-20: Use Executors.newFixedThreadPool(5)
    try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {

      // 3 recharge threads: each adds 20 when balance < 20
      for (int i = 0; i < 3; i++) {
        executorService.submit(() -> {
          for (int j = 0; j < 1000; j++) {
            // Read current balance
            int money = balance.get();
            if (money > 20) {
              System.out.println("money > 20, no need to recharge");
            } else {
              // CAS: only update if balance hasn't changed since we read it
              // If another thread changed it, this returns false and we retry
              if (balance.compareAndSet(money, money + 20)) {
                System.out.println("money < 20, recharge done, money: " + balance.get());
              }
            }
          }
        });
      }

      // 1 spend thread: subtracts 10 when balance >= 10
      executorService.submit(() -> {
        for (int i = 0; i < 100; i++) {
          int money = balance.get();
          if (money < 10) {
            System.out.println("money < 10, cannot expend.");
          } else {
            // CAS: only subtract if balance hasn't changed
            if (balance.compareAndSet(money, money - 10)) {
              System.out.println("expend 10, remaining money: " + balance.get());
            }
          }
        }
      });

      // Proper shutdown with timeout
      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    }

    System.out.println("Final balance: " + balance.get());
  }
}
