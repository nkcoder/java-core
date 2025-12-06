package org.nkcoder.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * Wildcards in Generics: Flexible type bounds with ?, extends, and super.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, unchanged. Wildcards enable
 * more flexible APIs while maintaining type safety.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Unbounded wildcard: ? (unknown type)</li>
 *   <li>Upper bounded: ? extends T (read-only, covariant)</li>
 *   <li>Lower bounded: ? super T (write-only, contravariant)</li>
 *   <li>PECS principle: Producer Extends, Consumer Super</li>
 * </ul>
 */
public class WildcardsExample {

  static void main(String[] args) {
    unboundedWildcard();
    upperBoundedWildcard();
    lowerBoundedWildcard();
    pecsExplained();
    wildcardCapture();
    practicalExamples();
  }

  // ===== Unbounded Wildcard: ? =====

  static void unboundedWildcard() {
    System.out.println("=== Unbounded Wildcard: ? ===");

    List<String> strings = List.of("Hello", "World");
    List<Integer> integers = List.of(1, 2, 3);
    List<Double> doubles = List.of(1.1, 2.2, 3.3);

    // ? means "unknown type" - can accept any List
    printList(strings);
    printList(integers);
    printList(doubles);

    System.out.println("""

        Unbounded wildcard (?):
        - Accepts any parameterized type
        - Read as Object (lose specific type info)
        - Can only add null (type unknown)
        - Use when you don't care about the element type
        """);
  }

  // Works with any List regardless of element type
  static void printList(List<?> list) {
    System.out.print("  List contents: ");
    for (Object item : list) {  // Can only read as Object
      System.out.print(item + " ");
    }
    System.out.println();

    // list.add("something");  // Compile error! Type unknown
    // list.add(null);         // Only null is allowed (but not useful)
  }

  // ===== Upper Bounded Wildcard: ? extends T =====

  static void upperBoundedWildcard() {
    System.out.println("=== Upper Bounded Wildcard: ? extends T ===");

    List<Integer> integers = List.of(1, 2, 3);
    List<Double> doubles = List.of(1.5, 2.5, 3.5);
    List<Number> numbers = new ArrayList<>(List.of(10, 20.5));

    // ? extends Number accepts List<Integer>, List<Double>, List<Number>
    System.out.println("  Sum of integers: " + sumNumbers(integers));
    System.out.println("  Sum of doubles: " + sumNumbers(doubles));
    System.out.println("  Sum of numbers: " + sumNumbers(numbers));

    // List<String> strings = List.of("a", "b");
    // sumNumbers(strings);  // Compile error! String is not a Number

    System.out.println("""

        Upper bounded wildcard (? extends T):
        - Accepts T or any subtype of T
        - Can READ as type T
        - Cannot WRITE (except null) - don't know exact subtype
        - Use for "producer" parameters (data comes OUT)
        """);
  }

  // Can read elements as Number
  static double sumNumbers(List<? extends Number> numbers) {
    double sum = 0;
    for (Number n : numbers) {  // Safe to read as Number
      sum += n.doubleValue();
    }
    // numbers.add(1);  // Compile error! Cannot add - might be List<Double>
    return sum;
  }

  // ===== Lower Bounded Wildcard: ? super T =====

  static void lowerBoundedWildcard() {
    System.out.println("=== Lower Bounded Wildcard: ? super T ===");

    List<Object> objects = new ArrayList<>();
    List<Number> numbers = new ArrayList<>();
    List<Integer> integers = new ArrayList<>();

    // ? super Integer accepts List<Integer>, List<Number>, List<Object>
    addIntegers(objects);   // Object is supertype of Integer
    addIntegers(numbers);   // Number is supertype of Integer
    addIntegers(integers);  // Integer itself

    System.out.println("  Objects: " + objects);
    System.out.println("  Numbers: " + numbers);
    System.out.println("  Integers: " + integers);

    // List<Double> doubles = new ArrayList<>();
    // addIntegers(doubles);  // Compile error! Double is not supertype of Integer

    System.out.println("""

        Lower bounded wildcard (? super T):
        - Accepts T or any supertype of T
        - Can WRITE type T (and subtypes)
        - Can only READ as Object (don't know exact supertype)
        - Use for "consumer" parameters (data goes IN)
        """);
  }

  // Can write Integer values
  static void addIntegers(List<? super Integer> list) {
    list.add(1);      // Safe - Integer is compatible with ? super Integer
    list.add(2);
    list.add(3);
    // list.add(1.5);  // Compile error! Double is not Integer

    // Reading is limited
    // Integer i = list.get(0);  // Compile error! Might be List<Object>
    Object o = list.get(0);      // Only safe read is as Object
  }

