package org.nkcoder.concurrency.executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledExecutorService: Schedule tasks with delay or periodically.
 *
 * <ul>
 *   <li>{@code scheduleAtFixedRate}: period between START of executions</li>
 *   <li>{@code scheduleWithFixedDelay}: delay between END and next START</li>
 *   <li>Handle exceptions in tasks or they stop repeating</li>
 * </ul>
 */
public class ScheduledThreadPool {

  public static void main(String[] args) throws InterruptedException {
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

    // Task takes 5s but period is 3s - runs back-to-back (no gap)
    executorService.scheduleAtFixedRate(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("I'm running at fixed rate.");
    }, 0, 3, TimeUnit.SECONDS);

    // Task takes 5s + 3s delay = 8s between executions
    executorService.scheduleWithFixedDelay(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("I'm running with fixed delay");
    }, 0, 3, TimeUnit.SECONDS);

    TimeUnit.MINUTES.sleep(2);
    executorService.shutdown();
  }
}
