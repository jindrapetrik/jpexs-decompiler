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
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * DefineButtonCxform tag - defines a color transform for a button.
 *
 * @author JPEXS
 */
@SWFVersion(from = 2)
public class DefineButtonCxformTag extends Tag implements CharacterModifier {

    public static final int ID = 23;

    public static final String NAME = "DefineButtonCxform";

    @SWFType(BasicType.UI16)
    public int buttonId;

    public CXFORM buttonColorTransform;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineButtonCxformTag(SWF swf) {
        super(swf, ID, NAME, null);
        buttonColorTransform = new CXFORM();
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineButtonCxformTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        buttonId = sis.readUI16("buttonId");
        buttonColorTransform = sis.readCXFORM("buttonColorTransform");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(buttonId);
        sos.writeCXFORM(buttonColorTransform);
    }

    @Override
    public int getCharacterId() {
        return buttonId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.buttonId = characterId;
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("bid", "" + buttonId);
        return ret;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        needed.add(buttonId);
    }
}
