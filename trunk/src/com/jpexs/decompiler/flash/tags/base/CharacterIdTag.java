/*
 *  Copyright (C) 2010-2014 PEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class CharacterIdTag extends Tag {

    public CharacterIdTag(SWF swf, int id, String name, byte[] data, long pos) {
        super(swf, id, name, data, pos);
    }

    public abstract int getCharacterId();
    /**
     * List of ExportAssetsTag used for converting to String
     */
    @Internal
    public List<ExportAssetsTag> exportAssetsTags = new ArrayList<>();
    protected String className;
    protected String exportName;

    public void setExportName(String exportName) {
        this.exportName = exportName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String getName() {
        String nameAppend = "";
        if (exportName != null) {
            nameAppend = ": " + exportName;
        }
        if (className != null) {
            nameAppend = ": " + className;
        }
        if (getCharacterId() != -1) {
            return super.getName() + " (" + getCharacterId() + nameAppend + ")";
        }
        if (!nameAppend.equals("")) {
            return super.getName() + " (" + nameAppend + ")";
        }
        return super.getName();
    }

    @Override
    public String getExportFileName() {
        return super.getName() + "_" + getCharacterId() + (((exportName != null) && (!exportName.isEmpty())) ? "_" + exportName : "");
    }

    public String getCharacterExportFileName() {
        return getCharacterId() + (((exportName != null) && (!exportName.isEmpty())) ? "_" + exportName : "");
    }

    public String getExportName() {
        return exportName;
    }
}
