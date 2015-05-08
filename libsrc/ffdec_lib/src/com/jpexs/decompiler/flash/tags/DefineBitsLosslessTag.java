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
import com.jpexs.decompiler.flash.types.BITMAPDATA;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.COLORMAPDATA;
import com.jpexs.decompiler.flash.types.PIX24;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

public class DefineBitsLosslessTag extends ImageTag implements AloneTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI8)
    public int bitmapFormat;

    @SWFType(BasicType.UI16)
    public int bitmapWidth;

    @SWFType(BasicType.UI16)
    public int bitmapHeight;

    @SWFType(BasicType.UI8)
    @Conditional(value = "bitmapFormat", options = {FORMAT_8BIT_COLORMAPPED})
    public int bitmapColorTableSize;

    public ByteArrayRange zlibBitmapData; //TODO: Parse COLORMAPDATA,BITMAPDATA

    public static final int FORMAT_8BIT_COLORMAPPED = 3;

    public static final int FORMAT_15BIT_RGB = 4;

    public static final int FORMAT_24BIT_RGB = 5;

    @HideInRawEdit
    private COLORMAPDATA colorMapData;

    @HideInRawEdit
    private BITMAPDATA bitmapData;

    @Internal
    private boolean decompressed = false;

    public static final int ID = 20;

    private byte[] createEmptyImage() {
        try {
            BITMAPDATA bitmapData = new BITMAPDATA();
            bitmapData.bitmapPixelDataPix24 = new PIX24[1];
            bitmapData.bitmapPixelDataPix24[0] = new PIX24();
            bitmapData.bitmapPixelDataPix24[0].reserved = 0xff;
            ByteArrayOutputStream bitmapDataOS = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(bitmapDataOS, getVersion());
            sos.writeBITMAPDATA(bitmapData, FORMAT_24BIT_RGB, 1, 1);
            ByteArrayOutputStream zlibOS = new ByteArrayOutputStream();
            SWFOutputStream sos2 = new SWFOutputStream(zlibOS, getVersion());
            sos2.writeBytesZlib(bitmapDataOS.toByteArray());
            return zlibOS.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(DefineBitsLosslessTag.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void setImage(byte[] data) throws IOException {
        SerializableImage image = new SerializableImage(ImageHelper.read(new ByteArrayInputStream(data)));
        int width = image.getWidth();
        int height = image.getHeight();
        bitmapData = new BITMAPDATA();
        bitmapData.bitmapPixelDataPix24 = new PIX24[width * height];
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int pos = 0; pos < pixels.length; pos++) {
            int argb = pixels[pos];
            //int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = (argb) & 0xff;
            bitmapData.bitmapPixelDataPix24[pos] = new PIX24();
            bitmapData.bitmapPixelDataPix24[pos].red = r;
            bitmapData.bitmapPixelDataPix24[pos].green = g;
            bitmapData.bitmapPixelDataPix24[pos].blue = b;
            bitmapData.bitmapPixelDataPix24[pos].reserved = 0xff; //documentation says 0, but image is sometimes broken with 0, so there is 0xff, which works (maybe alpha?)
        }

        int format = FORMAT_24BIT_RGB;
        ByteArrayOutputStream bitmapDataOS = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(bitmapDataOS, getVersion());
        sos.writeBITMAPDATA(bitmapData, format, width, height);
        ByteArrayOutputStream zlibOS = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(zlibOS, getVersion());
        sos2.writeBytesZlib(bitmapDataOS.toByteArray());
        zlibBitmapData = new ByteArrayRange(zlibOS.toByteArray());
        bitmapFormat = format;
        bitmapWidth = width;
        bitmapHeight = height;
        decompressed = false;
        clearCache();
        setModified(true);
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
        SerializableImage bi = new SerializableImage(bitmapWidth, bitmapHeight, SerializableImage.TYPE_INT_RGB);
        COLORMAPDATA colorMapData = null;
        BITMAPDATA bitmapData = null;
        if (bitmapFormat == DefineBitsLosslessTag.FORMAT_8BIT_COLORMAPPED) {
            colorMapData = getColorMapData();
        }
        if ((bitmapFormat == DefineBitsLosslessTag.FORMAT_15BIT_RGB) || (bitmapFormat == DefineBitsLosslessTag.FORMAT_24BIT_RGB)) {
            bitmapData = getBitmapData();
        }
        int pos32aligned = 0;
        int pos = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            for (int x = 0; x < bitmapWidth; x++) {
                int c = 0;
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_8BIT_COLORMAPPED) {
                    int colorTableIndex = colorMapData.colorMapPixelData[pos32aligned] & 0xff;
                    if (colorTableIndex < colorMapData.colorTableRGB.length) {
                        RGB color = colorMapData.colorTableRGB[colorTableIndex];
                        c = color.toInt();
                    }
                }
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_15BIT_RGB) {
                    c = new RGB(bitmapData.bitmapPixelDataPix15[pos].red * 8, bitmapData.bitmapPixelDataPix15[pos].green * 8, bitmapData.bitmapPixelDataPix15[pos].blue * 8).toInt();
                }
                if (bitmapFormat == DefineBitsLosslessTag.FORMAT_24BIT_RGB) {
                    c = new RGB(bitmapData.bitmapPixelDataPix24[pos].red, bitmapData.bitmapPixelDataPix24[pos].green, bitmapData.bitmapPixelDataPix24[pos].blue).toInt();
                }
                bi.setRGB(x, y, c);
                pos32aligned++;
                pos++;
            }
            while ((pos32aligned % 4 != 0)) {
                pos32aligned++;
            }
        }
        cachedImage = bi;
        return bi;
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    public COLORMAPDATA getColorMapData() {
        if (!decompressed) {
            uncompressData();
        }
        return colorMapData;
    }

    public BITMAPDATA getBitmapData() {
        if (!decompressed) {
            uncompressData();
        }
        return bitmapData;
    }

    private void uncompressData() {
        try {
            SWFInputStream sis = new SWFInputStream(swf, Helper.readStream(new InflaterInputStream(new ByteArrayInputStream(zlibBitmapData.getArray(), zlibBitmapData.getPos(), zlibBitmapData.getLength()))));
            if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
                colorMapData = sis.readCOLORMAPDATA(bitmapColorTableSize, bitmapWidth, bitmapHeight, "colorMapData");
            }
            if ((bitmapFormat == FORMAT_15BIT_RGB) || (bitmapFormat == FORMAT_24BIT_RGB)) {
                bitmapData = sis.readBITMAPDATA(bitmapFormat, bitmapWidth, bitmapHeight, "bitmapData");
            }
        } catch (IOException ex) {
        }
        decompressed = true;
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsLosslessTag(SWF swf) {
        super(swf, ID, "DefineBitsLossless", null);
        characterID = swf.getNextCharacterId();
        bitmapFormat = DefineBitsLosslessTag.FORMAT_24BIT_RGB;
        bitmapWidth = 1;
        bitmapHeight = 1;
        zlibBitmapData = new ByteArrayRange(createEmptyImage());
    }

    public DefineBitsLosslessTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineBitsLossless", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        bitmapFormat = sis.readUI8("bitmapFormat");
        bitmapWidth = sis.readUI16("bitmapWidth");
        bitmapHeight = sis.readUI16("bitmapHeight");
        if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
            bitmapColorTableSize = sis.readUI8("bitmapColorTableSize");
        }
        zlibBitmapData = sis.readByteRangeEx(sis.available(), "zlibBitmapData");
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
            sos.writeUI8(bitmapFormat);
            sos.writeUI16(bitmapWidth);
            sos.writeUI16(bitmapHeight);
            if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
                sos.writeUI8(bitmapColorTableSize);
            }
            sos.write(zlibBitmapData);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    @Override
    public String getImageFormat() {
        return "png";
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterID = characterId;
    }
}
