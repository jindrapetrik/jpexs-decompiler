/*
 * @(#)JFIFInputStream.java  
 * 
 * Copyright (c) 2008-2012 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.jpeg;

import java.io.*;
import java.util.*;

/**
 * JFIFInputStream.
 * <p>
 * This InputStream uses two special marker values which do not exist
 * in the JFIF stream:
 * <ul>
 * <li><b>-1</b>: marks junk data at the beginning of the file.</li>
 * <li><b>0</b>: marks entropy encoded image data.</li>
 * </ul>
 * <p>
 * The junk data at the beginning of the file can be accessed by calling the
 * read-methods immediately after opening the stream. Call nextSegment()
 * immediately after opening the stream if you are not interested into this
 * junk data.
 * <p>
 * Junk data at the end of the file is delivered as part of the EOI_MARKER segment.
 * Finish reading after encountering the EOI_MARKER segment if you are not interested
 * in this junk data.
 *
 * <p>
 * References:<br>
 * JPEG File Interchange Format Version 1.02<br>
 * <a href="http://www.jpeg.org/public/jfif.pdf">http://www.jpeg.org/public/jfif.pdf</a>
 * <p>
 *   Pennebaker, W., Mitchell, J. (1993).<br>
 *   JPEG Still Image Data Compression Standard.<br>
 *   Chapmann & Hall, New York.<br>
 *   ISBN 0-442-01272-1<br>
 *
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau
 * @version $Id: JFIFInputStream.java 299 2013-01-03 07:40:18Z werner $
 */
public class JFIFInputStream extends FilterInputStream {

    /**
     * This hash set holds the Id's of markers which stand alone,
     * respectively do no have a data segment.
     */
    private final HashSet<Integer> standaloneMarkers = new HashSet<Integer>();
    /** 
     * This hash set holds the Id's of markers which have a data
     * segment followed by a entropy-coded data segment.
     */
    private final HashSet<Integer> doubleSegMarkers = new HashSet<Integer>();

    /** Represents a segment within a JFIF File. 
     */
    public static class Segment {

        /**
         * Holds the marker code.
         * A marker is an unsigned short between 0xff01 and 0xfffe.
         */
        public final int marker;
        /**
         * Holds the offset of the first data byte to the beginning
         * of the stream.
         */
        public final long offset;
        /**
         * If the marker starts a marker segment, holds the length
         * of the data in the data segment.
         * If the marker starts a entropy-coded data segment, holds
         * the value -1.
         */
        public final int length;

        public Segment(int marker, long offset, int length) {
            this.marker = marker;
            this.offset = offset;
            this.length = length;
        }

        public boolean isEntropyCoded() {
            return length == -1;
        }

        @Override
        public String toString() {
            return "Segment marker=0x" + Integer.toHexString(marker) + " offset=" + offset + "=0x" + Long.toHexString(offset);
        }
    }
    private Segment segment;
    /** 
     * This variable is set to true, if a 0xff byte has been found in 
     * entropy-code data.
     */
    private boolean markerFound;
    private int marker = JUNK_MARKER;
    private long offset = 0;
    private boolean isStuffed0xff = false;
    /** JUNK_MARKER Marker (for data which is not part of the JFIF stream. */
    public final static int JUNK_MARKER = -1;
    /** Start of image */
    public final static int SOI_MARKER = 0xffd8;
    /** End of image */
    public final static int EOI_MARKER = 0xffd9;
    /** Temporary private use in arithmetic coding */
    public final static int TEM_MARKER = 0xff01;
    /** Start of scan */
    public final static int SOS_MARKER = 0xffda;
    /** APP1_MARKER Reserved for application use */
    public final static int APP1_MARKER = 0xffe1;
    /** APP2_MARKER Reserved for application use */
    public final static int APP2_MARKER = 0xffe2;
    /** Reserved for JPEG extensions */
    public final static int JPG0_MARKER = 0xfff0;
    public final static int JPG1_MARKER = 0xfff1;
    public final static int JPG2_MARKER = 0xfff2;
    public final static int JPG3_MARKER = 0xfff3;
    public final static int JPG4_MARKER = 0xfff4;
    public final static int JPG5_MARKER = 0xfff5;
    public final static int JPG6_MARKER = 0xfff6;
    public final static int JPG7_MARKER = 0xfff7;
    public final static int JPG8_MARKER = 0xfff8;
    public final static int JPG9_MARKER = 0xfff9;
    public final static int JPGA_MARKER = 0xfffA;
    public final static int JPGB_MARKER = 0xfffB;
    public final static int JPGC_MARKER = 0xfffC;
    public final static int JPGD_MARKER = 0xfffD;
    /** Start of frame markers */
    public final static int SOF0_MARKER = 0xffc0;//nondifferential Huffman-coding frames with baseline DCT.
    public final static int SOF1_MARKER = 0xffc1;//nondifferential Huffman-coding frames with extended sequential DCT.
    public final static int SOF2_MARKER = 0xffc2;//nondifferential Huffman-coding frames with progressive DCT.
    public final static int SOF3_MARKER = 0xffc3;//nondifferential Huffman-coding frames with lossless (sequential) data.

