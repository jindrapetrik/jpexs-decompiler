/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.morphshape.CanvasMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.morphshape.SVGMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author JPEXS
 */
public class DefineMorphShape2Tag extends MorphShapeTag {

    @SWFType(BasicType.UI16)
    public int characterId;

    public RECT startBounds;

    public RECT endBounds;

    public RECT startEdgeBounds;

    public RECT endEdgeBounds;

    @Reserved
    @SWFType(value = BasicType.UB, count = 6)
    public int reserved;

    public boolean usesNonScalingStrokes;

    public boolean usesScalingStrokes;

    public MORPHFILLSTYLEARRAY morphFillStyles;

    public MORPHLINESTYLEARRAY morphLineStyles;

    public SHAPE startEdges;

    public SHAPE endEdges;

    public static final int ID = 84;

    public static final int MAX_RATIO = 65535;

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
    public RECT getRect(Set<BoundedTag> added) {
        RECT rect = new RECT();
        rect.Xmin = Math.min(startBounds.Xmin, endBounds.Xmin);
        rect.Ymin = Math.min(startBounds.Ymin, endBounds.Ymin);
        rect.Xmax = Math.max(startBounds.Xmax, endBounds.Xmax);
        rect.Ymax = Math.max(startBounds.Ymax, endBounds.Ymax);
        return rect;
    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterId);
            sos.writeRECT(startBounds);
            sos.writeRECT(endBounds);
            sos.writeRECT(startEdgeBounds);
            sos.writeRECT(endEdgeBounds);
            sos.writeUB(6, reserved);
            sos.writeUB(1, usesNonScalingStrokes ? 1 : 0);
            sos.writeUB(1, usesScalingStrokes ? 1 : 0);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            SWFOutputStream sos2 = new SWFOutputStream(baos2, getVersion());
            sos2.writeMORPHFILLSTYLEARRAY(morphFillStyles, 2);
            sos2.writeMORPHLINESTYLEARRAY(morphLineStyles, 2);
            sos2.writeSHAPE(startEdges, 2);
            byte[] ba2 = baos2.toByteArray();
            sos.writeUI32(ba2.length);
            sos.write(ba2);
            sos.writeSHAPE(endEdges, 2);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineMorphShape2Tag(SWF swf) {
        super(swf, ID, "DefineMorphShape2", null);
        characterId = swf.getNextCharacterId();
        startBounds = new RECT();
        endBounds = new RECT();
        startEdgeBounds = new RECT();
        endEdgeBounds = new RECT();
        startEdges = SHAPE.createEmpty(2);
        endEdges = SHAPE.createEmpty(2);
        morphFillStyles = new MORPHFILLSTYLEARRAY();
        morphFillStyles.fillStyles = new MORPHFILLSTYLE[0];
        morphLineStyles = new MORPHLINESTYLEARRAY();
        morphLineStyles.lineStyles2 = new MORPHLINESTYLE2[0];
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineMorphShape2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineMorphShape2", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        startBounds = sis.readRECT("startBounds");
        endBounds = sis.readRECT("endBounds");
        startEdgeBounds = sis.readRECT("startEdgeBounds");
        endEdgeBounds = sis.readRECT("endEdgeBounds");
        reserved = (int) sis.readUB(6, "reserved");
        usesNonScalingStrokes = sis.readUB(1, "usesNonScalingStrokes") == 1;
        usesScalingStrokes = sis.readUB(1, "usesScalingStrokes") == 1;
        long offset = sis.readUI32("offset");
        morphFillStyles = sis.readMORPHFILLSTYLEARRAY("morphFillStyles");
        morphLineStyles = sis.readMORPHLINESTYLEARRAY(2, "morphLineStyles");
        startEdges = sis.readSHAPE(2, true, "startEdges");
        endEdges = sis.readSHAPE(2, true, "endEdges");
    }

    @Override
    public RECT getStartBounds() {
        return startBounds;
    }

    @Override
    public RECT getEndBounds() {
        return endBounds;
    }

    @Override
    public MORPHFILLSTYLEARRAY getFillStyles() {
        return morphFillStyles;
    }

    @Override
    public MORPHLINESTYLEARRAY getLineStyles() {
        return morphLineStyles;
    }

    @Override
    public SHAPE getStartEdges() {
        return startEdges;
    }

    @Override
    public SHAPE getEndEdges() {
        return endEdges;
    }

    @Override
    public int getShapeNum() {
        return 2;
    }

    @Override
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
                    scr.moveDeltaX = startPosX + (endPosX - startPosX) * ratio / 65535;
                    scr.moveDeltaY = startPosY + (endPosY - startPosY) * ratio / 65535;
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
                cer.controlDeltaX = cer1.controlDeltaX + (cer2.controlDeltaX - cer1.controlDeltaX) * ratio / 65535;
                cer.controlDeltaY = cer1.controlDeltaY + (cer2.controlDeltaY - cer1.controlDeltaY) * ratio / 65535;
                cer.anchorDeltaX = cer1.anchorDeltaX + (cer2.anchorDeltaX - cer1.anchorDeltaX) * ratio / 65535;
                cer.anchorDeltaY = cer1.anchorDeltaY + (cer2.anchorDeltaY - cer1.anchorDeltaY) * ratio / 65535;
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
                ser.deltaX = ser1.deltaX + (ser2.deltaX - ser1.deltaX) * ratio / 65535;
                ser.deltaY = ser1.deltaY + (ser2.deltaY - ser1.deltaY) * ratio / 65535;
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
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SHAPEWITHSTYLE shape = getShapeAtRatio(ratio);
        // shapeNum: 4
        // todo: Currently the generated image is not cached, because the cache
        // key contains the hashCode of the finalRecord object, and it is always
        // recreated
        BitmapExporter.export(swf, shape, null, image, transformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) {
        if (ratio == -2) {
            SHAPEWITHSTYLE beginShapes = getShapeAtRatio(0);
            SHAPEWITHSTYLE endShapes = getShapeAtRatio(65535);
            SVGMorphShapeExporter shapeExporter = new SVGMorphShapeExporter(swf, beginShapes, endShapes, exporter, null, colorTransform, zoom);
            shapeExporter.export();
        } else {
            SHAPEWITHSTYLE shapes = getShapeAtRatio(ratio);
            SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, shapes, exporter, null, colorTransform, zoom);
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
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation) {
        return transformation.toTransform().createTransformedShape(getShapeAtRatio(ratio).getOutline());
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        CanvasMorphShapeExporter cmse = new CanvasMorphShapeExporter(swf, getShapeAtRatio(0), getShapeAtRatio(MAX_RATIO), new ColorTransform(), unitDivisor, 0, 0);
        cmse.export();
        return cmse.getShapeData();
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }
}
