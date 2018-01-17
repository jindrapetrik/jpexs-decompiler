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
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 2) //Note: GIF and PNG since version 8
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
        imageData = new ByteArrayRange(createEmptyImage());
        forceWriteAsLong = true;
    }

    public DefineBitsJPEG2Tag(SWF swf, ByteArrayRange data, int characterID, byte[] imageData) throws IOException {
        super(swf, ID, NAME, data);
        this.characterID = characterID;
        this.imageData = new ByteArrayRange(imageData);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineBitsJPEG2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        imageData = sis.readByteRangeEx(sis.available(), "imageData");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterID);
        sos.write(imageData);
    }

    private byte[] createEmptyImage() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
        ByteArrayOutputStream bitmapDataOS = new ByteArrayOutputStream();
        ImageHelper.write(img, ImageFormat.JPEG, bitmapDataOS);
        return bitmapDataOS.toByteArray();
    }

    @Override
    public void setImage(byte[] data) {
        imageData = new ByteArrayRange(data);
        clearCache();
        setModified(true);
    }

    @Override
    public ImageFormat getImageFormat() {
        return getOriginalImageFormat();
    }

    @Override
    public ImageFormat getOriginalImageFormat() {
        return ImageTag.getImageFormat(imageData);
    }

    @Override
    public InputStream getOriginalImageData() {
        int errorLength = hasErrorHeader(imageData) ? 4 : 0;
        return new ByteArrayInputStream(imageData.getArray(), imageData.getPos() + errorLength, imageData.getLength() - errorLength);
    }

    @Override
    protected SerializableImage getImage() {
        try {
            BufferedImage image = ImageHelper.read(getOriginalImageData());
            if (image == null) {
                Logger.getLogger(DefineBitsJPEG2Tag.class.getName()).log(Level.SEVERE, "Failed to load image");
                return null;
            }

            SerializableImage img = new SerializableImage(image);
            return img;
        } catch (IOException ex) {
            Logger.getLogger(DefineBitsJPEG2Tag.class.getName()).log(Level.SEVERE, "Failed to get image", ex);
        }

        return null;
    }

    @Override
    public Dimension getImageDimension() {
        if (cachedImage != null) {
            return new Dimension(cachedImage.getWidth(), cachedImage.getHeight());
        }

        try {
            return ImageHelper.getDimesion(getOriginalImageData());
        } catch (IOException ex) {
            Logger.getLogger(DefineBitsJPEG2Tag.class.getName()).log(Level.SEVERE, "Failed to get image dimension", ex);
        }

        return null;
    }
}
