package com.jpexs.proxy;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArray {

    public byte[] bytes;
    public int offset = 0;

    /**
     * Create a ByteArray with the default size.
     */
    public ByteArray() {
        this(512);
    }

    /**
     * Create a ByteArray with a specific default size.
     */
    public ByteArray(int size) {
        bytes = new byte[size];
    }

    /**
     * Create a ByteArray from a String.
     */
    public ByteArray(String s) {
        this(s.length());
        append(s);
    }

    /**
     * Create a ByteArray from an array of bytes.
     */
    public ByteArray(byte[] b) {
        this(b.length);
        append(b);
    }

    /**
     * Append a byte.
     */
    public void append(byte ch) {
        if (offset == bytes.length) {
            byte[] tmpbytes = bytes;
            bytes = new byte[tmpbytes.length * 2];
            System.arraycopy(tmpbytes, 0, bytes, 0, offset);
        }
        bytes[offset++] = ch;
    }

    /**
     * Append a ByteArray.
     */
    public void append(ByteArray b) {
        if (bytes.length - offset < b.length()) {
            byte[] tmpbytes = bytes;
            bytes = new byte[tmpbytes.length + b.length()];
            System.arraycopy(tmpbytes, 0, bytes, 0, offset);
        }
        System.arraycopy(b.bytes, 0, bytes, offset, b.length());
        offset += b.length();
    }

    /**
     * Append an array of bytes.
     */
    public void append(byte[] b) {
        if (bytes.length - offset < b.length) {
            byte[] tmpbytes = bytes;
            bytes = new byte[tmpbytes.length + b.length];
            System.arraycopy(tmpbytes, 0, bytes, 0, offset);
        }
        System.arraycopy(b, 0, bytes, offset, b.length);
        offset += b.length;
    }

    /**
     * Append a String.
     */
    public void append(String s) {
        append(s.getBytes());
    }

    /**
     * Convert to String.
     */
    public String toString() {
        return new String(bytes, 0, offset);
    }

    /**
     * Return the bytes.
     */
    public byte[] getBytes() {
        return bytes;
    }

    public void writeTo(OutputStream out)
            throws IOException {
        out.write(bytes, 0, offset);
    }

    public byte get(int i) {
        return bytes[i];
    }

    /**
     * Return the number of bytes.
     */
    public int length() {
        return offset;
    }

    public void erase() {
        offset = 0;
    }

    public void chop() {
        chop(1);
    }

    public void chop(int i) {
        offset -= i;
        if (offset < 0) {
            offset = 0;
        }
    }
}
