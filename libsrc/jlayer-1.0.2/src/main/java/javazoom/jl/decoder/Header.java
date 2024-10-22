/*
 * 11/19/04 : 1.0 moved to LGPL.
 *            VBRI header support added, E.B javalayer@javazoom.net
 *
 * 12/04/03 : VBR (XING) header support added, E.B javalayer@javazoom.net
 *
 * 02/13/99 : Java Conversion by JavaZOOM , E.B javalayer@javazoom.net
 *
 * Declarations for MPEG header class
 * A few layer III, MPEG-2 LSF, and seeking modifications made by Jeff Tsay.
 * Last modified : 04/19/97
 *
 *  @(#) header.h 1.7, last edit: 6/15/94 16:55:33
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

/**
 * Class for extracting information from a frame header.
 */
public final class Header {

    public static final int[][] frequencies ={
            {22050, 24000, 16000, 1},
            {44100, 48000, 32000, 1},
            {11025, 12000, 8000, 1} // SZD: MPEG25
    };

    /**
     * Constant for MPEG-2 LSF version
     */
    public static final int MPEG2_LSF = 0;
    public static final int MPEG25_LSF = 2; // SZD

    /**
     * Constant for MPEG-1 version
     */
    public static final int MPEG1 = 1;

    public static final int STEREO = 0;
    public static final int JOINT_STEREO = 1;
    public static final int DUAL_CHANNEL = 2;
    public static final int SINGLE_CHANNEL = 3;
    public static final int FOURTYFOUR_POINT_ONE = 0;
    public static final int FOURTYEIGHT = 1;
    public static final int THIRTYTWO = 2;

    private int h_layer, h_protection_bit, h_bitrate_index,
            h_padding_bit, h_mode_extension;
    private int h_version;
    private int h_mode;
    private int h_sample_frequency;
    private int h_number_of_subbands, h_intensity_stereo_bound;
    private boolean h_copyright, h_original;
    // VBR support added by E.B
    private static final double[] h_vbr_time_per_frame = {-1, 384, 1152, 1152};
    private boolean h_vbr;
    private int h_vbr_frames;
    private int h_vbr_scale;
    private int h_vbr_bytes;
    private byte[] h_vbr_toc;

    private byte syncmode = Bitstream.INITIAL_SYNC;
    private Crc16 crc;

    public short checksum;
    public int framesize;
    public int nSlots;

    private int _headerstring = -1; // E.B

    Header() {
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("Layer ");
        buffer.append(layer_string());
        buffer.append(" frame ");
        buffer.append(mode_string());
        buffer.append(' ');
        buffer.append(version_string());
        if (!checksums())
            buffer.append(" no");
        buffer.append(" checksums");
        buffer.append(' ');
        buffer.append(sample_frequency_string());
        buffer.append(',');
        buffer.append(' ');
        buffer.append(bitrate_string());

        return buffer.toString();
    }

