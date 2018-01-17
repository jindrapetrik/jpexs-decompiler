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
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BITMAPDATA;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.COLORMAPDATA;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 2)
public class DefineBitsLosslessTag extends ImageTag implements AloneTag {

    public static final int ID = 20;

    public static final String NAME = "DefineBitsLossless";

    @SWFType(BasicType.UI8)
    @EnumValue(value = FORMAT_8BIT_COLORMAPPED, text = "8-bit colormapped")
    @EnumValue(value = FORMAT_15BIT_RGB, text = "15-bit RGB")
    @EnumValue(value = FORMAT_24BIT_RGB, text = "24-bit RGB")
    public int bitmapFormat;

    @SWFType(BasicType.UI16)
    public int bitmapWidth;

    @SWFType(BasicType.UI16)
    public int bitmapHeight;

    @SWFType(BasicType.UI8)
    @Conditional(value = "bitmapFormat", options = {FORMAT_8BIT_COLORMAPPED})
    public int bitmapColorTableSize;

    public ByteArrayRange zlibBitmapData;

    public static final int FORMAT_8BIT_COLORMAPPED = 3;

    public static final int FORMAT_15BIT_RGB = 4;

    public static final int FORMAT_24BIT_RGB = 5;

    @HideInRawEdit
    private COLORMAPDATA colorMapData;

    @HideInRawEdit
    private BITMAPDATA bitmapData;

    @Internal
    private boolean decompressed = false;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsLosslessTag(SWF swf) {
        this(swf, null, swf.getNextCharacterId());
    }

    public DefineBitsLosslessTag(SWF swf, ByteArrayRange data, int characterID) {
        super(swf, ID, NAME, data);
        this.characterID = characterID;
        bitmapFormat = DefineBitsLosslessTag.FORMAT_24BIT_RGB;
        bitmapWidth = 1;
        bitmapHeight = 1;
        zlibBitmapData = new ByteArrayRange(createEmptyImage());
        forceWriteAsLong = true;
    }

    public DefineBitsLosslessTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
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

        zlibBitmapData = sis.readByteRangeEx(sis.available(), "zlibBitmapData", DumpInfoSpecialType.ZLIB_DATA, null);
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
        sos.writeUI8(bitmapFormat);
        sos.writeUI16(bitmapWidth);
        sos.writeUI16(bitmapHeight);
        if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
            sos.writeUI8(bitmapColorTableSize);
        }
        sos.write(zlibBitmapData);
    }

    private byte[] createEmptyImage() {
        try {
            BITMAPDATA bitmapData = new BITMAPDATA();
            bitmapData.bitmapPixelDataPix24 = new int[]{0xff000000};
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
        SerializableImage image = new SerializableImage(ImageHelper.read(data));
        int width = image.getWidth();
        int height = image.getHeight();
        bitmapData = new BITMAPDATA();
        bitmapData.bitmapPixelDataPix24 = new int[width * height];
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int pos = 0; pos < pixels.length; pos++) {
            // set the reserved bits to 0xff, because:
            // documentation says 0, but image is sometimes broken with 0, so there is 0xff, which works (maybe alpha?)
            int argb = pixels[pos] | 0xff000000;
            bitmapData.bitmapPixelDataPix24[pos] = argb;
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
            byte[] uncompressedData = SWFInputStream.uncompressByteArray(zlibBitmapData.getArray(), zlibBitmapData.getPos(), zlibBitmapData.getLength());
            SWFInputStream sis = new SWFInputStream(swf, uncompressedData);
            if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
                colorMapData = sis.readCOLORMAPDATA(bitmapColorTableSize, bitmapWidth, bitmapHeight, "colorMapData");
            } else if ((bitmapFormat == FORMAT_15BIT_RGB) || (bitmapFormat == FORMAT_24BIT_RGB)) {
                bitmapData = sis.readBITMAPDATA(bitmapFormat, bitmapWidth, bitmapHeight, "bitmapData");
            }
        } catch (IOException ex) {
        }
        decompressed = true;
    }

    @Override
    public ImageFormat getImageFormat() {
        return ImageFormat.PNG;
    }

    @Override
    public ImageFormat getOriginalImageFormat() {
        return ImageFormat.PNG;
    }

    @Override
    public InputStream getOriginalImageData() {
        return null;
    }

    @Override
    protected SerializableImage getImage() {
        int[] pixels = new int[bitmapWidth * bitmapHeight];
        if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
            COLORMAPDATA colorMapData = getColorMapData();
            int pos32aligned = 0;
            int pos = 0;
            for (int y = 0; y < bitmapHeight; y++) {
                for (int x = 0; x < bitmapWidth; x++) {
                    int c = 0;
                    int colorTableIndex = colorMapData.colorMapPixelData[pos32aligned] & 0xff;
                    if (colorTableIndex < colorMapData.colorTableRGB.length) {
                        c = colorMapData.colorTableRGB[colorTableIndex];
                    }

                    pixels[pos++] = c;
                    pos32aligned++;
                }

                while ((pos32aligned % 4 != 0)) {
                    pos32aligned++;
                }
            }
        } else if ((bitmapFormat == FORMAT_15BIT_RGB) || (bitmapFormat == FORMAT_24BIT_RGB)) {
            BITMAPDATA bitmapData = getBitmapData();
            int pos = 0;
            int[] bitmapPixelData = null;
            if (bitmapFormat == FORMAT_15BIT_RGB) {
                bitmapPixelData = bitmapData.bitmapPixelDataPix15;
            } else if (bitmapFormat == FORMAT_24BIT_RGB) {
                bitmapPixelData = bitmapData.bitmapPixelDataPix24;
            }

            for (int y = 0; y < bitmapHeight; y++) {
                for (int x = 0; x < bitmapWidth; x++) {
                    int c = bitmapPixelData[pos] | 0xff000000;
                    pixels[pos++] = c;
                }
            }
        }

        SerializableImage bi = new SerializableImage(bitmapWidth, bitmapHeight, SerializableImage.TYPE_INT_RGB, pixels);
        return bi;
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(bitmapWidth, bitmapHeight);
    }
}
