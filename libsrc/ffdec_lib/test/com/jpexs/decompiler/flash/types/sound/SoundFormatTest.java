/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.testng.annotations.Test;

public class SoundFormatTest {

    @Test
    public void preservesPcm16AndDefaultConstructorBehavior() throws Exception {
        byte[] input = {0, (byte) 128, 0, 0, -1, 127};
        for (int format : new int[]{0, 3}) {
            SoundFormat sound = new SoundFormat(format, 22050, false);
            assertEquals(sound.getDecoder().decode(new SWFInputStream(null, input)), input);
        }
    }

    @Test
    public void preservesCompressedDecoderOutputWhenSampleSizeFlagIsClear() throws Exception {
        byte[] input = {(byte) 128, 0, 0, 0};
        SoundFormat small = new SoundFormat(1, 22050, false, false);
        SoundFormat large = new SoundFormat(1, 22050, false, true);
        byte[] decoded = small.getDecoder().decode(new SWFInputStream(null, input));
        assertTrue(decoded.length > 0);
        assertEquals(decoded, large.getDecoder().decode(new SWFInputStream(null, input)));
    }
   
    @Test
    public void defineSoundWavPreservesQuietAmplitudeAndDuration() throws Exception {
        DefineSoundTag tag = new DefineSoundTag(new SWF());
        tag.soundFormat = 3;
        tag.soundRate = 1;
        tag.soundSize = false;
        tag.soundType = false;
        tag.soundSampleCount = 4410;
        byte[] input = new byte[4410];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) Math.round(128 + 127 * .02 * Math.sin(2 * Math.PI * 330 * i / 11025));
        }
        tag.soundData = new ByteArrayRange(input);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(tag.getSoundFormat().createWav(null, tag.getRawSoundData(), output, 0, true));
        try (AudioInputStream wav = AudioSystem.getAudioInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            assertEquals(wav.getFormat().getSampleSizeInBits(), 16);
            assertEquals(wav.getFormat().getChannels(), 1);
            assertEquals(wav.getFrameLength(), 4410L);
            assertEquals(wav.getFormat().getSampleRate(), 11025f);
            byte[] pcm = new byte[8820];
            assertEquals(wav.read(pcm), pcm.length);
            for (int i = 0; i < input.length; i++) {
                int value = (short) ((pcm[i * 2] & 255) | (pcm[i * 2 + 1] << 8));
                assertEquals(value, ((input[i] & 255) - 128) * 256);
                assertTrue(Math.abs(value) <= 768);
            }
        }
    }   

    @Test
    public void soundInfoTrimUsesDecodedPcm16SamplePositions() throws Exception {
        SoundFormat format = new SoundFormat(3, 11025, false, false);
        SOUNDINFO info = new SOUNDINFO();
        info.hasInPoint = true;
        info.inPoint = 4;
        info.hasOutPoint = true;
        info.outPoint = 12;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(format.createWav(info, Arrays.asList(new ByteArrayRange(
                new byte[]{(byte) 128, (byte) 129, (byte) 130, (byte) 131})), output, 0, true));
        try (AudioInputStream wav = AudioSystem.getAudioInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            assertEquals(wav.getFrameLength(), 2L);
            assertEquals(wav.getFormat().getChannels(), 2);
            byte[] pcm = new byte[8];
            assertEquals(wav.read(pcm), pcm.length);
            assertEquals(pcm, new byte[]{0, 1, 0, 1, 0, 2, 0, 2});
        }
    }
}
