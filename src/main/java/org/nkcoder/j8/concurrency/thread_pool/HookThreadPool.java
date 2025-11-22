package org.nkcoder.j8.concurrency.thread_pool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutor Hook Methods Example
 *
 * ThreadPoolExecutor provides hook methods that allow you to customize behavior
 * before/after task execution and when the pool terminates.
 *
 * Hook methods:
 * 1. beforeExecute(Thread t, Runnable r)
 *    - Called before task execution in the executing thread
 *    - Useful for: logging, initialization, thread-local setup
 *    - If it throws exception, task won't execute
 *
 * 2. afterExecute(Runnable r, Throwable t)
 *    - Called after task completes (success or exception)
 *    - Throwable t is non-null if task threw unchecked exception
 *    - Useful for: cleanup, logging, statistics collection
 *    - Runs in the executing thread
 *
 * 3. terminated()
 *    - Called when executor has fully terminated
 *    - All tasks completed and executor is shut down
 *    - Useful for: resource cleanup, final logging
 *    - Called only once in the lifetime of the executor
 *
 * Common use cases:
 * - Performance monitoring (task timing, throughput tracking)
 * - Logging and auditing (who ran what and when)
 * - Resource management (acquire before, release after)
 * - Error handling and reporting
 * - Thread-local setup/cleanup (database connections, security context)
 * - Statistics collection (success rate, failure rate, average time)
 *
 * Best practices:
 * ✓ Keep hook methods fast - they run in worker threads
 * ✓ Handle exceptions in hooks - don't let them propagate
 * ✓ Always call super.hookMethod() (though default implementations are empty)
 * ✗ Don't perform blocking operations in hooks
 * ✗ Don't modify the task or throw exceptions unnecessarily
 *
 * This example demonstrates:
 * - beforeExecute() logs which thread will run which task
 * - afterExecute() logs when task completes
 * - terminated() logs when entire pool shuts down
 * - Useful for debugging and monitoring thread pool behavior
 */
public class HookThreadPool {

  public static void main(String[] args) {

    // Create custom ThreadPoolExecutor with overridden hook methods
    ThreadPoolExecutor threadPoolExecutor =
        new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>()) {

          @Override
          protected void beforeExecute(Thread t, Runnable r) {
            // Called right before task starts executing
            // Runs in the worker thread that will execute the task
            System.out.println(t.getName() + " will run task: " + ((Task) r).getName());
            // Always call super (though default implementation is empty)
            super.beforeExecute(t, r);
          }

          @Override
          protected void afterExecute(Runnable r, Throwable t) {
            // Called after task completes (successfully or with exception)
            // Runs in the same worker thread that executed the task
            // Throwable t is non-null if task threw unchecked exception
            System.out.println("task: is done: " + ((Task) r).getName());
            // Always call super (though default implementation is empty)
            super.afterExecute(r, t);
          }

          @Override
          protected void terminated() {
            // Called once when executor has completely shut down
            // All tasks have completed and all worker threads have exited
            System.out.println("thread pool is terminated.");
            // Always call super (though default implementation is empty)
            super.terminated();
          }
        };

    // Submit 10 tasks - observe beforeExecute and afterExecute for each
    for (int i = 0; i < 10; i++) {
      threadPoolExecutor.execute(new Task("task-" + i));
    }

    // Initiate shutdown - no new tasks will be accepted
    // After all tasks complete, terminated() will be called
    threadPoolExecutor.shutdown();
  }

  /**
   * Simple task with a name for demonstration purposes.
   */
  private static class Task implements Runnable {
    private final String name;

    private Task(String name) {
      this.name = name;
    }

    @Override
    public void run() {
      try {
        // Simulate some work
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public String getName() {
      return name;
    }
  }
}
