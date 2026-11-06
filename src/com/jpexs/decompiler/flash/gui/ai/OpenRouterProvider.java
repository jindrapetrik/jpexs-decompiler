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
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class OpenRouterProvider implements AIProvider {

    public static final String ID = "openrouter";
    private static final String MODELS_URL = "https://openrouter.ai/api/v1/models";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "OpenRouter";
    }

    @Override
    public List<AIModel> loadModels(String apiKey, String plan) throws IOException {
        String response = AIHttpClient.get(MODELS_URL, apiKey);
        return parseModelCatalog(response);
    }

    List<AIModel> parseModelCatalog(String response) {
        JsonArray models = parseModels(response);
        List<AIModel> result = new ArrayList<>();
        for (JsonValue item : models) {
            if (!item.isObject()) {
                continue;
            }
            JsonObject model = item.asObject();
            String id = stringValue(model.get("id"), null);
            if (id == null || id.trim().isEmpty()) {
                continue;
            }
            JsonObject pricing = objectValue(model.get("pricing"));
            double prompt = numberValue(pricing.get("prompt"), -1);
            double completion = numberValue(pricing.get("completion"), -1);
            boolean free = isFreePricing(pricing, prompt, completion);
            JsonObject architecture = objectValue(model.get("architecture"));
            result.add(new AIModel(id,
                    stringValue(model.get("name"), id),
                    stringValue(model.get("description"), ""),
                    free, prompt, completion,
                    pricing.toString(),
                    strings(architecture.get("input_modalities")),
                    strings(architecture.get("output_modalities"))));
        }
        return result;
    }

    @Override
    public void testConnection(String apiKey, String plan) throws IOException {
        String response = AIHttpClient.get("https://openrouter.ai/api/v1/key", apiKey);
        JsonValue root = Json.parse(response);
        JsonValue data = root.isObject() ? root.asObject().get("data") : null;
        if (data == null || !data.isObject()) {
            throw new AIProviderException("OpenRouter did not return valid API-key details.");
        }
    }

    @Override
    public String getChatEndpoint(String plan) {
        return "https://openrouter.ai/api/v1/chat/completions";
    }

    @Override
    public void addRequestHeaders(HttpURLConnection connection, String plan, String sessionId) {
    }

    @Override
    public JsonArray parseModels(String json) {
        JsonValue root = Json.parse(json);
        if (!root.isObject()) {
            throw new IllegalArgumentException("Invalid OpenRouter model catalog.");
        }
        JsonValue data = root.asObject().get("data");
        if (data == null || !data.isArray()) {
            throw new IllegalArgumentException("OpenRouter returned no model catalog.");
        }
        return data.asArray();
    }

    private static boolean isFreePricing(JsonObject pricing, double prompt, double completion) {
        if (prompt != 0 || completion != 0) {
            return false;
        }
        boolean foundPrice = false;
        for (JsonObject.Member member : pricing) {
            JsonValue value = member.getValue();
            if (value == null || value.isNull()) {
                continue;
            }
            double price;
            if (value.isNumber()) {
                price = value.asDouble();
            } else if (value.isString()) {
                try {
                    price = Double.parseDouble(value.asString());
                } catch (NumberFormatException ex) {
                    return false;
                }
            } else {
                return false;
            }
            foundPrice = true;
            if (price != 0) {
                return false;
            }
        }
        return foundPrice;
    }

    private static JsonObject objectValue(JsonValue value) {
        return value != null && value.isObject() ? value.asObject() : new JsonObject();
    }

    private static String stringValue(JsonValue value, String fallback) {
        return value != null && value.isString() ? value.asString() : fallback;
    }

    private static double numberValue(JsonValue value, double fallback) {
        if (value == null || value.isNull()) {
            return fallback;
        }
        try {
            return value.isNumber() ? value.asDouble()
                    : value.isString() ? Double.parseDouble(value.asString()) : fallback;
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static List<String> strings(JsonValue value) {
        List<String> result = new ArrayList<>();
        if (value != null && value.isArray()) {
            for (JsonValue entry : value.asArray()) {
                if (entry.isString()) {
                    result.add(entry.asString());
                }
            }
        }
        return result;
    }
}
