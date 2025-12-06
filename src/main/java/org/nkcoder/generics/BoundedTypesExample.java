package org.nkcoder.generics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bounded Types: Restricting type parameters with extends and super.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, unchanged. Bounded types
 * allow you to restrict what types can be used as type arguments.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Upper bounds: T extends Type</li>
 *   <li>Multiple bounds: T extends A & B & C</li>
 *   <li>Recursive bounds: T extends Comparable&lt;T&gt;</li>
 *   <li>Bounded wildcards vs bounded type parameters</li>
 * </ul>
 */
public class BoundedTypesExample {

  static void main(String[] args) {
    upperBounds();
    multipleBounds();
    recursiveBounds();
    comparablePattern();
    boundedVsWildcard();
    realWorldExamples();
  }

  // ===== Upper Bounds: T extends Type =====

  // Only accepts Number or its subtypes
  static class NumericBox<T extends Number> {
    private T value;

    public NumericBox(T value) {
      this.value = value;
    }

    public T getValue() {
      return value;
    }

    // Can call Number methods on T
    public double doubleValue() {
      return value.doubleValue();
    }

    public int intValue() {
      return value.intValue();
    }
  }

  static void upperBounds() {
    System.out.println("=== Upper Bounds: T extends Type ===");

    NumericBox<Integer> intBox = new NumericBox<>(42);
    NumericBox<Double> doubleBox = new NumericBox<>(3.14);
    NumericBox<Long> longBox = new NumericBox<>(100L);

    // NumericBox<String> stringBox = new NumericBox<>("hello");  // Compile error!

    System.out.println("  Integer box: " + intBox.getValue() + " as double: " + intBox.doubleValue());
    System.out.println("  Double box: " + doubleBox.getValue() + " as int: " + doubleBox.intValue());
    System.out.println("  Long box: " + longBox.getValue());

    // Generic method with bound
    System.out.println("  Sum: " + sum(List.of(1, 2, 3, 4, 5)));
    System.out.println("  Sum: " + sum(List.of(1.5, 2.5, 3.0)));

    System.out.println("""

        Upper bounds (T extends Type):
        - Restricts T to Type or its subtypes
        - Allows calling methods defined in Type
        - After erasure, T becomes Type (not Object)
        """);
  }

  static <T extends Number> double sum(List<T> numbers) {
    double total = 0;
    for (T num : numbers) {
      total += num.doubleValue();
    }
    return total;
  }

  // ===== Multiple Bounds: T extends A & B & C =====

  interface Printable {
    void print();
  }

  interface Loggable {
    void log();
  }

  static class Document implements Printable, Loggable, Comparable<Document> {
    private final String content;

    public Document(String content) {
      this.content = content;
    }

    @Override
    public void print() {
      System.out.println("    Printing: " + content);
    }

    @Override
    public void log() {
      System.out.println("    Logged: " + content);
    }

    @Override
    public int compareTo(Document other) {
      return content.compareTo(other.content);
    }

    @Override
    public String toString() {
      return "Document(" + content + ")";
    }
  }

  // Multiple bounds: first must be class (if present), rest interfaces
  static <T extends Printable & Loggable> void printAndLog(T item) {
    item.print();
    item.log();
  }

  // Class bound must come first
  static class MultiBoundBox<T extends Number & Comparable<T>> {
    private T value;

    public MultiBoundBox(T value) {
      this.value = value;
    }

    public boolean isGreaterThan(T other) {
      return value.compareTo(other) > 0;
    }

    public double getDoubleValue() {
      return value.doubleValue();
    }
  }

  static void multipleBounds() {
    System.out.println("=== Multiple Bounds: T extends A & B & C ===");

    Document doc = new Document("Hello, World");
    printAndLog(doc);

    // MultiBoundBox works with Integer (Number + Comparable<Integer>)
    MultiBoundBox<Integer> intBox = new MultiBoundBox<>(10);
    System.out.println("  10 > 5? " + intBox.isGreaterThan(5));
    System.out.println("  10 as double: " + intBox.getDoubleValue());

    MultiBoundBox<Double> doubleBox = new MultiBoundBox<>(3.14);
    System.out.println("  3.14 > 2.0? " + doubleBox.isGreaterThan(2.0));

    System.out.println("""

        Multiple bounds rules:
        - Use & to separate bounds
        - At most one class bound (must be first)
        - Can have multiple interface bounds
        - T has access to all bound methods
        """);
  }

  // ===== Recursive Bounds: T extends Comparable<T> =====

  static <T extends Comparable<T>> T findMax(List<T> list) {
    if (list.isEmpty()) throw new IllegalArgumentException("Empty list");
    T max = list.getFirst();
    for (T item : list) {
      if (item.compareTo(max) > 0) {
        max = item;
      }
    }
    return max;
  }

  static <T extends Comparable<T>> T findMin(List<T> list) {
    if (list.isEmpty()) throw new IllegalArgumentException("Empty list");
    T min = list.getFirst();
    for (T item : list) {
      if (item.compareTo(min) < 0) {
        min = item;
      }
    }
    return min;
  }

  // Self-referential generic class
  abstract static class SelfComparable<T extends SelfComparable<T>> {
    public abstract int compareTo(T other);

    public boolean isGreaterThan(T other) {
      return compareTo(other) > 0;
    }
  }

  static class Score extends SelfComparable<Score> {
    private final int value;

    public Score(int value) {
      this.value = value;
    }

    @Override
    public int compareTo(Score other) {
      return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
      return "Score(" + value + ")";
    }
  }

