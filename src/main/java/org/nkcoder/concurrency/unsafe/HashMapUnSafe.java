package org.nkcoder.concurrency.unsafe;

import java.util.HashMap;

/**
 * HashMap is NOT thread-safe. Concurrent access causes:
 *
 * <ul>
 *   <li>Lost updates (entries silently overwritten)</li>
 *   <li>Corrupted size counter</li>
 *   <li>Broken internal structure (infinite loops, NPE)</li>
 * </ul>
 *
 * <p>Fix: Use ConcurrentHashMap (best) or Collections.synchronizedMap
 */
public class HashMapUnSafe {
    private static final HashMap<Integer, String> container = new HashMap<>();

    public static class Insert implements Runnable {
        int start = 0;

        public Insert(int start) {
            this.start = start;
        }

        @Override
        public void run() {
            for (int i = start; i < 100000; i += 2) {
                container.put(i, Integer.toString(i));
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread insertOne = new Thread(new Insert(0));
        Thread insertTwo = new Thread(new Insert(1));

        insertOne.start();
        insertTwo.start();
        insertOne.join();
        insertTwo.join();

        // Expected: 100,000 but varies due to race conditions
        System.out.println("Expected map size: 100000");
        System.out.println("Actual map size: " + container.size());
        System.out.println("Lost entries: " + (100000 - container.size()));
    }
}
