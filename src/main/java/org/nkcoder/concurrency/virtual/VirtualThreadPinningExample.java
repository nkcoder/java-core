package org.nkcoder.concurrency.virtual;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates "Virtual Thread Pinning".
 *
 * <p>
 * Concept: Virtual threads are mounted on "carrier threads" (Platform threads). When a virtual
 * thread blocks (e.g., Thread.sleep, I/O), it should "unmount" so the carrier can run other virtual
 * threads.
 *
 * <p>
 * The Problem (Pinning): If a virtual thread blocks inside a `synchronized` block (or native
 * method), it cannot unmount. The carrier thread remains blocked ("pinned"). This limits throughput
 * to the number of carrier threads (OS threads), defeating the purpose of virtual threads.
 *
 * <p>
 * The Solution: Use {@link java.util.concurrent.locks.ReentrantLock} instead of `synchronized` for
 * guarding critical sections involving blocking operations.
 *
 * <p>
 * Note: JEP 425 and subsequent updates are working to reduce pinning scenarios (e.g., in
 * Object.wait), but `synchronized` usage is the classic offender.
 */
public class VirtualThreadPinningExample {

  public static void main(String[] args) throws InterruptedException {
    System.out.println("=== Virtual Thread Pinning Demo ===");
    // Reduce parallelism to make pinning obvious (e.g., 1 carrier thread)
    // In practice, this is controlled by -Djdk.virtualThreadScheduler.parallelism=1
    System.out.println(
        "NOTE: For clear demonstration, run with: -Djdk.virtualThreadScheduler.parallelism=1");
    System.out.println("Simulating limited resources...");

    System.out.println("\n--- Test 1: Synchronized (Blocking = PINNING) ---");
    testPinning(new SynchronizedService());

    System.out.println("\n--- Test 2: ReentrantLock (Blocking = UNMOUNTING) ---");
    testPinning(new LockService());
  }

  static void testPinning(BlockingService service) throws InterruptedException {
    long start = System.nanoTime();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      // Launch more tasks than typical carrier threads (usually = logical cores)
      for (int i = 0; i < 10; i++) {
        int id = i;
        executor.submit(() -> service.performBlockingTask(id));
      }
    } // wait for all to finish

    long duration = (System.nanoTime() - start) / 1_000_000;
    System.out.println("Total duration: " + duration + " ms");
  }

  interface BlockingService {
    void performBlockingTask(int id);
  }

  static class SynchronizedService implements BlockingService {
    @Override
    public synchronized void performBlockingTask(int id) {
      // synchronized + blocking = PINNING
      // With parallelism=1, these will run sequentially!
      sleep(100);
    }
  }

  static class LockService implements BlockingService {
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void performBlockingTask(int id) {
      lock.lock();
      try {
        // ReentrantLock + blocking = UNMOUNTING
        // Virtual thread unmounts, but since we hold a lock,
        // strictly speaking only one thread enters here at a time.
        // However, if we were waiting on I/O *outside* a lock but inside a synchronized block,
        // that's where the real pain is.
        // Let's simulate a logic where we sleep (I/O) inside the lock.
        sleep(100);
      } finally {
        lock.unlock();
      }
    }
  }

  static void sleep(int ms) {
    try {
      Thread.sleep(Duration.ofMillis(ms));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
