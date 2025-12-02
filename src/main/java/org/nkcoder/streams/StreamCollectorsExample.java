package org.nkcoder.streams;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Collectors: transform streams into collections and aggregations.
 *
 * <ul>
 *   <li>{@code toList()}, {@code toSet()}, {@code toMap()}: basic collectors
 *   <li>{@code groupingBy()}: group elements by classifier
 *   <li>{@code partitioningBy()}: split into true/false groups
 *   <li>{@code joining()}: concatenate strings
 * </ul>
 */
public class StreamCollectorsExample {
  record Person(String name, String city, int age) {}

  public static void main(String[] args) {
    basicCollectors();
    toMapCollector();
    groupByCollector();
    partitioningByCollector();
    joiningCollector();
    downstreamCollector();
  }

  static List<Person> people() {
    return List.of(
        new Person("Alice", "NYC", 30),
        new Person("Bob", "LA", 25),
        new Person("Charlie", "NYC", 35),
        new Person("Diana", "LA", 28),
        new Person("Eve", "NYC", 25));
  }

  static void basicCollectors() {
    System.out.println("=== Basic Collectors ===");

    // Before Java 16, use .collect(Collectors.toList())
    List<String> names =
        people().stream().map(Person::name).toList(); // .collect(Collectors.toList())
    System.out.println("Names (List): " + names);

    Set<String> cities = people().stream().map(Person::city).collect(Collectors.toSet());
    System.out.println("Cities (Set): " + cities);

    // Counting
    long count =
        people().stream().filter(p -> p.age > 10).count(); // .collect(Collectors.counting())
    System.out.println("Count: " + count);

    // Summing
    int totalAge =
        people().stream()
            .mapToInt(Person::age)
            .sum(); // .collect(Collectors.summingInt(Person::age))
    System.out.println("Total Age: " + totalAge);

    // Averaging
    double avgAge = people().stream().collect(Collectors.averagingInt(Person::age));
    System.out.println("Avg Age: " + avgAge);

    System.out.println();
  }

  static void toMapCollector() {
    System.out.println("=== ToMap Collectors ===");

    // Simple: name -> age
    Map<String, Integer> nameToAge =
        people().stream().collect(Collectors.toMap(Person::name, Person::age));
    System.out.println("Name to age: " + nameToAge);

    // With merge function (handle duplicate keys)
    Map<String, Integer> cityToTotalAge =
        people().stream().collect(Collectors.toMap(Person::city, Person::age, Integer::sum));
    System.out.println("City to total age: " + cityToTotalAge);

    // With specific map type
    Map<String, Integer> cityToCountSorted =
        people().stream()
            .collect(Collectors.toMap(Person::city, p -> 1, Integer::sum, TreeMap::new));
    System.out.println("City to count (sorted): " + cityToCountSorted);

    System.out.println();
  }

  static void groupByCollector() {
    System.out.println("=== GroupBy Collectors ===");

    // Group by single field
    Map<String, List<Person>> byCity =
        people().stream().collect(Collectors.groupingBy(Person::city));
    byCity.forEach((city, list) -> System.out.println(" " + city + ": " + list));

    // Group by computed value
    Map<String, List<Person>> byAgeGroup =
        people().stream().collect(Collectors.groupingBy(p -> p.age < 30 ? "young" : "senior"));
    System.out.println("By age group: " + byAgeGroup);

    // Multi-level grouping
    Map<String, Map<String, List<Person>>> byCityThenAge =
        people().stream()
            .collect(
                Collectors.groupingBy(
                    Person::city, Collectors.groupingBy(p -> p.age() < 30 ? "young" : "senior")));
    System.out.println("By city then age: " + byCityThenAge);

    System.out.println();
  }

  static void partitioningByCollector() {
    System.out.println("=== Partitioning By Collectors ===");

    // Partition into exactly two groups (true/false)
    Map<Boolean, List<Person>> byAge30 =
        people().stream().collect(Collectors.partitioningBy(p -> p.age < 30));
    System.out.println("Age >= 30: " + byAge30.get(true));
    System.out.println("Age < 30: " + byAge30.get(false));

    // With downstream collector
    Map<Boolean, Long> countByAge30 =
        people().stream()
            .collect(Collectors.partitioningBy(p -> p.age >= 30, Collectors.counting()));
    System.out.println("Count by age >= 30: " + countByAge30);

    System.out.println();
  }

  static void joiningCollector() {
    System.out.println("=== Joining Collectors ===");

    // Simple join
    String names = people().stream().map(Person::name).collect(Collectors.joining());
    System.out.println("Joined: " + names);

    // With delimiter
    String withComma = people().stream().map(Person::name).collect(Collectors.joining(", "));
    System.out.println("With comma: " + withComma);

    // With delimiter, prefix, suffix
    String formatted =
        people().stream().map(Person::name).collect(Collectors.joining(", ", "[", "]"));
    System.out.println("Formatted: " + formatted);

    System.out.println();
  }

  static void downstreamCollector() {
    System.out.println("=== Downstream Collectors ===");

    // groupingBy with counting
    Map<String, Long> countByCity =
        people().stream().collect(Collectors.groupingBy(Person::city, Collectors.counting()));
    System.out.println("Count by city: " + countByCity);

    // groupingBy with mapping
    Map<String, List<String>> namesByCity =
        people().stream()
            .collect(
                Collectors.groupingBy(
                    Person::city, Collectors.mapping(Person::name, Collectors.toList())));
    System.out.println("Names by city: " + namesByCity);

    // groupingBy with summarizing
    Map<String, Integer> maxAgeByCity =
        people().stream()
            .collect(
                Collectors.groupingBy(
                    Person::city,
                    Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparingInt(Person::age)),
                        opt -> opt.map(Person::age).orElse(0))));
    System.out.println("Max Age by city: " + maxAgeByCity);

    // Simpler: groupingBy with reducing
    Map<String, Integer> sumAgeByCity =
        people().stream()
            .collect(Collectors.groupingBy(Person::city, Collectors.summingInt(Person::age)));
    System.out.println("Sum Age by city: " + sumAgeByCity);

    System.out.println();
  }
}
