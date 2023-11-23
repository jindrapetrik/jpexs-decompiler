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
     * @param dataTag
     * @return true = it definitely is encrypted with this, false = it
     * definitely is not encrypted with this, null = it is unknown that it will
     * work
     */
    public Boolean suitableForBinaryData(DefineBinaryDataTag dataTag);
    
    /**
     * Is this data packed with this packer?
     *
     * @param data
     * @return true = it definitely is encrypted with this, false = it
     * definitely is not encrypted with this, null = it is unknown that it will
     * work
     */
    public Boolean suitableForData(byte[] data);

    /**
     * Unpack the data
     *
     * @param is
     * @param os
     * @return True if it was unpacked correctly, False if it is not suitable
     * for unpacking or an error happened.
     * @throws java.io.IOException
     */
    public boolean decrypt(InputStream is, OutputStream os) throws IOException;

    /**
     * Pack the data
     *
     * @param is
     * @param os
     * @return True if packed successfully, False if error happened.
     * @throws java.io.IOException
     */
    public boolean encrypt(InputStream is, OutputStream os) throws IOException;

    /**
     * Human readable name of this packer
     *
     * @return
     */
    public String getName();
    
    public String getIdentifier();
}
