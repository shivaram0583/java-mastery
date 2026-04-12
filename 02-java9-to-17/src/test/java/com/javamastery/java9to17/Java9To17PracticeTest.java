package com.javamastery.java9to17;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static com.javamastery.java9to17.Java9To17PracticeProblems.*;

class Java9To17PracticeTest {

    @Test
    void testStudentRecord() {
        var student = new Student("Alice", 20, 95.5);
        assertEquals("Alice", student.name());
        assertEquals(20, student.age());
        assertEquals(95.5, student.grade());
    }

    @Test
    void testStudentRecordValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Student("", 20, 90));
        assertThrows(IllegalArgumentException.class, () -> new Student("Alice", -1, 90));
        assertThrows(IllegalArgumentException.class, () -> new Student("Alice", 20, 101));
    }

    @Test
    void testStudentRecordEquality() {
        var s1 = new Student("Alice", 20, 95.5);
        var s2 = new Student("Alice", 20, 95.5);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void testExpressionEvaluator() {
        // 2 + 3 = 5
        assertEquals(5.0, evaluateExpr(new Add(new Num(2), new Num(3))));
        // (2 + 3) * 4 = 20
        assertEquals(20.0, evaluateExpr(new Mul(new Add(new Num(2), new Num(3)), new Num(4))));
        // 2 * 3 + 4 = 10
        assertEquals(10.0, evaluateExpr(new Add(new Mul(new Num(2), new Num(3)), new Num(4))));
    }

    @Test
    void testDescribeObject() {
        assertEquals("null value", describeObject(null));
        assertEquals("integer: 42", describeObject(42));
        assertEquals("negative integer: -5", describeObject(-5));
        assertTrue(describeObject("hello").contains("string of length 5"));
        assertEquals("blank string", describeObject("   "));
        assertTrue(describeObject(List.of(1, 2)).contains("2 elements"));
    }

    @Test
    void testClassifyDay() {
        assertEquals("Weekday", classifyDay("Monday"));
        assertEquals("Weekday", classifyDay("FRIDAY"));
        assertEquals("Weekend", classifyDay("saturday"));
        assertEquals("Invalid day", classifyDay("Funday"));
    }

    @Test
    void testHttpStatus() {
        assertEquals("OK", httpStatus(200));
        assertEquals("Not Found", httpStatus(404));
        assertEquals("Internal Server Error", httpStatus(500));
        assertEquals("Success", httpStatus(204));
        assertEquals("Client Error", httpStatus(422));
    }

    @Test
    void testProcessMultilineText() {
        var text = """
                  Hello
                  
                  World
                  
                  Java
                """;
        var result = processMultilineText(text);
        assertEquals(List.of("Hello", "World", "Java"), result);
    }

    @Test
    void testLineStatistics() {
        var text = "hello\n\nworld\n\n";
        var stats = lineStatistics(text);
        assertEquals(4L, stats.get("total"));
        assertEquals(2L, stats.get("blank"));
        assertEquals(2L, stats.get("nonBlank"));
    }

    @Test
    void testPaginate() {
        var items = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertEquals(List.of(1, 2, 3), paginate(items, 0, 3));
        assertEquals(List.of(4, 5, 6), paginate(items, 1, 3));
        assertEquals(List.of(10), paginate(items, 3, 3));
    }

    @Test
    void testTakeAfterDropping() {
        var nums = List.of(1, 2, 3, 5, 8, 3, 1);
        assertEquals(List.of(5, 8, 3, 1), takeAfterDropping(nums, 5));
    }

    @Test
    void testLookupValue() {
        var cache = Map.of("a", "cached_value");
        var db = Map.of("b", "db_value");
        assertEquals("cached_value", lookupValue("a", cache, db));
        assertEquals("db_value", lookupValue("b", cache, db));
        assertEquals("DEFAULT_VALUE", lookupValue("c", cache, db));
    }

    @Test
    void testCreateJsonUser() {
        var json = createJsonUser("Alice", 25, "alice@test.com");
        assertTrue(json.contains("\"name\": \"Alice\""));
        assertTrue(json.contains("\"age\": 25"));
        assertTrue(json.contains("\"email\": \"alice@test.com\""));
    }

    @Test
    void testCreateSelectQuery() {
        var sql = createSelectQuery("users", "active = true");
        assertTrue(sql.contains("SELECT *"));
        assertTrue(sql.contains("FROM users"));
        assertTrue(sql.contains("WHERE active = true"));
    }

    @Test
    void testPaymentProcessing() {
        var cc = new CreditCard("4111111111111111", "12/25", 99.99);
        assertTrue(processPayment(cc).contains("1111"));

        var bt = new BankTransfer("DE89370400440532013000", "COBADEFF", 500.0);
        assertTrue(processPayment(bt).contains("IBAN"));

        var dw = new DigitalWallet("PayPal", "user@test.com", 25.0);
        assertTrue(processPayment(dw).contains("PayPal"));
    }

    @Test
    void testPaymentValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new CreditCard("123", "12/25", 50.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DebitCard("4111111111111111", "1234", -10.0));
        assertThrows(IllegalArgumentException.class,
                () -> new BankTransfer("", "BIC", 100.0));
    }
}
