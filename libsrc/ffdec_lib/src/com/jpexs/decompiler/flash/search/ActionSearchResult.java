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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.search;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class ActionSearchResult implements ScriptSearchResult {

    private final ASMSource src;

    private final boolean pcode;

    private final String path;

    private static final int SERIAL_VERSION_MAJOR = 1;
    private static final int SERIAL_VERSION_MINOR = 0;

    public ActionSearchResult(SWF swf, InputStream is) throws IOException, ScriptNotFoundException {
        Map<String, ASMSource> asms = swf.getASMs(false);
        ObjectInputStream ois = new ObjectInputStream(is);
        int versionMajor = ois.read();
        ois.read(); //minor
        if (versionMajor != SERIAL_VERSION_MAJOR) {
            throw new IOException("Unknown search result version: " + versionMajor);
        }
        path = ois.readUTF();
        if (asms.containsKey(path)) {
            src = asms.get(path);
        } else {
            throw new ScriptNotFoundException();
        }
        pcode = ois.readBoolean();
    }

    public void save(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.write(SERIAL_VERSION_MAJOR);
        oos.write(SERIAL_VERSION_MINOR);
        oos.writeUTF(path);
        oos.writeBoolean(pcode);
        oos.flush();
        oos.close();
    }

    public ActionSearchResult(ASMSource src, boolean pcode, String path) {
        this.src = src;
        this.pcode = pcode;
        this.path = path;
    }

    public ASMSource getSrc() {
        return src;
    }

    public boolean isPcode() {
        return pcode;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public SWF getSWF() {
        return src.getSwf();
    }
}
