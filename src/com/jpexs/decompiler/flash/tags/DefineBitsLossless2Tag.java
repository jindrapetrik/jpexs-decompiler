/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.AloneTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.ALPHABITMAPDATA;
import com.jpexs.decompiler.flash.types.ALPHACOLORMAPDATA;
import com.jpexs.decompiler.flash.types.ARGB;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;
import javax.imageio.ImageIO;

public class DefineBitsLossless2Tag extends ImageTag implements AloneTag {

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

    public byte[] zlibBitmapData; //TODO: Parse ALPHACOLORMAPDATA,ALPHABITMAPDATA

    public static final int FORMAT_8BIT_COLORMAPPED = 3;
    public static final int FORMAT_32BIT_ARGB = 5;

    public static final int ID = 36;

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public void setImage(byte[] data) throws IOException {
        SerializableImage image = new SerializableImage(ImageIO.read(new ByteArrayInputStream(data)));
        ALPHABITMAPDATA bitmapData = new ALPHABITMAPDATA();
        bitmapFormat = DefineBitsLosslessTag.FORMAT_24BIT_RGB;
        bitmapWidth = image.getWidth();
        bitmapHeight = image.getHeight();
        bitmapData.bitmapPixelData = new ARGB[bitmapWidth * bitmapHeight];
        int pos = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            for (int x = 0; x < bitmapWidth; x++) {
                int argb = image.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = (argb) & 0xff;

                r = r * a / 255;
                g = g * a / 255;
                b = b * a / 255;

                bitmapData.bitmapPixelData[pos] = new ARGB();
                bitmapData.bitmapPixelData[pos].alpha = a;
                bitmapData.bitmapPixelData[pos].red = r;
                bitmapData.bitmapPixelData[pos].green = g;
                bitmapData.bitmapPixelData[pos].blue = b;
                pos++;
            }
        }
        ByteArrayOutputStream bitmapDataOS = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(bitmapDataOS, getVersion());
        sos.writeALPHABITMAPDATA(bitmapData, bitmapFormat, bitmapWidth, bitmapHeight);
        ByteArrayOutputStream zlibOS = new ByteArrayOutputStream();
        SWFOutputStream sos2 = new SWFOutputStream(zlibOS, getVersion());
        sos2.writeBytesZlib(bitmapDataOS.toByteArray());
        zlibBitmapData = zlibOS.toByteArray();
        decompressed = false;
        setModified(true);
    }

    public DefineBitsLossless2Tag(SWF swf, byte[] headerData, byte[] data, long pos) throws IOException {
        super(swf, ID, "DefineBitsLossless2", headerData, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        characterID = sis.readUI16();
        bitmapFormat = sis.readUI8();
        bitmapWidth = sis.readUI16();
        bitmapHeight = sis.readUI16();
        if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
            bitmapColorTableSize = sis.readUI8();
        }
        zlibBitmapData = sis.readBytesEx(sis.available());
    }
    private ALPHACOLORMAPDATA colorMapData;
    private ALPHABITMAPDATA bitmapData;
    private boolean decompressed = false;

    public ALPHACOLORMAPDATA getColorMapData() {
        if (!decompressed) {
            uncompressData();
        }
        return colorMapData;
    }

    public ALPHABITMAPDATA getBitmapData() {
        if (!decompressed) {
            uncompressData();
        }
        return bitmapData;
    }

    private void uncompressData() {
        try {
            SWFInputStream sis = new SWFInputStream(new InflaterInputStream(new ByteArrayInputStream(zlibBitmapData)), 10);
            if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
                colorMapData = sis.readALPHACOLORMAPDATA(bitmapColorTableSize, bitmapWidth, bitmapHeight);
            }
            if (bitmapFormat == FORMAT_32BIT_ARGB) {
                bitmapData = sis.readALPHABITMAPDATA(bitmapFormat, bitmapWidth, bitmapHeight);
            }
        } catch (IOException ex) {
        }
        decompressed = true;
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
        }
        return baos.toByteArray();
    }

    @Override
    public String getImageFormat() {
        return "png";
    }

    @Override
    public InputStream getImageData() {
        return null;
    }

    @Override
    public SerializableImage getImage() {
        SerializableImage bi = new SerializableImage(bitmapWidth, bitmapHeight, SerializableImage.TYPE_INT_ARGB);
        ALPHACOLORMAPDATA colorMapData = null;
        ALPHABITMAPDATA bitmapData = null;
        if (bitmapFormat == DefineBitsLossless2Tag.FORMAT_8BIT_COLORMAPPED) {
            colorMapData = getColorMapData();
        }
        if (bitmapFormat == DefineBitsLossless2Tag.FORMAT_32BIT_ARGB) {
            bitmapData = getBitmapData();
        }
        int pos32aligned = 0;
        int pos = 0;
        for (int y = 0; y < bitmapHeight; y++) {
            for (int x = 0; x < bitmapWidth; x++) {
                Color c = null;
                if ((bitmapFormat == DefineBitsLossless2Tag.FORMAT_8BIT_COLORMAPPED)) {
                    c = (multiplyAlpha(colorMapData.colorTableRGB[colorMapData.colorMapPixelData[pos32aligned] & 0xff].toColor()));
                }
                if ((bitmapFormat == DefineBitsLossless2Tag.FORMAT_32BIT_ARGB)) {
                    c = (multiplyAlpha(bitmapData.bitmapPixelData[pos].toColor()));
                }
                bi.setRGB(x, y, c.getRGB());
                pos32aligned++;
                pos++;
            }
            while ((pos32aligned % 4 != 0)) {
                pos32aligned++;
            }
        }
        return bi;
    }
}
