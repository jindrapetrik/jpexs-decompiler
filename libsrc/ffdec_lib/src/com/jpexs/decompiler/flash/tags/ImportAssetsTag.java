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
import com.jpexs.decompiler.flash.tags.base.ImportTag;
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
 * Imports characters from another file
 *
 * @author JPEXS
 */
@SWFVersion(from = 5, to = 7)
public class ImportAssetsTag extends Tag implements ImportTag {

    public static final int ID = 57;

    public static final String NAME = "ImportAssets";

    public String url;

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
    public ImportAssetsTag(SWF swf) {
        super(swf, ID, NAME, null);
        url = "";
        tags = new ArrayList<>();
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
        setModified(true);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public ImportAssetsTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        tags = new ArrayList<>();
        names = new ArrayList<>();
        url = sis.readString("url");
        int count = sis.readUI16("count");
        for (int i = 0; i < count; i++) {
            int charId = sis.readUI16("charId");
            String tagName = sis.readString("tagName");
            tags.add(charId);
            names.add(tagName);
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
        sos.writeString(url);
        sos.writeUI16(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            sos.writeUI16(tags.get(i));
            sos.writeString(names.get(i));
        }
    }

    @Override
    public Map<Integer, String> getAssets() {
        Map<Integer, String> assets = new HashMap<>();
        for (int i = 0; i < tags.size(); i++) {
            assets.put(tags.get(i), names.get(i));
        }
        return assets;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
