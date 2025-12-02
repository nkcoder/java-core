package org.nkcoder.streams;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Advanced stream operations.
 *
 * <ul>
 *   <li>{@code flatMap}: flatten nested structures</li>
 *   <li>{@code reduce}: combine elements into single result</li>
 *   <li>{@code takeWhile/dropWhile}: conditional slicing (Java 9+)</li>
 *   <li>{@code teeing}: collect into two collectors at once (Java 12+)</li>
 * </ul>
 */
public class StreamAdvancedExample {

  public static void main(String[] args) {
    flatMapExample();
    reduceExample();
    takeWhileDropWhile();
    peekForDebugging();
    teeingCollector();
    gatherersPreview();
  }

  static void flatMapExample() {
    System.out.println("=== flatMap ===");

    // Flatten list of lists
    List<List<Integer>> nested = List.of(List.of(1, 2, 3), List.of(4, 5), List.of(6, 7, 8, 9));

    List<Integer> flat = nested.stream().flatMap(List::stream).toList();
    System.out.println("Flattened: " + flat);

    // Split strings and flatten
    List<String> sentences = List.of("Hello World", "Java Streams");
    List<String> words = sentences.stream().flatMap(s -> Stream.of(s.split(" "))).toList();
    System.out.println("Words: " + words);

    // flatMap with Optional
    record Person(String name, String email) {}
    List<Person> people =
        List.of(
            new Person("Alice", "alice@example.com"),
            new Person("Bob", null),
            new Person("Charlie", "charlie@example.com"));

    List<String> emails =
        people.stream()
            .map(Person::email)
            .flatMap(Stream::ofNullable) // Filter nulls elegantly
            .toList();
    System.out.println("Emails (no nulls): " + emails);

    // flatMapToInt for primitives
    int sum = nested.stream().flatMapToInt(list -> list.stream().mapToInt(Integer::intValue)).sum();
    System.out.println("Sum: " + sum);

    System.out.println();
  }

  static void reduceExample() {
    System.out.println("=== reduce ===");

    List<Integer> numbers = List.of(1, 2, 3, 4, 5);

    // reduce with identity and accumulator
    int sum = numbers.stream().reduce(0, (a, b) -> a + b);
    System.out.println("Sum: " + sum);

    // Same with method reference
    int sum2 = numbers.stream().reduce(0, Integer::sum);
    System.out.println("Sum (method ref): " + sum2);

    // reduce without identity - returns Optional
    var product = numbers.stream().reduce((a, b) -> a * b);
    System.out.println("Product: " + product.orElse(0));

    // reduce on empty stream
    var emptyResult = Stream.<Integer>empty().reduce((a, b) -> a + b);
    System.out.println("Empty reduce: " + emptyResult);

    // String concatenation with reduce
    List<String> words = List.of("Java", "Stream", "API");
    String sentence = words.stream().reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
    System.out.println("Sentence: " + sentence);

    // Better: use joining collector for strings
    String joined = words.stream().collect(Collectors.joining(" "));
    System.out.println("Joined: " + joined);

    // reduce with combiner (for parallel streams)
    int parallelSum = numbers.parallelStream().reduce(0, Integer::sum, Integer::sum);
    System.out.println("Parallel sum: " + parallelSum);

    // Complex reduce: find longest string
    List<String> items = List.of("a", "bbb", "cc", "dddd");
    String longest = items.stream().reduce("", (a, b) -> a.length() >= b.length() ? a : b);
    System.out.println("Longest: " + longest);

    System.out.println();
  }

