package org.nkcoder.concurrency.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * execute() vs submit():
 *
 * <ul>
 *   <li>{@code execute()}: void return, exceptions printed immediately</li>
 *   <li>{@code submit()}: returns Future, exceptions captured (silent if ignored)</li>
 *   <li>Use submit() when you need result, cancellation, or programmatic exception handling</li>
 * </ul>
 */
public class ExecuteOrSubmit {

  private record Divide(int divider, int divisor) implements Runnable {

    @Override
    public void run() {
      int result = divider / divisor;
      System.out.println("result is: " + result);
    }
  }

  public static void main(String[] args) {
    try (ExecutorService executorService = Executors.newFixedThreadPool(3)) {
      for (int i = 0; i < 10; i++) {
        executorService.execute(new Divide(100, i));
        //        executorService.submit(new Divide(100, i));
      }
      executorService.shutdown();
    }
  }
}
