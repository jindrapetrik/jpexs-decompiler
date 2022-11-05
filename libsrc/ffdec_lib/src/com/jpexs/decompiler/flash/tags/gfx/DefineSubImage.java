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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import net.npe.dds.DDSReader;

/**
 *
 * @author JPEXS
 */
public class DefineSubImage extends ImageTag {

    public static final int ID = 1008;

    public static final String NAME = "DefineSubImage";

    public int imageCharacterId;

    public int x1;

    public int y1;

    public int x2;

    public int y2;

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
        sos.writeUI16(imageCharacterId);
        sos.writeUI16(x1);
        sos.writeUI16(y1);
        sos.writeUI16(x2);
        sos.writeUI16(y2);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineSubImage(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        imageCharacterId = sis.readUI16("imageCharacterId");
        x1 = sis.readUI16("x1");
        y1 = sis.readUI16("y1");
        x2 = sis.readUI16("x2");
        y2 = sis.readUI16("y2");
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
        if (serImage == null) {
            DefineExternalImage2 image = (DefineExternalImage2) swf.getImage(imageCharacterId | 0x8000);

            Path imagePath = image.getSwf().getFile() == null ? null : Paths.get(image.getSwf().getFile()).getParent().resolve(Paths.get(image.fileName));
            if (imagePath != null && imagePath.toFile().exists()) {
                try {
                    byte[] imageData = Files.readAllBytes(imagePath);
                    int[] pixels = DDSReader.read(imageData, DDSReader.ARGB, 0);
                    BufferedImage bufImage = new BufferedImage(DDSReader.getWidth(imageData), DDSReader.getHeight(imageData), BufferedImage.TYPE_INT_ARGB);
                    bufImage.getRaster().setDataElements(0, 0, bufImage.getWidth(), bufImage.getHeight(), pixels);
                    Image scaled = bufImage.getScaledInstance(image.targetWidth, image.targetHeight, Image.SCALE_DEFAULT);
                    bufImage = new BufferedImage(x2 - x1, y2 - y1, BufferedImage.TYPE_INT_ARGB);
                    bufImage.getGraphics().drawImage(scaled, -x1, -y1, null);
                    serImage = new SerializableImage(bufImage);
                } catch (IOException e) {
                    createFailedImage();
                }               
            } else {
                createFailedImage();
            }
        }
        return serImage;
    }

    private void createFailedImage() {
        serImage = new SerializableImage(x2 - x1, y2 - y1, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = serImage.getGraphics();
        g.setColor(Color.red);
        g.fillRect(0, 0, x2 - x1, y2 - y1);
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(x2 - x1, y2 - y1);
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        needed.add(imageCharacterId | 0x8000);
    }
}