    //public final static int SOF4_MARKER = 0xffc4;//
    public final static int SOF5_MARKER = 0xffc5;//differential Huffman-coding frames with differential sequential DCT.
    public final static int SOF6_MARKER = 0xffc6;//differential Huffman-coding frames with differential progressive DCT.
    public final static int SOF7_MARKER = 0xffc7;//differential Huffman-coding frames with differential lossless data.

    //public final static int SOF8_MARKER = 0xffc8;//
    public final static int SOF9_MARKER = 0xffc9;//nondifferential Arithmetic-coding frames with extended sequential DCT.
    public final static int SOFA_MARKER = 0xffcA;//nondifferential Arithmetic-coding frames with progressive DCT.
    public final static int SOFB_MARKER = 0xffcB;//nondifferential Arithmetic-coding frames with lossless (sequential) data.
    //public final static int SOFC_MARKER = 0xffcC;//
    public final static int SOFD_MARKER = 0xffcD;//differential Arithmetic-coding frames with differential sequential DCT.
    public final static int SOFE_MARKER = 0xffcE;//differential Arithmetic-coding frames with differential progressive DCT.
    public final static int SOFF_MARKER = 0xffcF;//differential Arithmetic-coding frames with differential lossless DCT.

    // Restart markers
    public final static int RST0_MARKER = 0xffd0;
    public final static int RST1_MARKER = 0xffd1;
    public final static int RST2_MARKER = 0xffd2;
    public final static int RST3_MARKER = 0xffd3;
    public final static int RST4_MARKER = 0xffd4;
    public final static int RST5_MARKER = 0xffd5;
    public final static int RST6_MARKER = 0xffd6;
    public final static int RST7_MARKER = 0xffd7;

    public JFIFInputStream(File f) throws IOException {
       this(new BufferedInputStream(new FileInputStream(f)));
    }

    public JFIFInputStream(InputStream in) {
        super(in);

        for (int i = RST0_MARKER; i <= RST7_MARKER; i++) {
            standaloneMarkers.add(i); // RST(i) Restart interval termination
        }
        standaloneMarkers.add(SOI_MARKER); // SOI_MARKER Start of image
        standaloneMarkers.add(EOI_MARKER); // EOI_MARKER End of image
        standaloneMarkers.add(TEM_MARKER); // TEM_MARKER Temporary private use in arithmetic coding
        standaloneMarkers.add(JPG0_MARKER); // JPEG Extensions
        standaloneMarkers.add(JPG1_MARKER);
        standaloneMarkers.add(JPG2_MARKER);
        standaloneMarkers.add(JPG3_MARKER);
        standaloneMarkers.add(JPG4_MARKER);
        standaloneMarkers.add(JPG5_MARKER);
        standaloneMarkers.add(JPG6_MARKER);
        standaloneMarkers.add(JPG7_MARKER);
        standaloneMarkers.add(JPG8_MARKER);
        standaloneMarkers.add(JPG9_MARKER);
        standaloneMarkers.add(JPGA_MARKER);
        standaloneMarkers.add(JPGB_MARKER);
        standaloneMarkers.add(JPGC_MARKER);
        standaloneMarkers.add(JPGD_MARKER);
        standaloneMarkers.add(0xffff); // Illegal marker
        doubleSegMarkers.add(SOS_MARKER); // SOS_MARKER Start of Scan

        // Start with a dummy entropy-coded data segment.
        segment = new Segment(-1, 0, -1);
    }

