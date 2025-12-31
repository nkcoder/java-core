package org.nkcoder.fp;

import java.util.List;
import java.util.Optional;

/**
 * Optional: container for potentially absent values.
 *
 * <ul>
 *   <li>Avoid null checks and NullPointerException
 *   <li>{@code orElse} vs {@code orElseGet}: eager vs lazy default
 *   <li>Never use Optional for fields or method parameters
 *   <li>Use for return types when absence is valid outcome
 * </ul>
 */
public class OptionalExample {

    public static void main(String[] args) {
        creatingOptionals();
        extractingValues();
        orElseVsOrElseGet();
        chainingOperations();
        optionalWithStreams();
        antiPatterns();
    }

    static void creatingOptionals() {
        System.out.println("=== Creating Optionals ===");

        // From non-null value
        Optional<String> present = Optional.of("hello");
        System.out.println("of(): " + present);

        // Empty optional
        Optional<String> empty = Optional.empty();
        System.out.println("empty(): " + empty);

        // From nullable value (may be null)
        String nullable = null;
        Optional<String> fromNullable = Optional.ofNullable(nullable);
        System.out.println("ofNullable(null): " + fromNullable);

        String nonNull = "world";
        Optional<String> fromNonNull = Optional.ofNullable(nonNull);
        System.out.println("ofNullable(value): " + fromNonNull);

        // of() with null throws NullPointerException!
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("of(null) throws NPE");
        }

        System.out.println();
    }

    static void extractingValues() {
        System.out.println("=== Extracting Values ===");

        Optional<String> present = Optional.of("hello");
        Optional<String> empty = Optional.empty();

        // isPresent / isEmpty (Java 11+)
        System.out.println("isPresent: " + present.isPresent());
        System.out.println("isEmpty: " + empty.isEmpty());

        // get() - throws if empty (avoid!)
        System.out.println("get(): " + present.get());

        // orElse - provide default value
        System.out.println("orElse: " + empty.orElse("default"));

        // orElseGet - lazy default (computed only if empty)
        System.out.println("orElseGet: " + empty.orElseGet(() -> "computed"));

        // orElseThrow - throw if empty
        try {
            empty.orElseThrow(() -> new IllegalStateException("Value required"));
        } catch (IllegalStateException e) {
            System.out.println("orElseThrow: " + e.getMessage());
        }

        // orElseThrow() no-arg (Java 10+) - throws NoSuchElementException
        try {
            empty.orElseThrow();
        } catch (Exception e) {
            System.out.println("orElseThrow(): " + e.getClass().getSimpleName());
        }

        // ifPresent - execute action if present
        present.ifPresent(v -> System.out.println("ifPresent: " + v));

        // ifPresentOrElse (Java 9+)
        empty.ifPresentOrElse(
                v -> System.out.println("Value: " + v), () -> System.out.println("ifPresentOrElse: no value"));

        System.out.println();
    }

    static void orElseVsOrElseGet() {
        System.out.println("=== orElse vs orElseGet ===");

        Optional<String> present = Optional.of("hello");

        // orElse ALWAYS evaluates default, even if not needed
        System.out.println("orElse with present value:");
        String result1 = present.orElse(expensiveDefault());

        // orElseGet only evaluates if empty
        System.out.println("orElseGet with present value:");
        String result2 = present.orElseGet(() -> expensiveDefault());

        System.out.println("Results: " + result1 + ", " + result2);

        System.out.println("Rule: Use orElseGet when default is expensive to compute");

        System.out.println();
    }

    static String expensiveDefault() {
        System.out.println("  Computing expensive default...");
        return "expensive";
    }

    static void chainingOperations() {
        System.out.println("=== Chaining Operations ===");

        Optional<String> name = Optional.of("  Alice  ");

        // map - transform value if present
        Optional<String> trimmed = name.map(String::trim);
        Optional<Integer> length = name.map(String::trim).map(String::length);
        System.out.println("map trim: " + trimmed);
        System.out.println("map trim->length: " + length);

        // filter - keep only if predicate matches
        Optional<String> longName = name.map(String::trim).filter(s -> s.length() > 3);
        System.out.println("filter length > 3: " + longName);

        Optional<String> shortName = name.map(String::trim).filter(s -> s.length() > 10);
        System.out.println("filter length > 10: " + shortName);

        // flatMap - when transformation returns Optional
        Optional<String> upper = name.flatMap(s -> toUpperIfNotEmpty(s.trim()));
        System.out.println("flatMap: " + upper);

        // or (Java 9+) - provide alternative Optional
        Optional<String> empty = Optional.empty();
        Optional<String> fallback = empty.or(() -> Optional.of("fallback"));
        System.out.println("or: " + fallback);

        System.out.println();
    }

    static Optional<String> toUpperIfNotEmpty(String s) {
        return s.isEmpty() ? Optional.empty() : Optional.of(s.toUpperCase());
    }

    static void optionalWithStreams() {
        System.out.println("=== Optional with Streams ===");

        List<Optional<String>> optionals =
                List.of(Optional.of("a"), Optional.empty(), Optional.of("b"), Optional.empty(), Optional.of("c"));

        // Filter and extract present values
        List<String> values = optionals.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        System.out.println("Filter + get: " + values);

        // Better: flatMap with stream() (Java 9+)
        List<String> values2 = optionals.stream().flatMap(Optional::stream).toList();
        System.out.println("flatMap stream: " + values2);

        // Stream from single Optional
        Optional<String> opt = Optional.of("hello");
        opt.stream().forEach(s -> System.out.println("Optional stream: " + s));

        // findFirst returns Optional
        Optional<String> first = List.of("apple", "banana", "cherry").stream()
                .filter(s -> s.startsWith("b"))
                .findFirst();
        System.out.println("findFirst: " + first);

        System.out.println();
    }

    static void antiPatterns() {
        System.out.println("=== Anti-Patterns (Don't Do These!) ===");

        Optional<String> opt = Optional.of("hello");

        System.out.println("""
          1. DON'T: if (opt.isPresent()) { return opt.get(); }
             DO: return opt.orElse(default) or opt.map(...)

          2. DON'T: Optional.of(value) when value could be null
             DO: Optional.ofNullable(value)

          3. DON'T: Use Optional for class fields
             DO: Use nullable fields with clear documentation

          4. DON'T: Use Optional as method parameter
             DO: Use overloaded methods or @Nullable 5. DON'T: Return Optional<Collection>
             DO: Return empty collection instead

          6. DON'T: opt.get() without checking
             DO: orElse, orElseGet, orElseThrow
          """);

        // Example: Prefer empty collection over Optional<List>
        System.out.println("Return empty list, not Optional<List>:");
        List<String> items = findItems(); // Returns empty list, not Optional
        System.out.println("  Items: " + items);
    }

    static List<String> findItems() {
        // Return empty list instead of Optional<List<String>>
        return List.of();
    }
}
