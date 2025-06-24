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
package com.jpexs.decompiler.flash;

/**
 * SWF header information.
 *
 * @author JPEXS
 */
public class SWFHeader {

    /**
     * Version of SWF
     */
    public int version;

    /**
     * Uncompressed size of the file
     */
    public long fileSize;

    /**
     * Used compression mode
     */
    public SWFCompression compression = SWFCompression.NONE;

    /**
     * LZMA properties
     */
    public byte[] lzmaProperties;

    /**
     * ScaleForm GFx
     */
    public boolean gfx = false;

    /**
     * Harman SWF Encryption
     */
    public boolean encrypted = false;

    /**
     * Frame rate
     */
    public float frameRate;
}