  // ===== PECS: Producer Extends, Consumer Super =====

  static void pecsExplained() {
    System.out.println("=== PECS: Producer Extends, Consumer Super ===");

    List<Integer> source = List.of(1, 2, 3, 4, 5);
    List<Number> destination = new ArrayList<>();

    // Copy using PECS principle
    copyAll(source, destination);
    System.out.println("  After copy: " + destination);

    // This works because:
    // - source is a "producer" (we read FROM it) → use extends
    // - destination is a "consumer" (we write TO it) → use super

    System.out.println("""

        PECS Principle (Joshua Bloch, Effective Java):
        - Producer Extends: If parameter produces data → ? extends T
        - Consumer Super: If parameter consumes data → ? super T
        - If both read and write needed → don't use wildcard

        Examples:
        - Collections.copy(dest, src): dest is ? super T, src is ? extends T
        - Collections.max(coll): coll is ? extends T (reads to find max)
        - Collections.fill(list, obj): list is ? super T (writes obj)
        """);
  }

  // PECS in action: src produces, dest consumes
  static <T> void copyAll(List<? extends T> src, List<? super T> dest) {
    for (T item : src) {   // Read from producer
      dest.add(item);      // Write to consumer
    }
  }

  // ===== Wildcard Capture =====

  static void wildcardCapture() {
    System.out.println("=== Wildcard Capture ===");

    List<Integer> ints = new ArrayList<>(List.of(1, 2, 3));

    // Swap requires helper method due to wildcard capture
    System.out.println("  Before swap: " + ints);
    swap(ints, 0, 2);
    System.out.println("  After swap: " + ints);

    // Reverse using swap
    List<String> strings = new ArrayList<>(List.of("A", "B", "C", "D"));
    System.out.println("  Before reverse: " + strings);
    reverse(strings);
    System.out.println("  After reverse: " + strings);

    System.out.println("""

        Wildcard capture:
        - Compiler creates placeholder for ? type
        - Helper method with type parameter can "capture" the wildcard
        - Allows operations that need consistent type
        """);
  }

  // Public API uses wildcard
  static void swap(List<?> list, int i, int j) {
    swapHelper(list, i, j);  // Delegate to helper
  }

  // Helper captures the wildcard as concrete type T
  private static <T> void swapHelper(List<T> list, int i, int j) {
    T temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
  }

  static void reverse(List<?> list) {
    reverseHelper(list);
  }

  private static <T> void reverseHelper(List<T> list) {
    int size = list.size();
    for (int i = 0; i < size / 2; i++) {
      T temp = list.get(i);
      list.set(i, list.get(size - 1 - i));
      list.set(size - 1 - i, temp);
    }
  }

  // ===== Practical Examples =====

  static void practicalExamples() {
    System.out.println("=== Practical Examples ===");

    // 1. Find max in any Number list
    List<Integer> ages = List.of(25, 30, 22, 35, 28);
    List<Double> prices = List.of(19.99, 29.99, 9.99, 49.99);

    System.out.println("  Max age: " + findMax(ages));
    System.out.println("  Max price: " + findMax(prices));

    // 2. Filter with predicate
    List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    List<Integer> evens = new ArrayList<>();
    copyIf(numbers, evens, n -> n % 2 == 0);
    System.out.println("  Even numbers: " + evens);

    // 3. Type hierarchy example
    class Animal { String name = "Animal"; }
    class Dog extends Animal { { name = "Dog"; } }
    class Cat extends Animal { { name = "Cat"; } }

    List<Dog> dogs = new ArrayList<>();
    dogs.add(new Dog());

    List<Animal> animals = new ArrayList<>();
    // addAll uses ? extends, so List<Dog> works for List<? extends Animal>
    addAll(animals, dogs);
    animals.add(new Cat());

    System.out.print("  Animals: ");
    for (Animal a : animals) {
      System.out.print(a.name + " ");
    }
    System.out.println();

    System.out.println();
  }

  // Read-only: use extends
  static <T extends Comparable<? super T>> T findMax(List<? extends T> list) {
    if (list.isEmpty()) throw new IllegalArgumentException("Empty list");
    T max = list.getFirst();
    for (T item : list) {
      if (item.compareTo(max) > 0) {
        max = item;
      }
    }
    return max;
  }

  // PECS: source extends, dest super
  interface Predicate<T> {
    boolean test(T t);
  }

  static <T> void copyIf(List<? extends T> src, List<? super T> dest, Predicate<? super T> predicate) {
    for (T item : src) {
      if (predicate.test(item)) {
        dest.add(item);
      }
    }
  }

  static <T> void addAll(List<? super T> dest, List<? extends T> src) {
    for (T item : src) {
      dest.add(item);
    }
  }
}
