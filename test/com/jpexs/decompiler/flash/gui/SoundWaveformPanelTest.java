/*
 *  Copyright (C) 2010-2026 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.testng.annotations.Test;

public class SoundWaveformPanelTest {

    @Test
    public void mapsWaveformPositionToSoundFrame() {
        assertEquals(SoundWaveformPanel.frameForX(0, 101, 1000), 0);
        assertEquals(SoundWaveformPanel.frameForX(50, 101, 1000), 500);
        assertEquals(SoundWaveformPanel.frameForX(100, 101, 1000), 1000);
        assertEquals(SoundWaveformPanel.frameForX(-10, 101, 1000), 0);
        assertEquals(SoundWaveformPanel.frameForX(110, 101, 1000), 1000);
    }

    @Test
    public void decodesMonoPeaks() throws Exception {
        byte[] wav = createWav(1, new short[]{-32768, 16384, -8192, 32767});

        SoundWaveformPanel.WaveformData data = SoundWaveformPanel.WaveformData.create(wav, 2);

        assertEquals(data.getChannelCount(), 1);
        assertEquals(data.getMinimum(0, 0), -1f, 0.0001f);
        assertEquals(data.getMaximum(0, 0), 0.5f, 0.0001f);
        assertEquals(data.getMinimum(0, 1), -0.25f, 0.0001f);
        assertEquals(data.getMaximum(0, 1), 32767f / 32768f, 0.0001f);
    }

    @Test
    public void keepsStereoChannelsSeparate() throws Exception {
        byte[] wav = createWav(2, new short[]{-32768, 8192, 16384, -16384});

        SoundWaveformPanel.WaveformData data = SoundWaveformPanel.WaveformData.create(wav, 1);

        assertEquals(data.getChannelCount(), 2);
        assertEquals(data.getMinimum(0, 0), -1f, 0.0001f);
        assertEquals(data.getMaximum(0, 0), 0.5f, 0.0001f);
        assertEquals(data.getMinimum(1, 0), -0.5f, 0.0001f);
        assertEquals(data.getMaximum(1, 0), 0.25f, 0.0001f);
    }

    private byte[] createWav(int channels, short[] samples) throws Exception {
        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            pcm[i * 2] = (byte) samples[i];
            pcm[i * 2 + 1] = (byte) (samples[i] >> 8);
        }
        AudioFormat format = new AudioFormat(44100, 16, channels, true, false);
        AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(pcm),
                format, samples.length / channels);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, output);
        return output.toByteArray();
    }
}