    /**
     * Read a 32-bit header from the bitstream.
     */
    void read_header(Bitstream stream, Crc16[] crcp) throws BitstreamException {
        int headerString;
        int channelBitrate;
        boolean sync = false;
        do {
            headerString = stream.syncHeader(syncmode);
            _headerstring = headerString; // E.B
            if (syncmode == Bitstream.INITIAL_SYNC) {
                h_version = ((headerString >>> 19) & 1);
                if (((headerString >>> 20) & 1) == 0) // SZD: MPEG2.5 detection
                    if (h_version == MPEG2_LSF)
                        h_version = MPEG25_LSF;
                    else
                        throw stream.newBitstreamException(Bitstream.UNKNOWN_ERROR);
                if ((h_sample_frequency = ((headerString >>> 10) & 3)) == 3) {
                    throw stream.newBitstreamException(Bitstream.UNKNOWN_ERROR);
                }
            }
            h_layer = 4 - (headerString >>> 17) & 3;
            h_protection_bit = (headerString >>> 16) & 1;
            h_bitrate_index = (headerString >>> 12) & 0xF;
            h_padding_bit = (headerString >>> 9) & 1;
            h_mode = ((headerString >>> 6) & 3);
            h_mode_extension = (headerString >>> 4) & 3;
            if (h_mode == JOINT_STEREO)
                h_intensity_stereo_bound = (h_mode_extension << 2) + 4;
            else
                h_intensity_stereo_bound = 0; // should never be used
            if (((headerString >>> 3) & 1) == 1)
                h_copyright = true;
            if (((headerString >>> 2) & 1) == 1)
                h_original = true;
            // calculate number of subbands:
            if (h_layer == 1)
                h_number_of_subbands = 32;
            else {
                channelBitrate = h_bitrate_index;
                // calculate bitrate per channel:
                if (h_mode != SINGLE_CHANNEL)
                    if (channelBitrate == 4)
                        channelBitrate = 1;
                    else
                        channelBitrate -= 4;
                if ((channelBitrate == 1) || (channelBitrate == 2))
                    if (h_sample_frequency == THIRTYTWO)
                        h_number_of_subbands = 12;
                    else
                        h_number_of_subbands = 8;
                else if ((h_sample_frequency == FOURTYEIGHT) || ((channelBitrate >= 3) && (channelBitrate <= 5)))
                    h_number_of_subbands = 27;
                else
                    h_number_of_subbands = 30;
            }
            if (h_intensity_stereo_bound > h_number_of_subbands)
                h_intensity_stereo_bound = h_number_of_subbands;
            // calculate framesize and nSlots
            calculate_framesize();
            // read framedata:
            int framesizeloaded = stream.read_frame_data(framesize);
            if ((framesize >= 0) && (framesizeloaded != framesize)) {
                // Data loaded does not match to expected framesize,
                // it might be an ID3v1 TAG. (Fix 11/17/04).
                throw stream.newBitstreamException(Bitstream.INVALIDFRAME);
            }
            if (stream.isSyncCurrentPosition(syncmode)) {
                if (syncmode == Bitstream.INITIAL_SYNC) {
                    syncmode = Bitstream.STRICT_SYNC;
                    stream.set_syncword(headerString & 0xFFF80CC0);
                }
                sync = true;
            } else {
                stream.unreadFrame();
            }
        }
        while (!sync);
        stream.parse_frame();
        if (h_protection_bit == 0) {
            // frame contains a crc checksum
            checksum = (short) stream.get_bits(16);
            if (crc == null)
                crc = new Crc16();
            crc.add_bits(headerString, 16);
            crcp[0] = crc;
        } else
            crcp[0] = null;
        if (h_sample_frequency == FOURTYFOUR_POINT_ONE) {
        }
    }

