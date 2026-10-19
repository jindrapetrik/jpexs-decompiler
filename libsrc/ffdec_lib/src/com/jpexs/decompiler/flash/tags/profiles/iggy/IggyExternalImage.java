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
package com.jpexs.decompiler.flash.tags.profiles.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.iggy.conversion.IggyBitmapConvertor;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * External Iggy bitmap. This tag is decoded only by the Iggy SWF profile.
 */
public final class IggyExternalImage extends ImageTag {

    public static final int ID = 513;
    public static final String NAME = "IggyExternalImage";
    public int width = 1;
    public int height = 1;
    public int pixelFormat = 4;
    public int originalTag = 36;
    public int originalBitmapFormat = 5;
    public long originalDataLength;
    public float scaleX = 1;
    public float scaleY = 1;
    public float offsetX;
    public float offsetY;
    public String fileName = "";
    public int resourceType;
    /**
     * -1 means that no platform texture metadata was supplied.
     */
    public long resourceOffset = -1;
    public int resourceFormat = 65535;
    public int resourceWidth;
    public int resourceHeight;
    /**
     * Optional user replacement, stored in this tag until written back to Iggy.
     */
    public ByteArrayRange replacementImage = ByteArrayRange.EMPTY;
    @HideInRawEdit
    private transient File sourceDirectory;

    public IggyExternalImage(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
    }

    public IggyExternalImage(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        int version = sis.readUI8("version");
        if (version != 1) {
            throw new IOException("Unsupported IggyExternalImage version " + version);
        }
        characterID = sis.readUI16("characterID");
        width = sis.readUI16("width");
        height = sis.readUI16("height");
        pixelFormat = sis.readUI8("pixelFormat");
        originalTag = sis.readUI8("originalTag");
        originalBitmapFormat = sis.readUI8("originalBitmapFormat");
        originalDataLength = sis.readUI32("originalDataLength");
        scaleX = sis.readFLOAT("scaleX");
        scaleY = sis.readFLOAT("scaleY");
        offsetX = sis.readFLOAT("offsetX");
        offsetY = sis.readFLOAT("offsetY");
        fileName = sis.readString("fileName");
        resourceType = sis.readUI8("resourceType");
        resourceOffset = sis.readUI32("resourceOffset");
        if (resourceOffset == 0xffffffffL) {
            resourceOffset = -1;
        }
        resourceFormat = sis.readUI16("resourceFormat");
        resourceWidth = sis.readUI16("resourceWidth");
        resourceHeight = sis.readUI16("resourceHeight");
        long length = sis.readUI32("replacementLength");
        if (length > sis.available()) {
            throw new IOException("Truncated Iggy bitmap replacement");
        }
        replacementImage = sis.readByteRangeEx(length, "replacementImage");
        if (sis.available() != 0) {
            throw new IOException("Unexpected IggyExternalImage trailing data");
        }
    }

    @Override
    public void getData(SWFOutputStream out) throws IOException {
        out.writeUI8(1);
        out.writeUI16(characterID);
        out.writeUI16(width);
        out.writeUI16(height);
        out.writeUI8(pixelFormat);
        out.writeUI8(originalTag);
        out.writeUI8(originalBitmapFormat);
        out.writeUI32(originalDataLength);
        out.writeFLOAT(scaleX);
        out.writeFLOAT(scaleY);
        out.writeFLOAT(offsetX);
        out.writeFLOAT(offsetY);
        out.writeString(fileName);
        out.writeUI8(resourceType);
        out.writeUI32(resourceOffset < 0 ? 0xffffffffL : resourceOffset);
        out.writeUI16(resourceFormat);
        out.writeUI16(resourceWidth);
        out.writeUI16(resourceHeight);
        out.writeUI32(replacementImage.getLength());
        out.write(replacementImage);
    }

    public void setSourceDirectory(File directory) {
        sourceDirectory = directory;
    }

    public File getResourceFile() {
        if (fileName.isEmpty()) {
            return null;
        }
        File name = new File(fileName);
        if (name.isAbsolute()) {
            return name;
        }
        File base = sourceDirectory;
        String movie = getSwf().getFile();
        if (movie == null && getSwf().getOpenableList() != null && getSwf().getOpenableList().sourceInfo != null) {
            movie = getSwf().getOpenableList().sourceInfo.getFile();
        }
        if (base == null && movie != null) {
            base = new File(movie).getAbsoluteFile().getParentFile();
        }
        return base == null ? null : new File(base, fileName);
    }

    @Override
    public InputStream getOriginalImageData() {
        return replacementImage.getLength() == 0 ? null : new ByteArrayInputStream(replacementImage.getRangeData());
    }

    @Override
    public SerializableImage getImage() {
        try {
            BufferedImage image = replacementImage.getLength() == 0 ? IggyBitmapConvertor.readExternal(this)
                    : ImageHelper.read(replacementImage.getRangeData());
            if (image != null) {
                return new SerializableImage(image);
            }
        } catch (IOException | IllegalArgumentException ex) {
            // Keep the external reference usable even when its companion file is unavailable.
        }
        boolean validSize = width > 0 && height > 0 && (long) width * height <= 67108864;
        BufferedImage image = new BufferedImage(validSize ? width : 1, validSize ? height : 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(SWF.ERROR_COLOR);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        return new SerializableImage(image);
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(width, height);
    }

    @Override
    public void setImage(byte[] data) throws IOException {
        BufferedImage image = ImageHelper.read(data);
        if (image == null || image.getWidth() > 65535 || image.getHeight() > 65535) {
            throw new IOException("Invalid Iggy bitmap image");
        }
        replacementImage = new ByteArrayRange(data.clone());
        width = image.getWidth();
        height = image.getHeight();
        clearCache();
        setModified(true);
    }

    @Override
    public ImageFormat getImageFormat() {
        return ImageFormat.PNG;
    }

    @Override
    public ImageFormat getOriginalImageFormat() {
        return replacementImage.getLength() == 0 ? ImageFormat.PNG : ImageTag.getImageFormat(replacementImage);
    }

    @Override
    public void getTagInfo(TagInfo info) {
        super.getTagInfo(info);
        info.addInfo("general", "fileName", fileName);
        info.addInfo("general", "resourceOffset", resourceOffset);
        info.addInfo("general", "resourceFormat", resourceFormat);
        info.addInfo("general", "resourceWidth", resourceWidth);
        info.addInfo("general", "resourceHeight", resourceHeight);
    }
}
