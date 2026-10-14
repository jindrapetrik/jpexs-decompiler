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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The DLL expands SWF filters into 44-byte x86 / 56-byte x64 records.
 */
final class IggyTimelineFilters {

    private static final int[] COLOR_MATRIX = {0, 1, 2, 3, 16, 4, 5, 6, 7, 17, 8, 9, 10, 11, 18, 12, 13, 14, 15, 19};

    private static int u8(IggyTimeline t, long at) throws IOException {
        return t.bytes(at, 1).get() & 255;
    }

    private static float f32(IggyTimeline t, long at) throws IOException {
        return t.bytes(at, 4).getFloat();
    }

    private static RGBA color(IggyTimeline t, long at) throws IOException {
        ByteBuffer b = t.bytes(at, 4);
        return new RGBA(b.get() & 255, b.get() & 255, b.get() & 255, b.get() & 255);
    }

    private static void color(IggyTimelineBuffer b, long at, RGBA c) throws IOException {
        b.u8(at, c.red);
        b.u8(at + 1, c.green);
        b.u8(at + 2, c.blue);
        b.u8(at + 3, c.alpha);
    }

    static FILTER read(SWF swf, IggyTimeline t, long at) throws IOException {
        int id = u8(t, at);
        int passes = u8(t, at + 1);
        int flags = u8(t, at + 2);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeUI8(id);
            switch (id) {
                case 0:
                case 2:
                case 4:
                    if (id == 4) {
                        int count = u8(t, at + 3);
                        long colors = t.reference(at + (t.is64() ? 32 : 28));
                        long ratios = t.reference(at + (t.is64() ? 40 : 32));
                        out.writeUI8(count);
                        for (int i = 0; i < count; i++) {
                            out.writeRGBA(color(t, colors + 4L * i));
                        }
                        for (int i = 0; i < count; i++) {
                            out.writeUI8(u8(t, ratios + i));
                        }
                    } else {
                        out.writeRGBA(color(t, at + 4));
                    }
                    out.writeFIXED(f32(t, at + 8));
                    out.writeFIXED(f32(t, at + 12));
                    if (id != 2) {
                        out.writeFIXED(f32(t, at + (id == 0 ? 16 : 20)));
                        out.writeFIXED(f32(t, at + (id == 0 ? 20 : 24)));
                    }
                    out.writeUFIXED8(f32(t, at + (id == 0 ? 24 : 16)));
                    out.writeUB(1, flags & 1);
                    out.writeUB(1, (flags >>> 1) & 1);
                    out.writeUB(1, 1); // CompositeSource is consumed but not retained by the DLL.
                    if (id == 4) {
                        out.writeUB(1, (flags >>> 2) & 1);
                    }
                    out.writeUB(id == 4 ? 4 : 5, passes);
                    break;
                case 1:
                    out.writeFIXED(f32(t, at + 4));
                    out.writeFIXED(f32(t, at + 8));
                    out.writeUB(5, passes);
                    out.writeUB(3, 0);
                    break;
                case 3:
                case 7:
                    if (id == 3) {
                        out.writeRGBA(color(t, at + 8));
                        out.writeRGBA(color(t, at + 12));
                    } else {
                        int count = u8(t, at + 5);
                        long colors = t.reference(at + (t.is64() ? 40 : 36));
                        long ratios = t.reference(at + (t.is64() ? 48 : 40));
                        out.writeUI8(count);
                        for (int i = 0; i < count; i++) {
                            out.writeRGBA(color(t, colors + 4L * i));
                        }
                        for (int i = 0; i < count; i++) {
                            out.writeUI8(u8(t, ratios + i));
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        out.writeFIXED(f32(t, at + 16 + 4 * i));
                    }
                    out.writeUFIXED8(f32(t, at + 32));
                    out.writeUB(1, flags != 0 ? 1 : 0);
                    out.writeUB(1, u8(t, at + 3) != 0 ? 1 : 0);
                    out.writeUB(1, 1);
                    out.writeUB(1, u8(t, at + 4) != 0 ? 1 : 0);
                    out.writeUB(4, passes);
                    break;
                case 5:
                    int x = passes;
                    int y = flags;
                    out.writeUI8(x);
                    out.writeUI8(y);
                    out.writeFLOAT(f32(t, at + 4));
                    out.writeFLOAT(f32(t, at + 8));
                    long matrix = t.reference(at + 16);
                    for (int i = 0; i < x * y; i++) {
                        out.writeFLOAT(f32(t, matrix + 4L * i));
                    }
                    out.writeRGBA(color(t, at + 12));
                    out.writeUB(6, 0);
                    out.writeUB(1, u8(t, at + 3) & 1);
                    out.writeUB(1, (u8(t, at + 3) >>> 1) & 1);
                    break;
                case 6:
                    long cm = t.reference(at + (t.is64() ? 8 : 4));
                    for (int i = 0; i < 20; i++) {
                        int index = COLOR_MATRIX[i];
                        out.writeFLOAT(f32(t, cm + 4L * index) * (index >= 16 ? 255 : 1));
                    }
                    break;
                default:
                    throw new IOException("Unsupported Iggy timeline filter: " + id);
            }
        }
        return new SWFInputStream(swf, bytes.toByteArray()).readFILTER("filter");
    }

