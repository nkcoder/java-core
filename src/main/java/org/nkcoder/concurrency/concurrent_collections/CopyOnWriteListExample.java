package org.nkcoder.concurrency.concurrent_collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CopyOnWriteArrayList/Set: Thread-safe collections optimized for read-heavy workloads.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Every write creates a new copy of the underlying array</li>
 *   <li>Reads never block (no synchronization)</li>
 *   <li>Iterators see a snapshot (never throw ConcurrentModificationException)</li>
 *   <li>Best for: many reads, few writes, small to medium size</li>
 * </ul>
 *
 * <p>Interview tip: Know when to use CopyOnWrite vs other concurrent collections,
 * and understand the memory/performance tradeoffs.
 */
public class CopyOnWriteListExample {

  static void main(String[] args) throws Exception {
    howItWorks();
    basicOperations();
    iteratorBehavior();
    copyOnWriteArraySet();
    performanceCharacteristics();
    realWorldUseCases();
    bestPractices();
  }

  static void howItWorks() {
    System.out.println("=== How Copy-On-Write Works ===");

    System.out.println("""
        Copy-On-Write strategy:

        Read operations:
        - No locking needed
        - Direct array access
        - Always consistent (see complete elements)

        Write operations:
        1. Acquire lock
        2. Copy entire internal array
        3. Modify the copy
        4. Replace internal array reference
        5. Release lock

        Result:
        - Readers see either old or new state (never partial)
        - Multiple readers can read simultaneously
        - Writers block other writers (but not readers)
        - Memory cost: O(n) per write operation
        """);
  }

  static void basicOperations() {
    System.out.println("=== Basic Operations ===");

    CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

    // Add operations (create copies)
    list.add("one");
    list.add("two");
    list.add("three");
    System.out.println("  After adds: " + list);

    // addIfAbsent - atomic check-and-add
    boolean added1 = list.addIfAbsent("two");   // Already exists
    boolean added2 = list.addIfAbsent("four");  // New
    System.out.println("  addIfAbsent(\"two\"): " + added1);
    System.out.println("  addIfAbsent(\"four\"): " + added2);
    System.out.println("  List: " + list);

    // addAllAbsent - add all that don't exist
    int count = list.addAllAbsent(List.of("five", "two", "six"));
    System.out.println("  addAllAbsent([five, two, six]): added " + count);
    System.out.println("  List: " + list);

    // Read operations (no copying)
    System.out.println("  get(0): " + list.get(0));
    System.out.println("  size(): " + list.size());
    System.out.println("  contains(\"three\"): " + list.contains("three"));

    // Remove (creates copy)
    list.remove("two");
    System.out.println("  After remove(\"two\"): " + list);

    System.out.println();
  }

  static void iteratorBehavior() throws Exception {
    System.out.println("=== Iterator Behavior (Snapshot) ===");

    CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>(List.of(1, 2, 3, 4, 5));

    // Iterator sees snapshot at creation time
    Iterator<Integer> iterator = list.iterator();

    // Modify list during iteration
    list.add(6);
    list.remove(Integer.valueOf(1));

    System.out.println("  Current list: " + list);
    System.out.print("  Iterator sees: ");
    while (iterator.hasNext()) {
      System.out.print(iterator.next() + " ");
    }
    System.out.println("(snapshot from before modification)");

    // No ConcurrentModificationException!
    System.out.println("\n  Safe iteration during modification:");
    CopyOnWriteArrayList<String> names = new CopyOnWriteArrayList<>(
        List.of("Alice", "Bob", "Charlie")
    );

    for (String name : names) {
      System.out.println("    Processing: " + name);
      if (name.equals("Bob")) {
        names.add("Diana"); // Safe! Won't affect this iteration
      }
    }
    System.out.println("  Final list: " + names);

    // Compare with synchronized list (throws CME)
    System.out.println("\n  synchronizedList would throw ConcurrentModificationException");

    System.out.println("""

        Iterator properties:
        - Snapshot semantics: sees state at iterator creation
        - Never throws ConcurrentModificationException
        - remove() on iterator NOT supported (throws UnsupportedOperationException)
        - Safe for concurrent iteration and modification
        """);
  }

  static void copyOnWriteArraySet() {
    System.out.println("=== CopyOnWriteArraySet ===");

    CopyOnWriteArraySet<String> set = new CopyOnWriteArraySet<>();

    // Unique elements only
    set.add("apple");
    set.add("banana");
    set.add("apple"); // Duplicate, not added

    System.out.println("  Set: " + set);
    System.out.println("  Size: " + set.size());

    // Backed by CopyOnWriteArrayList internally
    // Contains check is O(n), not O(1) like HashSet!

    // Useful for small sets of listeners
    Set<Runnable> listeners = new CopyOnWriteArraySet<>();
    listeners.add(() -> System.out.println("    Listener 1 notified"));
    listeners.add(() -> System.out.println("    Listener 2 notified"));

    System.out.println("  Notifying listeners:");
    for (Runnable listener : listeners) {
      listener.run();
    }

    System.out.println("""

        CopyOnWriteArraySet:
        - Uses CopyOnWriteArrayList internally
        - contains() is O(n), not O(1)
        - Best for small sets (dozens of elements)
        - Common use: listener/observer collections
        """);
  }

