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
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shape with style.
 *
 * @author JPEXS
 */
public class SHAPEWITHSTYLE extends SHAPE implements NeedsCharacters, Serializable {

    /**
     * Fill styles
     */
    public FILLSTYLEARRAY fillStyles;

    /**
     * Line styles
     */
    public LINESTYLEARRAY lineStyles;

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        fillStyles.getNeededCharacters(needed, swf);
        lineStyles.getNeededCharacters(needed, swf);
        for (SHAPERECORD r : shapeRecords) {
            r.getNeededCharacters(needed, swf);
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        modified |= fillStyles.replaceCharacter(oldCharacterId, newCharacterId);
        modified |= lineStyles.replaceCharacter(oldCharacterId, newCharacterId);
        for (SHAPERECORD r : shapeRecords) {
            modified |= r.replaceCharacter(oldCharacterId, newCharacterId);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        modified |= fillStyles.removeCharacter(characterId);
        modified |= lineStyles.removeCharacter(characterId);
        for (SHAPERECORD r : shapeRecords) {
            modified |= r.removeCharacter(characterId);
        }
        return modified;
    }

    @Override
    public SHAPEWITHSTYLE resize(double multiplierX, double multiplierY) {
        SHAPEWITHSTYLE ret = new SHAPEWITHSTYLE();
        ret.numFillBits = numFillBits;
        ret.numLineBits = numLineBits;
        List<SHAPERECORD> recs = new ArrayList<>();
        for (SHAPERECORD r : shapeRecords) {
            SHAPERECORD c = r.resize(multiplierX, multiplierY);
            recs.add(c);
        }

        ret.shapeRecords = recs;
        ret.fillStyles = fillStyles; // todo: clone?
        ret.lineStyles = lineStyles; // todo: clone?
        return ret;
    }

    /**
     * Creates empty shape with style.
     * @param shapeNum Shape number - 1 for DefineShape, 2 for DefineShape2, etc.
     * @return Empty shape with style
     */
    public static SHAPEWITHSTYLE createEmpty(int shapeNum) {
        SHAPEWITHSTYLE ret = new SHAPEWITHSTYLE();
        ret.shapeRecords = new ArrayList<>();
        ret.shapeRecords.add(new EndShapeRecord());
        ret.fillStyles = new FILLSTYLEARRAY();
        ret.fillStyles.fillStyles = new FILLSTYLE[0];
        ret.lineStyles = new LINESTYLEARRAY();
        if (shapeNum <= 3) {
            ret.lineStyles.lineStyles = new LINESTYLE[0];
        } else {
            ret.lineStyles.lineStyles2 = new LINESTYLE2[0];
        }

        return ret;
    }

    @Override
    public RECT getBounds(int shapeNum) {
        return SHAPERECORD.getBounds(shapeRecords, lineStyles, shapeNum, false);
    }

    /**
     * Updates morph shape tag.
     * @param morphShapeTag Morph shape tag
     * @param fill Fill
     */
    public void updateMorphShapeTag(MorphShapeTag morphShapeTag, boolean fill) {
        morphShapeTag.startEdges.shapeRecords.clear();
        morphShapeTag.endEdges.shapeRecords.clear();

        FILLSTYLEARRAY mergedFillStyles = new FILLSTYLEARRAY();
        LINESTYLEARRAY mergedLineStyles = new LINESTYLEARRAY();

        List<FILLSTYLE> mergedFillStyleList = new ArrayList<>();
        List<LINESTYLE> mergedLineStyleList = new ArrayList<>();
        List<LINESTYLE2> mergedLineStyle2List = new ArrayList<>();

        int lastFillCount = fillStyles.fillStyles.length;

        for (int i = 0; i < fillStyles.fillStyles.length; i++) {
            mergedFillStyleList.add(fillStyles.fillStyles[i]);
        }

        int lastLineCount = 0;

        if (lineStyles.lineStyles != null) {
            lastLineCount = lineStyles.lineStyles.length;
            for (int i = 0; i < lineStyles.lineStyles.length; i++) {
                mergedLineStyleList.add(lineStyles.lineStyles[i]);
            }
        }
        if (lineStyles.lineStyles2 != null) {
            lastLineCount = lineStyles.lineStyles2.length;
            for (int i = 0; i < lineStyles.lineStyles2.length; i++) {
                mergedLineStyle2List.add(lineStyles.lineStyles2[i]);
            }
        }

        int fillOffset = 0;
        int lineOffset = 0;
        List<SHAPERECORD> newShapeRecords = new ArrayList<>();
        for (int r = 0; r < shapeRecords.size(); r++) {
            SHAPERECORD rec = shapeRecords.get(r);
            rec = Helper.deepCopy(rec);
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateNewStyles) {
                    for (int i = 0; i < scr.fillStyles.fillStyles.length; i++) {
                        mergedFillStyleList.add(scr.fillStyles.fillStyles[i]);
                    }
                    fillOffset += lastFillCount;
                    lastFillCount = scr.fillStyles.fillStyles.length;
                    if (scr.lineStyles.lineStyles != null) {
                        for (int i = 0; i < scr.lineStyles.lineStyles.length; i++) {
                            mergedLineStyleList.add(scr.lineStyles.lineStyles[i]);
                        }
                        lineOffset += lastLineCount;
                        lastLineCount = scr.lineStyles.lineStyles.length;
                    }
                    if (scr.lineStyles.lineStyles2 != null) {
                        for (int i = 0; i < scr.lineStyles.lineStyles2.length; i++) {
                            mergedLineStyle2List.add(scr.lineStyles.lineStyles2[i]);
                        }
                        lineOffset += lastLineCount;
                        lastLineCount = scr.lineStyles.lineStyles2.length;
                    }
                    scr.stateNewStyles = false;
                }
                if (scr.stateFillStyle0) {
                    scr.fillStyle0 += fillOffset;
                }
                if (scr.stateFillStyle1) {
                    scr.fillStyle1 += fillOffset;
                }
                if (scr.stateLineStyle) {
                    scr.lineStyle += lineOffset;
                }
            }
            newShapeRecords.add(rec);
        }

