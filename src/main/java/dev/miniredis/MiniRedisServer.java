package dev.miniredis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiniRedisServer  {


    private final int port;

    public MiniRedisServer (int port){
        this.port = port;
    }

    public void start() throws IOException{
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("MiniRedis listening on port " + socket.getLocalPort());

            try(Socket client = socket.accept()){
                System.out.println("Client Connected!");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MiniRedisServer server = new MiniRedisServer(6379);
        server.start();
    }
}
