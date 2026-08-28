package dev.miniredis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class MiniRedisStoreTest {

    @Test
    void setValueThenGetStoredValue(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("language","Java");
        String actual = testStore.get("language");
        assertEquals("Java",actual);
    }
}
