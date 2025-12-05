package org.nkcoder.oop;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Inheritance and Composition in Java.
 *
 * <ul>
 *   <li>extends: Single inheritance of classes</li>
 *   <li>implements: Multiple inheritance of interfaces</li>
 *   <li>Composition over inheritance: Favor "has-a" over "is-a"</li>
 *   <li>When to use each approach</li>
 * </ul>
 */
public class InheritanceExample {

  public static void main(String[] args) {
    extendsVsImplements();
    methodOverriding();
    inheritanceProblems();
    compositionSolution();
    whenToUseWhich();
  }

  // ============ Extends vs Implements ============

  // Single inheritance with extends
  static class Animal {
    protected String name;

    Animal(String name) {
      this.name = name;
    }

    void eat() {
      System.out.println(name + " is eating");
    }

    void sleep() {
      System.out.println(name + " is sleeping");
    }
  }

  static class Dog extends Animal {
    Dog(String name) {
      super(name);  // Must call parent constructor
    }

    void bark() {
      System.out.println(name + " says: Woof!");
    }

    // Can only extend ONE class
    // static class Labrador extends Dog, Animal {} // ERROR!
  }

  // Multiple inheritance with implements
  interface Runnable {
    void run();
  }

  interface Swimmable {
    void swim();
  }

  interface Fetchable {
    void fetch();
  }

  // Can implement MULTIPLE interfaces
  static class Labrador extends Dog implements Runnable, Swimmable, Fetchable {
    Labrador(String name) {
      super(name);
    }

    @Override
    public void run() {
      System.out.println(name + " is running fast!");
    }

    @Override
    public void swim() {
      System.out.println(name + " is swimming!");
    }

    @Override
    public void fetch() {
      System.out.println(name + " fetched the ball!");
    }
  }

  static void extendsVsImplements() {
    System.out.println("=== Extends vs Implements ===");

    Labrador lab = new Labrador("Max");
    lab.eat();      // From Animal
    lab.bark();     // From Dog
    lab.run();      // From Runnable
    lab.swim();     // From Swimmable
    lab.fetch();    // From Fetchable

    // Polymorphism
    Animal animal = lab;
    Runnable runner = lab;
    Swimmable swimmer = lab;

    System.out.println("""

          extends: Single class inheritance
          - Inherits state (fields) and behavior (methods)
          - Can only extend ONE class
          - Creates tight coupling

          implements: Multiple interface inheritance
          - Inherits only behavior contracts
          - Can implement MANY interfaces
          - More flexible, loose coupling
          """);
  }

  // ============ Method Overriding ============

  static class Vehicle {
    protected String brand;

    Vehicle(String brand) {
      this.brand = brand;
    }

    void start() {
      System.out.println(brand + " starting...");
    }

    // Can be overridden
    String describe() {
      return "A vehicle by " + brand;
    }

    // Cannot be overridden
    final void honk() {
      System.out.println("Beep beep!");
    }
  }

  static class Car extends Vehicle {
    private final int doors;

    Car(String brand, int doors) {
      super(brand);
      this.doors = doors;
    }

    @Override  // Always use @Override annotation!
    void start() {
      System.out.println("Checking seatbelt...");
      super.start();  // Call parent implementation
      System.out.println("Engine running!");
    }

    @Override
    String describe() {
      // Covariant return: can return more specific type
      return super.describe() + " with " + doors + " doors";
    }

    // This would be an error:
    // @Override void honk() {} // Cannot override final method
  }

  static class ElectricCar extends Car {
    private final int batteryPercent;

    ElectricCar(String brand, int doors, int batteryPercent) {
      super(brand, doors);
      this.batteryPercent = batteryPercent;
    }

    @Override
    void start() {
      System.out.println("Battery at " + batteryPercent + "%");
      super.start();  // Calls Car.start() which calls Vehicle.start()
    }
  }

  static void methodOverriding() {
    System.out.println("=== Method Overriding ===");

    Vehicle v = new Vehicle("Generic");
    v.start();
    System.out.println();

    Car car = new Car("Toyota", 4);
    car.start();
    System.out.println("Description: " + car.describe());
    System.out.println();

    ElectricCar tesla = new ElectricCar("Tesla", 4, 85);
    tesla.start();

    System.out.println("""

          Method overriding rules:
          - Use @Override annotation (catches errors)
          - Same or more accessible visibility
          - Same or covariant return type
          - Cannot override final methods
          - super.method() calls parent version
          """);
  }

  // ============ Inheritance Problems ============

