package org.nkcoder.strings;

import java.util.stream.Collectors;

/**
 * Modern String Methods (Java 11+): Essential String API methods for modern Java.
 *
 * <p><strong>Java 25 Status:</strong> All methods shown are stable and widely used.
 * These are the go-to String methods for everyday Java development.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Blank checking and whitespace handling</li>
 *   <li>Lines and stream processing</li>
 *   <li>Indentation and transformation</li>
 *   <li>Repetition and stripping</li>
 * </ul>
 */
public class StringMethodsExample {

  static void main(String[] args) {
    blankAndEmpty();
    stripMethods();
    linesMethods();
    indentAndTransform();
    repeatMethod();
    otherUsefulMethods();
  }

  // ===== isBlank() vs isEmpty() =====

  static void blankAndEmpty() {
    System.out.println("=== isBlank() vs isEmpty() (Java 11+) ===");

    String empty = "";
    String blank = "   ";
    String whitespace = " \t\n ";
    String text = "hello";

    System.out.println("  String     | isEmpty() | isBlank()");
    System.out.println("  -----------+-----------+----------");
    System.out.printf("  \"\"         | %-9s | %s%n", empty.isEmpty(), empty.isBlank());
    System.out.printf("  \"   \"      | %-9s | %s%n", blank.isEmpty(), blank.isBlank());
    System.out.printf("  \" \\t\\n \"   | %-9s | %s%n", whitespace.isEmpty(), whitespace.isBlank());
    System.out.printf("  \"hello\"    | %-9s | %s%n", text.isEmpty(), text.isBlank());

    System.out.println("""

        Key difference:
        - isEmpty(): true only if length == 0
        - isBlank(): true if empty OR contains only whitespace
        - Use isBlank() for user input validation (more common)
        """);
  }

  // ===== strip(), stripLeading(), stripTrailing() =====

  static void stripMethods() {
    System.out.println("=== strip() vs trim() (Java 11+) ===");

    // Unicode whitespace that trim() misses
    String text = "\u2000 Hello World \u2000";  // \u2000 = Unicode space
    String tabs = "\t  Hello  \t";

    System.out.println("  Original with Unicode spaces: \"" + text + "\"");
    System.out.println("  trim():     \"" + text.trim() + "\"");
    System.out.println("  strip():    \"" + text.strip() + "\"");

    System.out.println("\n  Tab example: \"" + tabs + "\"");
    System.out.println("  strip():        \"" + tabs.strip() + "\"");
    System.out.println("  stripLeading(): \"" + tabs.stripLeading() + "\"");
    System.out.println("  stripTrailing():\"" + tabs.stripTrailing() + "\"");

    System.out.println("""

        strip() vs trim():
        - trim(): Only removes chars <= U+0020 (ASCII space)
        - strip(): Removes ALL Unicode whitespace (preferred)
        - stripLeading(): Only leading whitespace
        - stripTrailing(): Only trailing whitespace
        """);
  }

  // ===== lines() =====

  static void linesMethods() {
    System.out.println("=== lines() (Java 11+) ===");

    String multiline = """
        First line
        Second line
        Third line
        """;

    // lines() returns Stream<String>
    System.out.println("  Lines as stream:");
    multiline.lines()
        .map(line -> "    > " + line)
        .forEach(System.out::println);

    // Practical: Count non-blank lines
    String withBlanks = "Line 1\n\nLine 2\n   \nLine 3";
    long nonBlankCount = withBlanks.lines()
        .filter(line -> !line.isBlank())
        .count();
    System.out.println("\n  Non-blank line count: " + nonBlankCount);

    // Practical: Process CSV-like data
    String data = "Alice,30\nBob,25\nCharlie,35";
    System.out.println("\n  Processing data:");
    data.lines()
        .map(line -> line.split(","))
        .forEach(parts -> System.out.println("    Name: " + parts[0] + ", Age: " + parts[1]));

    System.out.println();
  }

