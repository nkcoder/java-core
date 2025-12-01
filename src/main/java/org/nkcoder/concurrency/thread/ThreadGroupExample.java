package org.nkcoder.concurrency.thread;

/**
 * ThreadGroup Example
 *
 * ThreadGroup is a way to organize threads into hierarchical groups.
 * It allows you to perform operations on multiple threads at once.
 *
 * Key characteristics:
 * 1. Provides a way to group related threads together
 * 2. Forms a tree structure - groups can contain threads and other groups
 * 3. Useful for managing threads as a unit (interrupt all, set priority, etc.)
 * 4. Every thread belongs to a thread group (main group by default)
 * 5. Thread inherits parent thread's group if not specified
 *
 * Common operations:
 * - activeCount(): Number of active threads in group
 * - enumerate(Thread[]): Copy active threads into array
 * - list(): Print information about group and threads (useful for debugging)
 * - interrupt(): Interrupt all threads in the group
 * - setMaxPriority(): Set maximum priority for threads in group
 *
 * Use cases:
 * - Organizing threads by functionality (e.g., worker-group, monitor-group)
 * - Bulk operations on related threads (interrupt all, enumerate all)
 * - Security: restricting what threads can do
 * - Debugging: identifying and tracking groups of threads
 *
 * Modern alternatives:
 * - ExecutorService: Better for managing pools of threads
 * - Fork/Join framework: For divide-and-conquer parallel tasks
 * - ThreadGroup is considered somewhat legacy but still useful for organization
 *
 * Best practices:
 * ✓ Use for logical grouping and debugging
 * ✓ Good for bulk operations on related threads
 * ✗ Don't rely on ThreadGroup for synchronization (use locks instead)
 * ✗ Consider ExecutorService for task management
 *
 * This example demonstrates:
 * - Creating a named thread group
 * - Adding threads to the group
 * - Querying group status (activeCount, list)
 * - Threads can access their group information
 */
public class ThreadGroupExample implements Runnable {

  // volatile ensures visibility of changes across threads
  // When main thread sets done=true, worker threads see it immediately
  private static volatile boolean done = false;

  public static void main(String[] args) throws InterruptedException {
    // Create a named thread group for organization
    ThreadGroup threadGroup = new ThreadGroup("readAndWriteToList-group");

    // Create threads belonging to this group
    // Constructor: new Thread(group, runnable, name)
    Thread t1 = new Thread(threadGroup, new ThreadGroupExample(), "t1");
    Thread t2 = new Thread(threadGroup, new ThreadGroupExample(), "t2");

    t1.start();
    t2.start();

    // list() prints group structure and all threads - useful for debugging
    // Output shows: ThreadGroup[readAndWriteToList-group,maxpri=10]
    //               Thread[t1,5,readAndWriteToList-group]
    //               Thread[t2,5,readAndWriteToList-group]
    threadGroup.list();

    // activeCount() returns number of active (running) threads in the group
    System.out.println("activeCount: " + threadGroup.activeCount());

    // Let threads run for 200ms
    Thread.sleep(200);

    // Signal threads to stop
    done = true;

    // Alternative: could use threadGroup.interrupt() to interrupt all threads at once
  }

  @Override
  public void run() {
    while (!done) {
      // Each thread can access its group information
      String name = Thread.currentThread().getThreadGroup().getName() + ": "
          + Thread.currentThread().getName();
      System.out.println("I'm: " + name);
    }

  }
}
