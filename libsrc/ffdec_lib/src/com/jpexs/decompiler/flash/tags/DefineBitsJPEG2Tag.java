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
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class DefineBitsJPEG2Tag extends ImageTag implements AloneTag {

    public static final int ID = 21;

    public static final String NAME = "DefineBitsJPEG2";

    @SWFType(BasicType.UI8)
    public ByteArrayRange imageData;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsJPEG2Tag(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
        imageData = ByteArrayRange.EMPTY;
        forceWriteAsLong = true;
    }

    public DefineBitsJPEG2Tag(SWF swf, ByteArrayRange data, int characterID, byte[] imageData) throws IOException {
        super(swf, ID, NAME, data);
        this.characterID = characterID;
        this.imageData = new ByteArrayRange(imageData);
    }

    public DefineBitsJPEG2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        imageData = sis.readByteRangeEx(sis.available(), "imageData");
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

    @Override
    public ImageFormat getImageFormat() {
        return ImageTag.getImageFormat(imageData);
    }

    @Override
    public InputStream getImageData() {
        int errorLength = hasErrorHeader(imageData) ? 4 : 0;
        return new ByteArrayInputStream(imageData.getArray(), imageData.getPos() + errorLength, imageData.getLength() - errorLength);
    }

    @Override
    public SerializableImage getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }
        try {
            BufferedImage image = ImageHelper.read(getImageData());
            if (image == null) {
                Logger.getLogger(DefineBitsJPEG2Tag.class.getName()).log(Level.SEVERE, "Failed to load image");
                return null;
            }

            SerializableImage ret = new SerializableImage(image);
            cachedImage = ret;
            return ret;
        } catch (IOException ex) {
            Logger.getLogger(DefineBitsJPEG2Tag.class.getName()).log(Level.SEVERE, "Failed to get image", ex);
        }
        return null;
    }

    @Override
    public void setImage(byte[] data) {
        imageData = new ByteArrayRange(data);
        clearCache();
        setModified(true);
    }
}
