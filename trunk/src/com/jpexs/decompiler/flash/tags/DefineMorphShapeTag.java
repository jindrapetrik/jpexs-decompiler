/*
 *  Copyright (C) 2010-2013 JPEXS
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
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author JPEXS
 */
public class DefineMorphShapeTag extends CharacterTag implements BoundedTag, MorphShapeTag, DrawableTag {

    public int characterId;
    public RECT startBounds;
    public RECT endBounds;
    public MORPHFILLSTYLEARRAY morphFillStyles;
    public MORPHLINESTYLEARRAY morphLineStyles;
    public SHAPE startEdges;
    public SHAPE endEdges;

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<>();
        ret.addAll(morphFillStyles.getNeededCharacters());
        ret.addAll(startEdges.getNeededCharacters());
        ret.addAll(endEdges.getNeededCharacters());
        return ret;
    }

    @Override
    public int getCharacterID() {
        return characterId;
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(characterId);
            sos.writeRECT(startBounds);
            sos.writeRECT(endBounds);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
            sos2.writeMORPHFILLSTYLEARRAY(morphFillStyles, 1);
            sos2.writeMORPHLINESTYLEARRAY(morphLineStyles, 1);
            sos2.writeSHAPE(startEdges, 1);
            byte d[] = baos2.toByteArray();
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
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineMorphShapeTag(byte data[], int version, long pos) throws IOException {
        super(46, "DefineMorphShape", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterId = sis.readUI16();
        startBounds = sis.readRECT();
        endBounds = sis.readRECT();
        long offset = sis.readUI32(); //ignore
        morphFillStyles = sis.readMORPHFILLSTYLEARRAY();
        morphLineStyles = sis.readMORPHLINESTYLEARRAY(1);
        startEdges = sis.readSHAPE(1);
        endEdges = sis.readSHAPE(1);
    }

    @Override
    public RECT getRect(HashMap<Integer, CharacterTag> characters) {
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

    // @Override
    public List<GeneralPath> getPaths(List<Tag> tags) {
        return null; //FIXME
    }

    @Override
    public BufferedImage toImage(int frame, List<Tag> tags, RECT displayRect, HashMap<Integer, CharacterTag> characters) {
        List<SHAPERECORD> finalRecords = new ArrayList<>();
        FILLSTYLEARRAY fillStyles = morphFillStyles.getFillStylesAt(frame);
        LINESTYLEARRAY lineStyles = morphLineStyles.getLineStylesAt(getShapeNum(), frame);
        int endIndex = 0;

        for (int startIndex = 0; startIndex < startEdges.shapeRecords.size(); startIndex++, endIndex++) {

            if (startIndex == 0) {
                StyleChangeRecord scr1 = (StyleChangeRecord) startEdges.shapeRecords.get(startIndex);
                StyleChangeRecord scr2 = (StyleChangeRecord) startEdges.shapeRecords.get(endIndex);
                StyleChangeRecord scr = (StyleChangeRecord) scr1.clone();
                if (scr1.stateMoveTo && scr2.stateMoveTo) {
                    scr.moveDeltaX = scr1.moveDeltaX + (scr2.moveDeltaX - scr1.moveDeltaX) * frame / 65535;
                    scr.moveDeltaY = scr1.moveDeltaY + (scr2.moveDeltaY - scr1.moveDeltaY) * frame / 65535;
                    finalRecords.add(scr);
                    continue;
                }
            }
            SHAPERECORD edge1 = null;
            do {
                edge1 = startEdges.shapeRecords.get(startIndex);
                if (edge1 instanceof StyleChangeRecord) {
                    finalRecords.add(edge1);
                    edge1 = null;
                    startIndex++;
                }
            } while (edge1 == null);
            SHAPERECORD edge2 = endEdges.shapeRecords.get(endIndex);
            if (edge1 instanceof EndShapeRecord) {
                finalRecords.add(edge1);
                break;
            }
            if (edge2 instanceof EndShapeRecord) {
                break;
            }
            if ((edge1 instanceof StyleChangeRecord) && (edge2 instanceof StyleChangeRecord)) {
                StyleChangeRecord scr1 = (StyleChangeRecord) edge1;
                StyleChangeRecord scr2 = (StyleChangeRecord) edge2;
                StyleChangeRecord scr = (StyleChangeRecord) scr1.clone();
                if (scr1.stateMoveTo && scr2.stateMoveTo) {
                    scr.moveDeltaX = scr1.moveDeltaX + (scr2.moveDeltaX - scr1.moveDeltaX) * frame / 65535;
                    scr.moveDeltaY = scr1.moveDeltaY + (scr2.moveDeltaY - scr1.moveDeltaY) * frame / 65535;
                    finalRecords.add(scr);
                    continue;
                }
            }
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
            cer.controlDeltaX = cer1.controlDeltaX + (cer2.controlDeltaX - cer1.controlDeltaX) * frame / 65535;
            cer.controlDeltaY = cer1.controlDeltaY + (cer2.controlDeltaY - cer1.controlDeltaY) * frame / 65535;
            cer.anchorDeltaX = cer1.anchorDeltaX + (cer2.anchorDeltaX - cer1.anchorDeltaX) * frame / 65535;
            cer.anchorDeltaY = cer1.anchorDeltaY + (cer2.anchorDeltaY - cer1.anchorDeltaY) * frame / 65535;
            finalRecords.add(cer);
        }
        return SHAPERECORD.shapeToImage(tags, 1, fillStyles, lineStyles, finalRecords);
    }

    @Override
    public Point getImagePos(int frame, HashMap<Integer, CharacterTag> characters) {
        return new Point(
                (startBounds.Xmin + (endBounds.Xmin - startBounds.Xmin) * frame / 65535) / 20,
                (startBounds.Ymin + (endBounds.Ymin - startBounds.Ymin) * frame / 65535) / 20);
    }

    @Override
    public int getNumFrames() {
        return 65536;
    }
}
