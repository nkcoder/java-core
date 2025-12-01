package org.nkcoder.concurrency.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FixedThreadPool: Fixed number of threads, unbounded queue.
 *
 * <ul>
 *   <li>Threads reused for multiple tasks</li>
 *   <li>Caution: unbounded queue can cause OutOfMemoryError</li>
 *   <li>Java 21+: Consider virtual threads for simpler scaling</li>
 * </ul>
 */
public class FixThreadPool {
  public static void main(String[] args) {
    try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {
      for (int i = 0; i < 20; i++) {
        executorService.submit(
            () -> {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              System.out.println("I'm done.");
            });
      }
      executorService.shutdown();
    }
  }
}
