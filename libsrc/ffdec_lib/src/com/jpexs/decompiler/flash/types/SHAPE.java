/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
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

    @SWFArray(value = "record")
    public List<SHAPERECORD> shapeRecords;

    private Shape cachedOutline;
    private Shape fastCachedOutline;

    public SHAPE() {
        shapeRecords = new ArrayList<>();
        shapeRecords.add(new EndShapeRecord());
    }    
    
    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        for (SHAPERECORD r : shapeRecords) {
            r.getNeededCharacters(needed, swf);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (SHAPERECORD r : shapeRecords) {
            modified |= r.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (SHAPERECORD r : shapeRecords) {
            modified |= r.removeCharacter(characterId);
        }
        return modified;
    }

    public RECT getBounds(int shapeNum) {
        LINESTYLEARRAY lsa = new LINESTYLEARRAY();
        lsa.lineStyles = new LINESTYLE[0];
        lsa.lineStyles2 = new LINESTYLE2[0];
        return SHAPERECORD.getBounds(shapeRecords, lsa, shapeNum, false);
    }

    public RECT getEdgeBounds() {
        return SHAPERECORD.getBounds(shapeRecords, null, 1, true);
    }

    public void clearCachedOutline() {
        cachedOutline = null;
        fastCachedOutline = null;
    }

    /**
     *
     * @param fast When the shape is large, can approximate to rectangles
     * instead of being slow.
     * @param shapeNum Version of DefineShape, 2 for DefineShape2 etc.
     * @param swf
     * @param stroked
     * @return
     */
    public Shape getOutline(boolean fast, int shapeNum, SWF swf, boolean stroked) {
        if (cachedOutline != null) {
            return cachedOutline;
        }
        if (fast && fastCachedOutline != null) {
            return fastCachedOutline;
        }

        List<GeneralPath> strokes = new ArrayList<>();
        List<GeneralPath> paths = PathExporter.export(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, this, strokes);

        boolean large = shapeRecords.size() > 500;

        if (!large) {
            fast = false;
        }

        Area area = new Area();
        for (GeneralPath path : paths) {
            area.add(new Area(fast ? path.getBounds2D() : path));
        }
        if (stroked) {
            for (GeneralPath path : strokes) {
                area.add(new Area(fast ? path.getBounds2D() : path));
            }
        }

        if (fast) {
            fastCachedOutline = area;
        } else {
            fastCachedOutline = null;
            cachedOutline = area;
        }
        return area;
    }

    public SHAPE resize(double multiplier) {
        return resize(multiplier, multiplier);
    }

    public SHAPE resize(double multiplierX, double multiplierY) {
        SHAPE ret = new SHAPE();
        ret.numFillBits = numFillBits;
        ret.numLineBits = numLineBits;
        List<SHAPERECORD> recs = new ArrayList<>();
        for (SHAPERECORD r : shapeRecords) {
            SHAPERECORD c = r.resize(multiplierX, multiplierY);
            recs.add(c);
        }

        ret.shapeRecords = recs;
        return ret;
    }

    public static SHAPE createEmpty(int shapeNum) {
        SHAPE ret = new SHAPE();
        ret.shapeRecords = new ArrayList<>();
        ret.shapeRecords.add(new EndShapeRecord());
        return ret;
    }
}
