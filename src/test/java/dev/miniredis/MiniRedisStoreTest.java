package dev.miniredis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MiniRedisStoreTest {

    @Test
    void setValueReturnsStoredValue(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("language","Java");
        String actual = testStore.get("language");
        assertEquals("Java",actual);
    }

    @Test
    void presentKeyShouldExist(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("David","exists");
        assertTrue(testStore.exists("David"));
    }

    @Test
    void missingKeyShouldNotExist(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("Alice","exists");
        assertFalse(testStore.exists("Ali"));
    }
}
