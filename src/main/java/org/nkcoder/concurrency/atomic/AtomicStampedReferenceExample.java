package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicStampedReference: Solves ABA problem with version stamp.
 *
 * <ul>
 *   <li>Pairs reference with integer stamp (version number)</li>
 *   <li>CAS checks BOTH reference AND stamp</li>
 *   <li>Detects A-&gt;B-&gt;A changes that AtomicReference misses</li>
 * </ul>
 */
public class AtomicStampedReferenceExample {

  public static void main(String[] args) throws InterruptedException {
    AtomicStampedReference<Integer> balance = new AtomicStampedReference<>(15, 0);

    try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {
      for (int i = 0; i < 3; i++) {
        executorService.submit(() -> {
          for (int j = 0; j < 1000; j++) {
            int currentStamp = balance.getStamp();
            int money = balance.getReference();
            if (money < 20) {
              if (balance.compareAndSet(money, money + 20, currentStamp, currentStamp + 1)) {
                System.out.println("Recharged: balance=" + balance.getReference()
                    + ", stamp=" + balance.getStamp());
              }
            }
          }
        });
      }

      executorService.submit(() -> {
        for (int j = 0; j < 100; j++) {
          int stamp2 = balance.getStamp();
          int money = balance.getReference();
          if (money > 10) {
            if (balance.compareAndSet(money, money - 10, stamp2, stamp2 + 1)) {
              System.out.println("Spent 10: balance=" + balance.getReference()
                  + ", stamp=" + balance.getStamp());
            }
          } else {
            System.out.println("Insufficient balance: " + money);
          }
        }
      });

      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    }
    System.out.println("Final balance: " + balance.getReference()
        + ", final stamp: " + balance.getStamp());
  }
}
