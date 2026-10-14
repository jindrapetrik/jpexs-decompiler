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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Decoded fixup program. Offsets are relative to its target data block.
 */
public final class IggyFixup {

    /** Maximum size of one relocation program accepted by the native loader. */
    public static final int NATIVE_BUFFER_SIZE = 65536;

    public static final class Field {

        public final long offset;
        public final int operation;

        public Field(long offset, int operation) {
            this.offset = offset;
            this.operation = operation;
        }
    }

    private static final class Template {

        int advance;
        int[] offsets;
        int[] operations;
    }

    private final List<Field> fields = new ArrayList<>();
    private long cursor;
    private final long targetSize;
    private final int pointerSize;

    private IggyFixup(long size, boolean is64) {
        targetSize = size;
        pointerSize = is64 ? 8 : 4;
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public long getLength() {
        return cursor;
    }

    private static int byteValue(ReadDataStreamInterface stream) throws IOException {
        int value = stream.readUI8();
        if (value < 0) {
            throw new IOException("Truncated Iggy fixup program");
        }
        return value;
    }

    private static Template template(ReadDataStreamInterface stream) throws IOException {
        Template t = new Template();
        t.advance = byteValue(stream);
        int count = byteValue(stream);
        t.offsets = new int[count];
        t.operations = new int[count];
        for (int i = 0; i < count; i++) {
            t.offsets[i] = byteValue(stream);
            t.operations[i] = byteValue(stream);
            width(t.operations[i], 8);
        }
        return t;
    }

    private static int width(int operation, int pointerSize) throws IOException {
        switch (operation) {
            case 2:
                return pointerSize;
            case 3:
            case 12:
            case 13:
            case 14:
            case 15:
                return 1;
            case 4:
                return 2;
            case 5:
                return 4;
            case 6:
                return 8;
            case 8:
            case 9:
            case 10:
            case 11:
                return pointerSize;
            default:
                throw new IOException("Invalid Iggy fixup operation: " + operation);
        }
    }

    private void field(long position, int operation) throws IOException {
        int size = width(operation, pointerSize);
        if (position < 0 || position > targetSize - size) {
            throw new IOException("Iggy fixup outside target block");
        }
        fields.add(new Field(position, operation));
    }

    private void advance(long amount) throws IOException {
        if (amount < 0 || amount > targetSize - cursor) {
            throw new IOException("Iggy fixup cursor outside target block");
        }
        cursor += amount;
    }

    private void apply(Template t) throws IOException {
        for (int i = 0; i < t.offsets.length; i++) {
            field(cursor + t.offsets[i], t.operations[i]);
        }
        advance(t.advance);
    }

    public static IggyFixup parse(boolean is64, byte[] program, long targetSize) throws IOException {
        return parse(is64, new MemoryDataStream(program), targetSize);
    }

    public static IggyFixup parse(boolean is64, ReadDataStreamInterface stream, long targetSize) throws IOException {
        IggyFixup result = new IggyFixup(targetSize, is64);
        Template[] templates = new Template[byteValue(stream)];
        for (int i = 0; i < templates.length; i++) {
            templates[i] = template(stream);
        }
        while (stream.available() != null && stream.available() > 0) {
            int code = byteValue(stream);
            if (code < 0xC0) {
                int repeats = code < 0x80 ? 1 : code - 127;
                int index = code < 0x80 ? code : byteValue(stream);
                if (index >= templates.length) {
                    throw new IOException("Invalid Iggy fixup template index");
                }
                for (int i = 0; i < repeats; i++) {
                    result.apply(templates[index]);
                }
            } else if (code < 0xD0) {
                result.advance(2 * code - 382);
            } else if (code == 0xD2 || code == 0xD4 || code == 0xD5 || code == 0xD6) {
                int count = byteValue(stream) + 1;
                int operation = code & 15;
                int size = width(operation, result.pointerSize);
                for (int i = 0; i < count; i++) {
                    result.field(result.cursor, operation);
                    result.advance(size);
                }
            } else if (code == 0xFC) {
                byteValue(stream);
            } else if (code == 0xFD) {
                result.apply(template(stream));
            } else if (code == 0xFE) {
                result.advance(byteValue(stream) + 1);
            } else if (code == 0xFF) {
                long amount = stream.readUI32();
                if (amount < 0) {
                    throw new IOException("Truncated Iggy cursor advance");
                }
                result.advance(amount);
            } else {
                throw new IOException("Invalid Iggy fixup opcode: " + code);
            }
        }
        return result;
    }

    /**
     * Convert payload scalars to native layout, keeping references relative.
     */
    public void normalize(byte[] data, boolean littleEndian, boolean nativeBitfields) throws IOException {
        for (Field field : fields) {
            int position = (int) field.offset;
            if (field.operation == 3 && !nativeBitfields) {
                data[position] = (byte) (Integer.reverse(data[position] & 255) >>> 24);
            } else if (!littleEndian && (field.operation == 2 || field.operation >= 4 && field.operation <= 6)) {
                int size = width(field.operation, pointerSize);
                for (int i = 0; i < size / 2; i++) {
                    byte temp = data[position + i];
                    data[position + i] = data[position + size - 1 - i];
                    data[position + size - 1 - i] = temp;
                }
            }
        }
    }

    private static void skip(ByteArrayOutputStream out, long distance) throws IOException {
        if (distance < 0 || distance > 0xffffffffL) {
            throw new IOException("Invalid Iggy fixup skip");
        }
        if (distance == 0) {
            return;
        }
        if (distance <= 32 && distance % 2 == 0) {
            out.write(191 + (int) distance / 2);
        } else if (distance <= 256) {
            out.write(0xfe);
            out.write((int) distance - 1);
        } else {
            out.write(0xff);
            for (int shift = 0; shift < 32; shift += 8) {
                out.write((int) (distance >>> shift));
            }
        }
    }

    private static byte[] encode(List<Field> fields, long size) throws IOException {
        int[] keys = new int[fields.size()];
        Map<Integer, Integer> counts = new HashMap<>();
        TreeSet<Integer> zeroAdvance = new TreeSet<>();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            long gap = i + 1 < fields.size() ? fields.get(i + 1).offset - field.offset : 0;
            // Preserve template field order, including unusual backwards offsets.
            // Such an input can still be partitioned without re-encoding it.
            if (gap < 0) {
                return null;
            }
            int key = field.operation * 256 + (gap <= 255 ? (int) gap : 0);
            keys[i] = key;
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            zeroAdvance.add(field.operation * 256);
        }
        List<Integer> templates = new ArrayList<>(zeroAdvance);
        List<Integer> candidates = new ArrayList<>(counts.keySet());
        candidates.sort((a, b) -> counts.get(a).equals(counts.get(b))
                ? Integer.compare(a, b) : Integer.compare(counts.get(b), counts.get(a)));
        for (int key : candidates) {
            if (templates.size() < 128 && !templates.contains(key)) {
                templates.add(key);
            }
        }
        Map<Integer, Integer> indices = new HashMap<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(templates.size());
        for (int i = 0; i < templates.size(); i++) {
            int key = templates.get(i);
            indices.put(key, i);
            out.write(key & 255);
            out.write(1);
            out.write(0);
            out.write(key / 256);
        }
        long cursor = 0;
        for (int i = 0; i < fields.size();) {
            Field field = fields.get(i);
            skip(out, field.offset - cursor);
            cursor = field.offset;
            int key = keys[i];
            if (!indices.containsKey(key)) {
                key = field.operation * 256;
            }
            int advance = key & 255;
            int repeats = 1;
            while (repeats < 64 && i + repeats < fields.size() && keys[i + repeats] == key
                    && fields.get(i + repeats).offset == cursor + (long) repeats * advance) {
                repeats++;
            }
            if (repeats > 1) {
                out.write(127 + repeats);
            }
            out.write(indices.get(key));
            cursor += (long) repeats * advance;
            i += repeats;
        }
        skip(out, size - cursor);
        return out.toByteArray();
    }

