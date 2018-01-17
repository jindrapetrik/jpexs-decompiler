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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.SymbolClassTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.annotations.Table;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes portions of a SWF file available for import by other SWF files
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ExportAssetsTag extends SymbolClassTypeTag {

    public static final int ID = 56;

    public static final String NAME = "ExportAssets";

    /**
     * HashMap with assets
     */
    @SWFType(value = BasicType.UI16)
    @SWFArray(value = "tag", countField = "count")
    @Table(value = "assets", itemName = "asset")
    public List<Integer> tags;

    @SWFArray(value = "name", countField = "count")
    @Table(value = "assets", itemName = "asset")
    public List<String> names;

    /**
     * Constructor
     *
     * @param swf
     */
    public ExportAssetsTag(SWF swf) {
        super(swf, ID, NAME, null);
        tags = new ArrayList<>();
        names = new ArrayList<>();
    }

    @Override
    public Map<Integer, String> getTagToNameMap() {
        Map<Integer, String> exportNames = new HashMap<>();
        for (int i = 0; i < tags.size(); i++) {
            int tagId = tags.get(i);
            String name = names.get(i);
            if ((!exportNames.containsKey(tagId)) && (!exportNames.containsValue(name))) {
                exportNames.put(tagId, name);
            }
        }
        return exportNames;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public ExportAssetsTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        int count = sis.readUI16("count");
        tags = new ArrayList<>();
        names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int characterId = sis.readUI16("characterId");
            String name = sis.readString("name");
            tags.add(characterId);
            names.add(name);
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            sos.writeUI16(tags.get(i));
            sos.writeString(names.get(i));
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i) == oldCharacterId) {
                tags.set(i, newCharacterId);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i) == characterId) {
                tags.remove(i);
                names.remove(i);
                i--;
                modified = true;
            }
        }
        return modified;
    }
}
