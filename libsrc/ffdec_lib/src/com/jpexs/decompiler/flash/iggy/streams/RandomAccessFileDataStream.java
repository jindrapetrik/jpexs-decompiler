package com.jpexs.decompiler.flash.iggy.streams;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author JPEXS
 */
public class RandomAccessFileDataStream extends AbstractDataStream {

    private File file;
    private RandomAccessFile raf;

    protected File getFile() {
        return file;
    }

    public RandomAccessFileDataStream(File file) throws FileNotFoundException {
        this.file = file;
        raf = new RandomAccessFile(file, "rw");
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
    public void seek(long pos, SeekMode mode) throws IOException {
        long newpos = pos;
        if (mode == SeekMode.CUR) {
            newpos = raf.getFilePointer() + pos;
        } else if (mode == SeekMode.END) {
            newpos = raf.length() - pos;
        }
        if (newpos > raf.length()) {
            throw new ArrayIndexOutOfBoundsException("Position outside bounds accessed: " + pos + ". Size: " + raf.length());
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

}