    /**
     * Parse frame to extract optional VBR frame.
     *
     * @param firstFrame
     * @author E.B (javalayer@javazoom.net)
     */
    void parseVBR(byte[] firstFrame) throws BitstreamException {
        // Trying Xing header.
        String xing = "Xing";
        byte[] tmp = new byte[4];
        int offset = 0;
        // Compute "Xing" offset depending on MPEG version and channels.
        if (h_version == MPEG1) {
            if (h_mode == SINGLE_CHANNEL) offset = 21 - 4;
            else offset = 36 - 4;
        } else {
            if (h_mode == SINGLE_CHANNEL) offset = 13 - 4;
            else offset = 21 - 4;
        }
        try {
            System.arraycopy(firstFrame, offset, tmp, 0, 4);
            // Is "Xing" ?
            if (xing.equals(new String(tmp))) {
                //Yes.
                h_vbr = true;
                h_vbr_frames = -1;
                h_vbr_bytes = -1;
                h_vbr_scale = -1;
                h_vbr_toc = new byte[100];

                int length = 4;
                // Read flags.
                byte[] flags = new byte[4];
                System.arraycopy(firstFrame, offset + length, flags, 0, flags.length);
                length += flags.length;
                // Read number of frames (if available).
                if ((flags[3] & (byte) (1 << 0)) != 0) {
                    System.arraycopy(firstFrame, offset + length, tmp, 0, tmp.length);
                    h_vbr_frames = (tmp[0] << 24) & 0xFF000000 | (tmp[1] << 16) & 0x00FF0000 | (tmp[2] << 8) & 0x0000FF00 | tmp[3] & 0x000000FF;
                    length += 4;
                }
                // Read size (if available).
                if ((flags[3] & (byte) (1 << 1)) != 0) {
                    System.arraycopy(firstFrame, offset + length, tmp, 0, tmp.length);
                    h_vbr_bytes = (tmp[0] << 24) & 0xFF000000 | (tmp[1] << 16) & 0x00FF0000 | (tmp[2] << 8) & 0x0000FF00 | tmp[3] & 0x000000FF;
                    length += 4;
                }
                // Read TOC (if available).
                if ((flags[3] & (byte) (1 << 2)) != 0) {
                    System.arraycopy(firstFrame, offset + length, h_vbr_toc, 0, h_vbr_toc.length);
                    length += h_vbr_toc.length;
                }
                // Read scale (if available).
                if ((flags[3] & (byte) (1 << 3)) != 0) {
                    System.arraycopy(firstFrame, offset + length, tmp, 0, tmp.length);
                    h_vbr_scale = (tmp[0] << 24) & 0xFF000000 | (tmp[1] << 16) & 0x00FF0000 | (tmp[2] << 8) & 0x0000FF00 | tmp[3] & 0x000000FF;
                    length += 4;
                }
                //System.out.println("VBR:"+xing+" Frames:"+ h_vbr_frames +" Size:"+h_vbr_bytes);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new BitstreamException("XingVBRHeader Corrupted", e);
        }

        // Trying VBRI header.
        String vbri = "VBRI";
        offset = 36 - 4;
        try {
            System.arraycopy(firstFrame, offset, tmp, 0, 4);
            // Is "VBRI" ?
            if (vbri.equals(new String(tmp))) {
                //Yes.
                h_vbr = true;
                h_vbr_frames = -1;
                h_vbr_bytes = -1;
                h_vbr_scale = -1;
                h_vbr_toc = new byte[100];
                // Bytes.
                int length = 4 + 6;
                System.arraycopy(firstFrame, offset + length, tmp, 0, tmp.length);
                h_vbr_bytes = (tmp[0] << 24) & 0xFF000000 | (tmp[1] << 16) & 0x00FF0000 | (tmp[2] << 8) & 0x0000FF00 | tmp[3] & 0x000000FF;
                length += 4;
                // Frames.
                System.arraycopy(firstFrame, offset + length, tmp, 0, tmp.length);
                h_vbr_frames = (tmp[0] << 24) & 0xFF000000 | (tmp[1] << 16) & 0x00FF0000 | (tmp[2] << 8) & 0x0000FF00 | tmp[3] & 0x000000FF;
                length += 4;
                //System.out.println("VBR:"+vbri+" Frames:"+ h_vbr_frames +" Size:"+h_vbr_bytes);
                // TOC
                // TODO
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new BitstreamException("VBRIVBRHeader Corrupted", e);
        }
    }

    // Functions to query header contents:

    /**
     * Returns version.
     */
    public int version() {
        return h_version;
    }

    /**
     * Returns Layer ID.
     */
    public int layer() {
        return h_layer;
    }

    /**
     * Returns bitrate index.
     */
    public int bitrate_index() {
        return h_bitrate_index;
    }

    /**
     * Returns Sample Frequency.
     */
    public int sample_frequency() {
        return h_sample_frequency;
    }

    /**
     * Returns Frequency.
     */
    public int frequency() {
        return frequencies[h_version][h_sample_frequency];
    }

    /**
     * Returns Mode.
     */
    public int mode() {
        return h_mode;
    }

    /**
     * Returns Protection bit.
     */
    public boolean checksums() {
        return h_protection_bit == 0;
    }

    /**
     * Returns Copyright.
     */
    public boolean copyright() {
        return h_copyright;
    }

    /**
     * Returns Original.
     */
    public boolean original() {
        return h_original;
    }

    /**
     * Return VBR.
     *
     * @return true if VBR header is found
     */
    public boolean vbr() {
        return h_vbr;
    }

    /**
     * Return VBR scale.
     *
     * @return scale of -1 if not available
     */
    public int vbr_scale() {
        return h_vbr_scale;
    }

    /**
     * Return VBR TOC.
     *
     * @return vbr toc ot null if not available
     */
    public byte[] vbr_toc() {
        return h_vbr_toc;
    }

    /**
     * Returns Checksum flag.
     * Compares computed checksum with stream checksum.
     */
    public boolean checksum_ok() {
        return (checksum == crc.checksum());
    }

    // Seeking and layer III stuff

    /**
     * Returns Layer III Padding bit.
     */
    public boolean padding() {
        return h_padding_bit != 0;
    }

    /**
     * Returns Slots.
     */
    public int slots() {
        return nSlots;
    }

    /**
     * Returns Mode Extension.
     */
    public int mode_extension() {
        return h_mode_extension;
    }

    // E.B -> private to public
    public static final int[][][] bitrates = {
            {{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
            112000, 128000, 144000, 160000, 176000, 192000, 224000, 256000, 0},
            {0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
            56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
            {0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
            56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},

            {{0 /*free format*/, 32000, 64000, 96000, 128000, 160000, 192000,
            224000, 256000, 288000, 320000, 352000, 384000, 416000, 448000, 0},
            {0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
            112000, 128000, 160000, 192000, 224000, 256000, 320000, 384000, 0},
            {0 /*free format*/, 32000, 40000, 48000, 56000, 64000, 80000,
            96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, 0}},
            // SZD: MPEG2.5
            {{0 /*free format*/, 32000, 48000, 56000, 64000, 80000, 96000,
            112000, 128000, 144000, 160000, 176000, 192000, 224000, 256000, 0},
            {0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
            56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0},
            {0 /*free format*/, 8000, 16000, 24000, 32000, 40000, 48000,
            56000, 64000, 80000, 96000, 112000, 128000, 144000, 160000, 0}},
    };


    /**
     * Calculate Frame size.
     * Calculates framesize in bytes excluding header size.
     * <p>
     * E.B -> private to public
     */
    public int calculate_framesize() {

        if (h_layer == 1) {
            framesize = (12 * bitrates[h_version][0][h_bitrate_index]) /
                    frequencies[h_version][h_sample_frequency];
            if (h_padding_bit != 0) framesize++;
            framesize <<= 2; // one slot is 4 bytes long
            nSlots = 0;
        } else {
            framesize = (144 * bitrates[h_version][h_layer - 1][h_bitrate_index]) /
                    frequencies[h_version][h_sample_frequency];
            if (h_version == MPEG2_LSF || h_version == MPEG25_LSF) framesize >>= 1; // SZD
            if (h_padding_bit != 0) framesize++;
            // Layer III slots
            if (h_layer == 3) {
                if (h_version == MPEG1) {
                    nSlots = framesize - ((h_mode == SINGLE_CHANNEL) ? 17 : 32) // side info size
                            - ((h_protection_bit != 0) ? 0 : 2)                 // CRC size
                            - 4;                                                // header size
                } else {  // MPEG-2 LSF, SZD: MPEG-2.5 LSF
                    nSlots = framesize - ((h_mode == SINGLE_CHANNEL) ? 9 : 17) // side info size
                            - ((h_protection_bit != 0) ? 0 : 2)                // CRC size
                            - 4;                                               // header size
                }
            } else {
                nSlots = 0;
            }
        }
        framesize -= 4; // subtract header size
        return framesize;
    }

    /**
     * Returns the maximum number of frames in the stream.
     *
     * @param streamSize stream size
     * @return number of frames
     */
    public int max_number_of_frames(int streamSize) { // E.B
        if (h_vbr) return h_vbr_frames;
        else {
            if ((framesize + 4 - h_padding_bit) == 0) return 0;
            else return (streamSize / (framesize + 4 - h_padding_bit));
        }
    }

    /**
     * Returns the maximum number of frames in the stream.
     *
     * @param streamSize stream size
     * @return number of frames
     */
    public int min_number_of_frames(int streamSize) { // E.B
        if (h_vbr) return h_vbr_frames;
        else {
            if ((framesize + 5 - h_padding_bit) == 0) return 0;
            else return (streamSize / (framesize + 5 - h_padding_bit));
        }
    }

    /**
     * Returns ms/frame.
     *
     * @return milliseconds per frame
     */
    public float ms_per_frame() { // E.B
        if (h_vbr) {
            double tpf = h_vbr_time_per_frame[layer()] / frequency();
            if ((h_version == MPEG2_LSF) || (h_version == MPEG25_LSF)) tpf /= 2;
            return ((float) (tpf * 1000));
        } else {
            final float[][] ms_per_frame_array = {
                    {8.707483f, 8.0f, 12.0f},
                    {26.12245f, 24.0f, 36.0f},
                    {26.12245f, 24.0f, 36.0f}
            };
            return (ms_per_frame_array[h_layer - 1][h_sample_frequency]);
        }
    }

    /**
     * Returns total ms.
     *
     * @param streamSize stream size
     * @return total milliseconds
     */
    public float total_ms(int streamSize) { // E.B
        return (max_number_of_frames(streamSize) * ms_per_frame());
    }

    /**
     * Returns synchronized header.
     */
    public int getSyncHeader() { // E.B
        return _headerstring;
    }

    // functions which return header information as strings:

    /**
     * Return Layer version.
     */
    public String layer_string() {
        switch (h_layer) {
        case 1:
            return "I";
        case 2:
            return "II";
        case 3:
            return "III";
        }
        return null;
    }

    // E.B -> private to public
    public static final String[][][] bitrate_str = {
            {{"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s",
                    "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s",
                    "160 kbit/s", "176 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s",
                    "forbidden"},
            {"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s",
                    "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s",
                    "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s",
                    "forbidden"},
            {"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s",
                    "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s",
                    "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s",
                    "forbidden"}},

            {{"free format", "32 kbit/s", "64 kbit/s", "96 kbit/s", "128 kbit/s",
                    "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "288 kbit/s",
                    "320 kbit/s", "352 kbit/s", "384 kbit/s", "416 kbit/s", "448 kbit/s",
                    "forbidden"},
            {"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s",
                    "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "160 kbit/s",
                    "192 kbit/s", "224 kbit/s", "256 kbit/s", "320 kbit/s", "384 kbit/s",
                    "forbidden"},
            {"free format", "32 kbit/s", "40 kbit/s", "48 kbit/s", "56 kbit/s",
                    "64 kbit/s", "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s",
                    "160 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s", "320 kbit/s",
                    "forbidden"}},
            // SZD: MPEG2.5
            {{"free format", "32 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s",
                    "80 kbit/s", "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s",
                    "160 kbit/s", "176 kbit/s", "192 kbit/s", "224 kbit/s", "256 kbit/s",
                    "forbidden"},
            {"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s",
                    "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s",
                    "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s",
                    "forbidden"},
            {"free format", "8 kbit/s", "16 kbit/s", "24 kbit/s", "32 kbit/s",
                    "40 kbit/s", "48 kbit/s", "56 kbit/s", "64 kbit/s", "80 kbit/s",
                    "96 kbit/s", "112 kbit/s", "128 kbit/s", "144 kbit/s", "160 kbit/s",
                    "forbidden"}},
    };

    /**
     * Return Bitrate.
     *
     * @return bitrate in bps
     */
    public String bitrate_string() {
        if (h_vbr) {
            return bitrate() / 1000 + " kb/s";
        } else return bitrate_str[h_version][h_layer - 1][h_bitrate_index];
    }

    /**
     * Return Bitrate.
     *
     * @return bitrate in bps and average bitrate for VBR header
     */
    public int bitrate() {
        if (h_vbr) {
            return ((int) ((h_vbr_bytes * 8) / (ms_per_frame() * h_vbr_frames))) * 1000;
        } else return bitrates[h_version][h_layer - 1][h_bitrate_index];
    }

    /**
     * Return Instant Bitrate.
     * Bitrate for VBR is not constant.
     *
     * @return bitrate in bps
     */
    public int bitrate_instant() {
        return bitrates[h_version][h_layer - 1][h_bitrate_index];
    }

    /**
     * Returns Frequency
     *
     * @return frequency string in kHz
     */
    public String sample_frequency_string() {
        switch (h_sample_frequency) {
        case THIRTYTWO:
            if (h_version == MPEG1)
                return "32 kHz";
            else if (h_version == MPEG2_LSF)
                return "16 kHz";
            else    // SZD
                return "8 kHz";
        case FOURTYFOUR_POINT_ONE:
            if (h_version == MPEG1)
                return "44.1 kHz";
            else if (h_version == MPEG2_LSF)
                return "22.05 kHz";
            else    // SZD
                return "11.025 kHz";
        case FOURTYEIGHT:
            if (h_version == MPEG1)
                return "48 kHz";
            else if (h_version == MPEG2_LSF)
                return "24 kHz";
            else    // SZD
                return "12 kHz";
        }
        return null;
    }

    /**
     * Returns Mode.
     */
    public String mode_string() {
        switch (h_mode) {
        case STEREO:
            return "Stereo";
        case JOINT_STEREO:
            return "Joint stereo";
        case DUAL_CHANNEL:
            return "Dual channel";
        case SINGLE_CHANNEL:
            return "Single channel";
        }
        return null;
    }

    /**
     * Returns Version.
     *
     * @return MPEG-1 or MPEG-2 LSF or MPEG-2.5 LSF
     */
    public String version_string() {
        switch (h_version) {
        case MPEG1:
            return "MPEG-1";
        case MPEG2_LSF:
            return "MPEG-2 LSF";
        case MPEG25_LSF:    // SZD
            return "MPEG-2.5 LSF";
        }
        return null;
    }

    /**
     * Returns the number of subbands in the current frame.
     *
     * @return number of subbands
     */
    public int number_of_subbands() {
        return h_number_of_subbands;
    }

    /**
     * Returns Intensity Stereo.
     * (Layer II joint stereo only).
     * Returns the number of subbands which are in stereo mode,
     * subbands above that limit are in intensity stereo mode.
     *
     * @return intensity
     */
    public int intensity_stereo_bound() {
        return h_intensity_stereo_bound;
    }
}
