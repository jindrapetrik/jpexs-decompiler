/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import net.npe.dds.DDSReader;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

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

    @HideInRawEdit
    private SerializableImage serImage;

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

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        bitmapFormat = sis.readUI16("bitmapFormat");
        targetWidth = sis.readUI16("targetWidth");
        targetHeight = sis.readUI16("targetHeight");
        exportName = sis.readNetString("exportName");
        fileName = sis.readNetString("fileName");
        
        if (bitmapFormat == BITMAP_FORMAT_TGA) {
          return;
        }

        Path imagePath = Paths.get(sis.getSwf().getFile()).getParent().resolve(Paths.get(fileName));
        byte[] imageData = Files.readAllBytes(imagePath);
        int [] pixels = DDSReader.read(imageData, DDSReader.ARGB, 0);
        BufferedImage bufImage = new BufferedImage(DDSReader.getWidth(imageData), DDSReader.getHeight(imageData), BufferedImage.TYPE_INT_ARGB);
        bufImage.getRaster().setDataElements(0, 0, bufImage.getWidth(), bufImage.getHeight(), pixels);
        Image scaled = bufImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        bufImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        bufImage.getGraphics().drawImage(scaled, 0, 0, null);
        serImage = new SerializableImage(bufImage);
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
        return serImage;
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(serImage.getWidth(), serImage.getHeight());
    }
}
