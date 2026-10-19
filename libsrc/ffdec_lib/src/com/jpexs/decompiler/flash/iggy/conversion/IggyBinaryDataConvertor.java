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
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Opaque type-20 payloads; the SWF reserved word is not stored by Iggy.
 */
public final class IggyBinaryDataConvertor {

    private IggyBinaryDataConvertor() {
    }

    public static DefineBinaryDataTag toSwf(SWF swf, IggyTimeline source, long at, int id) throws IOException {
        boolean wide = source.is64();
        long length = wide ? source.bytes(at + 32, 8).getLong() : source.bytes(at + 16, 4).getInt() & 0xffffffffL;
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new IOException("Invalid Iggy binary data length");
        }
        long address = source.reference(at + (wide ? 40 : 20));
        if (length > 0 && address == 0) {
            throw new IOException("Missing Iggy binary data");
        }
        java.nio.ByteBuffer body = source.bytes(address, (int) length);
        byte[] bytes = new byte[(int) length];
        body.get(bytes);
        DefineBinaryDataTag tag = new DefineBinaryDataTag(swf);
        tag.tag = id;
        tag.reserved = 0;
        tag.binaryData = new ByteArrayRange(bytes);
        tag.setModified(true);
        return tag;
    }

    public static IggyObjectData fromSwf(boolean wide, DefineBinaryDataTag tag) throws IOException {
        if (tag.reserved != 0) {
            throw new IOException("Iggy binary data requires reserved=0");
        }
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        int size = wide ? 48 : 24;
        long at = IggyAssetConvertor.header(b, tag.tag, 20, size);
        byte[] bytes = tag.binaryData.getRangeData();
        b.value(at + (wide ? 32 : 16), bytes.length, wide ? 8 : 4, wide ? 6 : 5);
        long body = bytes.length == 0 ? -1 : b.allocate(bytes.length);
        if (bytes.length > 0) {
            long end = b.data.position();
            b.data.seek(body, com.jpexs.decompiler.flash.iggy.streams.SeekMode.SET);
            b.data.writeBytes(bytes);
            b.data.seek(end, com.jpexs.decompiler.flash.iggy.streams.SeekMode.SET);
        }
        b.pointer(at + (wide ? 40 : 20), body);
        return new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()), b.characters, at, size, 20);
    }
}
