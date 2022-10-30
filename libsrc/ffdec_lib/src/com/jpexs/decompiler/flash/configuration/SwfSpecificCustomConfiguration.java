/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SwfSpecificCustomConfiguration implements Serializable {
    
    private Map<String, String> customData = new HashMap<>();
    
    public static final String KEY_LAST_SELECTED_PATH_RESOURCES = "lastSelectedPath.resources";
    public static final String KEY_LAST_SELECTED_PATH_TAGLIST = "lastSelectedPath.taglist";    
    
    public String getCustomData(String key, String defaultValue) {
        if (customData.containsKey(key)) {
            return customData.get(key);
        }
        
        return defaultValue;
    }
    
    public void setCustomData(String key, String value) {
        customData.put(key, value);
    }
}
