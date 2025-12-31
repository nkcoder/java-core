package org.nkcoder.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parallel streams: leverage multiple CPU cores for data processing.
 *
 * <ul>
 *   <li>Use {@code parallelStream()} or {@code stream().parallel()}
 *   <li>Uses ForkJoinPool.commonPool() by default
 *   <li>Best for CPU-intensive, independent operations on large datasets
 *   <li>Avoid: shared mutable state, I/O operations, small datasets
 * </ul>
 */
public class ParallelStreamExample {

    public static void main(String[] args) {
        basicParallelStream();
        parallelVsSequential();
        orderingBehavior();
        customThreadPool();
        whenToUseParallel();
        commonPitfalls();
    }

    static void basicParallelStream() {
        System.out.println("=== Basic Parallel Stream ===");

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        // Two ways to create parallel stream
        Stream<Integer> parallel1 = numbers.parallelStream();
        Stream<Integer> parallel2 = numbers.stream().parallel();

        // Check if parallel
        System.out.println(
                "Is parallel, parallel1: " + parallel1.isParallel() + ", parallel2: " + parallel2.isParallel());

        // Convert back to sequential
        Stream<Integer> sequential = numbers.parallelStream().sequential();
        System.out.println("Is sequential: " + !sequential.isParallel());

        // Process in parallel - notice thread names
        System.out.println("Processing in parallel:");
        numbers.parallelStream()
                .forEach(n -> System.out.println(
                        "  " + n + " on " + Thread.currentThread().getName()));

        System.out.println();
    }

    static void parallelVsSequential() {
        System.out.println("=== Performance Comparison ===");

        int size = 100_000_000;

        // Sequential
        long startSeq = System.currentTimeMillis();
        long sumSeq =
                IntStream.range(0, size).mapToLong(i -> (long) Math.sqrt(i)).sum();
        long timeSeq = System.currentTimeMillis() - startSeq;

        // Parallel
        long startPar = System.currentTimeMillis();
        long sumPar = IntStream.range(0, size)
                .parallel()
                .mapToLong(i -> (long) Math.sqrt(i))
                .sum();
        long timePar = System.currentTimeMillis() - startPar;

        System.out.println("Sequential: " + timeSeq + "ms, sum=" + sumSeq);
        System.out.println("Parallel: " + timePar + "ms, sum=" + sumPar);
        System.out.println("Speedup: " + (double) timeSeq / timePar + "x");

        System.out.println();
    }

    /**
     * Parallel streams track the source's encounter order and enforce it when the terminal operation requires ordering.
     *
     * <p>Encounter order (a.k.a. stream order) is the well-defined sequence in which a stream's source presents
     * elements. An ordered stream preserves that sequence through the pipeline unless you make it unordered.
     *
     * <pre>
     *   - Ordered sources: arrays, ArrayList, IntStream.range, LinkedHashSet etc.
     *   - Unordered sources: HashSet, ConcurrentHashMap.keySet() etc.
     * </pre>
     *
     * <p>How it affects operations:
     *
     * <pre>
     * - forEach: may ignore order on parallel streams.
     * - forEachOrdered: enforces encounter order, even in parallel (with coordination overhead).
     * - Collectors like .toList() on an ordered stream preserve encounter order; CONCURRENT collectors with unordered streams may not.
     * - findFirst: returns the first element in encounter order; findAny may return any match (faster in parallel).
     * </pre>
     */
    static void orderingBehavior() {
        System.out.println("=== Ordering Behavior ===");

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        // forEach - order NOT guaranteed in parallel
        System.out.print("forEach (unordered): ");
        numbers.parallelStream().forEach(n -> System.out.print(n + " "));
        System.out.println();

        // forEachOrdered - maintains encounter order (slower)
        System.out.print("forEachOrdered: ");
        numbers.parallelStream().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // Collector maintains order for ordered sources
        List<Integer> doubled = numbers.parallelStream().map(n -> n * 2).toList();
        System.out.println("Collected (ordered): " + doubled);

        // findFirst vs findAny
        var first = numbers.parallelStream().filter(n -> n > 3).findFirst();
        var any = numbers.parallelStream().filter(n -> n > 3).findAny();
        System.out.println("findFirst: " + first.orElse(-1)); // Always 4
        System.out.println("findAny: " + any.orElse(-1)); // Could be 4, 5, 6...

        System.out.println();
    }

    static void customThreadPool() {
        System.out.println("=== Custom Thread Pool ===");

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        // Default uses ForkJoinPool.commonPool()
        System.out.println(
                "Common pool parallelism: " + ForkJoinPool.commonPool().getParallelism());

        // Custom ForkJoinPool for isolation
        ForkJoinPool customPool = new ForkJoinPool(2); // Only 2 threads

        try {
            List<Integer> result = customPool
                    .submit(() -> numbers.parallelStream()
                            .map(n -> {
                                System.out.println("  " + n + " on "
                                        + Thread.currentThread().getName());
                                return n * 2;
                            })
                            .toList())
                    .get();

            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            customPool.shutdown();
        }

        System.out.println();
    }

    static void whenToUseParallel() {
        System.out.println("=== When to Use Parallel ===");

        System.out.println("""
          GOOD candidates for parallel:
          - Large datasets (10,000+ elements)
          - CPU-intensive operations (math, parsing)
          - Independent operations (no shared state)
          - Splittable sources (ArrayList, arrays, IntStream.range)

          BAD candidates for parallel:
          - Small datasets (overhead > benefit)
          - I/O operations (network, file)
          - LinkedList, Stream.iterate (poor splitting)
          - Operations with side effects
          - When order matters (use forEachOrdered, but slower)
          """);

        // Example: Small dataset - parallel is SLOWER
        List<Integer> small = List.of(1, 2, 3, 4, 5);

        long seqTime = timeIt(() -> small.stream().map(n -> n * 2).toList());
        long parTime = timeIt(() -> small.parallelStream().map(n -> n * 2).toList());

        System.out.println("Small dataset - Sequential: " + seqTime + "ns, Parallel: " + parTime + "ns");
    }

    static void commonPitfalls() {
        System.out.println("\n=== Common Pitfalls ===");

        // WRONG: Shared mutable state
        System.out.println("Pitfall 1: Shared mutable state");
        List<Integer> numbers = IntStream.range(0, 1000).boxed().toList();

        // This is WRONG - race condition!
        var wrongList = new ArrayList<Integer>();
        try {
            numbers.parallelStream().forEach(wrongList::add);
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        System.out.println("  Wrong size (race condition): " + wrongList.size() + " (expected 1000)");

        // Correct: use collect
        var correctList = numbers.parallelStream().toList();
        System.out.println("  Correct size (collect): " + correctList.size());

        // WRONG: Stateful lambda
        System.out.println("\nPitfall 2: Stateful lambda");
        // Don't do this: int[] counter = {0}; stream.peek(x -> counter[0]++);

        // WRONG: Blocking operations
        System.out.println("\nPitfall 3: Blocking operations in common pool");
        System.out.println("  Avoid I/O in parallel streams - blocks common pool threads");
        System.out.println("  Use custom ForkJoinPool or virtual threads instead");
    }

    static long timeIt(Runnable task) {
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            task.run();
        }
        return (System.nanoTime() - start) / 1000;
    }
}
