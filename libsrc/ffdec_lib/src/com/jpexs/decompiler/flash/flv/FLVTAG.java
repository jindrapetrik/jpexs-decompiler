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

/**
 *
 * @author JPEXS
 */
public class FLVTAG {

    public int tagType;

    public long timeStamp;

    public DATA data;

    public static final int DATATYPE_VIDEO = 9;

    public static final int DATATYPE_AUDIO = 8;

    public static final int DATATYPE_SCRIPT_DATA = 18;

    public FLVTAG(long timeStamp, SCRIPTDATA data) {
        tagType = DATATYPE_SCRIPT_DATA;
        this.data = data;
    }

    public FLVTAG(long timeStamp, VIDEODATA data) {
        this.tagType = DATATYPE_VIDEO;
        this.timeStamp = timeStamp;
        this.data = data;
    }

    public FLVTAG(long timeStamp, AUDIODATA data) {
        this.tagType = DATATYPE_AUDIO;
        this.timeStamp = timeStamp;
        this.data = data;
    }
}
