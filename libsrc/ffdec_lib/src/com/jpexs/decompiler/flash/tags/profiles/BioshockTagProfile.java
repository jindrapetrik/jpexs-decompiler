/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.profiles;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.profiles.bioshock.BioshockImage;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bioshock 1 or 2 game tag profile.
 * @author JPEXS
 */
public class BioshockTagProfile implements TagProfile {
    
    public static final String ID = "bioshock";
    
    public static final String NAME = "Bioshock 1/2";
    
    
    private static final Map<Integer, Class<? extends Tag>> SUPPORTED_TAGS = new LinkedHashMap<>();
        
    static {
        SUPPORTED_TAGS.put(BioshockImage.ID, BioshockImage.class);
    }
    
    public BioshockTagProfile() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Tag decodeTag(SWFInputStream sis, int tagId, ByteArrayRange data) throws IOException {
        if (!supportsSwf(sis.getSwf())) {
            return null;
        }
        if (!supportsTagId(tagId)) {
            return null;
        }
        
        if (tagId != BioshockImage.ID) {
            return null;
        }
        return new BioshockImage(sis, data);
    }

    @Override
    public boolean supportsTagId(int tagId) {
        return SUPPORTED_TAGS.containsKey(tagId);
    }

    @Override
    public Map<Integer, Class<? extends Tag>> getSupportedTags() {        
        return SUPPORTED_TAGS;
    }

    @Override
    public Class<? extends Tag> getTagIdClass(int tagId) {
        return SUPPORTED_TAGS.get(tagId);
    }

    @Override
    public boolean supportsSwf(SWF swf) {
        return !swf.gfx;
    }
    
}
