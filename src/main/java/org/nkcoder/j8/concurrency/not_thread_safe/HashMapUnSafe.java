package org.nkcoder.j8.concurrency.not_thread_safe;

import java.util.HashMap;

/**
 * HashMap Thread-Safety Demonstration
 *
 * This example demonstrates why HashMap is NOT thread-safe and the problems
 * that occur with concurrent access.
 *
 * HashMap put() operation steps:
 * 1. Calculate hash code for the key
 * 2. Find bucket index using hash
 * 3. Check if key already exists in bucket
 * 4. Insert new entry or update existing one
 * 5. Increment size counter
 * 6. Possibly trigger resize (rehash entire table)
 *
 * Race conditions with concurrent HashMap access:
 *
 * 1. Lost Updates:
 *    - Both threads compute same bucket index
 *    - Both create new entries for different keys
 *    - One thread's entry overwrites the other's
 *    - Keys are silently lost
 *
 * 2. Corrupted Size:
 *    - Size++ is not atomic (read-increment-write)
 *    - Two threads read same size value
 *    - Both increment and write back
 *    - One increment is lost
 *    - size() returns incorrect value
 *
 * 3. Broken Internal Structure:
 *    - HashMap uses linked list/tree per bucket
 *    - Concurrent modifications corrupt node links
 *    - Can create cycles in linked list
 *    - Results in infinite loops or NullPointerException
 *
 * 4. Concurrent Resize Disaster:
 *    - When threshold exceeded, HashMap resizes
 *    - Creates new bucket array, rehashes all entries
 *    - If two threads resize simultaneously:
 *      * Entries can be lost
 *      * Links can form cycles
 *      * Can cause infinite loop in get()
 *
 * How to Fix:
 *
 * Option 1: Collections.synchronizedMap (simple but slow)
 * Map<K,V> map = Collections.synchronizedMap(new HashMap<>());
 * - Wraps HashMap with synchronized methods
 * - Coarse-grained locking (locks entire map)
 * - Safe but poor concurrent performance
 *
 * Option 2: ConcurrentHashMap (recommended)
 * Map<K,V> map = new ConcurrentHashMap<>();
 * - Lock-free reads
 * - Fine-grained locking for writes (segments/buckets)
 * - Much better concurrent performance
 * - Iterators are weakly consistent (don't fail on modification)
 *
 * Option 3: External synchronization
 * synchronized(map) { map.put(key, value); }
 * - Manual locking around all access
 * - Error-prone (easy to forget)
 * - Same poor performance as Collections.synchronizedMap
 *
 * This example demonstrates:
 * - Two threads inserting different keys concurrently
 * - Expected: 100,000 entries (50,000 per thread, non-overlapping keys)
 * - Actual: Much less due to lost updates and corruption
 * - Run multiple times - results vary (non-deterministic race conditions)
 */
public class HashMapUnSafe {
    // HashMap is not thread-safe - concurrent access causes data corruption
    private static final HashMap<Integer, String> container = new HashMap<>();

    public static class Insert implements Runnable {
        int start = 0;

        public Insert(int start) {
            this.start = start;
        }

        @Override
        public void run() {
            // Each thread inserts 50,000 entries with different keys
            // Thread 1: 0, 2, 4, 6, ... (even numbers)
            // Thread 2: 1, 3, 5, 7, ... (odd numbers)
            // Keys don't overlap, but race conditions still occur!
            for (int i = start; i < 100000; i += 2) {
                container.put(i, Integer.toString(i));
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Two threads with non-overlapping keys (should work if HashMap was thread-safe)
        Thread insertOne = new Thread(new Insert(0));  // Even numbers
        Thread insertTwo = new Thread(new Insert(1));  // Odd numbers

        insertOne.start();
        insertTwo.start();

        // Wait for both threads to complete
        insertOne.join();
        insertTwo.join();

        // Expected: 100,000 (50,000 per thread, no key overlap)
        // Actual: Typically 85,000-95,000 (lost updates, corrupted structure)
        // Result varies on each run due to non-deterministic race conditions
        System.out.println("Expected map size: 100000");
        System.out.println("Actual map size: " + container.size());
        System.out.println("Lost entries: " + (100000 - container.size()));
    }
}

/*
 * How to Fix (choose one):
 *
 * 1. ConcurrentHashMap (best performance):
 * private static final ConcurrentHashMap<Integer, String> container = new ConcurrentHashMap<>();
 *
 * 2. Collections.synchronizedMap (simple, lower performance):
 * private static final Map<Integer, String> container = Collections.synchronizedMap(new HashMap<>());
 *
 * 3. External synchronization (manual, error-prone):
 * synchronized(container) { container.put(i, Integer.toString(i)); }
 *
 * Recommendation: Use ConcurrentHashMap for new code
 */
