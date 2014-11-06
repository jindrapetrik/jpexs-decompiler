/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class SHAPE implements NeedsCharacters, Serializable {

    @SWFType(value = BasicType.UB, count = 4)
    public int numFillBits;
    @SWFType(value = BasicType.UB, count = 4)
    public int numLineBits;
    public List<SHAPERECORD> shapeRecords;

    private Shape cachedOutline;
    
    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        for (SHAPERECORD r : shapeRecords) {
            r.getNeededCharacters(needed);
        }
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (SHAPERECORD r : shapeRecords) {
            modified |= r.removeCharacter(characterId);
        }
        return modified;
    }

    public RECT getBounds() {
        return SHAPERECORD.getBounds(shapeRecords);
    }

    public Shape getOutline() {
        if (cachedOutline != null) {
            return cachedOutline;
        }
        
        List<GeneralPath> paths = PathExporter.export(this);
        Area area = new Area();
        for (GeneralPath path : paths) {
            area.add(new Area(path));
        }

        cachedOutline = area;
        return area;
    }

    public static SHAPE createEmpty(int shapeNum) {
        SHAPE ret = new SHAPE();
        ret.shapeRecords = new ArrayList<>();
        ret.shapeRecords.add(new EndShapeRecord());
        return ret;
    }
}
