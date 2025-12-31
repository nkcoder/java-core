package org.nkcoder.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Generic Methods: Methods with their own type parameters.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, unchanged. Generic methods allow type parameters
 * independent of the enclosing class.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Type parameter declaration before return type
 *   <li>Static generic methods
 *   <li>Type inference
 *   <li>Generic constructors
 * </ul>
 */
public class GenericMethodsExample {

    static void main(String[] args) {
        basicGenericMethods();
        staticGenericMethods();
        typeInference();
        genericConstructors();
        varargs();
        methodsVsClasses();
        practicalPatterns();
    }

    // ===== Basic Generic Methods =====

    // Type parameter <T> declared before return type
    public <T> void printItem(T item) {
        System.out.println("    Item: " + item + " (type: " + item.getClass().getSimpleName() + ")");
    }

    // Generic method with return type using type parameter
    public <T> T identity(T value) {
        return value;
    }

    // Multiple type parameters
    public <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    static void basicGenericMethods() {
        System.out.println("=== Basic Generic Methods ===");

        GenericMethodsExample example = new GenericMethodsExample();

        // Type parameter inferred from argument
        example.printItem("Hello");
        example.printItem(42);
        example.printItem(3.14);
        example.printItem(List.of(1, 2, 3));

        // Identity function
        String s = example.identity("test");
        Integer n = example.identity(100);
        System.out.println("    Identity: " + s + ", " + n);

        // Multiple type parameters
        Map<String, Integer> map = example.createMap("age", 30);
        System.out.println("    Map: " + map);

        System.out.println();
    }

    // ===== Static Generic Methods =====

