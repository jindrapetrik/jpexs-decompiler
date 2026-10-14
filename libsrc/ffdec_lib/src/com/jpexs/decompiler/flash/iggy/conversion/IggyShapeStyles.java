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
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Expanded fill and stroke styles shared by ordinary and morph shapes.
 */
final class IggyShapeStyles {

    private IggyShapeStyles() {
    }

    static int u8(IggyTimeline t, long at) throws IOException {
        return t.bytes(at, 1).get() & 255;
    }

    static int u16(IggyTimeline t, long at) throws IOException {
        return t.bytes(at, 2).getShort() & 65535;
    }

    static int u32(IggyTimeline t, long at) throws IOException {
        int value = t.bytes(at, 4).getInt();
        if (value < 0) {
            throw new IOException("Iggy shape count exceeds supported size");
        }
        return value;
    }

    static float f32(IggyTimeline t, long at) throws IOException {
        return t.bytes(at, 4).getFloat();
    }

    static RGBA color(IggyTimeline t, long at) throws IOException {
        ByteBuffer b = t.bytes(at, 4);
        return new RGBA(b.get() & 255, b.get() & 255, b.get() & 255, b.get() & 255);
    }

    static void color(IggyTimelineBuffer b, long at, RGB color) throws IOException {
        b.u8(at, color.red);
        b.u8(at + 1, color.green);
        b.u8(at + 2, color.blue);
        b.u8(at + 3, color instanceof RGBA ? ((RGBA) color).alpha : 255);
    }

    static MATRIX matrix(IggyTimeline t, long at, double linearScale) throws IOException {
        ByteBuffer b = t.bytes(at, 24);
        MATRIX m = new MATRIX();
        m.hasScale = true;
        m.hasRotate = true;
        m.scaleX = (float) (b.getFloat() * linearScale);
        m.rotateSkew1 = (float) (b.getFloat() * linearScale);
        m.rotateSkew0 = (float) (b.getFloat() * linearScale);
        m.scaleY = (float) (b.getFloat() * linearScale);
        m.translateX = Math.round(b.getFloat() * 20);
        m.translateY = Math.round(b.getFloat() * 20);
        return m;
    }

    static void matrix(IggyTimelineBuffer b, long at, MATRIX m, double linearScale) throws IOException {
        b.f32(at, (m.hasScale ? m.scaleX : 1) / linearScale);
        b.f32(at + 4, (m.hasRotate ? m.rotateSkew1 : 0) / linearScale);
        b.f32(at + 8, (m.hasRotate ? m.rotateSkew0 : 0) / linearScale);
        b.f32(at + 12, (m.hasScale ? m.scaleY : 1) / linearScale);
        b.f32(at + 16, m.translateX / 20.0);
        b.f32(at + 20, m.translateY / 20.0);
    }

    private static void readFill(SWFOutputStream out, IggySwf movie, IggyTimeline t,
            long at, boolean morph) throws IOException {
        int type = u8(t, at);
        int value = t.is64() ? 16 : 8;
        long reference = at + (t.is64() ? 64 : 56);
        out.writeUI8(type);
        switch (type) {
            case 0:
                out.writeRGBA(color(t, at + value));
                if (morph) {
                    out.writeRGBA(color(t, at + value + 4));
                }
                break;
            case 16:
            case 18:
            case 19:
                out.writeMatrix(matrix(t, at + value, 20));
                if (morph) {
                    out.writeMatrix(matrix(t, at + value + 24, type == 19 ? 1 : 20));
                }
                long gradient = t.reference(reference);
                int count = u8(t, gradient + 2);
                if (count > 15) {
                    throw new IOException("Invalid Iggy gradient count");
                }
                out.writeUB(2, u8(t, gradient));
                out.writeUB(2, u8(t, gradient + 1));
                out.writeUB(4, count);
                for (int i = 0; i < count; i++) {
                    long record = gradient + 12 + 10L * i;
                    out.writeUI8(u8(t, record + 8));
                    out.writeRGBA(color(t, record));
                    if (morph) {
                        out.writeUI8(u8(t, record + 9));
                        out.writeRGBA(color(t, record + 4));
                    }
                }
                if (type == 19) {
                    out.writeSI16((short) Math.round(f32(t, gradient + 4) * 256));
                    if (morph) {
                        out.writeSI16((short) Math.round(f32(t, gradient + 8) * 256));
                    }
                }
                break;
            case 64:
            case 65:
            case 66:
            case 67:
                int id = movie.getDictionaryId(reference, t.reference(reference));
                out.writeUI16(id < 0 ? 65535 : id);
                out.writeMatrix(matrix(t, at + value, 20));
                if (morph) {
                    out.writeMatrix(matrix(t, at + value + 24, 20));
                }
                break;
            default:
                throw new IOException("Unsupported Iggy fill style " + type);
        }
    }

