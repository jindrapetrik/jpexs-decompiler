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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 *
 *
 * @author JPEXS
 */
public class DefineBitsJPEG4Tag extends ImageTag implements AloneTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI16)
    public int deblockParam;

    @SWFType(BasicType.UI8)
    public byte[] imageData;

    @SWFType(BasicType.UI8)
    public byte[] bitmapAlphaData;

    public static final int ID = 90;

    @Override
    public int getCharacterId() {
        return characterID;
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
        imageData = data;
        if (ImageTag.getImageFormat(data).equals("jpg")) {
            SerializableImage image = getImage();
            byte[] ba = new byte[image.getWidth() * image.getHeight()];
            for (int i = 0; i < ba.length; i++) {
                ba[i] = (byte) 255;
            }
            bitmapAlphaData = ba;
        } else {
            bitmapAlphaData = new byte[0];
        }
        setModified(true);
    }

    @Override
    public InputStream getImageData() {
        return null;
    }

    @Override
    public SerializableImage getImage() {
        try {
            SerializableImage img = new SerializableImage(ImageIO.read(new ByteArrayInputStream(imageData)));
            if (bitmapAlphaData.length == 0) {
                return img;
            }
            SerializableImage img2 = new SerializableImage(img.getWidth(), img.getHeight(), SerializableImage.TYPE_INT_ARGB);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int val = img.getRGB(x, y);
                    int a = bitmapAlphaData[x + y * img.getWidth()] & 0xff;
                    val = (val & 0xffffff) | (a << 24);
                    img2.setRGB(x, y, colorToInt(multiplyAlpha(intToColor(val))));
                }
            }
            return img2;
        } catch (IOException ex) {
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
            sos.writeUI32(imageData.length);
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
     * @param swf
     */
    public DefineBitsJPEG4Tag(SWF swf) {
        super(swf, ID, "DefineBitsJPEG4", null);
        characterID = swf.getNextCharacterId();
        imageData = new byte[0];
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
        characterID = sis.readUI16("characterID");
        long alphaDataOffset = sis.readUI32("alphaDataOffset");
        deblockParam = sis.readUI16("deblockParam");
        imageData = sis.readBytesEx(alphaDataOffset, "imageData");
        bitmapAlphaData = sis.readBytesEx(sis.available(), "bitmapAlphaData");
    }
}
