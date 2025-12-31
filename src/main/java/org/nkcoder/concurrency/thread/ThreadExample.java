package org.nkcoder.concurrency.thread;

/**
 * Thread Basics: Creating and managing threads in Java.
 *
 * <p><strong>Java 25 Note:</strong> Raw threads are foundational knowledge but rarely used directly in modern code.
 * Prefer {@code Executors.newVirtualThreadPerTaskExecutor()} for I/O-bound tasks or {@code ExecutorService} for thread
 * pool management.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Two ways to create threads: extend Thread or implement Runnable
 *   <li>Thread lifecycle: NEW -> RUNNABLE -> RUNNING -> BLOCKED/WAITING -> TERMINATED
 *   <li>Thread priority is a hint, not guaranteed
 *   <li>join() waits for thread completion
 * </ul>
 *
 * <p>Interview tip: Know the difference between Runnable and Thread, and understand thread states.
 */
public class ThreadExample {

    static void main(String[] args) throws Exception {
        creatingThreads();
        threadLifecycle();
        threadPriority();
        joiningThreads();
        interruptingThreads();
        threadInfo();
        bestPractices();
    }

    static void creatingThreads() throws Exception {
        System.out.println("=== Creating Threads ===");

        // Method 1: Extend Thread class (not recommended)
        class MyThread extends Thread {
            @Override
            public void run() {
                System.out.println(
                        "  [1] Extending Thread: " + Thread.currentThread().getName());
            }
        }

        Thread t1 = new MyThread();
        t1.start();
        t1.join();

        // Method 2: Implement Runnable (preferred)
        class MyRunnable implements Runnable {
            @Override
            public void run() {
                System.out.println(
                        "  [2] Implementing Runnable: " + Thread.currentThread().getName());
            }
        }

        Thread t2 = new Thread(new MyRunnable());
        t2.start();
        t2.join();

        // Method 3: Lambda (most concise)
        Thread t3 = new Thread(() -> {
            System.out.println(
                    "  [3] Lambda Runnable: " + Thread.currentThread().getName());
        });
        t3.start();
        t3.join();

        // Method 4: Thread.ofPlatform() builder (Java 21+)
        Thread t4 = Thread.ofPlatform().name("custom-thread").start(() -> {
            System.out.println(
                    "  [4] Platform thread builder: " + Thread.currentThread().getName());
        });
        t4.join();

        System.out.println("""

        Why prefer Runnable over extending Thread?
        - Java has single inheritance (can't extend another class)
        - Separates task (what to do) from mechanism (thread)
        - Runnable can be used with ExecutorService
        - Better design: composition over inheritance
        """);
    }

    static void threadLifecycle() throws Exception {
        System.out.println("=== Thread Lifecycle ===");

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("  After creation: " + thread.getState()); // NEW

        thread.start();
        System.out.println("  After start(): " + thread.getState()); // RUNNABLE

        Thread.sleep(50);
        System.out.println("  During sleep: " + thread.getState()); // TIMED_WAITING

        thread.join();
        System.out.println("  After completion: " + thread.getState()); // TERMINATED

        System.out.println("""

        Thread States:
        +-------------+----------------------------------------+
        | State       | Description                            |
        +-------------+----------------------------------------+
        | NEW         | Created but not started                |
        | RUNNABLE    | Executing or ready to execute          |
        | BLOCKED     | Waiting for monitor lock               |
        | WAITING     | Waiting indefinitely (wait, join)      |
        | TIMED_WAIT  | Waiting with timeout (sleep, wait(t))  |
        | TERMINATED  | Execution completed                    |
        +-------------+----------------------------------------+

        Note: A thread can only be started once!
        """);

        // Trying to start again throws IllegalThreadStateException
        try {
            thread.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("  Cannot restart thread: " + e.getClass().getSimpleName());
        }

        System.out.println();
    }