    /**
     * Gets the current segment from the input stream.
     *
     * @return The current segment. Returns null, if we encountered
     * the end of the stream.
     * @throws java.io.IOException
     */
    public Segment getSegment() throws IOException {
        return segment;
    }

    /**
     * Gets the next segment from the input stream.
     * 
     * @return The next segment. Returns null, if we encountered
     * the end of the stream.
     * @throws java.io.IOException
     */
    public Segment getNextSegment() throws IOException {
        // If we are inside of a marker segment, skip the
        // marker
        if (!segment.isEntropyCoded()) {
            markerFound = false;
            do {
                long skipped = in.skip(segment.length - offset + segment.offset);
                if (skipped == -1) {
                    segment = new Segment(0, offset, -1);
                    return null;
                }
                offset += skipped;
            } while (offset < segment.length + segment.offset);

            if (doubleSegMarkers.contains(segment.marker)) {
                segment = new Segment(0, offset, -1);
                return segment;
            }
        }

        // Scan the input stream for the next marker.
        while (!markerFound) {
            while (true) {
                int b;
                if (isStuffed0xff) {
                    b = 0xff;
                    isStuffed0xff = false;
                } else {
                    b = read0();
                }
                if (b == -1) {
                    return null;
                }
                if (b == 0xff) {
                    markerFound = true;
                    break;
                }
            }
            int b = read0();
            if (b == -1) {
                return null;
            }
            if (b == 0x00) {
                markerFound = false;
            } else if (b == 0xff) {
                isStuffed0xff = true;
                markerFound = false;
            } else {
                marker = 0xff00 | b;
            }
        }
        markerFound = false;
        /*
        if (marker <= 0xff00 || marker >= 0xffff) {
        throw new IOException("JFIFInputStream found illegal marker " + Integer.toHexString(marker) + " at offset " + offset + " 0x"+Long.toHexString(offset)+".");
        }*/

        // Note: 0xffff is an illegal marker segment, we process it here
        // for robustness.
        if (standaloneMarkers.contains(marker)) {
            segment = new Segment(0xff00 | marker, offset, -1);
        } else {
            int length = (read0() << 8) | read0();
            if (length < 2) {
                throw new IOException("JFIFInputStream found illegal segment length " + length + " after marker " + Integer.toHexString(marker) + " at offset " + offset + ".");
            }
            segment = new Segment(0xff00 | marker, offset, length - 2);
        }
        return segment;
    }

    public long getStreamPosition() {
        return offset;
    }

    private int read0() throws IOException {
        int b = in.read();
        if (b != -1) {
            offset++;
        }
        return b;
    }

