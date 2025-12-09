package org.nkcoder.fp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Functional interfaces: single abstract method interfaces for lambdas.
 *
 * <ul>
 *   <li>{@code Function<T,R>}: T → R (transform)
 *   <li>{@code Predicate<T>}: T → boolean (test)
 *   <li>{@code Consumer<T>}: T → void (action)
 *   <li>{@code Supplier<T>}: () → T (provide)
 *   <li>{@code @FunctionalInterface}: compile-time validation
 * </ul>
 */
public class FunctionalInterfaceExample {

    static void main(String[] args) {
        functionExample();
        predicateExample();
        consumerExample();
        supplierExample();
        composingFunctions();
        customFunctionalInterface();
        primitiveVariants();
        curryingAndPartialFunctions();
    }

    static void functionExample() {
        System.out.println("=== Function<T, R> ===");

        // Function: takes T, returns R
        Function<String, Integer> length = s -> s.length();
        Function<String, Integer> lengthRef = String::length; // Method reference

        System.out.println("Length of 'hello': " + length.apply("hello"));
        System.out.println("Length of 'hello' with ref: " + lengthRef.apply("hello"));

        // Chain with andThen
        Function<String, String> trim = String::trim;
        Function<String, String> upper = String::toUpperCase;
        Function<String, String> trimThenUpper = trim.andThen(upper);

        System.out.println("Trim then upper: " + trimThenUpper.apply("  hello  "));

        // Chain with compose (reverse order)
        Function<String, String> upperThenTrim = trim.compose(upper);
        System.out.println("Upper then trim: " + upperThenTrim.apply("  hello  "));

        // BiFunction: takes two arguments
        BiFunction<String, String, String> concat = (a, b) -> a + b;
        System.out.println("Concat: " + concat.apply("Hello, ", "World!"));

        // UnaryOperator: Function<T, T> (operand and result are the same type)
        UnaryOperator<String> shout = s -> s + "!";
        System.out.println("Shout: " + shout.apply("Hello"));

        System.out.println();
    }

    static void predicateExample() {
        System.out.println("=== Predicate<T> ===");

        // Predicate: takes T, returns boolean
        Predicate<String> isEmpty = s -> s.isEmpty();
        Predicate<String> isEmptyRef = String::isEmpty;

        System.out.println("Is empty '': " + isEmpty.test(""));
        System.out.println("Is empty 'hi': " + isEmpty.test("hi"));
        System.out.println("Is empty 'hi' with ref: " + isEmptyRef.test("hi"));

        // Combine predicates
        Predicate<String> isShort = s -> s.length() < 5;
        Predicate<String> startsWithA = s -> s.startsWith("a");

        Predicate<String> shortAndStartsA = isShort.and(startsWithA);
        Predicate<String> shortOrStartsA = isShort.or(startsWithA);
        Predicate<String> notShort = isShort.negate();

        System.out.println("'apple' short AND starts-A: " + shortAndStartsA.test("apple"));
        System.out.println("'apple' short OR starts-A: " + shortOrStartsA.test("apple"));
        System.out.println("'hi' NOT short: " + notShort.test("hi"));

        // Use with streams
        List<String> words = List.of("apple", "banana", "ant", "avocado");
        List<String> filtered = words.stream().filter(startsWithA.and(isShort)).toList();
        System.out.println("Short words starting with 'a': " + filtered);

        // Predicate.not() - static helper (Java 11+)
        List<String> nonEmpty =
                List.of("a", "", "b", "").stream().filter(Predicate.not(String::isEmpty)).toList();
        System.out.println("Non-empty: " + nonEmpty);

        // Predicate.isEqual() - static helper
        List<Integer> eqTo10 = Stream.of(1, 4, 10, 20, 11).filter(Predicate.isEqual(10)).toList();
        System.out.println("Equal to 10: " + eqTo10);

        System.out.println();
    }

    static void consumerExample() {
        System.out.println("=== Consumer<T> ===");

        // Consumer: takes T, returns void (side effect)
        Consumer<String> print = s -> System.out.println("  " + s);
        Consumer<String> printRef = System.out::println;

        print.accept("Hello");
        printRef.accept("Hello world");

        // Chain with andThen
        Consumer<String> printUpper = s -> System.out.println("  Upper: " + s.toUpperCase());
        Consumer<String> printBoth = print.andThen(printUpper);

        printBoth.accept("world");

        // Common use: forEach
        List.of("a", "b", "c").forEach(print);

        System.out.println();
    }

    static void supplierExample() {
        System.out.println("=== Supplier<T> ===");

        // Supplier: takes nothing, returns T
        Supplier<Double> random = () -> Math.random();
        Supplier<Double> randomRef = Math::random;

        System.out.println("Random: " + random.get());
        System.out.println("Random: " + random.get());
        System.out.println("Random with ref: " + randomRef.get());

        // Lazy initialization
        Supplier<List<String>> heavyComputation =
                () -> {
                    System.out.println("  Computing...");
                    return List.of("computed", "result");
                };

        System.out.println("Supplier created, not yet called");
        System.out.println("Calling get(): " + heavyComputation.get());

        // Common use: orElseGet
        String value = Optional.<String>empty().orElseGet(() -> "default");
        System.out.println("orElseGet: " + value);

        System.out.println();
    }