    static void fill(SWF swf, IggyTimelineBuffer b, long at, FILLSTYLE value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeFILLSTYLE(value.toShapeNum(4), 4);
        }
        writeFill(new SWFInputStream(swf, bytes.toByteArray()), b, at, false);
    }

    static void fill(SWF swf, IggyTimelineBuffer b, long at, MORPHFILLSTYLE value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeMORPHFILLSTYLE(value, 2);
        }
        writeFill(new SWFInputStream(swf, bytes.toByteArray()), b, at, true);
    }

    static FILLSTYLE fill(SWF swf, IggySwf movie, IggyTimeline t, long at) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            readFill(out, movie, t, at, false);
        }
        return new SWFInputStream(swf, bytes.toByteArray()).readFILLSTYLE(4, "fill");
    }

    static MORPHFILLSTYLE morphFill(SWF swf, IggySwf movie, IggyTimeline t, long at) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            readFill(out, movie, t, at, true);
        }
        return new SWFInputStream(swf, bytes.toByteArray()).readMORPHFILLSTYLE("fill");
    }

    private static void readLine(SWFOutputStream out, IggySwf movie, IggyTimeline t,
            long at, boolean morph) throws IOException {
        int width = t.is64() ? 76 : 64;
        out.writeUI16(Math.round(f32(t, at + width) * 20));
        if (morph) {
            out.writeUI16(Math.round(f32(t, at + width + 4) * 20));
        }
        int cap = u8(t, at + width + 8);
        int join = u8(t, at + width + 10);
        int flags = u8(t, at + width + 11);
        boolean filled = u8(t, at) != 0; // Solid fills and ordinary color strokes have the same representation.
        out.writeUB(2, cap);
        out.writeUB(2, join);
        out.writeUB(1, filled ? 1 : 0);
        out.writeUB(1, flags & 1);
        out.writeUB(1, flags >>> 1 & 1);
        out.writeUB(1, flags >>> 2 & 1);
        out.writeUB(5, 0);
        out.writeUB(1, flags >>> 3 & 1);
        out.writeUB(2, u8(t, at + width + 9));
        if (join == 2) {
            out.writeUFIXED8(f32(t, at + width - 4));
        }
        if (filled) {
            readFill(out, movie, t, at, morph);
        } else {
            out.writeRGBA(color(t, at + (t.is64() ? 16 : 8)));
            if (morph) {
                out.writeRGBA(color(t, at + (t.is64() ? 20 : 12)));
            }
        }
    }

    static void line(SWF swf, IggyTimelineBuffer b, long at, LINESTYLE2 value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeLINESTYLE2(value, 4);
        }
        writeLine(new SWFInputStream(swf, bytes.toByteArray()), b, at, false);
    }

    static void line(SWF swf, IggyTimelineBuffer b, long at, MORPHLINESTYLE2 value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            out.writeMORPHLINESTYLE2(value, 2);
        }
        writeLine(new SWFInputStream(swf, bytes.toByteArray()), b, at, true);
    }

    static LINESTYLE2 line(SWF swf, IggySwf movie, IggyTimeline t, long at) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            readLine(out, movie, t, at, false);
        }
        return new SWFInputStream(swf, bytes.toByteArray()).readLINESTYLE2(4, "line");
    }

    static MORPHLINESTYLE2 morphLine(SWF swf, IggySwf movie, IggyTimeline t, long at) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream out = new SWFOutputStream(bytes, 10, "UTF-8")) {
            readLine(out, movie, t, at, true);
        }
        return new SWFInputStream(swf, bytes.toByteArray()).readMORPHLINESTYLE2("line");
    }

    private static void writeFill(SWFInputStream in, IggyTimelineBuffer b, long at, boolean morph) throws IOException {
        int type = in.readUI8("type");
        int value = b.wide ? 16 : 8;
        long reference = at + (b.wide ? 64 : 56);
        b.u8(at, type);
        b.pointer(at + (b.wide ? 8 : 4), -1);
        b.pointer(reference, -1);
        switch (type) {
            case 0:
                color(b, at + value, in.readRGBA("color"));
                if (morph) {
                    color(b, at + value + 4, in.readRGBA("endColor"));
                }
                break;
            case 16:
            case 18:
            case 19:
                matrix(b, at + value, in.readMatrix("matrix"), 20);
                if (morph) {
                    matrix(b, at + value + 24, in.readMatrix("endMatrix"), type == 19 ? 1 : 20);
                }
                int spread = (int) in.readUB(2, "spread");
                int interpolation = (int) in.readUB(2, "interpolation");
                int count = (int) in.readUB(4, "count");
                if (count > 15) {
                    throw new IOException("Too many Iggy gradient stops");
                }
                long gradient = b.allocate(14 + count * 10);
                b.pointer(reference, gradient);
                b.u8(gradient, spread == 3 ? 0 : spread);
                b.u8(gradient + 1, interpolation);
                b.u8(gradient + 2, count);
                for (int i = 0; i < count; i++) {
                    long record = gradient + 12 + 10L * i;
                    b.u8(record + 8, in.readUI8("ratio"));
                    color(b, record, in.readRGBA("color"));
                    if (morph) {
                        b.u8(record + 9, in.readUI8("endRatio"));
                        color(b, record + 4, in.readRGBA("endColor"));
                    }
                }
                if (type == 19) {
                    b.f32(gradient + 4, in.readSI16("focal") / 256.0);
                    if (morph) {
                        b.f32(gradient + 8, in.readSI16("endFocal") / 256.0);
                    }
                }
                break;
            case 64:
            case 65:
            case 66:
            case 67:
                int id = in.readUI16("bitmapId");
                if (id != 65535) {
                    b.characters.put(reference, id);
                }
                matrix(b, at + value, in.readMatrix("matrix"), 20);
                if (morph) {
                    matrix(b, at + value + 24, in.readMatrix("endMatrix"), 20);
                }
                break;
            default:
                throw new IOException("Unsupported SWF fill style " + type);
        }
    }

    private static void writeLine(SWFInputStream in, IggyTimelineBuffer b, long at, boolean morph) throws IOException {
        int width = b.wide ? 76 : 64;
        b.f32(at + width, in.readUI16("width") / 20.0);
        if (morph) {
            b.f32(at + width + 4, in.readUI16("endWidth") / 20.0);
        }
        b.u8(at + width + 8, (int) in.readUB(2, "cap"));
        int join = (int) in.readUB(2, "join");
        b.u8(at + width + 10, join);
        boolean filled = in.readUB(1, "filled") != 0;
        int flags = (int) in.readUB(1, "noHScale") | (int) in.readUB(1, "noVScale") << 1
                | (int) in.readUB(1, "pixelHinting") << 2;
        in.readUB(5, "reserved");
        flags |= (int) in.readUB(1, "noClose") << 3;
        b.u8(at + width + 9, (int) in.readUB(2, "endCap"));
        b.value(at + width + 11, flags, 1, 3);
        if (join == 2) {
            b.f32(at + width - 4, in.readUFIXED8("miter"));
        }
        if (filled) {
            writeFill(in, b, at, morph);
        } else {
            b.u8(at, 0);
            b.pointer(at + (b.wide ? 8 : 4), -1);
            b.pointer(at + (b.wide ? 64 : 56), -1);
            color(b, at + (b.wide ? 16 : 8), in.readRGBA("color"));
            if (morph) {
                color(b, at + (b.wide ? 20 : 12), in.readRGBA("endColor"));
            }
        }
    }

}
