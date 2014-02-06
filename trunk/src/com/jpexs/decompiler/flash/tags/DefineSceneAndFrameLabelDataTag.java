/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineSceneAndFrameLabelDataTag extends Tag {

    @SWFType(BasicType.EncodedU32)
    public long[] sceneOffsets;

    public String[] sceneNames;

    @SWFType(BasicType.EncodedU32)
    public long[] frameNums;

    public String[] frameNames;
    public static final int ID = 86;

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
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
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineSceneAndFrameLabelDataTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineSceneAndFrameLabelData", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        int sceneCount = (int) sis.readEncodedU32();
        sceneOffsets = new long[sceneCount];
        sceneNames = new String[sceneCount];
        for (int i = 0; i < sceneCount; i++) {
            sceneOffsets[i] = sis.readEncodedU32();
            sceneNames[i] = sis.readString();
        }
        int frameLabelCount = (int) sis.readEncodedU32();
        frameNums = new long[frameLabelCount];
        frameNames = new String[frameLabelCount];
        for (int i = 0; i < frameLabelCount; i++) {
            frameNums[i] = sis.readEncodedU32();
            frameNames[i] = sis.readString();
        }

    }
}
