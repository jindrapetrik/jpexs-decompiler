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

import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 32-bit font with 16-byte glyph descriptors. The 32 layout uses a 216-byte
 * header; the 32 legacy layout uses a 208-byte font header.
 */
public final class IggyFont32 extends IggyFont {

    private byte[] header;
    private final int headerSize;
    // Opaque shape auxiliaries remain in the original main block.
    final Map<Long, Long> externalReferences = new LinkedHashMap<>();
    final Map<Long, Long> relocatedParts = new LinkedHashMap<>();
    final Map<Long, Integer> partSizes = new LinkedHashMap<>();
    private long originalDescriptors;
    private long originalCount;

    public IggyFont32(ReadDataStreamInterface s) throws IOException {
        this(s, false);
    }

    public IggyFont32(ReadDataStreamInterface s, boolean extended) throws IOException {
        headerSize = extended ? 216 : 208;
        readFromDataStream(s);
    }

    public int getHeaderSize() {
        return headerSize;
    }

    private static long reference(ReadDataStreamInterface s, long field) throws IOException {
        IggyLayout.checkRange(s, field, 4);
        s.seek(field, SeekMode.SET);
        return IggyLayout.readPointer(s, false);
    }

    private static void table(ReadDataStreamInterface s, long address, long size) throws IOException {
        if (address == 0 && size != 0) {
            throw new IOException("Missing Iggy font table");
        }
        IggyLayout.checkRange(s, address, size);
        s.seek(address, SeekMode.SET);
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface s) throws IOException {
        long start = s.position();
        IggyLayout.checkRange(s, start, headerSize);
        header = s.readBytes(headerSize);
        MemoryDataStream h = new MemoryDataStream(header);
        type = h.readUI16();
        fontId = h.readUI16();
        zeroone = h.readBytes(12);
        char_count = char_count2 = h.readUI16();
        originalCount = char_count;
        ascent = h.readUI16();
        descent = h.readUI16();
        leading = h.readSI16();
        flags = h.readUI32();
        zero_padd_3 = (long) (header[88] & 255) << 32 | (long) (header[89] & 255) << 40;
        h.seek(40, SeekMode.SET);
        kern_count = h.readUI32();
        unk_float = new float[5];
        for (int i = 0; i < 4; i++) {
            unk_float[i] = h.readFloat();
        }
        long descriptors = reference(s, start + 28);
        long codes = reference(s, start + 32);
        originalDescriptors = descriptors;
        long advances = reference(s, start + 36);
        long kernings = reference(s, start + 60);
        long nameAddress = reference(s, start + 76);
        name = "";
        if (nameAddress != 0) {
            table(s, nameAddress, 2);
            name = s.readWChar();
        }
        charOffsets = new ArrayList<>();
        glyphs = new ArrayList<>();
        List<Long> shapes = new ArrayList<>();
        table(s, descriptors, char_count * 16);
        for (int i = 0; i < char_count; i++) {
            s.seek(descriptors + 16L * i, SeekMode.SET);
            long zero = s.readUI32();
            int flag = s.readUI16();
            int flag2 = s.readUI8();
            int flag3 = s.readUI8();
            long context = s.readUI32();
            IggyCharOffset descriptor = new IggyCharOffset(flag, flag2, 0, 0);
            descriptor.zero = zero;
            descriptor.zero2 = flag3;
            descriptor.zero3 = context;
            descriptor.offset = s.readUI32();
            charOffsets.add(descriptor);
            shapes.add(descriptor.offset == 1 ? 0 : (descriptors + 16L * i + 12 + descriptor.offset) & 0xffffffffL);
        }
        for (long shape : shapes) {
            if (shape == 0) {
                glyphs.add(null);
            } else {
                table(s, shape, 40);
                glyphs.add(new IggyShape32(s));
            }
        }
        if (codes != 0) {
            table(s, codes, char_count * 2);
            List<Character> chars = new ArrayList<>();
            for (int i = 0; i < char_count; i++) {
                chars.add((char) s.readUI16());
            }
            codePoints = new IggyCharIndices(chars);
        }
        if (advances != 0) {
            table(s, advances, char_count * 4);
            charScales = new IggyCharAdvances(s, char_count);
        }
        if (kernings != 0) {
            table(s, kernings, kern_count * 6);
            charKernings = new IggyCharKerning(s, kern_count);
        } else if (kern_count != 0) {
            throw new IOException("Missing Iggy kerning table");
        }
    }

    private static void setReference(WriteDataStreamInterface s, long field, long target) throws IOException {
        long saved = s.position();
        s.seek(field, SeekMode.SET);
        IggyLayout.writePointer(s, false, target);
        s.seek(saved, SeekMode.SET);
    }

    private static void scalar(WriteDataStreamInterface s, long field, long value, boolean word) throws IOException {
        long saved = s.position();
        s.seek(field, SeekMode.SET);
        if (word) {
            s.writeUI16((int) value);
        } else {
            s.writeUI32(value);
        }
        s.seek(saved, SeekMode.SET);
    }

