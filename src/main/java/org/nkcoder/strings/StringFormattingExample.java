package org.nkcoder.strings;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * String Formatting: Various ways to format strings in Java.
 *
 * <p><strong>Java 25 Status:</strong> All formatting methods shown are stable. String.format() and formatted() are the
 * most commonly used approaches.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>String.format() and printf()
 *   <li>formatted() method (Java 15+)
 *   <li>Format specifiers
 *   <li>Locale-aware formatting
 * </ul>
 */
public class StringFormattingExample {

    static void main(String[] args) {
        basicFormatting();
        formatSpecifiers();
        numberFormatting();
        dateTimeFormatting();
        formattedMethod();
        localeFormatting();
        messageFormat();
        bestPractices();
    }

    // ===== Basic Formatting =====

    static void basicFormatting() {
        System.out.println("=== Basic Formatting ===");

        // String.format() - returns formatted String
        String name = "Alice";
        int age = 30;
        String formatted = String.format("Name: %s, Age: %d", name, age);
        System.out.println("  String.format(): " + formatted);

        // System.out.printf() - prints formatted directly
        System.out.printf("  printf(): Name: %s, Age: %d%n", name, age);

        // PrintStream.format() - same as printf
        System.out.format("  format(): Name: %s, Age: %d%n", name, age);

        // Common format specifiers
        System.out.println("""

        Common format specifiers:
        %s - String
        %d - Decimal integer
        %f - Floating point
        %b - Boolean
        %c - Character
        %n - Platform-specific newline
        %% - Literal percent sign
        """);
    }

    // ===== Format Specifiers in Detail =====

    static void formatSpecifiers() {
        System.out.println("=== Format Specifiers ===");

        // Width and alignment
        System.out.println("  Width and alignment:");
        System.out.printf("    |%10s|%n", "right"); // Right-aligned (default)
        System.out.printf("    |%-10s|%n", "left"); // Left-aligned
        System.out.printf("    |%10d|%n", 42); // Number right-aligned
        System.out.printf("    |%-10d|%n", 42); // Number left-aligned

        // Padding with zeros
        System.out.println("\n  Zero padding:");
        System.out.printf("    |%05d|%n", 42); // 00042
        System.out.printf("    |%08.2f|%n", 3.14); // 00003.14

        // Argument index
        System.out.println("\n  Argument index:");
        System.out.printf("    %2$s %1$s%n", "World", "Hello"); // Hello World
        System.out.printf("    %1$s %1$s%n", "Echo"); // Echo Echo

        // Flags
        System.out.println("\n  Flags:");
        System.out.printf("    + flag: %+d, %+d%n", 42, -42); // +42, -42
        System.out.printf("    ( flag: %(d, %(d%n", 42, -42); // 42, (42)
        System.out.printf("    , flag: %,d%n", 1000000); // 1,000,000
        System.out.printf("    # flag: %#x%n", 255); // 0xff

        System.out.println();
    }

    // ===== Number Formatting =====

    static void numberFormatting() {
        System.out.println("=== Number Formatting ===");

        double pi = 3.14159265359;
        double large = 1234567.89;
        int integer = 255;

        // Precision
        System.out.println("  Floating point precision:");
        System.out.printf("    Default: %f%n", pi); // 3.141593 (6 decimals)
        System.out.printf("    2 decimals: %.2f%n", pi); // 3.14
        System.out.printf("    0 decimals: %.0f%n", pi); // 3

        // Width and precision combined
        System.out.println("\n  Width + precision:");
        System.out.printf("    |%10.2f|%n", pi); // |      3.14|
        System.out.printf("    |%-10.2f|%n", pi); // |3.14      |

        // Scientific notation
        System.out.println("\n  Scientific notation:");
        System.out.printf("    %e%n", large); // 1.234568e+06
        System.out.printf("    %.2e%n", large); // 1.23e+06

        // Hex and octal
        System.out.println("\n  Integer formats:");
        System.out.printf("    Decimal: %d%n", integer);
        System.out.printf("    Hex: %x (or %X)%n", integer, integer);
        System.out.printf("    Octal: %o%n", integer);

        // Currency-like formatting
        System.out.println("\n  Currency-like:");
        System.out.printf("    Price: $%,.2f%n", 1234.5); // $1,234.50

        System.out.println();
    }

    // ===== Date/Time Formatting =====