    private static List<byte[]> split(boolean is64, byte[] program, long size) throws IOException {
        if (program.length <= NATIVE_BUFFER_SIZE) {
            return Collections.singletonList(program);
        }
        MemoryDataStream stream = new MemoryDataStream(program);
        Template[] templates = new Template[byteValue(stream)];
        for (int i = 0; i < templates.length; i++) {
            templates[i] = template(stream);
        }
        int header = (int) stream.position();
        ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        chunk.write(program, 0, header);
        List<byte[]> result = new ArrayList<>();
        long cursor = 0;
        while (stream.available() > 0) {
            int start = (int) stream.position();
            long before = cursor;
            int code = byteValue(stream);
            if (code < 0xc0) {
                int repeats = code < 128 ? 1 : code - 127;
                int index = code < 128 ? code : byteValue(stream);
                cursor += (long) repeats * templates[index].advance;
            } else if (code < 0xd0) {
                cursor += 2 * code - 382;
            } else if (code == 0xd2 || code == 0xd4 || code == 0xd5 || code == 0xd6) {
                cursor += (byteValue(stream) + 1L) * width(code & 15, is64 ? 8 : 4);
            } else if (code == 0xfc) {
                byteValue(stream);
            } else if (code == 0xfd) {
                cursor += template(stream).advance;
            } else if (code == 0xfe) {
                cursor += byteValue(stream) + 1;
            } else if (code == 0xff) {
                cursor += stream.readUI32();
            } else {
                throw new IOException("Invalid Iggy fixup opcode: " + code);
            }
            int length = (int) stream.position() - start;
            if (chunk.size() + length > NATIVE_BUFFER_SIZE) {
                result.add(chunk.toByteArray());
                chunk.reset();
                chunk.write(program, 0, header);
                // Each descriptor starts its cursor at the target block's base.
                skip(chunk, before);
            }
            if (chunk.size() + length > NATIVE_BUFFER_SIZE || cursor > size) {
                throw new IOException("Iggy fixup instruction exceeds native buffer or target");
            }
            chunk.write(program, start, length);
        }
        result.add(chunk.toByteArray());
        return result;
    }

