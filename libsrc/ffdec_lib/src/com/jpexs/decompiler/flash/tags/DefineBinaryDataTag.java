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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.packers.HarmanAirPacker;
import com.jpexs.decompiler.flash.packers.HarmanAirPackerWithKey;
import com.jpexs.decompiler.flash.packers.MochiCryptPacker16Bit;
import com.jpexs.decompiler.flash.packers.MochiCryptPacker32Bit;
import com.jpexs.decompiler.flash.packers.Packer;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PackedBinaryData;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * DefineBinaryData tag - Contains binary data.
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class DefineBinaryDataTag extends CharacterTag implements BinaryDataInterface {

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

    @Internal
    public Packer usedPacker;
    
    @Internal
    public String packerKey;

    @Internal
    private PackedBinaryData sub;

    private static final Packer[] PACKERS = {
        new MochiCryptPacker16Bit(),
        new MochiCryptPacker32Bit(),
        new HarmanAirPacker(),
        new HarmanAirPackerWithKey()
    };

    public static Packer[] getAvailablePackers() {
        return PACKERS;
    }

    /**
     * Constructor
     *
     * @param swf SWF
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

    public PackedBinaryData getSub() {
        return sub;
    }

    @Override
    public boolean unpack(Packer packer, String key) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!packer.decrypt(new ByteArrayInputStream(binaryData.getRangeData()), baos, key)) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
        sub = new PackedBinaryData(swf, this, new ByteArrayRange(baos.toByteArray()));
        usedPacker = packer;
        packerKey = key;
        return true;
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        tag = sis.readUI16("tag");
        reserved = sis.readUI32("reserved");
        binaryData = sis.readByteRangeEx(sis.available(), "binaryData");
    }

    public void loadEmbeddedSwf() {
        String path = getSwf().getShortPathTitle() + "/DefineBinaryData (" + getCharacterId() + ")";
        SwfSpecificCustomConfiguration conf = Configuration.getSwfSpecificCustomConfiguration(path);
        String charset = conf == null ? "WINDOWS-1252" : conf.getCustomData(CustomConfigurationKeys.KEY_CHARSET, Charset.defaultCharset().name());

        try {
            InputStream is = new ByteArrayInputStream(binaryData.getArray(), binaryData.getPos(), binaryData.getLength());
            detectPacker();
            String packerAdd = "";
            BinaryDataInterface binaryData = this;
            if (usedPacker != null) {
                unpack(usedPacker, packerKey);
                if (sub != null) {
                    is = new ByteArrayInputStream(sub.getDataBytes().getRangeData());
                    binaryData = sub;
                }
            }

            SWF bswf = new SWF(is, null, "(SWF Data)", Configuration.parallelSpeedUp.get(), charset);
            binaryData.setInnerSwf(bswf);
            bswf.binaryData = binaryData;
        } catch (IOException | InterruptedException ex) {
            // ignore
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
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

    @Override
    public void detectPacker() {
        for (Packer packer : PACKERS) {
            if (packer.suitableForBinaryData(this) == Boolean.TRUE) {
                usedPacker = packer;
                break;
            }
        }
    }

    @Override
    public boolean isSwfData() {
        try {
            if (binaryData.getLength() > 8) {
                String signature = new String(binaryData.getRangeData(0, 3), Utf8Helper.charset);
                if (SWF.swfSignatures.contains(signature)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            //ignored
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

    @Override
    public Packer getUsedPacker() {
        return usedPacker;
    }

    @Override
    public void setDataBytes(ByteArrayRange data) {
        this.binaryData = data;
    }

    @Override
    public ByteArrayRange getDataBytes() {
        return binaryData;
    }

    @Override
    public boolean pack() {
        if (sub == null) {
            return false;
        }
        sub.pack();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!usedPacker.encrypt(new ByteArrayInputStream(sub.getDataBytes().getRangeData()), baos, packerKey)) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
        setDataBytes(new ByteArrayRange(baos.toByteArray()));
        return true;
    }

    @Override
    public void setInnerSwf(SWF swf) {
        this.innerSwf = swf;
    }

    @Override
    public SWF getInnerSwf() {
        return this.innerSwf;
    }

    @Override
    public String getPathIdentifier() {
        return "DefineBinaryData (" + getCharacterId() + ")";
    }

    @Override
    public String getStoragesPathIdentifier() {
        return "binaryData[" + getCharacterId() + "]";
    }

    @Override
    public BinaryDataInterface getTopLevelBinaryData() {
        return this;
    }

    @Override
    public void setModified(boolean value) {
        super.setModified(value);
        if (!value) {
            if (sub != null) {
                sub.setModified(false);
            }
        }
    }

    @Override
    public String getClassExportFileName(String className) {
        return className;
    }
    
    @Override
    public String getPackerKey() {
        return packerKey;
    }
}
