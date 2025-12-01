package org.nkcoder.concurrency.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger: Lock-free thread-safe integer using CAS.
 *
 * <ul>
 *   <li>{@code incrementAndGet()}, {@code addAndGet()}, {@code compareAndSet()} are atomic</li>
 *   <li>Better than synchronized for simple counters</li>
 *   <li>Java 8+: Use LongAdder for high-contention counters</li>
 * </ul>
 */
public class AtomicIntegerExample {

  public static void main(String[] args) throws InterruptedException {
    AtomicInteger counter = new AtomicInteger(0);

    for (int i = 0; i < 100; i++) {
      new Thread(() -> counter.addAndGet(10)).start();
    }

    Thread.sleep(200);
    System.out.println("counter value: " + counter.get());
  }
}
