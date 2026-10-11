/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.profiles.castlecrashers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Castle Crashers Steam PIXL image (little-endian header, premultiplied BGRA).
 *
 * @author JPEXS
 */
public class PixlImage extends ImageTag {

    public static final int ID = 148;
    
    public static final int ID_INTERNAL = 67;

    public static final String NAME = "PixlImage";

    public static final long FORMAT_ARGB = 1;

    @SWFType(BasicType.UI16)
    public int reserved;

    public RECT bounds = new RECT();

    public ByteArrayRange padding = ByteArrayRange.EMPTY;

    public ByteArrayRange headerBeforeDimensions = ByteArrayRange.EMPTY;

    @SWFType(BasicType.UI32)
    public int width;

    @SWFType(BasicType.UI32)
    public int height;

    @SWFType(BasicType.UI32)
    public long formatID;

    public ByteArrayRange bitmapInfo = ByteArrayRange.EMPTY;

    @SWFType(BasicType.UI32)
    public long imageDataOffset;

    public ByteArrayRange textureInfo = ByteArrayRange.EMPTY;

    @SWFType(BasicType.UI32)
    public long texturePointer;

    public ByteArrayRange imageInfo = ByteArrayRange.EMPTY;

    @SWFType(BasicType.UI32)
    public long imageIdentifier;

    public ByteArrayRange reservedData = ByteArrayRange.EMPTY;

    public ByteArrayRange imageData = ByteArrayRange.EMPTY;

    public ByteArrayRange footer = ByteArrayRange.EMPTY;

    public PixlImage(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public PixlImage(int id, SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), id, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        int payloadLength = sis.available();
        ByteArrayRange payload = data.getSubRange(data.getLength() - payloadLength, payloadLength);
        if (payloadLength < 4) {
            throw new IOException("Truncated PIXL data");
        }
        long relocationCount = readUI32(payload, payloadLength - 4);
        if (relocationCount > 8) {
            throw new IOException("Invalid PIXL relocation count");
        }
        int footerLength = (int) (relocationCount * 4 + 4);
        int headerOffset = -1;
        for (int i = 20; i <= payload.getLength() - 96 - footerLength; i++) {
            if (payload.get(i) == 'L' && payload.get(i + 1) == 'X'
                    && payload.get(i + 2) == 'I' && payload.get(i + 3) == 'P') {
                headerOffset = i;
                break;
            }
        }
        if (headerOffset < 0) {
            throw new IOException("Missing little-endian PIXL header");
        }

        characterID = sis.readUI16("characterID");
        reserved = sis.readUI16("reserved");
        bounds = new RECT((int) sis.readSI32("Xmin"), (int) sis.readSI32("Xmax"),
                (int) sis.readSI32("Ymin"), (int) sis.readSI32("Ymax"));
        padding = sis.readByteRangeEx(headerOffset - 20, "padding");
        headerBeforeDimensions = sis.readByteRangeEx(16, "headerBeforeDimensions");
        width = (int) sis.readUI32("width");
        height = (int) sis.readUI32("height");
        formatID = sis.readUI32("formatID");
        bitmapInfo = sis.readByteRangeEx(8, "bitmapInfo");
        imageDataOffset = sis.readUI32("imageDataOffset");
        textureInfo = sis.readByteRangeEx(8, "textureInfo");
        // This slot is relocated on load, then replaced by a texture handle.
        // It is not a pixel byte count.
        texturePointer = sis.readUI32("texturePointer");
        imageInfo = sis.readByteRangeEx(28, "imageInfo");
        imageIdentifier = sis.readUI32("imageIdentifier");
        reservedData = sis.readByteRangeEx(12, "reservedData");
        imageData = sis.readByteRangeEx(sis.available() - footerLength, "imageData");
        footer = sis.readByteRangeEx(footerLength, "footer");
        clearCache();
    }

    private static long readUI32(ByteArrayRange data, int offset) {
        return (data.get(offset) & 0xffL) | ((data.get(offset + 1) & 0xffL) << 8)
                | ((data.get(offset + 2) & 0xffL) << 16) | ((data.get(offset + 3) & 0xffL) << 24);
    }

    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterID);
        sos.writeUI16(reserved);
        sos.writeSI32(bounds.Xmin);
        sos.writeSI32(bounds.Xmax);
        sos.writeSI32(bounds.Ymin);
        sos.writeSI32(bounds.Ymax);
        sos.write(padding);
        sos.write(headerBeforeDimensions);
        sos.writeUI32(width);
        sos.writeUI32(height);
        sos.writeUI32(formatID);
        sos.write(bitmapInfo);
        sos.writeUI32(imageDataOffset);
        sos.write(textureInfo);
        sos.writeUI32(texturePointer);
        sos.write(imageInfo);
        sos.writeUI32(imageIdentifier);
        sos.write(reservedData);
        sos.write(imageData);
        sos.write(footer);
    }

    @Override
    protected SerializableImage getImage() {
        long pixelCount = (long) width * height;
        long pixelOffset = imageDataOffset - 96;
        if (formatID != FORMAT_ARGB || width <= 0 || height <= 0
                || pixelOffset < 0 || pixelOffset > imageData.getLength()
                || pixelCount > (imageData.getLength() - pixelOffset) / 4) {
            SerializableImage failed = new SerializableImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            failed.fillTransparent();
            return failed;
        }

        int offset = (int) pixelOffset;

        int[] pixels = new int[(int) pixelCount];
        for (int i = 0; i < pixels.length; i++) {
            int b = imageData.get(offset++) & 0xff;
            int g = imageData.get(offset++) & 0xff;
            int r = imageData.get(offset++) & 0xff;
            int a = imageData.get(offset++) & 0xff;
            if (a != 0) {
                r = Math.min(255, (r * 255 + a / 2) / a);
                g = Math.min(255, (g * 255 + a / 2) / a);
                b = Math.min(255, (b * 255 + a / 2) / a);
                pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.getRaster().setDataElements(0, 0, width, height, pixels);
        return new SerializableImage(image);
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(width, height);
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return new RECT(bounds);
    }

    @Override
    public SHAPEWITHSTYLE getShape(RECT rect, boolean fill, int shapeNum) {
        SHAPEWITHSTYLE shape = super.getShape(rect, fill, shapeNum);
        // PIXL can be placed directly, with bounds independent of bitmap size.
        if (width > 0 && height > 0) {
            shape.fillStyles.fillStyles[0].bitmapMatrix.scaleX = (float) rect.getWidth() / width;
            shape.fillStyles.fillStyles[0].bitmapMatrix.scaleY = (float) rect.getHeight() / height;
        }
        return shape;
    }

    @Override
    public InputStream getOriginalImageData() {
        return null;
    }

    @Override
    public boolean importSupported() {
        return false;
    }

    @Override
    public void setImage(byte[] data) throws IOException {
        throw new IOException("PIXL image import is not supported");
    }

    @Override
    public ImageFormat getImageFormat() {
        return ImageFormat.PNG;
    }

    @Override
    public ImageFormat getOriginalImageFormat() {
        return ImageFormat.PNG;
    }
}
