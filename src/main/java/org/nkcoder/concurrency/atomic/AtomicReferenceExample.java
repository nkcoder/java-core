package org.nkcoder.concurrency.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * AtomicReference: Lock-free thread-safe object reference operations.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>CAS operations on object references</li>
 *   <li>Building lock-free data structures</li>
 *   <li>ABA problem and solutions (stamped/markable references)</li>
 *   <li>Immutable objects work best with atomic references</li>
 * </ul>
 *
 * <p>Interview tip: Understand the ABA problem and when to use
 * AtomicStampedReference vs AtomicMarkableReference.
 */
public class AtomicReferenceExample {

  public static void main(String[] args) throws Exception {
    basicOperations();
    compareAndSetForObjects();
    updateAndAccumulate();
    abaProblem();
    atomicStampedReference();
    atomicMarkableReference();
    lockFreeStack();
    bestPractices();
  }

  static void basicOperations() {
    System.out.println("=== Basic Operations ===");

    record User(String name, int age) {}

    AtomicReference<User> userRef = new AtomicReference<>(new User("Alice", 30));

    // get() - read current reference
    System.out.println("  get(): " + userRef.get());

    // set() - write new reference
    userRef.set(new User("Bob", 25));
    System.out.println("  set(Bob): " + userRef.get());

    // getAndSet() - atomically swap
    User old = userRef.getAndSet(new User("Charlie", 35));
    System.out.println("  getAndSet(): old=" + old.name() + ", new=" + userRef.get().name());

    System.out.println();
  }

  static void compareAndSetForObjects() {
    System.out.println("=== Compare-And-Set for Objects ===");

    record Config(String env, int timeout) {}

    AtomicReference<Config> configRef = new AtomicReference<>(
        new Config("dev", 1000)
    );

    Config current = configRef.get();
    Config updated = new Config("prod", 5000);

    // CAS succeeds - reference matches
    boolean success = configRef.compareAndSet(current, updated);
    System.out.println("  CAS(current, prod): " + success);
    System.out.println("  Config: " + configRef.get());

    // CAS fails - reference doesn't match (even with equal content!)
    Config anotherDev = new Config("dev", 1000); // Same content, different object
    boolean failed = configRef.compareAndSet(anotherDev, new Config("staging", 2000));
    System.out.println("  CAS with equal but different object: " + failed);

    System.out.println("""

        IMPORTANT: CAS compares references (==), not equals()!
        Two objects with same content are different references.

        This is why immutable objects work well:
        - You never modify, you replace with new object
        - Reference identity is what you want to compare
        """);
  }

  static void updateAndAccumulate() {
    System.out.println("=== Update and Accumulate ===");

    record Counter(int value) {}

    AtomicReference<Counter> ref = new AtomicReference<>(new Counter(0));

    // updateAndGet - transform atomically
    Counter result = ref.updateAndGet(c -> new Counter(c.value() + 1));
    System.out.println("  updateAndGet (+1): " + result);

    // getAndUpdate - return old, then transform
    Counter old = ref.getAndUpdate(c -> new Counter(c.value() * 2));
    System.out.println("  getAndUpdate (*2): old=" + old + ", new=" + ref.get());

    // accumulateAndGet - combine with another value
    Counter combined = ref.accumulateAndGet(
        new Counter(10),
        (current, given) -> new Counter(current.value() + given.value())
    );
    System.out.println("  accumulateAndGet (+10): " + combined);

    System.out.println();
  }

  static void abaProblem() {
    System.out.println("=== The ABA Problem ===");

    System.out.println("""
        The ABA Problem:

        Thread 1:                    Thread 2:
        ---------                    ---------
        1. Read A
        2. (suspended)               3. Change A -> B
                                     4. Change B -> A
        5. CAS(A, C) succeeds!
           But the value WAS changed!

        Example scenario:
        - Lock-free stack with nodes [A -> B -> C]
        - Thread 1 wants to pop A, reads A.next = B
        - Thread 2 pops A, pops B, pushes A back
        - Thread 1's CAS sees A, succeeds
        - But A.next is now wrong (was B, stack changed)!

        Solutions:
        1. AtomicStampedReference - version counter
        2. AtomicMarkableReference - boolean flag
        3. Hazard pointers (advanced)
        """);
  }

