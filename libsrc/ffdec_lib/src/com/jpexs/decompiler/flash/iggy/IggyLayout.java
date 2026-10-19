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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 * Relative references use native-width arithmetic; 1 is the null sentinel.
 */
final class IggyLayout {

    private IggyLayout() {
    }

    static void checkRange(ReadDataStreamInterface s, long position, long length) throws IOException {
        if (position < 0 || length < 0 || position > s.totalSize() - length) {
            throw new IOException("Iggy reference outside block: " + position);
        }
    }

    static long readPointer(ReadDataStreamInterface s, boolean is64) throws IOException {
        long position = s.position();
        long value = is64 ? s.readUI64() : s.readUI32();
        return value == 1 ? 0 : is64 ? position + value : (position + value) & 0xffffffffL;
    }

    static void writePointer(WriteDataStreamInterface s, boolean is64, long target) throws IOException {
        long value = target == 0 ? 1 : target - s.position();
        if (is64) {
            s.writeUI64(value);
        } else {
            s.writeUI32(value & 0xffffffffL);
        }
    }
}
