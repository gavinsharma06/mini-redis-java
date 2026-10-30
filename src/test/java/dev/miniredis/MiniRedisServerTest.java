package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class MiniRedisServerTest {

    @Test
    void startAcceptsSingleClientConnection() throws Exception {
        MiniRedisServer server = new MiniRedisServer(6380);
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        serverThread.start();
        Thread.sleep(500);
        try (Socket socket = new Socket("127.0.0.1", 6380);) {

        }
        serverThread.join(2000);
        assertFalse(serverThread.isAlive());


    }
}
