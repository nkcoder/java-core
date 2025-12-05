package org.nkcoder.collections;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Set implementations: unique elements, no duplicates.
 *
 * <ul>
 *   <li>HashSet: O(1) operations, no order guarantee</li>
 *   <li>LinkedHashSet: O(1) operations, maintains insertion order</li>
 *   <li>TreeSet: O(log n) operations, sorted order</li>
 *   <li>Java 9+: {@code Set.of()} for immutable sets</li>
 * </ul>
 */
public class SetExample {

  static void main(String[] args) {
    hashSetExample();
    linkedHashSetExample();
    treeSetExample();
    setOperationsExample();
    factoryMethodsExample();
  }

  static void hashSetExample() {
    System.out.println("=== HashSet ===");

    // Hash table - fastest, no ordering
    HashSet<String> set = new HashSet<>();
    set.add("banana");
    set.add("apple");
    set.add("cherry");
    set.add("apple");  // Duplicate ignored

    System.out.println("Size: " + set.size());  // 3, not 4
    System.out.println("Contains apple: " + set.contains("apple"));
    System.out.println("Elements (unordered): " + set);
  }

  static void linkedHashSetExample() {
    System.out.println("\n=== LinkedHashSet ===");

    // Hash table + linked list - maintains insertion order
    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add("banana");
    set.add("apple");
    set.add("cherry");

    System.out.println("Elements (insertion order): " + set);

    // Useful for removing duplicates while preserving order
    var listWithDupes = java.util.List.of("c", "a", "b", "a", "c", "d");
    var deduplicated = new LinkedHashSet<>(listWithDupes);
    System.out.println("Deduplicated: " + deduplicated);
  }

  static void treeSetExample() {
    System.out.println("\n=== TreeSet ===");

    // Red-black tree - sorted order
    TreeSet<Integer> numbers = new TreeSet<>();
    numbers.add(5);
    numbers.add(2);
    numbers.add(8);
    numbers.add(1);
    numbers.add(9);

    System.out.println("Sorted: " + numbers);
    System.out.println("First: " + numbers.first());
    System.out.println("Last: " + numbers.last());

    // NavigableSet methods
    System.out.println("Lower than 5: " + numbers.lower(5));    // 2 (strictly less)
    System.out.println("Floor of 5: " + numbers.floor(5));      // 5 (less or equal)
    System.out.println("Ceiling of 6: " + numbers.ceiling(6));  // 8 (greater or equal)
    System.out.println("Higher than 5: " + numbers.higher(5));  // 8 (strictly greater)

    // Subsets
    System.out.println("Head (< 5): " + numbers.headSet(5));
    System.out.println("Tail (>= 5): " + numbers.tailSet(5));
    System.out.println("SubSet [2, 8): " + numbers.subSet(2, 8));

    // Custom ordering
    TreeSet<String> descending = new TreeSet<>(java.util.Comparator.reverseOrder());
    descending.addAll(Set.of("a", "c", "b"));
    System.out.println("Descending: " + descending);
  }

  static void setOperationsExample() {
    System.out.println("\n=== Set Operations ===");

    Set<Integer> a = new HashSet<>(Set.of(1, 2, 3, 4));
    Set<Integer> b = new HashSet<>(Set.of(3, 4, 5, 6));

    // Union
    Set<Integer> union = new HashSet<>(a);
    union.addAll(b);
    System.out.println("Union: " + union);

    // Intersection
    Set<Integer> intersection = new HashSet<>(a);
    intersection.retainAll(b);
    System.out.println("Intersection: " + intersection);

    // Difference (a - b)
    Set<Integer> difference = new HashSet<>(a);
    difference.removeAll(b);
    System.out.println("Difference (a - b): " + difference);
  }

  static void factoryMethodsExample() {
    System.out.println("\n=== Factory Methods (Java 9+) ===");

    // Set.of() - immutable, no duplicates allowed
    Set<String> immutable = Set.of("a", "b", "c");
    System.out.println("Immutable set: " + immutable);

    try {
      Set.of("a", "a");  // Duplicate throws exception
    } catch (IllegalArgumentException e) {
      System.out.println("Set.of() rejects duplicates: " + e.getMessage());
    }

    // Set.copyOf() - immutable copy (Java 10+)
    HashSet<String> mutable = new HashSet<>(Set.of("x", "y"));
    Set<String> copy = Set.copyOf(mutable);
    mutable.add("z");
    System.out.println("Original (modified): " + mutable);
    System.out.println("Copy (unchanged): " + copy);
  }
}
