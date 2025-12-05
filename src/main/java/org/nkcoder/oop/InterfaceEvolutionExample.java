package org.nkcoder.oop;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Interface Evolution (Java 8+): Modern interface features.
 *
 * <ul>
 *   <li>Default methods (Java 8): Add behavior without breaking implementations</li>
 *   <li>Static methods (Java 8): Utility methods tied to the interface</li>
 *   <li>Private methods (Java 9): Helper methods for default method code reuse</li>
 *   <li>Enables multiple inheritance of behavior (not state)</li>
 * </ul>
 */
public class InterfaceEvolutionExample {

  static void main(String[] args) {
    defaultMethods();
    staticMethods();
    privateMethods();
    diamondProblem();
    interfaceVsAbstractClass();
  }

  // ============ Default Methods (Java 8+) ============

  interface Collection<T> {
    void add(T item);
    int size();

    // Default method - provides implementation
    default boolean isEmpty() {
      return size() == 0;
    }

    // Can be overridden by implementing classes
    default void addAll(List<T> items) {
      for (T item : items) {
        add(item);
      }
    }
  }

  static class SimpleList<T> implements Collection<T> {
    private final ArrayList<T> items = new ArrayList<>();

    @Override
    public void add(T item) {
      items.add(item);
    }

    @Override
    public int size() {
      return items.size();
    }

    // isEmpty() inherited from interface - no need to implement!
    // addAll() also inherited
  }

  static void defaultMethods() {
    System.out.println("=== Default Methods ===");

    SimpleList<String> list = new SimpleList<>();
    System.out.println("isEmpty: " + list.isEmpty());  // Uses default method

    list.add("Hello");
    list.addAll(List.of("World", "!"));  // Uses default method
    System.out.println("size: " + list.size());
    System.out.println("isEmpty: " + list.isEmpty());

    System.out.println("""

          Default methods allow:
          - Adding new methods to interfaces without breaking existing code
          - Providing common implementation shared by all implementors
          - Enabling interface evolution (e.g., Collection.stream())
          """);
  }

  // ============ Static Methods (Java 8+) ============

  interface Validator<T> {
    boolean validate(T value);

    // Static factory methods
    static <T> Validator<T> notNull() {
      return Objects::nonNull;
    }

    static Validator<String> notEmpty() {
      return value -> value != null && !value.isEmpty();
    }

    static Validator<String> minLength(int min) {
      return value -> value != null && value.length() >= min;
    }

    // Combining validators
    static <T> Validator<T> and(Validator<T> v1, Validator<T> v2) {
      return value -> v1.validate(value) && v2.validate(value);
    }
  }

  static void staticMethods() {
    System.out.println("=== Static Methods ===");

    // Use static factory methods
    Validator<String> nonNull = Validator.notNull();
    Validator<String> notEmpty = Validator.notEmpty();
    Validator<String> minLength = Validator.minLength(3);
    Validator<String> combined = Validator.and(notEmpty, minLength);

    System.out.println("nonNull.validate(null): " + nonNull.validate(null));
    System.out.println("nonNull.validate(\"\"): " + nonNull.validate(""));
    System.out.println("notEmpty.validate(\"\"): " + notEmpty.validate(""));
    System.out.println("notEmpty.validate(\"hi\"): " + notEmpty.validate("hi"));
    System.out.println("minLength.validate(\"hi\"): " + minLength.validate("hi"));
    System.out.println("combined.validate(\"hello\"): " + combined.validate("hello"));

    System.out.println("""

          Static interface methods are useful for:
          - Factory methods (e.g., List.of(), Map.of())
          - Utility methods related to the interface
          - Keeping helper methods with the interface they support
          """);
  }

  // ============ Private Methods (Java 9+) ============

  interface Logger {
    void log(String message);

    default void logInfo(String message) {
      log(formatMessage("INFO", message));
    }

    default void logWarn(String message) {
      log(formatMessage("WARN", message));
    }

    default void logError(String message) {
      log(formatMessage("ERROR", message));
    }

    // Private method - reusable helper for default methods
    private String formatMessage(String level, String message) {
      return "[" + level + "] " + LocalTime.now() + " - " + message;
    }

    // Private static method
    private static String sanitize(String input) {
      return input.replaceAll("[\n\r]", " ");
    }
  }

