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
import com.jpexs.decompiler.flash.tags.base.PasswordTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.HashType;
import com.jpexs.decompiler.flash.types.annotations.Optional;
import com.jpexs.decompiler.flash.types.annotations.Password;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enable flash profiling information
 *
 * @author JPEXS
 */
@SWFVersion(from = 17)
public class EnableTelemetryTag extends Tag implements PasswordTag {

    public static final int ID = 93;

    public static final String NAME = "EnableTelemetry";

    @SWFType(value = BasicType.UB, count = 16)
    @Reserved
    public int reserved;

    @Optional
    @Password(type = HashType.SHA256)
    public String passwordHash;

    /**
     * Constructor
     *
     * @param swf
     */
    public EnableTelemetryTag(SWF swf) {
        super(swf, ID, NAME, null);
        passwordHash = sha256("");
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

            passwordHash = Helper.byteArrayToHex(sis.readBytesEx(32, "passwordHash"));
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
            sos.write(Helper.hexToByteArray(passwordHash));
        }
    }

    private String sha256(String password) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes(Utf8Helper.charset));
            return Helper.byteArrayToHex(md.digest());
        } catch (NoSuchAlgorithmException ex) {

        }
        return null;
    }

    @Override
    public void setPassword(String password) {
        this.passwordHash = sha256(password);
    }

    @Override
    public boolean hasPassword(String password) {
        return sha256(password).equals(this.passwordHash);
    }
}