  // ===== indent() and transform() =====

  static void indentAndTransform() {
    System.out.println("=== indent() and transform() (Java 12+) ===");

    String text = "Line 1\nLine 2\nLine 3";

    // indent(n) adds n spaces to each line
    System.out.println("  Original:");
    System.out.println(text);

    System.out.println("\n  indent(4):");
    System.out.println(text.indent(4));

    // Negative indent removes spaces
    String indented = "    A\n    B\n    C";
    System.out.println("  indent(-2) (removes 2 spaces):");
    System.out.println(indented.indent(-2));

    // transform() applies a function to the string
    String result = "hello world"
        .transform(String::toUpperCase)
        .transform(s -> s.replace(" ", "_"))
        .transform(s -> "[" + s + "]");
    System.out.println("  transform() chaining: " + result);

    // Practical use of transform
    String json = "{\"name\": \"Alice\"}";
    String processed = json.transform(s -> s.isBlank() ? "{}" : s)
        .transform(String::strip);
    System.out.println("  JSON processed: " + processed);

    System.out.println();
  }

  // ===== repeat() =====

  static void repeatMethod() {
    System.out.println("=== repeat() (Java 11+) ===");

    // Basic repetition
    String dash = "-".repeat(40);
    System.out.println("  " + dash);

    String pattern = "ab".repeat(5);
    System.out.println("  \"ab\".repeat(5) = " + pattern);

    // Practical: Padding
    String name = "Alice";
    String padded = " ".repeat(10 - name.length()) + name;
    System.out.println("  Right-aligned: \"" + padded + "\"");

    // Practical: Simple progress bar
    int progress = 7;
    int total = 10;
    String bar = "█".repeat(progress) + "░".repeat(total - progress);
    System.out.println("  Progress: [" + bar + "] " + (progress * 10) + "%");

    // Edge cases
    System.out.println("  \"x\".repeat(0) = \"" + "x".repeat(0) + "\"");

    System.out.println();
  }

  // ===== Other Useful Methods =====

  static void otherUsefulMethods() {
    System.out.println("=== Other Useful Methods ===");

    // chars() and codePoints() - Stream of characters
    String text = "Hello";
    System.out.print("  chars(): ");
    text.chars().forEach(c -> System.out.print((char) c + " "));
    System.out.println();

    // String.join() - Joining strings (Java 8)
    String joined = String.join(", ", "Apple", "Banana", "Cherry");
    System.out.println("  join(): " + joined);

    // Collectors.joining() - From streams
    String collected = java.util.List.of("A", "B", "C").stream()
        .collect(Collectors.joining(" | ", "[", "]"));
    System.out.println("  Collectors.joining(): " + collected);

    // contains(), startsWith(), endsWith()
    String url = "https://example.com/api/users";
    System.out.println("  URL checks:");
    System.out.println("    starts with https: " + url.startsWith("https"));
    System.out.println("    contains api: " + url.contains("api"));
    System.out.println("    ends with users: " + url.endsWith("users"));

    // substring vs subSequence
    String str = "Hello World";
    System.out.println("  substring(0, 5): " + str.substring(0, 5));

    // split with limit
    String csv = "a,b,c,d,e";
    String[] parts = csv.split(",", 3);  // Limit to 3 parts
    System.out.println("  split with limit 3: " + java.util.Arrays.toString(parts));

    // replace vs replaceAll
    String withSpaces = "hello world foo";
    System.out.println("  replace(\" \", \"_\"): " + withSpaces.replace(" ", "_"));
    System.out.println("  replaceAll(\"\\\\s+\", \"_\"): " + withSpaces.replaceAll("\\s+", "_"));

    System.out.println("""

        Method summary (Java 25 recommendations):
        - Blank check: isBlank() (not isEmpty())
        - Whitespace: strip() (not trim())
        - Multi-line: lines() + Stream API
        - Repeat: "x".repeat(n)
        - Join: String.join() or Collectors.joining()
        """);
  }
}
