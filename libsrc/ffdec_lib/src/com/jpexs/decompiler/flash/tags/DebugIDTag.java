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
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.annotations.UUID;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DebugID tag - Contains a unique identifier for the SWF file.
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public class DebugIDTag extends Tag {

    public static final int ID = 63;

    public static final String NAME = "DebugID";

    @SWFType(value = BasicType.UI8, count = 16)
    @UUID
    public byte[] debugId;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DebugIDTag(SWF swf) {
        super(swf, ID, NAME, null);
        debugId = new byte[16];
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DebugIDTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        debugId = sis.readBytesEx(16, "debugId");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        if (debugId.length != 16) {
            Logger.getLogger(DebugIDTag.class.getName()).log(Level.WARNING, "DebugID should be 16 bytes");
        }

        sos.write(debugId);
    }
}