  static class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
      System.out.println(message);
    }
  }

  static void privateMethods() {
    System.out.println("=== Private Methods (Java 9+) ===");

    Logger logger = new ConsoleLogger();
    logger.logInfo("Application started");
    logger.logWarn("Low memory");
    logger.logError("Connection failed");
    logger.logInfo(Logger.sanitize("Hello world ! "));

    System.out.println("""

          Private interface methods (Java 9+):
          - Reduce code duplication in default methods
          - Cannot be inherited or overridden
          - Can be static or instance
          - Keep implementation details hidden
          """);
  }

  // ============ Diamond Problem ============
  interface Flyable {
    default String move() {
      return "flying";
    }
  }

  interface Swimmable {
    default String move() {
      return "swimming";
    }
  }

  // Must resolve conflict when both interfaces have same default method
  static class Duck implements Flyable, Swimmable {
    @Override
    public String move() {
      // Option 1: Provide own implementation
      return "flying and swimming";

      // Option 2: Choose one interface's implementation
      // return Flyable.super.move();

      // Option 3: Combine both
      // return Flyable.super.move() + " and " + Swimmable.super.move();
    }
  }

  interface Walkable {
    default String move() {
      return "walking";
    }
  }

  // No conflict: interface extends another with same method
  interface FlyingBird extends Flyable {
    @Override
    default String move() {
      return "flying gracefully";
    }
  }

  static class Eagle implements FlyingBird, Walkable {
    // FlyingBird is more specific than Walkable, so its move() wins
    // But here both have move(), so we must still resolve
    @Override
    public String move() {
      return FlyingBird.super.move();
    }
  }

  static void diamondProblem() {
    System.out.println("=== Diamond Problem Resolution ===");

    Duck duck = new Duck();
    System.out.println("Duck moves by: " + duck.move());

    Eagle eagle = new Eagle();
    System.out.println("Eagle moves by: " + eagle.move());

    System.out.println("""

          Resolution rules:
          1. Class method wins over interface default
          2. More specific interface wins (sub-interface over parent)
          3. If ambiguous, class MUST override and resolve
          4. Use InterfaceName.super.method() to call specific default
          """);
  }

  // ============ Interface vs Abstract Class ============
  // Abstract class: shared state + behavior
  abstract static class AbstractRepository<T> {
    protected final Map<Long, T> storage = new HashMap<>();
    protected long nextId = 1;

    // Can have constructors
    protected AbstractRepository() {
      System.out.println("Repository initialized");
    }

    // Can have instance fields (state)
    public long save(T entity) {
      long id = nextId++;
      storage.put(id, entity);
      return id;
    }

    // Abstract method
    public abstract T findById(long id);

    // Concrete method using state
    public int count() {
      return storage.size();
    }
  }

  // Interface: just behavior contract
  interface Repository<T> {
    long save(T entity);
    T findById(long id);

    // Default behavior (no state access)
    default boolean exists(long id) {
      return findById(id) != null;
    }

    // Static factory
    static <T> Repository<T> inMemory() {
      return new Repository<>() {
        private final Map<Long, T> storage = new HashMap<>();
        private long nextId = 1;

        @Override
        public long save(T entity) {
          long id = nextId++;
          storage.put(id, entity);
          return id;
        }

        @Override
        public T findById(long id) {
          return storage.get(id);
        }
      };
    }
  }

  static void interfaceVsAbstractClass() {
    System.out.println("=== Interface vs Abstract Class ===");

    System.out.println("""
          Use INTERFACE when:
          - Defining a contract/capability (Comparable, Serializable)
          - Multiple inheritance of behavior needed
          - Unrelated classes share behavior
          - API design (program to interface)

          Use ABSTRACT CLASS when:
          - Sharing state (fields) among subclasses
          - Need constructors with parameters
          - Closely related classes share implementation
          - Template method pattern

          Key differences:
          ┌─────────────────┬──────────────────┬──────────────────┐
          │ Feature         │ Interface        │ Abstract Class   │
          ├─────────────────┼──────────────────┼──────────────────┤
          │ Multiple inherit│ Yes              │ No               │
          │ Instance fields │ No (constants)   │ Yes              │
          │ Constructors    │ No               │ Yes              │
          │ Access modifiers│ public (default) │ Any              │
          │ State           │ No               │ Yes              │
          └─────────────────┴──────────────────┴──────────────────┘

          Modern approach: Prefer interfaces with default methods
          Use abstract classes only when you need shared state
          """);
  }
}
