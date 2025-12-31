package org.nkcoder.oop;

/**
 * Sealed classes (Java 17+): Restrict which classes can extend/implement.
 *
 * <ul>
 *   <li>{@code sealed}: declares restricted hierarchy
 *   <li>{@code permits}: lists allowed subclasses
 *   <li>Subclasses must be {@code final}, {@code sealed}, or {@code non-sealed}
 *   <li>Enables exhaustive pattern matching in switch
 * </ul>
 */
public class SealedClassExample {
    public static void main(String[] args) {
        SealedClassExample sealedClassExample = new SealedClassExample();
        sealedClassExample.basicSealedClass();
        sealedClassExample.sealedInterface();
        sealedClassExample.exhaustiveSwitch();
        sealedClassExample.realWorldExample();
    }

    // Basic sealed class
    abstract sealed class Shape permits Circle, Rectangle, Triangle {
        abstract double area();
    }

    final class Circle extends Shape {
        private final double radius;

        Circle(double radius) {
            this.radius = radius;
        }

        @Override
        double area() {
            return Math.PI * radius * radius;
        }
    }

    final class Rectangle extends Shape {
        private final double width, height;

        Rectangle(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        double area() {
            return width * height;
        }
    }

    final class Triangle extends Shape {
        private final double base, height;

        Triangle(double base, double height) {
            this.base = base;
            this.height = height;
        }

        @Override
        double area() {
            return 0.5 * base * height;
        }
    }

    // This would NOT compile - not in permit list
    //  class Pentagon extends Shape {}

    void basicSealedClass() {
        System.out.println("=== Basic Sealed Class ===");

        Shape circle = new Circle(0.5);
        Shape rectangle = new Rectangle(3, 4);
        Shape triangle = new Triangle(6, 4);

        System.out.println("Circle area: " + circle.area());
        System.out.println("Rectangle area: " + rectangle.area());
        System.out.println("Triangle area: " + triangle.area());

        System.out.println("""

          Sealed hierarchy:
          - Shape (sealed) permits Circle, Rectangle, Triangle
          - Circle (final) - cannot be extended
          - Rectangle (final) - cannot be extended
          - Triangle (final) - cannot be extended
          """);

        System.out.println();
    }

    // Sealed interface
    sealed interface Vehicle permits Car, Truck, Motorcycle {
        String describe();

        int wheels();
    }

    record Car(String model, int doors) implements Vehicle {
        @Override
        public String describe() {
            return model + " with " + doors + " doors";
        }

        @Override
        public int wheels() {
            return 4;
        }
    }

    record Truck(String model, double capacity) implements Vehicle {
        @Override
        public String describe() {
            return model + " truck, capacity: " + capacity + " tons";
        }

        @Override
        public int wheels() {
            return 6;
        }
    }

    record Motorcycle(String model) implements Vehicle {
        @Override
        public String describe() {
            return model + " motorcycle";
        }

        @Override
        public int wheels() {
            return 2;
        }
    }

    void sealedInterface() {
        System.out.println("=== Sealed Interface ===");

        Vehicle car = new Car("Toyota", 4);
        Vehicle truck = new Truck("Ford", 5.5);
        Vehicle motorcycle = new Motorcycle("Honda");

        System.out.println(car.describe() + " - " + car.wheels() + " wheels");
        System.out.println(truck.describe() + " - " + truck.wheels() + " wheels");
        System.out.println(motorcycle.describe() + " - " + motorcycle.wheels() + " wheels");

        System.out.println();
    }

    // Subclass modifiers: final, sealed, non-sealed
    sealed class Animal permits Dog, Cat, Bird {}

    final class Dog extends Animal {} // cannot be extended

    sealed class Cat extends Animal permits HouseCat, WildCat {} // Further restricted

    final class HouseCat extends Cat {}

    final class WildCat extends Cat {}

    non-sealed class Bird extends Animal {} // Open for extension

    class Parrot extends Bird {} // OK - Bird is non-sealed

    class Eagle extends Bird {} // OK - Bird is non-sealed

    void subclassModifiers() {
        System.out.println("\n=== Subclass Modifiers ===");

        System.out.println("""
          Three options for permitted subclasses:

          1. final - Stops the hierarchy
             final class Dog extends Animal {}

          2. sealed - Continues restricted hierarchy
             sealed class Cat extends Animal permits HouseCat, WildCat {}

          3. non-sealed - Opens up for any extension
             non-sealed class Bird extends Animal {}
             class Parrot extends Bird {}  // OK!
          """);

        Animal dog = new Dog();
        Animal houseCat = new HouseCat();
        Animal parrot = new Parrot();

        System.out.println("Dog: " + dog.getClass().getSimpleName());
        System.out.println("HouseCat: " + houseCat.getClass().getSimpleName());
        System.out.println("Parrot: " + parrot.getClass().getSimpleName());

        System.out.println();
    }

    // Exhaustive switch with sealed types
    String describeShape(Shape shape) {
        // Compiler knows all possible types - no default needed!
        return switch (shape) {
            case Circle c -> "Circle with radius " + c.radius;
            case Rectangle r -> "Rectangle " + r.width + "x" + r.height;
            case Triangle t -> "Triangle base=" + t.base + ", height=" + t.height;
            // No default needed - compiler verifies exhaustiveness
        };
    }

    int totalWheels(Vehicle vehicle) {
        return switch (vehicle) {
            case Car c -> c.wheels();
            case Truck t -> t.wheels();
            case Motorcycle m -> m.wheels();
        };
    }

    void exhaustiveSwitch() {
        System.out.println("=== Exhaustive Switch ===");

        Shape circle = new Circle(5);
        Shape rect = new Rectangle(3, 4);

        System.out.println(describeShape(circle));
        System.out.println(describeShape(rect));

        Vehicle car = new Car("Honda", 4);
        System.out.println("Total wheels: " + totalWheels(car));

        System.out.println("""

          Benefits of sealed + switch:
          - Compiler ensures all cases handled
          - No need for default case
          - Adding new subclass = compile error in all switches
          - Safer refactoring
          """);

        System.out.println();
    }

    // Real-world example: Result type
    sealed interface Result<T> permits Success, Failure {
        boolean isSuccess();
    }

    record Success<T>(T value) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    record Failure<T>(String error) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    <T> String handleResult(Result<T> result) {
        return switch (result) {
            case Success<T> s -> "Success: " + s.value();
            case Failure<T> f -> "Error: " + f.error();
        };
    }

    void realWorldExample() {
        System.out.println("=== Real-World Example: Result Type ===");

        Result<Integer> success = new Success<>(42);
        Result<Integer> failure = new Failure<>("Division by zero");

        System.out.println(handleResult(success));
        System.out.println(handleResult(failure));

        System.out.println("""

          Common sealed type patterns:
          - Result<T>: Success | Failure
          - Option<T>: Some | None
          - Tree<T>: Leaf | Branch
          - Expression: Literal | BinaryOp | UnaryOp
          - Event: Created | Updated | Deleted
          """);
        System.out.println();
    }
}
