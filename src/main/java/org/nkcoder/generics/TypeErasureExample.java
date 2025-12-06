package org.nkcoder.generics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Type Erasure: How Java implements generics and its implications.
 *
 * <p><strong>Java 25 Status:</strong> Core language behavior, unchanged. Understanding type
 * erasure is essential for working effectively with generics.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Generics are a compile-time feature</li>
 *   <li>Type parameters are erased at runtime</li>
 *   <li>Bridge methods maintain polymorphism</li>
 *   <li>Limitations and workarounds</li>
 * </ul>
 */
public class TypeErasureExample {

  static void main(String[] args) {
    whatIsTypeErasure();
    erasureRules();
    limitations();
    bridgeMethods();
    workarounds();
    reflectionAndGenerics();
  }

  // ===== What Is Type Erasure? =====

  static void whatIsTypeErasure() {
    System.out.println("=== What Is Type Erasure? ===");

    List<String> strings = new ArrayList<>();
    List<Integer> integers = new ArrayList<>();

    // At runtime, both are just ArrayList - type info is erased
    System.out.println("  strings class: " + strings.getClass());
    System.out.println("  integers class: " + integers.getClass());
    System.out.println("  Same class? " + (strings.getClass() == integers.getClass()));

    // This is why you can't do: if (obj instanceof List<String>)
    // At runtime, it's just List

    System.out.println("""

        Type erasure:
        - Generic type info exists only at compile time
        - At runtime, List<String> becomes just List
        - JVM has no knowledge of type arguments
        - Done for backward compatibility with pre-generics code
        """);
  }

  // ===== Erasure Rules =====
  static class Box<T> {
    private T content;
    public T get() { return content; }
    public void set(T content) { this.content = content; }
  }

  static class NumberBox<T extends Number> {
    private T content;
    public T get() { return content; }
    public void set(T content) { this.content = content; }
  }

  static class ComparableBox<T extends Comparable<T>> {
    private T content;
    public T get() { return content; }
    public void set(T content) { this.content = content; }
  }

  static void erasureRules() {
    System.out.println("=== Erasure Rules ===");

    // Unbounded type parameter T → Object
    System.out.println("  Box<T> after erasure:");
    for (Method m : Box.class.getDeclaredMethods()) {
      System.out.println("    " + m.getReturnType().getSimpleName() + " " + m.getName() + "()");
    }

    // Bounded type parameter T extends Number → Number
    System.out.println("\n  NumberBox<T extends Number> after erasure:");
    for (Method m : NumberBox.class.getDeclaredMethods()) {
      System.out.println("    " + m.getReturnType().getSimpleName() + " " + m.getName() + "()");
    }

    // Bounded type parameter T extends Number → Number
    System.out.println("\n  ComparableBox<T extends Comparable> after erasure:");
    for (Method m : ComparableBox.class.getDeclaredMethods()) {
      System.out.println("    " + m.getReturnType().getSimpleName() + " " + m.getName() + "()");
    }

    System.out.println("""

        Erasure rules:
        - Unbounded T → Object
        - T extends Bound → Bound (first bound if multiple)
        - T extends A & B → A (first bound)
        - Arrays of generic types → arrays of erased types
        """);
  }

  // ===== Limitations Due to Type Erasure =====
  static void limitations() {
    System.out.println("=== Limitations Due to Type Erasure ===");

    System.out.println("""
        1. Cannot instantiate type parameters:
           T obj = new T();  // Compile error!

        2. Cannot create arrays of parameterized types:
           List<String>[] array = new List<String>[10];  // Compile error!

        3. Cannot use instanceof with parameterized types:
           if (obj instanceof List<String>) { }  // Compile error!

        4. Cannot use primitives as type arguments:
           List<int> ints = ...;  // Compile error! Use List<Integer>

        5. Cannot create generic exceptions:
           class MyException<T> extends Exception { }  // Compile error!

        6. Cannot overload methods by type parameter only:
           void process(List<String> list) { }
           void process(List<Integer> list) { }  // Same erasure!
        """);

    // Demonstration of limitation #6
    // Both methods would have signature: process(List list) after erasure
    // This is called "erasure conflict"

    // Demonstration of heap pollution
    heapPollution();
  }

