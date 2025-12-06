package org.nkcoder.strings;

import java.util.Objects;

/**
 * String Basics: Immutability, String Pool, and Core Concepts.
 *
 * <p><strong>Java 25 Status:</strong> Fundamental concepts unchanged. Understanding
 * string immutability and the string pool is essential for writing efficient code.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>String immutability and its benefits</li>
 *   <li>String pool (interning)</li>
 *   <li>String comparison: == vs equals()</li>
 *   <li>Memory considerations</li>
 * </ul>
 */
public class StringBasicsExample {

 public static void main(String[] args) {
    stringImmutability();
    stringPool();
    equalsVsDoubleEquals();
    internMethod();
    memoryConsiderations();
    commonMistakes();
  }

  // ===== String Immutability =====

  static void stringImmutability() {
    System.out.println("=== String Immutability ===");

    String original = "Hello";
    String modified = original.toUpperCase();

    System.out.println("  Original: " + original);    // Still "Hello"
    System.out.println("  Modified: " + modified);    // "HELLO" - new object

    // Every "modification" creates a new String
    String s = "a";
    System.out.println("\n  String operations create new objects:");
    System.out.println("  s = \"a\"");
    System.out.println("  s.concat(\"b\") returns: " + s.concat("b"));
    System.out.println("  s is still: " + s);

    System.out.println("""

        Why immutability matters:
        1. Thread Safety - strings can be shared without synchronization
        2. Security - string values can't be changed after validation
        3. Caching - hashCode can be cached (used in HashMap keys)
        4. String Pool - safe to share instances

        Consequence:
        - String concatenation in loops creates many objects
        - Use StringBuilder for building strings dynamically
        """);
  }

  // ===== String Pool (String Interning) =====

  static void stringPool() {
    System.out.println("=== String Pool ===");

    // String literals go to the pool
    String a = "Hello";
    String b = "Hello";

    // Same reference (from pool)
    System.out.println("  String a = \"Hello\"");
    System.out.println("  String b = \"Hello\"");
    System.out.println("  a == b: " + (a == b));  // true - same pool reference

    // new String() creates object on heap, not in pool
    String c = new String("Hello");
    System.out.println("\n  String c = new String(\"Hello\")");
    System.out.println("  a == c: " + (a == c));  // false - different objects
    System.out.println("  a.equals(c): " + a.equals(c));  // true - same content

    // Compile-time constant expressions are pooled
    String d = "Hel" + "lo";  // Evaluated at compile time
    System.out.println("\n  String d = \"Hel\" + \"lo\"");
    System.out.println("  a == d: " + (a == d));  // true - compiler optimizes

    // Runtime concatenation is NOT pooled
    String prefix = "Hel";
    String e = prefix + "lo";  // Evaluated at runtime
    System.out.println("\n  String e = prefix + \"lo\" (runtime)");
    System.out.println("  a == e: " + (a == e));  // false - heap object

    System.out.println();
  }

  // ===== equals() vs == =====

  static void equalsVsDoubleEquals() {
    System.out.println("=== equals() vs == ===");

    String s1 = "Hello";
    String s2 = new String("Hello");
    String s3 = "Hello";

    System.out.println("  s1 = \"Hello\" (literal)");
    System.out.println("  s2 = new String(\"Hello\")");
    System.out.println("  s3 = \"Hello\" (literal)");

    System.out.println("\n  Reference comparison (==):");
    System.out.println("    s1 == s2: " + (s1 == s2));  // false
    System.out.println("    s1 == s3: " + (s1 == s3));  // true

    System.out.println("\n  Content comparison (equals):");
    System.out.println("    s1.equals(s2): " + s1.equals(s2));  // true
    System.out.println("    s1.equals(s3): " + s1.equals(s3));  // true

    // Null-safe comparison
    String nullStr = null;
    // nullStr.equals(s1);  // NullPointerException!
    System.out.println("\n  Null-safe patterns:");
    System.out.println("    \"Hello\".equals(nullStr): " + "Hello".equals(nullStr));
    System.out.println("    Objects.equals(nullStr, s1): " +
        Objects.equals(nullStr, s1));

    System.out.println("""

        RULE: Always use equals() for String comparison
        - == compares references (memory addresses)
        - equals() compares content
        - Only use == to check for null
        """);
  }

