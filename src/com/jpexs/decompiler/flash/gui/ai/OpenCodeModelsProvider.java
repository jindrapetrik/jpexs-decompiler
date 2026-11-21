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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OpenCodeModelsProvider implements AIProvider {

    public static final String ID = "opencode";
    public static final String PLAN_PAY_AS_YOU_GO = "payg";
    public static final String PLAN_GO = "go";
    private static final String PAYG_BASE_URL = "https://opencode.ai/inference";
    private static final String GO_BASE_URL = "https://opencode.ai/inference/go";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "OpenCode Models";
    }

    @Override
    public List<AIModel> loadModels(String apiKey, String plan) throws IOException {
        String response = AIHttpClient.get(modelsEndpoint(plan), apiKey);
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
            String id = string(model.get("id"), null);
            if (id == null || id.trim().isEmpty() || isExplicitlyFree(model, id)) {
                continue;
            }
            JsonObject pricing = object(model.get("pricing"));
            double prompt = number(pricing.get("prompt"), -1);
            double completion = number(pricing.get("completion"), -1);
            JsonObject architecture = object(model.get("architecture"));
            result.add(new AIModel(id, string(model.get("name"), id),
                    string(model.get("description"), ""),
                    false, prompt, completion, pricing.toString(),
                    strings(architecture.get("input_modalities")),
                    strings(architecture.get("output_modalities"))));
        }
        return result;
    }

    @Override
    public String getChatEndpoint(String plan) {
        return baseUrl(plan) + "/openai/v1/chat/completions";
    }

    @Override
    public void addRequestHeaders(HttpURLConnection connection, String plan, String sessionId) {
        connection.setRequestProperty("x-opencode-session", sessionId);
    }

    @Override
    public JsonArray parseModels(String json) {
        JsonValue root = Json.parse(json);
        if (!root.isObject()) {
            throw new IllegalArgumentException("Invalid OpenCode model catalog.");
        }
        JsonValue data = root.asObject().get("data");
        if (data == null || !data.isArray()) {
            throw new IllegalArgumentException("OpenCode returned no model catalog.");
        }
        return data.asArray();
    }

    private static String modelsEndpoint(String plan) {
        return baseUrl(plan) + "/v1/models";
    }

    private static String baseUrl(String plan) {
        return PLAN_GO.equals(plan) ? GO_BASE_URL : PAYG_BASE_URL;
    }

    private static boolean isExplicitlyFree(JsonObject model, String id) {
        String lowerId = id.toLowerCase(Locale.ENGLISH);
        if (lowerId.contains(":free") || lowerId.endsWith("-free") || lowerId.contains("/free/")) {
            return true;
        }
        if (isTrue(model.get("free")) || isTrue(model.get("is_free"))) {
            return true;
        }
        String tier = string(model.get("tier"), "").toLowerCase(Locale.ENGLISH);
        String access = string(model.get("access"), "").toLowerCase(Locale.ENGLISH);
        if ("free".equals(tier) || "free".equals(access)) {
            return true;
        }
        JsonObject pricing = object(model.get("pricing"));
        double prompt = number(pricing.get("prompt"), -1);
        double completion = number(pricing.get("completion"), -1);
        if (prompt != 0 || completion != 0) {
            return false;
        }
        for (JsonObject.Member member : pricing) {
            JsonValue value = member.getValue();
            if (value == null || value.isNull()) {
                continue;
            }
            if (number(value, Double.NaN) != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTrue(JsonValue value) {
        return value != null && value.isBoolean() && value.asBoolean();
    }

    private static JsonObject object(JsonValue value) {
        return value != null && value.isObject() ? value.asObject() : new JsonObject();
    }

    private static String string(JsonValue value, String fallback) {
        return value != null && value.isString() ? value.asString() : fallback;
    }

    private static double number(JsonValue value, double fallback) {
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
        if (value == null || !value.isArray()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (JsonValue entry : value.asArray()) {
            if (entry.isString()) {
                result.add(entry.asString());
            }
        }
        return result;
    }
}
