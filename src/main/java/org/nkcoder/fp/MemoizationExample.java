package org.nkcoder.fp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Memoization: Caching function results for performance.
 *
 * <ul>
 *   <li>Cache expensive computations based on input
 *   <li>Only works for pure functions (same input = same output)
 *   <li>Trade memory for speed
 *   <li>Thread-safe options with ConcurrentHashMap
 * </ul>
 */
public class MemoizationExample {

    static void main(String[] args) {
        basicMemoization();
        memoizeWithSupplier();
        recursiveMemoization();
        threadSafeMemoization();
        caveats();
    }

    // ============ Basic Memoization ============

    static void basicMemoization() {
        System.out.println("=== Basic Memoization ===");

        // Expensive function (simulated)
        Function<Integer, Long> expensiveSquare = n -> {
            System.out.println("  Computing square of " + n + "...");
            sleep(100); // Simulate expensive computation
            return (long) n * n;
        };

        // Without memoization - computes every time
        System.out.println("Without memoization:");
        System.out.println("square(5) = " + expensiveSquare.apply(5));
        System.out.println("square(5) = " + expensiveSquare.apply(5));
        System.out.println("square(5) = " + expensiveSquare.apply(5));

        // With memoization - computes once, caches result
        System.out.println("\nWith memoization:");
        var memoizedSquare = memoize(expensiveSquare);
        System.out.println("square(5) = " + memoizedSquare.apply(5));
        System.out.println("square(5) = " + memoizedSquare.apply(5)); // From cache
        System.out.println("square(5) = " + memoizedSquare.apply(5)); // From cache
        System.out.println("square(3) = " + memoizedSquare.apply(3)); // New computation

        System.out.println("""

        Memoization caches f(x) results:
        - First call: compute and store
        - Subsequent calls: return cached value
        - Key: function input, Value: function output
        """);
    }

    // Generic memoization wrapper
    static <T, R> Function<T, R> memoize(Function<T, R> function) {
        Map<T, R> cache = new HashMap<>();
        return input -> cache.computeIfAbsent(input, function);
    }

    // ============ Memoize with Supplier (Single Value) ============

    static void memoizeWithSupplier() {
        System.out.println("=== Memoize with Supplier (Lazy Singleton) ===");

        // Expensive initialization (runs once)
        Supplier<String> expensiveInit = () -> {
            System.out.println("  Initializing expensive resource...");
            sleep(100);
            return "Expensive Resource";
        };

        // Lazy memoized supplier
        Supplier<String> lazyResource = memoizeSupplier(expensiveInit);

        System.out.println("Supplier created, not yet called");
        System.out.println("First call: " + lazyResource.get());
        System.out.println("Second call: " + lazyResource.get()); // From cache
        System.out.println("Third call: " + lazyResource.get()); // From cache

        System.out.println("""

        Lazy initialization pattern:
        - Defer expensive initialization until needed
        - Compute exactly once
        - Thread-safe with proper implementation
        """);
    }

