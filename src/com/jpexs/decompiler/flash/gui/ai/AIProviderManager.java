/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AIProviderManager {

    private static final AIProviderManager INSTANCE = new AIProviderManager();
    private final Map<String, AIProvider> providers = new LinkedHashMap<>();

    private AIProviderManager() {
        register(new OpenRouterProvider());
        register(new OpenCodeModelsProvider());
        register(new OpenCodeEmbeddedProvider());
    }

    public static AIProviderManager getInstance() {
        return INSTANCE;
    }

    public synchronized void register(AIProvider provider) {
        providers.put(provider.getId(), provider);
    }

    public synchronized AIProvider getProvider(String id) {
        AIProvider provider = providers.get(id);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown AI provider: " + id);
        }
        return provider;
    }

    public synchronized List<AIProvider> getProviders() {
        return Collections.unmodifiableList(Arrays.asList(
                providers.values().toArray(new AIProvider[0])));
    }

    public String getApiKey(String providerId) throws IOException {
        return AICredentialManager.getCredential(providerId);
    }
}
