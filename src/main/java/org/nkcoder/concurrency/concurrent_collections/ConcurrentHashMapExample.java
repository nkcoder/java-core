package org.nkcoder.concurrency.concurrent_collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

/**
 * ConcurrentHashMap: High-performance thread-safe map for concurrent access.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Lock striping - better concurrency than full synchronization
 *   <li>Atomic compound operations (putIfAbsent, computeIfAbsent)
 *   <li>Weakly consistent iterators (no ConcurrentModificationException)
 *   <li>Bulk operations with parallelism threshold
 * </ul>
 *
 * <p>Interview tip: Know when to use computeIfAbsent vs putIfAbsent, and understand why check-then-act is broken even
 * with synchronized maps.
 */
public class ConcurrentHashMapExample {

    static void main(String[] args) throws Exception {
        whyConcurrentHashMap();
        basicOperations();
        atomicCompoundOperations();
        computeOperations();
        bulkOperations();
        concurrentHashMapVsAlternatives();
        bestPractices();
    }

    static void whyConcurrentHashMap() throws Exception {
        System.out.println("=== Why ConcurrentHashMap? ===");

        // Synchronized map - thread-safe for individual operations
        Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());

        // But check-then-act is STILL broken!
        // Thread 1: if (!map.containsKey(k)) map.put(k, v);
        // Thread 2: if (!map.containsKey(k)) map.put(k, v);
        // Both threads can pass the check and put!

        System.out.println("  Problem with synchronized maps:");
        System.out.println("  Individual operations are atomic, but compound operations are not.");

