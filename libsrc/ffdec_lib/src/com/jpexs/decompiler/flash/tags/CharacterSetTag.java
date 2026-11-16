/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class CharacterSetTag extends Tag {
    public static final int ID = 51;

    public static final String NAME = "CharacterSet";
   
    /**
     * Shift-JIS encoding 1/0
     * GetSystemMetrics(SM_DBCSENABLED) ? 1 : 0
     */
    @SWFType(BasicType.UI16)
    public int shiftJIS;
        
    /**
     * Mac Roman encoding 1/0
     */
    @SWFType(BasicType.UI16)
    public int macRoman;
    
    
    public CharacterSetTag(SWF swf) {
        super(swf, ID, NAME, null);       
    }
    
    public CharacterSetTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        shiftJIS = sis.readUI16("shiftJIS");
        macRoman = sis.readUI16("macRoman");
    }

    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(shiftJIS);
        sos.writeUI16(macRoman);
    }
}