    private void shape(WriteDataStreamInterface s, IggyShape glyph, List<IggyFixup.Field> fields) throws IOException {
        long start = s.position();
        IggyShape32 original = glyph instanceof IggyShape32 ? (IggyShape32) glyph : null;
        if (original != null) {
            relocatedParts.put(original.address, start);
            partSizes.put(original.address, 40);
        }
        s.writeBytes(original == null ? new byte[40] : original.header);
        s.seek(start, SeekMode.SET);
        s.writeFloat(glyph.minx);
        s.writeFloat(glyph.miny);
        s.writeFloat(glyph.maxx);
        s.writeFloat(glyph.maxy);
        IggyLayout.writePointer(s, false, glyph.nodes.isEmpty() ? 0 : start + 40);
        s.writeUI32(glyph.nodes.size());
        for (int i = 0; i < 3; i++) {
            long position = s.position();
            IggyLayout.writePointer(s, false, 0);
            if (original != null && original.auxiliary[i] != 0) {
                externalReferences.put(position, original.auxiliary[i]);
            }
            fields.add(new IggyFixup.Field(position, 2));
        }
        s.seek(start + 40, SeekMode.SET);
        if (original != null && original.nodeAddress != 0) {
            relocatedParts.put(original.nodeAddress, s.position());
            partSizes.put(original.nodeAddress, original.nodes.size() * 24);
        }
        for (int offset : new int[]{0, 4, 8, 12, 20}) {
            fields.add(new IggyFixup.Field(start + offset, 5));
        }
        fields.add(new IggyFixup.Field(start + 16, 2));
        for (IggyShapeNode node : glyph.nodes) {
            long position = s.position();
            node.writeToDataStream(s);
            for (int offset : new int[]{0, 4, 8, 12}) {
                fields.add(new IggyFixup.Field(position + offset, 5));
            }
            for (int offset : new int[]{18, 20, 22}) {
                fields.add(new IggyFixup.Field(position + offset, 4));
            }
        }
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface s) throws IOException {
        if (s.position() != 0) {
            throw new IOException("Serialize x86 fonts in a separate block");
        }
        externalReferences.clear();
        relocatedParts.clear();
        partSizes.clear();
        if (char_count > 65535 || glyphs.size() != char_count || charOffsets.size() != char_count) {
            throw new IOException("Invalid Iggy glyph count");
        }
        header[88] = (byte) (isBold() ? 1 : 0);
        header[89] = (byte) (isItalic() ? 1 : 0);
        s.writeBytes(header);
        for (int i = 0; i < 4; i++) {
            scalar(s, 44 + 4 * i, Float.floatToIntBits(unk_float[i]), false);
        }
        scalar(s, 0, type, true);
        scalar(s, 2, fontId, true);
        scalar(s, 16, char_count, true);
        scalar(s, 18, ascent, true);
        scalar(s, 20, descent, true);
        scalar(s, 22, leading, true);
        scalar(s, 24, flags, false);
        long kerningCount = charKernings == null ? 0 : charKernings.getKernCount();
        scalar(s, 40, kerningCount, false);
        scalar(s, 100, char_count, false);
        List<IggyFixup.Field> fields = new ArrayList<>();
        for (int offset : new int[]{2, 16, 18, 20, 22}) {
            fields.add(new IggyFixup.Field(offset, 4));
        }
        for (int offset : new int[]{40, 44, 48, 52, 56, 100}) {
            fields.add(new IggyFixup.Field(offset, 5));
        }
        fields.add(new IggyFixup.Field(27, 3));
        for (int offset : new int[]{28, 32, 36, 60, 76}) {
            setReference(s, offset, 0);
            fields.add(new IggyFixup.Field(offset, 2));
        }
        if (name != null) {
            setReference(s, 76, s.position());
            long position = s.position();
            s.writeWChar(name);
            for (int i = 0; i <= name.length(); i++) {
                fields.add(new IggyFixup.Field(position + 2L * i, 4));
            }
        }
        s.pad8bytes();
        long descriptors = s.position();
        setReference(s, 28, char_count == 0 ? 0 : descriptors);
        for (int i = 0; i < char_count; i++) {
            IggyCharOffset d = charOffsets.get(i);
            long position = s.position();
            if (i < originalCount && originalDescriptors != 0) {
                long old = originalDescriptors + 16L * i;
                relocatedParts.put(old, position);
                partSizes.put(old, 16);
            }
            s.writeUI32(d.zero);
            s.writeUI16(d.ischar1);
            s.writeUI8(d.ischar2);
            s.writeUI8((int) d.zero2);
            s.writeUI32(d.zero3);
            s.writeUI32(1);
            fields.add(new IggyFixup.Field(position + 4, 4));
            if (i >= originalCount || originalDescriptors == 0) {
                fields.add(new IggyFixup.Field(position + 8, 8));
            }
            fields.add(new IggyFixup.Field(position + 12, 2));
        }
        for (int i = 0; i < char_count; i++) {
            IggyShape glyph = glyphs.get(i);
            if (glyph != null) {
                s.pad8bytes();
                setReference(s, descriptors + 16L * i + 12, s.position());
                shape(s, glyph, fields);
            }
        }
        if (codePoints != null) {
            if (codePoints.chars.size() != char_count) {
                throw new IOException("Invalid Iggy code table size");
            }
            s.pad8bytes();
            long position = s.position();
            setReference(s, 32, position);
            for (char code : codePoints.chars) {
                s.writeUI16(code);
            }
            for (int i = 0; i < char_count; i++) {
                fields.add(new IggyFixup.Field(position + 2L * i, 4));
            }
        }
        if (charScales != null) {
            if (charScales.advances.size() != char_count) {
                throw new IOException("Invalid Iggy advance table size");
            }
            s.pad8bytes();
            long position = s.position();
            setReference(s, 36, position);
            charScales.writeToDataStream(s);
            for (int i = 0; i < char_count; i++) {
                fields.add(new IggyFixup.Field(position + 4L * i, 5));
            }
        }
        if (charKernings != null) {
            s.pad8bytes();
            long position = s.position();
            setReference(s, 60, position);
            charKernings.writeToDataStream(s);
            for (int i = 0; i < kerningCount * 3; i++) {
                fields.add(new IggyFixup.Field(position + 2L * i, 4));
            }
        }
        IggyFixup.writeFields(s.getIndexing(), fields, s.position());
    }
}
