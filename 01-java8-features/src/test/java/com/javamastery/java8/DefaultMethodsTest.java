package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Default & Static Methods in Interfaces")
class DefaultMethodsTest {

    interface Greeter {
        String greet(String name);

        default String greetWithTitle(String name, String title) {
            return greet(title + " " + name);
        }

        static Greeter formal() {
            return name -> "Good day, " + name + ".";
        }
    }

    interface Shouter {
        default String shout(String msg) {
            return msg.toUpperCase() + "!";
        }
    }

    interface Whisperer {
        default String shout(String msg) {
            return msg.toLowerCase() + "...";
        }
    }

    // Diamond problem: must override conflicting default methods
    static class Confused implements Shouter, Whisperer {
        @Override
        public String shout(String msg) {
            return Shouter.super.shout(msg); // explicitly choose Shouter
        }
    }

    @Test
    @DisplayName("Default method provides implementation")
    void defaultMethodWorks() {
        Greeter casual = name -> "Hey, " + name + "!";

        assertEquals("Hey, Mr Smith!", casual.greetWithTitle("Smith", "Mr"));
    }

    @Test
    @DisplayName("Static method on interface creates instance")
    void staticMethodOnInterface() {
        Greeter formal = Greeter.formal();

        assertEquals("Good day, Alice.", formal.greet("Alice"));
    }

    @Test
    @DisplayName("Diamond problem resolved by explicit override")
    void diamondProblemResolved() {
        Confused c = new Confused();
        assertEquals("HELLO!", c.shout("hello"));
    }

    @Test
    @DisplayName("Default method can be overridden")
    void defaultMethodCanBeOverridden() {
        Greeter yeller = new Greeter() {
            @Override
            public String greet(String name) {
                return name;
            }

            @Override
            public String greetWithTitle(String name, String title) {
                return (title + " " + name).toUpperCase();
            }
        };

        assertEquals("DR ALICE", yeller.greetWithTitle("Alice", "Dr"));
    }
}
