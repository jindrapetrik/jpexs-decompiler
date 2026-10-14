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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Linked display-list commands. Payload remains available for unconverted
 * variants.
 */
public final class IggyTimeline {

    public static final class Command {

        private final long address;
        private final int type;
        private final byte[] data;

        private Command(long address, int type, byte[] data) {
            this.address = address;
            this.type = type;
            this.data = data;
        }

        public long getAddress() {
            return address;
        }

        public int getType() {
            return type;
        }

        public byte[] getData() {
            return data.clone();
        }
    }

    private final List<Command> commands = new ArrayList<>();
    private final byte[] source;
    private final boolean is64;

    /**
     * A complete replacement list, with local fixups and dictionary references.
     */
    public static final class Replacement {

        public final byte[] data;
        public final List<IggyFixup.Field> fields;
        public final Map<Long, Integer> characters;
        public final long first;
        public final long last;
        public final int maxDepth;
        public final int frameCount;
        public final List<IggyNamedFrame> labels;

        public Replacement(byte[] data, List<IggyFixup.Field> fields, Map<Long, Integer> characters,
                long first, long last, int maxDepth, int frameCount, List<IggyNamedFrame> labels) {
            this.data = data.clone();
            this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
            this.characters = Collections.unmodifiableMap(new LinkedHashMap<>(characters));
            this.first = first;
            this.last = last;
            this.maxDepth = maxDepth;
            this.frameCount = frameCount;
            this.labels = Collections.unmodifiableList(new ArrayList<>(labels));
        }
    }

    public IggyTimeline(byte[] data, long address) throws IOException {
        this(data, address, true);
    }

    public IggyTimeline(byte[] data, long address, boolean is64) throws IOException {
        source = data;
        this.is64 = is64;
        Set<Long> visited = new HashSet<>();
        while (address != 0) {
            if (address < 0 || address > data.length - (is64 ? 16 : 8) || !visited.add(address)) {
                throw new IOException("Invalid Iggy timeline link");
            }
            long next = reference(address);
            int type = bytes(address + (is64 ? 8 : 4), 1).get() & 255;
            int size;
            switch (type) {
                case 1:
                    size = is64 ? 16 : 8;
                    break;
                case 2:
                    ByteBuffer flags = bytes(address + (is64 ? 9 : 5), 2);
                    int value = flags.get() & 255;
                    int filters = flags.get() & 255;
                    size = filters != 127 && filters > 0 ? (is64 ? 96 : 64)
                            : ((value & 32) != 0 || (value & 8) != 0
                            && reference(address + (is64 ? 48 : 40)) != 0) ? (is64 ? 88 : 60)
                            : (value & 2) != 0 ? (is64 ? 80 : 56)
                                    : (value & 64) != 0 ? (is64 ? 72 : 52) : (is64 ? 64 : 48);
                    break;
                case 3:
                case 5:
                    size = is64 ? 24 : 12;
                    break;
                case 4:
                    size = is64 ? 24 : 16;
                    break;
                case 6:
                    size = is64 ? 56 : 40;
                    break;
                default:
                    throw new IOException("Unsupported Iggy timeline command: " + type);
            }
            if (address > data.length - size) {
                throw new IOException("Truncated Iggy timeline command");
            }
            commands.add(new Command(address, type, Arrays.copyOfRange(data, (int) address, (int) address + size)));
            address = next;
        }
    }

    public boolean is64() {
        return is64;
    }

    /**
     * Bounded, read-only view of an auxiliary record in the shared main block.
     */
    public ByteBuffer bytes(long address, int length) throws IOException {
        if (address < 0 || length < 0 || address > source.length - length) {
            throw new IOException("Iggy timeline reference outside main block");
        }
        return ByteBuffer.wrap(source, (int) address, length).slice().asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
    }

    public long reference(long field) throws IOException {
        ByteBuffer value = bytes(field, is64 ? 8 : 4);
        long offset = is64 ? value.getLong() : value.getInt();
        if (offset == 1) {
            return 0;
        }
        long target = is64 ? field + offset : (field + offset) & 0xffffffffL;
        bytes(target, 1);
        return target;
    }

    public String string(long address) throws IOException {
        if (address == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        while (true) {
            char value = bytes(address, 2).getChar();
            if (value == 0) {
                return result.toString();
            }
            result.append(value);
            address += 2;
        }
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
