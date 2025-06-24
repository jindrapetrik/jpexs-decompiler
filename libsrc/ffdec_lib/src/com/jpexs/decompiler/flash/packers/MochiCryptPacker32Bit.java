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
package com.jpexs.decompiler.flash.packers;

import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * MochiCrypt 32bit packer.
 *
 * @author JPEXS
 */
public class MochiCryptPacker32Bit implements Packer {

    @Override
    public Boolean suitableForBinaryData(DefineBinaryDataTag dataTag) {
        /*if (dataTag.getClassNames().contains("mochicrypt.Payload")) {
            return true;
        }*/
        return null;
    }

    @Override
    public boolean decrypt(InputStream is, OutputStream os, String key) throws IOException {
        byte[] payload = Helper.readStream(is);
        if (!handleXor(payload)) {
            return false;
        }
        try {
            Helper.copyStreamEx(new InflaterInputStream(new ByteArrayInputStream(payload)), os);
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    private boolean handleXor(byte[] payload) {
        if (payload.length < 32) {
            return false;
        }
        int[] S = new int[256];
        int i = 0;
        int j;
        int k;
        int n;
        int u;
        int v;

        n = payload.length - 32;
        while (i < 256) {
            S[i] = i;
            i++;
        }
        j = 0;
        i = 0;
        while (i < 256) {
            j = (j + S[i] + (payload[n + (i & 31)] & 0xff)) & 255;
            u = S[i];
            S[i] = S[j];
            S[j] = u;
            i++;
        }

        if (n > 0x20000) {
            n = 0x20000;
        }
        j = 0;
        i = 0;
        k = 0;
        while (k < n) {
            i = (i + 1) & 255;
            u = S[i];
            j = (j + u) & 255;
            v = S[j];
            S[i] = v;
            S[j] = u;
            payload[k] = (byte) ((payload[k] & 0xff) ^ S[u + v & 255]);
            k++;
        }
        return true;
    }

    @Override
    public boolean encrypt(InputStream is, OutputStream os, String key) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream def = new DeflaterOutputStream(baos);
        Helper.copyStreamEx(is, def);
        def.finish();
        byte[] payload = baos.toByteArray();

        if (!handleXor(payload)) {
            return false;
        }

        os.write(payload);

        return true;
    }

    @Override
    public String getName() {
        return "MochiCrypt 32bit";
    }

    @Override
    public Boolean suitableForData(byte[] data) {
        return null;
    }

    @Override
    public String getIdentifier() {
        return "mochicrypt32";
    }

    @Override
    public boolean usesKey() {
        return false;
    }        
}
