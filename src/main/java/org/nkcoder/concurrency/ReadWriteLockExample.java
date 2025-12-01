package org.nkcoder.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLock Example
 *
 * ReadWriteLock is a lock that allows multiple readers or one writer at a time.
 * It's designed to improve performance for read-heavy scenarios.
 *
 * Key characteristics:
 * 1. Read lock - Shared lock: multiple threads can hold it simultaneously
 * 2. Write lock - Exclusive lock: only one thread can hold it, no readers allowed
 * 3. When write lock is held, no other thread can acquire read or write lock
 * 4. Allows more concurrency than a simple lock when reads >> writes
 *
 * Lock rules:
 * - Multiple readers can read simultaneously (if no writer)
 * - Writers must wait for all readers to finish
 * - When a writer holds the lock, all readers and writers must wait
 * - ReentrantReadWriteLock supports lock downgrading (write → read, not read → write)
 *
 * Fair vs Non-fair mode:
 * - Non-fair (default): Higher throughput, but possible writer starvation
 * - Fair: No starvation, but lower throughput
 *
 * Use cases:
 * ✓ Read-heavy data structures (caches, registries, configuration)
 * ✓ When reads are much more frequent than writes
 * ✗ Not beneficial if writes are frequent (overhead of maintaining two locks)
 *
 * This example demonstrates:
 * - 18 concurrent readers can execute simultaneously
 * - 2 writers must execute exclusively (one at a time)
 * - Total execution time should be ~2 seconds (not 20 seconds) if readers run in parallel
 */
public class ReadWriteLockExample {

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();
  private int value = 0;

  /**
   * Read operation - can be performed by multiple threads simultaneously.
   *
   * @return current value
   */
  public int readValue() {
    try {
      // Acquire read lock - multiple threads can hold this simultaneously
      readLock.lock();
      try {
        // Simulate read operation taking 1 second
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
      System.out.println("read value: " + value);
      return value;
    } finally {
      // Always unlock in finally block to ensure lock is released
      readLock.unlock();
    }
  }

  /**
   * Write operation - must be performed exclusively (no other readers or writers).
   *
   * @param newValue the new value to set
   */
  public void setValue(int newValue) {
    try {
      // Acquire write lock - exclusive access, no other threads can hold read/write lock
      writeLock.lock();
      try {
        // Simulate write operation taking 1 second
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
      System.out.println("set value to: " + newValue);
      this.value = newValue;
    } finally {
      // Always unlock in finally block to ensure lock is released
      writeLock.unlock();
    }
  }

  /**
   * Demonstrates ReadWriteLock behavior with 18 readers and 2 writers.
   *
   * @param args command line arguments
   * @throws InterruptedException if interrupted while waiting
   */
  public static void main(String[] args) throws InterruptedException {
    ReadWriteLockExample readWriteLockExample = new ReadWriteLockExample();
    Runnable readRunnable = readWriteLockExample::readValue;
    Runnable writeRunnable = () -> readWriteLockExample.setValue(10);

    ExecutorService executorService = Executors.newFixedThreadPool(20);

    // Submit 18 read tasks - these can execute concurrently
    for (int i = 0; i < 18; i++) {
      executorService.submit(readRunnable);
    }

    // Submit 2 write tasks - these must execute exclusively (one at a time)
    for (int i = 0; i < 2; i++) {
      executorService.submit(writeRunnable);
    }

    executorService.shutdown();

    // Wait for all tasks to complete (max 10 seconds)
    // If not done in 10 seconds, force shutdown
    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
      executorService.shutdownNow();
    }
  }
}
