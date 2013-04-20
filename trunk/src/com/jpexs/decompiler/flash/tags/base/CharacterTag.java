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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class CharacterTag extends Tag {

    public CharacterTag(int id, String name, byte[] data, long pos) {
        super(id, name, data, pos);
    }

    public abstract int getCharacterID();
    /**
     * List of ExportAssetsTag used for converting to String
     */
    public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
    private String className;

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String getName() {
        String nameAppend = "";
        for (ExportAssetsTag eat : exportAssetsTags) {
            int pos = eat.tags.indexOf(getCharacterID());
            if (pos > -1) {
                nameAppend = ": " + eat.names.get(pos);
            }
        }
        if (className != null) {
            nameAppend = ": " + className;
        }
        return super.getName() + " (" + getCharacterID() + nameAppend + ")";
    }

    @Override
    public String getExportName() {
        return super.getName() + "_" + getCharacterID();
    }
}
