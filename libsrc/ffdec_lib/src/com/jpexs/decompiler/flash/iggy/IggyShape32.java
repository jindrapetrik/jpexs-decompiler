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
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A 40-byte x86 shape record; nodes are addressed through the field at +16.
 */
final class IggyShape32 extends IggyShape {

    byte[] header;
    final long address;
    final long nodeAddress;
    final long[] auxiliary = new long[3];

    IggyShape32(ReadDataStreamInterface s) throws IOException {
        long start = s.position();
        address = start;
        IggyLayout.checkRange(s, start, 40);
        header = s.readBytes(40);
        s.seek(start, SeekMode.SET);
        minx = s.readFloat();
        miny = s.readFloat();
        maxx = s.readFloat();
        maxy = s.readFloat();
        nodeAddress = IggyLayout.readPointer(s, false);
        count = s.readUI32();
        for (int i = 0; i < auxiliary.length; i++) {
            auxiliary[i] = IggyLayout.readPointer(s, false);
        }
        nodes = new ArrayList<>();
        if (count != 0) {
            if (nodeAddress == 0) {
                throw new IOException("Missing Iggy glyph nodes");
            }
            IggyLayout.checkRange(s, nodeAddress, count * 24);
            s.seek(nodeAddress, SeekMode.SET);
            for (int i = 0; i < count; i++) {
                nodes.add(new IggyShapeNode(s, i == 0));
            }
        }
    }
}
