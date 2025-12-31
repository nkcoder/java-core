package org.nkcoder.collections;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * Deep Dive into ConcurrentHashMap.
 *
 * <p>Key Concepts: 1. **Thread Safety != Atomic Logic**: Just because the map is thread-safe doesn't mean your logic
 * is. Using `get` then `put` exhibits race conditions (Check-Then-Act). 2. **Correct Atomic Operations**:
 * {@code computeIfAbsent}, {@code compute}, {@code merge}. 3. **Internals**: Segment Locking (Java 7) vs CAS + Node
 * Locking (Java 8+).
 */
public class ConcurrentHashMapDeepDive {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ConcurrentHashMap Deep Dive ===");

        testRaceCondition();
        testCorrectUsage();
    }

    // The WRONG way: "Check-Then-Act" race condition
    static void testRaceCondition() throws InterruptedException {
        System.out.println("\n--- 1. Race Condition Demo (The Wrong Way) ---");
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // We want to count occurrences of "key"
        // Intended logic: value = value + 1

        int threadCount = 100;
        int incrementsPerThread = 1000;
        int expectedTotal = threadCount * incrementsPerThread;

        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // BUG: This sequence is NOT atomic
                        Integer oldVal = map.get("key");
                        int newVal = (oldVal == null) ? 1 : oldVal + 1;
                        map.put("key", newVal);
                    }
                });
            }
        } // waits for termination

        System.out.println("Expected: " + expectedTotal);
        System.out.println("Actual:   " + map.get("key"));
        System.out.println("Result:   " + (map.get("key").equals(expectedTotal) ? "PASS" : "FAIL (Race Condition)"));
    }

    // The CORRECT way: Atomic methods
    static void testCorrectUsage() throws InterruptedException {
        System.out.println("\n--- 2. Atomic Operations (The Correct Way) ---");
        ConcurrentHashMap<String, LongAdder> map = new ConcurrentHashMap<>();

        int threadCount = 100;
        int incrementsPerThread = 1000;
        int expectedTotal = threadCount * incrementsPerThread;

        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // CORRECT: computeIfAbsent is atomic for insertion
                        // LongAdder is thread-safe for increments
                        map.computeIfAbsent("key", k -> new LongAdder()).increment();
                    }
                });
            }
        }

        System.out.println("Expected: " + expectedTotal);
        long actual = map.get("key").sum();
        System.out.println("Actual:   " + actual);
        System.out.println("Result:   " + (actual == expectedTotal ? "PASS" : "FAIL"));

        System.out.println("\nNote on Internals:");
        System.out.println("- Java 8+ uses CAS (Compare-And-Swap) for the first node insertion.");
        System.out.println("- If collision occurs, it locks ONLY that specific bucket/node (synchronized).");
        System.out.println("- This allows extremely high concurrency compared to Collections.synchronizedMap().");
    }
}
