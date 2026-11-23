package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

public class MiniRedisServerTest {

    @Test
    void startProcessesMultipleRespCommandsOverTcp() throws Exception {
        CountDownLatch serverReady = new CountDownLatch(1);
        String request = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$5\r\nGavin\r\n";
        String getRequest = "*2\r\n$3\r\nGET\r\n$4\r\nname\r\n";
        AtomicInteger boundPort = new AtomicInteger(0);
        MiniRedisServer server = new MiniRedisServer(0,serverReady, boundPort);
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
        try (Socket client = new Socket("127.0.0.1", boundPort.get());) {
            OutputStream output = client.getOutputStream();
            output.write(request.getBytes(StandardCharsets.UTF_8));
            InputStream input = client.getInputStream();
            client.setSoTimeout(3000);
            byte[] response = input.readNBytes(5);
            String actual = new String(response, StandardCharsets.UTF_8);
            assertEquals("+OK\r\n", actual);
            output.write(getRequest.getBytes(StandardCharsets.UTF_8));
            byte[] getResponse = input.readNBytes(11);
            String getActual = new String(getResponse, StandardCharsets.UTF_8);
            assertEquals("$5\r\nGavin\r\n",getActual);
            client.shutdownOutput();
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

    @Test
    void malformedRespTerminatesServerWithIOException() throws Exception{
        CountDownLatch serverReady = new CountDownLatch(1);
        String malformedRequest = "*1\r\n!3\r\nGET\r\n";
        AtomicInteger boundPort = new AtomicInteger(0);
        MiniRedisServer server = new MiniRedisServer(0,serverReady, boundPort);
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
        try (Socket client = new Socket("127.0.0.1", boundPort.get());) {
            OutputStream output = client.getOutputStream();
            output.write(malformedRequest.getBytes(StandardCharsets.UTF_8));
            client.shutdownOutput();
        }
        serverThread.join(2000);
        assertFalse(serverThread.isAlive());
        assertInstanceOf(IOException.class, serverFailure.get());
    }
}