        mergedFillStyles.fillStyles = new FILLSTYLE[mergedFillStyleList.size()];
        for (int i = 0; i < mergedFillStyleList.size(); i++) {
            mergedFillStyles.fillStyles[i] = mergedFillStyleList.get(i);
        }
        mergedLineStyles.lineStyles = new LINESTYLE[mergedLineStyleList.size()];
        for (int i = 0; i < mergedLineStyleList.size(); i++) {
            mergedLineStyles.lineStyles[i] = mergedLineStyleList.get(i);
        }
        mergedLineStyles.lineStyles2 = new LINESTYLE2[mergedLineStyle2List.size()];
        for (int i = 0; i < mergedLineStyle2List.size(); i++) {
            mergedLineStyles.lineStyles2[i] = mergedLineStyle2List.get(i);
        }

        morphShapeTag.morphFillStyles = mergedFillStyles.toMorphFillStyleArray();
        morphShapeTag.morphLineStyles = mergedLineStyles.toMorphLineStyleArray();
        SHAPE startShapes = new SHAPE();
        startShapes.numFillBits = SWFOutputStream.getNeededBitsU(mergedFillStyleList.size());
        startShapes.numLineBits = SWFOutputStream.getNeededBitsU(mergedLineStyleList.size() + mergedLineStyle2List.size());
        startShapes.shapeRecords = newShapeRecords;
        morphShapeTag.startEdges = startShapes;

        SHAPE endShapes = new SHAPE();
        endShapes.numFillBits = 0;
        endShapes.numLineBits = 0;
        List<SHAPERECORD> endRecords = new ArrayList<>();
        for (int i = 0; i < newShapeRecords.size(); i++) {
            SHAPERECORD rec = newShapeRecords.get(i);
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateMoveTo) {
                    StyleChangeRecord nscr = new StyleChangeRecord();
                    nscr.stateMoveTo = true;
                    nscr.moveDeltaX = scr.moveDeltaX;
                    nscr.moveDeltaY = scr.moveDeltaY;
                    nscr.calculateBits();
                    endRecords.add(nscr);
                }
            } else {
                endRecords.add(Helper.deepCopy(rec));
            }
        }
        endShapes.shapeRecords = endRecords;
        morphShapeTag.endEdges = endShapes;

        if (!fill) {
            morphShapeTag.updateBounds();
        }
    }

    public int getMinShapeNum(int sourceShapeNum) {
        int result = 1;
        int sn;
        
        if (fillStyles.fillStyles.length > 255) {
            result = 2;
        }
        if (sourceShapeNum >= 4 && lineStyles.lineStyles2.length > 255) {
            result = 2;
        }
        if (sourceShapeNum < 4 && lineStyles.lineStyles.length > 255) {
            result = 2;
        }
        
        sn = fillStyles.getMinShapeNum();
        if (sn > result) {
            result = sn;
        }
        sn = lineStyles.getMinShapeNum(sourceShapeNum);
        if (sn > result) {
            result = sn;
        }
        for (SHAPERECORD sr : shapeRecords) {
            if (sr instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) sr;
                if (scr.stateNewStyles) {
                    if (2 > result) {
                        result = 2;
                    }
                    sn = scr.fillStyles.getMinShapeNum();
                    if (sn > result) {
                        result = sn;
                    }
                    sn = scr.lineStyles.getMinShapeNum(sourceShapeNum);
                    if (sn > result) {
                        result = sn;
                    }
                }
            }
        }
        return result;
    }
}
