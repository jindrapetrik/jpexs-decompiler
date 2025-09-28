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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class PlaceImagePrivateTag extends Tag {
    public static final int ID = 85;

    public static final String NAME = "PlaceImagePrivate";
    
    @SWFType(BasicType.UI16)
    public int imageId = 0;
    
    @SWFType(BasicType.UI16)
    public int depth = 0;
    
    public RECT bounds = new RECT();
    
    public PlaceImagePrivateTag(SWF swf) {
        super(swf, ID, NAME, null);       
    }
    
    public PlaceImagePrivateTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        imageId = sis.readUI16("imageId");
        if (sis.available() > 0) {
            depth = sis.readUI16("depth");
        }
        if (sis.available() > 0) {
            bounds = sis.readRECT("bounds");
        }
    }

    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(imageId);
        sos.writeUI16(depth);
        sos.writeRECT(bounds);
    }
}
