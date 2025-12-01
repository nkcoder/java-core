package org.nkcoder.concurrency.executors;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutor: Fine-grained control over thread pool behavior.
 *
 * <ul>
 *   <li>Configure: corePoolSize, maximumPoolSize, workQueue, rejectionHandler</li>
 *   <li>Task flow: core threads &rarr; queue &rarr; max threads &rarr; reject</li>
 *   <li>Java 21+: Consider virtual threads for simpler scaling</li>
 * </ul>
 */
public class ThreadPoolExecutorExample {

  public static void main(String[] args) throws InterruptedException {
    // Capacity: 5 threads + 10 queued = 15 tasks; 30 submitted = 15 rejected
    ThreadPoolExecutor threadPoolExecutor =
        new ThreadPoolExecutor(
            5, 5, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),
            (r) -> {
              Thread thread = new Thread(r);
              System.out.println("created thread: " + thread);
              return thread;
            },
            ((r, executor) -> System.out.println("task is discarded: " + r.toString()))
        );

    for (int i = 0; i < 30; i++) {
      threadPoolExecutor.submit(() -> {
        System.out.println("I'm running in thread: " + Thread.currentThread().getName());
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    threadPoolExecutor.shutdown();
    if (!threadPoolExecutor.awaitTermination(20, TimeUnit.SECONDS)) {
      threadPoolExecutor.shutdownNow();
    }
    System.out.println("All tasks completed or timed out");
  }
}

/*
 * Java 21 Alternative (Virtual Threads):
 *
 * try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
 *     for (int i = 0; i < 30; i++) {
 *         executor.submit(() -> {
 *             System.out.println("Running in virtual thread: " + Thread.currentThread());
 *             Thread.sleep(500);
 *         });
 *     }
 * } // Auto shutdown
 *
 * Benefits: No queue sizing, no thread pool sizing, scales automatically
 */
