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

import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Local graph builder; all references remain relative until installed in the
 * main block.
 */
final class IggyTimelineBuffer {

    final boolean wide;
    final MemoryDataStream data = new MemoryDataStream();
    final Map<Long, IggyFixup.Field> fields = new LinkedHashMap<>();
    final Map<Long, Integer> characters = new LinkedHashMap<>();

    IggyTimelineBuffer(boolean wide) throws IOException {
        this.wide = wide;
        data.writeUI64(0);
    }

    long allocate(int size) throws IOException {
        data.pad8bytes();
        long result = data.position();
        data.writeBytes(new byte[size]);
        return result;
    }

    void value(long at, long value, int width, int operation) throws IOException {
        long end = data.position();
        data.seek(at, SeekMode.SET);
        if (width == 8) {
            data.writeUI64(value);
        } else if (width == 4) {
            data.writeUI32(value);
        } else if (width == 2) {
            data.writeUI16((int) value);
        } else {
            data.writeUI8((int) value);
        }
        data.seek(end, SeekMode.SET);
        if (operation != 0) {
            fields.put(at, new IggyFixup.Field(at, operation));
        }
    }

    void u8(long at, int value) throws IOException {
        value(at, value, 1, 0);
    }

    void u16(long at, int value) throws IOException {
        value(at, value, 2, 4);
    }

    void u32(long at, long value) throws IOException {
        value(at, value, 4, 5);
    }

    void f32(long at, double value) throws IOException {
        u32(at, Float.floatToIntBits((float) value));
    }

    void pointer(long at, long target) throws IOException {
        value(at, target < 0 ? 1 : target - at, wide ? 8 : 4, 2);
    }

    void string(long at, String text) throws IOException {
        if (text == null) {
            pointer(at, -1);
            return;
        }
        long start = allocate((text.length() + 1) * 2);
        for (int i = 0; i < text.length(); i++) {
            u16(start + 2L * i, text.charAt(i));
        }
        u16(start + 2L * text.length(), 0);
        pointer(at, start);
    }
}
