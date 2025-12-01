package org.nkcoder.collections;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Queue and Deque implementations.
 *
 * <ul>
 *   <li>Queue: FIFO (first-in-first-out)</li>
 *   <li>Deque: double-ended queue (both ends accessible)</li>
 *   <li>PriorityQueue: elements ordered by priority (heap)</li>
 *   <li>ArrayDeque: faster than LinkedList for most use cases</li>
 * </ul>
 */
public class QueueExample {

  public static void main(String[] args) {
    queueBasicsExample();
    dequeExample();
    priorityQueueExample();
    stackWithDequeExample();
  }

  static void queueBasicsExample() {
    System.out.println("=== Queue Basics ===");

    // LinkedList implements Queue
    Queue<String> queue = new LinkedList<>();

    // Add elements (two ways)
    queue.add("first");      // Throws exception if full
    queue.offer("second");   // Returns false if full (preferred)
    queue.offer("third");

    System.out.println("Queue: " + queue);

    // Examine head (two ways)
    System.out.println("element(): " + queue.element());  // Throws if empty
    System.out.println("peek(): " + queue.peek());        // Returns null if empty

    // Remove head (two ways)
    System.out.println("remove(): " + queue.remove());    // Throws if empty
    System.out.println("poll(): " + queue.poll());        // Returns null if empty

    System.out.println("Remaining: " + queue);

    // Safe polling on empty queue
    Queue<String> empty = new LinkedList<>();
    System.out.println("poll() on empty: " + empty.poll());  // null, not exception
  }

  static void dequeExample() {
    System.out.println("\n=== Deque (Double-Ended Queue) ===");

    // ArrayDeque is generally faster than LinkedList
    Deque<String> deque = new ArrayDeque<>();

    // Add to both ends
    deque.addFirst("first");
    deque.addLast("last");
    deque.offerFirst("new-first");
    deque.offerLast("new-last");
    System.out.println("Deque: " + deque);

    // Examine both ends
    System.out.println("First: " + deque.peekFirst());
    System.out.println("Last: " + deque.peekLast());

    // Remove from both ends
    System.out.println("Remove first: " + deque.pollFirst());
    System.out.println("Remove last: " + deque.pollLast());
    System.out.println("Remaining: " + deque);

    // As FIFO queue
    deque.clear();
    deque.offer("a");
    deque.offer("b");
    deque.offer("c");
    System.out.println("FIFO poll: " + deque.poll() + ", " + deque.poll());

    // As LIFO stack
    deque.clear();
    deque.push("x");  // Same as addFirst
    deque.push("y");
    deque.push("z");
    System.out.println("LIFO pop: " + deque.pop() + ", " + deque.pop());
  }

  static void priorityQueueExample() {
    System.out.println("\n=== PriorityQueue ===");

    // Natural ordering (min-heap)
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    minHeap.offer(5);
    minHeap.offer(2);
    minHeap.offer(8);
    minHeap.offer(1);

    System.out.print("Min-heap order: ");
    while (!minHeap.isEmpty()) {
      System.out.print(minHeap.poll() + " ");
    }
    System.out.println();

    // Max-heap with reverse comparator
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
    maxHeap.offer(5);
    maxHeap.offer(2);
    maxHeap.offer(8);
    maxHeap.offer(1);

    System.out.print("Max-heap order: ");
    while (!maxHeap.isEmpty()) {
      System.out.print(maxHeap.poll() + " ");
    }
    System.out.println();

    // Custom objects with comparator
    record Task(String name, int priority) {}

    PriorityQueue<Task> taskQueue = new PriorityQueue<>(
        Comparator.comparingInt(Task::priority)
    );
    taskQueue.offer(new Task("Low", 3));
    taskQueue.offer(new Task("High", 1));
    taskQueue.offer(new Task("Medium", 2));

    System.out.println("Tasks by priority:");
    while (!taskQueue.isEmpty()) {
      Task task = taskQueue.poll();
      System.out.println("  " + task.name() + " (priority: " + task.priority() + ")");
    }
  }

  static void stackWithDequeExample() {
    System.out.println("\n=== Stack with Deque (Preferred over Stack class) ===");

    // Use Deque instead of legacy Stack class
    Deque<String> stack = new ArrayDeque<>();

    stack.push("bottom");
    stack.push("middle");
    stack.push("top");

    System.out.println("Stack: " + stack);
    System.out.println("Peek: " + stack.peek());
    System.out.println("Pop: " + stack.pop());
    System.out.println("After pop: " + stack);

    // Check empty before pop
    while (!stack.isEmpty()) {
      System.out.println("Popped: " + stack.pop());
    }
  }
}
