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
package com.jpexs.decompiler.flash.tags.gfx.enums;

import java.lang.reflect.Field;

/**
 * All File formats supported and/or used by GFx.
 *
 * @author JPEXS
 */
public class FileFormatType {

    /*public static final int FILE_SWF = 0;
    public static final int FILE_GFX = 1;*/

    public static final int FILE_DEFAULT = 0;

    // Image formats supported by Flash.        
    public static final int FILE_JPEG = 10;
    public static final int FILE_PNG = 11;
    public static final int FILE_GIF = 12;
    // Other image formats.
    public static final int FILE_TGA = 13;
    public static final int FILE_DDS = 14;
    public static final int FILE_HDR = 15;
    public static final int FILE_BMP = 16;
    public static final int FILE_DIB = 17;
    public static final int FILE_PFM = 18;
    public static final int FILE_TIFF = 19;

    // Sound formats
    public static final int FILE_WAVE = 20;

    //Additional Image Format
    public static final int FILE_PVR = 21;
    public static final int FILE_ETC = 22;
    public static final int FILE_SIF = 23;
    public static final int FILE_GXT = 24;
    public static final int FILE_GTX = 25;

    public static String fileFormatToString(int fileFormat) {
        try {
            for (Field f : FileFormatType.class.getDeclaredFields()) {
                if (f.getInt(FileFormatType.class) == fileFormat) {
                    return f.getName().substring(5); //strip "FILE_" prefix
                }
            }
        } catch (IllegalAccessException iae) {
            return null;
        }
        return null;
    }

    public static String fileFormatExtension(int fileFormat) {
        String name = fileFormatToString(fileFormat);
        if (name == null) {
            return null;
        }
        return name.toLowerCase();
    }
}
