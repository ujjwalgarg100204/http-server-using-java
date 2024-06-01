package com.ujjwalgarg.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * The HttpServer class represents an HTTP server that listens on a specified
 * port and handles incoming requests.
 */
public class HttpServer {
    private final int port; // Port on which the server listens for incoming connections
    private final Map<String, Map<RequestMethod, RequestHandler>> handlers; // Map of route patterns to request handlers

    /**
     * Constructs an HttpServer object with the specified port.
     * 
     * @param port the port on which the server listens for incoming connections
     */
    public HttpServer(int port) {
        this.port = port;
        this.handlers = new HashMap<>();
    }

    /**
     * Adds a request handler for a specific HTTP method and route pattern.
     * 
     * @param reqMethod the HTTP method (GET, POST, etc.)
     * @param pattern   the route pattern (regex) to match incoming requests
     * @param handler   the request handler to process incoming requests matching
     *                  the pattern and method
     */
    public void addHandler(RequestMethod reqMethod, String pattern, RequestHandler handler) {
        // Ensure the map contains an entry for the pattern
        if (!handlers.containsKey(pattern)) {
            handlers.put(pattern, new HashMap<>());
        }

        // Add the handler for the specified HTTP method and pattern
        handlers.get(pattern).put(reqMethod, handler);
    }

    /**
     * Starts the HTTP server, listening for incoming connections on the specified
     * port.
     * 
     * The server accepts incoming connections in a loop and spawns a new thread for
     * each connection,
     * which is then handled by a SocketConnectionHandler.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Listening on port: %d".formatted(port));

            // Accept incoming connections in a loop
            Socket clientSocket = null;
            while ((clientSocket = serverSocket.accept()) != null) {
                // Create a new SocketConnectionHandler for each connection and start it in a
                // new thread
                SocketConnectionHandler socketConnHandler = new SocketConnectionHandler(clientSocket, handlers);
                Thread thread = new Thread(socketConnHandler);
                thread.start();
            }

        } catch (IOException e) {
            // Handle IOException if occurred while creating the ServerSocket or accepting
            // connections
            System.out.println("Error in SocketHandler");
            e.printStackTrace();
        }
    }
}
