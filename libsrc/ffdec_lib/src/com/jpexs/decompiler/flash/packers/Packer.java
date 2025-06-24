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
package com.jpexs.decompiler.flash.packers;

import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Packer interface.
 *
 * @author JPEXS
 */
public interface Packer {

    /**
     * Is this DefineBinaryData packed with this packer?
     *
     * @param dataTag Data tag to check
     * @return true = it definitely is encrypted with this, false = it
     * definitely is not encrypted with this, null = it is unknown that it will
     * work
     */
    public Boolean suitableForBinaryData(DefineBinaryDataTag dataTag);

    /**
     * Is this data packed with this packer?
     *
     * @param data Data to check
     * @return true = it definitely is encrypted with this, false = it
     * definitely is not encrypted with this, null = it is unknown that it will
     * work
     */
    public Boolean suitableForData(byte[] data);

    /**
     * Unpack the data
     *
     * @param is Data to unpack
     * @param os Stream to write unpacked data to
     * @param key Key
     * @return True if it was unpacked correctly, False if it is not suitable
     * for unpacking or an error happened.
     * @throws IOException On I/O error
     */
    public boolean decrypt(InputStream is, OutputStream os, String key) throws IOException;

    /**
     * Pack the data
     *
     * @param is Data to pack
     * @param os Stream to write packed data to
     * @param key Key
     * @return True if packed successfully, False if error happened.
     * @throws IOException On I/O error
     */
    public boolean encrypt(InputStream is, OutputStream os, String key) throws IOException;

    /**
     * Human readable name of this packer
     *
     * @return Name of this packer
     */
    public String getName();

    /**
     * Unique identifier of this packer
     *
     * @return Identifier of this packer
     */
    public String getIdentifier();
    
    /**
     * Checks whether the packer uses encryption key.
     * @return True if key is used
     */
    public boolean usesKey();
}
