/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * Shape.
 *
 * @author JPEXS
 */
public class SHAPE implements NeedsCharacters, Serializable {

    /**
     * Number of fill bits
     */
    @SWFType(value = BasicType.UB, count = 4)
    public int numFillBits;

    /**
     * Number of line bits
     */
    @SWFType(value = BasicType.UB, count = 4)
    public int numLineBits;

    /**
     * Shape records
     */
    @SWFArray(value = "record")
    public List<SHAPERECORD> shapeRecords;

    private transient Shape cachedOutline;
    private transient Shape fastCachedOutline;

    /**
     * Constructor.
     */
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

    /**
     * Get bounds of shape.
     * @param shapeNum Version of DefineShape, 2 for DefineShape2 etc.
     * @return Bounds of shape
     */
    public RECT getBounds(int shapeNum) {
        LINESTYLEARRAY lsa = new LINESTYLEARRAY();
        lsa.lineStyles = new LINESTYLE[0];
        lsa.lineStyles2 = new LINESTYLE2[0];
        return SHAPERECORD.getBounds(shapeRecords, lsa, shapeNum, false);
    }

    /**
     * Get edge bounds of shape.
     * @return Edge bounds of shape
     */
    public RECT getEdgeBounds() {
        return SHAPERECORD.getBounds(shapeRecords, null, 1, true);
    }

    /**
     * Clears cached outline.
     */
    public void clearCachedOutline() {
        cachedOutline = null;
        fastCachedOutline = null;
    }

    /**
     * Get outline of shape.
     * @param fast When the shape is large, can approximate to rectangles
     * instead of being slow.
     * @param shapeNum Version of DefineShape, 2 for DefineShape2 etc.
     * @param swf SWF
     * @param stroked If stroked
     * @return Outline
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

    /**
     * Resizes shape.
     * @param multiplier Multiplier
     * @return Resized shape
     */
    public SHAPE resize(double multiplier) {
        return resize(multiplier, multiplier);
    }

    /**
     * Resizes shape.
     * @param multiplierX Multiplier X
     * @param multiplierY Multiplier Y
     * @return Resized shape
     */
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

    /**
     * Creates empty shape.
     * @param shapeNum Version of DefineShape, 2 for DefineShape2 etc.
     * @return Empty shape
     */
    public static SHAPE createEmpty(int shapeNum) {
        SHAPE ret = new SHAPE();
        ret.shapeRecords = new ArrayList<>();
        ret.shapeRecords.add(new EndShapeRecord());
        return ret;
    }
}