  static void takeWhileDropWhile() {
    System.out.println("=== takeWhile / dropWhile (Java 9+) ===");

    List<Integer> numbers = List.of(1, 2, 3, 4, 5, 4, 3, 2, 1);

    // takeWhile - take elements while predicate is true, stop at first false
    List<Integer> taken = numbers.stream().takeWhile(n -> n < 4).toList();
    System.out.println("takeWhile < 4: " + taken); // [1, 2, 3]

    // dropWhile - skip elements while predicate is true, take rest
    List<Integer> dropped = numbers.stream().dropWhile(n -> n < 4).toList();
    System.out.println("dropWhile < 4: " + dropped); // [4, 5, 4, 3, 2, 1]

    // Combine for "window"
    List<Integer> window = numbers.stream().dropWhile(n -> n < 3).takeWhile(n -> n <= 5).toList();
    System.out.println("Window [3,5]: " + window);

    // Contrast with filter (processes ALL elements)
    List<Integer> filtered = numbers.stream().filter(n -> n < 4).toList();
    System.out.println("filter < 4: " + filtered); // [1, 2, 3, 3, 2, 1]

    // Useful for sorted/ordered data
    List<String> sorted = List.of("apple", "apricot", "banana", "cherry", "date");
    List<String> aWords = sorted.stream().takeWhile(s -> s.startsWith("a")).toList();
    System.out.println("Words starting with 'a': " + aWords);

    System.out.println();
  }

  static void peekForDebugging() {
    System.out.println("=== peek for Debugging ===");

    List<String> result =
        List.of("one", "two", "three").stream()
            .peek(s -> System.out.println("  Original: " + s))
            .map(String::toUpperCase)
            .peek(s -> System.out.println("  After map: " + s))
            .filter(s -> s.length() > 3)
            .peek(s -> System.out.println("  After filter: " + s))
            .toList();

    System.out.println("Result: " + result);

    System.out.println(
        """

          Note: peek is for debugging only!
          - Don't rely on it for side effects
          - May not execute in parallel streams
          - May be optimized away
          """);

    System.out.println();
  }

  static void teeingCollector() {
    System.out.println("=== teeing Collector (Java 12+) ===");

    List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    // Collect into two results simultaneously
    record Stats(long count, double average) {}

    Stats stats =
        numbers.stream()
            .collect(
                Collectors.teeing(
                    Collectors.counting(), Collectors.averagingInt(Integer::intValue), Stats::new));
    System.out.println("Stats: count=" + stats.count() + ", avg=" + stats.average());

    // Min and max in single pass
    record Range(Integer min, Integer max) {}

    Range range =
        numbers.stream()
            .collect(
                Collectors.teeing(
                    Collectors.minBy(Integer::compare),
                    Collectors.maxBy(Integer::compare),
                    (min, max) -> new Range(min.orElse(null), max.orElse(null))));
    System.out.println("Range: " + range.min() + " to " + range.max());

    // Partition alternative
    record Partition(List<Integer> small, List<Integer> large) {}

    Partition partition =
        numbers.stream()
            .collect(
                Collectors.teeing(
                    Collectors.filtering(n -> n <= 5, Collectors.toList()),
                    Collectors.filtering(n -> n > 5, Collectors.toList()),
                    Partition::new));
    System.out.println("Small: " + partition.small());
    System.out.println("Large: " + partition.large());

    System.out.println();
  }

  static void gatherersPreview() {
    System.out.println("=== Gatherers Preview (Java 22+) ===");

    // Note: Gatherers is a preview feature in Java 22+
    // This is what it will look like:

    System.out.println(
        """
          // Windowing (fixed size chunks)
          Stream.of(1, 2, 3, 4, 5, 6, 7)
              .gather(Gatherers.windowFixed(3))
              .toList();  // [[1,2,3], [4,5,6], [7]]

          // Sliding window
          Stream.of(1, 2, 3, 4, 5)
              .gather(Gatherers.windowSliding(3))
              .toList();  // [[1,2,3], [2,3,4], [3,4,5]]

          // Fold (like reduce but more flexible)
          Stream.of("a", "b", "c")
              .gather(Gatherers.fold(() -> "", (a, b) -> a + b))
              .findFirst();  // "abc"

          // Scan (running accumulation)
          Stream.of(1, 2, 3, 4)
              .gather(Gatherers.scan(() -> 0, Integer::sum))
              .toList();  // [1, 3, 6, 10]
          """);

    // Current workaround for windowing
    List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7);
    int windowSize = 3;

    List<List<Integer>> windows =
        IntStream.range(0, (numbers.size() + windowSize - 1) / windowSize)
            .mapToObj(
                i ->
                    numbers.subList(i * windowSize, Math.min((i + 1) * windowSize, numbers.size())))
            .toList();
    System.out.println("Manual windowing: " + windows);
  }
}