  static void performanceCharacteristics() throws Exception {
    System.out.println("=== Performance Characteristics ===");

    int size = 1000;
    int iterations = 10000;

    // CopyOnWrite - reads
    CopyOnWriteArrayList<Integer> cowList = new CopyOnWriteArrayList<>();
    for (int i = 0; i < size; i++) cowList.add(i);

    long start1 = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      cowList.get(i % size);
    }
    long cowReadTime = System.nanoTime() - start1;

    // Synchronized list - reads
    List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
    for (int i = 0; i < size; i++) syncList.add(i);

    long start2 = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      syncList.get(i % size);
    }
    long syncReadTime = System.nanoTime() - start2;

    System.out.println("  Read performance (" + iterations + " reads):");
    System.out.println("    CopyOnWriteArrayList: " + cowReadTime / 1_000_000 + "ms");
    System.out.println("    synchronizedList: " + syncReadTime / 1_000_000 + "ms");

    // Write performance (COW is much slower)
    CopyOnWriteArrayList<Integer> cowWrite = new CopyOnWriteArrayList<>();
    List<Integer> syncWrite = Collections.synchronizedList(new ArrayList<>());

    int writeCount = 1000;

    long start3 = System.nanoTime();
    for (int i = 0; i < writeCount; i++) {
      cowWrite.add(i);
    }
    long cowWriteTime = System.nanoTime() - start3;

    long start4 = System.nanoTime();
    for (int i = 0; i < writeCount; i++) {
      syncWrite.add(i);
    }
    long syncWriteTime = System.nanoTime() - start4;

    System.out.println("\n  Write performance (" + writeCount + " adds):");
    System.out.println("    CopyOnWriteArrayList: " + cowWriteTime / 1_000_000 + "ms");
    System.out.println("    synchronizedList: " + syncWriteTime / 1_000_000 + "ms");

    System.out.println("""

        Performance summary:
        +-------------------+------------------+---------------------+
        | Operation         | CopyOnWrite      | synchronizedList    |
        +-------------------+------------------+---------------------+
        | Read              | O(1), no lock    | O(1), requires lock |
        | Write             | O(n), copies all | O(1), requires lock |
        | Iteration         | Snapshot, fast   | Needs external lock |
        | Memory            | 2x during write  | Constant            |
        +-------------------+------------------+---------------------+

        Rule of thumb:
        - Read/Write ratio > 100:1 -> CopyOnWrite might be good
        - Frequent writes -> Use other collections
        - Large collections -> Avoid CopyOnWrite
        """);
  }

  static void realWorldUseCases() throws Exception {
    System.out.println("=== Real-World Use Cases ===");

    // 1. Event listeners (read-heavy, occasional add/remove)
    System.out.println("  1. Event Listeners:");

    interface EventListener {
      void onEvent(String event);
    }

    class EventBus {
      private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

      void addListener(EventListener listener) {
        listeners.add(listener);
      }

      void removeListener(EventListener listener) {
        listeners.remove(listener);
      }

      void fireEvent(String event) {
        // Safe iteration - no locking needed
        for (EventListener listener : listeners) {
          listener.onEvent(event);
        }
      }
    }

    EventBus bus = new EventBus();
    bus.addListener(e -> System.out.println("      Listener A: " + e));
    bus.addListener(e -> System.out.println("      Listener B: " + e));
    bus.fireEvent("UserLoggedIn");

    // 2. Configuration/feature flags (rarely changes)
    System.out.println("\n  2. Feature Flags:");

    CopyOnWriteArraySet<String> enabledFeatures = new CopyOnWriteArraySet<>();
    enabledFeatures.add("dark_mode");
    enabledFeatures.add("new_dashboard");

    // Many reads from different threads
    System.out.println("    dark_mode enabled: " + enabledFeatures.contains("dark_mode"));
    System.out.println("    beta_feature enabled: " + enabledFeatures.contains("beta_feature"));

    // 3. Caching immutable snapshots
    System.out.println("\n  3. Snapshot Cache:");

    class SnapshotCache {
      private volatile CopyOnWriteArrayList<String> cache = new CopyOnWriteArrayList<>();

      List<String> getSnapshot() {
        return cache; // Safe to return - immutable during reads
      }

      void refresh(List<String> newData) {
        cache = new CopyOnWriteArrayList<>(newData);
      }
    }

    SnapshotCache snapshotCache = new SnapshotCache();
    snapshotCache.refresh(List.of("item1", "item2", "item3"));
    System.out.println("    Snapshot: " + snapshotCache.getSnapshot());

    System.out.println();
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        GOOD use cases:
        - Event listeners/observers
        - Configuration lists
        - Caches that rarely change
        - Small collections (< 100 elements typically)
        - Read-to-write ratio > 100:1

        BAD use cases:
        - Frequently modified collections
        - Large collections
        - Write-heavy workloads
        - When you need consistent iteration + modification

        DO:
        - Use addIfAbsent() for unique elements
        - Batch writes when possible (addAll instead of multiple add)
        - Consider alternatives for larger collections

        DON'T:
        - Use iterator.remove() (throws UnsupportedOperationException)
        - Use for write-heavy scenarios
        - Store large objects (copies are expensive)

        Alternatives:
        +---------------------------+--------------------------------+
        | Scenario                  | Better choice                  |
        +---------------------------+--------------------------------+
        | Write-heavy list          | Collections.synchronizedList   |
        | Large concurrent list     | ConcurrentLinkedQueue          |
        | Map with reads & writes   | ConcurrentHashMap              |
        | Queue operations          | ConcurrentLinkedQueue          |
        | Blocking operations       | BlockingQueue implementations  |
        +---------------------------+--------------------------------+
        """);
  }
}
