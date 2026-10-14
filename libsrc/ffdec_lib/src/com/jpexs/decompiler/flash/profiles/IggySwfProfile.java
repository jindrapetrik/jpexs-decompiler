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
package com.jpexs.decompiler.flash.profiles;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.profiles.iggy.IggyExternalImage;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/** SWF representation of resources referenced by an Iggy movie. */
public final class IggySwfProfile extends AbstractSwfProfile {
    public static final String ID = "iggy";
    public static final String NAME = "Iggy";
    private static final Map<Integer, Class<? extends Tag>> TAGS = Collections.singletonMap(IggyExternalImage.ID, IggyExternalImage.class);

    public IggySwfProfile() {
        super(ID, NAME);
    }

    @Override
    public Tag decodeTag(SWFInputStream sis, int tagId, ByteArrayRange data) throws IOException {
        return supportsSwf(sis.getSwf()) && tagId == IggyExternalImage.ID ? new IggyExternalImage(sis, data) : null;
    }

    @Override
    public boolean supportsTagId(int tagId) {
        return TAGS.containsKey(tagId);
    }

    @Override
    public Map<Integer, Class<? extends Tag>> getSupportedTags() {
        return TAGS;
    }

    @Override
    public Class<? extends Tag> getTagIdClass(int tagId) {
        return TAGS.get(tagId);
    }

    @Override
    public boolean supportsSwf(SWF swf) {
        return !swf.gfx;
    }
}
