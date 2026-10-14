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

import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Backend-specific 12-byte texture records, including their dictionary IDs.
 */
public final class IggyTextureMetadata {

    public static final class Record {

        private final byte[] data;
        private final int characterId;

        private Record(byte[] data, int id) {
            this.data = data;
            characterId = id;
        }

        public byte[] getData() {
            return data.clone();
        }

        public int getCharacterId() {
            return characterId;
        }
    }

    private final List<Record> records = new ArrayList<>();

    public IggyTextureMetadata(byte[] data) throws IOException {
        this(data, true);
    }

    /**
     * 1.2.56 x86 records start after 12 bytes; x64 uses a 16-byte header.
     */
    public IggyTextureMetadata(byte[] data, boolean is64) throws IOException {
        //Files without external texture resources may retain an empty type-8
        //descriptor. There is no metadata header or record array in that case.
        if (data.length == 0) {
            return;
        }

        int headerSize = is64 ? 16 : 12;
        if (data.length < headerSize) {
            throw new IOException("Invalid Iggy texture metadata");
        }
        MemoryDataStream stream = new MemoryDataStream(data);
        int count = stream.readUI16();
        if (count < 0 || data.length < (long) headerSize + 12L * count) {
            throw new IOException("Invalid Iggy texture metadata");
        }
        stream.seek(headerSize, SeekMode.SET);
        for (int i = 0; i < count; i++) {
            byte[] record = stream.readBytes(12);
            records.add(new Record(record, (record[10] & 255) | (record[11] & 255) << 8));
        }
    }

    public static byte[] update(byte[] source, boolean is64, java.util.Map<Integer, byte[]> changes) throws IOException {
        int size = is64 ? 16 : 12;
        IggyTextureMetadata original = new IggyTextureMetadata(source, is64);
        List<byte[]> records = new ArrayList<>();
        java.util.Map<Integer, Integer> last = new java.util.LinkedHashMap<>();
        for (int i = 0; i < original.records.size(); i++) {
            last.put(original.records.get(i).characterId, i);
        }
        for (int i = 0; i < original.records.size(); i++) {
            Record record = original.records.get(i);
            if (changes.containsKey(record.characterId) && changes.get(record.characterId) == null) {
                continue;
            }
            records.add(changes.containsKey(record.characterId) && last.get(record.characterId) == i
                    ? changes.get(record.characterId) : record.data);
        }
        for (java.util.Map.Entry<Integer, byte[]> change : changes.entrySet()) {
            if (change.getValue() != null) {
                if (change.getValue().length != 12) {
                    throw new IOException("Invalid Iggy texture record");
                }
                if (!last.containsKey(change.getKey())) {
                    records.add(change.getValue());
                }
            }
        }
        if (records.size() > 65535) {
            throw new IOException("Too many Iggy textures");
        }
        if (source.length == 0 && records.isEmpty()) {
            return source;
        }
        int oldEnd = source.length == 0 ? 0 : size + original.records.size() * 12;
        int tail = source.length - oldEnd;
        byte[] result = new byte[size + records.size() * 12 + tail];
        if (source.length != 0) {
            System.arraycopy(source, 0, result, 0, size);
        }
        result[0] = (byte) records.size();
        result[1] = (byte) (records.size() >>> 8);
        int offset = size;
        for (byte[] record : records) {
            System.arraycopy(record, 0, result, offset, 12);
            offset += 12;
        }
        if (tail > 0) {
            System.arraycopy(source, oldEnd, result, offset, tail);
        }
        return result;
    }

    public List<Record> getRecords() {
        return Collections.unmodifiableList(records);
    }
}
