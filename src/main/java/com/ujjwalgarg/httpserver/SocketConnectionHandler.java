package com.ujjwalgarg.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The SocketConnectionHandler class implements the Runnable interface to handle
 * individual client connections.
 * It reads the request, matches it to a handler, and sends the appropriate
 * response.
 */
public class SocketConnectionHandler implements Runnable {

    private final Socket clientSocket; // Client socket for this connection
    private final Map<String, Map<RequestMethod, RequestHandler>> handlers; // Handlers for different routes and methods

    /**
     * Constructs a SocketConnectionHandler with the specified client socket and
     * handlers map.
     * 
     * @param clientSocket the socket connected to the client
     * @param handlers     a map of route patterns to method-specific request
     *                     handlers
     */
    public SocketConnectionHandler(Socket clientSocket, Map<String, Map<RequestMethod, RequestHandler>> handlers) {
        this.clientSocket = clientSocket;
        this.handlers = handlers;
    }

    /**
     * Sends a basic HTTP response with the given status code and message.
     * 
     * @param statusCode the HTTP status code
     * @param statusMsg  the HTTP status message
     * @param out        the OutputStream to send the response to
     * @throws IOException if an I/O error occurs
     */
    private void respond(int statusCode, String statusMsg, OutputStream out) throws IOException {
        out.write("HTTP/1.1 %d %s\r\n\r\n".formatted(statusCode, statusMsg).getBytes());
    }

    /**
     * Handles the client connection. This method reads the request, finds the
     * appropriate handler, and sends the response.
     */
    @Override
    public void run() {
        BufferedReader in = null;
        OutputStream out = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = clientSocket.getOutputStream();

            // Parse the request
            Request req = new Request(in);
            if (!req.parse()) {
                respond(500, "Unable to parse request", out);
                return;
            }

            // Find the handler for the request path and method
            Map<RequestMethod, RequestHandler> methodHandler = null;
            for (Map.Entry<String, Map<RequestMethod, RequestHandler>> p : handlers.entrySet()) {
                if (Pattern.matches(p.getKey(), req.getPath())) {
                    methodHandler = p.getValue();
                    break; // Path matched, stop searching
                }
            }

            // If no handler was found, respond with 404 Not Found
            if (methodHandler == null) {
                respond(404, "Not Found", out);
                return;
            }

            // Find the handler for the request method
            System.out.println(req);
            RequestHandler handler = methodHandler.get(req.getMethod());
            if (handler == null) {
                respond(405, "Method not allowed", out);
                return;
            }

            // Handle the request and send the response
            Response res = new Response(out);
            handler.handle(req, res);
            res.send();
        } catch (IOException e) {
            try {
                respond(500, "Server Error", out);
                e.printStackTrace();
            } catch (IOException err) {
                err.printStackTrace();
            }
        } finally {
            // Close resources
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error while closing InputStream, OutputStream, and Client Socket");
                e.printStackTrace();
            }
        }
    }

}
