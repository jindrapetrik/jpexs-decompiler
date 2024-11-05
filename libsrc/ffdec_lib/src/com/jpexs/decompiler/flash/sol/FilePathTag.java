/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.sol;

import com.jpexs.decompiler.flash.amf.amf0.Amf0InputStream;
import com.jpexs.decompiler.flash.amf.amf0.Amf0OutputStream;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author JPEXS
 */
public class FilePathTag extends Tag {
    
    public static final int ID = 3;
    
    public String filePath;

    public FilePathTag(byte[] data, boolean forceWriteAsLong) {
        super(ID, "DefineFilePath", data, forceWriteAsLong);
    }

    @Override
    public void readData() throws IOException {
        Amf0InputStream is = new Amf0InputStream(new MemoryInputStream(data));
        int filePathLen = is.readU16("filePath");
        filePath = new String(is.readBytes(filePathLen), "UTF-8");
    }

    @Override
    public void writeData(OutputStream os) throws IOException {
        Amf0OutputStream aos = new Amf0OutputStream(os);                
        byte[] filePathData = filePath.getBytes("UTF-8");
        aos.writeU16(filePathData.length);
        aos.writeBytes(filePathData);
    }
    
}
