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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Type-21 sound definitions; compressed codec payloads remain verbatim.
 */
public final class IggySoundConvertor {

    private static final int[] RATES = {5500, 11025, 22050, 44100};

    private IggySoundConvertor() {
    }

    public static DefineSoundTag toSwf(SWF swf, IggyTimeline source, long at, int id) throws IOException {
        boolean wide = source.is64();
        int header = wide ? 32 : 16;
        DefineSoundTag sound = new DefineSoundTag(swf);
        sound.soundId = id;
        int rate = IggyShapeStyles.u16(source, at + (wide ? 52 : 32));
        sound.soundRate = -1;
        for (int i = 0; i < RATES.length; i++) {
            if (rate == RATES[i]) {
                sound.soundRate = i;
                break;
            }
        }
        if (sound.soundRate < 0) {
            throw new IOException("Unsupported Iggy sound rate " + rate);
        }
        sound.soundFormat = IggyShapeStyles.u8(source, at + (wide ? 54 : 34));
        int size = IggyShapeStyles.u8(source, at + (wide ? 55 : 35));
        int channels = IggyShapeStyles.u8(source, at + (wide ? 56 : 36));
        if ((size != 8 && size != 16) || (channels != 1 && channels != 2)) {
            throw new IOException("Invalid Iggy sound format");
        }
        sound.soundSize = size == 16;
        sound.soundType = channels == 2;
        sound.soundSampleCount = source.bytes(at + header + 4, 4).getInt() & 0xffffffffL;
        int length = IggyShapeStyles.u32(source, at + (wide ? 48 : 28));
        byte[] data = new byte[length];
        if (length != 0) {
            source.bytes(source.reference(at + (wide ? 40 : 24)), length).get(data);
        }
        // The DLL stores native-endian 16-bit PCM in big-endian byte order.
        if (sound.soundFormat == 0 && sound.soundSize) {
            swapSamples(data);
        }
        sound.soundData = new ByteArrayRange(data);
        sound.setModified(true);
        return sound;
    }

    private static void swapSamples(byte[] data) throws IOException {
        if ((data.length & 1) != 0) {
            throw new IOException("Odd Iggy 16-bit PCM payload size");
        }
        for (int i = 0; i < data.length; i += 2) {
            byte value = data[i];
            data[i] = data[i + 1];
            data[i + 1] = value;
        }
    }

    public static IggyObjectData fromSwf(boolean wide, DefineSoundTag sound) throws IOException {
        if (sound.soundRate < 0 || sound.soundRate >= RATES.length || sound.soundFormat < 0 || sound.soundFormat > 15) {
            throw new IOException("Invalid SWF sound format");
        }
        byte[] data = sound.soundData.getRangeData();
        if (sound.soundFormat == 0 && sound.soundSize) {
            swapSamples(data);
        }
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        int size = wide ? 64 : 40;
        long at = IggyAssetConvertor.header(b, sound.soundId, 21, size);
        int header = wide ? 32 : 16;
        b.u32(at + header, data.length + 7L);
        b.u32(at + header + 4, sound.soundSampleCount);
        long body = data.length == 0 ? -1 : b.allocate(data.length);
        if (body >= 0) {
            for (int i = 0; i < data.length; i++) {
                b.u8(body + i, data[i] & 255);
            }
        }
        b.pointer(at + (wide ? 40 : 24), body);
        b.u32(at + (wide ? 48 : 28), data.length);
        b.u16(at + (wide ? 52 : 32), RATES[sound.soundRate]);
        b.u8(at + (wide ? 54 : 34), sound.soundFormat);
        b.u8(at + (wide ? 55 : 35), sound.soundSize ? 16 : 8);
        b.u8(at + (wide ? 56 : 36), sound.soundType ? 2 : 1);
        return new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()), b.characters, at, size, 21);
    }
}
