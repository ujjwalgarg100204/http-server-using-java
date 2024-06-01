package com.ujjwalgarg.httpserver;

import java.io.IOException;

/**
 * The RequestHandler interface defines a contract for handling HTTP requests.
 * Implementing classes should provide the logic to process the request and
 * generate a corresponding response.
 */
@FunctionalInterface
public interface RequestHandler {

    /**
     * Handles an HTTP request and generates a response.
     *
     * @param req the HTTP request to handle
     * @param res the HTTP response to populate
     * @throws IOException if an I/O error occurs while handling the request
     */
    public void handle(Request req, Response res) throws IOException;

}
