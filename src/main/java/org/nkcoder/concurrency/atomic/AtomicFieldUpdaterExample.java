package org.nkcoder.concurrency.atomic;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * AtomicFieldUpdater: Atomic operations on volatile fields via reflection.
 *
 * <ul>
 *   <li>Zero memory overhead per instance (shared updater)</li>
 *   <li>Field must be volatile, non-static, non-final</li>
 *   <li>Java 9+: Consider VarHandle for better performance</li>
 * </ul>
 */
public class AtomicFieldUpdaterExample {

  public static class Student {
    int id;
    volatile int score;  // Must be volatile for AtomicFieldUpdater
  }

  public static void main(String[] args) throws InterruptedException {
    ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    AtomicInteger count = new AtomicInteger(0);
    AtomicIntegerFieldUpdater<Student> updaterCount =
        AtomicIntegerFieldUpdater.newUpdater(Student.class, "score");

    Random random = new Random();
    Student student = new Student();

    for (int i = 0; i < 1000; i++) {
      executorService.submit(() -> {
        if (random.nextDouble() > 0.5) {
          count.incrementAndGet();
          updaterCount.incrementAndGet(student);
        }
      });
    }

    executorService.shutdown();
    boolean terminated = executorService.awaitTermination(3, TimeUnit.SECONDS);

    System.out.println("count: " + count.get()
        + ", updateCount: " + updaterCount.get(student)
        + ", terminated: " + terminated);
  }
}
