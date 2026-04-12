package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Text Blocks")
class TextBlocksTest {

    @Test
    @DisplayName("Traditional and text block HTML produce same result")
    void htmlEquivalence() {
        assertEquals(TextBlocksDemo.htmlTraditional(), TextBlocksDemo.htmlTextBlock());
    }

    @Test
    @DisplayName("JSON text block contains formatted values")
    void jsonTextBlock() {
        String json = TextBlocksDemo.jsonTextBlock("Bob", 25);
        assertTrue(json.contains("\"name\": \"Bob\""));
        assertTrue(json.contains("\"age\": 25"));
        assertTrue(json.contains("\"active\": true"));
    }

    @Test
    @DisplayName("SQL text block contains table name")
    void sqlQuery() {
        String sql = TextBlocksDemo.sqlQuery("employees");
        assertTrue(sql.contains("FROM employees"));
        assertTrue(sql.contains("SELECT id, name, email"));
        assertTrue(sql.contains("LIMIT 100"));
    }

    @Test
    @DisplayName("Text block preserves multi-line structure")
    void textBlockMultiLine() {
        String html = TextBlocksDemo.htmlTextBlock();
        assertTrue(html.contains("\n"));
        assertTrue(html.startsWith("<html>"));
        assertTrue(html.endsWith("</html>"));
    }

    @Test
    @DisplayName("Email template includes recipient info")
    void emailTemplate() {
        String email = TextBlocksDemo.emailTemplate("alice@example.com", "Hello", "Welcome aboard.");
        assertTrue(email.contains("To: alice@example.com"));
        assertTrue(email.contains("Subject: Hello"));
        assertTrue(email.contains("Dear alice"));
        assertTrue(email.contains("Welcome aboard."));
    }
}
