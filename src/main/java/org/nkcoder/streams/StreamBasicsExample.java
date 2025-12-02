package org.nkcoder.streams;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 *
 * <pre>
 * Stream basics: creating streams and core operations.
 *
 * - Streams are lazy - intermediate ops don't execute until terminal op
 * - Streams can only be consumed once
 * - Intermediate: filter, map, flatmap, sorted, distinct, limit, skip
 * - Terminal: forEach, collect, reduce, count, findFirst, anyMatch
 * </pre>
 */
public class StreamBasicsExample {

  public static void main(String[] args) {
    creatingStreams();
    intermediateOperations();
    terminalOperations();
    lazyEvaluation();
  }

  static void creatingStreams() {
    System.out.println("=== Creating Streams ===");

    // From collection
    List<String> list = List.of("a", "b", "c");
    Stream<String> fromList = list.stream();
    System.out.println("fromList = " + fromList);

    // From values
    Stream<String> fromValues = Stream.of("x", "y", "z");
    System.out.println("fromValues = " + fromValues);

    // From array
    String[] array = {"1", "2", "3"};
    Stream<String> fromArray = Stream.of(array);
    System.out.println("fromArray = " + fromArray);

    // Infinite streams (must limit)
    Stream<Integer> infinite = Stream.iterate(0, n -> n + 2).limit(5);
    System.out.println("First 5 even numbers (by iterate stream): " + infinite.toList());

    Stream<Double> randoms = Stream.generate(Math::random).limit(3);
    System.out.println("Three random numbers (by generate stream): " + randoms.toList());

    // Primitive streams (avoid boxing overhead)
    IntStream range = IntStream.range(1, 5); // 1, 2, 3, 4
    IntStream rangeClosed = IntStream.rangeClosed(1, 5); // 1, 2, 3, 4, 5
    System.out.println("Range sum: " + IntStream.range(1, 5).sum());
  }

  static void intermediateOperations() {
    System.out.println("=== Intermediate Operations ===");

    List<String> words = List.of("apple", "banana", "apricot", "cherry", "avocado");

    // filter - keep elements matching predicate
    List<String> startsWithA = words.stream().filter(s -> s.startsWith("a")).toList();
    System.out.println("Starts with 'a':  " + startsWithA);

    // map - transform each element
    List<Integer> lengths = words.stream().map(String::length).toList();
    System.out.println("Lengths: " + lengths);

    // sorted
    List<String> sorted = words.stream().sorted().toList();
    System.out.println("Sorted: " + sorted);

    // distinct
    List<String> distinct = words.stream().distinct().toList();
    System.out.println("Distinct: " + distinct);

    // limit and skip
    List<String> limit2 = words.stream().limit(2).toList();
    List<String> skip2 = words.stream().skip(2).toList();

    // Chaining multiple operations
    List<String> chaining =
        words.stream().filter(s -> s.length() > 5).map(String::toUpperCase).sorted().toList();
    System.out.println("chaining: " + chaining);

    System.out.println();
  }

  static void terminalOperations() {
    System.out.println("=== Terminal Operations ===");

    // forEach - perform action on each element
    List<Integer> numbers = List.of(1, 2, 3, 4, 5);
    numbers.stream().forEach(n -> System.out.println(n + " "));

    // count
    long count = numbers.stream().filter(n -> n > 2).count();
    System.out.println("Count > 2: " + count);

    // findFirst / findAny
    var first = numbers.stream().filter(n -> n > 2).findFirst();
    System.out.println("First > 2: " + first);

    // anyMatch / allMatch / noneMatch
    boolean anyEven = numbers.stream().anyMatch(n -> n % 2 == 0);
    boolean allPositive = numbers.stream().allMatch(n -> n > 0);
    boolean allNegative = numbers.stream().allMatch(n -> n < 0);
    System.out.println("Any even: " + anyEven);
    System.out.println("All positive: " + allPositive);
    System.out.println("All negative: " + allNegative);

    // min / max
    var min = numbers.stream().min(Integer::compare);
    var max = numbers.stream().max(Integer::compare);
    System.out.println("Min: " + min + ", Max: " + max);

    // toList() - Java 16+ shorthand for collect(Collectors.toList())
    List<Integer> doubled = numbers.stream().map(n -> n * 2).toList();
    System.out.println("Doubled: " + doubled);

    System.out.println();
  }

  static void lazyEvaluation() {
    System.out.println("=== Lazy Evaluation ===");

    List<String> words = List.of("one", "two", "three", "four", "five", "six");

    Stream<String> lazy =
        words.stream()
            .filter(
                s -> {
                  System.out.println("  filtering: " + s);
                  return s.length() > 3;
                })
            .map(
                s -> {
                  System.out.println("  mapping: " + s);
                  return s.toUpperCase();
                });

    System.out.println("Stream created, no processing yet...");
    System.out.println("Calling findFirst():");

    // Only processed until fist match found
    var result = lazy.findFirst();
    System.out.println("Result: " + result.orElse("none"));
  }
}
