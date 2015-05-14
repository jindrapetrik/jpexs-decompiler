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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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
public class DefineBitsJPEG4Tag extends ImageTag implements AloneTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI16)
    public int deblockParam;

    @SWFType(BasicType.UI8)
    public ByteArrayRange imageData;

    @SWFType(BasicType.UI8)
    public ByteArrayRange bitmapAlphaData;

    public static final int ID = 90;

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterID = characterId;
    }

    @Override
    public String getImageFormat() {
        String fmt = ImageTag.getImageFormat(imageData);
        if (fmt.equals("jpg")) {
            fmt = "png"; //transparency
        }
        return fmt;
    }

    @Override
    public void setImage(byte[] data) {
        imageData = new ByteArrayRange(data);
        if (ImageTag.getImageFormat(data).equals("jpg")) {
            SerializableImage image = getImage();
            byte[] ba = new byte[image.getWidth() * image.getHeight()];
            for (int i = 0; i < ba.length; i++) {
                ba[i] = (byte) 255;
            }
            bitmapAlphaData = new ByteArrayRange(ba);
        } else {
            bitmapAlphaData = ByteArrayRange.EMPTY;
        }
        clearCache();
        setModified(true);
    }

    @Override
    public InputStream getImageData() {
        return new ByteArrayInputStream(imageData.getArray(), imageData.getPos(), imageData.getLength());
    }

    @Override
    public SerializableImage getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }
        try {
            BufferedImage image = ImageHelper.read(getImageData());
            if (image == null) {
                Logger.getLogger(DefineBitsJPEG4Tag.class.getName()).log(Level.SEVERE, "Failed to load image");
                return null;
            }

            SerializableImage img = new SerializableImage(image);
            if (bitmapAlphaData.getLength() == 0) {
                cachedImage = img;
                return img;
            }

            int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                int a = bitmapAlphaData.get(i) & 0xff;
                pixels[i] = multiplyAlpha((pixels[i] & 0xffffff) | (a << 24));
            }

            cachedImage = img;
            return img;
        } catch (IOException ex) {
            Logger.getLogger(DefineBitsJPEG4Tag.class.getName()).log(Level.SEVERE, "Failed to get image", ex);
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
            sos.writeUI32(imageData.getLength());
            sos.writeUI16(deblockParam);
            sos.write(imageData);
            sos.write(bitmapAlphaData);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsJPEG4Tag(SWF swf) {
        super(swf, ID, "DefineBitsJPEG4", null);
        characterID = swf.getNextCharacterId();
        imageData = ByteArrayRange.EMPTY;
        bitmapAlphaData = ByteArrayRange.EMPTY;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineBitsJPEG4Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineBitsJPEG4", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        long alphaDataOffset = sis.readUI32("alphaDataOffset");
        deblockParam = sis.readUI16("deblockParam");
        imageData = sis.readByteRangeEx(alphaDataOffset, "imageData");
        bitmapAlphaData = sis.readByteRangeEx(sis.available(), "bitmapAlphaData");
    }
}
