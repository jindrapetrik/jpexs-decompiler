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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.IOException;
import java.util.Set;

public class DefineShape3Tag extends ShapeTag {

    @SWFType(BasicType.UI16)
    public int shapeId;
    public RECT shapeBounds;
    public SHAPEWITHSTYLE shapes;
    public static final int ID = 32;

    @Override
    public int getShapeNum() {
        return 3;
    }

    @Override
    public SHAPEWITHSTYLE getShapes() {
        return shapes;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        shapes.getNeededCharacters(needed);
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = shapes.removeCharacter(characterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public RECT getRect() {
        return shapeBounds;
    }

    @Override
    public int getCharacterId() {
        return shapeId;
    }

    public DefineShape3Tag(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineShape3", pos, length);
        shapeId = sis.readUI16("shapeId");
        shapeBounds = sis.readRECT("shapeBounds");
        shapes = sis.readSHAPEWITHSTYLE(3, false, "shapes");
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }
}
