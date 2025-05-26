/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SWF specific custom configuration.
 *
 * @author JPEXS
 */
public class SwfSpecificCustomConfiguration implements Serializable {

    private static final long serialVersionUID = 0x2acb421da57f5eb4L;

    private Map<String, String> customData = new HashMap<>();

    public static final String LIST_SEPARATOR = "{*sep*}";

    public Map<String, String> getAllCustomData() {
        return customData;
    }        

    public List<String> getCustomDataAsList(String key) {
        String data = getCustomData(key, "");
        String[] parts = (data + LIST_SEPARATOR).split(Pattern.quote(LIST_SEPARATOR));
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        return result;
    }

    public String getCustomData(String key, String defaultValue) {
        if (customData.containsKey(key)) {
            return customData.get(key);
        }

        return defaultValue;
    }

    public void setCustomData(String key, String value) {
        customData.put(key, value);
    }

    public void setCustomData(String key, Iterable<? extends CharSequence> value) {
        customData.put(key, String.join(LIST_SEPARATOR, value));
    }
}
