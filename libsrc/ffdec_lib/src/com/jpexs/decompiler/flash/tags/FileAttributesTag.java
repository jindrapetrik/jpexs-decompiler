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
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 * FileAttributes tag - attributes of the SWF file.
 *
 * @author JPEXS
 */
@SWFVersion(from = 8)
public class FileAttributesTag extends Tag {

    public static final int ID = 69;

    public static final String NAME = "FileAttributes";

    @Reserved
    public boolean reservedA;

    public boolean useDirectBlit;

    public boolean useGPU;

    public boolean hasMetadata;

    public boolean actionScript3;

    public boolean useNetwork;

    public boolean noCrossDomainCache;

    public boolean swfRelativeUrls;

    @SWFType(value = BasicType.UB, count = 24)
    @Reserved
    public int reservedB;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public FileAttributesTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public FileAttributesTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        reservedA = sis.readUB(1, "reservedA") == 1; // reserved
        // UB[1] == 0  (reserved)
        useDirectBlit = sis.readUB(1, "useDirectBlit") != 0;
        useGPU = sis.readUB(1, "useGPU") != 0;
        hasMetadata = sis.readUB(1, "hasMetadata") != 0;
        actionScript3 = sis.readUB(1, "actionScript3") != 0;
        noCrossDomainCache = sis.readUB(1, "noCrossDomainCache") != 0;
        swfRelativeUrls = sis.readUB(1, "swfRelativeUrls") == 1;
        useNetwork = sis.readUB(1, "useNetwork") != 0;
        // UB[24] == 0 (reserved)
        int bitCount = 24;
        if (sis.available() * 8 < bitCount) {
            bitCount = sis.available() * 8;
        }

        reservedB = (int) sis.readUB(bitCount, "reservedB"); //reserved
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUB(1, reservedA ? 1 : 0); //reserved
        sos.writeUB(1, useDirectBlit ? 1 : 0);
        sos.writeUB(1, useGPU ? 1 : 0);
        sos.writeUB(1, hasMetadata ? 1 : 0);
        sos.writeUB(1, actionScript3 ? 1 : 0);
        sos.writeUB(1, noCrossDomainCache ? 1 : 0);
        sos.writeUB(1, swfRelativeUrls ? 1 : 0);
        sos.writeUB(1, useNetwork ? 1 : 0);
        sos.writeUB(24, reservedB); //reserved
    }
}
