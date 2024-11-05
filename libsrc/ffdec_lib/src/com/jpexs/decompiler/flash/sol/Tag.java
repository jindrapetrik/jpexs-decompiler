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

import java.io.IOException;
import java.io.OutputStream;

/**
 * SOL file tag.
 * @author JPEXS
 */
public abstract class Tag {
    protected boolean forceWriteAsLong;
    private final int tagType;
    private final String tagName;
    protected byte[] data;
    
    public Tag(int tagType, String tagName, byte[] data, boolean forceWriteAsLong) {
        this.tagType = tagType;
        this.tagName = tagName;
        this.data = data;
        this.forceWriteAsLong = forceWriteAsLong;
    }
        
    public abstract void readData() throws IOException;
    
    public abstract void writeData(OutputStream os) throws IOException;

    public int getTagType() {
        return tagType;
    }        

    public boolean isForceWriteAsLong() {
        return forceWriteAsLong;
    }            
}
