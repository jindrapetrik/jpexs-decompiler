/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.morphshape.CanvasMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.morphshape.SVGMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class MorphShapeTag extends DrawableTag {

    public static final int MAX_RATIO = 65535;

    @SWFType(BasicType.UI16)
    public int characterId;

    public RECT startBounds;

    public RECT endBounds;

    public MORPHFILLSTYLEARRAY morphFillStyles;

    public MORPHLINESTYLEARRAY morphLineStyles;

    public SHAPE startEdges;

    public SHAPE endEdges;

    public MorphShapeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract int getShapeNum();

    @Override
    public RECT getRect() {
        return getRect(new HashSet<>());
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        morphFillStyles.getNeededCharacters(needed);
        startEdges.getNeededCharacters(needed);
        endEdges.getNeededCharacters(needed);
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        modified |= morphFillStyles.replaceCharacter(oldCharacterId, newCharacterId);
        modified |= startEdges.replaceCharacter(oldCharacterId, newCharacterId);
        modified |= endEdges.replaceCharacter(oldCharacterId, newCharacterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        modified |= morphFillStyles.removeCharacter(characterId);
        modified |= startEdges.removeCharacter(characterId);
        modified |= endEdges.removeCharacter(characterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        RECT rect = new RECT();
        rect.Xmin = Math.min(startBounds.Xmin, endBounds.Xmin);
        rect.Ymin = Math.min(startBounds.Ymin, endBounds.Ymin);
        rect.Xmax = Math.max(startBounds.Xmax, endBounds.Xmax);
        rect.Ymax = Math.max(startBounds.Ymax, endBounds.Ymax);
        return rect;
    }

    public RECT getStartBounds() {
        return startBounds;
    }

    public RECT getEndBounds() {
        return endBounds;
    }

    public MORPHFILLSTYLEARRAY getFillStyles() {
        return morphFillStyles;
    }

    public MORPHLINESTYLEARRAY getLineStyles() {
        return morphLineStyles;
    }

    public SHAPE getStartEdges() {
        return startEdges;
    }

    public SHAPE getEndEdges() {
        return endEdges;
    }

    public SHAPEWITHSTYLE getShapeAtRatio(int ratio) {
        List<SHAPERECORD> finalRecords = new ArrayList<>();
        FILLSTYLEARRAY fillStyles = morphFillStyles.getFillStylesAt(ratio);
        LINESTYLEARRAY lineStyles = morphLineStyles.getLineStylesAt(getShapeNum(), ratio);

        int startPosX = 0, startPosY = 0;
        int endPosX = 0, endPosY = 0;
        int posX = 0, posY = 0;

        for (int startIndex = 0, endIndex = 0;
                startIndex < startEdges.shapeRecords.size()
                && endIndex < endEdges.shapeRecords.size(); startIndex++, endIndex++) {

            SHAPERECORD edge1 = startEdges.shapeRecords.get(startIndex);
            SHAPERECORD edge2 = endEdges.shapeRecords.get(endIndex);
            if (edge1 instanceof StyleChangeRecord || edge2 instanceof StyleChangeRecord) {
                StyleChangeRecord scr1;
                if (edge1 instanceof StyleChangeRecord) {
                    scr1 = (StyleChangeRecord) edge1;
                    if (scr1.stateMoveTo) {
                        startPosX = scr1.moveDeltaX;
                        startPosY = scr1.moveDeltaY;
                    }
                } else {
                    scr1 = new StyleChangeRecord();
                    startIndex--;
                }
                StyleChangeRecord scr2;
                if (edge2 instanceof StyleChangeRecord) {
                    scr2 = (StyleChangeRecord) edge2;
                    if (scr2.stateMoveTo) {
                        endPosX = scr2.moveDeltaX;
                        endPosY = scr2.moveDeltaY;
                    }
                } else {
                    scr2 = new StyleChangeRecord();
                    endIndex--;
                }
                StyleChangeRecord scr = scr1.clone();
                if (scr1.stateMoveTo || scr2.stateMoveTo) {
                    scr.moveDeltaX = startPosX + (endPosX - startPosX) * ratio / MAX_RATIO;
                    scr.moveDeltaY = startPosY + (endPosY - startPosY) * ratio / MAX_RATIO;
                    scr.stateMoveTo = scr.moveDeltaX != posX || scr.moveDeltaY != posY;
                }
                finalRecords.add(scr);
                continue;
            }

            if (edge1 instanceof EndShapeRecord) {
                finalRecords.add(edge1);
                break;
            }
            if (edge2 instanceof EndShapeRecord) {
                finalRecords.add(edge2);
                break;
            }

            if (edge1 instanceof CurvedEdgeRecord || edge2 instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer1 = null;
                if (edge1 instanceof CurvedEdgeRecord) {
                    cer1 = (CurvedEdgeRecord) edge1;
                } else if (edge1 instanceof StraightEdgeRecord) {
                    cer1 = SHAPERECORD.straightToCurve((StraightEdgeRecord) edge1);
                }
                CurvedEdgeRecord cer2 = null;
                if (edge2 instanceof CurvedEdgeRecord) {
                    cer2 = (CurvedEdgeRecord) edge2;
                } else if (edge2 instanceof StraightEdgeRecord) {
                    cer2 = SHAPERECORD.straightToCurve((StraightEdgeRecord) edge2);
                }
                if ((cer2 == null) || (cer1 == null)) {
                    continue;
                }
                CurvedEdgeRecord cer = new CurvedEdgeRecord();
                cer.controlDeltaX = cer1.controlDeltaX + (cer2.controlDeltaX - cer1.controlDeltaX) * ratio / MAX_RATIO;
                cer.controlDeltaY = cer1.controlDeltaY + (cer2.controlDeltaY - cer1.controlDeltaY) * ratio / MAX_RATIO;
                cer.anchorDeltaX = cer1.anchorDeltaX + (cer2.anchorDeltaX - cer1.anchorDeltaX) * ratio / MAX_RATIO;
                cer.anchorDeltaY = cer1.anchorDeltaY + (cer2.anchorDeltaY - cer1.anchorDeltaY) * ratio / MAX_RATIO;
                startPosX += cer1.controlDeltaX + cer1.anchorDeltaX;
                startPosY += cer1.controlDeltaY + cer1.anchorDeltaY;
                endPosX += cer2.controlDeltaX + cer2.anchorDeltaX;
                endPosY += cer2.controlDeltaY + cer2.anchorDeltaY;
                posX += cer.controlDeltaX + cer.anchorDeltaX;
                posY += cer.controlDeltaY + cer.anchorDeltaY;
                finalRecords.add(cer);
            } else {
                StraightEdgeRecord ser1 = null;
                if (edge1 instanceof StraightEdgeRecord) {
                    ser1 = (StraightEdgeRecord) edge1;
                }
                StraightEdgeRecord ser2 = null;
                if (edge2 instanceof StraightEdgeRecord) {
                    ser2 = (StraightEdgeRecord) edge2;
                }
                if ((ser2 == null) || (ser1 == null)) {
                    continue;
                }
                StraightEdgeRecord ser = new StraightEdgeRecord();
                ser.generalLineFlag = true;
                ser.vertLineFlag = false;
                ser.deltaX = ser1.deltaX + (ser2.deltaX - ser1.deltaX) * ratio / MAX_RATIO;
                ser.deltaY = ser1.deltaY + (ser2.deltaY - ser1.deltaY) * ratio / MAX_RATIO;
                startPosX += ser1.deltaX;
                startPosY += ser1.deltaY;
                endPosX += ser2.deltaX;
                endPosY += ser2.deltaY;
                posX += ser.deltaX;
                posY += ser.deltaX;
                finalRecords.add(ser);
            }
        }
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = fillStyles;
        shape.lineStyles = lineStyles;
        shape.shapeRecords = finalRecords;
        return shape;
    }

    @Override
    public int getUsedParameters() {
        return PARAMETER_RATIO;
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, ColorTransform colorTransform) {
        SHAPEWITHSTYLE shape = getShapeAtRatio(ratio);
        // morphShape using shapeNum=3, morphShape2 using shapeNum=4
        // todo: Currently the generated image is not cached, because the cache
        // key contains the hashCode of the finalRecord object, and it is always
        // recreated
        BitmapExporter.export(swf, shape, null, image, transformation, strokeTransformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) {
        if (ratio == -2) {
            SHAPEWITHSTYLE beginShapes = getShapeAtRatio(0);
            SHAPEWITHSTYLE endShapes = getShapeAtRatio(65535);
            SVGMorphShapeExporter shapeExporter = new SVGMorphShapeExporter(swf, beginShapes, endShapes, getCharacterId(), exporter, null, colorTransform, 1);
            shapeExporter.export();
        } else {
            SHAPEWITHSTYLE shapes = getShapeAtRatio(ratio);
            SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, shapes, getCharacterId(), exporter, null, colorTransform, 1);
            shapeExporter.export();
        }
    }

    @Override
    public int getNumFrames() {
        return 65536;
    }

    @Override
    public boolean isSingleFrame() {
        // Morpshape is a single frame specified with the ratio
        return true;
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked) {
        return transformation.toTransform().createTransformedShape(getShapeAtRatio(ratio).getOutline(swf, stroked));
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        CanvasMorphShapeExporter cmse = new CanvasMorphShapeExporter(swf, getShapeAtRatio(0), getShapeAtRatio(MAX_RATIO), null, unitDivisor, 0, 0);
        cmse.export();
        result.append(cmse.getShapeData());
    }
}
