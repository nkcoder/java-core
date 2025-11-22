package org.nkcoder.j8.concurrency;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CopyOnWriteArrayList Example
 *
 * CopyOnWriteArrayList is a thread-safe variant of ArrayList where all mutative operations
 * (add, set, remove, etc.) create a fresh copy of the underlying array.
 *
 * Key characteristics:
 * 1. Thread-safe without explicit synchronization in client code
 * 2. All write operations (add/set/remove) create a new copy of the array
 * 3. Read operations (get/iterator) work on a snapshot - never throw ConcurrentModificationException
 * 4. Excellent for read-heavy scenarios with few writes
 * 5. Write operations are expensive (O(n) space and time due to copying)
 * 6. Iterator is a snapshot - won't see modifications made after iterator creation
 *
 * Trade-offs:
 * ✓ Great for: Many reads, few writes (e.g., event listener lists, observers)
 * ✗ Bad for: Frequent writes or large lists (memory and performance overhead)
 *
 * Comparison:
 * - Collections.synchronizedList: Uses locks for both read and write (slower reads)
 * - CopyOnWriteArrayList: Lock-free reads (faster), but expensive writes
 */
public class CopyOnWriteListExample {

  public static void readAndWriteToList() throws InterruptedException {
    // Initialize with two elements
    CopyOnWriteArrayList<String> arrayList =
        new CopyOnWriteArrayList<>(Arrays.asList("111", "222"));

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    // Submit 50 read tasks and occasional write tasks
    for (int i = 0; i < 50; i++) {
      // Read operation: safe and lock-free, even during concurrent writes
      // Each read sees a consistent snapshot of the list
      executorService.submit(() -> System.out.println("second data: " + arrayList.get(1)));

      // Write operation every 10 iterations
      // Each add() creates a NEW copy of the internal array
      if (i % 10 == 0) {
        executorService.submit(() -> arrayList.add(1, "333"));
      }
    }

    // One more write operation
    executorService.submit(() -> arrayList.add(1, "444"));

    // Wait for tasks to complete (max 3 seconds)
    executorService.awaitTermination(3, TimeUnit.SECONDS);

    // Iterator is "snapshot" style - sees the list state at iterator creation time
    // Won't throw ConcurrentModificationException even if list is modified during iteration
    Iterator<String> iterator = arrayList.iterator();
    while (iterator.hasNext()) {
      System.out.println(iterator.next());
    }

    executorService.shutdown();
  }

  public static void main(String[] args) throws InterruptedException {
    readAndWriteToList();
  }
}
