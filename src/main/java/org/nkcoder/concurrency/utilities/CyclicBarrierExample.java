package org.nkcoder.concurrency.utilities;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CyclicBarrier Example
 *
 * CyclicBarrier is a synchronization aid that allows a set of threads to wait for each other
 * to reach a common barrier point before proceeding.
 *
 * Key characteristics:
 * 1. All threads must call await() - they wait for each other (mutual wait)
 * 2. When all threads arrive, the barrier is tripped and all are released
 * 3. Reusable (cyclic) - can be reset and used again after barrier is tripped
 * 4. Optional barrier action - runs once when barrier is tripped (before threads are released)
 * 5. Useful for multi-phase parallel algorithms
 *
 * CyclicBarrier vs CountDownLatch:
 *
 * CyclicBarrier:
 * - All participating threads wait for EACH OTHER
 * - Reusable - automatically resets after all threads pass
 * - Used when threads perform work in synchronized phases
 * - Example: 10 threads all wait for each other, then proceed together
 *
 * CountDownLatch:
 * - One/more threads wait for OTHER threads to complete
 * - Single-use - cannot be reset
 * - Used when one thread waits for N operations to complete
 * - Example: Main thread waits for 10 worker threads to finish
 *
 * This example demonstrates multi-phase parallel execution:
 * - Phase 0: All threads wait to start together
 * - Phase 1: All threads complete task one, wait for others
 * - Phase 2: All threads complete task two, wait for others
 */
public class CyclicBarrierExample {

  void doTask(CyclicBarrier cyclicBarrier) {

    try {
      // Phase 0: Wait for all threads to be ready before starting
      cyclicBarrier.await();

      // Phase 1: All threads execute task one
      doTaskOne();
      // Wait for all threads to complete task one before proceeding
      cyclicBarrier.await();

      // Phase 2: All threads execute task two
      doTaskTwo();
      // Wait for all threads to complete task two
      cyclicBarrier.await();

    } catch (InterruptedException | BrokenBarrierException e) {
      e.printStackTrace();
    }
  }

  private void doTaskOne() {
    try {
      // Simulate work with random delay
      Thread.sleep(new Random().nextInt(500));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("task one, name: " + Thread.currentThread().getName());
  }

  private void doTaskTwo() {
    try {
      // Simulate work with random delay
      Thread.sleep(new Random().nextInt(1000));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("task two, name: " + Thread.currentThread().getName());
  }

  public static void main(String[] args) {
    CyclicBarrierExample cyclicBarrierExample = new CyclicBarrierExample();
    final AtomicInteger phase = new AtomicInteger(0);

    final int THREAD_NUMBER = 10;

    // Barrier action: executed once when all threads reach the barrier
    // Runs in the last thread to arrive before releasing all threads
    Runnable action = () -> {
      if (phase.get() == 0) {
        System.out.println("all threads are ready to do the task.");
        phase.addAndGet(1);
      } else if (phase.get() == 1) {
        System.out.println("all threads have finished task: " + phase.get());
        phase.addAndGet(1);
      } else {
        System.out.println("all threads have finished task: " + phase.get());
      }
    };

    // Create barrier for 10 threads with a barrier action
    // The action runs each time all threads reach the barrier
    CyclicBarrier cyclicBarrier = new CyclicBarrier(THREAD_NUMBER, action);

    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_NUMBER);

    // Submit 10 tasks - each will go through 3 synchronization points
    for (int i = 0; i < THREAD_NUMBER; i++) {
      executorService.submit(() -> cyclicBarrierExample.doTask(cyclicBarrier));
    }

    executorService.shutdown();
  }
}
