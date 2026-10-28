package dev.miniredis;

import java.io.IOException;
import java.net.ServerSocket;

public class MiniRedisServer  {


    private final int port;

    public MiniRedisServer (int port){
        this.port = port;
    }

    public void start() throws IOException{
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("MiniRedis listening on port " + socket.getLocalPort());
        }
    }
}
