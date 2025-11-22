package org.nkcoder.j8.concurrency.atomic;

import java.util.concurrent.atomic.AtomicInteger;

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
