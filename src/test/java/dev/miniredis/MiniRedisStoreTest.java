package dev.miniredis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MiniRedisStoreTest {

    @Test
    void setValueGetStoredValue(){
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

    @Test
    void presentKeyDeleteKeyShouldNotExist(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("David","bout to be deleted");
        assertEquals(1,testStore.delete("David"));
        assertFalse(testStore.exists("David"));
    }

    @Test
    void missingKeyDeleteKeyReturnZero(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("David","Dave be deleted instead");
        assertEquals(0,testStore.delete("Dave"));
    }
}
