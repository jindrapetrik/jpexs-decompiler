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
package com.jpexs.decompiler.flash.types.sound;

import com.jpexs.decompiler.flash.SWFInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.MarkingBufferedInputStream;
import javazoom.jl.decoder.MarkingPushbackInputStream;

/**
 * MP3 sound data with frames.
 *
 * @author JPEXS
 */
public class MP3SOUNDDATA {

    public int seekSamples;

    public List<MP3FRAME> frames;

    public MP3SOUNDDATA(SWFInputStream sis, boolean raw) throws IOException {
        if (!raw) {
            seekSamples = sis.readSI16("seekSamples");
        }
        frames = new ArrayList<>();
        MP3FRAME f;
        Decoder decoder = new Decoder();

        byte[] data = sis.readBytesEx(sis.available(), "soundStream");
        MarkingBufferedInputStream mis = new MarkingBufferedInputStream(new ByteArrayInputStream(data));
        Bitstream bitstream = new Bitstream(mis); //new ByteArrayInputStream(data)
        long initLen = mis.getPosition();
        MarkingPushbackInputStream mpis = bitstream.getSource();
        while (true) {
            //System.err.println("initLen = "+initLen);
            long posBefore = initLen + mpis.getPosition();
            MP3FRAME frame = MP3FRAME.readFrame(bitstream, decoder);
            if (frame == null) {
                break;
            }
            long posAfter = initLen + mpis.getPosition();
            frame.setFullData(Arrays.copyOfRange(data, (int) posBefore, (int) posAfter));
            frames.add(frame);
        }
    }

    public int sampleCount() {
        int r = 0;
        for (MP3FRAME f : frames) {
            r += f.getSamples().getBufferLength();
        }
        return r;
    }
}
