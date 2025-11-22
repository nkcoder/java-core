package org.nkcoder.j8.concurrency.thread_pool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledThreadPool Example
 *
 * ScheduledExecutorService is a thread pool that can schedule tasks to run
 * after a delay or periodically at fixed intervals.
 *
 * Key scheduling methods:
 * 1. schedule(task, delay, unit) - Execute once after delay
 * 2. scheduleAtFixedRate(task, initialDelay, period, unit)
 *    - Period starts from beginning of previous execution
 *    - If task takes longer than period, next execution starts immediately
 *    - Fixed rate regardless of execution time
 * 3. scheduleWithFixedDelay(task, initialDelay, delay, unit)
 *    - Delay starts after previous execution completes
 *    - Guarantees fixed delay between executions
 *    - Execution time does not affect delay
 *
 * Critical difference: Fixed Rate vs Fixed Delay
 *
 * Fixed Rate (scheduleAtFixedRate):
 * - Time between START of executions is constant (period)
 * - If execution takes 5s and period is 3s, next starts immediately
 * - Pattern: [exec-5s][exec-5s][exec-5s]...
 * - Use for: Regular tasks regardless of duration (monitoring, polling)
 *
 * Fixed Delay (scheduleWithFixedDelay):
 * - Time between END and next START is constant (delay)
 * - If execution takes 5s and delay is 3s, gap is always 3s
 * - Pattern: [exec-5s][wait-3s][exec-5s][wait-3s]...
 * - Use for: Tasks that should not overlap or need recovery time
 *
 * Example from this code (task takes 5s, interval/delay is 3s):
 * scheduleAtFixedRate:
 * - T=0s: Start exec1 (takes 5s)
 * - T=3s: Should start exec2, but exec1 still running (skipped/delayed)
 * - T=5s: exec1 ends, exec2 starts immediately (no 3s wait!)
 * - T=10s: exec2 ends, exec3 starts immediately
 * - Pattern: executions run back-to-back when task > period
 *
 * scheduleWithFixedDelay:
 * - T=0s: Start exec1 (takes 5s)
 * - T=5s: exec1 ends, wait 3s
 * - T=8s: Start exec2 (takes 5s)
 * - T=13s: exec2 ends, wait 3s
 * - T=16s: Start exec3 (takes 5s)
 * - Pattern: always 3s gap between executions
 *
 * Use cases:
 * - Periodic maintenance tasks
 * - Scheduled reports
 * - Cache refresh
 * - Health checks and monitoring
 * - Delayed execution
 *
 * Best practices:
 * ✓ Use scheduleWithFixedDelay when tasks can vary in duration
 * ✓ Use scheduleAtFixedRate for precise timing (if task is fast enough)
 * ✓ Handle exceptions in tasks (or they'll stop repeating)
 * ✗ Don't use for real-time requirements (Java is not real-time)
 *
 * This example demonstrates:
 * - Task takes 5 seconds to execute
 * - scheduleAtFixedRate with 3s period: executions overlap (run back-to-back)
 * - scheduleWithFixedDelay with 3s delay: 3s gap between executions
 */
public class ScheduledThreadPool {

  public static void main(String[] args) throws InterruptedException {
    // Create scheduled pool with 3 threads
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

    // Fixed Rate: attempts to maintain constant rate between execution STARTS
    // initialDelay=0 (start immediately), period=3 seconds
    // But task takes 5 seconds! So executions will run back-to-back
    executorService.scheduleAtFixedRate(() -> {
      try {
        // Task takes 5 seconds - longer than the 3-second period!
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("I'm running at fixed rate.");
      // Next execution should start at T+3s, but this task isn't done until T+5s
      // Result: next execution starts immediately after this one finishes (no gap!)
    }, 0, 3, TimeUnit.SECONDS);

    // Fixed Delay: guarantees constant delay between execution END and next START
    // initialDelay=0 (start immediately), delay=3 seconds after completion
    // Task takes 5 seconds, then waits 3 seconds, then starts again
    executorService.scheduleWithFixedDelay(() -> {
      try {
        // Task takes 5 seconds
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("I'm running with fixed delay");
      // After this completes, scheduler waits 3 seconds before next execution
      // Result: executions are spaced 8 seconds apart (5s task + 3s delay)
    }, 0, 3, TimeUnit.SECONDS);

    // Let it run for 2 minutes to observe the patterns
    TimeUnit.MINUTES.sleep(2);
    executorService.shutdown();
  }
}
