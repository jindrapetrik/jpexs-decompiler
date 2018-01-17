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
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class DefineText2Tag extends StaticTextTag {

    public static final int ID = 33;

    public static final String NAME = "DefineText2";

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineText2Tag(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
        textBounds = new RECT();
        textMatrix = new MATRIX();
        textRecords = new ArrayList<>();
        glyphBits = 0;
        advanceBits = 0;
    }

    public DefineText2Tag(SWF swf, int characterID, RECT textBounds, MATRIX textMatrix, List<TEXTRECORD> textRecords) {
        super(swf, ID, NAME, null);
        this.characterID = characterID;
        this.textBounds = textBounds;
        this.textMatrix = textMatrix;
        this.textRecords = textRecords;
        this.glyphBits = 0;
        this.advanceBits = 0;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineText2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public int getTextNum() {
        return 2;
    }
}
