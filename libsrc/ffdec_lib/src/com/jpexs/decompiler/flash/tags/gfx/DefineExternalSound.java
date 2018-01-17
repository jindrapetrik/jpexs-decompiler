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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class DefineExternalSound extends Tag {

    public static final int ID = 1006;

    public static final String NAME = "DefineExternalSound";

    public int characterId;

    public int soundFormat;

    public int bits;

    public int channels;

    public long sampleRate;

    public long sampleCount;

    public long seekSample;

    public String exportName;

    public String fileName;

    public static final int SOUND_FORMAT_WAV = 0;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeUI16(soundFormat);
        sos.writeUI16(bits);
        sos.writeUI16(channels);
        sos.writeUI32(sampleRate);
        sos.writeUI32(sampleCount);
        sos.writeUI32(seekSample);
        sos.writeNetString(exportName);
        sos.writeNetString(fileName);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineExternalSound(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        soundFormat = sis.readUI16("soundFormat");
        bits = sis.readUI16("bits");
        channels = sis.readUI16("channels");
        sampleRate = sis.readUI32("sampleRate");
        sampleCount = sis.readUI32("sampleCount");
        seekSample = sis.readUI32("seekSample");
        exportName = sis.readNetString("exportName");
        fileName = sis.readNetString("fileName");

    }
}
