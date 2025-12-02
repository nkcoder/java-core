package org.nkcoder.oop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Advanced record features.
 *
 * <ul>
 *   <li>Records can implement interfaces</li>
 *   <li>Generic records</li>
 *   <li>Nested records</li>
 *   <li>Records with collections (defensive copies)</li>
 *   <li>Records in serialization</li>
 * </ul>
 */
public class RecordAdvancedExample {

  public static void main(String[] args) {
    recordsWithInterfaces();
    genericRecords();
    nestedRecords();
    defensiveCopies();
    recordsAndCollections();
    localRecords();
    whenToUseRecords();
  }

  // Records implementing interfaces
  interface Printable {
    String format();
  }

  interface HasArea {
    double area();
  }

  record Square(double side) implements Printable, HasArea {
    @Override
    public String format() {
      return "Square[side=" + side + ", area=" + area() + "]";
    }

    @Override
    public double area() {
      return side * side;
    }
  }

  static void recordsWithInterfaces() {
    System.out.println("=== Records with Interfaces ===");

    Square sq = new Square(5);
    System.out.println("Default toString: " + sq);
    System.out.println("Custom format: " + sq.format());
    System.out.println("Area: " + sq.area());

    // Polymorphism works
    Printable p = sq;
    HasArea h = sq;
    System.out.println("As Printable: " + p.format());
    System.out.println("As HasArea: " + h.area());

    System.out.println();
  }

  // Generic records
  record Pair<T, U>(T first, U second) {
    public Pair<U, T> swap() {
      return new Pair<>(second, first);
    }
  }

  record Box<T>(T value) {
    public <U> Box<U> map(Function<T, U> mapper) {
      return new Box<>(mapper.apply(value));
    }
  }

  static void genericRecords() {
    System.out.println("=== Generic Records ===");

    Pair<String, Integer> pair = new Pair<>("age", 30);
    System.out.println("Pair: " + pair);
    System.out.println("Swapped: " + pair.swap());

    Box<String> stringBox = new Box<>("hello");
    Box<Integer> intBox = stringBox.map(String::length);
    System.out.println("String box: " + stringBox);
    System.out.println("Mapped to int: " + intBox);

    // Type inference with var
    var inferredPair = new Pair<>("key", List.of(1, 2, 3));
    System.out.println("Inferred: " + inferredPair);

    System.out.println();
  }

  // Nested records
  record Address(String street, String city, String zipCode) {}

  record Employee(String name, Address address, double salary) {
    // Nested record inside record
    record Summary(String name, String city) {}

    public Summary summary() {
      return new Summary(name, address.city());
    }
  }

  static void nestedRecords() {
    System.out.println("=== Nested Records ===");

    Address addr = new Address("123 Main St", "Boston", "02101");
    Employee emp = new Employee("Alice", addr, 75000);

    System.out.println("Employee: " + emp);
    System.out.println("Address: " + emp.address());
    System.out.println("City: " + emp.address().city());
    System.out.println("Summary: " + emp.summary());

    System.out.println();
  }

  // Defensive copies for mutable components
  record Team(String name, List<String> members) {
    // Compact constructor with defensive copy
    public Team {
      // Create unmodifiable copy to ensure immutability
      members = List.copyOf(members);
    }
  }

  static void defensiveCopies() {
    System.out.println("=== Defensive Copies ===");

    var mutableList = new ArrayList<>(List.of("Alice", "Bob"));
    Team team = new Team("Engineering", mutableList);

    // Original list modification doesn't affect record
    mutableList.add("Charlie");
    System.out.println("Original list: " + mutableList);
    System.out.println("Team members: " + team.members());

    // Record's list is unmodifiable
    try {
      team.members().add("David");
    } catch (UnsupportedOperationException e) {
      System.out.println("Cannot modify team members: immutable");
    }

    System.out.println("""

          Important: Always use defensive copies for mutable components!
          - List.copyOf() for lists
          - Set.copyOf() for sets
          - Map.copyOf() for maps
          """);

    System.out.println();
  }

  // Records with Comparable and Comparator
  record Product(String name, double price) implements Comparable<Product> {
    @Override
    public int compareTo(Product other) {
      return Double.compare(this.price, other.price);
    }

    // Static factory methods
    public static Comparator<Product> byName() {
      return Comparator.comparing(Product::name);
    }

    public static Comparator<Product> byPriceDesc() {
      return Comparator.comparing(Product::price).reversed();
    }
  }

  static void recordsAndCollections() {
    System.out.println("=== Records in Collections ===");

    List<Product> products = List.of(
        new Product("Apple", 1.50),
        new Product("Banana", 0.75),
        new Product("Cherry", 3.00)
    );

    // Natural ordering (by price, from Comparable)
    var byPrice = products.stream().sorted().toList();
    System.out.println("By price: " + byPrice);

    // Custom comparator
    var byName = products.stream().sorted(Product.byName()).toList();
    System.out.println("By name: " + byName);

    var byPriceDesc = products.stream().sorted(Product.byPriceDesc()).toList();
    System.out.println("By price desc: " + byPriceDesc);

    // Records work well as Map keys (proper equals/hashCode)
    var inventory = new HashMap<Product, Integer>();
    inventory.put(new Product("Apple", 1.50), 100);
    inventory.put(new Product("Banana", 0.75), 200);

    // Lookup works because equals/hashCode are correct
    int appleCount = inventory.get(new Product("Apple", 1.50));
    System.out.println("Apple count: " + appleCount);

    System.out.println();
  }

  static void localRecords() {
    System.out.println("=== Local Records ===");

    // Records can be declared locally in methods
    record Stats(int count, double sum, double average) {}

    List<Integer> numbers = List.of(1, 2, 3, 4, 5);

    Stats stats = new Stats(
        numbers.size(),
        numbers.stream().mapToInt(i -> i).sum(),
        numbers.stream().mapToInt(i -> i).average().orElse(0)
    );

    System.out.println("Stats: " + stats);
    System.out.println("Count: " + stats.count());
    System.out.println("Average: " + stats.average());

    // Useful for grouping related data in stream operations
    record NameLength(String name, int length) {}

    var nameLengths = List.of("Alice", "Bob", "Charlie").stream()
        .map(n -> new NameLength(n, n.length()))
        .filter(nl -> nl.length() > 3)
        .toList();
    System.out.println("Names > 3 chars: " + nameLengths);

    System.out.println();
  }

  static void whenToUseRecords() {
    System.out.println("=== When to Use Records ===");

    System.out.println("""
          USE records for:
          - DTOs (Data Transfer Objects)
          - Value objects (immutable data holders)
          - Compound map keys
          - Multiple return values
          - Pattern matching (with sealed types)
          - Simple domain objects

          DON'T use records for:
          - Entities with identity (use classes)
          - Mutable objects
          - Objects with complex behavior
          - When you need inheritance
          - JPA/Hibernate entities (no no-arg constructor)

          Record vs Class:
          - Record: data-focused, immutable, auto-generated methods
          - Class: behavior-focused, mutable state, manual implementation
          """);
  }
}