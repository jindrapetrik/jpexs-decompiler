/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy.streams;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class RandomAccessFileDataStream extends AbstractDataStream {

    private File file;
    private RandomAccessFile raf;
    private IggyIndexBuilder indexing;

    protected File getFile() {
        return file;
    }

    @Override
    public void setIndexing(IggyIndexBuilder indexing) {
        this.indexing = indexing;
    }

    public RandomAccessFileDataStream(File file) throws FileNotFoundException {
        this.file = file;
        raf = new RandomAccessFile(file, "rw");
    }

    @Override
    public Long totalSize() {
        try {
            return raf.length();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public Long available() {
        try {
            return raf.length() - raf.getFilePointer();
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public long position() {
        try {
            return raf.getFilePointer();
        } catch (IOException ex) {
            return -1;
        }
    }

    @Override
    public int read() throws IOException {
        int val = raf.read();
        if (val == -1) {
            throw new EOFException();
        }
        return val;
    }

    @Override
    public byte[] readBytes(int numBytes) throws IOException {
        byte buf[] = new byte[numBytes];
        raf.readFully(buf);
        return buf;
    }

    @Override
    public void seek(long pos, SeekMode mode) throws IOException {
        long newpos = pos;
        if (mode == SeekMode.CUR) {
            newpos = raf.getFilePointer() + pos;
        } else if (mode == SeekMode.END) {
            newpos = raf.length() - pos;
        }
        if (newpos > raf.length()) {
            raf.seek(raf.length());
            long curpos = raf.length();
            for (long i = curpos; i < newpos; i++) {
                raf.write(0);
            }
        } else if (newpos < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative position accessed: " + pos);
        } else {
            raf.seek(newpos);
        }
    }

    @Override
    public void close() {
        try {
            raf.close();
        } catch (IOException ex) {
            //ignore
        }
    }

    @Override
    public void write(int val) throws IOException {
        raf.write(val);
    }

    @Override
    public IggyIndexBuilder getIndexing() {
        return indexing;
    }

    @Override
    public boolean writeWChar(String name) throws IOException {
        return super.writeWChar(name);
    }

    @Override
    public void pad8bytes() throws IOException {
        super.pad8bytes();
    }

}