  // ===== intern() Method =====

  static void internMethod() {
    System.out.println("=== intern() Method ===");

    String a = "Hello";
    String b = new String("Hello");
    String c = b.intern();  // Get pooled version

    System.out.println("  a = \"Hello\" (literal, in pool)");
    System.out.println("  b = new String(\"Hello\") (heap)");
    System.out.println("  c = b.intern() (get pool reference)");

    System.out.println("\n  a == b: " + (a == b));  // false
    System.out.println("  a == c: " + (a == c));  // true - c points to pool

    System.out.println("""

        intern() behavior:
        - If string exists in pool, returns pool reference
        - If not, adds to pool and returns new reference
        - Use sparingly - pool has limited size

        When to use intern():
        - Processing many duplicate strings (e.g., XML parsing)
        - Memory optimization for repeated values
        - Generally: let JVM handle it (literals auto-interned)
        """);
  }

  // ===== Memory Considerations =====

  static void memoryConsiderations() {
    System.out.println("=== Memory Considerations ===");

    // Substring behavior (Java 7+)
    String large = "A".repeat(10000);
    String small = large.substring(0, 10);

    // In Java 7+, substring creates NEW char array (no memory leak)
    System.out.println("  large.length(): " + large.length());
    System.out.println("  small.length(): " + small.length());
    System.out.println("  small: " + small);

    // String memory: header + char array reference + hash + char[]
    // char[] uses 2 bytes per character (UTF-16)
    // Compact strings (Java 9+): Latin-1 strings use 1 byte per char

    System.out.println("""

        Memory facts:
        - Each String object: ~40+ bytes overhead
        - Character storage: 1 byte (Latin-1) or 2 bytes (UTF-16)
        - Java 9+ Compact Strings: auto-detects encoding
        - Substring: creates new String (no shared backing array)

        Optimizations:
        - String pool reduces duplicates
        - Compact strings save ~50% for ASCII
        - StringBuilder for concatenation in loops
        """);
  }

  // ===== Common Mistakes =====

  static void commonMistakes() {
    System.out.println("=== Common Mistakes ===");

    // Mistake 1: Using == for comparison
    System.out.println("  Mistake 1: Using == instead of equals()");
    String input = new String("yes");
    // BAD: if (input == "yes")  // Might fail!
    // GOOD:
    if ("yes".equals(input)) {
      System.out.println("    Correct: \"yes\".equals(input)");
    }

    // Mistake 2: String concatenation in loops
    System.out.println("\n  Mistake 2: Concatenation in loops");
    System.out.println("""
        BAD:
          String result = "";
          for (...) { result += item; }  // Creates many String objects

        GOOD:
          StringBuilder sb = new StringBuilder();
          for (...) { sb.append(item); }
          String result = sb.toString();
        """);

    // Mistake 3: Not handling null
    System.out.println("  Mistake 3: NullPointerException");
    System.out.println("""
        BAD:
          if (userInput.equals("expected"))  // NPE if userInput is null

        GOOD:
          if ("expected".equals(userInput))  // Safe
          if (Objects.equals(userInput, "expected"))  // Also safe
        """);

    // Mistake 4: Forgetting immutability
    System.out.println("  Mistake 4: Forgetting immutability");
    String s = "hello";
    s.toUpperCase();  // Does nothing! Return value ignored
    System.out.println("    After s.toUpperCase(): " + s);  // Still "hello"
    s = s.toUpperCase();  // Must reassign
    System.out.println("    After s = s.toUpperCase(): " + s);

    System.out.println();
  }
}
