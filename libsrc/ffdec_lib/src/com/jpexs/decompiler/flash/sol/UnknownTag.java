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
package com.jpexs.decompiler.flash.sol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Unknown tag.
 * @author JPEXS
 */
public class UnknownTag extends Tag {

    public UnknownTag(int tagType, byte[] data, boolean forceWriteAsLong) {
        super(tagType, "Unknown(" + tagType + ")", data, forceWriteAsLong);
    }

    @Override
    public void readData() {

    }

    @Override
    public void writeData(OutputStream os) throws IOException {
        os.write(data);
    }        
}