    /**
     * Reads the next byte of data from this input stream. The value 
     * byte is returned as an <code>int</code> in the range 
     * <code>0</code> to <code>255</code>. If no byte is available 
     * because the end of the stream has been reached, the value 
     * <code>-1</code> is returned. This method blocks until input data 
     * is available, the end of the stream is detected, or an exception 
     * is thrown. 
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    @Override
    public int read() throws IOException {
        if (markerFound) {
            return -1;
        }

        int b;
        if (isStuffed0xff) {
            isStuffed0xff = false;
            b = 0xff;
        } else {
            b = read0();
        }

        if (segment.isEntropyCoded()) {
            if (b == 0xff) {
                b = read0();
                if (b == 0x00) {
                    // found a stuffed 0xff byte
                    return 0xff;
                } else if (b == 0xff) {
                    // found an invalid sequence of two 0xff bytes
                    isStuffed0xff = true;
                    return 0xff;
                }
                markerFound = true;
                marker = 0xff00 | b;
                return -1;
            }
        }
        return b;
    }

    /**
     * Reads up to <code>len</code> b of data from this input stream 
     * into an array of b. This method blocks until some input is 
     * available. 
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code> 
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of b read.
     * @return     the total number of b read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        if (markerFound) {
            return -1;
        }

        int count = 0;
        if (segment.isEntropyCoded()) {
            for (; count < len; count++) {
                int data = read();
                if (data == -1) {
                    if (count==0) return -1;
                    break;
                }

                b[off + count] = (byte) data;
            }
        } else {
            long available = segment.length - offset + segment.offset;
            if (available <= 0) {
                return -1;
            }
            if (available < len) {
                len = (int) available;
            }
            count = in.read(b, off, len);
            if (count != -1) {
                offset += count;
            }
        }
        return count;
    }

    /** Fully skips the specified number of bytes. */
    public final void skipFully(long n) throws IOException {
        long total = 0;
        long cur = 0;

        while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
            total += cur;
        }
        offset+=total;
        if (total < n) {
            throw new EOFException();
        }
    }

    /**
     * Skips over and discards <code>n</code> b of data from the 
     * input stream. The <code>skip</code> method may, for a variety of 
     * reasons, end up skipping over some smaller number of b, 
     * possibly <code>0</code>. The actual number of b skipped is 
     * returned. 
     * <p>
     * This method
     * simply performs <code>in.skip(n)</code>.
     *
     * @param      n   the number of b to be skipped.
     * @return     the actual number of b skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public long skip(long n) throws IOException {
        if (markerFound) {
            return -1;
        }

        long count = 0;
        if (segment.isEntropyCoded()) {
            for (; count < n; count++) {
                int data = read();
                if (data == -1) {
                    break;
                }
            }
        } else {
            long available = segment.length - offset + segment.offset;
            if (available < n) {
                n = (int) available;
            }
            count = in.skip(n);
            if (count != -1) {
                offset += count;
            }
        }
        return count;
    }

    /**
     * Marks the current position in this input stream. A subsequent 
     * call to the <code>reset</code> method repositions this stream at 
     * the last marked position so that subsequent reads re-read the same b.
     * <p>
     * The <code>readlimit</code> argument tells this input stream to 
     * allow that many b to be read before the mark position gets 
     * invalidated. 
     * <p>
     * This method simply performs <code>in.mark(readlimit)</code>.
     *
     * @param   readlimit   the maximum limit of b that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.FilterInputStream#in
     * @see     java.io.FilterInputStream#reset()
     */
    @Override
    public synchronized void mark(int readlimit) {
        // do nothing, since we don't support marking
    }

    /**
     * Repositions this stream to the position at the time the 
     * <code>mark</code> method was last called on this input stream. 
     * <p>
     * This method
     * simply performs <code>in.reset()</code>.
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parse, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails.
     * If this happens within readlimit b, it allows the outer
     * code to reset the stream and try another parser.
     *
     * @exception  IOException  if the stream has not been marked or if the
     *               mark has been invalidated.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.FilterInputStream#mark(int)
     */
    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("Reset not supported");
    }

    /**
     * Tests if this input stream supports the <code>mark</code> 
     * and <code>reset</code> methods. 
     * This method
     * simply performs <code>in.markSupported()</code>.
     *
     * @return  <code>true</code> if this stream type supports the
     *          <code>mark</code> and <code>reset</code> method;
     *          <code>false</code> otherwise.
     * @see     java.io.FilterInputStream#in
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    @Override
    public boolean markSupported() {
        return false;
    }
}
