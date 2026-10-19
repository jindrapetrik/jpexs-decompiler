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
package com.jpexs.decompiler.flash.iggy.streams;

import java.io.IOException;
import java.util.List;

/**
 * Compatibility facade for the validated fixup-program decoder.
 */
public class IggyIndexParser {

    public static IggyFixup parseFixups(boolean is64, ReadDataStreamInterface stream, long targetSize) throws IOException {
        return IggyFixup.parse(is64, stream, targetSize);
    }

    /**
     * Legacy diagnostic API; offsets now include the individual field
     * locations.
     */
    public static void parseIndex(boolean is64, ReadDataStreamInterface stream,
            List<Integer> indexTableEntry, List<Long> offsets) throws IOException {
        byte[] bytes = stream.readBytes((int) (long) stream.available());
        MemoryDataStream table = new MemoryDataStream(bytes);
        int count = table.readUI8();
        if (count < 0) {
            throw new IOException("Missing Iggy fixup table");
        }
        for (int i = 0; i < count; i++) {
            int advance = table.readUI8();
            int fields = table.readUI8();
            if (advance < 0 || fields < 0) {
                throw new IOException("Truncated Iggy fixup table");
            }
            indexTableEntry.add(advance);
            table.readBytes(fields * 2);
        }
        IggyFixup program = IggyFixup.parse(is64, bytes, Integer.MAX_VALUE);
        for (IggyFixup.Field field : program.getFields()) {
            offsets.add(field.offset);
        }
        offsets.add(program.getLength());
    }
}
