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
package com.jpexs.decompiler.flash.types.sound;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 *
 * @author JPEXS
 */
public class MP3FRAME {

    private Header h;

    private SampleBuffer samples;

    private MP3FRAME() {

    }

    public static MP3FRAME readFrame(Bitstream bitstream, Decoder decoder) throws IOException {
        MP3FRAME ret = new MP3FRAME();
        try {
            ret.h = bitstream.readFrame();
            if (ret.h == null) {
                return null;
            }
        } catch (BitstreamException ex) {
            Logger.getLogger(MP3FRAME.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ret.samples = (SampleBuffer) decoder.decodeFrame(ret.h, bitstream);
        } catch (DecoderException ex) {
            Logger.getLogger(MP3FRAME.class.getName()).log(Level.SEVERE, null, ex);
        }
        bitstream.closeFrame();
        return ret;
    }

    public boolean isStereo() {
        return h.mode() != Header.SINGLE_CHANNEL;
    }

    public SampleBuffer getSamples() {
        return samples;
    }

    public int getSamplingRate() {
        switch (h.sample_frequency()) {
            case Header.THIRTYTWO:
                if (h.version() == Header.MPEG1) {
                    return 32000;
                } else if (h.version() == Header.MPEG2_LSF) {
                    return 16000;
                } else // SZD
                {
                    return 8000;
                }
            case Header.FOURTYFOUR_POINT_ONE:
                if (h.version() == Header.MPEG1) {
                    return 44100;
                } else if (h.version() == Header.MPEG2_LSF) {
                    return 22050;
                } else // SZD
                {
                    return 11025;
                }
            case Header.FOURTYEIGHT:
                if (h.version() == Header.MPEG1) {
                    return 48000;
                } else if (h.version() == Header.MPEG2_LSF) {
                    return 24000;
                } else // SZD
                {
                    return 12000;
                }
            default:
                return 0;
        }
    }

    public int getBitRate() {
        return h.bitrate();
    }
}
