package com.ujjwalgarg.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The Response class represents an HTTP response.
 * It includes methods to set the status code, status message, headers, and body
 * of the response.
 * It also provides a method to send the response to the client.
 */
public class Response {

    private final Map<String, String> headers; // Stores HTTP headers
    private int statusCode; // HTTP status code (e.g., 200, 404)
    private String statusMsg; // HTTP status message (e.g., "OK", "Not Found")
    private String responseBody; // Body of the response
    private final OutputStream out; // Output stream to write the response to

    /**
     * Constructs a Response object with the specified output stream.
     * 
     * @param out the output stream to write the response to
     */
    public Response(OutputStream out) {
        this.out = out;
        this.headers = new HashMap<>();
    }

    /**
     * Adds a header to the response.
     * 
     * @param key the name of the header
     * @param val the value of the header
     */
    public void addHeader(String key, String val) {
        this.headers.put(key.toLowerCase(), val);
    }

    /**
     * Returns the headers of the response.
     * 
     * @return a map of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the status code of the response.
     * 
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the status code of the response.
     * 
     * @param responseCode the status code to set
     */
    public void setStatusCode(int responseCode) {
        this.statusCode = responseCode;
    }

    /**
     * Returns the status message of the response.
     * 
     * @return the status message
     */
    public String getStatusMsg() {
        return statusMsg;
    }

    /**
     * Sets the status message of the response.
     * 
     * @param responseMsg the status message to set
     */
    public void setStatusMsg(String responseMsg) {
        this.statusMsg = responseMsg;
    }

    /**
     * Returns the body of the response.
     * 
     * @return the response body
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Sets the body of the response and updates the Content-Length header.
     * 
     * @param responseBody the response body to set
     */
    public void setResponseBody(String responseBody) {
        this.headers.put("Content-Length", Integer.toString(responseBody.length()));
        this.responseBody = responseBody;
    }

    /**
     * Sends the response to the client by writing the status line, headers, and
     * body to the output stream.
     * 
     * @throws IOException if an I/O error occurs
     */
    public void send() throws IOException {
        headers.put("Connection", "close"); // Ensure the connection is closed after the response is sent
        this.out.write(String.format("HTTP/1.1 %d %s\r\n", this.statusCode, this.statusMsg).getBytes());

        // Write headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            this.out.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()).getBytes());
        }

        // Write a blank line to separate headers from the body
        this.out.write("\r\n".getBytes());

        // Write the response body, if it exists
        if (responseBody != null) {
            this.out.write(responseBody.getBytes());
        }

        // Close the output stream
        this.out.close();
    }
}