    static void threadPriority() throws Exception {
        System.out.println("=== Thread Priority ===");

        Thread lowPriority = new Thread(() -> {
            System.out.println("    Low priority running");
        });
        lowPriority.setPriority(Thread.MIN_PRIORITY); // 1

        Thread normalPriority = new Thread(() -> {
            System.out.println("    Normal priority running");
        });
        normalPriority.setPriority(Thread.NORM_PRIORITY); // 5 (default)

        Thread highPriority = new Thread(() -> {
            System.out.println("    High priority running");
        });
        highPriority.setPriority(Thread.MAX_PRIORITY); // 10

        System.out.println("  Priorities: MIN=" + Thread.MIN_PRIORITY + ", NORM="
                + Thread.NORM_PRIORITY + ", MAX="
                + Thread.MAX_PRIORITY);

        // Start all threads
        lowPriority.start();
        normalPriority.start();
        highPriority.start();

        lowPriority.join();
        normalPriority.join();
        highPriority.join();

        System.out.println("""

        Priority is just a HINT to the scheduler:
        - OS may ignore it completely
        - Behavior varies by platform
        - Don't rely on priority for correctness
        - Use synchronization for ordering guarantees
        """);
    }

    static void joiningThreads() throws Exception {
        System.out.println("=== Joining Threads ===");

        Thread worker = new Thread(() -> {
            try {
                System.out.println("  Worker starting...");
                Thread.sleep(200);
                System.out.println("  Worker finished!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        worker.start();

        // join() - wait indefinitely
        System.out.println("  Main waiting for worker...");
        worker.join();
        System.out.println("  Main continues after worker done");

        // join(timeout) - wait with timeout
        Thread slowWorker = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        slowWorker.start();
        System.out.println("\n  Waiting for slow worker (max 100ms)...");
        slowWorker.join(100);

        if (slowWorker.isAlive()) {
            System.out.println("  Timeout! Worker still running: " + slowWorker.isAlive());
            slowWorker.interrupt();
        }

        System.out.println();
    }

    static void interruptingThreads() throws Exception {
        System.out.println("=== Interrupting Threads ===");

        // Thread that checks interrupt flag
        Thread pollingThread = new Thread(() -> {
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                count++;
                if (count % 1000000 == 0) {
                    System.out.println("    Polling thread working... count=" + count);
                }
            }
            System.out.println("    Polling thread interrupted after count=" + count);
        });

        pollingThread.start();
        Thread.sleep(50);
        pollingThread.interrupt();
        pollingThread.join();

        // Thread that catches InterruptedException
        Thread sleepingThread = new Thread(() -> {
            try {
                System.out.println("    Sleeping thread going to sleep...");
                Thread.sleep(10000);
                System.out.println("    This won't print");
            } catch (InterruptedException e) {
                System.out.println("    Sleeping thread interrupted during sleep!");
                // Restore interrupt status (good practice)
                Thread.currentThread().interrupt();
            }
        });

        sleepingThread.start();
        Thread.sleep(50);
        sleepingThread.interrupt();
        sleepingThread.join();

        System.out.println("""

        Interrupt handling:
        1. Check Thread.currentThread().isInterrupted() in loops
        2. Catch InterruptedException for blocking operations
        3. Restore interrupt status: Thread.currentThread().interrupt()

        Methods that throw InterruptedException:
        - Thread.sleep()
        - Object.wait()
        - Thread.join()
        - BlockingQueue operations
        - Future.get()
        """);
    }

    static void threadInfo() {
        System.out.println("=== Thread Information ===");

        Thread current = Thread.currentThread();

        System.out.println("  Current thread:");
        System.out.println("    Name: " + current.getName());
        System.out.println("    ID: " + current.threadId());
        System.out.println("    Priority: " + current.getPriority());
        System.out.println("    State: " + current.getState());
        System.out.println("    isAlive: " + current.isAlive());
        System.out.println("    isDaemon: " + current.isDaemon());
        System.out.println("    isVirtual: " + current.isVirtual());

        System.out.println("\n  Active threads: " + Thread.activeCount());

        System.out.println();
    }

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        DO:
        - Use ExecutorService instead of raw threads
        - Implement Runnable rather than extend Thread
        - Handle InterruptedException properly
        - Use meaningful thread names for debugging
        - Keep thread tasks short and focused

        DON'T:
        - Use Thread.stop() (deprecated, unsafe)
        - Use Thread.suspend()/resume() (deprecated)
        - Rely on thread priority for correctness
        - Ignore InterruptedException (at minimum restore flag)
        - Share mutable state without synchronization

        Modern alternatives (Java 21+):
        - Virtual threads for I/O-bound tasks
        - Structured concurrency for task management
        - CompletableFuture for async workflows

        When to use raw threads:
        - Learning/understanding concurrency
        - Very specific thread control needs
        - Legacy code maintenance
        """);
    }
}
