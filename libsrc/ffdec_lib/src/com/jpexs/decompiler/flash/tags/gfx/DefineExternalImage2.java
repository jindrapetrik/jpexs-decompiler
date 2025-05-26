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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.tags.gfx.enums.FileFormatType;
import com.jpexs.decompiler.flash.tags.gfx.enums.IdType;
import com.jpexs.decompiler.flash.types.annotations.HideInRawEdit;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * DefineExternalImage2 tag - external image. Extends functionality of
 * DefineExternalImage.
 *
 * @author JPEXS
 */
public class DefineExternalImage2 extends AbstractGfxImageTag {

    public static final int ID = 1009;

    public static final String NAME = "DefineExternalImage2";

    public int imageID;

    public int idType;

    public int bitmapFormat;

    public int targetWidth;

    public int targetHeight;

    public String exportName;

    public String fileName;

    public byte[] extraData; //?   

    @HideInRawEdit
    private SerializableImage serImage;

    @HideInRawEdit
    private String cachedImageFilename = null;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(imageID);
        sos.writeUI16(idType);
        sos.writeUI16(bitmapFormat);
        sos.writeUI16(targetWidth);
        sos.writeUI16(targetHeight);
        sos.writeNetString(exportName);
        sos.writeNetString(fileName);
        if (extraData != null) {
            sos.write(extraData);
        }
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineExternalImage2(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
        characterID = idType == IdType.IDTYPE_NONE ? imageID : -1;
    }

    public DefineExternalImage2(SWF swf) {
        super(swf, ID, NAME, null);
        exportName = "";
        fileName = "";
        targetWidth = 1;
        targetHeight = 1;
        idType = IdType.IDTYPE_NONE;
        bitmapFormat = FileFormatType.FILE_DDS;
        characterID = -1;
        createFailedImage();
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        imageID = sis.readUI16("imageID");
        idType = sis.readUI16("idType");
        bitmapFormat = sis.readUI16("bitmapFormat");
        targetWidth = sis.readUI16("targetWidth");
        targetHeight = sis.readUI16("targetHeight");
        exportName = sis.readNetString("exportName");
        fileName = sis.readNetString("fileName");
        if (sis.available() > 0) { //there is usually one zero byte, god knows why
            extraData = sis.readBytesEx(sis.available(), "extraData");
        }
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
    public SerializableImage getImage() {
        initImage();
        return serImage;
    }

    @Override
    public Dimension getImageDimension() {
        return new Dimension(targetWidth, targetHeight);
    }

    private void initImage() {
        if (Objects.equals(cachedImageFilename, fileName)
                && serImage != null && (serImage.getWidth() == targetWidth && serImage.getHeight() == targetHeight)) {
            return;
        }

        if (targetWidth <= 0 || targetHeight <= 0) {
            serImage = new SerializableImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR_PRE);
            serImage.fillTransparent();
            return;
        }

        BufferedImage bufImage = getExternalBufferedImage(fileName, bitmapFormat);
        if (bufImage == null) {
            createFailedImage();
            return;
        }
        Image scaled = bufImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        bufImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        bufImage.getGraphics().drawImage(scaled, 0, 0, null);
        serImage = new SerializableImage(bufImage);
        cachedImageFilename = fileName;
    }

    @Override
    public boolean importSupported() {
        return false;
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("iid", "" + getUniqueId());
        return ret;
    }

    @Override
    public String getUniqueId() {
        if (idType == IdType.IDTYPE_NONE) {
            return super.getUniqueId();
        }
        return "i" + imageID;
    }

    @Override
    public void setCharacterId(int characterId) {

    }

    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);

        tagInfo.addInfo("general", "exportName", exportName);
        tagInfo.addInfo("general", "fileName", fileName);
        String bitmapFormatStr = "0x" + Integer.toHexString(bitmapFormat);
        String fileFormatStr = FileFormatType.fileFormatToString(bitmapFormat);
        if (fileFormatStr != null) {
            bitmapFormatStr = fileFormatStr + " (" + bitmapFormat + ")";
        }
        tagInfo.addInfo("general", "bitmapFormat", bitmapFormatStr);

        if (idType != IdType.IDTYPE_NONE) {
            tagInfo.addInfo("general", "imageId", imageID);
        }
        tagInfo.addInfo("general", "idType", IdType.idTypeToString(idType) + " (" + idType + ")");
    }
}
