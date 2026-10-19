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
import com.jpexs.decompiler.flash.iggy.streams.ReadDataStreamInterface;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.iggy.streams.WriteDataStreamInterface;
import java.io.IOException;

/**
 * The 128-byte Iggy 32 root found in version-0x900 files. This layout
 * differs from the 64-byte root of the 32 legacy layout. Opaque
 * context words remain byte-for-byte intact.
 */
public final class IggyFlashHeader32 implements IggyFlashHeaderInterface {

    public static final int STRUCT_SIZE = 128;
    private byte[] data;

    public IggyFlashHeader32(ReadDataStreamInterface stream) throws IOException {
        readFromDataStream(stream);
    }

    private long word(int offset) {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (long) (data[offset + i] & 255) << (8 * i);
        }
        return value;
    }

    private long pointer(int offset) {
        long value = word(offset);
        return value == 1 ? 0 : (offset + value) & 0xffffffffL;
    }

    @Override
    public void readFromDataStream(ReadDataStreamInterface stream) throws IOException {
        IggyLayout.checkRange(stream, stream.position(), STRUCT_SIZE);
        data = stream.readBytes(STRUCT_SIZE);
    }

    @Override
    public void writeToDataStream(WriteDataStreamInterface stream) throws IOException {
        stream.writeBytes(data);
        if (stream.getIndexing() != null) {
            stream.getIndexing().writeLengthCustom(STRUCT_SIZE,
                    new int[]{0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 64, 80, 84, 88, 96, 100, 104, 108, 120},
                    new int[]{2, 2, 2, 2, 2, 2, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2, 2, 5, 2, 2, 2, 2, 5});
        }
    }

    @Override
    public int getHeaderSize() {
        return STRUCT_SIZE;
    }

    @Override
    public long getBaseAddress() {
        return pointer(0);
    }

    @Override
    public long getDictionarySize() {
        return word(0x28);
    }

    @Override
    public long getTimelineAddress() {
        return pointer(8);
    }

    @Override
    public long getSourceNameAddress() {
        // sub_1003B9D0 copies this zero-terminated 8-bit name into the Player.
        return pointer(4);
    }

    @Override
    public long getFrameLabelsAddress() {
        return pointer(0x10);
    }

    @Override
    public long getFrameLabelCount() {
        return word(0x34);
    }

    @Override
    public long getScenesAddress() {
        return pointer(0x14);
    }

    @Override
    public long getSceneCount() {
        return word(0x38);
    }

    @Override
    public long getFrameCount() {
        return word(0x2c);
    }

    @Override
    public long getBackgroundColor() {
        return word(0x3c);
    }

    @Override
    public long getXMin() {
        return (int) word(0x18);
    }

    @Override
    public long getYMin() {
        return (int) word(0x1c);
    }

    @Override
    public long getXMax() {
        return (int) word(0x20);
    }

    @Override
    public long getYMax() {
        return (int) word(0x24);
    }

    @Override
    public float getFrameRate() {
        return Float.intBitsToFloat((int) word(0x40));
    }

    @Override
    public long getFlashFilenameAddress() {
        return pointer(0x64);
    }

    @Override
    public long getImportsAddress() {
        return pointer(0x50);
    }

    @Override
    public long getImportCount() {
        return word(0x58);
    }

    @Override
    public long getDictionaryFixupsAddress() {
        return pointer(0x60);
    }

    @Override
    public long getAbcAddress() {
        return pointer(0x68);
    }

    @Override
    public int getAbcPointerOffset() {
        return 0x68;
    }

    @Override
    public long getSymbolClassesAddress() {
        return pointer(0x6c);
    }

    @Override
    public long getSymbolClassCount() {
        return word(0x78);
    }

    @Override
    public boolean hasAS3() {
        return (data[0x70] & 255) != 0;
    }

    @Override
    public void setMovieProperties(long xMin, long yMin, long xMax, long yMax,
            float fps, long frames, long color) throws IOException {
        MemoryDataStream stream = new MemoryDataStream();
        stream.writeBytes(data);
        stream.seek(0x18, SeekMode.SET);
        stream.writeSI32(xMin);
        stream.writeSI32(yMin);
        stream.writeSI32(xMax);
        stream.writeSI32(yMax);
        stream.seek(0x2c, SeekMode.SET);
        stream.writeUI32(frames);
        stream.seek(0x3c, SeekMode.SET);
        stream.writeUI32(color);
        stream.writeFloat(fps);
        data = stream.getAllBytes();
    }
}
