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
import com.jpexs.decompiler.flash.SWFLimitedInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes portions of a SWF file available for import by other SWF files
 *
 * @author JPEXS
 */
public class ExportAssetsTag extends Tag {

    /**
     * HashMap with assets
     */
    @SWFType(value = BasicType.UI16)
    @SWFArray(value = "tag", countField = "count")
    public List<Integer> tags;

    @SWFArray(value = "name", countField = "count")
    public List<String> names;
    public static final int ID = 56;

    public ExportAssetsTag(SWF swf) {
        super(swf, ID, "ExportAssets", 0, 0);
        tags = new ArrayList<>();
        names = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public ExportAssetsTag(SWFLimitedInputStream sis, long pos, int length) throws IOException {
        super(sis.swf, ID, "ExportAssets", pos, length);
        int count = sis.readUI16();
        tags = new ArrayList<>();
        names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int characterId = sis.readUI16();
            tags.add(characterId);
            String name = sis.readString();
            names.add(name);
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
            sos.writeUI16(tags.size());
            for (int i = 0; i < tags.size(); i++) {
                sos.writeUI16(tags.get(i));
                sos.writeString(names.get(i));
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
