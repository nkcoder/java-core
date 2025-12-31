package org.nkcoder.pattern;

/**
 * Switch Expressions (Java 14+): Switch as an expression that returns a value.
 *
 * <p><strong>Java 25 Status:</strong> Finalized and widely used. Prefer switch expressions over switch statements for
 * cleaner, more concise code.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Arrow syntax (->) for concise case handling
 *   <li>Switch can return a value (expression, not just statement)
 *   <li>No fall-through with arrow syntax
 *   <li>yield keyword for multi-line cases
 *   <li>Multiple labels per case
 * </ul>
 */
public class SwitchExpressionExample {

    static void main(String[] args) {
        oldSwitchStatement();
        arrowSyntax();
        switchAsExpression();
        yieldKeyword();
        multipleLabels();
        exhaustiveness();
        realWorldExamples();
    }

    static void oldSwitchStatement() {
        System.out.println("=== Old Switch Statement ===");

        int day = 3;

        // Old style - verbose, fall-through bugs possible
        String dayName;
        switch (day) {
            case 1:
                dayName = "Monday";
                break; // Easy to forget!
            case 2:
                dayName = "Tuesday";
                break;
            case 3:
                dayName = "Wednesday";
                break;
            default:
                dayName = "Unknown";
                break;
        }

        System.out.println("  Day " + day + " is " + dayName);

        System.out.println("""

        Problems with old switch:
        - Easy to forget break (fall-through bugs)
        - Verbose boilerplate
        - Cannot use as expression
        - Variable must be declared outside
        """);
    }

    static void arrowSyntax() {
        System.out.println("=== Arrow Syntax (Java 14+) ===");

        int day = 3;

        // Arrow syntax - no break needed, no fall-through
        switch (day) {
            case 1 -> System.out.println("  Monday");
            case 2 -> System.out.println("  Tuesday");
            case 3 -> System.out.println("  Wednesday");
            case 4 -> System.out.println("  Thursday");
            case 5 -> System.out.println("  Friday");
            case 6, 7 -> System.out.println("  Weekend!"); // Multiple labels
            default -> System.out.println("  Invalid day");
        }

        // Can use block for multiple statements
        int score = 85;
        switch (score / 10) {
            case 10, 9 -> {
                System.out.println("  Excellent!");
                System.out.println("  Grade: A");
            }
            case 8 -> System.out.println("  Grade: B");
            case 7 -> System.out.println("  Grade: C");
            default -> System.out.println("  Needs improvement");
        }

        System.out.println();
    }

    static void switchAsExpression() {
        System.out.println("=== Switch as Expression ===");

        int day = 5;

        // Switch returns a value directly
        String dayType =
                switch (day) {
                    case 1, 2, 3, 4, 5 -> "Weekday";
                    case 6, 7 -> "Weekend";
                    default -> "Invalid";
                };

        System.out.println("  Day " + day + " is a " + dayType);

        // Use in method calls
        int month = 2;
        int daysInMonth =
                switch (month) {
                    case 1, 3, 5, 7, 8, 10, 12 -> 31;
                    case 4, 6, 9, 11 -> 30;
                    case 2 -> 28; // Simplified, ignoring leap years
                    default -> throw new IllegalArgumentException("Invalid month: " + month);
                };

        System.out.println("  Month " + month + " has " + daysInMonth + " days");

        // Inline usage
        String status = "ACTIVE";
        System.out.println("  Status color: "
                + switch (status) {
                    case "ACTIVE" -> "green";
                    case "PENDING" -> "yellow";
                    case "ERROR" -> "red";
                    default -> "gray";
                });

        System.out.println();
    }

    static void yieldKeyword() {
        System.out.println("=== yield Keyword ===");

        int score = 75;

        // yield returns a value from a block in switch expression
        String grade =
                switch (score / 10) {
                    case 10, 9 -> "A";
                    case 8 -> "B";
                    case 7 -> {
                        System.out.println("    Processing score: " + score);
                        yield "C"; // Use yield in blocks
                    }
                    case 6 -> "D";
                    default -> {
                        if (score < 0) {
                            yield "Invalid";
                        }
                        yield "F";
                    }
                };

        System.out.println("  Grade: " + grade);

        // yield is required for blocks that must return a value
        String result =
                switch ("test") {
                    case "test" -> {
                        String upper = "test".toUpperCase();
                        String modified = upper + "!";
                        yield modified; // Can't use return here
                    }
                    default -> "default";
                };

        System.out.println("  Result: " + result);

        System.out.println("""

        yield vs return:
        - yield: Returns value from switch expression block
        - return: Returns from the method
        - Arrow without block doesn't need yield
        """);
    }

