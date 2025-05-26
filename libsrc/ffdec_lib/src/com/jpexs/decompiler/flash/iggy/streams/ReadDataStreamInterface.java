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
 * Read data stream interface.
 *
 * @author JPEXS
 */
public interface ReadDataStreamInterface extends AutoCloseable {

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
     * @return Position
     */
    public long position();

    /**
     * Reads unsigned 64-bit integer.
     *
     * @return Unsigned 64-bit integer
     * @throws IOException On I/O error
     */
    public long readUI64() throws IOException;

    /**
     * Reads signed 64-bit integer.
     *
     * @return Signed 64-bit integer
     * @throws IOException On I/O error
     */
    public long readSI64() throws IOException;

    /**
     * Reads unsigned 32-bit integer.
     *
     * @return Unsigned 32-bit integer
     * @throws IOException On I/O error
     */
    public long readUI32() throws IOException;

    /**
     * Reads unsigned 16-bit integer.
     * @return Unsigned 16-bit integer
     * @throws IOException On I/O error
     */
    public int readUI16() throws IOException;

    /**
     * Reads unsigned 8-bit integer.
     * @return Unsigned 8-bit integer
     * @throws IOException On I/O error
     */
    public int readUI8() throws IOException;

    /**
     * Reads unsigned 8-bit integer from specified address.
     * @param addr Address
     * @return Unsigned 8-bit integer
     * @throws IOException On I/O error
     */
    public int readUI8(long addr) throws IOException;

    /**
     * Reads byte.
     * @return Byte
     * @throws IOException On I/O error
     */
    public int read() throws IOException;

    /**
     * Reads byte array.
     * @param numBytes Number of bytes to read
     * @return Byte array
     * @throws IOException On I/O error
     */
    public byte[] readBytes(int numBytes) throws IOException;

    /**
     * Reads float.
     * @return Float
     * @throws IOException On I/O error
     */
    public float readFloat() throws IOException;

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
     * Reads UTF-8 string.
     * @return UTF-8 string
     * @throws IOException On I/O error
     */
    public String readWChar() throws IOException;

    /**
     * Pads 8 bytes.
     * @throws IOException On I/O error
     */
    public void pad8bytes() throws IOException;

    /**
     * Closes the stream.
     */
    @Override
    public void close();
}
