/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.sound;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;

/**
 *
 * @author JPEXS
 */
public class MP3SOUNDDATA {

    public int seekSamples;
    public List<MP3FRAME> frames;

    public MP3SOUNDDATA(SWF swf, byte[] data, boolean raw) throws IOException {
        SWFInputStream sis = new SWFInputStream(swf, data);
        if (!raw) {
            seekSamples = sis.readSI16();
        }
        frames = new ArrayList<>();
        MP3FRAME f;
        Decoder decoder = new Decoder();
        Bitstream bitstream = new Bitstream(new ByteArrayInputStream(data, 2, data.length - 2));
        while ((f = MP3FRAME.readFrame(bitstream, decoder)) != null) {
            frames.add(f);
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
