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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.gfx.TgaSupport;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.gfx.enums.FileFormatType;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import net.npe.dds.DDSReader;

/**
 * Base class for GFX image tags.
 *
 * @author JPEXS
 */
public abstract class AbstractGfxImageTag extends ImageTag {

    /**
     * Constructs new AbstractGfxImageTag.
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public AbstractGfxImageTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Loads DDS image.
     * @param imageData Image data
     * @return BufferedImage
     */
    private BufferedImage loadDds(byte[] imageData) {
        int[] pixels = DDSReader.read(imageData, DDSReader.ARGB, 0);
        BufferedImage bufImage = new BufferedImage(DDSReader.getWidth(imageData), DDSReader.getHeight(imageData), BufferedImage.TYPE_INT_ARGB);
        bufImage.getRaster().setDataElements(0, 0, bufImage.getWidth(), bufImage.getHeight(), pixels);
        return bufImage;
    }

    /**
     * Gets external buffered image.
     * @param fileName File name
     * @param bitmapFormat Bitmap format
     * @return BufferedImage
     */
    protected BufferedImage getExternalBufferedImage(String fileName, int bitmapFormat) {
        Path imagePath = null;

        fileName = fileName.replace("\\", "/");

        try {
            imagePath = getSwf().getFile() == null ? null : Paths.get(getSwf().getFile()).getParent().resolve(Paths.get(fileName));
        } catch (InvalidPathException ip) {
            //ignore
        }
        if (imagePath == null || !imagePath.toFile().exists()) {

            SwfSpecificCustomConfiguration cc = Configuration.getSwfSpecificCustomConfiguration(getSwf().getShortPathTitle());
            if (cc == null) {
                return null;
            }
            String paths = cc.getCustomData(CustomConfigurationKeys.KEY_PATH_RESOLVING, "");
            if (paths.trim().isEmpty()) {
                return null;
            }
            String[] rows = paths.trim().split("\r\n");
            boolean found = false;
            for (String row : rows) {
                String prefix = "";
                String searchPath;
                if (row.contains("|")) {
                    prefix = row.substring(0, row.indexOf("|"));
                    searchPath = row.substring(row.indexOf("|") + 1);
                } else {
                    searchPath = row;
                }
                String fileNameNoPrefix = fileName;
                if (!prefix.isEmpty() && fileName.startsWith(prefix)) {
                    fileNameNoPrefix = fileName.substring(prefix.length());
                }
                if (!searchPath.isEmpty()) {
                    Path newImagePath = Paths.get(searchPath).resolve(fileNameNoPrefix);
                    if (newImagePath.toFile().exists()) {
                        found = true;
                        imagePath = newImagePath;
                        break;
                    }
                }
            }
            if (!found) {
                return null;
            }
        }

        byte[] imageData;
        try {
            imageData = Files.readAllBytes(imagePath);
        } catch (IOException ex) {
            return null;
        }
        if (imageData.length >= 4
                && imageData[0] == 0x44
                && imageData[1] == 0x44
                && imageData[2] == 0x53
                && imageData[3] == 0x20) {
            return loadDds(imageData);
        }

        if (fileName.toLowerCase().endsWith(".tga")
                || bitmapFormat == FileFormatType.FILE_TGA) {
            TgaSupport.init();
        }

        try {
            return ImageIO.read(imagePath.toFile());
        } catch (IOException ex) {
            return null;
        }
    }
}
