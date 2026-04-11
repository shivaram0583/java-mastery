package com.javamastery.advanced;

import java.io.*;

/**
 * Demonstrates Java Serialization: Serializable, transient,
 * serialVersionUID, custom read/write, and Externalizable.
 */
public class SerializationDemo {

    // --- 1. Basic Serializable ---
    static class User implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String name;
        private int age;
        private transient String password; // NOT serialized

        User(String name, int age, String password) {
            this.name = name;
            this.age = age;
            this.password = password;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + ", password='" + password + "'}";
        }
    }

    // --- 2. Custom serialization ---
    static class SecureUser implements Serializable {
        @Serial
        private static final long serialVersionUID = 2L;

        private String name;
        private transient String token;

        SecureUser(String name, String token) {
            this.name = name;
            this.token = token;
        }

        // Custom serialization: encode token before writing
        @Serial
        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.defaultWriteObject();
            // Write encoded version of transient field
            oos.writeObject(token != null ? new StringBuilder(token).reverse().toString() : null);
        }

        // Custom deserialization: decode token after reading
        @Serial
        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ois.defaultReadObject();
            String encoded = (String) ois.readObject();
            this.token = encoded != null ? new StringBuilder(encoded).reverse().toString() : null;
        }

        @Override
        public String toString() {
            return "SecureUser{name='" + name + "', token='" + token + "'}";
        }
    }

    // --- 3. Externalizable (full control) ---
    static class Product implements Externalizable {
        private String id;
        private double price;

        public Product() {} // Required no-arg constructor for Externalizable

        Product(String id, double price) {
            this.id = id;
            this.price = price;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(id);
            out.writeDouble(price);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            this.id = in.readUTF();
            this.price = in.readDouble();
        }

        @Override
        public String toString() {
            return "Product{id='" + id + "', price=" + price + "}";
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Serialization Demo ===\n");

        // --- Basic serialization ---
        System.out.println("--- Basic Serialization ---");
        User user = new User("Alice", 30, "secret123");
        System.out.println("  Before: " + user);

        byte[] bytes = serialize(user);
        System.out.println("  Serialized size: " + bytes.length + " bytes");

        User deserialized = deserialize(bytes, User.class);
        System.out.println("  After:  " + deserialized);
        System.out.println("  Note: password is null (marked transient)");

        // --- Custom serialization ---
        System.out.println("\n--- Custom Serialization ---");
        SecureUser secure = new SecureUser("Bob", "abc-token-xyz");
        System.out.println("  Before: " + secure);

        bytes = serialize(secure);
        SecureUser secureBack = deserialize(bytes, SecureUser.class);
        System.out.println("  After:  " + secureBack);
        System.out.println("  Token preserved via custom writeObject/readObject");

        // --- Externalizable ---
        System.out.println("\n--- Externalizable ---");
        Product product = new Product("SKU-001", 29.99);
        System.out.println("  Before: " + product);

        bytes = serialize(product);
        Product productBack = deserialize(bytes, Product.class);
        System.out.println("  After:  " + productBack);

        // --- Modern alternative ---
        System.out.println("\n--- Modern Alternatives ---");
        System.out.println("  Java Serialization is fragile and has security issues.");
        System.out.println("  Prefer: JSON (Jackson/Gson), Protocol Buffers, Avro, or Records");
        System.out.println("  serialVersionUID prevents InvalidClassException on schema changes");
    }

    static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    static <T> T deserialize(byte[] bytes, Class<T> type) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) ois.readObject();
        }
    }
}
