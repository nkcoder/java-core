package org.nkcoder.concurrency.unsafe;

import java.util.ArrayList;

/**
 * ArrayList Thread-Safety Demonstration
 *
 * This example demonstrates why ArrayList is NOT thread-safe.
 * The add() method involves multiple steps that can be interleaved:
 * 1. Check current size
 * 2. Store element at index
 * 3. Increment size
 *
 * When two threads execute simultaneously, race conditions occur:
 * 1. Both threads might read the same size value
 * 2. Both write to the same index, overwriting data
 * 3. The size increment might be lost
 *
 * How to Fix:
 * - Collections.synchronizedList(new ArrayList<>())
 * - CopyOnWriteArrayList
 * - External synchronization with synchronized blocks
 * - Vector
 */
public class ArrayListUnSafe {

    // ArrayList is not thread-safe, the `add()` method involves multiple steps:
    // 1. Check current size
    // 2. Store element at index
    // 3. Increment size
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
        // When two threads execute simultaneously, race conditions occur:
        // 1. Both threads might read the same size value
        // 2. Both write to the same index, overwriting data
        // 3. The size increment might be lost
        Thread insertOne = new Thread(new InsertThread());
        Thread insertTwo = new Thread(new InsertThread());

        insertOne.start();
        insertTwo.start();

        // Waits for the threads to finish
        insertOne.join();
        insertTwo.join();

        // Expectation: 2000000, Actual: varies (typically less than 2000000)
        System.out.println("contain size: " + container.size());
    }
}

/*
 * How to Fix:
 * - Collections.synchronizedList(new ArrayList<>())
 * - CopyOnWriteArrayList
 * - External synchronization with synchronized blocks
 * - Vector
 */
