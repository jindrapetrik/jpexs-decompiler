/*
 * 11/19/04  1.0 moved to LGPL.
 *
 * 11/17/04     Uncomplete frames discarded. E.B, javalayer@javazoom.net
 *
 * 12/05/03     ID3v2 tag returned. E.B, javalayer@javazoom.net
 *
 * 12/12/99     Based on Ibitstream. Exceptions thrown on errors,
 *             Temporary removed seek functionality. mdm@techie.com
 *
 * 02/12/99 : Java Conversion by E.B , javalayer@javazoom.net
 *
 * 04/14/97 : Added function prototypes for new syncing and seeking
 * mechanisms. Also made this file portable. Changes made by Jeff Tsay
 *
 *  @(#) ibitstream.h 1.5, last edit: 6/15/94 16:55:34
 *  @(#) Copyright (C) 1993, 1994 Tobias Bading (bading@cs.tu-berlin.de)
 *  @(#) Berlin University of Technology
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.decoder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;


/**
 * The <code>Bistream</code> class is responsible for parsing
 * an MPEG audio bitstream.
 *
 * <b>REVIEW:</b> much of the parsing currently occurs in the
 * various decoders. This should be moved into this class and associated
 * inner classes.
 */
public final class Bitstream implements BitstreamErrors {

    /**
     * Synchronization control constant for the initial
     * synchronization to the start of a frame.
     */
    static byte INITIAL_SYNC = 0;

    /**
     * Synchronization control constant for non-initial frame
     * synchronizations.
     */
    static byte STRICT_SYNC = 1;

    /**
     * Maximum size of the frame buffer.
     * <p>
     * max. 1730 bytes per frame: 144 * 384kbit/s / 32000 Hz + 2 Bytes CRC
     */
    private static final int BUFFER_INT_SIZE = 433;

    /**
     * The frame buffer that holds the data for the current frame.
     */
    private final int[] framebuffer = new int[BUFFER_INT_SIZE];

    /**
     * Number of valid bytes in the frame buffer.
     */
    private int framesize;

    /**
     * The bytes read from the stream.
     */
    private byte[] frame_bytes = new byte[BUFFER_INT_SIZE * 4];

    /**
     * Index into <code>framebuffer</code> where the next bits are
     * retrieved.
     */
    private int wordpointer;

    /**
     * Number (0-31, from MSB to LSB) of next bit for get_bits()
     */
    private int bitindex;

    /**
     * The current specified syncword
     */
    private int syncword;

    /**
     * Audio header position in stream.
     */
    private int header_pos = 0;

    /**
     *
     */
    private boolean single_ch_mode;

    private final int[] bitmask = {
            0, // dummy
            0x00000001, 0x00000003, 0x00000007, 0x0000000F, 0x0000001F, 0x0000003F, 0x0000007F, 0x000000FF, 0x000001FF, 0x000003FF,
            0x000007FF, 0x00000FFF, 0x00001FFF, 0x00003FFF, 0x00007FFF, 0x0000FFFF, 0x0001FFFF
    };

    private final PushbackInputStream source;

    private final Header header = new Header();

    private final byte[] syncBuf = new byte[4];

    private Crc16[] crc = new Crc16[1];

    private byte[] rawid3v2 = null;

    private boolean firstframe = true;

    /**
     * Construct a IBitstream that reads data from a
     * given InputStream.
     *
     * @param in The InputStream to read from.
     */
    public Bitstream(InputStream in) {
        if (in == null)
            throw new NullPointerException("in");
        in = new BufferedInputStream(in);
        loadID3v2(in);
        firstframe = true;
        source = new PushbackInputStream(in, BUFFER_INT_SIZE * 4);

        closeFrame();
    }

    /**
     * Return position of the first audio header.
     *
     * @return size of ID3v2 tag frames.
     */
    public int header_pos() {
        return header_pos;
    }

