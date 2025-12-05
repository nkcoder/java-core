package org.nkcoder.concurrency.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Daemon Threads: Background service threads that don't prevent JVM shutdown.
 *
 * <p><strong>Java 25 Note:</strong> Understanding daemon threads is useful for interviews and
 * legacy code, but modern applications typically use ExecutorService with proper shutdown hooks.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>JVM exits when only daemon threads remain</li>
 *   <li>Daemon threads are abruptly terminated on JVM shutdown</li>
 *   <li>Must be set before thread starts</li>
 *   <li>Use for background services (GC, housekeeping)</li>
 * </ul>
 *
 * <p>Interview tip: Know when to use daemon threads and understand
 * the implications of abrupt termination.
 */
public class DaemonThreadExample {

  public static void main(String[] args) throws Exception {
    whatAreDaemonThreads();
    daemonVsUserThreads();
    settingDaemonStatus();
    daemonWithExecutors();
    useCases();
    bestPractices();
  }

  static void whatAreDaemonThreads() {
    System.out.println("=== What Are Daemon Threads? ===");

    System.out.println("""
        Two types of threads in Java:

        User Threads (default):
        - JVM waits for ALL user threads to complete before exiting
        - Main thread is a user thread
        - Created threads are user threads by default

        Daemon Threads:
        - JVM does NOT wait for daemon threads
        - Terminated abruptly when last user thread exits
        - Used for background/service tasks
        - Example: Garbage Collector is a daemon thread

        Key point: When all user threads finish, JVM exits immediately,
        killing any running daemon threads without cleanup!
        """);
  }

  static void daemonVsUserThreads() throws Exception {
    System.out.println("=== Daemon vs User Threads ===");

    // User thread - JVM would wait for it
    Thread userThread = new Thread(() -> {
      for (int i = 0; i < 3; i++) {
        System.out.println("    User thread: " + i);
        sleep(50);
      }
      System.out.println("    User thread completed");
    });
    userThread.setName("user-thread");

    // Daemon thread - JVM won't wait for it
    Thread daemonThread = new Thread(() -> {
      for (int i = 0; i < 100; i++) { // Would run 100 times if allowed
        System.out.println("    Daemon thread: " + i);
        sleep(50);
      }
      System.out.println("    Daemon thread completed (may never print!)");
    });
    daemonThread.setName("daemon-thread");
    daemonThread.setDaemon(true);

    System.out.println("  User thread isDaemon: " + userThread.isDaemon());
    System.out.println("  Daemon thread isDaemon: " + daemonThread.isDaemon());

    daemonThread.start();
    userThread.start();

    // Wait for user thread only
    userThread.join();
    System.out.println("  User thread done. Daemon may still be running...");

    // Give daemon a moment to show it's still running
    Thread.sleep(100);
    System.out.println("  Daemon alive: " + daemonThread.isAlive());

    // In a real scenario, if main() returned here and there were no other
    // user threads, the JVM would exit and kill the daemon thread

    System.out.println();
  }

  static void settingDaemonStatus() throws Exception {
    System.out.println("=== Setting Daemon Status ===");

    // Must set daemon BEFORE starting the thread
    Thread thread = new Thread(() -> {
      System.out.println("    Running as daemon: " + Thread.currentThread().isDaemon());
    });

    thread.setDaemon(true);
    thread.start();
    thread.join();

    // Trying to set daemon after start throws exception
    Thread runningThread = new Thread(() -> sleep(1000));
    runningThread.start();

    try {
      runningThread.setDaemon(true);
    } catch (IllegalThreadStateException e) {
      System.out.println("  Cannot setDaemon after start: " + e.getClass().getSimpleName());
    }

    runningThread.interrupt();

    // Child threads inherit daemon status from parent
    Thread parentDaemon = new Thread(() -> {
      Thread child = new Thread(() -> {});
      System.out.println("  Child of daemon inherits: isDaemon=" + child.isDaemon());
    });
    parentDaemon.setDaemon(true);
    parentDaemon.start();
    parentDaemon.join();

    // Using Thread builder (Java 21+)
    Thread builderDaemon = Thread.ofPlatform()
        .daemon(true)
        .name("builder-daemon")
        .unstarted(() -> {
          System.out.println("  Builder daemon running: " +
              Thread.currentThread().isDaemon());
        });
    builderDaemon.start();
    builderDaemon.join();

    System.out.println();
  }

  static void daemonWithExecutors() throws Exception {
    System.out.println("=== Daemon Threads with Executors ===");

    // Custom ThreadFactory for daemon threads
    ThreadFactory daemonFactory = runnable -> {
      Thread thread = new Thread(runnable);
      thread.setDaemon(true);
      thread.setName("daemon-worker");
      return thread;
    };

    ExecutorService daemonExecutor = Executors.newFixedThreadPool(2, daemonFactory);

    daemonExecutor.submit(() -> {
      System.out.println("    Task on daemon thread: " +
          Thread.currentThread().isDaemon());
    });

    Thread.sleep(100);
    daemonExecutor.shutdown();

    // Using Thread.ofPlatform().factory()
    ThreadFactory platformDaemonFactory = Thread.ofPlatform()
        .daemon(true)
        .name("platform-daemon-", 0)
        .factory();

    ExecutorService executor2 = Executors.newFixedThreadPool(2, platformDaemonFactory);
    executor2.submit(() -> {
      System.out.println("    Platform daemon factory: " +
          Thread.currentThread().getName() + ", daemon=" +
          Thread.currentThread().isDaemon());
    });

    Thread.sleep(100);
    executor2.shutdown();

    System.out.println();
  }

  static void useCases() {
    System.out.println("=== Use Cases for Daemon Threads ===");

    System.out.println("""
        Good use cases for daemon threads:

        1. Background Services:
           - Garbage collection
           - Cache cleanup
           - Log flushing
           - Monitoring/heartbeat

        2. Support Tasks:
           - Timer threads
           - Signal handlers
           - Reference processing

        3. Non-Critical Work:
           - Statistics collection
           - Lazy initialization
           - Prefetching

        Example - Background cache cleanup:
        """);

    // Example: Background cleanup daemon
    Thread cleanupDaemon = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        System.out.println("    [Cleanup] Running periodic cleanup...");
        sleep(100);
      }
    });
    cleanupDaemon.setDaemon(true);
    cleanupDaemon.setName("cache-cleanup");
    cleanupDaemon.start();

    // Main work continues...
    sleep(250);
    System.out.println("    [Main] Application work done");
    // If this were the last user thread, JVM would exit here

    cleanupDaemon.interrupt();

    System.out.println();
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        DO:
        - Use for truly background, non-critical tasks
        - Set daemon status before starting thread
        - Use ThreadFactory for consistent daemon creation
        - Design for abrupt termination (no cleanup guaranteed)

        DON'T:
        - Use for tasks that must complete
        - Use for tasks with resources that need closing
        - Use for tasks that write important data
        - Rely on finally blocks in daemon threads

        DANGER - These may not execute in daemon threads:
        - finally blocks
        - shutdown hooks (not for daemon cleanup)
        - try-with-resources cleanup

        Safe alternatives:
        - Use user threads with proper shutdown handling
        - Use ExecutorService.shutdown() for graceful termination
        - Use virtual threads for many concurrent tasks

        When NOT to use daemon threads:
        - Database transactions
        - File I/O that must complete
        - Network requests with responses
        - Any task that must finish
        """);
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