  // Problem 1: Fragile Base Class
  static class CountingSet<E> extends HashSet<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) {
      addCount++;
      return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      addCount += c.size();
      return super.addAll(c);  // BUG: HashSet.addAll() calls add() internally!
    }

    public int getAddCount() {
      return addCount;
    }
  }

  // Problem 2: Tight Coupling
  static class ReportGenerator {
    void generateHeader() {
      System.out.println("=== Report ===");
    }

    void generateBody() {
      System.out.println("Report content here");
    }

    void generateFooter() {
      System.out.println("=== End ===");
    }

    // Template method
    void generate() {
      generateHeader();
      generateBody();
      generateFooter();
    }
  }

  // Any change to ReportGenerator can break subclasses
  static class SalesReport extends ReportGenerator {
    @Override
    void generateBody() {
      System.out.println("Sales: $10,000");
      System.out.println("Units: 500");
    }
  }

  static void inheritanceProblems() {
    System.out.println("=== Inheritance Problems ===");

    // Fragile base class problem
    CountingSet<String> set = new CountingSet<>();
    set.addAll(List.of("a", "b", "c"));
    System.out.println("Added 3 items, count = " + set.getAddCount());
    // Expected: 3, Actual: 6 (because HashSet.addAll calls add internally)

    System.out.println("""

          Inheritance problems:
          1. Fragile Base Class: Parent changes break children
          2. Tight Coupling: Children depend on parent internals
          3. Breaks Encapsulation: Must know parent implementation
          4. Inflexible: Locked into hierarchy at compile time
          """);
  }

  // ============ Composition Solution ============

  // Composition: "has-a" instead of "is-a"
  interface Printer {
    void print(String content);
  }

  static class ConsolePrinter implements Printer {
    @Override
    public void print(String content) {
      System.out.println(content);
    }
  }

  static class FilePrinter implements Printer {
    @Override
    public void print(String content) {
      System.out.println("[Writing to file]: " + content);
    }
  }

  interface ReportContent {
    String getHeader();
    String getBody();
    String getFooter();
  }

  static class SalesReportContent implements ReportContent {
    @Override
    public String getHeader() {
      return "=== Sales Report ===";
    }

    @Override
    public String getBody() {
      return "Sales: $10,000\nUnits: 500";
    }

    @Override
    public String getFooter() {
      return "=== End of Sales Report ===";
    }
  }

  // Composed report generator - flexible and testable
  static class ComposedReportGenerator {
    private final ReportContent content;
    private final Printer printer;

    // Inject dependencies via constructor
    ComposedReportGenerator(ReportContent content, Printer printer) {
      this.content = content;
      this.printer = printer;
    }

    void generate() {
      printer.print(content.getHeader());
      printer.print(content.getBody());
      printer.print(content.getFooter());
    }
  }

  // Composition-based set wrapper (correct counting)
  static class CountingSetWrapper<E> {
    private final Set<E> set = new HashSet<>();
    private int addCount = 0;

    public void add(E e) {
      addCount++;
      set.add(e);
    }

    public void addAll(Collection<? extends E> c) {
      addCount += c.size();
      set.addAll(c);  // No double counting - we control the behavior
    }

    public int getAddCount() {
      return addCount;
    }

    public Set<E> getSet() {
      return Set.copyOf(set);
    }
  }

  static void compositionSolution() {
    System.out.println("=== Composition Solution ===");

    // Flexible: can swap implementations
    ReportContent salesContent = new SalesReportContent();
    Printer consolePrinter = new ConsolePrinter();
    Printer filePrinter = new FilePrinter();

    System.out.println("Print to console:");
    new ComposedReportGenerator(salesContent, consolePrinter).generate();

    System.out.println("\nPrint to file:");
    new ComposedReportGenerator(salesContent, filePrinter).generate();

    // Correct counting
    System.out.println("\nCounting wrapper:");
    CountingSetWrapper<String> wrapper = new CountingSetWrapper<>();
    wrapper.addAll(List.of("a", "b", "c"));
    System.out.println("Added 3 items, count = " + wrapper.getAddCount());  // Correct: 3

    System.out.println("""

          Composition benefits:
          - Loose coupling: Components are independent
          - Flexible: Swap implementations at runtime
          - Testable: Easy to mock dependencies
          - Encapsulated: No internal dependency on parent
          """);
  }

  // ============ When to Use Which ============

  static void whenToUseWhich() {
    System.out.println("=== When to Use Which ===");

    System.out.println("""
          USE INHERITANCE when:
          - True "is-a" relationship (Dog is-a Animal)
          - Subclass is a specialized version of parent
          - You control both parent and child classes
          - Reusing implementation, not just interface
          - Framework requires it (e.g., extending Thread)

          USE COMPOSITION when:
          - "Has-a" relationship (Car has-a Engine)
          - Need runtime flexibility
          - Combining behaviors from multiple sources
          - Parent class might change
          - You don't control the parent class

          PREFER COMPOSITION because:
          - More flexible (runtime vs compile-time)
          - Better encapsulation
          - Easier to test
          - Avoids fragile base class problem

          Rule of thumb:
          ┌────────────────────────────────────────────────┐
          │ "Favor composition over inheritance"          │
          │                    - Effective Java, Item 18  │
          └────────────────────────────────────────────────┘

          Decision tree:
          1. Is it a true "is-a" relationship? → Maybe inheritance
          2. Will you override many methods? → Consider composition
          3. Need multiple behaviors? → Composition + interfaces
          4. Is the parent class stable? → Inheritance might be OK
          5. Uncertain? → Start with composition
          """);
  }
}