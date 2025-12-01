package org.nkcoder.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * List implementations: ArrayList vs LinkedList.
 *
 * <ul>
 *   <li>ArrayList: O(1) random access, O(n) insert/delete in middle</li>
 *   <li>LinkedList: O(n) random access, O(1) insert/delete at ends</li>
 *   <li>Java 9+: {@code List.of()} for immutable lists</li>
 *   <li>Java 10+: {@code List.copyOf()} for immutable copy</li>
 * </ul>
 */
public class ListExample {

  public static void main(String[] args) {
    arrayListExample();
    linkedListExample();
    factoryMethodsExample();
  }

  static void arrayListExample() {
    System.out.println("=== ArrayList ===");

    // Dynamic array - best for random access
    ArrayList<String> list = new ArrayList<>();
    list.add("apple");
    list.add("banana");
    list.add("cherry");
    list.add(1, "avocado");  // Insert at index

    System.out.println("Get by index: " + list.get(2));
    System.out.println("Contains banana: " + list.contains("banana"));
    System.out.println("Index of cherry: " + list.indexOf("cherry"));

    list.remove("banana");
    list.removeFirst();  // Java 21+
    System.out.println("After removals: " + list);

    // Iteration
    list.forEach(item -> System.out.println("  - " + item));
  }

  static void linkedListExample() {
    System.out.println("\n=== LinkedList ===");

    // Doubly-linked list - efficient insert/delete at ends
    LinkedList<String> list = new LinkedList<>();
    list.add("one");
    list.add("two");
    list.addFirst("zero");
    list.addLast("three");

    System.out.println("First: " + list.getFirst());
    System.out.println("Last: " + list.getLast());

    list.removeFirst();
    list.removeLast();
    System.out.println("After removing ends: " + list);

    // As Deque (double-ended queue)
    list.push("pushed");  // Add to front
    System.out.println("Peek: " + list.peek());
    System.out.println("Pop: " + list.pop());
  }

  static void factoryMethodsExample() {
    System.out.println("\n=== Factory Methods (Java 9+) ===");

    // List.of() - immutable list
    List<String> immutable = List.of("a", "b", "c");
    System.out.println("Immutable list: " + immutable);

    try {
      immutable.add("d");
    } catch (UnsupportedOperationException e) {
      System.out.println("Cannot modify immutable list: " + e.getClass().getSimpleName());
    }

    // List.copyOf() - immutable copy (Java 10+)
    ArrayList<String> mutable = new ArrayList<>(List.of("x", "y", "z"));
    List<String> copy = List.copyOf(mutable);
    mutable.add("modified");
    System.out.println("Original (modified): " + mutable);
    System.out.println("Copy (unchanged): " + copy);

    // No nulls allowed in factory methods
    try {
      List.of("a", null, "b");
    } catch (NullPointerException e) {
      System.out.println("List.of() does not allow nulls");
    }
  }
}
