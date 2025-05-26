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
package com.jpexs.decompiler.flash.tags.converters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;

/**
 * Converts shape number (DefineShape, DefineShape2, ...)
 * @author JPEXS
 */
public class ShapeTypeConverter {

    /**
     * Get minimum DefineShape number, which can be converted to including loosing information.     
     * @param shapeTag Shape tag
     * @return DefineShape number
     */
    public int getForcedMinShapeNum(ShapeTag shapeTag) {
        if (shapeTag.getShapeNum() > 1) {
            if (shapeTag.shapes.fillStyles.fillStyles.length > 255) {
                return 2;
            }
            if (shapeTag.shapes.lineStyles.lineStyles != null && shapeTag.shapes.lineStyles.lineStyles.length > 255) {
                return 2;
            }
            if (shapeTag.shapes.lineStyles.lineStyles2 != null && shapeTag.shapes.lineStyles.lineStyles2.length > 255) {
                return 2;
            }
            for (SHAPERECORD rec : shapeTag.shapes.shapeRecords) {
                if (rec instanceof StyleChangeRecord) {
                    StyleChangeRecord scr = (StyleChangeRecord) rec;
                    if (scr.stateNewStyles) {
                        return 2;
                    }
                }
            }
        }
        return 1;
    }

    /**
     * Get minimum DefineShape number, which can be shape converted to without loosing information.
     * @param shapeTag Shape tag
     * @return DefineShape number
     */
    public int getMinShapeNum(ShapeTag shapeTag) {
        int result = shapeTag.shapes.getMinShapeNum(shapeTag.getShapeNum());
        if (shapeTag.getShapeNum() == 4) {
            DefineShape4Tag shape4 = (DefineShape4Tag) shapeTag;
            if (shape4.usesFillWindingRule) {
                return 4;
            }
        }
        return result;
    }

    /**
     * Converts shape tag referenced by character id in selected SWF file.
     * @param swf SWF
     * @param characterId Character id
     * @param targetShapeNum Target shape num
     */
    public void convertCharacter(SWF swf, int characterId, int targetShapeNum) {
        CharacterTag ct = swf.getCharacter(characterId);
        if (!(ct instanceof ShapeTag)) {
            throw new IllegalArgumentException("Character " + characterId + " is not a shape");
        }
        ShapeTag sh = (ShapeTag) ct;
        if (targetShapeNum == sh.getShapeNum()) {
            return;
        }
        Timelined tim = sh.getTimelined();
        ShapeTag converted = convertTagType(sh, swf, targetShapeNum);
        converted.setCharacterId(characterId);
        swf.replaceTag(ct, converted);
        converted.setTimelined(tim);
        swf.updateCharacters();
        swf.clearShapeCache();
        swf.assignClassesToSymbols();
        swf.assignExportNamesToSymbols();
        tim.resetTimeline();
    }
    
    /**
     * Converts DefineShape tag type
     * @param sourceShapeTag Source tag
     * @param targetSWF Target swf
     * @param targetShapeNum Target DefineShape number
     * @return Converted DefineShapeX tag
     * @throws IllegalArgumentException When conversion is not possible - see getForcedMinShapeNum
     */
    public ShapeTag convertTagType(ShapeTag sourceShapeTag, SWF targetSWF, int targetShapeNum) {
        int sourceShapeNum = sourceShapeTag.getShapeNum();
        ShapeTag result;
        switch (targetShapeNum) {
            case 1:
                result = new DefineShapeTag(targetSWF);
                break;
            case 2:
                result = new DefineShape2Tag(targetSWF);
                break;
            case 3:
                result = new DefineShape3Tag(targetSWF);
                break;
            case 4:
                result = new DefineShape4Tag(targetSWF);
                break;
            default:
                throw new IllegalArgumentException("Target shape num must be 1-4. Provided: " + targetShapeNum);
        }
        result.shapeBounds = Helper.deepCopy(sourceShapeTag.shapeBounds);
        result.shapes = Helper.deepCopy(sourceShapeTag.shapes);

        if (sourceShapeNum > 1 && targetShapeNum == 1) {
            if (result.shapes.fillStyles.fillStyles.length > 255) {
                throw new IllegalArgumentException("DefineShape1 does not allow more than 255 fill styles");
            }
            if (result.shapes.lineStyles.lineStyles != null && result.shapes.lineStyles.lineStyles.length > 255) {
                throw new IllegalArgumentException("DefineShape1 does not allow more than 255 line styles");
            }
            if (result.shapes.lineStyles.lineStyles2 != null && result.shapes.lineStyles.lineStyles2.length > 255) {
                throw new IllegalArgumentException("DefineShape1 does not allow more than 255 line styles");
            }
            for (SHAPERECORD rec : result.shapes.shapeRecords) {
                if (rec instanceof StyleChangeRecord) {
                    StyleChangeRecord scr = (StyleChangeRecord) rec;
                    if (scr.stateNewStyles) {
                        throw new IllegalArgumentException("DefineShape1 does not allow multiple style lists");
                    }
                }
            }
        }
        result.shapes.lineStyles = result.shapes.lineStyles.toShapeNum(sourceShapeNum, targetShapeNum);
        result.shapes.fillStyles = result.shapes.fillStyles.toShapeNum(targetShapeNum);
        for (SHAPERECORD rec : result.shapes.shapeRecords) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateNewStyles) {
                    scr.fillStyles = scr.fillStyles.toShapeNum(targetShapeNum);
                    scr.lineStyles = scr.lineStyles.toShapeNum(sourceShapeNum, targetShapeNum);
                }
            }
        }
        if (targetShapeNum == 4) {
            DefineShape4Tag result4 = (DefineShape4Tag) result;
            if (sourceShapeNum == 4) {
                DefineShape4Tag source4 = (DefineShape4Tag) sourceShapeTag;
                result4.edgeBounds = Helper.deepCopy(source4.edgeBounds);
                result4.usesFillWindingRule = source4.usesFillWindingRule;
                result4.usesNonScalingStrokes = source4.usesNonScalingStrokes;
                result4.usesScalingStrokes = source4.usesScalingStrokes;
            } else {
                result4.updateEdgeBounds();
                result4.usesScalingStrokes = true;
            }
        }
        return result;
    }
}
