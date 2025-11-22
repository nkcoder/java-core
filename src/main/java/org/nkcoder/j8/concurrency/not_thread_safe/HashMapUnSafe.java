package org.nkcoder.j8.concurrency.not_thread_safe;

import java.util.HashMap;

public class HashMapUnSafe {
    /*
     * HashMap is not thread-safe and cannot be shared between multiple threads.
     * The `put()` operation involves:
     * 1. Calculate hash code
     * 2. Find bucket index
     * 3. Check for existing key
     * 4. Insert or update entry
     * 5. Possibly resize internal array
     */
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
        /*
         * In multi thread, race condition occur:
         * 1. Lost updates: both threads might compute the same bucket, and one overwrites another
         * 2. Inconsistent size: the size counter gets corrupted
         * 3. Broken structure: internal linked list nodes can get corrupted
         */
        Thread insertOne = new Thread(new Insert(0));
        Thread insertTwo = new Thread(new Insert(1));

        insertOne.start();
        insertTwo.start();

        insertOne.join();
        insertTwo.join();

        // Expected: 10000, Actual: 84771 (typically less than 10000
        System.out.println("map size: " + container.size());
    }
}
