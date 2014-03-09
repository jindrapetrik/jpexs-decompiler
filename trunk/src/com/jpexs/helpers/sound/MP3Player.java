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
package com.jpexs.helpers.sound;

/**
 *
 * @author JPEXS
 */
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

public class MP3Player extends SoundPlayer {

    private Bitstream bitstream;

    private Decoder decoder;

    private AudioDevice audio;

    private boolean closed = false;

    private boolean complete = false;

    private int positionSamples = 0;
    private long sampleCount;
    private int frameRate;
    private int positionFrames = 0;

    public MP3Player(InputStream stream, long sampleCount, int frameRate) throws JavaLayerException {
        this(stream, null);
        this.sampleCount = sampleCount;
        this.frameRate = frameRate;
    }

    private MP3Player(InputStream stream, AudioDevice device) throws JavaLayerException {
        super(stream);
        bitstream = new Bitstream(stream);
        decoder = new Decoder();

        if (device != null) {
            audio = device;
        } else {
            FactoryRegistry r = FactoryRegistry.systemRegistry();
            audio = r.createAudioDevice();
        }
        audio.open(decoder);
    }

    @Override
    public void play() {
        try {
            play(Integer.MAX_VALUE);
        } catch (JavaLayerException ex) {
            Logger.getLogger(MP3Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean play(int frames) throws JavaLayerException {
        if (complete) {
            return false;
        }
        boolean ret = true;

        while (frames-- > 0 && ret) {
            ret = decodeFrame();
        }

        if (!ret) {
            // last frame, ensure all data flushed to the audio device. 
            AudioDevice out = audio;
            if (out != null) {
                out.flush();
                synchronized (this) {
                    complete = (!closed);
                    stop();
                }
            }
        }
        return ret;
    }

    @Override
    public synchronized void skip(long samples) {
        if (complete) {
            return;
        }
        long endPosition = positionSamples + samples;
        if (samples == 0) {
            return;
        }
        try {
            while (true) {
                Header h = bitstream.readFrame();
                if (h == null) {
                    complete = true;
                    return;
                }
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
                positionSamples += output.getBufferLength();
                positionFrames++;
                bitstream.closeFrame();
                if (positionSamples >= endPosition) {
                    break;
                }
            }
        } catch (Exception ex) {
            //ingore
        }

    }

    @Override
    public synchronized void stop() {
        AudioDevice out = audio;
        if (out != null) {
            closed = true;
            audio = null;
            out.close();
        }
    }

    @Override
    public synchronized boolean isPlaying() {
        return !complete;
    }

    @Override
    public long getSamplePosition() {
        return positionSamples;
    }

    private boolean decodeFrame() throws JavaLayerException {
        try {
            AudioDevice out = audio;
            if (out == null) {
                return false;
            }

            Header h = bitstream.readFrame();
            if (h == null) {
                return false;
            }

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);
            positionSamples += output.getBufferLength();
            positionFrames++;

            synchronized (this) {
                out = audio;
                if (out != null) {
                    out.write(output.getBuffer(), 0, output.getBufferLength());
                }
            }

            bitstream.closeFrame();
        } catch (RuntimeException ex) {
            return true;
        }
        return true;
    }

    @Override
    public long samplesCount() {
        return sampleCount;
    }

    @Override
    public long getFrameRate() {
        return frameRate;
    }

}
