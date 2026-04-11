package com.javamastery.advanced;

import org.junit.jupiter.api.Test;
import java.lang.reflect.*;
import static org.junit.jupiter.api.Assertions.*;

class ReflectionTest {

    static class SampleClass {
        private String value = "original";
        public String getValue() { return value; }
        private int secret() { return 42; }
    }

    @Test
    void canReadPrivateField() throws Exception {
        SampleClass obj = new SampleClass();
        Field field = SampleClass.class.getDeclaredField("value");
        field.setAccessible(true);
        assertEquals("original", field.get(obj));
    }

    @Test
    void canModifyPrivateField() throws Exception {
        SampleClass obj = new SampleClass();
        Field field = SampleClass.class.getDeclaredField("value");
        field.setAccessible(true);
        field.set(obj, "modified");
        assertEquals("modified", obj.getValue());
    }

    @Test
    void canInvokePrivateMethod() throws Exception {
        SampleClass obj = new SampleClass();
        Method method = SampleClass.class.getDeclaredMethod("secret");
        method.setAccessible(true);
        assertEquals(42, method.invoke(obj));
    }

    @Test
    void canCreateInstanceDynamically() throws Exception {
        Class<?> clazz = Class.forName("com.javamastery.advanced.ReflectionTest$SampleClass");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        assertNotNull(instance);
        assertInstanceOf(SampleClass.class, instance);
    }

    @Test
    void canListDeclaredMethods() {
        Method[] methods = SampleClass.class.getDeclaredMethods();
        assertTrue(methods.length >= 2);
    }
}