    static void composingFunctions() {
        System.out.println("=== Composing Functions ===");

        // Build a pipeline
        Function<String, String> pipeline =
                ((Function<String, String>) String::trim)
                        .andThen(String::toLowerCase)
                        .andThen(s -> s.replace(" ", "_"));

        String result = pipeline.apply("  Hello World  ");
        System.out.println("Pipeline result: " + result);

        // Identity function
        Function<String, String> identity = Function.identity();
        System.out.println("Identity: " + identity.apply("same"));

        // Combine predicates for validation
        Predicate<String> notNull = s -> s != null;
        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> notBlank = s -> !s.isBlank();
        Predicate<String> validLength = s -> s.length() >= 3 && s.length() <= 50;

        Predicate<String> isValidUsername = notNull.and(notEmpty).and(notBlank).and(validLength);

        System.out.println("'bob' valid: " + isValidUsername.test("bob"));
        System.out.println("'ab' valid: " + isValidUsername.test("ab"));
        System.out.println("'' valid: " + isValidUsername.test(""));

        System.out.println();
    }

    static void customFunctionalInterface() {
        System.out.println("=== Custom Functional Interface ===");

        // Define custom functional interface
        @FunctionalInterface
        interface Validator<T> {
            boolean validate(T value);

            // Can have default methods
            default Validator<T> and(Validator<T> other) {
                return value -> this.validate(value) && other.validate(value);
            }

            // Can have static methods
            static <T> Validator<T> notNull() {
                return Objects::nonNull;
            }
        }

        // Use custom interface
        Validator<String> lengthCheck = s -> s.length() > 3;
        Validator<String> combined = Validator.<String>notNull().and(lengthCheck);

        System.out.println("'hello' valid: " + combined.validate("hello"));
        System.out.println("'hi' valid: " + combined.validate("hi"));

        // TriFunction doesn't exist in JDK - create your own
        @FunctionalInterface
        interface TriFunction<A, B, C, R> {
            R apply(A a, B b, C c);
        }

        TriFunction<Integer, Integer, Integer, Integer> sum3 = (a, b, c) -> a + b + c;
        System.out.println("Sum of 1,2,3: " + sum3.apply(1, 2, 3));

        System.out.println();
    }

    static void primitiveVariants() {
        System.out.println("=== Primitive Variants (Avoid Boxing) ===");

        // IntFunction, LongFunction, DoubleFunction: primitive → R
        IntFunction<String> intToString = i -> "Number: " + i;
        System.out.println(intToString.apply(42));

        // ToIntFunction, ToLongFunction, ToDoubleFunction: T → primitive
        ToIntFunction<String> stringLength = String::length;
        System.out.println("Length: " + stringLength.applyAsInt("hello"));

        // IntPredicate, LongPredicate, DoublePredicate
        IntPredicate isEven = n -> n % 2 == 0;
        System.out.println("4 is even: " + isEven.test(4));

        // IntConsumer, LongConsumer, DoubleConsumer
        IntConsumer printInt = n -> System.out.println("Int: " + n);
        printInt.accept(100);

        // IntSupplier, LongSupplier, DoubleSupplier
        IntSupplier randomInt = () -> (int) (Math.random() * 100);
        System.out.println("Random int: " + randomInt.getAsInt());

        // IntUnaryOperator, IntBinaryOperator
        IntUnaryOperator square = n -> n * n;
        IntBinaryOperator add = (a, b) -> a + b;
        System.out.println("Square of 5: " + square.applyAsInt(5));
        System.out.println("3 + 4: " + add.applyAsInt(3, 4));
    }

    static void curryingAndPartialFunctions() {
        System.out.println("=== Currying And Partial Functions ===");

        // Currying: transform multi-arg function into chain of single-arg functions
        Function<Integer, Function<Integer, Integer>> curriedAdd = a -> b -> a + b;
        Function<Integer, Integer> add5 = curriedAdd.apply(5);
        int result = add5.apply(6);   // 11
        System.out.println("result from curriedAdd: " + result);

        // Configuration formatter
        Function<String, Function<LocalDate, String>> dateFormatter =
                pattern -> date -> date.format(DateTimeFormatter.ofPattern(pattern));

        Function<LocalDate, String> isoFormatter = dateFormatter.apply("yyyy-MM-dd");
        Function<LocalDate, String> auFormatter = dateFormatter.apply("dd/MM/yyyy");
        System.out.println("isoFormatter: " + isoFormatter.apply(LocalDate.parse("2020-02-02")));
        System.out.println("auFormatter: " + auFormatter.apply(LocalDate.parse("2020-02-02")));

        BiFunction<String, String, String> greet = (greeting, name) -> greeting + ", " + name + "!";
        Function<String, String> sayHello = partial(greet, "Hello");
        Function<String, String> sayGoodbye = partial(greet, "Goodbye");
        System.out.println("sayHello: " + sayHello.apply("Hello"));
        System.out.println("sayGoodbye: " + sayGoodbye.apply("Goodbye"));
    }
    static <T, U, R> Function<U, R> partial(BiFunction<T, U, R> biFunc, T firstArg) {
        return u -> biFunc.apply(firstArg, u);
    }
}
