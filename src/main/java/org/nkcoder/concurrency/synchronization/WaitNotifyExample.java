package org.nkcoder.concurrency.synchronization;

import java.util.LinkedList;
import java.util.Queue;

/**
 * wait/notify: Inter-thread communication mechanism.
 *
 * <p><strong>Java 25 Note:</strong> {@code wait/notify} is foundational knowledge but verbose and
 * error-prone. Prefer {@code BlockingQueue} for producer-consumer, {@code Condition} for more
 * control, or higher-level utilities like {@code CountDownLatch}.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>wait() releases lock and waits for notification</li>
 *   <li>notify() wakes one waiting thread, notifyAll() wakes all</li>
 *   <li>Must be called within synchronized block on same object</li>
 *   <li>Always use while loop around wait() (spurious wakeups)</li>
 * </ul>
 *
 * <p>Interview tip: Classic producer-consumer problem. Know why you need
 * while (not if) around wait(), and when to use notify vs notifyAll.
 */
public class WaitNotifyExample {

  static void main(String[] args) throws Exception {
    basicWaitNotify();
    producerConsumer();
    notifyVsNotifyAll();
    spuriousWakeups();
    bestPractices();
  }

  static void basicWaitNotify() throws Exception {
    System.out.println("=== Basic wait/notify ===");

    final Object lock = new Object();

    Thread waiter = new Thread(() -> {
      synchronized (lock) {
        System.out.println("  Waiter: Acquired lock, now waiting...");
        try {
          lock.wait(); // Releases lock and waits
          System.out.println("  Waiter: Woke up!");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });

    Thread notifier = new Thread(() -> {
      try {
        Thread.sleep(100); // Let waiter start first
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      synchronized (lock) {
        System.out.println("  Notifier: Acquired lock, notifying...");
        lock.notify(); // Wakes up waiter, but doesn't release lock yet
        System.out.println("  Notifier: Called notify, still in synchronized block");
      }
      // Lock released here, waiter can now proceed
    });

    waiter.start();
    notifier.start();

    waiter.join();
    notifier.join();

    System.out.println("""

        How wait/notify works:
        1. Thread calls wait() inside synchronized block
        2. Lock is released, thread enters WAITING state
        3. Another thread calls notify() inside synchronized (same object)
        4. Waiting thread wakes up, but must re-acquire lock
        5. Once lock acquired, thread continues after wait()
        """);
  }

  static void producerConsumer() throws Exception {
    System.out.println("=== Producer-Consumer Pattern ===");

    class BoundedBuffer<T> {
      private final Queue<T> queue = new LinkedList<>();
      private final int capacity;

      BoundedBuffer(int capacity) {
        this.capacity = capacity;
      }

      synchronized void put(T item) throws InterruptedException {
        // Wait while buffer is full
        while (queue.size() == capacity) {
          System.out.println("    Buffer full, producer waiting...");
          wait();
        }
        queue.add(item);
        System.out.println("    Produced: " + item + ", size=" + queue.size());
        notifyAll(); // Wake up consumers
      }

      synchronized T take() throws InterruptedException {
        // Wait while buffer is empty
        while (queue.isEmpty()) {
          System.out.println("    Buffer empty, consumer waiting...");
          wait();
        }
        T item = queue.poll();
        System.out.println("    Consumed: " + item + ", size=" + queue.size());
        notifyAll(); // Wake up producers
        return item;
      }
    }

    BoundedBuffer<Integer> buffer = new BoundedBuffer<>(3);

    // Producer
    Thread producer = new Thread(() -> {
      try {
        for (int i = 1; i <= 5; i++) {
          buffer.put(i);
          Thread.sleep(50);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    // Consumer (slower)
    Thread consumer = new Thread(() -> {
      try {
        for (int i = 0; i < 5; i++) {
          buffer.take();
          Thread.sleep(150);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    producer.start();
    consumer.start();

    producer.join();
    consumer.join();

    System.out.println();
  }

  static void notifyVsNotifyAll() throws Exception {
    System.out.println("=== notify() vs notifyAll() ===");

    final Object lock = new Object();

    // Create multiple waiting threads
    Runnable waiterTask = () -> {
      synchronized (lock) {
        try {
          System.out.println("    " + Thread.currentThread().getName() + " waiting...");
          lock.wait();
          System.out.println("    " + Thread.currentThread().getName() + " woke up!");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    };

    Thread w1 = new Thread(waiterTask, "Waiter-1");
    Thread w2 = new Thread(waiterTask, "Waiter-2");
    Thread w3 = new Thread(waiterTask, "Waiter-3");

    w1.start();
    w2.start();
    w3.start();

    Thread.sleep(100); // Let all waiters start

    // notify() - wakes only ONE thread
    System.out.println("\n  Calling notify() (wakes one thread):");
    synchronized (lock) {
      lock.notify();
    }

    Thread.sleep(100);

    // notifyAll() - wakes ALL threads
    System.out.println("\n  Calling notifyAll() (wakes remaining threads):");
    synchronized (lock) {
      lock.notifyAll();
    }

    w1.join();
    w2.join();
    w3.join();

    System.out.println("""

        When to use which:
        +-------------+--------------------------------------------+
        | notify()    | When any ONE waiter can proceed            |
        |             | More efficient (one thread wakes)          |
        |             | Risk: may wake wrong thread (lost wakeup)  |
        +-------------+--------------------------------------------+
        | notifyAll() | When waiters have different conditions     |
        |             | Safer (no lost wakeups)                    |
        |             | Less efficient (all wake, most re-wait)    |
        +-------------+--------------------------------------------+

        Rule: When in doubt, use notifyAll()
        """);
  }

  static void spuriousWakeups() throws Exception {
    System.out.println("=== Spurious Wakeups ===");

    System.out.println("""
        A "spurious wakeup" is when wait() returns without notify/notifyAll.
        This can happen due to OS/JVM implementation details.

        // WRONG - using if
        synchronized (lock) {
            if (!condition) {
                lock.wait();
            }
            // BUG: condition might be false (spurious wakeup)
            doSomething();
        }

        // CORRECT - using while
        synchronized (lock) {
            while (!condition) {
                lock.wait();
            }
            // SAFE: condition is definitely true
            doSomething();
        }

        Even without spurious wakeups, while is needed because:
        - Multiple threads may be notified (notifyAll)
        - Only one can proceed
        - Others must re-check and wait again
        """);

    // Demonstration
    class ConditionalWait {
      private boolean ready = false;

      synchronized void waitUntilReady() throws InterruptedException {
        while (!ready) { // ALWAYS use while, not if
          System.out.println("    Waiting for ready...");
          wait();
          System.out.println("    Woke up, checking condition...");
        }
        System.out.println("    Ready! Proceeding...");
      }

      synchronized void setReady() {
        ready = true;
        notifyAll();
      }
    }

    ConditionalWait cw = new ConditionalWait();

    Thread waiter = new Thread(() -> {
      try {
        cw.waitUntilReady();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    waiter.start();
    Thread.sleep(100);
    cw.setReady();
    waiter.join();

    System.out.println();
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        DO:
        - Always call wait/notify inside synchronized block
        - Always use while loop around wait() (not if)
        - Prefer notifyAll() unless you're sure notify() is safe
        - Document the condition being waited for

        DON'T:
        - Call wait/notify without holding lock (IllegalMonitorStateException)
        - Use if instead of while around wait()
        - Hold other locks while waiting (can cause deadlock)
        - Assume order of thread wakeup

        Common pattern:
        ```
        synchronized (lock) {
            while (!conditionMet()) {
                lock.wait();
            }
            // proceed
        }
        ```

        Modern alternatives (preferred):
        - java.util.concurrent.locks.Condition
          - More flexible: multiple conditions per lock
          - Interruptible, timed waits

        - BlockingQueue
          - Built-in producer-consumer support
          - put() blocks when full, take() blocks when empty

        - CountDownLatch, CyclicBarrier, Semaphore
          - Higher-level synchronization utilities

        - CompletableFuture
          - Async programming without explicit waits
        """);
  }
}
