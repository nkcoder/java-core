package org.nkcoder.oop;

/**
 * Records (Java 16+): Immutable data carriers with minimal boilerplate.
 *
 * <ul>
 *   <li>Auto-generates: constructor, getters, equals, hashCode, toString
 *   <li>Fields are final and private
 *   <li>Cannot extend classes (implicitly extends Record)
 *   <li>Can implement interfaces
 * </ul>
 */
public class RecordExample {

    public static void main(String[] args) {
        basicRecord();
        recordMethods();
        compactConstructor();
        customConstructor();
        recordWithMethods();
    }

    // Basic record declaration
    record Point(int x, int y) {}

    static void basicRecord() {
        System.out.println("=== Basic Record ===");

        // Constructor is auto-generated
        Point p1 = new Point(3, 4);
        Point p2 = new Point(3, 4);
        Point p3 = new Point(5, 6);

        // Accessor methods (not getX(), just x())
        System.out.println("x: " + p1.x());
        System.out.println("y: " + p1.y());

        // toString is auto-generated
        System.out.println("toString: " + p1);

        // equals compares all components
        System.out.println("p1.equals(p2): " + p1.equals(p2)); // true
        System.out.println("p1.equals(p3): " + p1.equals(p3)); // false

        // hashCode is consistent with equals
        System.out.println("p1.hashCode == p2.hashCode: " + (p1.hashCode() == p2.hashCode()));

        System.out.println();
    }

    static void recordMethods() {
        System.out.println("=== Record Auto-Generated Methods ===");

        record Person(String name, int age) {}

        Person alice = new Person("Alice", 30);

        // What's auto-generated:
        System.out.println("1. Constructor: new Person(name, age)");
        System.out.println("2. Accessors: name() = " + alice.name() + ", age() = " + alice.age());
        System.out.println("3. toString(): " + alice.toString());
        System.out.println("4. equals(): " + alice.equals(new Person("Alice", 30)));
        System.out.println("5. hashCode(): " + alice.hashCode());

        // Records are implicitly final
        System.out.println("Records are final - cannot be extended");

        System.out.println();
    }

    // Compact constructor for validation
    record Email(String value) {
        // Compact constructor - no parameter list
        public Email {
            if (value == null || !value.contains("@")) {
                throw new IllegalArgumentException("Invalid email: " + value);
            }
            // Can reassign parameters (transforms before assignment)
            value = value.toLowerCase().trim();
        }
    }

    static void compactConstructor() {
        System.out.println("=== Compact Constructor (Validation) ===");

        Email email = new Email("  Alice@Example.COM  ");
        System.out.println("Normalized email: " + email.value());

        try {
            new Email("invalid");
        } catch (IllegalArgumentException e) {
            System.out.println("Validation failed: " + e.getMessage());
        }

        System.out.println();
    }

    // Multiple constructors
    record Rectangle(double width, double height) {
        // Compact constructor
        public Rectangle {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Dimensions must be positive");
            }
        }

        // Additional constructor must delegate to canonical
        public Rectangle(double side) {
            this(side, side); // Square
        }
    }

    static void customConstructor() {
        System.out.println("=== Custom Constructors ===");

        Rectangle rect = new Rectangle(3, 4);
        Rectangle square = new Rectangle(5); // Uses custom constructor

        System.out.println("Rectangle: " + rect);
        System.out.println("Square: " + square);

        System.out.println();
    }

    // Record with instance methods
    record Circle(double radius) {
        // Compact constructor
        public Circle {
            if (radius <= 0) {
                throw new IllegalArgumentException("Radius must be positive");
            }
        }

        // Instance methods
        public double area() {
            return Math.PI * radius * radius;
        }

        public double circumference() {
            return 2 * Math.PI * radius;
        }

        // Static methods
        public static Circle unit() {
            return new Circle(1);
        }

        // Static fields are allowed
        public static final Circle UNIT = new Circle(1);
    }

    static void recordWithMethods() {
        System.out.println("=== Record with Methods ===");

        Circle c = new Circle(5);
        System.out.println("Circle: " + c);
        System.out.println("Area: " + String.format("%.2f", c.area()));
        System.out.println("Circumference: " + String.format("%.2f", c.circumference()));

        Circle unit = Circle.unit();
        System.out.println("Unit circle: " + unit);
        System.out.println("Unit constant: " + Circle.UNIT);

        System.out.println("""

          Record limitations:
          - Cannot extend other classes
          - Fields are implicitly final (immutable)
          - Cannot declare instance fields (only components)
          - Cannot be abstract
          """);
    }
}