        // Demonstrate the race condition
        Map<String, Integer> syncCounter = Collections.synchronizedMap(new HashMap<>());
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 1000; i++) {
                executor.submit(() -> {
                    // BROKEN check-then-act pattern
                    if (!syncCounter.containsKey("count")) {
                        syncCounter.put("count", 1);
                    } else {
                        syncCounter.put("count", syncCounter.get("count") + 1);
                    }
                });
            }
        }
        System.out.println("  synchronizedMap counter: " + syncCounter.get("count") + " (expected 1000)");

        // ConcurrentHashMap with atomic operations
        ConcurrentHashMap<String, LongAdder> concurrentCounter = new ConcurrentHashMap<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < 1000; i++) {
                executor.submit(() -> {
                    // Atomic compound operation
                    concurrentCounter
                            .computeIfAbsent("count", k -> new LongAdder())
                            .increment();
                });
            }
        }
        System.out.println(
                "  ConcurrentHashMap counter: " + concurrentCounter.get("count").sum());

        System.out.println();
    }

    static void basicOperations() {
        System.out.println("=== Basic Operations ===");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // Standard Map operations (all thread-safe)
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        System.out.println("  get(\"one\"): " + map.get("one"));
        System.out.println("  size(): " + map.size());
        System.out.println("  containsKey(\"two\"): " + map.containsKey("two"));

        // Null keys and values are NOT allowed (unlike HashMap)
        try {
            map.put(null, 4);
        } catch (NullPointerException e) {
            System.out.println("  put(null, 4): NullPointerException (nulls not allowed)");
        }

        // Iteration is weakly consistent
        System.out.println("  Iterating (weakly consistent - no CME):");
        map.forEach((k, v) -> System.out.println("    " + k + " = " + v));

        System.out.println();
    }

    static void atomicCompoundOperations() {
        System.out.println("=== Atomic Compound Operations ===");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("a", 1);

        // putIfAbsent - only put if key doesn't exist
        Integer old1 = map.putIfAbsent("a", 10); // Returns 1, doesn't update
        Integer old2 = map.putIfAbsent("b", 2); // Returns null, inserts
        System.out.println("  putIfAbsent(\"a\", 10): returned " + old1 + ", a=" + map.get("a"));
        System.out.println("  putIfAbsent(\"b\", 2): returned " + old2 + ", b=" + map.get("b"));

        // remove(key, value) - only remove if value matches
        boolean removed1 = map.remove("a", 999); // Won't remove, value doesn't match
        boolean removed2 = map.remove("a", 1); // Removes
        System.out.println("  remove(\"a\", 999): " + removed1);
        System.out.println("  remove(\"a\", 1): " + removed2);

        // replace(key, value) - only if key exists
        map.put("c", 3);
        Integer old3 = map.replace("c", 30);
        Integer old4 = map.replace("d", 40); // Returns null, doesn't insert
        System.out.println("  replace(\"c\", 30): returned " + old3 + ", c=" + map.get("c"));
        System.out.println("  replace(\"d\", 40): returned " + old4 + ", d=" + map.get("d"));

        // replace(key, oldValue, newValue) - only if value matches
        boolean replaced = map.replace("c", 30, 300);
        System.out.println("  replace(\"c\", 30, 300): " + replaced + ", c=" + map.get("c"));

        System.out.println();
    }

    static void computeOperations() {
        System.out.println("=== Compute Operations (Most Important!) ===");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // computeIfAbsent - compute value if key absent
        // The lambda is called ONLY if key doesn't exist
        Integer val1 = map.computeIfAbsent("x", key -> {
            System.out.println("    Computing value for key: " + key);
            return key.length() * 10;
        });
        System.out.println("  computeIfAbsent(\"x\"): " + val1);

        // Second call - lambda NOT called (key exists)
        Integer val2 = map.computeIfAbsent("x", key -> {
            System.out.println("    This won't print!");
            return 999;
        });
        System.out.println("  computeIfAbsent(\"x\") again: " + val2);

        // computeIfPresent - compute new value if key exists
        map.put("y", 5);
        Integer val3 = map.computeIfPresent("y", (key, oldVal) -> oldVal * 2);
        Integer val4 = map.computeIfPresent("z", (key, oldVal) -> oldVal * 2); // z doesn't exist
        System.out.println("  computeIfPresent(\"y\"): " + val3);
        System.out.println("  computeIfPresent(\"z\"): " + val4);

        // compute - always compute (can insert, update, or remove)
        Integer val5 = map.compute("y", (key, oldVal) -> oldVal == null ? 1 : oldVal + 1);
        System.out.println("  compute(\"y\", increment): " + val5);

        // Returning null removes the entry
        map.compute("y", (key, oldVal) -> null);
        System.out.println("  compute returning null removes entry: y=" + map.get("y"));

        // merge - combine old and new values
        map.put("m", 10);
        Integer val6 = map.merge("m", 5, Integer::sum); // 10 + 5
        Integer val7 = map.merge("n", 5, Integer::sum); // No old value, just 5
        System.out.println("  merge(\"m\", 5, sum): " + val6);
        System.out.println("  merge(\"n\", 5, sum): " + val7);

        System.out.println("""

        Key methods:
        +-------------------+-------------+---------------------------+
        | Method            | Key exists? | Behavior                  |
        +-------------------+-------------+---------------------------+
        | computeIfAbsent   | No          | Compute and insert        |
        | computeIfAbsent   | Yes         | Return existing (no-op)   |
        | computeIfPresent  | No          | Return null (no-op)       |
        | computeIfPresent  | Yes         | Compute and update        |
        | compute           | Either      | Always compute            |
        | merge             | No          | Use new value directly    |
        | merge             | Yes         | Merge old and new         |
        +-------------------+-------------+---------------------------+
        """);
    }

    static void bulkOperations() {
        System.out.println("=== Bulk Operations (Java 8+) ===");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        for (int i = 1; i <= 100; i++) {
            map.put("key" + i, i);
        }

        // Parallelism threshold: operations run in parallel if size > threshold
        // Long.MAX_VALUE = sequential, 1 = always parallel

        // forEach - parallel iteration
        System.out.println("  forEach (parallel with threshold 10):");
        map.forEach(10, (k, v) -> {
            if (v <= 3)
                System.out.println(
                        "    " + k + "=" + v + " on " + Thread.currentThread().getName());
        });

        // search - find first match (parallel)
        String found = map.search(10, (k, v) -> v == 50 ? k : null);
        System.out.println("  search for value 50: " + found);

        // reduce - aggregate values (parallel)
        Integer sum = map.reduce(
                10,
                (k, v) -> v, // Transform
                Integer::sum // Combine
                );
        System.out.println("  reduce (sum all values): " + sum);

        // reduceValues - simpler for value aggregation
        Integer max = map.reduceValues(10, Integer::max);
        System.out.println("  reduceValues (max): " + max);

        // Specialized reducers
        long sumLong = map.reduceValuesToLong(10, v -> v, 0L, Long::sum);
        System.out.println("  reduceValuesToLong: " + sumLong);

        System.out.println();
    }

    static void concurrentHashMapVsAlternatives() {
        System.out.println("=== ConcurrentHashMap vs Alternatives ===");

        System.out.println("""
        +------------------------+------------------+------------------+-------------------+
        | Feature                | ConcurrentHashMap| synchronizedMap  | Hashtable         |
        +------------------------+------------------+------------------+-------------------+
        | Locking                | Segment/bucket   | Whole map        | Whole map         |
        | Concurrent reads       | Yes (lock-free)  | Blocked          | Blocked           |
        | Concurrent writes      | Yes (different   | No               | No                |
        |                        | segments)        |                  |                   |
        | Null keys/values       | Not allowed      | Allowed          | Not allowed       |
        | Atomic compounds       | Yes (compute*)   | No               | No                |
        | Iterator behavior      | Weakly consistent| Fail-fast        | Fail-fast         |
        | Performance            | Best             | Worst            | Bad               |
        +------------------------+------------------+------------------+-------------------+

        When to use what:
        - ConcurrentHashMap: Default choice for concurrent maps
        - synchronizedMap: Legacy code, need null values
        - Hashtable: Never (legacy, replaced by ConcurrentHashMap)

        ConcurrentHashMap internals (Java 8+):
        - Uses CAS + synchronized on bins (not segments)
        - Red-black trees for bins with many collisions
        - Lock-free reads in most cases
        """);
    }

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        DO:
        - Use computeIfAbsent for lazy initialization:
            cache.computeIfAbsent(key, k -> expensiveComputation(k))

        - Use merge for counting/accumulating:
            wordCounts.merge(word, 1, Integer::sum)

        - Use LongAdder for high-contention counters:
            map.computeIfAbsent(key, k -> new LongAdder()).increment()

        - Set initial capacity if size is known:
            new ConcurrentHashMap<>(expectedSize)

        DON'T:
        - Use check-then-act patterns:
            // BROKEN
            if (!map.containsKey(k)) map.put(k, v);
            // CORRECT
            map.putIfAbsent(k, v);

        - Iterate and modify without atomic ops:
            // BROKEN
            Integer old = map.get(k);
            map.put(k, old + 1);
            // CORRECT
            map.compute(k, (key, val) -> val + 1);

        - Put null keys or values (throws NPE)

        - Hold locks inside compute lambdas (can deadlock):
            map.compute(k, (key, val) -> {
                synchronized(other) { ... } // DANGER!
            });

        Common patterns:
        - Cache: computeIfAbsent
        - Counter: merge with Integer::sum or LongAdder
        - Multimap: computeIfAbsent with List::new
        """);
    }
}