    static void multipleLabels() {
        System.out.println("=== Multiple Labels ===");

        // Group cases with same handling
        char grade = 'B';
        String description =
                switch (grade) {
                    case 'A', 'B' -> "Good";
                    case 'C' -> "Average";
                    case 'D', 'F' -> "Poor";
                    default -> "Unknown grade";
                };

        System.out.println("  Grade " + grade + ": " + description);

        // Useful for enums
        enum Day {
            MON,
            TUE,
            WED,
            THU,
            FRI,
            SAT,
            SUN
        }

        Day today = Day.SAT;
        String schedule =
                switch (today) {
                    case MON, TUE, WED, THU, FRI -> "Work day";
                    case SAT, SUN -> "Rest day";
                }; // No default needed - enum is exhaustive!

        System.out.println("  " + today + " is a " + schedule);

        System.out.println();
    }

    static void exhaustiveness() {
        System.out.println("=== Exhaustiveness ===");

        // Switch expressions MUST be exhaustive (cover all cases)
        enum Status {
            PENDING,
            ACTIVE,
            COMPLETED,
            CANCELLED
        }

        Status status = Status.ACTIVE;

        // All enum values covered - no default needed
        String action =
                switch (status) {
                    case PENDING -> "Wait";
                    case ACTIVE -> "Process";
                    case COMPLETED -> "Archive";
                    case CANCELLED -> "Clean up";
                };

        System.out.println("  Action for " + status + ": " + action);

        // For non-sealed types, default is required
        int code = 200;
        String meaning =
                switch (code) {
                    case 200 -> "OK";
                    case 404 -> "Not Found";
                    case 500 -> "Server Error";
                    default -> "Unknown code"; // Required for int
                };

        System.out.println("  HTTP " + code + ": " + meaning);

        System.out.println("""

        Exhaustiveness rules:
        - Enum: Must cover all values OR have default
        - Sealed types: Must cover all permitted types OR have default
        - Other types: Must have default
        - Compiler enforces this at compile time!
        """);
    }

    static void realWorldExamples() {
        System.out.println("=== Real-World Examples ===");

        // 1. State machine
        enum OrderState {
            CREATED,
            PAID,
            SHIPPED,
            DELIVERED
        }

        OrderState state = OrderState.PAID;
        OrderState nextState =
                switch (state) {
                    case CREATED -> OrderState.PAID;
                    case PAID -> OrderState.SHIPPED;
                    case SHIPPED -> OrderState.DELIVERED;
                    case DELIVERED -> throw new IllegalStateException("Order already delivered");
                };
        System.out.println("  Order transition: " + state + " -> " + nextState);

        // 2. Factory pattern
        String type = "circle";
        double area =
                switch (type) {
                    case "circle" -> Math.PI * 5 * 5;
                    case "square" -> 10 * 10;
                    case "rectangle" -> 10 * 5;
                    default -> 0;
                };
        System.out.println("  Area of " + type + ": " + String.format("%.2f", area));

        // 3. Mapping/conversion
        int httpStatus = 201;
        record HttpResponse(int code, String status, boolean success) {}

        HttpResponse response =
                switch (httpStatus / 100) {
                    case 2 -> new HttpResponse(httpStatus, "Success", true);
                    case 3 -> new HttpResponse(httpStatus, "Redirect", true);
                    case 4 -> new HttpResponse(httpStatus, "Client Error", false);
                    case 5 -> new HttpResponse(httpStatus, "Server Error", false);
                    default -> new HttpResponse(httpStatus, "Unknown", false);
                };
        System.out.println("  HTTP Response: " + response);

        // 4. String parsing with validation
        String input = "YES";
        boolean confirmed =
                switch (input.toUpperCase()) {
                    case "YES", "Y", "TRUE", "1" -> true;
                    case "NO", "N", "FALSE", "0" -> false;
                    default -> throw new IllegalArgumentException("Invalid input: " + input);
                };
        System.out.println("  Confirmed: " + confirmed);

        System.out.println();
    }
}