  static void atomicStampedReference() {
    System.out.println("=== AtomicStampedReference (ABA Solution) ===");

    // Wraps reference + integer stamp (version number)
    AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

    // Get both value and stamp
    int[] stampHolder = new int[1];
    String value = ref.get(stampHolder);
    int stamp = stampHolder[0];
    System.out.println("  Initial: value=" + value + ", stamp=" + stamp);

    // Update with stamp check - must match both!
    boolean success = ref.compareAndSet("A", "B", 0, 1);
    System.out.println("  CAS(A->B, stamp 0->1): " + success);

    value = ref.get(stampHolder);
    System.out.println("  After: value=" + value + ", stamp=" + stampHolder[0]);

    // Simulate ABA
    ref.set("A", 2); // Back to A, but stamp is different

    // This would succeed with regular AtomicReference, but fails here
    boolean abaFailed = ref.compareAndSet("A", "C", 0, 3); // Old stamp!
    System.out.println("  CAS with old stamp (0): " + abaFailed);

    boolean abaSuccess = ref.compareAndSet("A", "C", 2, 3); // Current stamp
    System.out.println("  CAS with current stamp (2): " + abaSuccess);

    System.out.println();
  }

  static void atomicMarkableReference() {
    System.out.println("=== AtomicMarkableReference ===");

    // Wraps reference + boolean mark
    // Useful for: marked-for-deletion, logical deletion
    record Node(int value) {}

    AtomicMarkableReference<Node> ref = new AtomicMarkableReference<>(
        new Node(42), false
    );

    // Get value and mark
    boolean[] markHolder = new boolean[1];
    Node node = ref.get(markHolder);
    System.out.println("  Initial: " + node + ", marked=" + markHolder[0]);

    // Mark for deletion (logically delete)
    ref.set(node, true);
    ref.get(markHolder);
    System.out.println("  After marking: marked=" + markHolder[0]);

    // CAS only if not marked
    Node newNode = new Node(100);
    boolean success = ref.compareAndSet(node, newNode, false, false);
    System.out.println("  CAS (expecting not marked): " + success);

    // attemptMark - atomically set mark if reference matches
    ref.set(newNode, false);
    boolean marked = ref.attemptMark(newNode, true);
    System.out.println("  attemptMark: " + marked);

    System.out.println("""

        Use cases for AtomicMarkableReference:
        - Lazy deletion in lock-free lists
        - Two-phase operations (mark then remove)
        - Logical vs physical deletion

        AtomicStampedReference vs AtomicMarkableReference:
        - Stamped: Need to track version/counter (ABA)
        - Markable: Need simple on/off flag
        """);
  }

  static void lockFreeStack() throws Exception {
    System.out.println("=== Lock-Free Stack Example ===");

    // Simple lock-free stack using AtomicReference
    class LockFreeStack<T> {
      private static class Node<T> {
        final T value;
        Node<T> next;

        Node(T value) { this.value = value; }
      }

      private final AtomicReference<Node<T>> top = new AtomicReference<>();

      void push(T value) {
        Node<T> newNode = new Node<>(value);
        Node<T> oldTop;
        do {
          oldTop = top.get();
          newNode.next = oldTop;
        } while (!top.compareAndSet(oldTop, newNode));
      }

      T pop() {
        Node<T> oldTop;
        Node<T> newTop;
        do {
          oldTop = top.get();
          if (oldTop == null) return null;
          newTop = oldTop.next;
        } while (!top.compareAndSet(oldTop, newTop));
        return oldTop.value;
      }
    }

    LockFreeStack<Integer> stack = new LockFreeStack<>();

    // Concurrent pushes
    try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
      for (int i = 0; i < 100; i++) {
        int value = i;
        executor.submit(() -> stack.push(value));
      }
    }

    // Pop all
    int count = 0;
    while (stack.pop() != null) count++;
    System.out.println("  Pushed 100, popped: " + count);

    System.out.println("""

        Lock-free algorithms:
        - Use CAS loops instead of locks
        - Retry on contention (optimistic)
        - No deadlocks possible
        - Can have live-lock (unlikely in practice)

        Note: This simple stack has ABA vulnerability.
        Production code should use AtomicStampedReference
        or java.util.concurrent classes.
        """);
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        DO:
        - Use with immutable objects (replace, don't mutate)
        - Use updateAndGet for transform operations
        - Consider ABA problem for lock-free structures
        - Use AtomicStampedReference when ABA matters

        DON'T:
        - Mutate objects held by AtomicReference
        - Assume CAS compares by equals() (it uses ==)
        - Forget that multiple CAS operations aren't atomic together

        When to use each:
        +---------------------------+-------------------------------+
        | Class                     | Use case                      |
        +---------------------------+-------------------------------+
        | AtomicReference<T>        | Simple reference swap         |
        | AtomicStampedReference<T> | Prevent ABA (version counter) |
        | AtomicMarkableReference<T>| Two-state flag (deleted/not)  |
        +---------------------------+-------------------------------+

        Related classes:
        - AtomicReferenceArray<T> - atomic array of references
        - AtomicReferenceFieldUpdater - atomic updates to volatile fields
        - VarHandle (Java 9+) - more flexible atomic operations
        """);
  }
}
