/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public class DefineBitsJPEG2Tag extends ImageTag implements AloneTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI8)
    public ByteArrayRange imageData;

    public static final int ID = 21;

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public String getImageFormat() {
        return ImageTag.getImageFormat(imageData);
    }

    @Override
    public InputStream getImageData() {
        if (SWF.hasErrorHeader(imageData)) {
            return new ByteArrayInputStream(imageData.getArray(), imageData.getPos() + 4, imageData.getLength() - 4);
        }
        return new ByteArrayInputStream(imageData.getArray(), imageData.getPos(), imageData.getLength());
    }

    @Override
    public SerializableImage getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }
        try {
            BufferedImage image = ImageIO.read(getImageData());
            SerializableImage ret = image == null ? null : new SerializableImage(image);
            cachedImage = ret;
            return ret;
        } catch (IOException ex) {
        }
        return null;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsJPEG2Tag(SWF swf) {
        super(swf, ID, "DefineBitsJPEG2", null);
        characterID = swf.getNextCharacterId();
        imageData = ByteArrayRange.EMPTY;
    }

    public DefineBitsJPEG2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineBitsJPEG2", data);
        characterID = sis.readUI16("characterID");
        imageData = sis.readByteRangeEx(sis.available(), "imageData");
    }

    public DefineBitsJPEG2Tag(SWF swf, ByteArrayRange data, int characterID, byte[] imageData) throws IOException {
        super(swf, ID, "DefineBitsJPEG2", data);
        this.characterID = characterID;
        this.imageData = new ByteArrayRange(imageData);
    }

    @Override
    public void setImage(byte[] data) {
        imageData = new ByteArrayRange(data);
        clearCache();
        setModified(true);
    }

    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterID);
            sos.write(imageData);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }
}
