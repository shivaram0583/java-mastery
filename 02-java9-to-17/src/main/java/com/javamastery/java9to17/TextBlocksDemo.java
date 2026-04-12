package com.javamastery.java9to17;

/**
 * Demonstrates Text Blocks (Java 13 preview, Java 15 standard).
 *
 * Text blocks use triple-quote delimiters (""") to create multi-line strings
 * that are more readable for JSON, SQL, HTML, and other structured text.
 */
public class TextBlocksDemo {

    // ==================== Basic Text Blocks ====================

    /** Traditional multi-line string with concatenation */
    public static String htmlTraditional() {
        return "<html>\n" +
               "    <body>\n" +
               "        <p>Hello, World</p>\n" +
               "    </body>\n" +
               "</html>";
    }

    /** Same HTML using text blocks — much cleaner */
    public static String htmlTextBlock() {
        return """
                <html>
                    <body>
                        <p>Hello, World</p>
                    </body>
                </html>""";
    }

    // ==================== JSON ====================

    /** JSON with text blocks — no escaping of quotes needed */
    public static String jsonTextBlock(String name, int age) {
        return """
                {
                    "name": "%s",
                    "age": %d,
                    "active": true
                }""".formatted(name, age);
    }

    // ==================== SQL ====================

    /** SQL query using text block */
    public static String sqlQuery(String table) {
        return """
                SELECT id, name, email
                FROM %s
                WHERE active = true
                ORDER BY name ASC
                LIMIT 100""".formatted(table);
    }

    // ==================== Indentation Control ====================

    /**
     * The closing """ position controls indentation stripping.
     * Moving """ to the left ADDS leading whitespace; to the right strips it.
     */
    public static String indentationDemo() {
        // Indentation is stripped based on the position of the closing """
        return """
            Line 1 (4 spaces relative to closing delimiter)
            Line 2
            Line 3
            """;
    }

    /** stripIndent() removes incidental whitespace */
    public static String stripIndent() {
        return "    Hello\n    World".stripIndent();
    }

    // ==================== Escape Sequences ====================

    /**
     * Text blocks support new escape sequences:
     * \s — explicit space (prevents trailing whitespace trimming)
     * \ — line continuation (suppresses the newline)
     */
    public static String escapeSequences() {
        // \s preserves trailing spaces (text blocks trim trailing whitespace by default)
        String withSpace = """
                Name:  \s
                Email: \s
                """;

        // \ at end of line continues the line (no newline inserted)
        String singleLine = """
                This is a very long \
                sentence that spans \
                multiple source lines \
                but is actually one line.""";

        return "With \\s:\n" + withSpace + "\nWith \\:\n" + singleLine;
    }

    // ==================== String Templates ====================

    /** Using formatted() with text blocks for templating */
    public static String emailTemplate(String recipient, String subject, String body) {
        return """
                To: %s
                Subject: %s
                
                Dear %s,
                
                %s
                
                Best regards,
                Java Mastery Team
                """.formatted(recipient, subject, recipient.split("@")[0], body);
    }

    public static void main(String[] args) {
        System.out.println("=== Text Blocks Demo ===\n");

        System.out.println("--- Traditional HTML ---");
        System.out.println(htmlTraditional());

        System.out.println("\n--- Text Block HTML ---");
        System.out.println(htmlTextBlock());

        System.out.println("\n--- JSON Text Block ---");
        System.out.println(jsonTextBlock("Alice", 30));

        System.out.println("\n--- SQL Text Block ---");
        System.out.println(sqlQuery("users"));

        System.out.println("\n--- Indentation Demo ---");
        System.out.println("[" + indentationDemo() + "]");

        System.out.println("\n--- Escape Sequences ---");
        System.out.println(escapeSequences());

        System.out.println("\n--- Email Template ---");
        System.out.println(emailTemplate("alice@example.com", "Welcome!", "Thank you for joining."));

        // Both traditional and text block produce the same string
        System.out.println("\n--- Equivalence check ---");
        System.out.println("HTML equal: " + htmlTraditional().equals(htmlTextBlock()));
    }
}