    /**
     * Compress a relocation program and partition it into native-sized blocks.
     * All scalar, pointer and host-context operations retain their exact order.
     *
     * @param is64 Target pointer width
     * @param program Original relocation program
     * @param size Target data block size
     * @return Programs of at most 64 KiB, each starting at the same target base
     * @throws IOException If the input is invalid or cannot be represented
     */
    public static List<byte[]> compact(boolean is64, byte[] program, long size) throws IOException {
        List<Field> expected = parse(is64, program, size).getFields();
        byte[] encoded = encode(expected, size);
        List<byte[]> chunks = split(is64, encoded != null && encoded.length < program.length ? encoded : program, size);
        int next = 0;
        for (byte[] chunk : chunks) {
            for (Field field : parse(is64, chunk, size).getFields()) {
                if (next >= expected.size() || field.offset != expected.get(next).offset
                        || field.operation != expected.get(next).operation) {
                    throw new IOException("Compacted Iggy fixup changed an operation");
                }
                next++;
            }
        }
        if (next != expected.size()) {
            throw new IOException("Compacted Iggy fixup lost operations");
        }
        return chunks;
    }

    /**
     * Emit explicit field operations without losing runtime context fixups.
     */
    public static void writeFields(IggyIndexBuilder builder, List<Field> fields, long size) throws IOException {
        List<Field> ordered = new ArrayList<>(fields);
        ordered.sort((a, b) -> Long.compare(a.offset, b.offset));
        long cursor = 0;
        for (Field field : ordered) {
            if (field.offset < cursor || field.offset >= size) {
                throw new IOException("Invalid output fixup position");
            }
            if (field.offset > cursor) {
                builder.writeLengthUI32(field.offset - cursor);
                cursor = field.offset;
            }
            builder.writeLengthCustom(0, new int[]{0}, new int[]{field.operation});
        }
        if (size > cursor) {
            builder.writeLengthUI32(size - cursor);
        }
    }
}
