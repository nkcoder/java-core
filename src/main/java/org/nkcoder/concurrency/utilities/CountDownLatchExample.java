package org.nkcoder.concurrency.utilities;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CountDownLatch: Wait for N events to complete.
 *
 * <ul>
 *   <li>{@code countDown()} decrements; {@code await()} blocks until count reaches 0</li>
 *   <li>Single-use (unlike CyclicBarrier which is reusable)</li>
 * </ul>
 */
public class CountDownLatchExample {

  private void doSomeTask(CountDownLatch countDownLatch) {
    try {
      System.out.println("I'm trying to do some task: " + Thread.currentThread().getName());
      Thread.sleep(new Random().nextInt(1000));
      countDownLatch.countDown();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("task done by thread: " + Thread.currentThread().getName());
  }

  public static void main(String[] args) {
    CountDownLatchExample countDownLatchExample = new CountDownLatchExample();
    CountDownLatch countDownLatch = new CountDownLatch(10);
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 10; i++) {
      executorService.submit(() -> countDownLatchExample.doSomeTask(countDownLatch));
    }

    try {
      countDownLatch.await();  // Blocks until count reaches 0
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("main task continue.");
    executorService.shutdown();
  }
}
