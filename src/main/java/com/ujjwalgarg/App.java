package com.ujjwalgarg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import com.ujjwalgarg.httpserver.HttpServer;
import com.ujjwalgarg.httpserver.RequestMethod;

/**
 * The App class represents the main entry point of the HTTP server application.
 */
public class App {
    private static int PORT = 4221;

    public static void main(String[] args) {
        // Create an instance of the HTTP server on a specified port
        HttpServer server = new HttpServer(PORT);

        // Add request handlers for different routes and HTTP methods
        server.addHandler(RequestMethod.GET, "^/$", (req, res) -> {
            // Handler for root path (GET /)
            res.setStatusCode(200);
            res.setStatusMsg("OK");
        });
        server.addHandler(RequestMethod.GET, "^/echo/[^/]+$", (req, res) -> {
            // Handler for /echo/<something> (GET)
            String path = req.getPath();
            if (req.getHeader("accept-encoding") != null) {
                String encodingVal = req.getHeader("accept-encoding");
                if (encodingVal.equals("gzip")) {
                    // Compress the response if client accepts gzip encoding
                    String compressed = gzipCompress(path.substring(6));
                    res.addHeader("Content-Encoding", "gzip");
                    res.setResponseBody(compressed);
                } else {
                    res.setResponseBody(path.substring(6));
                }
                res.setStatusCode(200);
                res.addHeader("Content-Type", "text/plain");
                res.setStatusMsg("OK");
                return;
            }

            res.setStatusCode(200);
            res.addHeader("Content-Type", "text/plain");
            res.setResponseBody(path.substring(6));
            res.setStatusMsg("OK");
        });
        server.addHandler(RequestMethod.GET, "^/user-agent$", (req, res) -> {
            // Handler for /user-agent (GET)
            String userAgentHeader = req.getHeader("user-agent");
            res.setStatusCode(200);
            res.setStatusMsg("OK");
            res.addHeader("Content-Type", "text/plain");
            res.setResponseBody(userAgentHeader);
        });
        server.addHandler(RequestMethod.GET, "^/files/[^/]+$", (req, res) -> {
            // Handler for retrieving files (GET /files/<filename>)
            File dir = new File(args[1]);
            String fileToFind = req.getPath().substring(7);
            boolean fileExists = false;
            for (String file : dir.list()) {
                if (fileToFind.equals(file)) {
                    fileExists = true;
                    break;
                }
            }

            if (!fileExists) {
                // Respond with 404 Not Found if file does not exist
                res.setStatusCode(404);
                res.setStatusMsg("Not Found");
                return;
            }

            // Read file contents and set as response body
            res.addHeader("Content-Type", "application/octet-stream");
            BufferedReader br = new BufferedReader(new FileReader(new File(dir, fileToFind)));
            StringBuilder sb = new StringBuilder();
            while (br.ready()) {
                sb.append(br.readLine());
            }
            br.close();
            res.setResponseBody(sb.toString());

            res.setStatusCode(200);
            res.setStatusMsg("OK");
        });
        server.addHandler(RequestMethod.POST, "^/files/[^/]+$", (req, res) -> {
            // Handler for uploading files (POST /files/<filename>)
            String fileToWrite = req.getPath().substring(7);

            // Write request body (file content) to specified file
            File newFile = new File(args[1] + fileToWrite);
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
            bw.write(req.getRequestBody());
            bw.close();

            res.setStatusCode(201);
            res.setStatusMsg("Created");
        });

        // Start the HTTP server
        server.start();
    }

    /**
     * Compresses a string using GZIP compression and returns the base64-encoded
     * result.
     * 
     * @param s the string to compress
     * @return the base64-encoded compressed string
     * @throws IOException if an I/O error occurs
     */
    private static String gzipCompress(String s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
            byte[] input = s.getBytes("UTF-8");
            gzipOutputStream.write(input, 0, input.length);
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
