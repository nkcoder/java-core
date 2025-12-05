package org.nkcoder.collections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

/**
 * SequencedCollection (Java 21): Unified API for ordered collections.
 *
 * <ul>
 *   <li>{@code getFirst()}, {@code getLast()}: access ends</li>
 *   <li>{@code addFirst()}, {@code addLast()}: add at ends</li>
 *   <li>{@code removeFirst()}, {@code removeLast()}: remove from ends</li>
 *   <li>{@code reversed()}: reversed view of the collection</li>
 * </ul>
 *
 * <p>Implemented by: List, Deque, LinkedHashSet, SortedSet, LinkedHashMap, SortedMap
 */
public class SequencedCollectionExample {

  static void main(String[] args) {
    sequencedCollectionExample();
    sequencedSetExample();
    sequencedMapExample();
    reversedViewExample();
  }

  static void sequencedCollectionExample() {
    System.out.println("=== SequencedCollection (List) ===");

    // ArrayList implements SequencedCollection
    SequencedCollection<String> list = new ArrayList<>(List.of("a", "b", "c"));

    // Access ends (replaces get(0) and get(size-1))
    System.out.println("First: " + list.getFirst());
    System.out.println("Last: " + list.getLast());

    // Add at ends
    list.addFirst("start");
    list.addLast("end");
    System.out.println("After addFirst/addLast: " + list);

    // Remove from ends
    list.removeFirst();
    list.removeLast();
    System.out.println("After removeFirst/removeLast: " + list);

    // Reversed view
    System.out.println("Reversed: " + list.reversed());
  }

  static void sequencedSetExample() {
    System.out.println("\n=== SequencedSet (LinkedHashSet) ===");

    // LinkedHashSet implements SequencedSet
    SequencedSet<String> set = new LinkedHashSet<>(List.of("one", "two", "three"));

    System.out.println("First: " + set.getFirst());
    System.out.println("Last: " + set.getLast());

    // Add at specific position
    set.addFirst("zero");  // Moves to front if exists, else adds
    set.addLast("four");
    System.out.println("Set: " + set);

    // Reversed iteration
    System.out.println("Reversed: " + set.reversed());

    // Note: addFirst on existing element moves it to front
    set.addFirst("three");
    System.out.println("After addFirst('three'): " + set);
  }

  static void sequencedMapExample() {
    System.out.println("\n=== SequencedMap (LinkedHashMap) ===");

    // LinkedHashMap implements SequencedMap
    SequencedMap<String, Integer> map = new LinkedHashMap<>();
    map.put("b", 2);
    map.put("c", 3);
    map.put("d", 4);

    System.out.println("Map: " + map);

    // Access first/last entries
    System.out.println("First entry: " + map.firstEntry());
    System.out.println("Last entry: " + map.lastEntry());

    // Add at ends
    map.putFirst("a", 1);
    map.putLast("e", 5);
    System.out.println("After putFirst/putLast: " + map);

    // Poll (remove and return) from ends
    System.out.println("Poll first: " + map.pollFirstEntry());
    System.out.println("Poll last: " + map.pollLastEntry());
    System.out.println("Remaining: " + map);

    // Reversed view
    System.out.println("Reversed: " + map.reversed());

    // Sequenced key/value/entry sets
    SequencedMap<String, Integer> fresh = new LinkedHashMap<>();
    fresh.put("x", 1);
    fresh.put("y", 2);
    fresh.put("z", 3);
    System.out.println("Keys reversed: " + fresh.sequencedKeySet().reversed());
    System.out.println("Values reversed: " + fresh.sequencedValues().reversed());
  }

  static void reversedViewExample() {
    System.out.println("\n=== Reversed View Behavior ===");

    ArrayList<String> original = new ArrayList<>(List.of("a", "b", "c"));
    List<String> reversed = original.reversed();

    System.out.println("Original: " + original);
    System.out.println("Reversed view: " + reversed);

    // Views are backed by original - modifications reflect in both
    reversed.addFirst("z");  // Adds to end of original
    System.out.println("After reversed.addFirst('z'):");
    System.out.println("  Original: " + original);
    System.out.println("  Reversed: " + reversed);

    // Iterate in reverse order
    System.out.print("Reverse iteration: ");
    for (String s : original.reversed()) {
      System.out.print(s + " ");
    }
    System.out.println();

    // Double reverse returns view of original
    System.out.println("Double reversed: " + original.reversed().reversed());
  }
}