    /**
     * Load ID3v2 frames.
     *
     * @param in MP3 InputStream.
     * @author JavaZOOM
     */
    private void loadID3v2(InputStream in) {
        int size = -1;
        try {
            // Read ID3v2 header (10 bytes).
            in.mark(10);
            size = readID3v2Header(in);
            header_pos = size;
        } catch (IOException e) {
        } finally {
            try {
                // Unread ID3v2 header (10 bytes).
                in.reset();
            } catch (IOException e) {
            }
        }
        // Load ID3v2 tags.
        try {
            if (size > 0) {
                rawid3v2 = new byte[size];
                in.read(rawid3v2, 0, rawid3v2.length);
            }
        } catch (IOException e) {
        }
    }

    /**
     * Parse ID3v2 tag header to find out size of ID3v2 frames.
     *
     * @param in MP3 InputStream
     * @return size of ID3v2 frames + header
     * @throws IOException
     * @author JavaZOOM
     */
    private int readID3v2Header(InputStream in) throws IOException {
        byte[] id3header = new byte[4];
        int size = -10;
        in.read(id3header, 0, 3);
        // Look for ID3v2
        if ((id3header[0] == 'I') && (id3header[1] == 'D') && (id3header[2] == '3')) {
            in.read(id3header, 0, 3);
            @SuppressWarnings("unused")
            int majorVersion = id3header[0];
            @SuppressWarnings("unused")
            int revision = id3header[1];
            in.read(id3header, 0, 4);
            size = (id3header[0] << 21) + (id3header[1] << 14) + (id3header[2] << 7) + (id3header[3]);
        }
        return (size + 10);
    }

    /**
     * Return raw ID3v2 frames + header.
     *
     * @return ID3v2 InputStream or null if ID3v2 frames are not available.
     */
    public InputStream getRawID3v2() {
        if (rawid3v2 == null)
            return null;
        else {
            ByteArrayInputStream bain = new ByteArrayInputStream(rawid3v2);
            return bain;
        }
    }

    /**
     * Close the Bitstream.
     *
     * @throws BitstreamException
     */
    public void close() throws BitstreamException {
        try {
            source.close();
        } catch (IOException ex) {
            throw newBitstreamException(STREAM_ERROR, ex);
        }
    }

    /**
     * Reads and parses the next frame from the input source.
     *
     * @return the Header describing details of the frame read,
     * or null if the end of the stream has been reached.
     */
    public Header readFrame() throws BitstreamException {
        Header result = null;
        try {
            result = readNextFrame();
            // E.B, Parse VBR (if any) first frame.
            if (firstframe) {
                result.parseVBR(frame_bytes);
                firstframe = false;
            }
        } catch (BitstreamException ex) {
            if ((ex.getErrorCode() == INVALIDFRAME)) {
                // Try to skip this frame.
                //System.out.println("INVALIDFRAME");
                try {
                    closeFrame();
                    result = readNextFrame();
                } catch (BitstreamException e) {
                    if ((e.getErrorCode() != STREAM_EOF)) {
                        // wrap original exception so stack trace is maintained.
                        throw newBitstreamException(e.getErrorCode(), e);
                    }
                }
            } else if ((ex.getErrorCode() != STREAM_EOF)) {
                // wrap original exception so stack trace is maintained.
                throw newBitstreamException(ex.getErrorCode(), ex);
            }
        }
        return result;
    }

    /**
     * Read next MP3 frame.
     *
     * @return MP3 frame header.
     * @throws BitstreamException
     */
    private Header readNextFrame() throws BitstreamException {
        if (framesize == -1) {
            nextFrame();
        }
        return header;
    }

    /**
     * Read next MP3 frame.
     *
     * @throws BitstreamException
     */
    private void nextFrame() throws BitstreamException {
        // entire frame is read by the header class.
        header.read_header(this, crc);
    }

    /**
     * Unreads the bytes read from the frame.
     *
     * @throws BitstreamException
     */
    // REVIEW: add new error codes for this.
    public void unreadFrame() throws BitstreamException {
        if (wordpointer == -1 && bitindex == -1 && (framesize > 0)) {
            try {
                source.unread(frame_bytes, 0, framesize);
            } catch (IOException ex) {
                throw newBitstreamException(STREAM_ERROR);
            }
        }
    }

