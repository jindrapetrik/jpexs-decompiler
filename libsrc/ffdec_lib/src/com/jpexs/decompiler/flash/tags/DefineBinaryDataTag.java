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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class DefineBinaryDataTag extends CharacterTag {

    public static final int ID = 87;

    public static final String NAME = "DefineBinaryData";

    @SWFType(BasicType.UI16)
    public int tag;

    public ByteArrayRange binaryData;

    @Reserved
    @SWFType(BasicType.UI32)
    public long reserved;

    @Internal
    public SWF innerSwf;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBinaryDataTag(SWF swf) {
        super(swf, ID, NAME, null);
        tag = swf.getNextCharacterId();
        binaryData = ByteArrayRange.EMPTY;
    }

    public DefineBinaryDataTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        tag = sis.readUI16("tag");
        reserved = sis.readUI32("reserved");
        binaryData = sis.readByteRangeEx(sis.available(), "binaryData");

        if (Configuration.autoLoadEmbeddedSwfs.get()) {
            try {
                InputStream is = new ByteArrayInputStream(binaryData.getArray(), binaryData.getPos(), binaryData.getLength());
                SWF bswf = new SWF(is, null, "(SWF Data)", Configuration.parallelSpeedUp.get());
                innerSwf = bswf;
                bswf.binaryData = this;
            } catch (IOException | InterruptedException ex) {
                // ignore
            }
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
        sos.writeUI16(tag);
        sos.writeUI32(reserved);
        sos.write(binaryData);
    }

    @Override
    public int getCharacterId() {
        return tag;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.tag = characterId;
    }

    public boolean isSwfData() {
        try {
            if (binaryData.getLength() > 8) {
                String signature = new String(binaryData.getRangeData(0, 3), Utf8Helper.charset);
                if (SWF.swfSignatures.contains(signature)) {
                    return true;
                }
            }
        } catch (Exception ex) {
        }

        return false;
    }

    @Override
    public boolean isModified() {
        if (super.isModified()) {
            return true;
        }
        if (innerSwf != null) {
            return innerSwf.isModified();
        }
        return false;
    }
}
