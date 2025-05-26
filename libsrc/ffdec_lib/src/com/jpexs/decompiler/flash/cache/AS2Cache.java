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

import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.helpers.Cache;

/**
 * Cache for AS2 decompiled code.
 *
 * @author JPEXS
 */
public class AS2Cache {

    private final Cache<ASMSource, HighlightedText> cache = Cache.getInstance(true, false, "as2", false);

    private final Cache<ASMSource, ActionList> pcodeCache = Cache.getInstance(true, true, "as2pcode", false);

    /**
     * Constructor.
     */
    public AS2Cache() {
        
    }
    
    /**
     * Clears the cache.
     */
    public void clear() {
        pcodeCache.clear();
        cache.clear();
    }

    /**
     * Checks if the source is cached.
     * @param src Source
     * @return True if the source is cached
     */
    public boolean isCached(ASMSource src) {
        return cache.contains(src);
    }

    /**
     * Checks if the PCode is cached.
     * @param src Source
     * @return True if the PCode is cached
     */
    public boolean isPCodeCached(ASMSource src) {
        return pcodeCache.contains(src);
    }

    /**
     * Gets the cached text.
     * @param src Source
     * @return Cached text
     */
    public HighlightedText get(ASMSource src) {
        return cache.get(src);
    }

    /**
     * Gets the cached PCode.
     * @param src Source
     * @return Cached PCode
     */
    public ActionList getPCode(ASMSource src) {
        return pcodeCache.get(src);
    }

    /**
     * Puts the text into the cache.
     * @param src Source
     * @param text Text
     */
    public void put(ASMSource src, HighlightedText text) {
        cache.put(src, text);
    }

    /**
     * Puts the PCode into the cache.
     * @param src Source
     * @param actionList PCode
     */
    public void put(ASMSource src, ActionList actionList) {
        pcodeCache.put(src, actionList);
    }

    /**
     * Removes the source from the cache.
     * @param src Source
     */
    public void remove(ASMSource src) {
        if (src != null) {
            cache.remove(src);
            pcodeCache.remove(src);
        }
    }
}