    /**
     * Close MP3 frame.
     */
    public void closeFrame() {
        framesize = -1;
        wordpointer = -1;
        bitindex = -1;
    }

    /**
     * Determines if the next 4 bytes of the stream represent a
     * frame header.
     */
    public boolean isSyncCurrentPosition(int syncmode) throws BitstreamException {
        int read = readBytes(syncBuf, 0, 4);
        int headerString = ((syncBuf[0] << 24) & 0xFF000000) | ((syncBuf[1] << 16) & 0x00FF0000)
                | ((syncBuf[2] << 8) & 0x0000FF00) | ((syncBuf[3] << 0) & 0x000000FF);

        try {
            source.unread(syncBuf, 0, read);
        } catch (IOException ex) {
        }

        boolean sync = false;
        switch (read) {
        case 0:
            sync = true;
            break;
        case 4:
            sync = isSyncMark(headerString, syncmode, syncword);
            break;
        }

        return sync;
    }

    // REVIEW: this class should provide inner classes to
    // parse the frame contents. Eventually, readBits will
    // be removed.
    public int readBits(int n) {
        return get_bits(n);
    }

    public int readCheckedBits(int n) {
        // REVIEW: implement CRC check.
        return get_bits(n);
    }

    BitstreamException newBitstreamException(int errorcode) {
        return new BitstreamException(errorcode, null);
    }

    BitstreamException newBitstreamException(int errorcode, Throwable throwable) {
        return new BitstreamException(errorcode, throwable);
    }

    /**
     * Get next 32 bits from bitstream.
     * They are stored in the headerString.
     * syncMode allows Synchro flag ID
     * The returned value is False at the end of stream.
     */
    int syncHeader(byte syncMode) throws BitstreamException {
        boolean sync;
        int headerString;
        // read additional 2 bytes
        int bytesRead = readBytes(syncBuf, 0, 3);

        if (bytesRead != 3)
            throw newBitstreamException(STREAM_EOF, null);

        headerString = ((syncBuf[0] << 16) & 0x00FF0000) | ((syncBuf[1] << 8) & 0x0000FF00) | ((syncBuf[2] << 0) & 0x000000FF);

        do {
            headerString <<= 8;

            if (readBytes(syncBuf, 3, 1) != 1)
                throw newBitstreamException(STREAM_EOF, null);

            headerString |= (syncBuf[3] & 0x000000FF);

            sync = isSyncMark(headerString, syncMode, syncword);
        } while (!sync);

        return headerString;
    }

    public boolean isSyncMark(int headerstring, int syncmode, int word) {
        boolean sync = false;

        if (syncmode == INITIAL_SYNC) {
            //sync =  ((headerstring & 0xFFF00000) == 0xFFF00000);
            sync = ((headerstring & 0xFFE00000) == 0xFFE00000); // SZD: MPEG 2.5
        } else {
            sync = ((headerstring & 0xFFF80C00) == word) && (((headerstring & 0x000000C0) == 0x000000C0) == single_ch_mode);
        }

        // filter out invalid sample rate
        if (sync)
            sync = (((headerstring >>> 10) & 3) != 3);
        // filter out invalid layer
        if (sync)
            sync = (((headerstring >>> 17) & 3) != 0);
        // filter out invalid version
        if (sync)
            sync = (((headerstring >>> 19) & 3) != 1);

        return sync;
    }

    /**
     * Reads the data for the next frame. The frame is not parsed
     * until parse frame is called.
     */
    int read_frame_data(int bytesize) throws BitstreamException {
        int numread = 0;
        numread = readFully(frame_bytes, 0, bytesize);
        framesize = bytesize;
        wordpointer = -1;
        bitindex = -1;
        return numread;
    }

