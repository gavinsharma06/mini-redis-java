package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class MiniRedisServerTest {

    @Test
    void startAcceptsSingleClientConnection() throws Exception {
        CountDownLatch serverReady = new CountDownLatch(1);
        MiniRedisServer server = new MiniRedisServer(6380,serverReady);
        AtomicReference<Throwable> serverFailure = new AtomicReference<>();
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                serverFailure.set(e);
            }
        });
        serverThread.start();
        assertTrue(serverReady.await(2, TimeUnit.SECONDS));
        try (Socket socket = new Socket("127.0.0.1", 6380);) {

        }
        serverThread.join(2000);
        assertFalse(serverThread.isAlive());
        assertNull(serverFailure.get());

    }

    @Test
    void miniRedisServerOnInvalidPortThrowsIllegalArgumentException(){
        assertThrows(IllegalArgumentException.class, () -> new MiniRedisServer(-1));
        assertDoesNotThrow(()-> new MiniRedisServer(0));
        assertDoesNotThrow(()-> new MiniRedisServer(6379));
        assertThrows(IllegalArgumentException.class, () -> new MiniRedisServer(65536));
    }
}
