package org.nkcoder.strings;

/**
 * Text Blocks (Java 15+): Multi-line string literals with proper formatting.
 *
 * <p><strong>Java 25 Status:</strong> Finalized and widely used. Text blocks are the standard way to write multi-line
 * strings, JSON, SQL, HTML in Java.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Triple-quote syntax for multi-line strings
 *   <li>Automatic indentation handling
 *   <li>Escape sequences and special characters
 *   <li>Practical uses: JSON, SQL, HTML, etc.
 * </ul>
 */
public class TextBlockExample {

    static void main(String[] args) {
        basicTextBlock();
        indentationRules();
        escapeSequences();
        practicalExamples();
        textBlockVsString();
    }

    // ===== Basic Text Block =====

    static void basicTextBlock() {
        System.out.println("=== Basic Text Block ===");

        // Old way - painful escaping and concatenation
        String oldWay = "{\n" + "  \"name\": \"Alice\",\n" + "  \"age\": 30\n" + "}";
        System.out.println("  Old way:");
        System.out.println(oldWay);

        // Text block - clean and readable
        String textBlock = """
        {
          "name": "Alice",
          "age": 30
        }
        """;
        System.out.println("\n  Text block:");
        System.out.println(textBlock);

        // Opening """ must be followed by newline
        // Content starts on next line
        // Closing """ position matters for indentation

        System.out.println();
    }

    // ===== Indentation Rules =====

    static void indentationRules() {
        System.out.println("=== Indentation Rules ===");

        // Closing """ at start of line - no incidental indentation removed
        String noIndent = """
Hello
World
""";
        System.out.println("  Closing at column 0:");
        System.out.println(">" + noIndent + "<");

        // Closing """ indented - that much indentation removed from all lines
        String withIndent = """
        Hello
        World
        """;
        System.out.println("  Closing indented (incidental whitespace removed):");
        System.out.println(">" + withIndent + "<");

        // Content more indented than closing - preserves extra indent
        String extraIndent = """
            Line 1
                Line 2 (extra indent)
            Line 3
        """;
        System.out.println("  Preserved intentional indentation:");
        System.out.println(extraIndent);

        // Trailing """ on same line as content - no trailing newline
        String noTrailingNewline = """
        No newline at end""";
        System.out.println("  No trailing newline: \"" + noTrailingNewline + "\"");

        System.out.println("""

        Indentation rules:
        - Position of closing \"\"\" determines incidental whitespace
        - Whitespace to the left of \"\"\" is removed from all lines
        - Extra indentation beyond \"\"\" is preserved
        - Trailing \"\"\" on content line = no trailing newline
        """);
    }

    // ===== Escape Sequences =====

    static void escapeSequences() {
        System.out.println("=== Escape Sequences ===");

        // Standard escapes work
        String escapes = """
        Tab:\there
        Newline: explicit\\n in text
        Quote: "quoted"
        Backslash: \\
        """;
        System.out.println("  Standard escapes:");
        System.out.println(escapes);

        // New escape: \s (space that prevents trailing whitespace trimming)
        String preserveSpace = """
        Line with trailing spaces   \s
        Next line
        """;
        System.out.println("  \\s preserves trailing space:");
        System.out.println(preserveSpace.replace(" ", "·")); // Show spaces

        // New escape: \ at end of line (line continuation, no newline)
        String lineContinuation = """
        This is a very long line that we want to \
        write across multiple source lines \
        but should appear as one line.""";
        System.out.println("  Line continuation (\\):");
        System.out.println(lineContinuation);

        // Triple quotes in content
        String tripleQuote = """
        Text with \""" triple quotes inside
        """;
        System.out.println("  Triple quotes in content:");
        System.out.println(tripleQuote);

        System.out.println();
    }

    // ===== Practical Examples =====

    static void practicalExamples() {
        System.out.println("=== Practical Examples ===");

        // JSON
        String json = """
        {
          "users": [
            {"id": 1, "name": "Alice"},
            {"id": 2, "name": "Bob"}
          ],
          "total": 2
        }
        """;
        System.out.println("  JSON:");
        System.out.println(json);

        // SQL
        String sql = """
        SELECT u.id, u.name, o.total
        FROM users u
        JOIN orders o ON u.id = o.user_id
        WHERE o.status = 'COMPLETED'
          AND o.created_at > :startDate
        ORDER BY o.total DESC
        LIMIT 10
        """;
        System.out.println("  SQL:");
        System.out.println(sql);

        // HTML
        String html = """
        <!DOCTYPE html>
        <html>
          <head>
            <title>Welcome</title>
          </head>
          <body>
            <h1>Hello, World!</h1>
          </body>
        </html>
        """;
        System.out.println("  HTML:");
        System.out.println(html);

        // With String formatting
        String name = "Alice";
        int age = 30;

        // Using formatted() method (Java 15+)
        String formatted = """
        {
          "name": "%s",
          "age": %d
        }
        """.formatted(name, age);
        System.out.println("  With formatted():");
        System.out.println(formatted);

        // Using String.format()
        String template = """
        Dear %s,

        Your order #%d has been shipped.

        Best regards,
        The Team
        """;
        String email = String.format(template, "Bob", 12345);
        System.out.println("  Email template:");
        System.out.println(email);
    }

    // ===== Text Block vs String =====

    static void textBlockVsString() {
        System.out.println("=== Text Block vs Regular String ===");

        // They produce the same result
        String regular = "Line 1\nLine 2\nLine 3\n";
        String textBlock = """
        Line 1
        Line 2
        Line 3
        """;

        System.out.println("  Are they equal? " + regular.equals(textBlock));
        System.out.println("  Regular hashCode: " + regular.hashCode());
        System.out.println("  Text block hashCode: " + textBlock.hashCode());

        // Text blocks are just syntactic sugar - compile to same bytecode
        // Can be used anywhere a String is expected

        System.out.println("""

        When to use text blocks:
        ✓ Multi-line content (JSON, SQL, HTML, XML)
        ✓ Documentation or messages
        ✓ Code samples in comments
        ✓ Regular expressions (avoids double escaping)

        When NOT to use:
        ✗ Single-line strings
        ✗ Strings built dynamically at runtime
        ✗ When exact whitespace control is critical
        """);
    }
}