    /**
     * Parses the data previously read with read_frame_data().
     */
    void parse_frame() throws BitstreamException {
        // Convert Bytes read to int
        int b = 0;
        byte[] byteread = frame_bytes;
        int bytesize = framesize;

        // Check ID3v1 TAG (True only if last frame).

        for (int k = 0; k < bytesize; k = k + 4) {
            @SuppressWarnings("unused")
            int convert = 0;
            byte b0 = 0;
            byte b1 = 0;
            byte b2 = 0;
            byte b3 = 0;
            b0 = byteread[k];
            if (k + 1 < bytesize)
                b1 = byteread[k + 1];
            if (k + 2 < bytesize)
                b2 = byteread[k + 2];
            if (k + 3 < bytesize)
                b3 = byteread[k + 3];
            framebuffer[b++] = ((b0 << 24) & 0xFF000000) | ((b1 << 16) & 0x00FF0000) | ((b2 << 8) & 0x0000FF00)
                    | (b3 & 0x000000FF);
        }
        wordpointer = 0;
        bitindex = 0;
    }

    /**
     * Read bits from buffer into the lower bits of an unsigned int.
     * The LSB contains the latest read bit of the stream.
     * (1 <= number_of_bits <= 16)
     */
    public int get_bits(int number_of_bits) {
        int returnvalue = 0;
        int sum = bitindex + number_of_bits;

        // E.B
        // There is a problem here, wordpointer could be -1 ?!
        if (wordpointer < 0)
            wordpointer = 0;
        // E.B : End.

        if (sum <= 32) {
            // all bits contained in *wordpointer
            returnvalue = (framebuffer[wordpointer] >>> (32 - sum)) & bitmask[number_of_bits];
            if ((bitindex += number_of_bits) == 32) {
                bitindex = 0;
                wordpointer++; // added by me!
            }
            return returnvalue;
        }

        int Right = (framebuffer[wordpointer] & 0x0000FFFF);
        wordpointer++;
        int Left = (framebuffer[wordpointer] & 0xFFFF0000);
        returnvalue = ((Right << 16) & 0xFFFF0000) | ((Left >>> 16) & 0x0000FFFF);

        returnvalue >>>= 48 - sum;
        returnvalue &= bitmask[number_of_bits];
        bitindex = sum - 32;
        return returnvalue;
    }

    /**
     * Set the word we want to sync the header to.
     * In Big-Endian byte order
     */
    void set_syncword(int syncword0) {
        syncword = syncword0 & 0xFFFFFF3F;
        single_ch_mode = ((syncword0 & 0x000000C0) == 0x000000C0);
    }

    /**
     * Reads the exact number of bytes from the source
     * input stream into a byte array.
     *
     * @param b    The byte array to read the specified number
     *             of bytes into.
     * @param offs The index in the array where the first byte
     *             read should be stored.
     * @param len  the number of bytes to read.
     * @throws BitstreamException is thrown if the specified
     *                            number of bytes could not be read from the stream.
     */
    private int readFully(byte[] b, int offs, int len) throws BitstreamException {
        int nRead = 0;
        try {
            while (len > 0) {
                int bytesread = source.read(b, offs, len);
                if (bytesread == -1) {
                    while (len-- > 0) {
                        b[offs++] = 0;
                    }
                    break;
                    //throw newBitstreamException(UNEXPECTED_EOF, new EOFException());
                }
                nRead = nRead + bytesread;
                offs += bytesread;
                len -= bytesread;
            }
        } catch (IOException ex) {
            throw newBitstreamException(STREAM_ERROR, ex);
        }
        return nRead;
    }

    /**
     * Similar to readFully, but doesn't throw exception when
     * EOF is reached.
     */
    private int readBytes(byte[] b, int offs, int len) throws BitstreamException {
        int totalBytesRead = 0;
        try {
            while (len > 0) {
                int bytesread = source.read(b, offs, len);
                if (bytesread == -1) {
                    break;
                }
                totalBytesRead += bytesread;
                offs += bytesread;
                len -= bytesread;
            }
        } catch (IOException ex) {
            throw newBitstreamException(STREAM_ERROR, ex);
        }
        return totalBytesRead;
    }
}
