package org.nkcoder.concurrency.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore: Limit concurrent access with N permits.
 *
 * <ul>
 *   <li>{@code acquire()} blocks if no permits; {@code release()} returns permit</li>
 *   <li>Not owner-based: any thread can release</li>
 *   <li>Use for resource pools, rate limiting</li>
 * </ul>
 */
public class SemaphoreExample implements Runnable {
  private static final Semaphore SEMAPHORE = new Semaphore(5);

  @Override
  public void run() {
    try {
      SEMAPHORE.acquire();
      TimeUnit.SECONDS.sleep(3);
      System.out.println(Thread.currentThread().getId() + ": I'm done.");
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    } finally {
      SEMAPHORE.release();  // Always release in finally
    }
  }

  public static void main(String[] args) {
    SemaphoreExample semaphoreExample = new SemaphoreExample();
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);

    // 20 tasks but only 5 run concurrently due to semaphore
    for (int i = 0; i < 20; i++) {
      newFixedThreadPool.submit(semaphoreExample);
    }
  }
}
