package org.nkcoder.jvm;

/**
 * Demonstrates "False Sharing" (Cache Line Contention).
 *
 * <p>
 * Concept: CPU caches are organized in lines (usually 64 bytes). When a core reads a memory
 * location, it fetches the entire cache line. If two independent variables (e.g., fields of an
 * object) sit on the same cache line and are updated by different cores, the cores will fight for
 * ownership of that cache line (Cache Coherency Protocols like MESI). This causes severe
 * performance degradation, known as "False Sharing".
 *
 * <p>
 * This example compares: 1. Padded structure (preventing false sharing by spacing out fields) 2.
 * Unpadded structure (suffering from false sharing)
 *
 * <p>
 * To run: {@code java org.nkcoder.jvm.FalseSharingExample}
 */
public class FalseSharingExample {

  public static void main(String[] args) throws InterruptedException {
    System.out.println("=== False Sharing Demo ===");
    System.out.println("Running benchmark (this may take a few seconds)...");

    testPadding(false); // Unpadded
    testPadding(true); // Padded
  }

  private static void testPadding(boolean usePadding) throws InterruptedException {
    Data data = usePadding ? new PaddedData() : new UnpaddedData();

    Thread t1 = new Thread(() -> work(data, 0));
    Thread t2 = new Thread(() -> work(data, 1));

    long start = System.nanoTime();
    t1.start();
    t2.start();
    t1.join();
    t2.join();
    long duration = System.nanoTime() - start;

    System.out.printf("%-10s: %d ms%n", (usePadding ? "Padded" : "Unpadded"), duration / 1_000_000);
  }

  private static void work(Data data, int threadIndex) {
    long iterations = 100_000_000L;
    if (threadIndex == 0) {
      for (long i = 0; i < iterations; i++) {
        data.counter1++;
      }
    } else {
      for (long i = 0; i < iterations; i++) {
        data.counter2++;
      }
    }
  }

  // Base class for data structure
  abstract static class Data {
    volatile long counter1 = 0;
    volatile long counter2 = 0;
  }

  // Fields are likely to be next to each other in memory -> Same cache line
  static class UnpaddedData extends Data {
  }

  // @Contended (Java 8+) is the official way, but requires -XX:-RestrictContended
  // Here we use manual padding (longs are 8 bytes, cache line is 64 bytes)
  static class PaddedData extends Data {
    volatile long counter1 = 0;
    // Padding to ensure counter2 is on a different cache line
    long p1, p2, p3, p4, p5, p6, p7;
    volatile long counter2 = 0;
  }
}
