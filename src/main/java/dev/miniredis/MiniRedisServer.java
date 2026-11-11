package dev.miniredis;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class MiniRedisServer  {


    private final int port;
    private final CountDownLatch serverReady;
    private final AtomicInteger boundPort;


    public MiniRedisServer (int port){
        this(port, new CountDownLatch(1));
    }
    public MiniRedisServer(int port, CountDownLatch serverReady){
        this(port, serverReady, new AtomicInteger(0));
    }
    public MiniRedisServer(int port, CountDownLatch serverReady, AtomicInteger boundPort){
        if (0 > port || port > 65535){
            throw new IllegalArgumentException("Invalid port");
        }
        this.port = port;
        this.serverReady = serverReady;
        this.boundPort = boundPort;
    }

    public void start() throws IOException{
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("MiniRedis listening on port " + socket.getLocalPort());
            boundPort.set(socket.getLocalPort());
            serverReady.countDown();
            try(Socket client = socket.accept()){
                InputStream input = client.getInputStream();
                System.out.println("Client Connected!");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MiniRedisServer server = new MiniRedisServer(6379);
        server.start();
    }
}
