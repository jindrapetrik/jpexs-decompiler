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
package com.jpexs.decompiler.flash.tags.base;

import java.util.Map;

/**
 * Import tag interface.
 *
 * @author JPEXS
 */
public interface ImportTag {

    /**
     * Get URL of the imported file.
     * @return URL of the imported file
     */
    public String getUrl();

    /**
     * Set URL of the imported file.
     * @param url URL of the imported file
     */
    public void setUrl(String url);

    /**
     * Gets map of assets.
     * @return Map of assets - character id to name
     */
    public Map<Integer, String> getAssets();
}
