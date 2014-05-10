/*
 * @(#)RIFFPrimitivesInputStream.java  1.0  2005-01-15
 *
 * Copyright (c) 2005 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.riff;

import java.io.*;

/**
 * A RIFF primitives input stream lets an application read primitive data
 * types in the Microsoft Resource Interfache File Format (RIFF) format from an
 * underlying input stream.
 *
 * Reference:
 * AVI RIFF File Reference
 * http://msdn.microsoft.com/archive/default.asp?url=/archive/en-us/directx9_c/directx/htm/avirifffilereference.asp
 *
 * @author	Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0 2005-01-15 Created.
 */
public class RIFFPrimitivesInputStream extends FilterInputStream {
    private long scan, mark;
    
    /**
     * Creates a new instance.
     *
     * @param  in   the input stream.
     */
    public RIFFPrimitivesInputStream(InputStream in) {
        super(in);
    }
    
    /**
     * Read 1 byte from the input stream and interpret
     * them as an 8 Bit unsigned UBYTE value.
     */
    public int readUBYTE()
    throws IOException {
        int b0 = in.read();
        
        if (b0 == -1) {
            throw new EOFException();
        }

        scan += 1;
        return b0 & 0xff;
    }
    /**
     * Read 2 bytes from the input stream and interpret
     * them as a 16 Bit signed WORD value.
     */
    public short readWORD()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        
        if (b1 == -1) {
            throw new EOFException();
        }
        scan += 2;
        
        return (short) (((b0 & 0xff) << 0) | ((b1 & 0xff) << 8));
    }
    
    /**
     * Read 2 bytes from the input stream and interpret
     * them as a 16 Bit unsigned UWORD value.
     */
    public int readUWORD()
    throws IOException {
        return readWORD() & 0xffff;
    }
    
    /**
     * Read 4 bytes from the input stream and interpret
     * them as a 32 Bit signed LONG value.
     */
    public int readLONG()
    throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        
        if (b3 == -1) {
            throw new EOFException();
        }
        scan += 4;
        
        return ((b0&0xff) << 0) +
        ((b1&0xff) << 8) +
        ((b2&0xff) << 16) +
        ((b3&0xff) << 24);
    }
    
    /**
     * Read 4 bytes from the input stream and interpret
     * them as a four byte character code.
     *
     * Cited from Referenced "AVI RIFF File Reference":
     * "A FOURCC (four-character code) is a 32-bit unsigned integer created by
     * concatenating four ASCII characters. For example, the FOURCC 'abcd' is
     * represented on a Little-Endian system as 0x64636261. FOURCCs can contain
     * space characters, so ' abc' is a valid FOURCC. The AVI file format uses
     * FOURCC codes to identify stream types, data chunks, index entries, and
     * other information."
     */
    public int readFourCC()
    throws IOException {
        int b3 = in.read();
        int b2 = in.read();
        int b1 = in.read();
        int b0 = in.read();

        if (b0 == -1) {
            throw new EOFException();
        }
        scan += 4;

        return ((b0&0xff) << 0) +
        ((b1&0xff) << 8) +
        ((b2&0xff) << 16) +
        ((b3&0xff) << 24);
    }
    /**
     * Read 4 bytes from the input stream and interpret
     * them as a four byte character code.
     *
     * Cited from Referenced "AVI RIFF File Reference":
     * "A FOURCC (four-character code) is a 32-bit unsigned integer created by
     * concatenating four ASCII characters. For example, the FOURCC 'abcd' is
     * represented on a Little-Endian system as 0x64636261. FOURCCs can contain
     * space characters, so ' abc' is a valid FOURCC. The AVI file format uses
     * FOURCC codes to identify stream types, data chunks, index entries, and
     * other information."
     */
    public String readFourCCString()
    throws IOException {
        byte[] buf = new byte[4];
        readFully(buf, 0, 4);
        //scan += 4; <- scan is updated by method readFully
        return new String(buf, "ASCII");
    }
    
    /**
     * Read 4 Bytes from the input Stream and interpret
     * them as an unsigned Integer value of type ULONG.
     */
    public long readULONG()
    throws IOException {
        return (long)(readLONG()) & 0x00ffffffff;
    }
    
    /**
     * Align to an even byte position in the input stream.
     * This will skip one byte in the stream if the current
     * read position is not even.
     */
    public void align()
    throws IOException {
        if (scan % 2 == 1) {
            in.skip(1);
            scan++;
        }
    }
    
    /**
     * Get the current read position within the file (as seen
     * by this input stream filter).
     */
    public long getScan()
    { return scan; }
    
    /**
     * Reads one byte.
     */
    public int read()
    throws IOException {
        int data = in.read();
        if (data != -1) scan++;
        return data;
    }
    /**
     * Reads a sequence of bytes.
     */
    public int readFully(byte[] b,int offset, int length)
    throws IOException {
        int count = read(b, offset, length);
        if (count != length) {
            throw new EOFException("readFully for "+length+" bytes, unexpected EOF after "+count+" bytes.");
            }
        //scan += count; <- scan is already counted by read method
        return count;
    }
    /**
     * Reads a sequence of bytes.
     */
    public int read(byte[] b,int offset, int length)
    throws IOException {
        int count = 0;
        while (count < length) {
            int result = in.read(b,offset+count,length-count);
            if (result == -1) break;
            count += result;
        }
        scan += count;
        return count;
    }
    /**
     * Marks the input stream.
     * @param	readlimit	The maximum limit of bytes that can be read before
     * the mark position becomes invalid.
     */
    public void mark(int readlimit) {
        in.mark(readlimit);
        mark = scan;
    }
    /**
     * Repositions the stream at the previously marked position.
     *
     * @exception  IOException  If the stream has not been marked or if the
     * mark has been invalidated.
     */
    public void reset()
    throws IOException {
        in.reset();
        scan = mark;
    }
    /**
     * Skips over and discards n bytes of data from this input stream. This skip
     * method tries to skip the provided number of bytes.
     */
    public long skip(long n)
    throws IOException {
        long skipped = in.skip(n);
        scan += skipped;
        return skipped;
    }
    /**
     * Skips over and discards n bytes of data from this input stream. Throws
     *
     * @param      n   the number of bytes to be skipped.
     * @exception  EOFException  if this input stream reaches the end before
     *               skipping all the bytes.
     */
    public void skipFully(long n)
    throws IOException {
        if (n==0) return;

        int total = 0;
        int cur = 0;
        
        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }
        if (cur == 0) throw new EOFException();
        scan += total;
    }
}