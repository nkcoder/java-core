package org.nkcoder.j8.concurrency.thread_pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

/*
 * Difference between `execute()` and `submit` on ExecutorService:
 *
 * Return Type
 * - execute(): Returns void - fire and forget
 * - submit(): Returns Future<T> - allows you to track task status and get results
 *
 * Exception Handling
 * - execute():
 *   Unchecked exceptions are printed to System.err by the default uncaught exception handler
 *   You see the stack trace immediately
 * - submit():
 *   Exceptions are captured and stored in the Future object
 *   No output until you call future.get()
 *   Silent failure if you ignore the returned Future
 *
 * When to use each:
 * - execute(): When you don't need a result and want immediate exception feedback
 * - submit(): When you need to:
 *   Get a return value (with Callable<T>)
 *   Check if task completed
 *   Cancel the task
 *   Handle exceptions programmatically
 */
