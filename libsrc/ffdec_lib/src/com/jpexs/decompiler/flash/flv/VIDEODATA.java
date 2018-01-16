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
package com.jpexs.decompiler.flash.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class VIDEODATA extends DATA {

    public int frameType;

    public int codecId;

    public byte[] videoData;

    public VIDEODATA(int frameType, int codecId, byte[] videoData) {
        this.frameType = frameType;
        this.codecId = codecId;
        this.videoData = videoData;
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FLVOutputStream flv = new FLVOutputStream(baos);
            flv.writeUB(4, frameType);
            flv.writeUB(4, codecId);
            flv.write(videoData);
        } catch (IOException ex) {
            // ignore
        }
        return baos.toByteArray();
    }
}
