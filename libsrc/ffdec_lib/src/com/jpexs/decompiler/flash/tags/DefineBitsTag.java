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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class DefineBitsTag extends ImageTag implements TagChangedListener {

    public static final int ID = 6;

    public static final String NAME = "DefineBits";

    @SWFType(BasicType.UI8)
    public ByteArrayRange jpegData;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsTag(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
        jpegData = ByteArrayRange.EMPTY;
        forceWriteAsLong = true;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineBitsTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        jpegData = sis.readByteRangeEx(sis.available(), "jpegData");
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
        sos.write(jpegData);
    }

    @Override
    public boolean importSupported() {
        // importing a new image will replace the current DefineBitsTag with a new DefineBitsJPEG2Tag
        return true;
    }

    @Override
    public void setImage(byte[] data) {
        throw new UnsupportedOperationException("Set image is not supported for DefineBits");
    }

    @Override
    public ImageFormat getImageFormat() {
        return ImageFormat.JPEG;
    }

    @Override
    public ImageFormat getOriginalImageFormat() {
        return ImageFormat.JPEG;
    }

    private static List<byte[]> parseJpegChunks(byte[] data) {
        //A little inspired by shum way :-)
        List<byte[]> ret = new ArrayList<>();
        int pos = 0;
        int n = data.length;
        // Finding first marker, and skipping the data before this marker.
        // (FF 00 - code is escaped FF; FF FF ... (FF xx) - fill bytes before marker).
        while (pos < n && ((data[pos] & 0xFF) != 0xFF
                || (pos + 1 < n && ((data[pos + 1] & 0xFF) == 0x00 || (data[pos + 1] & 0xFF) == 0xFF)))) {
            pos++;
        }

        while (pos < n) {
            int start = pos++;
            int code = data[pos++] & 0xFF;

            // Some tags have length field -- using it
            if ((code >= 0xC0 && code <= 0xC7)
                    || (code >= 0xC9 && code <= 0xCF)
                    || (code >= 0xDA && code <= 0xEF)
                    || code == 0xFE) {
                int length = (data[pos] & 0xFF) << 8 + (data[pos + 1] & 0xFF);
                pos += length;
            }

            // Finding next marker.
            while (pos < n && ((data[pos] & 0xFF) != 0xFF
                    || (pos + 1 < n && ((data[pos + 1] & 0xff) == 0x00 || (data[pos + 1] & 0xFF) == 0xFF)))) {
                pos++;
            }

            if (code == 0xD8 || code == 0xD9) {
                // Removing SOI and EOI to avoid wrong EOI-SOI pairs in the middle.
                continue;
            }
            ret.add(Arrays.copyOfRange(data, start, pos));
        }
        return ret;
    }

    @Override
    public InputStream getOriginalImageData() {
        if (swf.getJtt() != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int errorLength = hasErrorHeader(jpegData) ? 4 : 0;

                List<byte[]> jpegChunks = parseJpegChunks(jpegData.getRangeData(errorLength, jpegData.getLength() - errorLength));
                ByteArrayRange jttdata = swf.getJtt().jpegData;
                if (jttdata.getLength() != 0) {
                    int jttErrorLength = hasErrorHeader(jttdata) ? 4 : 0;
                    List<byte[]> chunksJtt = parseJpegChunks(jttdata.getRangeData(jttErrorLength, jttdata.getLength() - jttErrorLength));
                    for (int c = 0; c < jpegChunks.size(); c++) {
                        int chunkType = (jpegChunks.get(c)[1] & 0xFF);
                        if (chunkType >= 0xC0 && chunkType <= 0xCF) {
                            jpegChunks.addAll(c, chunksJtt);
                            break;
                        }
                    }
                }
                jpegChunks.add(0, new byte[]{(byte) 0xFF, (byte) 0xD8}); //SOI to beginning                
                jpegChunks.add(new byte[]{(byte) 0xFF, (byte) 0xD9}); //EOI to the end

                for (byte[] chunk : jpegChunks) {
                    baos.write(chunk);
                }
                return new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException ex) {
                // this should never happen, since IOException comes from OutputStream, but ByteArrayOutputStream should never throw it
                throw new Error(ex);
            }
        }

        return null;
    }

    @Override
    protected SerializableImage getImage() {
        InputStream imageStream = getOriginalImageData();
        if (imageStream != null) {
            try {
                BufferedImage image = ImageHelper.read(imageStream);
                if (image == null) {
                    Logger.getLogger(DefineBitsTag.class.getName()).log(Level.SEVERE, "Failed to load image");
                    return null;
                }

                SerializableImage img = new SerializableImage(image);
                return img;
            } catch (IOException ex) {
                Logger.getLogger(DefineBitsTag.class.getName()).log(Level.SEVERE, "Failed to get image", ex);
            }
        }

        return null;
    }

    @Override
    public Dimension getImageDimension() {
        if (cachedImage != null) {
            return new Dimension(cachedImage.getWidth(), cachedImage.getHeight());
        }

        InputStream imageStream = getOriginalImageData();
        if (imageStream != null) {
            try {
                return ImageHelper.getDimesion(imageStream);
            } catch (IOException ex) {
                Logger.getLogger(DefineBitsJPEG3Tag.class.getName()).log(Level.SEVERE, "Failed to get image dimension", ex);
            }
        }

        return null;
    }

    @Override
    public void handleEvent(Tag tag) {
        // cache should be cleared when Jtt tag changes
        clearCache();
    }
}
