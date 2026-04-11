package com.javamastery.advanced;

import org.junit.jupiter.api.Test;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class SerializationTest {

    @Test
    void basicSerializationRoundTrip() throws Exception {
        var user = new SerializationDemo.User("Alice", 30, "secret");

        byte[] bytes = SerializationDemo.serialize(user);
        var restored = SerializationDemo.deserialize(bytes, SerializationDemo.User.class);

        assertNotNull(restored);
        assertTrue(restored.toString().contains("Alice"));
        assertTrue(restored.toString().contains("30"));
    }

    @Test
    void transientFieldIsNull() throws Exception {
        var user = new SerializationDemo.User("Bob", 25, "password123");

        byte[] bytes = SerializationDemo.serialize(user);
        var restored = SerializationDemo.deserialize(bytes, SerializationDemo.User.class);

        // password was transient — should be null
        assertTrue(restored.toString().contains("password='null'"));
    }

    @Test
    void customSerializationPreservesToken() throws Exception {
        var secure = new SerializationDemo.SecureUser("Carol", "my-token");

        byte[] bytes = SerializationDemo.serialize(secure);
        var restored = SerializationDemo.deserialize(bytes, SerializationDemo.SecureUser.class);

        assertTrue(restored.toString().contains("my-token"));
    }

    @Test
    void externalizableRoundTrip() throws Exception {
        var product = new SerializationDemo.Product("SKU-001", 29.99);

        byte[] bytes = SerializationDemo.serialize(product);
        var restored = SerializationDemo.deserialize(bytes, SerializationDemo.Product.class);

        assertTrue(restored.toString().contains("SKU-001"));
        assertTrue(restored.toString().contains("29.99"));
    }
}
