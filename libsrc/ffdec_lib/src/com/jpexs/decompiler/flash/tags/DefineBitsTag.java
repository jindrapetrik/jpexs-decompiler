/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.helpers.JpegFixer;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DefineBits tag - Contains a JPEG image. JPEG header is not included in the
 * data - needs JPEGTables tag.
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
     * @param swf SWF
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
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
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
     * @throws IOException On I/O error
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

    @Override
    public InputStream getOriginalImageData() {
        if (swf.getJtt() != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ByteArrayRange jttdata = swf.getJtt().jpegData;
                if (jttdata.getLength() != 0) {
                    baos.write(jttdata.getRangeData());
                }
                baos.write(jpegData.getRangeData());
                JpegFixer fixer = new JpegFixer();
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                fixer.fixJpeg(new ByteArrayInputStream(baos.toByteArray()), baos2);
                return new ByteArrayInputStream(baos2.toByteArray());
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

        SerializableImage img = new SerializableImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = img.getGraphics();
        g.setColor(SWF.ERROR_COLOR);
        g.fillRect(0, 0, 1, 1);
        return img;
    }

    @Override
    public Dimension getImageDimension() {
        if (cachedImage != null) {
            return new Dimension(cachedImage.getWidth(), cachedImage.getHeight());
        }

        InputStream imageStream = getOriginalImageData();
        if (imageStream != null) {
            try {
                return ImageHelper.getDimension(imageStream);
            } catch (IOException ex) {
                Logger.getLogger(DefineBitsJPEG3Tag.class.getName()).log(Level.SEVERE, "Failed to get image dimension", ex);
            }
        }

        return new Dimension(1, 1);
    }

    @Override
    public void handleEvent(Tag tag) {
        // cache should be cleared when Jtt tag changes
        clearCache();
    }
}
