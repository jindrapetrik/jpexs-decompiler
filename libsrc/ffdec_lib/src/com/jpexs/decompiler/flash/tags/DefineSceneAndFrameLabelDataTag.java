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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.annotations.Table;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class DefineSceneAndFrameLabelDataTag extends Tag {

    public static final int ID = 86;

    public static final String NAME = "DefineSceneAndFrameLabelData";

    @SWFType(value = BasicType.EncodedU32)
    @SWFArray(value = "offset", countField = "sceneCount")
    @Table(value = "scenes", itemName = "scene")
    public long[] sceneOffsets;

    @SWFArray(value = "name", countField = "sceneCount")
    @Table(value = "scenes", itemName = "scene")
    public String[] sceneNames;

    @SWFType(value = BasicType.EncodedU32)
    @SWFArray(value = "frameNum", countField = "frameLabelCount")
    @Table(value = "frames", itemName = "frame")
    public long[] frameNums;

    @SWFArray(countField = "frameLabelCount")
    @Table(value = "frames", itemName = "frame")
    public String[] frameNames;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineSceneAndFrameLabelDataTag(SWF swf) {
        super(swf, ID, NAME, null);
        sceneOffsets = new long[0];
        sceneNames = new String[0];
        frameNums = new long[0];
        frameNames = new String[0];
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineSceneAndFrameLabelDataTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        int sceneCount = (int) sis.readEncodedU32("sceneCount");
        sceneOffsets = new long[sceneCount];
        sceneNames = new String[sceneCount];
        for (int i = 0; i < sceneCount; i++) {
            sceneOffsets[i] = sis.readEncodedU32("sceneOffset");
            sceneNames[i] = sis.readString("sceneName");
        }
        int frameLabelCount = (int) sis.readEncodedU32("frameLabelCount");
        frameNums = new long[frameLabelCount];
        frameNames = new String[frameLabelCount];
        for (int i = 0; i < frameLabelCount; i++) {
            frameNums[i] = sis.readEncodedU32("frameNum");
            frameNames[i] = sis.readString("frameName");
        }

    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        int sceneCount = sceneOffsets.length;
        sos.writeEncodedU32(sceneCount);
        for (int i = 0; i < sceneCount; i++) {
            sos.writeEncodedU32(sceneOffsets[i]);
            sos.writeString(sceneNames[i]);
        }
        int frameLabelCount = frameNums.length;
        sos.writeEncodedU32(frameLabelCount);
        for (int i = 0; i < frameLabelCount; i++) {
            sos.writeEncodedU32(frameNums[i]);
            sos.writeString(frameNames[i]);
        }
    }
}
