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
package com.jpexs.decompiler.flash.flv;

/**
 * FLV tag.
 *
 * @author JPEXS
 */
public class FLVTAG {

    /**
     * Tag type
     */
    public int tagType;

    /**
     * Timestamp in milliseconds
     */
    public long timeStamp;

    /**
     * Data
     */
    public DATA data;

    /**
     * Data type: video
     */
    public static final int DATATYPE_VIDEO = 9;

    /**
     * Data type: audio
     */
    public static final int DATATYPE_AUDIO = 8;

    /**
     * Data type: script data
     */
    public static final int DATATYPE_SCRIPT_DATA = 18;

    /**
     * Constructor.
     * @param timeStamp Timestamp in milliseconds
     * @param data Data
     */
    public FLVTAG(long timeStamp, SCRIPTDATA data) {
        tagType = DATATYPE_SCRIPT_DATA;
        this.timeStamp = timeStamp;
        this.data = data;
    }

    /**
     * Constructor.
     * @param timeStamp Timestamp in milliseconds
     * @param data Data
     */
    public FLVTAG(long timeStamp, UnparsedDATA data) {
        tagType = data.getDataType();
        this.timeStamp = timeStamp;
        this.data = data;
    }

    /**
     * Constructor.
     * @param timeStamp Timestamp in milliseconds
     * @param data Data
     */
    public FLVTAG(long timeStamp, VIDEODATA data) {
        this.tagType = DATATYPE_VIDEO;
        this.timeStamp = timeStamp;
        this.data = data;
    }

    /**
     * Constructor.
     * @param timeStamp Timestamp in milliseconds
     * @param data Data
     */
    public FLVTAG(long timeStamp, AUDIODATA data) {
        this.tagType = DATATYPE_AUDIO;
        this.timeStamp = timeStamp;
        this.data = data;
    }
}
