package org.nkcoder.j8.concurrency.thread_safe;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConcurrentLinkedQueueExample {

  public static void main(String[] args) {
    ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    for (int i = 0; i < 30; i++) {
      final String id = "id-" + i;
      executorService.submit(() -> queue.offer(id));
      executorService.submit(() -> System.out.println("value: " + queue.poll()));
    }

    executorService.shutdown();
  }
}
