package org.nkcoder.concurrency;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ConcurrentLinkedQueue Example
 *
 * ConcurrentLinkedQueue is a thread-safe, lock-free queue based on linked nodes.
 * It implements a non-blocking algorithm using CAS (Compare-And-Swap) operations.
 *
 * Key characteristics:
 * 1. Thread-safe without using locks (lock-free)
 * 2. Non-blocking operations - threads never wait for locks
 * 3. Unbounded - can grow dynamically
 * 4. Does NOT allow null elements
 * 5. Weakly consistent iterator (may not reflect recent modifications)
 *
 * Common operations:
 * - offer(e): Adds element to tail, always returns true (never blocks)
 * - poll(): Removes and returns head element, returns null if empty
 * - peek(): Returns head element without removing, returns null if empty
 *
 * Use cases:
 * - Producer-consumer patterns with multiple threads
 * - When you need thread-safety without blocking
 * - High-concurrency scenarios where lock contention would be an issue
 */
public class ConcurrentLinkedQueueExample {

  public static void main(String[] args) {
    // Create a thread-safe queue
    ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    // Create a thread pool with 5 threads
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    // Submit 30 producer tasks and 30 consumer tasks
    // Note: Output may show null values because poll() might execute before offer()
    // This demonstrates concurrent access - producers and consumers run simultaneously
    for (int i = 0; i < 30; i++) {
      final String id = "id-" + i;
      // Producer: add elements to the queue
      executorService.submit(() -> queue.offer(id));
      // Consumer: remove elements from the queue (may return null if queue is empty)
      executorService.submit(() -> System.out.println("value: " + queue.poll()));
    }

    // Shutdown the executor (no new tasks will be accepted)
    executorService.shutdown();
  }
}
