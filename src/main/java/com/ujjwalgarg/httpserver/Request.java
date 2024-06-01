package com.ujjwalgarg.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Request class represents an HTTP request.
 * It includes methods to parse and retrieve details about the request such as
 * method, path, headers, and query parameters.
 */
public class Request {
    private RequestMethod method; // HTTP method (e.g., GET, POST)
    private String path; // Request path (e.g., /index.html)
    private String httpVersion; // HTTP version (e.g., HTTP/1.1)
    private final Map<String, String> headers; // Stores HTTP headers
    private final Map<String, String> queryParameters; // Stores query parameters
    private String requestBody; // Body of the request
    private final BufferedReader in; // BufferedReader to read the request from

    /**
     * Constructs a Request object with the specified BufferedReader.
     * 
     * @param in the BufferedReader to read the request from
     */
    public Request(BufferedReader in) {
        this.in = in;
        this.headers = new HashMap<>();
        this.queryParameters = new HashMap<>();
    }

    /**
     * Returns the HTTP method of the request.
     * 
     * @return the method
     */
    public RequestMethod getMethod() {
        return method;
    }

    /**
     * Returns the path of the request.
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the HTTP version of the request.
     * 
     * @return the HTTP version
     */
    public String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Returns the all headers of the request.
     * 
     * @return a map of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the value of a specific header.
     * 
     * @param key the name of the header
     * @return the value of the header
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * Returns the body of the request.
     * 
     * @return the request body
     */
    public String getRequestBody() {
        return requestBody;
    }

    /**
     * Returns the query parameters of the request.
     * 
     * @return a map of query parameters
     */
    public Map<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    /**
     * Returns the value of a specific query parameter.
     * 
     * @param key the name of the query parameter
     * @return the value of the query parameter
     */
    public String getQueryParameter(String key) {
        return this.queryParameters.get(key);
    }

    /**
     * Parses the HTTP request from the BufferedReader.
     * 
     * @return true if the request was parsed successfully, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean parse() throws IOException {
        // Read and parse the request line
        String reqLine = this.in.readLine();
        String[] splittedReqLine = reqLine.split(" ");
        System.out.println(reqLine);

        this.method = RequestMethod.valueOf(splittedReqLine[0]);
        this.path = splittedReqLine[1];
        this.httpVersion = splittedReqLine[2].substring(0, splittedReqLine[2].length() - 1);

        // Parse query parameters
        if (!this.__parseQueryParameters()) {
            return false;
        }

        // Parse headers
        while (true) {
            String headerLine = this.in.readLine();
            System.out.println(headerLine);
            if (headerLine.length() == 0) {
                break;
            }

            int headerDivider = headerLine.indexOf(":");
            if (headerDivider == -1) {
                return false;
            }

            headers.put(headerLine.substring(0, headerDivider).toLowerCase(),
                    headerLine.substring(headerDivider + 2));
        }

        // Parse request body
        StringBuilder sb = new StringBuilder();
        while (this.in.ready()) {
            sb.append((char) this.in.read());
        }
        this.requestBody = sb.toString();

        return true;
    }

    /**
     * Parses the query parameters from the request path.
     * 
     * @return true if query parameters were parsed successfully, false otherwise
     */
    private boolean __parseQueryParameters() {
        int queryParamsStartIndex = this.path.indexOf("?");
        if (queryParamsStartIndex == -1) {
            return true;
        }

        String queryParams = this.path.substring(queryParamsStartIndex + 1); // +1 to skip the "?"
        for (String parameter : queryParams.split("&")) {
            if (parameter.indexOf("=") != -1) {
                String[] keyVal = parameter.split("=");
                this.queryParameters.put(keyVal[0], keyVal[1]);
            } else {
                this.queryParameters.put(parameter, null);
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "Request [method=" + method + ", path=" + path + ", httpVersion=" + httpVersion + ", headers=" + headers
                + ", queryParameters=" + queryParameters + ", requestBody=" + requestBody + "]";
    }
}
