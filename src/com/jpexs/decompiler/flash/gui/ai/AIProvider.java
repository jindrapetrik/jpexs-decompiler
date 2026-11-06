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
import java.io.IOException;
import java.util.List;

public interface AIProvider {

    String getId();

    String getDisplayName();

    List<AIModel> loadModels(String apiKey, String plan) throws IOException;

    default void testConnection(String apiKey, String plan) throws IOException {
        loadModels(apiKey, plan);
    }

    String getChatEndpoint(String plan);

    void addRequestHeaders(java.net.HttpURLConnection connection, String plan, String sessionId);

    JsonArray parseModels(String json);
}
