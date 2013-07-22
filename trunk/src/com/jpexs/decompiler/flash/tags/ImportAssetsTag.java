/*
 *  Copyright (C) 2010-2013 JPEXS
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Imports characters from another file
 *
 * @author JPEXS
 */
public class ImportAssetsTag extends Tag implements ImportTag {

    public String url;
    /**
     * HashMap with assets
     */
    public HashMap<Integer, String> assets;
    public static final int ID = 57;

    /**
     * Constructor
     *
     * @param data Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public ImportAssetsTag(SWF swf, byte data[], int version, long pos) throws IOException {
        super(swf, ID, "ImportAssets", data, pos);
        assets = new HashMap<>();
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        url = sis.readString();
        int count = sis.readUI16();
        for (int i = 0; i < count; i++) {
            int characterId = sis.readUI16();
            String name = sis.readString();
            assets.put(characterId, name);
        }
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeString(url);
            sos.writeUI16(assets.size());
            for (int characterId : assets.keySet()) {
                sos.writeUI16(characterId);
                sos.writeString(assets.get(characterId));
            }
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    @Override
    public HashMap<Integer, String> getAssets() {
        return assets;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
