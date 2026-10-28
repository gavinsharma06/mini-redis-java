package dev.miniredis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MiniRedisServerTest {

    @Test
    void startCreatesAndClosesListeningSocket_AndDoesNotThrowException(){
        MiniRedisServer server = new MiniRedisServer(6379);
        assertDoesNotThrow(() -> server.start());
    }
}
