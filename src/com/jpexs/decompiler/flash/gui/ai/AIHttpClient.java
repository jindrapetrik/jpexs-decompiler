/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

final class AIHttpClient {

    private static final int CONNECT_TIMEOUT_MILLIS = 15000;
    private static final int READ_TIMEOUT_MILLIS = 120000;
    private static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;
    private static final int MAX_STREAMED_CHARS = 4 * 1024 * 1024;

    private AIHttpClient() {
    }

    static String get(String endpoint, String apiKey) throws IOException {
        HttpURLConnection connection = open(endpoint, "GET", apiKey);
        return readResponse(connection);
    }

    static String postStreaming(AIProvider provider, String endpoint, String apiKey, String plan,
            String sessionId, JsonObject request, Consumer<String> onChunk) throws IOException {
        HttpURLConnection connection = open(endpoint, "POST", apiKey);
        try {
            provider.addRequestHeaders(connection, plan, sessionId);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                String response = readResponse(connection);
                throw statusException(status, response, connection.getURL().toString());
            }

            StringBuilder answer = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    String piece = extractDelta(data);
                    if (!piece.isEmpty()) {
                        if (answer.length() + piece.length() > MAX_STREAMED_CHARS) {
                            throw new AIProviderException("The streamed answer exceeded the size limit.");
                        }
                        answer.append(piece);
                        onChunk.accept(piece);
                    }
                }
            }
            return answer.toString();
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection open(String endpoint, String method, String apiKey) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        connection.setRequestMethod(method);
        if (apiKey != null && !apiKey.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        }
        connection.setRequestProperty("User-Agent", "JPEXS-Free-Flash-Decompiler");
        return connection;
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) {
            connection.disconnect();
            if (status < 200 || status >= 300) {
                throw statusException(status, "", connection.getURL().toString());
            }
            throw new AIProviderException("The provider returned an empty response.");
        }
        try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > MAX_RESPONSE_BYTES) {
                    throw new AIProviderException("The provider response exceeded the size limit.");
                }
                output.write(buffer, 0, count);
            }
            String response = new String(output.toByteArray(), StandardCharsets.UTF_8);
            if (status < 200 || status >= 300) {
                throw statusException(status, response, connection.getURL().toString());
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private static AIProviderException statusException(int status, String response, String endpoint) {
        if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return new AIProviderException("Authentication failed. Check the API key.");
        }
        if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            if (endpoint.contains("opencode.ai")) {
                return new AIProviderException("OpenCode returned HTTP 403. Free-tier chat models "
                        + "are restricted to OpenCode itself. Choose a paid/Go model; if you already "
                        + "did, verify the key's workspace and plan.");
            }
            return new AIProviderException("The provider denied access (HTTP 403). Check the API key "
                    + "and the selected model's access requirements.");
        }
        if (status == 429) {
            return new AIProviderException("The provider rate limit was reached. Try again later.");
        }
        if (status >= 300 && status < 400) {
            return new AIProviderException("The provider redirected the request; it was not followed "
                    + "to protect the API key.");
        }
        String error = "";
        try {
            com.eclipsesource.json.JsonValue value = Json.parse(response);
            if (value.isObject()) {
                com.eclipsesource.json.JsonValue message = value.asObject().get("error");
                if (message != null && message.isObject()) {
                    com.eclipsesource.json.JsonValue detail = message.asObject().get("message");
                    if (detail != null && detail.isString()) {
                        error = " " + detail.asString();
                    }
                } else if (message != null && message.isString()) {
                    error = " " + message.asString();
                }
            }
        } catch (RuntimeException ignored) {
            // The status code is still useful if an upstream error body is not JSON.
        }
        return new AIProviderException("Provider request failed (HTTP " + status + ")." + error);
    }

    private static String extractDelta(String data) throws AIProviderException {
        try {
            com.eclipsesource.json.JsonValue value = Json.parse(data);
            if (!value.isObject()) {
                return "";
            }
            com.eclipsesource.json.JsonValue error = value.asObject().get("error");
            if (error != null && !error.isNull()) {
                String message = error.isObject()
                        ? error.asObject().getString("message", "The provider returned a stream error.")
                        : error.toString();
                throw new AIProviderException(message);
            }
            com.eclipsesource.json.JsonValue choices = value.asObject().get("choices");
            if (choices == null || !choices.isArray() || choices.asArray().isEmpty()) {
                return "";
            }
            com.eclipsesource.json.JsonValue delta = choices.asArray().get(0).asObject().get("delta");
            if (delta == null || !delta.isObject()) {
                return "";
            }
            com.eclipsesource.json.JsonValue content = delta.asObject().get("content");
            return content != null && content.isString() ? content.asString() : "";
        } catch (AIProviderException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AIProviderException("Received invalid streaming data from the provider.", ex);
        }
    }
}
