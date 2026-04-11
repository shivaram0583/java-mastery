package com.javamastery.advanced;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DesignPatternsTest {

    @Test
    void singletonReturnsSameInstance() {
        var db1 = DesignPatternsDemo.DatabaseConnection.INSTANCE;
        var db2 = DesignPatternsDemo.DatabaseConnection.INSTANCE;
        assertSame(db1, db2);
        assertNotNull(db1.getUrl());
    }

    @Test
    void builderCreatesImmutableRequest() {
        var request = new DesignPatternsDemo.HttpRequest.Builder()
                .url("https://example.com")
                .method("POST")
                .header("Accept", "application/json")
                .body("{\"key\":\"value\"}")
                .build();

        assertNotNull(request);
        assertTrue(request.toString().contains("POST"));
        assertTrue(request.toString().contains("https://example.com"));
    }

    @Test
    void builderRequiresUrl() {
        assertThrows(NullPointerException.class, () ->
                new DesignPatternsDemo.HttpRequest.Builder().build());
    }

    @Test
    void strategyAppliesDifferentPricing() {
        double base = 100.0;
        assertEquals(100.0, DesignPatternsDemo.applyPricing(base, p -> p));
        assertEquals(80.0, DesignPatternsDemo.applyPricing(base, p -> p * 0.8));
        assertEquals(50.0, DesignPatternsDemo.applyPricing(base, p -> p * 0.5));
    }

    @Test
    void observerReceivesEvents() {
        var bus = new DesignPatternsDemo.EventBus();
        String[] received = {null};
        bus.subscribe("test", data -> received[0] = data.toString());
        bus.publish("test", "hello");
        assertEquals("hello", received[0]);
    }

    @Test
    void factoryCreatesCorrectShapes() {
        var circle = DesignPatternsDemo.createShape("circle", 5);
        var rect = DesignPatternsDemo.createShape("rectangle", 4, 6);

        assertInstanceOf(DesignPatternsDemo.Circle.class, circle);
        assertInstanceOf(DesignPatternsDemo.Rectangle.class, rect);
        assertEquals(24.0, rect.area());
        assertTrue(circle.area() > 78 && circle.area() < 79);
    }
}
