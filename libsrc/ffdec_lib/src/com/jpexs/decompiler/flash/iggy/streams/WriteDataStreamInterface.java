/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

/**
 * Write data stream interface.
 *
 * @author JPEXS
 */
public interface WriteDataStreamInterface extends AutoCloseable {

    /**
     * Gets available bytes.
     *
     * @return null if unknown, long value otherwise
     */
    public Long available();

    /**
     * Gets total size of the stream.
     *
     * @return Size
     */
    public Long totalSize();

    /**
     * Gets current position in the stream.
     *
     * @return Position
     */
    public long position();

    /**
     * Sets older offset to this position.
     * @param savedPos Saved position
     * @throws IOException On I/O error
     */
    public void setOlderOffsetToThisPos(long savedPos) throws IOException;

    /**
     * Sets older offset to this position check.
     * @param savedPos Saved position
     * @param expected Expected value
     * @throws IOException On I/O error
     */
    public void setOlderOffsetToThisPosCheck(long savedPos, long expected) throws IOException;

    /**
     * Writes unsigned 64-bit integer.
     *
     * @param val Unsigned 64-bit integer
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeUI64(long val) throws IOException;

    /**
     * Writes signed 64-bit integer.
     *
     * @param val Signed 64-bit integer
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeSI64(long val) throws IOException;

    /**
     * Writes unsigned 32-bit integer.
     *
     * @param val Unsigned 32-bit integer
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeUI32(long val) throws IOException;

    /**
     * Writes unsigned 16-bit integer.
     * @param val Unsigned 16-bit integer
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeUI16(int val) throws IOException;

    /**
     * Writes UTF-8 string.
     * @param val UTF-8 string
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeWChar(String val) throws IOException;

    /**
     * Pads 8 bytes.
     * @throws IOException On I/O error
     */
    public void pad8bytes() throws IOException;

    /**
     * Writes unsigned 8-bit integer.
     * @param val Unsigned 8-bit integer
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeUI8(int val) throws IOException;

    /**
     * Write byte.
     * @param val Byte
     * @throws IOException On I/O error
     */
    public void write(int val) throws IOException;

    /**
     * Writes byte array.
     * @param data Byte array
     * @throws IOException On I/O error
     */
    public void writeBytes(byte[] data) throws IOException;

    /**
     * Writes float.
     * @param val Float
     * @return True if successful, false if not enough space
     * @throws IOException On I/O error
     */
    public boolean writeFloat(float val) throws IOException;

    /**
     * Seeks to specified position.
     * @param pos Position
     * @param mode Seek mode
     * @throws IOException On I/O error
     */
    public void seek(long pos, SeekMode mode) throws IOException;

    /**
     * Gets all bytes.
     * @return All bytes
     * @throws IOException On I/O error
     */
    public byte[] getAllBytes() throws IOException;

    /**
     * Closes the stream.
     */
    @Override
    public void close();

    /**
     * Sets indexing.
     * @param indexing Indexing
     */
    public void setIndexing(IggyIndexBuilder indexing);

    /**
     * Gets indexing.
     * @return Indexing
     */
    public IggyIndexBuilder getIndexing();
}
