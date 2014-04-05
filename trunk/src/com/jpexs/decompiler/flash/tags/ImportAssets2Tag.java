/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports characters from another file, v2
 *
 * @author JPEXS
 */
public class ImportAssets2Tag extends Tag implements ImportTag {

    public String url;
    @Reserved
    @SWFType(BasicType.UI8)
    public int reserved1 = 1;
    @Reserved
    @SWFType(BasicType.UI8)
    public int reserved2 = 0;
    /**
     * HashMap with assets
     */
    @SWFType(value = BasicType.UI16)
    @SWFArray(value = "tag", countField = "count")
    public List<Integer> tags;
    @SWFArray(value = "name", countField = "count")
    public List<String> names;
    public static final int ID = 71;

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public ImportAssets2Tag(SWF swf, byte[] headerData, byte[] data, long pos) throws IOException {
        super(swf, ID, "ImportAssets2", headerData, data, pos);
        tags = new ArrayList<>();
        names = new ArrayList<>();
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        url = sis.readString();
        reserved1 = sis.readUI8();//reserved, must be 1
        reserved2 = sis.readUI8();//reserved, must be 0
        int count = sis.readUI16();
        for (int i = 0; i < count; i++) {
            int charId = sis.readUI16();
            String tagName = sis.readString();
            tags.add(charId);
            names.add(tagName);
        }
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeString(url);
            sos.writeUI8(reserved1);
            sos.writeUI8(reserved2);
            sos.writeUI16(tags.size());
            for (int i = 0; i < tags.size(); i++) {
                sos.writeUI16(tags.get(i));
                sos.writeString(names.get(i));
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
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
