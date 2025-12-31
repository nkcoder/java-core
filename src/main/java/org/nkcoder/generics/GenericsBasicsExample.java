package org.nkcoder.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * Generics Basics (Java 5+): Type-safe generic classes and methods.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, unchanged. Generics enable compile-time type safety and
 * eliminate the need for casting.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Generic classes with type parameters
 *   <li>Type safety at compile time
 *   <li>Elimination of casts
 *   <li>Generic interfaces and inheritance
 * </ul>
 */
public class GenericsBasicsExample {

    static void main(String[] args) {
        beforeGenerics();
        withGenerics();
        genericClasses();
        genericInterfaces();
        multipleTypeParameters();
        diamondOperator();
    }

    // ===== Before Generics (Raw Types) =====

    static void beforeGenerics() {
        System.out.println("=== Before Generics (Raw Types) ===");

        // Without generics - no type safety
        @SuppressWarnings("rawtypes")
        List rawList = new ArrayList();

        rawList.add("Hello");
        rawList.add(42); // No compile error - but is this intended?
        rawList.add(3.14); // Mixed types - dangerous!

        // Must cast when retrieving - can fail at runtime
        for (Object item : rawList) {
            // String s = (String) item;  // ClassCastException for 42 and 3.14!
            System.out.println("  Item: " + item + " (type: " + item.getClass().getSimpleName() + ")");
        }

        System.out.println("""

        Problems with raw types:
        - No compile-time type checking
        - Runtime ClassCastException possible
        - Must cast on every retrieval
        - Intent is unclear (what types are allowed?)
        """);
    }

    // ===== With Generics =====

    static void withGenerics() {
        System.out.println("=== With Generics ===");

        // With generics - type-safe
        List<String> strings = new ArrayList<>();
        strings.add("Hello");
        strings.add("World");
        // strings.add(42);  // Compile error! Type safety enforced

        // No cast needed
        for (String s : strings) {
            System.out.println("  String: " + s.toUpperCase());
        }

        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);

        // Can use Integer methods directly
        int sum = 0;
        for (Integer n : numbers) {
            sum += n; // Auto-unboxing works
        }
        System.out.println("  Sum: " + sum);

        System.out.println("""

        Benefits of generics:
        - Compile-time type checking
        - No casts needed
        - Self-documenting code
        - IDE support and auto-completion
        """);
    }

    // ===== Generic Classes =====
    // Simple generic class with one type parameter
    static class Box<T> {
        private T content;

        public Box(T content) {
            this.content = content;
        }

        public T get() {
            return content;
        }

        public void set(T content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Box[" + content + "]";
        }
    }

    static void genericClasses() {
        System.out.println("=== Generic Classes ===");

        // Box of String
        Box<String> stringBox = new Box<>("Hello");
        String s = stringBox.get(); // No cast needed
        System.out.println("  String box: " + stringBox + ", value: " + s);

        // Box of Integer
        Box<Integer> intBox = new Box<>(42);
        Integer n = intBox.get();
        System.out.println("  Integer box: " + intBox + ", value: " + n);

        // Box of custom type
        record Person(String name, int age) {}
        Box<Person> personBox = new Box<>(new Person("Alice", 30));
        Person p = personBox.get();
        System.out.println("  Person box: " + personBox + ", name: " + p.name);

        // Type parameter naming conventions
        System.out.println("""

        Type parameter naming conventions:
        - T: Type (general purpose)
        - E: Element (collections)
        - K: Key (maps)
        - V: Value (maps)
        - N: Number
        - R: Result/Return type
        """);
    }

    // ===== Generic Interfaces =====

    interface Container<E> {
        void add(E element);

        E get(int index);

        int size();
    }

    // Implementing with concrete type
    static class StringContainer implements Container<String> {
        private final List<String> items = new ArrayList<>();

        @Override
        public void add(String element) {
            items.add(element);
        }

        @Override
        public String get(int index) {
            return items.get(index);
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    // Implementing with type parameter (still generic)
    static class GenericContainer<T> implements Container<T> {
        private final List<T> items = new ArrayList<>();

        @Override
        public void add(T element) {
            items.add(element);
        }

        @Override
        public T get(int index) {
            return items.get(index);
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    static void genericInterfaces() {
        System.out.println("=== Generic Interfaces ===");

        // Using concrete implementation
        StringContainer sc = new StringContainer();
        sc.add("Hello");
        sc.add("World");
        System.out.println("  StringContainer[0]: " + sc.get(0));

        // Using generic implementation
        GenericContainer<Integer> gc = new GenericContainer<>();
        gc.add(10);
        gc.add(20);
        System.out.println("  GenericContainer<Integer>[1]: " + gc.get(1));

        // Interface reference
        Container<Double> container = new GenericContainer<>();
        container.add(3.14);
        System.out.println("  Container<Double>[0]: " + container.get(0));

        System.out.println();
    }

    // ===== Multiple Type Parameters =====
    static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    static class Triple<A, B, C> {
        private final A first;
        private final B second;
        private final C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }

        public C getThird() {
            return third;
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ", " + third + ")";
        }
    }

    static void multipleTypeParameters() {
        System.out.println("=== Multiple Type Parameters ===");

        Pair<String, Integer> nameAge = new Pair<>("Alice", 30);
        System.out.println("  Name-Age pair: " + nameAge);

        Pair<Integer, List<String>> idNames = new Pair<>(1, List.of("Alice", "Bob"));
        System.out.println("  ID-Names pair: " + idNames);

        Triple<String, Integer, Boolean> record = new Triple<>("Item", 100, true);
        System.out.println("  Triple: " + record);

        // Nested generics
        Pair<String, Pair<Integer, Double>> nested = new Pair<>("data", new Pair<>(42, 3.14));
        System.out.println("  Nested pair: " + nested);

        System.out.println();
    }

    // ===== Diamond Operator (Java 7+) =====
    static void diamondOperator() {
        System.out.println("=== Diamond Operator (Java 7+) ===");

        // Before Java 7 - redundant type specification
        List<String> oldWay = new ArrayList<String>();

        // Java 7+ - diamond operator infers type
        List<String> newWay = new ArrayList<>();

        // Works with complex types too
        List<Pair<String, Integer>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("one", 1));
        pairs.add(new Pair<>("two", 2));
        System.out.println("  Pairs: " + pairs);

        // Anonymous class with diamond (Java 9+)
        Box<String> anonymousBox = new Box<>("Anonymous") {
            @Override
            public String toString() {
                return "AnonymousBox[" + get() + "]";
            }
        };
        System.out.println("  Anonymous box: " + anonymousBox);

        System.out.println("""

        Diamond operator:
        - Introduced in Java 7
        - Compiler infers type arguments
        - Works in variable declarations
        - Works with anonymous classes (Java 9+)
        """);
    }
}