    static void dateTimeFormatting() {
        System.out.println("=== Date/Time Formatting ===");

        // %t specifiers work with java.util.Date, Calendar, or long (millis)
        // NOT directly with java.time classes
        java.util.Date now = new java.util.Date();
        long timestamp = System.currentTimeMillis();

        // Using %t prefix for date/time
        System.out.println("  Date components (with java.util.Date):");
        System.out.printf("    Year: %tY%n", now); // 2024
        System.out.printf("    Month: %tm%n", now); // 12
        System.out.printf("    Day: %td%n", now); // 15
        System.out.printf("    Day name: %tA%n", now); // Monday

        System.out.println("\n  Time components:");
        System.out.printf("    Hour (24): %tH%n", now); // 14
        System.out.printf("    Hour (12): %tI%n", now); // 02
        System.out.printf("    Minute: %tM%n", now); // 30
        System.out.printf("    Second: %tS%n", now); // 45
        System.out.printf("    AM/PM: %tp%n", now); // pm

        System.out.println("\n  Combined formats:");
        System.out.printf("    Date: %tF%n", now); // 2024-12-15
        System.out.printf("    Time: %tT%n", now); // 14:30:45
        System.out.printf("    DateTime: %tc%n", now); // Mon Dec 15 14:30:45 PST 2024

        // Using timestamp (long)
        System.out.println("\n  Using timestamp (long):");
        System.out.printf("    Date from millis: %tF%n", timestamp);

        // Modern approach: java.time with DateTimeFormatter
        System.out.println("\n  Modern approach (java.time):");
        LocalDateTime localNow = LocalDateTime.now();
        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("    DateTimeFormatter: " + localNow.format(formatter));
        System.out.println(
                "    ISO format: " + localNow.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        System.out.println("""

        IMPORTANT: %t specifiers work with:
        - java.util.Date
        - java.util.Calendar
        - long (milliseconds)
        NOT with java.time classes (LocalDate, LocalDateTime)

        For java.time, use DateTimeFormatter:
        - DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        - DateTimeFormatter.ISO_LOCAL_DATE
        """);
    }

    // ===== formatted() Method (Java 15+) =====

    static void formattedMethod() {
        System.out.println("=== formatted() Method (Java 15+) ===");

        // formatted() is an instance method on String
        String template = "Hello, %s! You have %d messages.";
        String result = template.formatted("Alice", 5);
        System.out.println("  " + result);

        // Great with text blocks
        String json = """
        {
          "name": "%s",
          "age": %d,
          "active": %b
        }
        """.formatted("Bob", 25, true);
        System.out.println("  JSON:\n" + json);

        // Equivalent to String.format() but more readable
        String email = """
        Dear %s,

        Your order #%d has shipped.
        Estimated delivery: %s

        Best regards,
        The Team
        """.formatted("Charlie", 12345, LocalDate.now().plusDays(3));
        System.out.println("  Email:\n" + email);
    }

    // ===== Locale-Aware Formatting =====

    static void localeFormatting() {
        System.out.println("=== Locale-Aware Formatting ===");

        double number = 1234567.89;

        // Default locale
        System.out.println("  Default locale: " + Locale.getDefault());
        System.out.printf("    Number: %,.2f%n", number);

        // US locale
        System.out.println("\n  US locale:");
        System.out.printf(Locale.US, "    Number: %,.2f%n", number); // 1,234,567.89

        // German locale
        System.out.println("\n  German locale:");
        System.out.printf(Locale.GERMANY, "    Number: %,.2f%n", number); // 1.234.567,89

        // French locale
        System.out.println("\n  French locale:");
        System.out.printf(Locale.FRANCE, "    Number: %,.2f%n", number); // 1 234 567,89

        // Date formatting with locale - use DateTimeFormatter for java.time
        System.out.println("\n  Date by locale (modern way):");
        LocalDate date = LocalDate.of(2024, 3, 15);
        var usFormat = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yy", Locale.US);
        var ukFormat = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy", Locale.UK);
        System.out.println("    US format: " + date.format(usFormat)); // 03/15/24
        System.out.println("    UK format: " + date.format(ukFormat)); // 15/03/24

        System.out.println();
    }

    // ===== MessageFormat =====

    static void messageFormat() {
        System.out.println("=== MessageFormat ===");

        // MessageFormat uses numbered placeholders
        String pattern = "At {0,time} on {0,date}, {1} sent {2} messages.";
        String result = MessageFormat.format(pattern, new java.util.Date(), "Alice", 5);
        System.out.println("  " + result);

        // With choice format (pluralization)
        String choicePattern = "There {0,choice,0#are no files|1#is one file|1<are {0} files}.";
        System.out.println("  Choice format:");
        System.out.println("    0: " + MessageFormat.format(choicePattern, 0));
        System.out.println("    1: " + MessageFormat.format(choicePattern, 1));
        System.out.println("    5: " + MessageFormat.format(choicePattern, 5));

        System.out.println("""

        MessageFormat:
        - Good for i18n/l10n (internationalization)
        - Numbered placeholders {0}, {1}, etc.
        - Supports formatting: {0,number,currency}
        - Choice format for pluralization
        """);
    }

    // ===== Best Practices =====

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        Recommended approaches by use case:

        Simple concatenation:
          "Hello, " + name           // Fine for simple cases

        Building with values:
          String.format("...")       // Traditional
          "...".formatted(...)       // Java 15+ (preferred with text blocks)

        Building in loops:
          StringBuilder              // Always for loops

        Joining collections:
          String.join(delimiter, list)
          Collectors.joining()

        Logging:
          logger.info("User {} logged in", userId)  // SLF4J style
          // Don't format the string yourself!

        Locale-sensitive:
          String.format(locale, "...", args)

        Internationalization:
          MessageFormat or ResourceBundle

        Complex date/time:
          DateTimeFormatter (not %t specifiers)

        Performance tips:
        - Pre-compile Pattern for regex
        - Use StringBuilder in loops
        - Let logging frameworks handle formatting
        - Avoid String.format() in hot paths
        """);
    }
}
