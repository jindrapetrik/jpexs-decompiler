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
public class ProductInfoTag extends Tag {

    public static final int ID = 41;

    public static final String NAME = "ProductInfo";

    @SWFType(BasicType.UI32)
    public long productID;

    @SWFType(BasicType.UI32)
    public long edition;

    @SWFType(BasicType.UI8)
    public int majorVersion;

    @SWFType(BasicType.UI8)
    public int minorVersion;

    @SWFType(BasicType.UI32)
    public long buildLow;

    @SWFType(BasicType.UI32)
    public long buildHigh;

    @SWFType(BasicType.UI32)
    public long compilationDateLow;

    @SWFType(BasicType.UI32)
    public long compilationDateHigh;

    /**
     * Constructor
     *
     * @param swf
     */
    public ProductInfoTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public ProductInfoTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        /*
         * 0: Unknown
         * 1: Macromedia Flex for J2EE
         * 2: Macromedia Flex for .NET
         * 3: Adobe Flex
         */
        productID = sis.readUI32("productID");

        /*
         * 0: Developer Edition
         * 1: Full Commercial Edition
         * 2: Non Commercial Edition
         * 3: Educational Edition
         * 4: Not For Resale (NFR) Edition
         * 5: Trial Edition
         * 6: None
         */
        edition = sis.readUI32("edition");
        majorVersion = sis.readUI8("majorVersion");
        minorVersion = sis.readUI8("minorVersion");
        buildLow = sis.readUI32("buildLow");
        buildHigh = sis.readUI32("buildHigh");
        compilationDateLow = sis.readUI32("compilationDateLow");
        compilationDateHigh = sis.readUI32("compilationDateHigh");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI32(productID);
        sos.writeUI32(edition);
        sos.writeUI8(majorVersion);
        sos.writeUI8(minorVersion);
        sos.writeUI32(buildLow);
        sos.writeUI32(buildHigh);
        sos.writeUI32(compilationDateLow);
        sos.writeUI32(compilationDateHigh);
    }
}
