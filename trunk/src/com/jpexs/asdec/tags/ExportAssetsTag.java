/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.tags;

import com.jpexs.asdec.SWFInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Makes portions of a SWF file available for import by other SWF files
 *
 * @author JPEXS
 */
public class ExportAssetsTag extends Tag {
    /**
     * HashMap with assets
     */
    public HashMap<Integer, String> assets;

    /**
     * Constructor
     *
     * @param data    Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public ExportAssetsTag(byte[] data, int version) throws IOException {
        super(56, data);
        assets = new HashMap<Integer, String>();
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        int count = sis.readUI16();
        for (int i = 0; i < count; i++) {
            int characterId = sis.readUI16();
            String name = sis.readString();
            assets.put(characterId, name);
        }
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "ExportAssets";
    }
}