  static void recursiveBounds() {
    System.out.println("=== Recursive Bounds: T extends Comparable<T> ===");

    List<Integer> numbers = List.of(3, 1, 4, 1, 5, 9, 2, 6);
    System.out.println("  Numbers: " + numbers);
    System.out.println("  Max: " + findMax(numbers));
    System.out.println("  Min: " + findMin(numbers));

    List<String> words = List.of("banana", "apple", "cherry");
    System.out.println("  Words: " + words);
    System.out.println("  Max: " + findMax(words));

    Score s1 = new Score(100);
    Score s2 = new Score(80);
    System.out.println("  " + s1 + " > " + s2 + "? " + s1.isGreaterThan(s2));

    System.out.println("""

        Recursive bounds pattern:
        - T extends Comparable<T> ensures T can compare to itself
        - Used for self-referential type safety
        - Common in fluent APIs and builder patterns
        """);
  }

  // ===== The Comparable Pattern =====

  // More flexible: allows comparing with supertypes
  static <T extends Comparable<? super T>> T findMaxFlexible(List<? extends T> list) {
    if (list.isEmpty()) throw new IllegalArgumentException("Empty list");
    T max = list.getFirst();
    for (T item : list) {
      if (item.compareTo(max) > 0) {
        max = item;
      }
    }
    return max;
  }

  static class Animal implements Comparable<Animal> {
    protected final String name;
    protected final int age;

    public Animal(String name, int age) {
      this.name = name;
      this.age = age;
    }

    @Override
    public int compareTo(Animal other) {
      return Integer.compare(age, other.age);
    }

    @Override
    public String toString() {
      return name + "(" + age + ")";
    }
  }

  static class Dog extends Animal {
    public Dog(String name, int age) {
      super(name, age);
    }
  }

  static void comparablePattern() {
    System.out.println("=== The Comparable Pattern ===");

    List<Dog> dogs = List.of(
        new Dog("Max", 5),
        new Dog("Buddy", 3),
        new Dog("Charlie", 7)
    );

    // Dog doesn't implement Comparable<Dog>, but Animal implements Comparable<Animal>
    // With ? super T, this still works!
    Dog oldestDog = findMaxFlexible(dogs);
    System.out.println("  Dogs: " + dogs);
    System.out.println("  Oldest: " + oldestDog);

    // findMax(dogs) would fail - Dog doesn't implement Comparable<Dog>

    System.out.println("""

        Best practice for Comparable:
        - Use: T extends Comparable<? super T>
        - Allows subclasses to inherit comparability
        - This is how Collections.max() is declared
        """);
  }

  // ===== Bounded Type Parameters vs Bounded Wildcards =====

  // Type parameter - establishes relationship
  static <T> void copyExact(List<T> dest, List<T> src) {
    dest.addAll(src);
  }

  // Wildcards - more flexible
  static <T> void copyFlexible(List<? super T> dest, List<? extends T> src) {
    dest.addAll(src);
  }

  static void boundedVsWildcard() {
    System.out.println("=== Bounded Type Parameters vs Wildcards ===");

    List<Number> numbers = new ArrayList<>();
    List<Integer> integers = List.of(1, 2, 3);

    // copyExact(numbers, integers);  // Compile error! List<Number> != List<Integer>
    copyFlexible(numbers, integers);   // Works! Integer extends Number

    System.out.println("  After flexible copy: " + numbers);

    System.out.println("""

        When to use which:
        - Type parameter <T>: Need same type in multiple places
        - Wildcard <?>: Maximum flexibility, each occurrence independent
        - PECS: ? extends for producers, ? super for consumers
        """);
  }

  // ===== Real-World Examples =====

  // Builder pattern with recursive bounds
  abstract static class Builder<T extends Builder<T>> {
    protected String name;
    protected int value;

    @SuppressWarnings("unchecked")
    protected T self() {
      return (T) this;
    }

    public T name(String name) {
      this.name = name;
      return self();
    }

    public T value(int value) {
      this.value = value;
      return self();
    }
  }

  static class ConcreteBuilder extends Builder<ConcreteBuilder> {
    private String extra;

    public ConcreteBuilder extra(String extra) {
      this.extra = extra;
      return this;
    }

    public String build() {
      return name + ":" + value + ":" + extra;
    }
  }

  // Repository pattern with bounds
  interface Entity<ID extends Serializable> {
    ID getId();
  }

  record User(Long id, String name) implements Entity<Long> {
    @Override
    public Long getId() { return id; }
  }

  static class Repository<E extends Entity<ID>, ID extends Serializable> {
    private final List<E> entities = new ArrayList<>();

    public void save(E entity) {
      entities.add(entity);
    }

    public E findById(ID id) {
      return entities.stream()
          .filter(e -> e.getId().equals(id))
          .findFirst()
          .orElse(null);
    }
  }

  static void realWorldExamples() {
    System.out.println("=== Real-World Examples ===");

    // Builder with recursive bounds - fluent API works!
    String result = new ConcreteBuilder()
        .name("test")
        .value(42)
        .extra("additional")
        .build();
    System.out.println("  Builder result: " + result);

    // Repository pattern
    Repository<User, Long> userRepo = new Repository<>();
    userRepo.save(new User(1L, "Alice"));
    userRepo.save(new User(2L, "Bob"));

    User found = userRepo.findById(1L);
    System.out.println("  Found user: " + found);

    System.out.println("""

        Common bounded type patterns:
        - Builder<T extends Builder<T>>: Fluent builders
        - Entity<ID extends Serializable>: Type-safe IDs
        - Enum<E extends Enum<E>>: Java's enum declaration
        - Comparable<T>: Self-comparable types
        """);
  }
}
