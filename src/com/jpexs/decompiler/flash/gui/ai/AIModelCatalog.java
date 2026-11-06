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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AIModelCatalog {

    public enum Filter {
        ALL, FREE, PAID
    }

    public List<AIModel> load(String providerId, String plan) throws IOException {
        AIProvider provider = AIProviderManager.getInstance().getProvider(providerId);
        String apiKey = "";
        if (AICredentialManager.isUnlocked()) {
            apiKey = AIProviderManager.getInstance().getApiKey(providerId);
        } else if (!OpenRouterProvider.ID.equals(providerId)) {
            throw new IOException("Unlock the credential store and configure an API key in AI Settings.");
        }
        return provider.loadModels(apiKey, plan);
    }

    public List<AIModel> filter(List<AIModel> models, Filter filter) {
        if (filter == Filter.ALL) {
            return Collections.unmodifiableList(new ArrayList<>(models));
        }
        List<AIModel> result = new ArrayList<>();
        for (AIModel model : models) {
            if ((filter == Filter.FREE) == model.isFree()) {
                result.add(model);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
