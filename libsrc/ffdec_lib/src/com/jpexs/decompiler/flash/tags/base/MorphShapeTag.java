/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
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
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
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
 * Base class for morph shape tags.
 *
 * @author JPEXS
 */
public abstract class MorphShapeTag extends DrawableTag {

    /**
     * Maximum ratio value
     */
    public static final int MAX_RATIO = 65535;

    /**
     * Character ID
     */
    @SWFType(BasicType.UI16)
    public int characterId;

    /**
     * Start bounds
     */
    public RECT startBounds;

    /**
     * End bounds
     */
    public RECT endBounds;

    /**
     * Morph fill styles
     */
    public MORPHFILLSTYLEARRAY morphFillStyles;

    /**
     * Morph line styles
     */
    public MORPHLINESTYLEARRAY morphLineStyles;

    /**
     * Start edges
     */
    public SHAPE startEdges;

    /**
     * End edges
     */
    public SHAPE endEdges;

    /**
     * Constructor.
     *
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public MorphShapeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Get morph shape number.
     * DefineMorphShape = 1, DefineMorphShape2 = 2
     * @return Morph shape number
     */
    public abstract int getShapeNum();

    @Override
    public RECT getRectWithStrokes() {
        int shapeNum = getShapeNum();
        int maxWidth = 0;
        if (shapeNum == 1) {
            for (MORPHLINESTYLE ls : morphLineStyles.lineStyles) {
                if (ls.startWidth > maxWidth) {
                    maxWidth = ls.startWidth;
                }
                if (ls.endWidth > maxWidth) {
                    maxWidth = ls.endWidth;
                }
            }
        }
        if (shapeNum == 2) {
            for (MORPHLINESTYLE2 ls : morphLineStyles.lineStyles2) {
                if (ls.startWidth > maxWidth) {
                    maxWidth = ls.startWidth;
                }
                if (ls.endWidth > maxWidth) {
                    maxWidth = ls.endWidth;
                }
            }
        }
        RECT r = new RECT(getRect());
        r.Xmin -= maxWidth;
        r.Ymin -= maxWidth;
        r.Xmax += maxWidth;
        r.Ymax += maxWidth;
        return r;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        morphFillStyles.getNeededCharacters(needed, swf);
        startEdges.getNeededCharacters(needed, swf);
        endEdges.getNeededCharacters(needed, swf);
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
    public RECT getRect() {
        return getRect(new HashSet<>());
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

    /**
     * Gets start bounds.
     * @return Start bounds
     */
    public RECT getStartBounds() {
        return startBounds;
    }

    /**
     * Gets end bounds.
     * @return End bounds
     */
    public RECT getEndBounds() {
        return endBounds;
    }

    /**
     * Gets morph fill styles.
     * @return Morph fill styles
     */
    public MORPHFILLSTYLEARRAY getFillStyles() {
        return morphFillStyles;
    }

    /**
     * Gets morph line styles.
     * @return Morph line styles
     */
    public MORPHLINESTYLEARRAY getLineStyles() {
        return morphLineStyles;
    }

    /**
     * Gets start edges.
     * @return Start edges
     */
    public SHAPE getStartEdges() {
        return startEdges;
    }

    /**
     * Gets end edges.
     * @return End edges
     */
    public SHAPE getEndEdges() {
        return endEdges;
    }

    /**
     * Gets shape tag at ratio.
     * @param ratio Ratio
     * @return Shape tag
     */
    public abstract ShapeTag getShapeTagAtRatio(int ratio);

    /**
     * Gets start shape tag.
     * @return Start shape tag
     */
    public ShapeTag getStartShapeTag() {
        return getShapeTagAtRatio(0);
    }

    /**
     * Gets end shape tag.
     * @return End shape tag
     */
    public ShapeTag getEndShapeTag() {
        return getShapeTagAtRatio(65535);
    }

    /**
     * Gets shape at ratio.
     * @param ratio Ratio
     * @return Shape
     */
    public SHAPEWITHSTYLE getShapeAtRatio(int ratio) {
        List<SHAPERECORD> finalRecords = new ArrayList<>();
        FILLSTYLEARRAY fillStyles = morphFillStyles.getFillStylesAt(ratio);
        LINESTYLEARRAY lineStyles = morphLineStyles.getLineStylesAt(getShapeNum(), ratio);

        int startPosX = 0;
        int startPosY = 0;
        int endPosX = 0;
        int endPosY = 0;
        int posX = 0;
        int posY = 0;

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
                    scr.moveDeltaX = startPosX + (int) Math.round((endPosX - startPosX) * ratio / (double) MAX_RATIO);
                    scr.moveDeltaY = startPosY + (int) Math.round((endPosY - startPosY) * ratio / (double) MAX_RATIO);
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
                cer.controlDeltaX = cer1.controlDeltaX + (int) Math.round((cer2.controlDeltaX - cer1.controlDeltaX) * ratio / (double) MAX_RATIO);
                cer.controlDeltaY = cer1.controlDeltaY + (int) Math.round((cer2.controlDeltaY - cer1.controlDeltaY) * ratio / (double) MAX_RATIO);
                cer.anchorDeltaX = cer1.anchorDeltaX + (int) Math.round((cer2.anchorDeltaX - cer1.anchorDeltaX) * ratio / (double) MAX_RATIO);
                cer.anchorDeltaY = cer1.anchorDeltaY + (int) Math.round((cer2.anchorDeltaY - cer1.anchorDeltaY) * ratio / (double) MAX_RATIO);
                startPosX += cer1.controlDeltaX + cer1.anchorDeltaX;
                startPosY += cer1.controlDeltaY + cer1.anchorDeltaY;
                endPosX += cer2.controlDeltaX + cer2.anchorDeltaX;
                endPosY += cer2.controlDeltaY + cer2.anchorDeltaY;
                posX += cer.controlDeltaX + cer.anchorDeltaX;
                posY += cer.controlDeltaY + cer.anchorDeltaY;
                cer.calculateBits();
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
                ser.deltaX = ser1.deltaX + (int) Math.round((ser2.deltaX - ser1.deltaX) * ratio / (double) MAX_RATIO);
                ser.deltaY = ser1.deltaY + (int) Math.round((ser2.deltaY - ser1.deltaY) * ratio / (double) MAX_RATIO);
                startPosX += ser1.deltaX;
                startPosY += ser1.deltaY;
                endPosX += ser2.deltaX;
                endPosY += ser2.deltaY;
                posX += ser.deltaX;
                posY += ser.deltaX;
                ser.simplify();
                ser.calculateBits();
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
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        SHAPEWITHSTYLE shape = getShapeAtRatio(ratio);
        // morphShape using shapeNum=3, morphShape2 using shapeNum=4
        // todo: Currently the generated image is not cached, because the cache
        // key contains the hashCode of the finalRecord object, and it is always
        // recreated
        BitmapExporter.export(ShapeTag.WIND_EVEN_ODD /*??? FIXME*/, getShapeNum() == 2 ? 4 : 1, swf, shape, null, image, unzoom, transformation, strokeTransformation, colorTransform, scaleStrokes, canUseSmoothing);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) {

        if (ratio == -2) {
            SHAPEWITHSTYLE beginShapes = getShapeAtRatio(0);
            SHAPEWITHSTYLE endShapes = getShapeAtRatio(65535);
            SVGMorphShapeExporter shapeExporter = new SVGMorphShapeExporter(getShapeNum(), swf, beginShapes, endShapes, getCharacterId(), exporter, null, colorTransform, 1);
            shapeExporter.export();
        } else {
            SHAPEWITHSTYLE shapes = getShapeAtRatio(ratio);
            SVGShapeExporter shapeExporter = new SVGShapeExporter(ShapeTag.WIND_EVEN_ODD /*??? FIXME*/, getShapeNum() == 2 ? 4 : 1, swf, shapes, getCharacterId(), exporter, null, colorTransform, 1);
            shapeExporter.export();
        }
    }

    @Override
    public int getNumFrames() {
        return 65536;
    }

    @Override
    public boolean isSingleFrame() {
        // Morphshape is a single frame specified with the ratio
        return true;
    }

    @Override
    public Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
        return transformation.toTransform().createTransformedShape(getShapeAtRatio(ratio).getOutline(fast, getShapeNum() == 2 ? 4 : 1, swf, stroked));
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        CanvasMorphShapeExporter cmse = new CanvasMorphShapeExporter(getShapeNum(), swf, getShapeAtRatio(0), getShapeAtRatio(MAX_RATIO), null, unitDivisor, 0, 0);
        cmse.export();
        result.append(cmse.getShapeData());
    }

    /**
     * Updates start bounds.
     */
    public void updateStartBounds() {
        startBounds = SHAPERECORD.getBounds(startEdges.shapeRecords, morphLineStyles.getStartLineStyles(getShapeNum()), getShapeNum() == 2 ? 4 : 3, false);
    }

    /**
     * Updates end bounds.
     */
    public void updateEndBounds() {
        endBounds = SHAPERECORD.getBounds(endEdges.shapeRecords, morphLineStyles.getEndLineStyles(getShapeNum()), getShapeNum() == 2 ? 4 : 3, false);
    }

    /**
     * Updates bounds.
     */
    public void updateBounds() {
        updateStartBounds();
        updateEndBounds();
    }
}
