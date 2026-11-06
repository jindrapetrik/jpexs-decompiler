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
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * OpenCode models available to the embedded OpenCode CLI.
 */
public class OpenCodeEmbeddedProvider implements AIProvider {

    public static final String ID = "opencode-cli";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "OpenCode Embedded (CLI)";
    }

    @Override
    public List<AIModel> loadModels(String apiKey, String plan) throws IOException {
        List<String> ids = OpenCodeEmbeddedRuntime.listModels();
        List<AIModel> result = new ArrayList<>();
        for (String id : ids) {
            result.add(new AIModel(id, id, "", isFreeId(id), -1, -1, "{}",
                    Collections.<String>emptyList(), Collections.<String>emptyList()));
        }
        return result;
    }

    @Override
    public void testConnection(String apiKey, String plan) throws IOException {
        loadModels(apiKey, plan);
    }

    @Override
    public String getChatEndpoint(String plan) {
        // The embedded provider does not make HTTP chat requests.
        return "embedded";
    }

    @Override
    public void addRequestHeaders(java.net.HttpURLConnection connection, String plan, String sessionId) {
        // Not used by the embedded provider.
    }

    @Override
    public JsonArray parseModels(String json) {
        JsonValue value = Json.parse(json);
        if (value.isArray()) {
            return value.asArray();
        }
        return new JsonArray();
    }

    private static boolean isFreeId(String id) {
        String lower = id == null ? "" : id.toLowerCase(Locale.ROOT);
        return lower.contains(":free") || lower.endsWith("-free") || lower.contains("/free/")
                || lower.contains("free-");
    }
}
