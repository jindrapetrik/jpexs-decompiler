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
package com.jpexs.decompiler.flash.cache;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.helpers.Cache;

/**
 * Cache for AS3 decompiled code.
 *
 * @author JPEXS
 */
public class AS3Cache {

    private final Cache<ScriptPack, HighlightedText> cache = Cache.getInstance(true, false, "as3", false);

    /**
     * Constructor.
     */
    public AS3Cache() {

    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Checks if the cache contains the specified pack.
     *
     * @param pack Pack to check
     * @return True if the pack is cached, false otherwise
     */
    public boolean isCached(ScriptPack pack) {
        return cache.contains(pack);
    }

    /**
     * Gets the cached text for the specified pack.
     *
     * @param pack Pack to get the text for
     * @return Cached text for the pack
     */
    public HighlightedText get(ScriptPack pack) {
        return cache.get(pack);
    }

    /**
     * Puts the specified text into the cache for the specified pack.
     *
     * @param pack Pack to put the text for
     * @param text Text to put
     */
    public void put(ScriptPack pack, HighlightedText text) {
        cache.put(pack, text);
    }

    /**
     * Removes the specified pack from the cache.
     *
     * @param pack Pack to remove
     */
    public void remove(ScriptPack pack) {
        if (pack != null) {
            cache.remove(pack);
        }
    }
}
