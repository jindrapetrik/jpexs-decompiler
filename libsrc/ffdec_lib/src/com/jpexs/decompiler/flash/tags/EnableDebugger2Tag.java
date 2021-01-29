/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.tags.base.PasswordTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.HashType;
import com.jpexs.decompiler.flash.types.annotations.Password;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.MD5Crypt;
import java.io.IOException;

/**
 * Marks the file is not importable for editing
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public final class EnableDebugger2Tag extends Tag implements PasswordTag {

    public static final int ID = 64;

    public static final String NAME = "EnableDebugger2";

    @Reserved
    @SWFType(BasicType.UI16)
    public int reserved;

    /**
     * MD5 hash of password
     */
    @Password(type = HashType.MD5CRYPT)
    public String passwordHash;

    /**
     * Constructor
     *
     * @param swf
     */
    public EnableDebugger2Tag(SWF swf) {
        super(swf, ID, NAME, null);
        setPassword("");
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public EnableDebugger2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        if (sis.available() > 0) {
            reserved = sis.readUI16("reserved");
        } else {
            reserved = 0;
        }
        if (sis.available() > 0) {
            passwordHash = sis.readString("passwordHash");
        } else {
            passwordHash = null;
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
        sos.writeUI16(reserved);
        if (passwordHash != null) {
            sos.writeString(passwordHash);
        }
    }

    @Override
    public void setPassword(String password) {
        this.passwordHash = MD5Crypt.crypt(password, 2);
    }

    @Override
    public boolean hasPassword(String password) {
        return this.passwordHash.equals(MD5Crypt.crypt(password, 2));
    }
}
