package org.nkcoder.concurrency.synchronization;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * volatile keyword: Visibility guarantee for shared variables.
 *
 * <p><strong>Java 25 Note:</strong> {@code volatile} remains relevant for visibility guarantees (flags, status
 * variables, DCL pattern). For atomic operations, prefer {@code Atomic*} classes.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Guarantees visibility: all threads see latest value
 *   <li>Prevents instruction reordering around volatile access
 *   <li>Does NOT provide atomicity for compound operations
 *   <li>Lighter weight than synchronized
 * </ul>
 *
 * <p>Interview tip: Know what volatile guarantees (visibility) and what it doesn't (atomicity). Classic question: is
 * volatile enough for a counter?
 */
public class VolatileExample {

    static void main(String[] args) throws Exception {
        visibilityProblem();
        volatileSolution();
        volatileNotAtomic();
        volatileVsSynchronized();
        validUseCases();
        bestPractices();
    }

    static void visibilityProblem() throws Exception {
        System.out.println("=== Visibility Problem ===");

        class NoVisibility {
            private boolean running = true; // Not volatile!

            void run() {
                int count = 0;
                while (running) { // May never see update from other thread!
                    count++;
                }
                System.out.println("    Stopped after: " + count);
            }

            void stop() {
                running = false;
            }
        }

        System.out.println("""
        Problem without volatile:
        - Each thread may cache variables locally (CPU cache, registers)
        - Changes made by one thread may not be visible to others
        - Thread may NEVER see the updated value (infinite loop)

        // Thread 1                    // Thread 2
        while (running) {              running = false;
            // may run forever!
        }

        Why this happens:
        1. JIT compiler may hoist the check: if (running) while(true)
        2. CPU cache may hold stale value
        3. No happens-before relationship established
        """);

        // Note: Actually running this might or might not demonstrate the issue
        // depending on JIT compilation and CPU. The issue is real but not always
        // reproducible in simple tests.
    }

    private static volatile boolean volatileRunning = true;

    static void volatileSolution() throws Exception {
        System.out.println("=== Volatile Solution ===");

        volatileRunning = true;

        Thread worker = new Thread(() -> {
            int count = 0;
            while (volatileRunning) { // Always reads from main memory
                count++;
                if (count % 10_000_000 == 0) {
                    // Just to show progress
                }
            }
            System.out.println("    Worker stopped after iterations");
        });

        worker.start();
        Thread.sleep(100);

        volatileRunning = false; // Immediately visible to worker thread
        System.out.println("    Stop signal sent");

        worker.join(1000);
        System.out.println("    Worker terminated: " + !worker.isAlive());

        System.out.println("""

        volatile guarantees:
        1. Visibility: Writes are immediately visible to all threads
        2. Ordering: Prevents reordering of instructions around volatile

        Memory barrier effects:
        - volatile write: All previous writes become visible
        - volatile read: Invalidates cached copies, reads from memory
        """);
    }

