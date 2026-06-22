package com.travelplanner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Minimal blocking HTTP GET/POST client built on {@link HttpURLConnection} - deliberately
 * avoids java.net.http.HttpClient (Java 11+) so the backend keeps compiling on Java 8.
 */
public final class SimpleHttpClient {

    private SimpleHttpClient() {
    }

    public static String getJson(String url, Map<String, String> headers, int connectTimeoutMs, int readTimeoutMs)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        int status = connection.getResponseCode();
        InputStream stream = (status >= 200 && status < 300) ? connection.getInputStream() : connection.getErrorStream();
        String body = readAll(stream);

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " from " + url + ": " + body);
        }
        return body;
    }

    public static String postJson(String url, Map<String, String> headers, String jsonBody,
                                   int connectTimeoutMs, int readTimeoutMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        byte[] payload = jsonBody.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(payload.length);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(payload);
        }

        int status = connection.getResponseCode();
        InputStream stream = (status >= 200 && status < 300) ? connection.getInputStream() : connection.getErrorStream();
        String body = readAll(stream);

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " from " + url + ": " + body);
        }
        return body;
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 must be supported", e);
        }
    }

    private static String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
