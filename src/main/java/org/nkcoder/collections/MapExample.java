package org.nkcoder.collections;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Map implementations: key-value pairs.
 *
 * <ul>
 *   <li>HashMap: O(1) operations, no order guarantee</li>
 *   <li>LinkedHashMap: O(1) operations, maintains insertion order</li>
 *   <li>TreeMap: O(log n) operations, sorted by keys</li>
 *   <li>Java 9+: {@code Map.of()}, {@code Map.entry()} for immutable maps</li>
 * </ul>
 */
public class MapExample {

  static void main(String[] args) {
    hashMapExample();
    linkedHashMapExample();
    treeMapExample();
    mapMethodsExample();
    factoryMethodsExample();
  }

  static void hashMapExample() {
    System.out.println("=== HashMap ===");

    HashMap<String, Integer> map = new HashMap<>();
    map.put("apple", 3);
    map.put("banana", 5);
    map.put("cherry", 2);

    System.out.println("Get apple: " + map.get("apple"));
    System.out.println("Get missing: " + map.get("orange"));  // null
    System.out.println("GetOrDefault: " + map.getOrDefault("orange", 0));
    System.out.println("Contains key 'banana': " + map.containsKey("banana"));
    System.out.println("Contains value 5: " + map.containsValue(5));

    // Iteration
    System.out.println("Entries:");
    map.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
  }

  static void linkedHashMapExample() {
    System.out.println("\n=== LinkedHashMap ===");

    // Maintains insertion order
    LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
    map.put("third", 3);
    map.put("first", 1);
    map.put("second", 2);
    System.out.println("Insertion order: " + map);

    // Access-order mode (useful for LRU cache)
    LinkedHashMap<String, Integer> accessOrder = new LinkedHashMap<>(16, 0.75f, true);
    accessOrder.put("a", 1);
    accessOrder.put("b", 2);
    accessOrder.put("c", 3);
    accessOrder.get("a");  // Access moves "a" to end
    System.out.println("Access order (after get 'a'): " + accessOrder);
  }

  static void treeMapExample() {
    System.out.println("\n=== TreeMap ===");

    // Sorted by keys
    TreeMap<String, Integer> map = new TreeMap<>();
    map.put("cherry", 2);
    map.put("apple", 3);
    map.put("banana", 5);
    System.out.println("Sorted by key: " + map);

    System.out.println("First key: " + map.firstKey());
    System.out.println("Last key: " + map.lastKey());

    // NavigableMap methods
    System.out.println("Lower than 'cherry': " + map.lowerKey("cherry"));
    System.out.println("Floor of 'cherry': " + map.floorKey("cherry"));
    System.out.println("Ceiling of 'blueberry': " + map.ceilingKey("blueberry"));

    // Submaps
    System.out.println("Head (< 'cherry'): " + map.headMap("cherry"));
    System.out.println("Tail (>= 'banana'): " + map.tailMap("banana"));

    // Descending order
    System.out.println("Descending: " + map.descendingMap());
  }

  static void mapMethodsExample() {
    System.out.println("\n=== Useful Map Methods (Java 8+) ===");

    Map<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);

    // putIfAbsent - only puts if key is absent
    map.putIfAbsent("a", 100);  // Ignored, "a" exists
    map.putIfAbsent("c", 3);    // Added
    System.out.println("After putIfAbsent: " + map);

    // computeIfAbsent - compute value if key is absent
    map.computeIfAbsent("d", k -> k.length() * 10);
    System.out.println("After computeIfAbsent: " + map);

    // computeIfPresent - update existing value
    map.computeIfPresent("a", (k, v) -> v * 2);
    System.out.println("After computeIfPresent: " + map);

    // merge - combine old and new values
    map.merge("a", 5, Integer::sum);  // a = 2 + 5 = 7
    map.merge("e", 5, Integer::sum);  // e = 5 (key was absent)
    System.out.println("After merge: " + map);

    // replaceAll - transform all values
    map.replaceAll((k, v) -> v + 100);
    System.out.println("After replaceAll: " + map);
  }

  static void factoryMethodsExample() {
    System.out.println("\n=== Factory Methods (Java 9+) ===");

    // Map.of() - up to 10 key-value pairs
    Map<String, Integer> small = Map.of("a", 1, "b", 2, "c", 3);
    System.out.println("Map.of(): " + small);

    // Map.ofEntries() - any number of entries
    Map<String, Integer> larger = Map.ofEntries(
        Map.entry("one", 1),
        Map.entry("two", 2),
        Map.entry("three", 3),
        Map.entry("four", 4)
    );
    System.out.println("Map.ofEntries(): " + larger);

    // Immutability
    try {
      small.put("d", 4);
    } catch (UnsupportedOperationException e) {
      System.out.println("Cannot modify immutable map");
    }

    // Map.copyOf() (Java 10+)
    HashMap<String, Integer> mutable = new HashMap<>(Map.of("x", 1));
    Map<String, Integer> copy = Map.copyOf(mutable);
    mutable.put("y", 2);
    System.out.println("Original (modified): " + mutable);
    System.out.println("Copy (unchanged): " + copy);

    // No null keys or values
    try {
      Map.of("key", null);
    } catch (NullPointerException e) {
      System.out.println("Map.of() does not allow null values");
    }
  }
}
