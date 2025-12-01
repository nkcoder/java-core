package org.nkcoder.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable and unmodifiable collections.
 *
 * <ul>
 *   <li>{@code List.of()}, {@code Set.of()}, {@code Map.of()}: truly immutable (Java 9+)</li>
 *   <li>{@code Collections.unmodifiableXxx()}: unmodifiable view of mutable collection</li>
 *   <li>{@code List.copyOf()}: immutable copy (Java 10+)</li>
 *   <li>Defensive copies: protect internal state from external modification</li>
 * </ul>
 */
public class ImmutableCollectionsExample {

  public static void main(String[] args) {
    factoryMethodsExample();
    unmodifiableViewExample();
    copyOfExample();
    defensiveCopyExample();
    immutabilityVsUnmodifiableExample();
  }

  static void factoryMethodsExample() {
    System.out.println("=== Factory Methods (Java 9+) ===");

    // Truly immutable - cannot be modified
    List<String> list = List.of("a", "b", "c");
    Set<String> set = Set.of("x", "y", "z");
    Map<String, Integer> map = Map.of("one", 1, "two", 2);

    System.out.println("Immutable list: " + list);
    System.out.println("Immutable set: " + set);
    System.out.println("Immutable map: " + map);

    // Modification attempts throw UnsupportedOperationException
    try {
      list.add("d");
    } catch (UnsupportedOperationException e) {
      System.out.println("Cannot add to immutable list");
    }

    // Null values not allowed
    try {
      List.of("a", null);
    } catch (NullPointerException e) {
      System.out.println("Null not allowed in factory methods");
    }

    // Duplicate keys not allowed in Set.of() and Map.of()
    try {
      Set.of("a", "a");
    } catch (IllegalArgumentException e) {
      System.out.println("Duplicates not allowed in Set.of()");
    }
  }

  static void unmodifiableViewExample() {
    System.out.println("\n=== Unmodifiable View (Collections.unmodifiableXxx) ===");

    ArrayList<String> mutable = new ArrayList<>(List.of("a", "b", "c"));
    List<String> unmodifiable = Collections.unmodifiableList(mutable);

    System.out.println("Unmodifiable view: " + unmodifiable);

    // Cannot modify through the view
    try {
      unmodifiable.add("d");
    } catch (UnsupportedOperationException e) {
      System.out.println("Cannot modify through unmodifiable view");
    }

    // BUT: changes to original are visible through view!
    mutable.add("d");
    System.out.println("After modifying original:");
    System.out.println("  Original: " + mutable);
    System.out.println("  View: " + unmodifiable);  // Also shows "d"!
  }

  static void copyOfExample() {
    System.out.println("\n=== copyOf() Methods (Java 10+) ===");

    ArrayList<String> mutable = new ArrayList<>(List.of("a", "b", "c"));

    // Creates independent immutable copy
    List<String> copy = List.copyOf(mutable);

    mutable.add("d");  // Original modified
    System.out.println("Original (modified): " + mutable);
    System.out.println("Copy (unchanged): " + copy);

    // copyOf on already immutable returns same instance (optimization)
    List<String> immutable = List.of("x", "y");
    List<String> copyOfImmutable = List.copyOf(immutable);
    System.out.println("Same instance: " + (immutable == copyOfImmutable));   // true

    // Works with Set and Map too
    Set<String> setCopy = Set.copyOf(Set.of("1", "2", "3"));
    Map<String, Integer> mapCopy = Map.copyOf(Map.of("a", 1, "b", 2));
    System.out.println("Set copy: " + setCopy);
    System.out.println("Map copy: " + mapCopy);
  }

  static void defensiveCopyExample() {
    System.out.println("\n=== Defensive Copies ===");

    // Bad: exposing internal mutable collection
    class BadClass {
      private final List<String> items = new ArrayList<>();

      void addItem(String item) {
        items.add(item);
      }

      List<String> getItems() {
        return items;  // Exposes internal state!
      }
    }

    BadClass bad = new BadClass();
    bad.addItem("secret");
    bad.getItems().clear();  // External code can modify internal state!
    System.out.println("BadClass items after external clear: " + bad.getItems());

    // Good: returning defensive copy or unmodifiable view
    class GoodClass {
      private final List<String> items = new ArrayList<>();

      void addItem(String item) {
        items.add(item);
      }

      // Option 1: Return unmodifiable view
      List<String> getItemsView() {
        return Collections.unmodifiableList(items);
      }

      // Option 2: Return defensive copy (truly independent)
      List<String> getItemsCopy() {
        return List.copyOf(items);
      }
    }

    GoodClass good = new GoodClass();
    good.addItem("secret");

    try {
      good.getItemsView().clear();
    } catch (UnsupportedOperationException e) {
      System.out.println("Cannot modify through view");
    }

    List<String> copy = good.getItemsCopy();
    System.out.println("Defensive copy is independent: " + copy);
  }

  static void immutabilityVsUnmodifiableExample() {
    System.out.println("\n=== Immutable vs Unmodifiable ===");

    // Unmodifiable: just a read-only wrapper
    // - Original can still be modified
    // - View reflects those changes
    ArrayList<String> original = new ArrayList<>(List.of("a", "b"));
    List<String> unmodifiable = Collections.unmodifiableList(original);
    original.add("c");
    System.out.println("Unmodifiable reflects changes: " + unmodifiable);

    // Immutable (List.of / List.copyOf): truly unchangeable
    // - No way to modify the data
    // - Thread-safe without synchronization
    List<String> immutable = List.of("x", "y", "z");
    // No reference to any underlying mutable collection exists

    System.out.println("\nKey differences:");
    System.out.println("  Unmodifiable: wrapper, original can change, view updates");
    System.out.println("  Immutable: independent copy, truly unchangeable");
    System.out.println("  Use immutable when possible for thread safety and clarity");
  }
}
