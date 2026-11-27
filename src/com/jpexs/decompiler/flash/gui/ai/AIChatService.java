/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class AIChatService {

    public String newSessionId() {
        return "ses_" + UUID.randomUUID().toString().replace("-", "");
    }

    public String send(String providerId, String plan, String modelId, String context,
            List<AIChatMessage> history, String sessionId, Consumer<String> onChunk)
            throws IOException {
        AIProvider provider = AIProviderManager.getInstance().getProvider(providerId);
        String apiKey = AIProviderManager.getInstance().getApiKey(providerId);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AIProviderException("No API key is saved for " + provider.getDisplayName()
                    + ". Configure it in Settings > Artificial Intelligence.");
        }
        JsonArray messages = new JsonArray();
        if (context != null && !context.trim().isEmpty()) {
            messages.add(new JsonObject().add("role", "system").add("content", context));
        }
        for (AIChatMessage message : history) {
            messages.add(new JsonObject().add("role", message.getRole())
                    .add("content", message.getContent()));
        }
        JsonObject request = new JsonObject()
                .add("model", modelId)
                .add("messages", messages)
                .add("stream", true);
        return AIHttpClient.postStreaming(provider, provider.getChatEndpoint(plan), apiKey,
                plan, sessionId, request, onChunk);
    }
}