    // Static methods can have their own type parameters
    public static <T> List<T> asList(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static <T> T getFirst(List<T> list) {
        if (list.isEmpty()) return null;
        return list.getFirst();
    }

    public static <T> T getLast(List<T> list) {
        if (list.isEmpty()) return null;
        return list.getLast();
    }

    public static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    static void staticGenericMethods() {
        System.out.println("=== Static Generic Methods ===");

        List<String> words = asList("apple", "banana", "cherry");
        System.out.println("  Created list: " + words);
        System.out.println("  First: " + getFirst(words));
        System.out.println("  Last: " + getLast(words));

        List<Integer> nums = asList(5, 2, 8, 1, 9);
        System.out.println("  Numbers: " + nums);

        System.out.println("  max(10, 20): " + max(10, 20));
        System.out.println("  min(\"a\", \"z\"): " + min("a", "z"));

        System.out.println("""

        Static generic methods:
        - Have their own type parameters
        - Independent of class type parameters
        - Commonly used for utility methods
        - Examples: Collections.sort(), Arrays.asList()
        """);
    }

    // ===== Type Inference =====

    public static <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    public static <T, R> R transform(T input, Function<T, R> transformer) {
        return transformer.apply(input);
    }

    public static <T> T createDefault(Supplier<T> supplier) {
        return supplier.get();
    }

    static void typeInference() {
        System.out.println("=== Type Inference ===");

        // Type inferred from assignment context
        List<String> strings = emptyList();
        List<Integer> integers = emptyList();
        System.out.println("  Empty string list: " + strings);
        System.out.println("  Empty integer list: " + integers);

        // Type inferred from lambda
        String upper = transform("hello", String::toUpperCase);
        Integer length = transform("hello", String::length);
        System.out.println("  Transform to upper: " + upper);
        System.out.println("  Transform to length: " + length);

        // Explicit type witness (rarely needed)
        List<Double> explicit = GenericMethodsExample.<Double>emptyList();
        System.out.println("  Explicit type witness: " + explicit);

        // Type inference with method chaining
        List<String> result = emptyList();
        result.add("test");
        String first = getFirst(transform(result, ArrayList::new));
        System.out.println("  Chained result: " + first);

        System.out.println("""

        Type inference rules:
        - From arguments when possible
        - From assignment target type
        - From return context in expressions
        - Use explicit <Type> witness when ambiguous
        """);
    }

    // ===== Generic Constructors =====

    static class Container<T> {
        private T content;

        // Generic constructor - different from class type parameter
        public <U extends T> Container(U content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }
    }

    // Class with only generic constructor
    static class Holder {
        private final Object value;

        public <T> Holder(T value) {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue() {
            return (T) value;
        }
    }

    static void genericConstructors() {
        System.out.println("=== Generic Constructors ===");

        // Container<Number> can be constructed with Integer (Integer extends Number)
        Container<Number> numContainer = new Container<>(42);
        System.out.println("  Container<Number> with Integer: " + numContainer.getContent());

        Container<CharSequence> csContainer = new Container<>("Hello");
        System.out.println("  Container<CharSequence> with String: " + csContainer.getContent());

        // Holder with generic constructor
        Holder holder = new Holder(List.of(1, 2, 3));
        List<Integer> list = holder.getValue();
        System.out.println("  Holder value: " + list);

        System.out.println("""

        Generic constructors:
        - Can have their own type parameters
        - Type parameter can relate to class type parameter
        - Useful for accepting subtypes
        """);
    }

    // ===== Varargs with Generics =====

    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... entries) {
        Map<K, V> map = new HashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    static void varargs() {
        System.out.println("=== Varargs with Generics ===");

        List<String> words = listOf("a", "b", "c");
        System.out.println("  listOf: " + words);

        String result = firstNonNull(null, null, "found", "other");
        System.out.println("  firstNonNull: " + result);

        Integer num = firstNonNull(null, 42);
        System.out.println("  firstNonNull int: " + num);

        Map<String, Integer> map = mapOf(Map.entry("one", 1), Map.entry("two", 2));
        System.out.println("  mapOf: " + map);

        System.out.println("""

        Generic varargs:
        - @SafeVarargs suppresses heap pollution warning
        - Only use when method doesn't store into varargs array
        - Only on final/static/private methods (or constructors)
        """);
    }

    // ===== Generic Methods vs Generic Classes =====

    // Generic class - type parameter shared across all methods
    static class GenericClass<T> {
        private T value;

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        // This method can have its OWN type parameter too
        public <U> U convert(Function<T, U> converter) {
            return converter.apply(value);
        }
    }

    // Non-generic class with generic methods
    static class UtilityClass {
        public static <T> T coalesce(T value, T defaultValue) {
            return value != null ? value : defaultValue;
        }

        public static <T, U> U mapOrDefault(T value, Function<T, U> mapper, U defaultValue) {
            return value != null ? mapper.apply(value) : defaultValue;
        }
    }

    static void methodsVsClasses() {
        System.out.println("=== Generic Methods vs Generic Classes ===");

        // Generic class - T is fixed at instantiation
        GenericClass<String> gc = new GenericClass<>();
        gc.setValue("hello");
        Integer length = gc.convert(String::length);
        System.out.println("  GenericClass<String>.convert to length: " + length);

        // Generic methods - type varies per call
        String s = UtilityClass.coalesce(null, "default");
        Integer n = UtilityClass.coalesce(null, 42);
        System.out.println("  coalesce: " + s + ", " + n);

        String name = null;
        Integer nameLength = UtilityClass.mapOrDefault(name, String::length, 0);
        System.out.println("  mapOrDefault null: " + nameLength);

        name = "Alice";
        nameLength = UtilityClass.mapOrDefault(name, String::length, 0);
        System.out.println("  mapOrDefault Alice: " + nameLength);

        System.out.println("""

        When to use which:
        - Generic class: Type consistent across instance
        - Generic method: Type varies per invocation
        - Both: When you need both behaviors
        """);
    }

    // ===== Practical Patterns =====

    // Factory method pattern
    public static <T> T create(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    // Collecting to specific collection type
    public static <T, C extends Collection<T>> C toCollection(Iterable<T> source, Supplier<C> collectionFactory) {
        C result = collectionFactory.get();
        for (T item : source) {
            result.add(item);
        }
        return result;
    }

    // Type-safe builder
    public static <T> Builder<T> builder(Supplier<T> supplier) {
        return new Builder<>(supplier);
    }

    static class Builder<T> {
        private final Supplier<T> supplier;
        private final List<java.util.function.Consumer<T>> operations = new ArrayList<>();

        public Builder(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public Builder<T> with(java.util.function.Consumer<T> operation) {
            operations.add(operation);
            return this;
        }

        public T build() {
            T instance = supplier.get();
            for (var op : operations) {
                op.accept(instance);
            }
            return instance;
        }
    }

    // Comparing with key extractor
    public static <T, U extends Comparable<U>> Comparator<T> comparing(Function<T, U> keyExtractor) {
        return (a, b) -> keyExtractor.apply(a).compareTo(keyExtractor.apply(b));
    }

    static void practicalPatterns() {
        System.out.println("=== Practical Patterns ===");

        // Factory method
        try {
            StringBuilder sb = create(StringBuilder.class);
            sb.append("Created!");
            System.out.println("  Factory created: " + sb);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Collecting to specific type
        List<Integer> source = List.of(1, 2, 3);
        ArrayList<Integer> arrayList = toCollection(source, ArrayList::new);
        System.out.println("  Collected to ArrayList: " + arrayList.getClass().getSimpleName());

        // Type-safe builder
        StringBuilder built = builder(StringBuilder::new)
                .with(sb -> sb.append("Hello"))
                .with(sb -> sb.append(" "))
                .with(sb -> sb.append("World"))
                .build();
        System.out.println("  Built string: " + built);

        // Custom comparator
        record Person(String name, int age) {}
        List<Person> people =
                new ArrayList<>(List.of(new Person("Alice", 30), new Person("Bob", 25), new Person("Charlie", 35)));
        people.sort(comparing(Person::age));
        System.out.println("  Sorted by age: " + people);

        System.out.println("""

        Common generic method patterns:
        - Factory methods: create(Class<T>)
        - Collectors: toCollection(src, Supplier<C>)
        - Builders: builder(Supplier<T>)
        - Comparators: comparing(Function<T, U>)
        - Transformers: transform(T, Function<T, R>)
        """);
    }
}