    // Memoized Supplier (single value, computed once)
    static <T> Supplier<T> memoizeSupplier(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;
            private boolean computed = false;

            @Override
            public synchronized T get() {
                if (!computed) {
                    value = supplier.get();
                    computed = true;
                }
                return value;
            }
        };
    }

    // ============ Recursive Memoization ============

    static void recursiveMemoization() {
        System.out.println("=== Recursive Memoization (Fibonacci) ===");

        // Naive recursive Fibonacci - exponential time O(2^n)
        System.out.println("Naive Fibonacci (slow for large n):");
        long start = System.currentTimeMillis();
        System.out.println("fib(30) = " + naiveFib(30));
        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");

        // Memoized Fibonacci - linear time O(n)
        System.out.println("\nMemoized Fibonacci (fast):");
        start = System.currentTimeMillis();
        System.out.println("fib(30) = " + memoizedFib(30));
        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");

        // Even larger values
        System.out.println("\nMemoized fib(50) = " + memoizedFib(50));

        System.out.println("""

        Memoization transforms recursive algorithms:
        - Avoids redundant computations
        - Fibonacci: O(2^n) -> O(n)
        - Classic dynamic programming technique
        """);
    }

    // Naive Fibonacci - O(2^n)
    static long naiveFib(int n) {
        if (n <= 1) return n;
        return naiveFib(n - 1) + naiveFib(n - 2);
    }

    // Memoized Fibonacci using instance cache
    private static final Map<Integer, Long> fibCache = new HashMap<>();

    static long memoizedFib(int n) {
        if (n <= 1) return n;
        return fibCache.computeIfAbsent(n, k -> memoizedFib(k - 1) + memoizedFib(k - 2));
    }

    // ============ Thread-Safe Memoization ============

    static void threadSafeMemoization() {
        System.out.println("=== Thread-Safe Memoization ===");

        // Thread-safe memoization with ConcurrentHashMap
        Function<String, String> expensiveLookup = key -> {
            System.out.println("  Looking up: " + key + " on thread "
                    + Thread.currentThread().getName());
            sleep(50);
            return "Value for " + key;
        };

        var threadSafeMemo = memoizeThreadSafe(expensiveLookup);

        // Simulate concurrent access
        System.out.println("Concurrent lookups:");
        Thread t1 = new Thread(() -> System.out.println("T1: " + threadSafeMemo.apply("key1")), "Thread-1");
        Thread t2 = new Thread(() -> System.out.println("T2: " + threadSafeMemo.apply("key1")), "Thread-2");
        Thread t3 = new Thread(() -> System.out.println("T3: " + threadSafeMemo.apply("key2")), "Thread-3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nSubsequent calls (all from cache):");
        System.out.println("key1: " + threadSafeMemo.apply("key1"));
        System.out.println("key2: " + threadSafeMemo.apply("key2"));

        System.out.println("""

        Thread-safe memoization:
        - ConcurrentHashMap for thread-safe cache
        - computeIfAbsent is atomic
        - Note: computation may run multiple times during race
        - For guaranteed single computation, use external locking
        """);
    }

    // Thread-safe memoization wrapper
    static <T, R> Function<T, R> memoizeThreadSafe(Function<T, R> function) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, function);
    }

    // ============ Caveats ============

    static void caveats() {
        System.out.println("=== Memoization Caveats ===");

        System.out.println("""
        WHEN TO USE memoization:
        - Pure functions (same input = same output)
        - Expensive computations
        - Repeated calls with same arguments
        - Limited input domain

        WHEN NOT TO USE:
        - Impure functions (side effects, randomness, time-dependent)
        - Cheap computations (cache overhead not worth it)
        - Large input domain (memory exhaustion)
        - Single-use computations

        PITFALLS:
        1. Memory leaks - cache grows unbounded
           Solution: Use weak references or LRU cache

        2. Stale data - cached values become outdated
           Solution: TTL (time-to-live) or manual invalidation

        3. Identity vs equality - objects as keys
           Solution: Ensure proper equals/hashCode

        4. Thread safety - race conditions
           Solution: ConcurrentHashMap or synchronization
        """);

        // Example: Memory-bounded memoization (simple LRU)
        System.out.println("-- Bounded Cache Example --");
        Function<Integer, Integer> squareFn = n -> {
            System.out.println("  Computing for " + n);
            return n * n;
        };
        var boundedMemo = memoizeBounded(squareFn, 3); // Max 3 entries

        System.out.println("square(1) = " + boundedMemo.apply(1));
        System.out.println("square(2) = " + boundedMemo.apply(2));
        System.out.println("square(3) = " + boundedMemo.apply(3));
        System.out.println("square(4) = " + boundedMemo.apply(4)); // Evicts oldest
        System.out.println("square(1) = " + boundedMemo.apply(1)); // Recomputed (was evicted)
    }

    // Simple bounded memoization (FIFO eviction)
    static <T, R> Function<T, R> memoizeBounded(Function<T, R> function, int maxSize) {
        // LinkedHashMap with access-order for LRU, but we use insertion-order for simplicity
        Map<T, R> cache = new java.util.LinkedHashMap<>(maxSize, 0.75f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, R> eldest) {
                return size() > maxSize;
            }
        };
        return input -> cache.computeIfAbsent(input, function);
    }

    // Helper
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
