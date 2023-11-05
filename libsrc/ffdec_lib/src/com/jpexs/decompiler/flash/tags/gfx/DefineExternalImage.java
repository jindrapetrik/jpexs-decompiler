/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.gfx.TgaSupport;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import javax.imageio.ImageIO;
import net.npe.dds.DDSReader;

/**
 *
 * @author JPEXS
 */
public class DefineExternalImage extends ImageTag {

    public static final int ID = 1001;

    public static final String NAME = "DefineExternalImage";

    public int bitmapFormat;

    public int targetWidth;

    public int targetHeight;

    public String exportName;

    public String fileName;

    public static final int BITMAP_FORMAT_DEFAULT = 0;

    public static final int BITMAP_FORMAT_TGA = 1;

    public static final int BITMAP_FORMAT_DDS = 2;
    
    //It looks like gfxexport produces BITMAP_FORMAT2_* values for format,
    //but BITMAP_FORMAT_* works the same way
    public static final int BITMAP_FORMAT2_JPEG = 10;

    public static final int BITMAP_FORMAT2_TGA = 13;

    public static final int BITMAP_FORMAT2_DDS = 14;

    @HideInRawEdit
    private SerializableImage serImage;

    @HideInRawEdit
    private String cachedImageFilename = null;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterID);
        sos.writeUI16(bitmapFormat);
        sos.writeUI16(targetWidth);
        sos.writeUI16(targetHeight);
        sos.writeNetString(exportName);
        sos.writeNetString(fileName);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineExternalImage(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    public DefineExternalImage(SWF swf) {
        super(swf, ID, NAME, null);
        exportName = "";
        fileName = "";
        targetWidth = 1;
        targetHeight = 1;
        bitmapFormat = BITMAP_FORMAT_DDS;
        createFailedImage();
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        bitmapFormat = sis.readUI16("bitmapFormat");
        targetWidth = sis.readUI16("targetWidth");
        targetHeight = sis.readUI16("targetHeight");
        exportName = sis.readNetString("exportName");
        fileName = sis.readNetString("fileName");
    }

    private void createFailedImage() {
        if (targetWidth <= 0 || targetHeight <= 0) {
            serImage = new SerializableImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            serImage.fillTransparent();
            return;
        }

        serImage = new SerializableImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = serImage.getGraphics();
        g.setColor(SWF.ERROR_COLOR);
        g.fillRect(0, 0, targetWidth, targetHeight);
        cachedImageFilename = null;
    }

    @Override
    public void setImage(byte[] data) throws IOException {
        serImage = new SerializableImage(ImageHelper.read(data));
        clearCache();
        setModified(true);
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
        initImage();
        return serImage;
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(targetWidth, targetHeight);
    }

    private void initImage() {
        if (!Objects.equals(cachedImageFilename, fileName)
                || (serImage != null && (serImage.getWidth() != targetWidth || serImage.getHeight() != targetHeight))) {

            if (targetWidth <= 0 || targetHeight <= 0) {
                serImage = new SerializableImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR_PRE);
                serImage.fillTransparent();
            } else if (bitmapFormat == BITMAP_FORMAT2_JPEG || bitmapFormat == BITMAP_FORMAT2_TGA || bitmapFormat == BITMAP_FORMAT_TGA) {
                Path imagePath = getSwf().getFile() == null ? null : Paths.get(getSwf().getFile()).getParent().resolve(Paths.get(fileName));
                if (imagePath != null && imagePath.toFile().exists()) {
                    try {
                        if (bitmapFormat == BITMAP_FORMAT2_TGA || bitmapFormat == BITMAP_FORMAT_TGA) {
                            TgaSupport.init();
                        }
                        BufferedImage bufImage = ImageIO.read(imagePath.toFile());
                        Image scaled = bufImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
                        bufImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                        bufImage.getGraphics().drawImage(scaled, 0, 0, null);
                        serImage = new SerializableImage(bufImage);
                        cachedImageFilename = fileName;
                    } catch (IOException ex) {
                        createFailedImage();
                    }
                } else {
                    createFailedImage();
                }
            } else if (bitmapFormat == BITMAP_FORMAT2_DDS || bitmapFormat == BITMAP_FORMAT2_DDS) {
                Path imagePath = getSwf().getFile() == null ? null : Paths.get(getSwf().getFile()).getParent().resolve(Paths.get(fileName));
                if (imagePath != null && imagePath.toFile().exists()) {
                    try {
                        byte[] imageData = Files.readAllBytes(imagePath);
                        int[] pixels = DDSReader.read(imageData, DDSReader.ARGB, 0);
                        BufferedImage bufImage = new BufferedImage(DDSReader.getWidth(imageData), DDSReader.getHeight(imageData), BufferedImage.TYPE_INT_ARGB);
                        bufImage.getRaster().setDataElements(0, 0, bufImage.getWidth(), bufImage.getHeight(), pixels);
                        Image scaled = bufImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
                        bufImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                        bufImage.getGraphics().drawImage(scaled, 0, 0, null);
                        serImage = new SerializableImage(bufImage);
                        cachedImageFilename = fileName;
                    } catch (IOException ex) {
                        createFailedImage();
                    }
                } else {
                    createFailedImage();
                }
            } else {
                createFailedImage();
            }
        }
    }

    @Override
    public boolean importSupported() {
        return false;
    }
    
    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);
        
        tagInfo.addInfo("general", "exportName", exportName);
        tagInfo.addInfo("general", "fileName", fileName);
        String bitmapFormatStr = "0x" + Integer.toHexString(bitmapFormat);
        switch (bitmapFormat) {
            case BITMAP_FORMAT_DEFAULT:
                bitmapFormatStr = "default (0)";
                break;
            case BITMAP_FORMAT_TGA:
                bitmapFormatStr = "TGA (1)";
                break;
            case BITMAP_FORMAT_DDS:
                bitmapFormatStr = "DDS (2)";
                break;
            case BITMAP_FORMAT2_JPEG:
                bitmapFormatStr = "JPEG (10)";
                break;
            case BITMAP_FORMAT2_TGA:
                bitmapFormatStr = "TGA (13)";
                break;
            case BITMAP_FORMAT2_DDS:
                bitmapFormatStr = "DDS (14)";
                break;
        }
        tagInfo.addInfo("general", "bitmapFormat", bitmapFormatStr);
    }
}
