package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * AtomicIntegerArray: Atomic operations on array elements.
 *
 * <ul>
 *   <li>Each element updated independently via CAS</li>
 *   <li>No lock contention between different indices</li>
 * </ul>
 */
public class AtomicIntegerArrayExample {

  public static void main(String[] args) throws InterruptedException {
    AtomicIntegerArray atomicIntegerArray = new AtomicIntegerArray(10);
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 10; i++) {
      executorService.submit(() -> {
        for (int j = 0; j < 100; j++) {
          atomicIntegerArray.incrementAndGet(j % 10);
        }
      });
    }

    executorService.shutdown();
    executorService.awaitTermination(5, TimeUnit.SECONDS);
    System.out.println("array: " + atomicIntegerArray);
  }
}
