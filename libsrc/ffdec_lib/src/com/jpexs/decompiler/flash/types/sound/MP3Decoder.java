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

import com.jpexs.decompiler.flash.SWFInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class MP3Decoder extends SoundDecoder {

    private final Decoder decoder = new Decoder();

    private final MyInputStream inputStream = new MyInputStream();

    private final Bitstream bitStream = new Bitstream(inputStream);

    class MyInputStream extends InputStream {

        byte[] buf = new byte[4096];

        int pos = 0;

        int remaining = 0;

        public void add(byte[] data) {
            if (data == null || data.length == 0) {
                return;
            }

            int remaining = this.remaining;
            int requiredSize = data.length + remaining;
            byte[] oldBuf = this.buf;
            byte[] buf = oldBuf;
            int pos = this.pos;
            if (requiredSize > buf.length) {
                int newSize = buf.length;
                while (requiredSize > newSize) {
                    newSize *= 2;
                }

                buf = new byte[newSize];
                this.buf = buf;
            }

            if (remaining > 0) {
                System.arraycopy(oldBuf, pos, buf, 0, remaining);
            }

            this.pos = 0;

            System.arraycopy(data, 0, buf, remaining, data.length);
            this.remaining = remaining + data.length;
        }

        @Override
        public int read() throws IOException {
            if (remaining > 0) {
                int result = buf[pos] & 0xff;
                remaining--;
                pos++;
                return result;
            }

            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            len = Math.min(len, remaining);
            if (len > 0) {
                System.arraycopy(buf, pos, b, off, len);
                remaining -= len;
                pos += len;
            }

            return len;
        }
    }

    public MP3Decoder(SoundFormat soundFormat) {
        super(soundFormat);
    }

    @Override
    public void decode(SWFInputStream sis, OutputStream os) throws IOException {
        byte[] data = sis.readBytesEx(sis.available(), "soundStream");
        inputStream.add(data);

        SampleBuffer buf;
        while ((buf = readFrame(decoder, bitStream)) != null) {
            short[] audio = buf.getBuffer();
            byte[] d = new byte[buf.getBufferLength() * 2];
            for (int i = 0; i < buf.getBufferLength(); i++) {
                int s = audio[i];
                d[i * 2] = (byte) (s & 0xff);
                d[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
            }
            os.write(d);
        }
    }

    private SampleBuffer readFrame(Decoder decoder, Bitstream bitstream) {
        try {
            Header h = bitstream.readFrame();
            if (h == null) {
                return null;
            }
            soundFormat.samplingRate = getSamplingRate(h);
            soundFormat.stereo = h.mode() != Header.SINGLE_CHANNEL;
            try {
                SampleBuffer ret = (SampleBuffer) decoder.decodeFrame(h, bitstream);
                bitstream.closeFrame();
                return ret;
            } catch (DecoderException ex) {
                return null;
            }
        } catch (BitstreamException ex) {
            return null;
        }

    }

    private static int getSamplingRate(Header h) {
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
}
