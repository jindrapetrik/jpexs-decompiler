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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class DefineMorphShapeTag extends MorphShapeTag {

    public static final int ID = 46;

    public static final String NAME = "DefineMorphShape";

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineMorphShapeTag(SWF swf) {
        super(swf, ID, NAME, null);
        characterId = swf.getNextCharacterId();
        startBounds = new RECT();
        endBounds = new RECT();
        startEdges = SHAPE.createEmpty(1);
        endEdges = SHAPE.createEmpty(1);
        morphFillStyles = new MORPHFILLSTYLEARRAY();
        morphFillStyles.fillStyles = new MORPHFILLSTYLE[0];
        morphLineStyles = new MORPHLINESTYLEARRAY();
        morphLineStyles.lineStyles = new MORPHLINESTYLE[0];
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineMorphShapeTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        startBounds = sis.readRECT("startBounds");
        endBounds = sis.readRECT("endBounds");
        long offset = sis.readUI32("offset"); // ignore
        morphFillStyles = sis.readMORPHFILLSTYLEARRAY("morphFillStyles");
        morphLineStyles = sis.readMORPHLINESTYLEARRAY(1, "morphLineStyles");
        startEdges = sis.readSHAPE(1, true, "startEdges");
        endEdges = sis.readSHAPE(1, true, "endEdges");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeRECT(startBounds);
        sos.writeRECT(endBounds);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(baos2, getVersion());
        sos2.writeMORPHFILLSTYLEARRAY(morphFillStyles, 1);
        sos2.writeMORPHLINESTYLEARRAY(morphLineStyles, 1);
        sos2.writeSHAPE(startEdges, 1);
        byte[] ba2 = baos2.toByteArray();
        sos.writeUI32(ba2.length);
        sos.write(ba2);
        sos.writeSHAPE(endEdges, 1);
    }

    @Override
    public int getShapeNum() {
        return 1;
    }
}