  @SuppressWarnings("unchecked")
  static void heapPollution() {
    System.out.println("  Heap pollution example:");

    List<String> strings = new ArrayList<>();
    strings.add("Hello");

    // Unsafe cast - causes heap pollution
    List rawList = strings;         // Raw type - loses type info
    rawList.add(42);                // No compile error! (unchecked)

    // strings now contains a String and an Integer!
    System.out.println("    List content: " + strings);

    try {
      for (String s : strings) {    // ClassCastException at runtime
        System.out.println(s);
      }
    } catch (ClassCastException e) {
      System.out.println("    ClassCastException: " + e.getMessage());
    }

    System.out.println();
  }

  // ===== Bridge Methods =====

  interface Processor<T> {
    T process(T input);
  }

  static class StringProcessor implements Processor<String> {
    @Override
    public String process(String input) {
      return input.toUpperCase();
    }
  }

  static void bridgeMethods() {
    System.out.println("=== Bridge Methods ===");

    // After erasure, Processor has: Object process(Object input)
    // But StringProcessor has: String process(String input)
    // Compiler generates a bridge method to maintain polymorphism

    System.out.println("  Methods in StringProcessor:");
    for (Method m : StringProcessor.class.getDeclaredMethods()) {
      System.out.println("    " + m.getReturnType().getSimpleName() + " " +
          m.getName() + "(...) - bridge: " + m.isBridge());
    }

    // Bridge method enables polymorphism
    Processor<String> proc = new StringProcessor();
    String result = proc.process("hello");
    System.out.println("  Processed: " + result);

    System.out.println("""

        Bridge methods:
        - Generated by compiler to preserve polymorphism
        - Have same erasure as superclass/interface method
        - Delegate to the actual implementation
        - Marked as synthetic and bridge in bytecode
        """);
  }

  // ===== Workarounds for Type Erasure =====

  // Workaround 1: Class token
  static <T> T createInstance(Class<T> clazz) throws Exception {
    return clazz.getDeclaredConstructor().newInstance();
  }

  // Workaround 2: Type token (super type token pattern)
  abstract static class TypeReference<T> {
    private final Type type;

    protected TypeReference() {
      Type superclass = getClass().getGenericSuperclass();
      this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
      return type;
    }
  }

  // Workaround 3: Factory pattern
  interface Factory<T> {
    T create();
  }

  static <T> T createWithFactory(Factory<T> factory) {
    return factory.create();
  }

  static void workarounds() {
    System.out.println("=== Workarounds for Type Erasure ===");

    // 1. Class token
    try {
      StringBuilder sb = createInstance(StringBuilder.class);
      sb.append("Created with class token");
      System.out.println("  Class token: " + sb);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 2. Type token (captures generic type info)
    TypeReference<List<String>> typeRef = new TypeReference<>() {};
    System.out.println("  Type token: " + typeRef.getType());

    // 3. Factory pattern
    String created = createWithFactory(() -> "Created with factory");
    System.out.println("  Factory: " + created);

    List<Integer> listCreated = createWithFactory(ArrayList::new);
    System.out.println("  Factory (ArrayList): " + listCreated.getClass().getSimpleName());

    System.out.println("""

        Common workarounds:
        - Pass Class<T> token for runtime type access
        - Use TypeReference pattern (Jackson, Guava)
        - Use factory interfaces/lambdas
        - Use reflection carefully
        """);
  }

  // ===== Reflection and Generics =====

  static class GenericFields {
    public List<String> strings;
    public List<Integer> integers;
    public List<?> unknown;
  }

  static void reflectionAndGenerics() {
    System.out.println("=== Reflection and Generics ===");

    // Generic type info IS preserved in some places:
    // - Field declarations
    // - Method signatures
    // - Class extends/implements
    // - NOT in local variables or return values at runtime

    System.out.println("  Field generic types:");
    for (Field field : GenericFields.class.getDeclaredFields()) {
      Type genericType = field.getGenericType();
      if (genericType instanceof ParameterizedType pt) {
        System.out.println("    " + field.getName() + ": " + pt);
        System.out.println("      Raw type: " + pt.getRawType());
        System.out.println("      Type args: " + java.util.Arrays.toString(pt.getActualTypeArguments()));
      }
    }

    // Subclass retains type info
    class StringList extends ArrayList<String> {}
    Type superclass = StringList.class.getGenericSuperclass();
    if (superclass instanceof ParameterizedType pt) {
      System.out.println("\n  StringList extends: " + pt);
    }

    System.out.println("""

        Generic info preserved in:
        - Field.getGenericType()
        - Method.getGenericParameterTypes()
        - Method.getGenericReturnType()
        - Class.getGenericSuperclass()
        - Class.getGenericInterfaces()

        This is how frameworks like Jackson, Gson, Spring work!
        """);
  }
}
