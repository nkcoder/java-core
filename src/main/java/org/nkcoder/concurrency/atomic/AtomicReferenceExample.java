package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicReference: Atomic operations on object references.
 *
 * <ul>
 *   <li>Only the REFERENCE is atomic, not the object's fields</li>
 *   <li>Best used with immutable objects</li>
 *   <li>ABA problem: use AtomicStampedReference if A-&gt;B-&gt;A changes matter</li>
 * </ul>
 */
public class AtomicReferenceExample {

  public static void main(String[] args) throws InterruptedException {
    AtomicReference<Integer> balance = new AtomicReference<>(15);

    try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {
      for (int i = 0; i < 3; i++) {
        executorService.submit(() -> {
          for (int j = 0; j < 1000; j++) {
            int money = balance.get();
            if (money > 20) {
              System.out.println("money > 20, no need to recharge");
            } else {
              if (balance.compareAndSet(money, money + 20)) {
                System.out.println("money < 20, recharge done, money: " + balance.get());
              }
            }
          }
        });
      }

      executorService.submit(() -> {
        for (int i = 0; i < 100; i++) {
          int money = balance.get();
          if (money < 10) {
            System.out.println("money < 10, cannot expend.");
          } else {
            if (balance.compareAndSet(money, money - 10)) {
              System.out.println("expend 10, remaining money: " + balance.get());
            }
          }
        }
      });

      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    }
    System.out.println("Final balance: " + balance.get());
  }
}
