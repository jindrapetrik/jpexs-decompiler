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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.morphshape.CanvasMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.morphshape.SVGMorphShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
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
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author JPEXS
 */
public class DefineMorphShapeTag extends CharacterTag implements MorphShapeTag {

    @SWFType(BasicType.UI16)
    public int characterId;
    public RECT startBounds;
    public RECT endBounds;
    public MORPHFILLSTYLEARRAY morphFillStyles;
    public MORPHLINESTYLEARRAY morphLineStyles;
    public SHAPE startEdges;
    public SHAPE endEdges;
    public static final int ID = 46;
    public static final int MAX_RATIO = 65535;

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<>();
        ret.addAll(morphFillStyles.getNeededCharacters());
        ret.addAll(startEdges.getNeededCharacters());
        ret.addAll(endEdges.getNeededCharacters());
        return ret;
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
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            SWFOutputStream sos2 = new SWFOutputStream(baos2, getVersion());
            sos2.writeMORPHFILLSTYLEARRAY(morphFillStyles, 1);
            sos2.writeMORPHLINESTYLEARRAY(morphLineStyles, 1);
            sos2.writeSHAPE(startEdges, 1);
            byte[] d = baos2.toByteArray();
            sos.writeUI32(d.length);
            sos.write(d);
            sos.writeSHAPE(endEdges, 1);

        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public DefineMorphShapeTag(SWF swf, byte[] headerData, byte[] data, long pos) throws IOException {
        super(swf, ID, "DefineMorphShape", headerData, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        characterId = sis.readUI16();
        startBounds = sis.readRECT();
        endBounds = sis.readRECT();
        long offset = sis.readUI32(); //ignore
        morphFillStyles = sis.readMORPHFILLSTYLEARRAY();
        morphLineStyles = sis.readMORPHLINESTYLEARRAY(1);
        startEdges = sis.readSHAPE(1,true);
        endEdges = sis.readSHAPE(1,true);
    }

    @Override
    public RECT getRect() {
        RECT rect = new RECT();
        rect.Xmin = Math.min(startBounds.Xmin, endBounds.Xmin);
        rect.Ymin = Math.min(startBounds.Ymin, endBounds.Ymin);
        rect.Xmax = Math.max(startBounds.Xmax, endBounds.Xmax);
        rect.Ymax = Math.max(startBounds.Ymax, endBounds.Ymax);
        return rect;
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
        return 1;
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
                StyleChangeRecord scr = (StyleChangeRecord) scr1.clone();
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
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SHAPEWITHSTYLE shape = getShapeAtRatio(ratio);
        // shapeNum: 3
        // todo: Currently the generated image is not cached, because the cache 
        // key contains the hashCode of the finalRecord object, and it is always 
        // recreated
        BitmapExporter.export(swf, shape, null, image, transformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) {
        if (ratio == -2) {
            SHAPEWITHSTYLE beginShapes = getShapeAtRatio(0);
            SHAPEWITHSTYLE endShapes = getShapeAtRatio(65535);
            SVGMorphShapeExporter shapeExporter = new SVGMorphShapeExporter(swf, beginShapes, endShapes, exporter, null, colorTransform);
            shapeExporter.export();
        } else {
            SHAPEWITHSTYLE shapes = getShapeAtRatio(ratio);
            SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, shapes, exporter, null, colorTransform);
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
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        return transformation.toTransform().createTransformedShape(getShapeAtRatio(ratio).getOutline());
    }
    
    @Override
    public String toHtmlCanvas(double unitDivisor) {
        CanvasMorphShapeExporter cmse = new CanvasMorphShapeExporter(swf, getShapeAtRatio(0), getShapeAtRatio(MAX_RATIO), new ColorTransform(), unitDivisor, 0, 0);
        cmse.export();
        
        return cmse.getShapeData();
    }
}
