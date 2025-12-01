package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicStampedReference Example - Solving the ABA Problem
 *
 * AtomicStampedReference pairs a reference with an integer "stamp" (version number)
 * to detect if a value has changed even if it changed back to the original value.
 *
 * The ABA Problem:
 * 1. Thread 1 reads value A
 * 2. Thread 2 changes A → B → A (back to original)
 * 3. Thread 1's CAS succeeds (thinks nothing changed)
 * 4. But the value DID change (could cause logic errors)
 *
 * Example scenario:
 * - Stack top pointer points to Node A
 * - Thread 1 reads A, plans to pop it
 * - Thread 2 pops A, pops B, pushes A back (A is now different object!)
 * - Thread 1's CAS succeeds but operates on wrong Node A
 *
 * Solution: AtomicStampedReference
 * - Maintains reference + stamp (version counter)
 * - CAS checks BOTH reference and stamp
 * - Even if reference returns to A, stamp is different
 * - Detects any intermediate changes
 *
 * Key characteristics:
 * 1. Pairs reference with integer stamp/version
 * 2. CAS operates on both reference AND stamp
 * 3. Solves ABA problem by tracking versions
 * 4. Stamp typically increments on each change
 * 5. Lock-free but more complex than AtomicReference
 *
 * Common operations:
 * - get(stampHolder): Returns reference and stores stamp in int[] array
 * - getReference() / getStamp(): Get reference or stamp separately
 * - compareAndSet(expectedRef, newRef, expectedStamp, newStamp): Atomic CAS on both
 * - set(newRef, newStamp): Unconditional update
 *
 * When to use:
 * ✓ Lock-free data structures (stacks, queues)
 * ✓ When ABA problem can occur
 * ✓ Need to detect any change in history
 * ✓ Optimistic locking patterns
 *
 * When NOT to use:
 * ✗ Simple reference swapping (use AtomicReference)
 * ✗ When ABA is not a concern
 * ✗ Stamp overflow is acceptable
 *
 * AtomicStampedReference vs AtomicReference:
 * - AtomicReference: Can't detect A→B→A
 * - AtomicStampedReference: Detects all changes via stamp
 * - AtomicMarkedReference: Boolean mark instead of int stamp
 *
 * This example demonstrates:
 * - Account balance with version tracking
 * - Multiple recharge threads incrementing balance and stamp
 * - One spend thread decrementing balance and stamp
 * - CAS with stamp prevents lost updates from concurrent modifications
 * - Proper stamp management (reading current stamp before CAS)
 */
public class AtomicStampedReferenceExample {

  public static void main(String[] args) throws InterruptedException {

    // Create AtomicStampedReference with initial value 15 and stamp 0
    // Stamp acts as version number - increments on each successful change
    AtomicStampedReference<Integer> balance = new AtomicStampedReference<>(15, 0);

    // Java 21: Consider Executors.newVirtualThreadPerTaskExecutor()
    try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {

      // 3 recharge threads: add 20 when balance < 20
      for (int i = 0; i < 3; i++) {
        executorService.submit(() -> {
          for (int j = 0; j < 1000; j++) {
            // Must read current stamp before CAS attempt
            int currentStamp = balance.getStamp();
            int money = balance.getReference();

            if (money < 20) {
              // CAS checks BOTH value and stamp
              // Only succeeds if neither changed since we read them
              // Increment stamp to mark this version
              if (balance.compareAndSet(money, money + 20, currentStamp, currentStamp + 1)) {
                System.out.println("Recharged: balance=" + balance.getReference()
                    + ", stamp=" + balance.getStamp());
              }
              // If CAS fails, loop retries with new stamp/value
            }
          }
        });
      }

      // 1 spend thread: subtract 10 when balance > 10
      executorService.submit(() -> {
        for (int j = 0; j < 100; j++) {
          // Always read current stamp - it changes with each successful operation
          int stamp2 = balance.getStamp();
          int money = balance.getReference();

          if (money > 10) {
            // CAS with stamp prevents ABA problem
            // If balance changed from 25→15→25, stamp is different
            // So CAS fails (detects intermediate change)
            if (balance.compareAndSet(money, money - 10, stamp2, stamp2 + 1)) {
              System.out.println("Spent 10: balance=" + balance.getReference()
                  + ", stamp=" + balance.getStamp());
            }
          } else {
            System.out.println("Insufficient balance: " + money);
          }
        }
      });

      // Proper shutdown
      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    }

    System.out.println("Final balance: " + balance.getReference()
        + ", final stamp: " + balance.getStamp());
  }
}
