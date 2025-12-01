package org.nkcoder.concurrency.unsafe;

import java.util.ArrayList;

/**
 * ArrayList is NOT thread-safe. {@code add()} involves: check size, store, increment.
 *
 * <p>Race conditions cause lost updates and ArrayIndexOutOfBoundsException.
 *
 * <p>Fix: Collections.synchronizedList, CopyOnWriteArrayList, or Vector.
 */
public class ArrayListUnSafe {
    private static final ArrayList<Integer> container = new ArrayList<>();

    public static class InsertThread implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 1000000; i++) {
                container.add(i);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread insertOne = new Thread(new InsertThread());
        Thread insertTwo = new Thread(new InsertThread());

        insertOne.start();
        insertTwo.start();
        insertOne.join();
        insertTwo.join();

        // Expected: 2000000 but varies due to race conditions
        System.out.println("contain size: " + container.size());
    }
}
