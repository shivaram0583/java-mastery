package com.javamastery.java9to17;

/**
 * Demonstrates private methods in interfaces (Java 9).
 *
 * Private methods allow code reuse between default methods within
 * the same interface, without exposing the helper to implementing classes.
 */
public class PrivateInterfaceMethodsDemo {

    interface Loggable {
        // Default methods can share logic via private helpers
        default void logInfo(String message) {
            log("INFO", message);
        }

        default void logError(String message) {
            log("ERROR", message);
        }

        // Private method — not visible to implementing classes
        private void log(String level, String message) {
            System.out.println("[" + level + "] " + java.time.LocalTime.now() + " - " + message);
        }
    }

    // Simple implementation — only needs to exist, private log() is hidden
    static class MyService implements Loggable {
        void doWork() {
            logInfo("Starting work...");
            // some work
            logInfo("Work completed.");
        }

        void handleError() {
            logError("Something went wrong!");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Private Interface Methods Demo (Java 9) ===\n");

        MyService service = new MyService();
        service.doWork();
        service.handleError();
    }
}
