/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.cache;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.helpers.Cache;

/**
 *
 * @author JPEXS
 */
public class AS3Cache {

    private final Cache<ScriptPack, HighlightedText> cache = Cache.getInstance(true, false, "as3");

    public void clear() {
        cache.clear();
    }

    public boolean isCached(ScriptPack pack) {
        return cache.contains(pack);
    }

    public HighlightedText get(ScriptPack pack) {
        return cache.get(pack);
    }

    public void put(ScriptPack pack, HighlightedText text) {
        cache.put(pack, text);
    }

    public void remove(ScriptPack pack) {
        if (pack != null) {
            cache.remove(pack);
        }
    }
}
