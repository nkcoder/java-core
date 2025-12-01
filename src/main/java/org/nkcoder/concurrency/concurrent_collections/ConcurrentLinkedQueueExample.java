package org.nkcoder.concurrency.concurrent_collections;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ConcurrentLinkedQueue: Thread-safe, lock-free queue using CAS.
 *
 * <ul>
 *   <li>{@code offer()} adds to tail, {@code poll()} removes from head</li>
 *   <li>Unbounded, non-blocking, no null elements</li>
 * </ul>
 */
public class ConcurrentLinkedQueueExample {

  public static void main(String[] args) {
    ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    // poll() may return null if it runs before offer()
    for (int i = 0; i < 30; i++) {
      final String id = "id-" + i;
      executorService.submit(() -> queue.offer(id));
      executorService.submit(() -> System.out.println("value: " + queue.poll()));
    }

    executorService.shutdown();
  }
}
