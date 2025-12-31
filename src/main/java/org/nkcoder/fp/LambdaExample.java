package org.nkcoder.fp;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Lambda expressions: concise syntax for functional interfaces.
 *
 * <ul>
 *   <li>Syntax: {@code (params) -> expression} or {@code (params) -> { statements }}
 *   <li>Method references: {@code Class::method} shorthand
 *   <li>Effectively final: lambdas can only capture final or effectively final variables
 *   <li>Target typing: lambda type inferred from context
 * </ul>
 */
public class LambdaExample {

    public static void main(String[] args) {
        lambdaSyntax();
        methodReferences();
        effectivelyFinal();
        targetTyping();
        lambdaVsAnonymous();
        commonPatterns();
    }

    static void lambdaSyntax() {
        System.out.println("=== Lambda Syntax ===");

        // No parameters
        Runnable r1 = () -> System.out.println("  No params");
        r1.run();

        // Single parameter (parentheses optional)
        Function<String, Integer> f1 = s -> s.length(); // String::length
        Function<String, Integer> f2 = (s) -> s.length(); // String::length
        System.out.println("Single param: " + f1.apply("hello"));

        // Multiple parameters
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b; // Integer::sum
        System.out.println("Multiple params: " + add.apply(3, 4));

        // Explicit types (usually unnecessary)
        BiFunction<Integer, Integer, Integer> multiply = (Integer a, Integer b) -> a * b;
        System.out.println("Explicit types: " + multiply.apply(3, 4));

        // Block body (needs return statement)
        Function<String, String> process = s -> {
            String trimmed = s.trim();
            String upper = trimmed.toUpperCase();
            return upper;
        };
        System.out.println("Block body: " + process.apply("  hello  "));

        // Expression body (implicit return)
        Function<String, String> simple = s -> s.trim().toUpperCase();
        System.out.println("Expression body: " + simple.apply("  hello  "));

        System.out.println();
    }

    static void methodReferences() {
        System.out.println("=== Method References ===");

        List<String> words = List.of("banana", "apple", "cherry");

        // 1. Static method reference: ClassName::staticMethod
        // Lambda: s -> Integer.parseInt(s)
        Function<String, Integer> parse = Integer::parseInt;
        System.out.println("Static method: " + parse.apply("42"));

        // 2. Instance method on parameter: ClassName::instanceMethod
        // Lambda: s -> s.toUpperCase()
        Function<String, String> upper = String::toUpperCase;
        System.out.println("Instance on param: " + words.stream().map(upper).toList());

        // 3. Instance method on specific object: object::instanceMethod
        // Lambda: s -> System.out.println(s)
        var printer = new Printer();
        words.forEach(printer::print);

        // 4. Constructor reference: ClassName::new
        // Lambda: s -> new StringBuilder(s)
        Function<String, StringBuilder> sbCreator = StringBuilder::new;
        System.out.println("Constructor: " + sbCreator.apply("hello"));

        // Comparator examples
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> natural = Comparator.naturalOrder();
        Comparator<String> reverse = Comparator.reverseOrder();

        System.out.println(
                "Sorted by length: " + words.stream().sorted(byLength).toList());
        System.out.println("Natural order: " + words.stream().sorted(natural).toList());
        System.out.println("Reverse order: " + words.stream().sorted(reverse).toList());

        System.out.println();
    }

    static class Printer {
        void print(String s) {
            System.out.println("  Printer: " + s);
        }
    }

    static void effectivelyFinal() {
        System.out.println("=== Effectively Final ===");

        // Variable must be final or effectively final
        String prefix = "Hello, "; // Effectively final (never modified)

        Function<String, String> greet = name -> prefix + name;
        System.out.println(greet.apply("World"));

        // This would NOT compile:
        // String mutable = "Hi";
        // Function<String, String> broken = s -> mutable + s;
        // mutable = "Hey";  // Error: variable must be final or effectively final

        // Workaround: use array or AtomicReference for mutable state
        int[] counter = {0}; // Array is final, contents are mutable
        List.of("a", "b", "c").forEach(s -> counter[0]++);
        System.out.println("Counter: " + counter[0]);

        // Better: avoid mutation, use streams
        long count = List.of("a", "b", "c").stream().count();
        System.out.println("Count (functional): " + count);

        System.out.println("""

          Why effectively final?
          - Lambda may execute later (in another thread)
          - Prevents confusing bugs from shared mutable state
          - Encourages functional style
          """);

        System.out.println();
    }

    static void targetTyping() {
        System.out.println("=== Target Typing ===");

        // Same lambda, different target types
        Runnable r = () -> System.out.println("  Runnable");
        Callable<String> c = () -> "Callable";

        // Context determines the type
        process(() -> System.out.println("  As Runnable"));

        // Ambiguous without cast or explicit target
        // process(() -> "result");  // Error: ambiguous

        // Generic type inference
        List<String> words = List.of("a", "bb", "ccc");

        // map() infers Function<String, Integer> from context
        List<Integer> lengths = words.stream()
                .map(s -> s.length()) // Type inferred
                .toList();
        System.out.println("Lengths: " + lengths);

        // Comparator type inferred
        words.stream()
                .sorted((a, b) -> a.length() - b.length()) // Comparator<String> inferred
                .forEach(s -> System.out.println("  " + s));

        System.out.println();
    }

    static void process(Runnable r) {
        r.run();
    }

    static void lambdaVsAnonymous() {
        System.out.println("=== Lambda vs Anonymous Class ===");

        // Anonymous class
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
                System.out.println("  Anonymous class");
                System.out.println("  'this' refers to: " + this.getClass().getSimpleName());
            }
        };
        anonymous.run();

        // Lambda
        Runnable lambda = () -> {
            System.out.println("  Lambda");
            // 'this' in lambda refers to enclosing class, not the lambda itself
            // this.getClass() won't work because we are in static method scope, not class scope. We can use
            // LambdaExample.class
            // System.out.println("  'this' refers to: " + this.getClass().getSimpleName());
        };
        lambda.run();

        System.out.println("""

          Key differences:
          - 'this': Lambda uses enclosing scope, anonymous has its own
          - Verbosity: Lambda is more concise
          - Scope: Lambda can only implement functional interfaces
          - Shadowing: Lambda cannot shadow enclosing variables
          """);

        System.out.println();
    }

    static void commonPatterns() {
        System.out.println("=== Common Patterns ===");

        List<String> words = List.of("apple", "banana", "cherry", "date");

        // Filtering
        var filtered = words.stream().filter(s -> s.length() > 5).toList();
        System.out.println("Filter: " + filtered);

        // Mapping
        var mapped = words.stream().map(String::toUpperCase).toList();
        System.out.println("Map: " + mapped);

        // Sorting with custom comparator
        var sorted = words.stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
        System.out.println("Sort by length desc: " + sorted);

        // Reduce
        var concatenated = words.stream().reduce("", (a, b) -> a + b);
        System.out.println("Reduce: " + concatenated);

        // Find
        var first = words.stream().filter(s -> s.startsWith("c")).findFirst().orElse("not found");
        System.out.println("Find: " + first);

        // Match
        boolean anyLong = words.stream().anyMatch(s -> s.length() > 5);
        boolean allShort = words.stream().allMatch(s -> s.length() < 10);
        System.out.println("Any > 5 chars: " + anyLong);
        System.out.println("All < 10 chars: " + allShort);
    }
}
