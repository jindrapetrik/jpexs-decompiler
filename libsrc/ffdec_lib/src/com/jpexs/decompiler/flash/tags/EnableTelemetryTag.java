/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enable flash profiling information
 *
 * @author JPEXS
 */
public class EnableTelemetryTag extends Tag {

    public static final int ID = 93;

    public static final String NAME = "EnableTelemetry";

    @SWFType(value = BasicType.UB, count = 16)
    @Reserved
    public int reserved;

    @Optional
    @SWFType(value = BasicType.UI8, count = 32)
    public byte[] passwordHash;

    /**
     * Constructor
     *
     * @param swf
     */
    public EnableTelemetryTag(SWF swf) {
        super(swf, ID, NAME, null);
        passwordHash = new byte[32];
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public EnableTelemetryTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        reserved = (int) sis.readUB(16, "reserved");
        if (sis.available() > 0) {
            if (sis.available() != 32) {
                Logger.getLogger(EnableTelemetryTag.class.getName()).log(Level.WARNING, "PasswordHash should be 32 bytes");
            }

            passwordHash = sis.readBytesEx(32, "passwordHash");
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUB(16, reserved);
        if (passwordHash != null) {
            sos.write(passwordHash);
        }
    }
}
