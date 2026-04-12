package com.javamastery.java18to24;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static com.javamastery.java18to24.Java18To24PracticeProblems.*;

class Java18To24PracticeTest {

    @Test
    void testCreateVirtualThreads() throws Exception {
        var results = createVirtualThreads();
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(s -> s.contains("true")));
    }

    @Test
    void testSimulateConcurrentRequests() throws Exception {
        int numRequests = 1000;
        int processed = simulateConcurrentRequests(numRequests);
        assertEquals(numRequests, processed);
    }

    @Test
    void testExtractHeadNames() {
        var company = new Company("Acme", List.of(
                new Department("Engineering", new Employee("Alice", 35)),
                new Department("Sales", new Employee("Bob", 40))
        ));
        var names = extractHeadNames(company);
        assertEquals(List.of("Alice", "Bob"), names);
    }

    @Test
    void testDescribeEmployee() {
        assertEquals("Alice is 30 years old", describeEmployee(new Employee("Alice", 30)));
        assertEquals("Not an employee", describeEmployee("not an employee"));
    }

    @Test
    void testDescribeDepartment() {
        var dept = new Department("Engineering", new Employee("Alice", 30));
        assertEquals("Engineering headed by Alice (age 30)", describeDepartment(dept));
    }

    @Test
    void testEvaluateExpressions() {
        assertEquals(5.0, evaluate(new Literal(5)));
        assertEquals(-3.0, evaluate(new Negate(new Literal(3))));
        assertEquals(7.0, evaluate(new Add(new Literal(3), new Literal(4))));
        assertEquals(12.0, evaluate(new Multiply(new Literal(3), new Literal(4))));
        assertEquals(2.5, evaluate(new Divide(new Literal(5), new Literal(2))));

        // (2 * 3) + 4 = 10
        assertEquals(10.0, evaluate(
                new Add(new Multiply(new Literal(2), new Literal(3)), new Literal(4))));
    }

    @Test
    void testDivisionByZero() {
        assertThrows(ArithmeticException.class, () ->
                evaluate(new Divide(new Literal(5), new Literal(0))));
    }

    @Test
    void testExpressionToString() {
        assertEquals("5.0", expressionToString(new Literal(5)));
        assertEquals("(3.0 + 4.0)", expressionToString(new Add(new Literal(3), new Literal(4))));
    }

    @Test
    void testRotateLeft() {
        assertEquals(List.of(2, 3, 4, 5, 1), rotateLeft(List.of(1, 2, 3, 4, 5)));
        assertEquals(List.of(), rotateLeft(List.of()));
    }

    @Test
    void testSwapFirstLast() {
        assertEquals(List.of(5, 2, 3, 4, 1), swapFirstLast(List.of(1, 2, 3, 4, 5)));
        assertEquals(List.of(1), swapFirstLast(List.of(1)));
    }

    @Test
    void testFindMiddle() {
        assertEquals(Optional.of(3), findMiddle(List.of(1, 2, 3, 4, 5)));
        assertEquals(Optional.empty(), findMiddle(List.of()));
    }

    @Test
    void testTrafficStateMachine() {
        TrafficState state = new Red();
        state = nextState(state);
        assertInstanceOf(Green.class, state);
        state = nextState(state);
        assertInstanceOf(Yellow.class, state);
        state = nextState(state);
        assertInstanceOf(Red.class, state);
    }

    @Test
    void testDescribeState() {
        assertTrue(describeState(new Red()).contains("STOP"));
        assertTrue(describeState(new Green()).contains("GO"));
        assertTrue(describeState(new Yellow()).contains("CAUTION"));
    }
}