    static void volatileNotAtomic() throws Exception {
        System.out.println("=== Volatile is NOT Atomic ===");

        class VolatileCounter {
            private volatile int count = 0;

            void increment() {
                count++; // NOT ATOMIC even with volatile!
                // This is: read count, add 1, write count
            }

            int getCount() {
                return count;
            }
        }

        VolatileCounter counter = new VolatileCounter();

        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(counter::increment);
            }
        }

        System.out.println("  Volatile counter: " + counter.getCount() + " (expected 10000)");
        System.out.println("  Lost updates due to race condition!");

        System.out.println("""

        Why volatile++ is broken:

        Thread 1:                Thread 2:
        1. Read count (0)        1. Read count (0)
        2. Add 1 (1)             2. Add 1 (1)
        3. Write count (1)       3. Write count (1)
                                 // Expected 2, got 1!

        volatile ensures each step sees latest value,
        but doesn't make the entire read-modify-write atomic.

        Solutions:
        - synchronized
        - AtomicInteger
        - LongAdder (for high contention)
        """);
    }

    static void volatileVsSynchronized() {
        System.out.println("=== Volatile vs Synchronized ===");

        System.out.println("""
        +-------------------+------------------------+------------------------+
        | Feature           | volatile               | synchronized           |
        +-------------------+------------------------+------------------------+
        | Visibility        | Yes                    | Yes                    |
        | Atomicity         | No (except 64-bit r/w) | Yes (entire block)     |
        | Blocking          | No                     | Yes (waits for lock)   |
        | Scope             | Single variable        | Code block/method      |
        | Performance       | Faster (no lock)       | Slower (lock overhead) |
        | Use case          | Flags, status          | Compound operations    |
        +-------------------+------------------------+------------------------+

        volatile is enough for:
        - Simple flags (stop, shutdown)
        - Publishing immutable objects
        - Status that's read often, written rarely
        - When only one thread writes

        Need synchronized (or Atomic) for:
        - Increment/decrement (count++)
        - Check-then-act (if not exists, create)
        - Read-modify-write operations
        - Multiple variables that must be consistent
        """);
    }

    static void validUseCases() throws Exception {
        System.out.println("=== Valid Use Cases for Volatile ===");

        // 1. Shutdown flag
        System.out.println("  1. Shutdown flag:");

        class Service {
            private volatile boolean shutdown = false;

            void run() {
                while (!shutdown) {
                    // do work
                }
                System.out.println("    Service stopped gracefully");
            }

            void shutdown() {
                shutdown = true;
            }
        }

        Service service = new Service();
        Thread serviceThread = new Thread(service::run);
        serviceThread.start();
        Thread.sleep(50);
        service.shutdown();
        serviceThread.join();

        // 2. Double-checked locking (with volatile!)
        System.out.println("\n  2. Double-checked locking pattern:");
        System.out.println("""
        // MUST be volatile for DCL to work correctly!
        private static volatile Singleton instance;

        static Singleton getInstance() {
            if (instance == null) {                  // First check (no lock)
                synchronized (Singleton.class) {
                    if (instance == null) {          // Second check (with lock)
                        instance = new Singleton();  // Safe with volatile
                    }
                }
            }
            return instance;
        }
        """);

        System.out.println("""

        Why volatile is needed for DCL:
        Without volatile, instance = new Singleton() can be reordered:
        1. Allocate memory
        2. Assign reference to instance (not null now!)
        3. Run constructor (object not fully initialized!)

        Another thread might see non-null but uninitialized object!
        volatile prevents this reordering.
        """);

        // 3. Publishing immutable objects
        System.out.println("  3. Publishing immutable objects:");

        record Config(String host, int port) {}

        class ConfigHolder {
            private volatile Config config = new Config("localhost", 8080);

            void updateConfig(Config newConfig) {
                config = newConfig; // Atomic reference assignment
            }

            Config getConfig() {
                return config; // Safe - Config is immutable
            }
        }

        ConfigHolder holder = new ConfigHolder();
        System.out.println("    Config: " + holder.getConfig());

        System.out.println();
    }

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        When to use volatile:
        - Single writer, multiple readers
        - Simple flags (boolean)
        - Reference to immutable object
        - Status/state variables

        When NOT to use volatile:
        - Counter (use AtomicInteger)
        - Check-then-act (use synchronized)
        - Multiple dependent variables (use synchronized)
        - When you need to block (use locks)

        Common mistakes:
        - Thinking volatile makes operations atomic
        - Using volatile for counters
        - Forgetting volatile in double-checked locking
        - Using volatile when synchronized is needed

        Rule of thumb:
        "If you need to read-modify-write, volatile is NOT enough"

        Modern alternatives:
        - AtomicReference for reference updates with CAS
        - VarHandle for advanced atomic operations (Java 9+)
        - Atomic* classes for numeric operations
        """);
    }
}
