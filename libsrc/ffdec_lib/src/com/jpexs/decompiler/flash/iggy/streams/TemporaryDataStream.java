/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy.streams;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class TemporaryDataStream extends RandomAccessFileDataStream {

    public TemporaryDataStream() throws IOException {
        this(new byte[0]);
    }

    public TemporaryDataStream(byte[] data) throws IOException {
        super(File.createTempFile("tempdatastream", ".bin"));
        this.getFile().deleteOnExit();
        writeBytes(data);
        seek(0, SeekMode.SET);
    }

    @Override
    public void close() {
        try {
            this.getFile().delete();
        } catch (Exception ex) {
            //ignore
        }
    }
}
