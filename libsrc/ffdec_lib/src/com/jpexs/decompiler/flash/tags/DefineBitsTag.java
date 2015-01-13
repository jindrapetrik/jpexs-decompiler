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
 * License along with this library. */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefineBitsTag extends ImageTag implements TagChangedListener {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI8)
    public ByteArrayRange jpegData;

    public static final int ID = 6;

    @Override
    public void setImage(byte[] data) {
        throw new UnsupportedOperationException("Set image is not supported for DefineBits");
    }

    @Override
    public boolean importSupported() {
        // importing a new image will replace the current DefineBitsTag with a new DefineBitsJPEG2Tag
        return true;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsTag(SWF swf) {
        super(swf, ID, "DefineBits", null);
        characterID = swf.getNextCharacterId();
        jpegData = ByteArrayRange.EMPTY;
    }

    public DefineBitsTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineBits", data);
        characterID = sis.readUI16("characterID");
        jpegData = sis.readByteRangeEx(sis.available(), "jpegData");
    }

    @Override
    public InputStream getImageData() {
        return null;
    }

    @Override
    public SerializableImage getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }
        if (swf.getJtt() != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] jttdata = swf.getJtt().jpegData;
                if (jttdata.length != 0) {
                    boolean jttError = SWF.hasErrorHeader(jttdata);
                    baos.write(jttdata, jttError ? 4 : 0, jttdata.length - (jttError ? 6 : 2));
                    baos.write(jpegData.getArray(), jpegData.getPos() + (SWF.hasErrorHeader(jpegData) ? 6 : 2), jpegData.getLength() - (jttError ? 6 : 2));
                } else {
                    baos.write(jpegData.getArray(), jpegData.getPos(), jpegData.getLength());
                }
                SerializableImage ret = new SerializableImage(ImageHelper.read(new ByteArrayInputStream(baos.toByteArray())));
                cachedImage = ret;
                return ret;
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterID);
            sos.write(jpegData);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public String getImageFormat() {
        return "jpg";
    }

    @Override
    public void handleEvent(Tag tag) {
        clearCache();
    }
}