    static void write(SWF swf, IggyTimelineBuffer b, long at, FILTER filter) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeFILTER(filter);
        }
        SWFInputStream in = new SWFInputStream(swf, bytes.toByteArray());
        int id = in.readUI8("id");
        b.u8(at, id);
        switch (id) {
            case 0:
            case 2:
            case 4:
                if (id == 4) {
                    int count = in.readUI8("count");
                    b.u8(at + 3, count);
                    long colors = b.allocate(4 * count);
                    long ratios = b.allocate(count);
                    b.pointer(at + (b.wide ? 32 : 28), colors);
                    b.pointer(at + (b.wide ? 40 : 32), ratios);
                    for (int i = 0; i < count; i++) {
                        color(b, colors + 4L * i, in.readRGBA("color"));
                    }
                    for (int i = 0; i < count; i++) {
                        b.u8(ratios + i, in.readUI8("ratio"));
                    }
                } else {
                    color(b, at + 4, in.readRGBA("color"));
                }
                b.f32(at + 8, in.readFIXED("blurX"));
                b.f32(at + 12, in.readFIXED("blurY"));
                if (id != 2) {
                    b.f32(at + (id == 0 ? 16 : 20), in.readFIXED("angle"));
                    b.f32(at + (id == 0 ? 20 : 24), in.readFIXED("distance"));
                }
                b.f32(at + (id == 0 ? 24 : 16), in.readUFIXED8("strength"));
                int flags = (int) in.readUB(1, "inner") | (int) in.readUB(1, "knockout") << 1;
                in.readUB(1, "composite");
                if (id == 4) {
                    flags |= (int) in.readUB(1, "onTop") << 2;
                }
                b.value(at + 2, flags, 1, 3);
                b.u8(at + 1, (int) in.readUB(id == 4 ? 4 : 5, "passes"));
                break;
            case 1:
                b.f32(at + 4, in.readFIXED("blurX"));
                b.f32(at + 8, in.readFIXED("blurY"));
                b.u8(at + 1, (int) in.readUB(5, "passes"));
                break;
            case 3:
            case 7:
                if (id == 3) {
                    color(b, at + 8, in.readRGBA("shadow"));
                    color(b, at + 12, in.readRGBA("highlight"));
                } else {
                    int count = in.readUI8("count");
                    b.u8(at + 5, count);
                    long colors = b.allocate(4 * count);
                    long ratios = b.allocate(count);
                    b.pointer(at + (b.wide ? 40 : 36), colors);
                    b.pointer(at + (b.wide ? 48 : 40), ratios);
                    for (int i = 0; i < count; i++) {
                        color(b, colors + 4L * i, in.readRGBA("color"));
                    }
                    for (int i = 0; i < count; i++) {
                        b.u8(ratios + i, in.readUI8("ratio"));
                    }
                }
                for (int i = 0; i < 4; i++) {
                    b.f32(at + 16 + 4 * i, in.readFIXED("value"));
                }
                b.f32(at + 32, in.readUFIXED8("strength"));
                b.u8(at + 2, (int) in.readUB(1, "inner"));
                b.u8(at + 3, (int) in.readUB(1, "knockout"));
                in.readUB(1, "composite");
                b.u8(at + 4, (int) in.readUB(1, "onTop"));
                b.u8(at + 1, (int) in.readUB(4, "passes"));
                break;
            case 5:
                int x = in.readUI8("matrixX");
                int y = in.readUI8("matrixY");
                b.u8(at + 1, x);
                b.u8(at + 2, y);
                b.f32(at + 4, in.readFLOAT("divisor"));
                b.f32(at + 8, in.readFLOAT("bias"));
                long matrix = b.allocate(4 * x * y);
                b.pointer(at + 16, matrix);
                for (int i = 0; i < x * y; i++) {
                    b.f32(matrix + 4L * i, in.readFLOAT("value"));
                }
                color(b, at + 12, in.readRGBA("defaultColor"));
                in.readUB(6, "reserved");
                int conv = (int) in.readUB(1, "clamp") | (int) in.readUB(1, "preserveAlpha") << 1;
                b.value(at + 3, conv, 1, 3);
                break;
            case 6:
                long cm = b.allocate(80);
                b.pointer(at + (b.wide ? 8 : 4), cm);
                for (int i = 0; i < 20; i++) {
                    int index = COLOR_MATRIX[i];
                    b.f32(cm + 4L * index, in.readFLOAT("value") / (index >= 16 ? 255 : 1));
                }
                break;
            default:
                throw new IOException("Unsupported SWF timeline filter: " + id);
        }
    }
}
