package dev.miniredis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class MiniRedisStoreTest {

    @Test
    void setValueGetStoredValue() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("language", "Java");
        String actual = testStore.get("language");
        assertEquals("Java", actual);
    }

    @Test
    void presentKeyShouldExist() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("David", "exists");
        assertTrue(testStore.exists("David"));
    }

    @Test
    void missingKeyShouldNotExist() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("Alice", "exists");
        assertFalse(testStore.exists("Ali"));
    }

    @Test
    void presentKeyDeleteKeyShouldNotExist() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("David", "bout to be deleted");
        assertEquals(1, testStore.delete("David"));
        assertFalse(testStore.exists("David"));
    }

    @Test
    void missingKeyDeleteKeyReturnZero() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("David", "Dave be deleted instead");
        assertEquals(0, testStore.delete("Dave"));
    }

    @Test
    void incrMissingKeyReturnOneShouldExist() {
        MiniRedisStore testStore = new MiniRedisStore();
        assertEquals(1L, testStore.incr("david"));
        assertEquals("1", testStore.get("david"));
    }

    @Test
    void incrExistingPositiveNumber() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "52");
        assertEquals(53L, testStore.incr("counter"));
        assertEquals("53", testStore.get("counter"));
    }

    @Test
    void incrNegativeNumber() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "-1");
        assertEquals(0L, testStore.incr("counter"));
        assertEquals("0", testStore.get("counter"));
    }

    @Test
    void incrInvalidValueThrowsException() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "banana");
        assertThrows(NumberFormatException.class, () -> testStore.incr("counter"));
    }

    @Test
    void incrInvalidValueDoesNotChangeStoredValue() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "banana");
        assertThrows(NumberFormatException.class, () -> testStore.incr("counter"));
        assertEquals("banana", testStore.get("counter"));
    }

    @Test
    void incrOverflowThrowsException() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", String.valueOf(Long.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> testStore.incr("counter"));
    }

    @Test
    void incrOverflowDoesNotChangeStoredValue() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", String.valueOf(Long.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> testStore.incr("counter"));
        assertEquals(String.valueOf(Long.MAX_VALUE), testStore.get("counter"));
    }

    @Test
    void incrInvalidNumberThrowsException() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "9223372036854775808");
        assertThrows(NumberFormatException.class, () -> testStore.incr("counter"));
    }

    @Test
    void incrInvalidNumberDoesNotChangeStoredValue() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("counter", "9223372036854775808");
        assertThrows(NumberFormatException.class, () -> testStore.incr("counter"));
        assertEquals("9223372036854775808", testStore.get("counter"));
    }

    @Test
    void expireExistingKeyWithPositiveTTL_keepsKeyAvailableAndReturnsOne() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        Clock clock = Clock.fixed(instant, ZoneOffset.UTC);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("name", "bro");
        assertEquals(1L, testStore.expire("name", 5L));
        assertTrue(testStore.exists("name"));
        assertEquals("bro", testStore.get("name"));
    }

    @Test
    void expireExistingKeyWithPositiveTTL_RemovesKeyAfterTTL() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("name", "Gavin");
        testStore.expire("name", 5L);
        clock.advanceSeconds(5L);
        assertFalse(testStore.exists("name"));
    }

    @Test
    void expireOnExistingKey_AndTTLIsZero_RemovesKeyReturnsOne() {
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("name", "Brah");
        assertEquals(1L, testStore.expire("name", 0L));
        assertFalse(testStore.exists("name"));
        assertNull(testStore.get("name"));
    }

    @Test
    void expireOnMissingKey_ReturnsZero_AndDoesNotModifyStore() {
        MiniRedisStore testStore = new MiniRedisStore();
        assertEquals(0L, testStore.expire("something", 5L));
        assertFalse(testStore.exists("something"));
        assertNull(testStore.get("something"));
    }

    @Test
    void expireOnExistingKeyWithExistingTTL_ReplacesTheTTL() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "10");
        testStore.expire("something", 5L);
        clock.advanceSeconds(2L);
        assertTrue(testStore.exists("something"));
        testStore.expire("something", 10L);
        clock.advanceSeconds(3L);
        assertTrue(testStore.exists("something"));
        clock.advanceSeconds(7L);
        assertFalse(testStore.exists("something"));
    }

    @Test
    void setOnExistingKeyWithExistingTTL_RemovesTTL() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "10");
        testStore.expire("something", 5L);
        testStore.set("something", "5");
        clock.advanceSeconds(7L);
        assertTrue(testStore.exists("something"));
        assertEquals("5", testStore.get("something"));
    }

    @Test
    void expireOnExpiredKey_ReturnsZero() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zoneId = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zoneId);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "5");
        testStore.expire("something", 3L);
        clock.advanceSeconds(7L);
        assertEquals(0L, testStore.expire("something", 4L));
        assertFalse(testStore.exists("something"));
        assertNull(testStore.get("something"));
    }

    @Test
    void incrOnExistingKeyWithExistingTTL_DoesNotModifyTheTTL() {
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "10");
        testStore.expire("something", 5L);
        assertEquals(11L,testStore.incr("something"));
        assertEquals("11", testStore.get("something"));
        clock.advanceSeconds(5L);
        assertFalse(testStore.exists("something"));
    }

    @Test
    void expireOnExistingKeyWithTTLOverflow_throwsException(){
        long tooLong= Long.MAX_VALUE;
        long notTooLong = Long.MAX_VALUE-500;
        Instant instant = Instant.ofEpochMilli(notTooLong);
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "10");
        assertThrows(ArithmeticException.class, () -> testStore.expire("something", tooLong));
        testStore.set("somethingElse","5");
        assertThrows(ArithmeticException.class, () -> testStore.expire("somethingElse", 1));
    }

    @Test
    void getOnKeyWithExpiredTTL_ExpiresKeyReturnsNull(){
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zoneId = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant,zoneId);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("Gavin","6");
        testStore.expire("Gavin",5L);
        clock.advanceSeconds(5);
        assertNull(testStore.get("Gavin"));
        assertFalse(testStore.exists("Gavin"));
    }

    @Test
    void incrOnKeyWithExpiredTTL_RecreatesKeyReturnsOne(){
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zoneId = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant,zoneId);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("Gavin","6");
        testStore.expire("Gavin",5L);
        assertEquals("6",testStore.get("Gavin"));
        clock.advanceSeconds(5);
        assertEquals(1L,testStore.incr("Gavin"));
        assertEquals("1",testStore.get("Gavin"));
    }

    @Test
    void deleteOnKeyWithExpiredTTL_ExpiresKeyReturnsZero(){
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zoneId = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant,zoneId);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("Gavin","6");
        testStore.expire("Gavin",5L);
        assertEquals("6",testStore.get("Gavin"));
        clock.advanceSeconds(5);
        assertEquals(0,testStore.delete("Gavin"));
        assertFalse(testStore.exists("Gavin"));
    }

    @Test
    void deleteAndRecreateExpiringKey_NewValueSurvivesOriginalDeadline(){
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zoneId = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant,zoneId);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("Gavin","5");
        testStore.expire("Gavin",5L);
        assertEquals(1,testStore.delete("Gavin"));
        testStore.set("Gavin","10");
        clock.advanceSeconds(5L);
        assertEquals("10",testStore.get("Gavin"));
        assertTrue(testStore.exists("Gavin"));
    }

    @Test
    void expireOnExistingKeyWithInvalidTTL_PreservesPreviousTTL_MultiplicationOverflow(){
        long tooLong= Long.MAX_VALUE;
        Instant instant = Instant.parse("2026-01-01T00:00:00Z");
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "10");
        testStore.expire("something", 5L);
        assertThrows(ArithmeticException.class, () -> testStore.expire("something", tooLong));
        clock.advanceSeconds(4L);
        assertEquals("10",testStore.get("something"));
        clock.advanceSeconds(1L);
        assertFalse(testStore.exists("something"));
    }

    @Test
    void expireOnExistingKeyWithInvalidTTL_PreservesPreviousTTL_AdditionOverflow(){
        long notTooLong = Long.MAX_VALUE-5000;
        Instant instant = Instant.ofEpochMilli(notTooLong);
        ZoneId zone = ZoneOffset.UTC;
        MutableClock clock = new MutableClock(instant, zone);
        MiniRedisStore testStore = new MiniRedisStore(clock);
        testStore.set("something", "10");
        testStore.expire("something", 5L);
        assertThrows(ArithmeticException.class, () -> testStore.expire("something", 10L));
        clock.advanceSeconds(4L);
        assertEquals("10",testStore.get("something"));
        clock.advanceSeconds(1L);
        assertFalse(testStore.exists("something"));
    }

    @Test
    void expireOnExistingKeyWithNegativeTTL_RemovesTheKey(){
        MiniRedisStore testStore = new MiniRedisStore();
        testStore.set("something", "5");
        assertTrue(testStore.exists("something"));
        assertEquals(1L,testStore.expire("something",-7L));
        assertFalse(testStore.exists("something"));
    }

}
