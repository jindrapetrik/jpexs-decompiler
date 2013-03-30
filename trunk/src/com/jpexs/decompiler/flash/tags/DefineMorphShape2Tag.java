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
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 *
 *
 * @author JPEXS
 */
public class DefineMorphShape2Tag extends CharacterTag implements BoundedTag, AloneTag {

    public int characterId;
    public RECT startBounds;
    public RECT endBounds;
    public RECT startEdgeBounds;
    public RECT endEdgeBounds;
    public boolean usesNonScalingStrokes;
    public boolean usesScalingStrokes;
    public MORPHFILLSTYLEARRAY morphFillStyles;
    public MORPHLINESTYLEARRAY morphLineStyles;
    public SHAPE startEdges;
    public SHAPE endEdges;

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
            sos.writeRECT(startEdgeBounds);
            sos.writeRECT(endEdgeBounds);
            sos.writeUB(6, 0);
            sos.writeUB(1, usesNonScalingStrokes ? 1 : 0);
            sos.writeUB(1, usesScalingStrokes ? 1 : 0);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
            sos2.writeMORPHFILLSTYLEARRAY(morphFillStyles, 2);
            sos2.writeMORPHLINESTYLEARRAY(morphLineStyles, 2);
            sos2.writeSHAPE(startEdges, 1);
            byte ba2[] = baos2.toByteArray();
            sos.writeUI32(ba2.length);
            sos.write(ba2);
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
     * @throws IOException
     */
    public DefineMorphShape2Tag(byte data[], int version, long pos) throws IOException {
        super(84, "DefineMorphShape2", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterId = sis.readUI16();
        startBounds = sis.readRECT();
        endBounds = sis.readRECT();
        startEdgeBounds = sis.readRECT();
        endEdgeBounds = sis.readRECT();
        sis.readUB(6); //reserved
        usesNonScalingStrokes = sis.readUB(1) == 1;
        usesScalingStrokes = sis.readUB(1) == 1;
        long offset = sis.readUI32();
        morphFillStyles = sis.readMORPHFILLSTYLEARRAY();
        morphLineStyles = sis.readMORPHLINESTYLEARRAY(2);
        startEdges = sis.readSHAPE(1);
        endEdges = sis.readSHAPE(1);
    }
}
