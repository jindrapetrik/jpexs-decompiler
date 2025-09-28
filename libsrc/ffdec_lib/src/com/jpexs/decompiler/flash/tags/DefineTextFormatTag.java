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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterModifier;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.text.format.TextFormatRecord;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DefineTextFormat tag - define text format. Used by Flash Generator Templates.
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class DefineTextFormatTag extends Tag implements CharacterModifier {

    public static final int ID = 42;

    public static final String NAME = "DefineTextFormat";   
    
    @SWFType(BasicType.UI16)
    public int textId;
    
    public RECT bounds;
    
    public List<TextFormatRecord> records = new ArrayList<>();
    
    
    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineTextFormatTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public DefineTextFormatTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        textId = sis.readUI16("textId");
        bounds = sis.readRECT("textBounds");
        records = sis.readTextFormatRecords("textFormatRecords");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(textId);
        sos.writeRECT(bounds);
        sos.writeTextFormatRecords(records);
    }

    @Override
    public int getCharacterId() {
        return textId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.textId = characterId;
    }
}
