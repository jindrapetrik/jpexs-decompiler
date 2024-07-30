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

import com.jpexs.decompiler.flash.tags.gfx.enums.FileFormatType;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gfx.TgaSupport;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import net.npe.dds.DDSReader;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractGfxImageTag extends ImageTag {
                      
    public AbstractGfxImageTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }
    
    private BufferedImage loadDds(byte[] imageData) {
        int[] pixels = DDSReader.read(imageData, DDSReader.ARGB, 0);
        BufferedImage bufImage = new BufferedImage(DDSReader.getWidth(imageData), DDSReader.getHeight(imageData), BufferedImage.TYPE_INT_ARGB);
        bufImage.getRaster().setDataElements(0, 0, bufImage.getWidth(), bufImage.getHeight(), pixels);
        return bufImage; 
    }
    
    protected BufferedImage getExternalBufferedImage(String fileName, int bitmapFormat) {
        Path imagePath = getSwf().getFile() == null ? null : Paths.get(getSwf().getFile()).getParent().resolve(Paths.get(fileName));
        if (imagePath == null || !imagePath.toFile().exists()) {
            return null;
        }
                
        byte[] imageData;
        try {
            imageData = Files.readAllBytes(imagePath);
        } catch (IOException ex) {
            return null;
        }
        if (imageData.length >= 4 &&
                imageData[0] == 0x44 && 
                imageData[1] == 0x44 &&
                imageData[2] == 0x53 &&
                imageData[3] == 0x20) {
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
