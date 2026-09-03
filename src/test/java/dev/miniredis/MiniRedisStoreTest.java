package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

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

    @Test
    void incrMissingKeyReturnOneShouldExist(){
        MiniRedisStore testStore = new MiniRedisStore();
        assertEquals(1L,testStore.incr("david"));
        assertEquals("1",testStore.get("david"));
    }

    @Test
    void incrExistingPositiveNumber(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter","52");
        assertEquals(53L,testStore.incr("counter"));
        assertEquals("53",testStore.get("counter"));
    }

    @Test
    void incrNegativeNumber(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "-1");
        assertEquals(0L, testStore.incr("counter"));
        assertEquals("0", testStore.get("counter"));
    }
    @Test
    void incrInvalidValueThrowException(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter","banana");
        assertThrows(NumberFormatException.class,() -> testStore.incr("counter"));
    }

    @Test
    void incrInvalidValueDoesNotChangeStoredValue(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter","banana");
        assertThrows(NumberFormatException.class,() -> testStore.incr("counter"));
        assertEquals("banana", testStore.get("counter"));
    }
}
