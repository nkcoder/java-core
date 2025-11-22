package org.nkcoder.j8.concurrency.thread_pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
