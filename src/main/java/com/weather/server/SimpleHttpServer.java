package com.weather.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleHttpServer {
    private final int port;
    private final ExecutorService threadPool;
    private final WeatherHandler weatherHandler;
    private volatile boolean running;

    public SimpleHttpServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(10);
        this.weatherHandler = new WeatherHandler();
    }

    public void start() throws IOException {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Weather service started on port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();

            if (line != null && line.startsWith("GET")) {
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    String path = parts[1];
                    if (path.startsWith("/weather")) {
                        String query = path.contains("?") ? path.split("\\?")[1] : null;
                        weatherHandler.handleWeatherRequest(query, output);
                    } else {
                        sendNotFound(output);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void sendNotFound(OutputStream output) throws IOException {
        String response = """
                HTTP/1.1 404 Not Found\r
                Content-Type: text/plain\r
                \r
                404 Not Found""";
        output.write(response.getBytes());
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
    }

    public static void main(String[] args) {
        SimpleHttpServer server = new SimpleHttpServer(8082);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
