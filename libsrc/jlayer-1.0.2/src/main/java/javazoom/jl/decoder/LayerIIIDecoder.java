/*
 * 11/19/04     1.0 moved to LGPL.
 *
 * 18/06/01  Michael Scheerer,  Fixed bugs which causes
 *           negative indexes in method huffmann_decode and in method
 *           dequantize_sample.
 *
 * 16/07/01  Michael Scheerer, Catched a bug in method
 *           huffmann_decode, which causes an outOfIndexException.
 *           Cause : Indexnumber of 24 at SfBandIndex,
 *           which has only a length of 22. I have simply and dirty
 *           fixed the index to <= 22, because I'm not really be able
 *           to fix the bug. The Indexnumber is taken from the MP3
 *           file and the origin Ma-Player with the same code works
 *           well.
 *
 * 02/19/99  Java Conversion by E.B, javalayer@javazoom.net
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
 * Class Implementing Layer 3 Decoder.
 *
 * @since 0.0
 */
final class LayerIIIDecoder implements FrameDecoder {

    static final double d43 = (4.0 / 3.0);

    public int[] scalefac_buffer;

    private int checkSumHuff = 0;
    private int[] is_1d;
    private float[][][] ro;
    private float[][][] lr;
    private float[] out_1d;
    private float[][] prevblck;
    private float[][] k;
    private int[] nonzero;
    private Bitstream stream;
    private Header header;
    private SynthesisFilter filter1, filter2;
    private Obuffer buffer;
    private int which_channels;
    private BitReserve br;
    private III_side_info_t si;

    private temporaire2[] III_scalefac_t;
    private temporaire2[] scalefac;

    private int max_gr;
    private int frame_start;
    private int part2_start;
    private int channels;
    private int first_channel;
    private int last_channel;
    private int sfreq;

    /**
     * Constructor.
     * <p>
     * REVIEW: these constructor arguments should be moved to the
     * decodeFrame() method, where possible, so that one
     */
    public LayerIIIDecoder(Bitstream stream0, Header header0,
                           SynthesisFilter filtera, SynthesisFilter filterb,
                           Obuffer buffer0, int which_ch0) {
        huffcodetab.initHuff();
        is_1d = new int[SBLIMIT * SSLIMIT + 4];
        ro = new float[2][SBLIMIT][SSLIMIT];
        lr = new float[2][SBLIMIT][SSLIMIT];
        out_1d = new float[SBLIMIT * SSLIMIT];
        prevblck = new float[2][SBLIMIT * SSLIMIT];
        k = new float[2][SBLIMIT * SSLIMIT];
        nonzero = new int[2];

        // III_scalefact_t
        III_scalefac_t = new temporaire2[2];
        III_scalefac_t[0] = new temporaire2();
        III_scalefac_t[1] = new temporaire2();
        scalefac = III_scalefac_t;

        // L3TABLE INIT

        sfBandIndex = new SBI[9]; // SZD: MPEG2.5 +3 indices
        int[] l0 = {0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576};
        int[] s0 = {0, 4, 8, 12, 18, 24, 32, 42, 56, 74, 100, 132, 174, 192};
        int[] l1 = {0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 114, 136, 162, 194, 232, 278, 330, 394, 464, 540, 576};
        int[] s1 = {0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 136, 180, 192};
        int[] l2 = {0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576};
        int[] s2 = {0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 134, 174, 192};

        int[] l3 = {0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 52, 62, 74, 90, 110, 134, 162, 196, 238, 288, 342, 418, 576};
        int[] s3 = {0, 4, 8, 12, 16, 22, 30, 40, 52, 66, 84, 106, 136, 192};
        int[] l4 = {0, 4, 8, 12, 16, 20, 24, 30, 36, 42, 50, 60, 72, 88, 106, 128, 156, 190, 230, 276, 330, 384, 576};
        int[] s4 = {0, 4, 8, 12, 16, 22, 28, 38, 50, 64, 80, 100, 126, 192};
        int[] l5 = {0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 54, 66, 82, 102, 126, 156, 194, 240, 296, 364, 448, 550, 576};
        int[] s5 = {0, 4, 8, 12, 16, 22, 30, 42, 58, 78, 104, 138, 180, 192};
        // SZD: MPEG2.5
        int[] l6 = {0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576};
        int[] s6 = {0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 134, 174, 192};
        int[] l7 = {0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464, 522, 576};
        int[] s7 = {0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 134, 174, 192};
        int[] l8 = {0, 12, 24, 36, 48, 60, 72, 88, 108, 132, 160, 192, 232, 280, 336, 400, 476, 566, 568, 570, 572, 574, 576};
        int[] s8 = {0, 8, 16, 24, 36, 52, 72, 96, 124, 160, 162, 164, 166, 192};

        sfBandIndex[0] = new SBI(l0, s0);
        sfBandIndex[1] = new SBI(l1, s1);
        sfBandIndex[2] = new SBI(l2, s2);

        sfBandIndex[3] = new SBI(l3, s3);
        sfBandIndex[4] = new SBI(l4, s4);
        sfBandIndex[5] = new SBI(l5, s5);
        // SZD: MPEG2.5
        sfBandIndex[6] = new SBI(l6, s6);
        sfBandIndex[7] = new SBI(l7, s7);
        sfBandIndex[8] = new SBI(l8, s8);

        // END OF L3TABLE INIT

        if (reorder_table == null) { // SZD: generate LUT
            reorder_table = new int[9][];
            for (int i = 0; i < 9; i++)
                reorder_table[i] = reorder(sfBandIndex[i].s);
        }

        // Sftable
        int[] ll0 = {0, 6, 11, 16, 21};
        int[] ss0 = {0, 6, 12};
        sftable = new Sftable(ll0, ss0);
        // END OF Sftable

        // scalefac_buffer
        scalefac_buffer = new int[54];
        // END OF scalefac_buffer

        stream = stream0;
        header = header0;
        filter1 = filtera;
        filter2 = filterb;
        buffer = buffer0;
        which_channels = which_ch0;

        frame_start = 0;
        channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
        max_gr = (header.version() == Header.MPEG1) ? 2 : 1;

        sfreq = header.sample_frequency() +
                ((header.version() == Header.MPEG1) ? 3 :
                        (header.version() == Header.MPEG25_LSF) ? 6 : 0);    // SZD

        if (channels == 2) {
            switch (which_channels) {
            case OutputChannels.LEFT_CHANNEL:
            case OutputChannels.DOWNMIX_CHANNELS:
                first_channel = last_channel = 0;
                break;

            case OutputChannels.RIGHT_CHANNEL:
                first_channel = last_channel = 1;
                break;

            case OutputChannels.BOTH_CHANNELS:
            default:
                first_channel = 0;
                last_channel = 1;
                break;
            }
        } else {
            first_channel = last_channel = 0;
        }

        for (int ch = 0; ch < 2; ch++)
            for (int j = 0; j < 576; j++)
                prevblck[ch][j] = 0.0f;

        nonzero[0] = nonzero[1] = 576;

        br = new BitReserve();
        si = new III_side_info_t();
    }

    /**
     * Notify decoder that a seek is being made.
     */
    public void seek_notify() {
        frame_start = 0;
        for (int ch = 0; ch < 2; ch++)
            for (int j = 0; j < 576; j++)
                prevblck[ch][j] = 0.0f;
        br = new BitReserve();
    }

    public void decodeFrame() {
        decode();
    }

    // subband samples are buffered and passed to the
    // SynthesisFilter in one go.
    private float[] samples1 = new float[32];
    private float[] samples2 = new float[32];

    /**
     * Decode one frame, filling the buffer with the output samples.
     */
    public void decode() {
        int nSlots = header.slots();
        int flush_main;
        int gr, ch, ss, sb, sb18;
        int main_data_end;
        int bytes_to_discard;
        int i;

        get_side_info();

        for (i = 0; i < nSlots; i++)
            br.hputbuf(stream.get_bits(8));

        main_data_end = br.hsstell() >>> 3; // of previous frame

        if ((flush_main = (br.hsstell() & 7)) != 0) {
            br.hgetbits(8 - flush_main);
            main_data_end++;
        }

        bytes_to_discard = frame_start - main_data_end
                - si.main_data_begin;

        frame_start += nSlots;

        if (bytes_to_discard < 0)
            return;

        if (main_data_end > 4096) {
            frame_start -= 4096;
            br.rewindNbytes(4096);
        }

        for (; bytes_to_discard > 0; bytes_to_discard--)
            br.hgetbits(8);

        for (gr = 0; gr < max_gr; gr++) {

            for (ch = 0; ch < channels; ch++) {
                part2_start = br.hsstell();

                if (header.version() == Header.MPEG1)
                    get_scale_factors(ch, gr);
                else // MPEG-2 LSF, SZD: MPEG-2.5 LSF
                    get_LSF_scale_factors(ch, gr);

                huffman_decode(ch, gr);
                dequantize_sample(ro[ch], ch, gr);
            }

            stereo(gr);

            if ((which_channels == OutputChannels.DOWNMIX_CHANNELS) && (channels > 1))
                do_downmix();

            for (ch = first_channel; ch <= last_channel; ch++) {

                reorder(lr[ch], ch, gr);
                antialias(ch, gr);

                hybrid(ch, gr);

                for (sb18 = 18; sb18 < 576; sb18 += 36) // Frequency inversion
                    for (ss = 1; ss < SSLIMIT; ss += 2)
                        out_1d[sb18 + ss] = -out_1d[sb18 + ss];

                if ((ch == 0) || (which_channels == OutputChannels.RIGHT_CHANNEL)) {
                    for (ss = 0; ss < SSLIMIT; ss++) { // Polyphase synthesis
                        sb = 0;
                        for (sb18 = 0; sb18 < 576; sb18 += 18) {
                            samples1[sb] = out_1d[sb18 + ss];
                            sb++;
                        }
                        filter1.input_samples(samples1);
                        filter1.calculate_pcm_samples(buffer);
                    }
                } else {
                    for (ss = 0; ss < SSLIMIT; ss++) { // Polyphase synthesis
                        sb = 0;
                        for (sb18 = 0; sb18 < 576; sb18 += 18) {
                            samples2[sb] = out_1d[sb18 + ss];
                            sb++;
                        }
                        filter2.input_samples(samples2);
                        filter2.calculate_pcm_samples(buffer);
                    }

                }
            }
        }

        counter++;
        buffer.write_buffer(1);
    }

    /**
     * Reads the side info from the stream, assuming the entire.
     * frame has been read already.
     * Mono   : 136 bits (= 17 bytes)
     * Stereo : 256 bits (= 32 bytes)
     */
    private boolean get_side_info() {
        int ch, gr;
        if (header.version() == Header.MPEG1) {

            si.main_data_begin = stream.get_bits(9);
            if (channels == 1)
                si.private_bits = stream.get_bits(5);
            else si.private_bits = stream.get_bits(3);

            for (ch = 0; ch < channels; ch++) {
                si.ch[ch].scfsi[0] = stream.get_bits(1);
                si.ch[ch].scfsi[1] = stream.get_bits(1);
                si.ch[ch].scfsi[2] = stream.get_bits(1);
                si.ch[ch].scfsi[3] = stream.get_bits(1);
            }

            for (gr = 0; gr < 2; gr++) {
                for (ch = 0; ch < channels; ch++) {
                    si.ch[ch].gr[gr].part2_3_length = stream.get_bits(12);
                    si.ch[ch].gr[gr].big_values = stream.get_bits(9);
                    si.ch[ch].gr[gr].global_gain = stream.get_bits(8);
                    si.ch[ch].gr[gr].scalefac_compress = stream.get_bits(4);
                    si.ch[ch].gr[gr].window_switching_flag = stream.get_bits(1);
                    if ((si.ch[ch].gr[gr].window_switching_flag) != 0) {
                        si.ch[ch].gr[gr].block_type = stream.get_bits(2);
                        si.ch[ch].gr[gr].mixed_block_flag = stream.get_bits(1);

                        si.ch[ch].gr[gr].table_select[0] = stream.get_bits(5);
                        si.ch[ch].gr[gr].table_select[1] = stream.get_bits(5);

                        si.ch[ch].gr[gr].subblock_gain[0] = stream.get_bits(3);
                        si.ch[ch].gr[gr].subblock_gain[1] = stream.get_bits(3);
                        si.ch[ch].gr[gr].subblock_gain[2] = stream.get_bits(3);

                        // Set region_count parameters since they are implicit in this case.

                        if (si.ch[ch].gr[gr].block_type == 0) {
                            // Side info bad: block_type == 0 in split block
                            return false;
                        } else if (si.ch[ch].gr[gr].block_type == 2
                                && si.ch[ch].gr[gr].mixed_block_flag == 0) {
                            si.ch[ch].gr[gr].region0_count = 8;
                        } else {
                            si.ch[ch].gr[gr].region0_count = 7;
                        }
                        si.ch[ch].gr[gr].region1_count = 20 -
                                si.ch[ch].gr[gr].region0_count;
                    } else {
                        si.ch[ch].gr[gr].table_select[0] = stream.get_bits(5);
                        si.ch[ch].gr[gr].table_select[1] = stream.get_bits(5);
                        si.ch[ch].gr[gr].table_select[2] = stream.get_bits(5);
                        si.ch[ch].gr[gr].region0_count = stream.get_bits(4);
                        si.ch[ch].gr[gr].region1_count = stream.get_bits(3);
                        si.ch[ch].gr[gr].block_type = 0;
                    }
                    si.ch[ch].gr[gr].preflag = stream.get_bits(1);
                    si.ch[ch].gr[gr].scalefac_scale = stream.get_bits(1);
                    si.ch[ch].gr[gr].count1table_select = stream.get_bits(1);
                }
            }
        } else { // MPEG-2 LSF, SZD: MPEG-2.5 LSF

            si.main_data_begin = stream.get_bits(8);
            if (channels == 1)
                si.private_bits = stream.get_bits(1);
            else si.private_bits = stream.get_bits(2);

            for (ch = 0; ch < channels; ch++) {

                si.ch[ch].gr[0].part2_3_length = stream.get_bits(12);
                si.ch[ch].gr[0].big_values = stream.get_bits(9);
                si.ch[ch].gr[0].global_gain = stream.get_bits(8);
                si.ch[ch].gr[0].scalefac_compress = stream.get_bits(9);
                si.ch[ch].gr[0].window_switching_flag = stream.get_bits(1);

                if ((si.ch[ch].gr[0].window_switching_flag) != 0) {

                    si.ch[ch].gr[0].block_type = stream.get_bits(2);
                    si.ch[ch].gr[0].mixed_block_flag = stream.get_bits(1);
                    si.ch[ch].gr[0].table_select[0] = stream.get_bits(5);
                    si.ch[ch].gr[0].table_select[1] = stream.get_bits(5);

                    si.ch[ch].gr[0].subblock_gain[0] = stream.get_bits(3);
                    si.ch[ch].gr[0].subblock_gain[1] = stream.get_bits(3);
                    si.ch[ch].gr[0].subblock_gain[2] = stream.get_bits(3);

                    // Set region_count parameters since they are implicit in this case.

                    if (si.ch[ch].gr[0].block_type == 0) {
                        // Side info bad: block_type == 0 in split block
                        return false;
                    } else if (si.ch[ch].gr[0].block_type == 2
                            && si.ch[ch].gr[0].mixed_block_flag == 0) {
                        si.ch[ch].gr[0].region0_count = 8;
                    } else {
                        si.ch[ch].gr[0].region0_count = 7;
                        si.ch[ch].gr[0].region1_count = 20 -
                                si.ch[ch].gr[0].region0_count;
                    }

                } else {
                    si.ch[ch].gr[0].table_select[0] = stream.get_bits(5);
                    si.ch[ch].gr[0].table_select[1] = stream.get_bits(5);
                    si.ch[ch].gr[0].table_select[2] = stream.get_bits(5);
                    si.ch[ch].gr[0].region0_count = stream.get_bits(4);
                    si.ch[ch].gr[0].region1_count = stream.get_bits(3);
                    si.ch[ch].gr[0].block_type = 0;
                }

                si.ch[ch].gr[0].scalefac_scale = stream.get_bits(1);
                si.ch[ch].gr[0].count1table_select = stream.get_bits(1);
            }
        }
        return true;
    }

    /**
     *
     */
    private void get_scale_factors(int ch, int gr) {
        int sfb, window;
        gr_info_s gr_info = (si.ch[ch].gr[gr]);
        int scale_comp = gr_info.scalefac_compress;
        int length0 = slen[0][scale_comp];
        int length1 = slen[1][scale_comp];

        if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2)) {
            if ((gr_info.mixed_block_flag) != 0) { // MIXED
                for (sfb = 0; sfb < 8; sfb++)
                    scalefac[ch].l[sfb] = br.hgetbits(
                            slen[0][gr_info.scalefac_compress]);
                for (sfb = 3; sfb < 6; sfb++)
                    for (window = 0; window < 3; window++)
                        scalefac[ch].s[window][sfb] = br.hgetbits(
                                slen[0][gr_info.scalefac_compress]);
                for (sfb = 6; sfb < 12; sfb++)
                    for (window = 0; window < 3; window++)
                        scalefac[ch].s[window][sfb] = br.hgetbits(
                                slen[1][gr_info.scalefac_compress]);
                for (sfb = 12, window = 0; window < 3; window++)
                    scalefac[ch].s[window][sfb] = 0;

            } else { // SHORT

                scalefac[ch].s[0][0] = br.hgetbits(length0);
                scalefac[ch].s[1][0] = br.hgetbits(length0);
                scalefac[ch].s[2][0] = br.hgetbits(length0);
                scalefac[ch].s[0][1] = br.hgetbits(length0);
                scalefac[ch].s[1][1] = br.hgetbits(length0);
                scalefac[ch].s[2][1] = br.hgetbits(length0);
                scalefac[ch].s[0][2] = br.hgetbits(length0);
                scalefac[ch].s[1][2] = br.hgetbits(length0);
                scalefac[ch].s[2][2] = br.hgetbits(length0);
                scalefac[ch].s[0][3] = br.hgetbits(length0);
                scalefac[ch].s[1][3] = br.hgetbits(length0);
                scalefac[ch].s[2][3] = br.hgetbits(length0);
                scalefac[ch].s[0][4] = br.hgetbits(length0);
                scalefac[ch].s[1][4] = br.hgetbits(length0);
                scalefac[ch].s[2][4] = br.hgetbits(length0);
                scalefac[ch].s[0][5] = br.hgetbits(length0);
                scalefac[ch].s[1][5] = br.hgetbits(length0);
                scalefac[ch].s[2][5] = br.hgetbits(length0);
                scalefac[ch].s[0][6] = br.hgetbits(length1);
                scalefac[ch].s[1][6] = br.hgetbits(length1);
                scalefac[ch].s[2][6] = br.hgetbits(length1);
                scalefac[ch].s[0][7] = br.hgetbits(length1);
                scalefac[ch].s[1][7] = br.hgetbits(length1);
                scalefac[ch].s[2][7] = br.hgetbits(length1);
                scalefac[ch].s[0][8] = br.hgetbits(length1);
                scalefac[ch].s[1][8] = br.hgetbits(length1);
                scalefac[ch].s[2][8] = br.hgetbits(length1);
                scalefac[ch].s[0][9] = br.hgetbits(length1);
                scalefac[ch].s[1][9] = br.hgetbits(length1);
                scalefac[ch].s[2][9] = br.hgetbits(length1);
                scalefac[ch].s[0][10] = br.hgetbits(length1);
                scalefac[ch].s[1][10] = br.hgetbits(length1);
                scalefac[ch].s[2][10] = br.hgetbits(length1);
                scalefac[ch].s[0][11] = br.hgetbits(length1);
                scalefac[ch].s[1][11] = br.hgetbits(length1);
                scalefac[ch].s[2][11] = br.hgetbits(length1);
                scalefac[ch].s[0][12] = 0;
                scalefac[ch].s[1][12] = 0;
                scalefac[ch].s[2][12] = 0;
            } // SHORT

        } else { // LONG types 0,1,3

            if ((si.ch[ch].scfsi[0] == 0) || (gr == 0)) {
                scalefac[ch].l[0] = br.hgetbits(length0);
                scalefac[ch].l[1] = br.hgetbits(length0);
                scalefac[ch].l[2] = br.hgetbits(length0);
                scalefac[ch].l[3] = br.hgetbits(length0);
                scalefac[ch].l[4] = br.hgetbits(length0);
                scalefac[ch].l[5] = br.hgetbits(length0);
            }
            if ((si.ch[ch].scfsi[1] == 0) || (gr == 0)) {
                scalefac[ch].l[6] = br.hgetbits(length0);
                scalefac[ch].l[7] = br.hgetbits(length0);
                scalefac[ch].l[8] = br.hgetbits(length0);
                scalefac[ch].l[9] = br.hgetbits(length0);
                scalefac[ch].l[10] = br.hgetbits(length0);
            }
            if ((si.ch[ch].scfsi[2] == 0) || (gr == 0)) {
                scalefac[ch].l[11] = br.hgetbits(length1);
                scalefac[ch].l[12] = br.hgetbits(length1);
                scalefac[ch].l[13] = br.hgetbits(length1);
                scalefac[ch].l[14] = br.hgetbits(length1);
                scalefac[ch].l[15] = br.hgetbits(length1);
            }
            if ((si.ch[ch].scfsi[3] == 0) || (gr == 0)) {
                scalefac[ch].l[16] = br.hgetbits(length1);
                scalefac[ch].l[17] = br.hgetbits(length1);
                scalefac[ch].l[18] = br.hgetbits(length1);
                scalefac[ch].l[19] = br.hgetbits(length1);
                scalefac[ch].l[20] = br.hgetbits(length1);
            }

            scalefac[ch].l[21] = 0;
            scalefac[ch].l[22] = 0;
        }
    }

    // MDM: new_slen is fully initialized before use, no need
    // to reallocate array.
    private final int[] new_slen = new int[4];

    /**
     *
     */
    private void get_LSF_scale_data(int ch, int gr) {

        int scalefac_comp, int_scalefac_comp;
        int mode_ext = header.mode_extension();
        int m;
        int blocktypenumber;
        int blocknumber = 0;

        gr_info_s gr_info = (si.ch[ch].gr[gr]);

        scalefac_comp = gr_info.scalefac_compress;

        if (gr_info.block_type == 2) {
            if (gr_info.mixed_block_flag == 0)
                blocktypenumber = 1;
            else if (gr_info.mixed_block_flag == 1)
                blocktypenumber = 2;
            else
                blocktypenumber = 0;
        } else {
            blocktypenumber = 0;
        }

        if (!(((mode_ext == 1) || (mode_ext == 3)) && (ch == 1))) {

            if (scalefac_comp < 400) {

                new_slen[0] = (scalefac_comp >>> 4) / 5;
                new_slen[1] = (scalefac_comp >>> 4) % 5;
                new_slen[2] = (scalefac_comp & 0xF) >>> 2;
                new_slen[3] = (scalefac_comp & 3);
                si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 0;

            } else if (scalefac_comp < 500) {

                new_slen[0] = ((scalefac_comp - 400) >>> 2) / 5;
                new_slen[1] = ((scalefac_comp - 400) >>> 2) % 5;
                new_slen[2] = (scalefac_comp - 400) & 3;
                new_slen[3] = 0;
                si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 1;

            } else if (scalefac_comp < 512) {

                new_slen[0] = (scalefac_comp - 500) / 3;
                new_slen[1] = (scalefac_comp - 500) % 3;
                new_slen[2] = 0;
                new_slen[3] = 0;
                si.ch[ch].gr[gr].preflag = 1;
                blocknumber = 2;
            }
        }

        if ((((mode_ext == 1) || (mode_ext == 3)) && (ch == 1))) {
            int_scalefac_comp = scalefac_comp >>> 1;

            if (int_scalefac_comp < 180) {
                new_slen[0] = int_scalefac_comp / 36;
                new_slen[1] = (int_scalefac_comp % 36) / 6;
                new_slen[2] = (int_scalefac_comp % 36) % 6;
                new_slen[3] = 0;
                si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 3;
            } else if (int_scalefac_comp < 244) {
                new_slen[0] = ((int_scalefac_comp - 180) & 0x3F) >>> 4;
                new_slen[1] = ((int_scalefac_comp - 180) & 0xF) >>> 2;
                new_slen[2] = (int_scalefac_comp - 180) & 3;
                new_slen[3] = 0;
                si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 4;
            } else if (int_scalefac_comp < 255) {
                new_slen[0] = (int_scalefac_comp - 244) / 3;
                new_slen[1] = (int_scalefac_comp - 244) % 3;
                new_slen[2] = 0;
                new_slen[3] = 0;
                si.ch[ch].gr[gr].preflag = 0;
                blocknumber = 5;
            }
        }

        for (int x = 0; x < 45; x++) // why 45, not 54?
            scalefac_buffer[x] = 0;

        m = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < nr_of_sfb_block[blocknumber][blocktypenumber][i];
                 j++) {
                scalefac_buffer[m] = (new_slen[i] == 0) ? 0 :
                        br.hgetbits(new_slen[i]);
                m++;

            }
        }
    }

    /**
     *
     */
    private void get_LSF_scale_factors(int ch, int gr) {
        int m = 0;
        int sfb, window;
        gr_info_s gr_info = (si.ch[ch].gr[gr]);

        get_LSF_scale_data(ch, gr);

        if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2)) {
            if (gr_info.mixed_block_flag != 0) { // MIXED
                for (sfb = 0; sfb < 8; sfb++) {
                    scalefac[ch].l[sfb] = scalefac_buffer[m];
                    m++;
                }
                for (sfb = 3; sfb < 12; sfb++) {
                    for (window = 0; window < 3; window++) {
                        scalefac[ch].s[window][sfb] = scalefac_buffer[m];
                        m++;
                    }
                }
                for (window = 0; window < 3; window++)
                    scalefac[ch].s[window][12] = 0;

            } else { // SHORT

                for (sfb = 0; sfb < 12; sfb++) {
                    for (window = 0; window < 3; window++) {
                        scalefac[ch].s[window][sfb] = scalefac_buffer[m];
                        m++;
                    }
                }

                for (window = 0; window < 3; window++)
                    scalefac[ch].s[window][12] = 0;
            }
        } else { // LONG types 0,1,3

            for (sfb = 0; sfb < 21; sfb++) {
                scalefac[ch].l[sfb] = scalefac_buffer[m];
                m++;
            }
            scalefac[ch].l[21] = 0; // Jeff
            scalefac[ch].l[22] = 0;
        }
    }

    /**
     *
     */
    int[] x = {0};
    int[] y = {0};
    int[] v = {0};
    int[] w = {0};

    private void huffman_decode(int ch, int gr) {
        x[0] = 0;
        y[0] = 0;
        v[0] = 0;
        w[0] = 0;

        int part2_3_end = part2_start + si.ch[ch].gr[gr].part2_3_length;
        int num_bits;
        int region1Start;
        int region2Start;
        int index;

        int buf, buf1;

        huffcodetab h;

        // Find region boundary for short block case

        if (((si.ch[ch].gr[gr].window_switching_flag) != 0) &&
                (si.ch[ch].gr[gr].block_type == 2)) {

            // Region2.
            //MS: Extrahandling for 8KHZ
            region1Start = (sfreq == 8) ? 72 : 36; // sfb[9/3]*3=36 or in case 8KHZ = 72
            region2Start = 576; // No Region2 for short block case

        } else { // Find region boundary for long block case

            buf = si.ch[ch].gr[gr].region0_count + 1;
            buf1 = buf + si.ch[ch].gr[gr].region1_count + 1;

            if (buf1 > sfBandIndex[sfreq].l.length - 1) buf1 = sfBandIndex[sfreq].l.length - 1;

            region1Start = sfBandIndex[sfreq].l[buf];
            region2Start = sfBandIndex[sfreq].l[buf1]; /* MI */
        }

        index = 0;
        // Read big values area
        for (int i = 0; i < (si.ch[ch].gr[gr].big_values << 1); i += 2) {
            if (i < region1Start) h = huffcodetab.ht[si.ch[ch].gr[gr].table_select[0]];
            else if (i < region2Start) h = huffcodetab.ht[si.ch[ch].gr[gr].table_select[1]];
            else h = huffcodetab.ht[si.ch[ch].gr[gr].table_select[2]];

            huffcodetab.huffman_decoder(h, x, y, v, w, br);

            is_1d[index++] = x[0];
            is_1d[index++] = y[0];

            checkSumHuff = checkSumHuff + x[0] + y[0];
        }

        // Read count1 area
        h = huffcodetab.ht[si.ch[ch].gr[gr].count1table_select + 32];
        num_bits = br.hsstell();

        while ((num_bits < part2_3_end) && (index < 576)) {

            huffcodetab.huffman_decoder(h, x, y, v, w, br);

            is_1d[index++] = v[0];
            is_1d[index++] = w[0];
            is_1d[index++] = x[0];
            is_1d[index++] = y[0];
            checkSumHuff = checkSumHuff + v[0] + w[0] + x[0] + y[0];
            num_bits = br.hsstell();
        }

        if (num_bits > part2_3_end) {
            br.rewindNbits(num_bits - part2_3_end);
            index -= 4;
        }

        num_bits = br.hsstell();

        // Dismiss stuffing bits
        if (num_bits < part2_3_end)
            br.hgetbits(part2_3_end - num_bits);

        // Zero out rest

        nonzero[ch] = Math.min(index, 576);

        if (index < 0) index = 0;

        // may not be necessary
        for (; index < 576; index++)
            is_1d[index] = 0;
    }

    /**
     *
     */
    private void i_stereo_k_values(int is_pos, int io_type, int i) {
        if (is_pos == 0) {
            k[0][i] = 1.0f;
            k[1][i] = 1.0f;
        } else if ((is_pos & 1) != 0) {
            k[0][i] = io[io_type][(is_pos + 1) >>> 1];
            k[1][i] = 1.0f;
        } else {
            k[0][i] = 1.0f;
            k[1][i] = io[io_type][is_pos >>> 1];
        }
    }

    /**
     *
     */
    private void dequantize_sample(float[][] xr, int ch, int gr) {
        gr_info_s gr_info = (si.ch[ch].gr[gr]);
        int cb = 0;
        int next_cb_boundary;
        int cb_begin = 0;
        int cb_width = 0;
        int index = 0, t_index, j;
        float g_gain;
        float[][] xr_1d = xr;

        // choose correct scalefactor band per block type, initialize boundary

        if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2)) {
            if (gr_info.mixed_block_flag != 0)
                next_cb_boundary = sfBandIndex[sfreq].l[1]; // LONG blocks: 0,1,3
            else {
                cb_width = sfBandIndex[sfreq].s[1];
                next_cb_boundary = (cb_width << 2) - cb_width;
                cb_begin = 0;
            }
        } else {
            next_cb_boundary = sfBandIndex[sfreq].l[1]; // LONG blocks: 0,1,3
        }

        // Compute overall (global) scaling.

        g_gain = (float) Math.pow(2.0, (0.25 * (gr_info.global_gain - 210.0)));

        for (j = 0; j < nonzero[ch]; j++) {
            // Modif E.B 02/22/99
            int reste = j % SSLIMIT;
            int quotien = (j - reste) / SSLIMIT;
            if (is_1d[j] == 0) xr_1d[quotien][reste] = 0.0f;
            else {
                int abv = is_1d[j];
                // Pow Array fix (11/17/04)
                if (abv < t_43.length) {
                    if (is_1d[j] > 0) xr_1d[quotien][reste] = g_gain * t_43[abv];
                    else {
                        if (-abv < t_43.length) xr_1d[quotien][reste] = -g_gain * t_43[-abv];
                        else xr_1d[quotien][reste] = -g_gain * (float) Math.pow(-abv, d43);
                    }
                } else {
                    if (is_1d[j] > 0) xr_1d[quotien][reste] = g_gain * (float) Math.pow(abv, d43);
                    else xr_1d[quotien][reste] = -g_gain * (float) Math.pow(-abv, d43);
                }
            }
        }

        // apply formula per block type
        for (j = 0; j < nonzero[ch]; j++) {
            // Modif E.B 02/22/99
            int reste = j % SSLIMIT;
            int quotien = (j - reste) / SSLIMIT;

            if (index == next_cb_boundary) { /* Adjust critical band boundary */
                if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2)) {
                    if (gr_info.mixed_block_flag != 0) {

                        if (index == sfBandIndex[sfreq].l[8]) {
                            next_cb_boundary = sfBandIndex[sfreq].s[4];
                            next_cb_boundary = (next_cb_boundary << 2) -
                                    next_cb_boundary;
                            cb = 3;
                            cb_width = sfBandIndex[sfreq].s[4] -
                                    sfBandIndex[sfreq].s[3];

                            cb_begin = sfBandIndex[sfreq].s[3];
                            cb_begin = (cb_begin << 2) - cb_begin;

                        } else if (index < sfBandIndex[sfreq].l[8]) {

                            next_cb_boundary = sfBandIndex[sfreq].l[(++cb) + 1];

                        } else {

                            next_cb_boundary = sfBandIndex[sfreq].s[(++cb) + 1];
                            next_cb_boundary = (next_cb_boundary << 2) -
                                    next_cb_boundary;

                            cb_begin = sfBandIndex[sfreq].s[cb];
                            cb_width = sfBandIndex[sfreq].s[cb + 1] -
                                    cb_begin;
                            cb_begin = (cb_begin << 2) - cb_begin;
                        }

                    } else {

                        next_cb_boundary = sfBandIndex[sfreq].s[(++cb) + 1];
                        next_cb_boundary = (next_cb_boundary << 2) -
                                next_cb_boundary;

                        cb_begin = sfBandIndex[sfreq].s[cb];
                        cb_width = sfBandIndex[sfreq].s[cb + 1] -
                                cb_begin;
                        cb_begin = (cb_begin << 2) - cb_begin;
                    }

                } else { // long blocks

                    next_cb_boundary = sfBandIndex[sfreq].l[(++cb) + 1];
                }
            }

            // Do long/short dependent scaling operations

            if ((gr_info.window_switching_flag != 0) &&
                    (((gr_info.block_type == 2) && (gr_info.mixed_block_flag == 0)) ||
                            ((gr_info.block_type == 2) && (gr_info.mixed_block_flag != 0) && (j >= 36)))) {

                t_index = (index - cb_begin) / cb_width;
                int idx = scalefac[ch].s[t_index][cb]
                        << gr_info.scalefac_scale;
                idx += (gr_info.subblock_gain[t_index] << 2);

                xr_1d[quotien][reste] *= two_to_negative_half_pow[idx];

            } else {   // LONG block types 0,1,3 & 1st 2 subbands of switched blocks
                int idx = scalefac[ch].l[cb];

                if (gr_info.preflag != 0)
                    idx += pretab[cb];

                idx = idx << gr_info.scalefac_scale;
                xr_1d[quotien][reste] *= two_to_negative_half_pow[idx];
            }
            index++;
        }

        for (j = nonzero[ch]; j < 576; j++) {
            // Modif E.B 02/22/99
            int reste = j % SSLIMIT;
            int quotien = (j - reste) / SSLIMIT;
            if (reste < 0) reste = 0;
            if (quotien < 0) quotien = 0;
            xr_1d[quotien][reste] = 0.0f;
        }
    }

    /**
     *
     */
    private void reorder(float[][] xr, int ch, int gr) {
        gr_info_s gr_info = (si.ch[ch].gr[gr]);
        int freq, freq3;
        int index;
        int sfb, sfb_start, sfb_lines;
        int src_line, des_line;
        float[][] xr_1d = xr;

        if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2)) {

            for (index = 0; index < 576; index++)
                out_1d[index] = 0.0f;

            if (gr_info.mixed_block_flag != 0) {
                // NO REORDER FOR LOW 2 SUBBANDS
                for (index = 0; index < 36; index++) {
                    // Modif E.B 02/22/99
                    int reste = index % SSLIMIT;
                    int quotien = (index - reste) / SSLIMIT;
                    out_1d[index] = xr_1d[quotien][reste];
                }
                for (sfb = 3; sfb < 13; sfb++) {
                    sfb_start = sfBandIndex[sfreq].s[sfb];
                    sfb_lines = sfBandIndex[sfreq].s[sfb + 1] - sfb_start;

                    int sfb_start3 = (sfb_start << 2) - sfb_start;

                    for (freq = 0, freq3 = 0; freq < sfb_lines;
                         freq++, freq3 += 3) {

                        src_line = sfb_start3 + freq;
                        des_line = sfb_start3 + freq3;
                        // Modif E.B 02/22/99
                        int reste = src_line % SSLIMIT;
                        int quotien = (src_line - reste) / SSLIMIT;

                        out_1d[des_line] = xr_1d[quotien][reste];
                        src_line += sfb_lines;
                        des_line++;

                        reste = src_line % SSLIMIT;
                        quotien = (src_line - reste) / SSLIMIT;

                        out_1d[des_line] = xr_1d[quotien][reste];
                        src_line += sfb_lines;
                        des_line++;

                        reste = src_line % SSLIMIT;
                        quotien = (src_line - reste) / SSLIMIT;

                        out_1d[des_line] = xr_1d[quotien][reste];
                    }
                }

            } else { // pure short
                for (index = 0; index < 576; index++) {
                    int j = reorder_table[sfreq][index];
                    int reste = j % SSLIMIT;
                    int quotien = (j - reste) / SSLIMIT;
                    out_1d[index] = xr_1d[quotien][reste];
                }
            }
        } else { // long blocks
            for (index = 0; index < 576; index++) {
                // Modif E.B 02/22/99
                int reste = index % SSLIMIT;
                int quotien = (index - reste) / SSLIMIT;
                out_1d[index] = xr_1d[quotien][reste];
            }
        }
    }

    int[] is_pos = new int[576];
    float[] is_ratio = new float[576];

    /**
     *
     */
    private void stereo(int gr) {
        int sb, ss;

        if (channels == 1) { // mono , bypass xr[0][][] to lr[0][][]

            for (sb = 0; sb < SBLIMIT; sb++)
                for (ss = 0; ss < SSLIMIT; ss += 3) {
                    lr[0][sb][ss] = ro[0][sb][ss];
                    lr[0][sb][ss + 1] = ro[0][sb][ss + 1];
                    lr[0][sb][ss + 2] = ro[0][sb][ss + 2];
                }

        } else {

            gr_info_s gr_info = (si.ch[0].gr[gr]);
            int mode_ext = header.mode_extension();
            int sfb;
            int i;
            int lines, temp, temp2;

            boolean ms_stereo = ((header.mode() == Header.JOINT_STEREO) && ((mode_ext & 0x2) != 0));
            boolean i_stereo = ((header.mode() == Header.JOINT_STEREO) && ((mode_ext & 0x1) != 0));
            boolean lsf = ((header.version() == Header.MPEG2_LSF || header.version() == Header.MPEG25_LSF));    // SZD

            int io_type = (gr_info.scalefac_compress & 1);

            // initialization

            for (i = 0; i < 576; i++) {
                is_pos[i] = 7;

                is_ratio[i] = 0.0f;
            }

            if (i_stereo) {
                if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2)) {
                    if (gr_info.mixed_block_flag != 0) {

                        int max_sfb = 0;

                        for (int j = 0; j < 3; j++) {
                            int sfbcnt;
                            sfbcnt = 2;
                            for (sfb = 12; sfb >= 3; sfb--) {
                                i = sfBandIndex[sfreq].s[sfb];
                                lines = sfBandIndex[sfreq].s[sfb + 1] - i;
                                i = (i << 2) - i + (j + 1) * lines - 1;

                                while (lines > 0) {
                                    if (ro[1][i / 18][i % 18] != 0.0f) {
                                        // MDM: in java, array access is very slow.
                                        // Is quicker to compute div and mod values.
                                        //if (ro[1][ss_div[i]][ss_mod[i]] != 0.0f) {
                                        sfbcnt = sfb;
                                        sfb = -10;
                                        lines = -10;
                                    }

                                    lines--;
                                    i--;

                                } // while (lines > 0)

                            } // for (sfb=12 ...
                            sfb = sfbcnt + 1;

                            if (sfb > max_sfb)
                                max_sfb = sfb;

                            while (sfb < 12) {
                                temp = sfBandIndex[sfreq].s[sfb];
                                sb = sfBandIndex[sfreq].s[sfb + 1] - temp;
                                i = (temp << 2) - temp + j * sb;

                                for (; sb > 0; sb--) {
                                    is_pos[i] = scalefac[1].s[j][sfb];
                                    if (is_pos[i] != 7)
                                        if (lsf)
                                            i_stereo_k_values(is_pos[i], io_type, i);
                                        else
                                            is_ratio[i] = TAN12[is_pos[i]];

                                    i++;
                                } // for (; sb>0...
                                sfb++;
                            } // while (sfb < 12)
                            sfb = sfBandIndex[sfreq].s[10];
                            sb = sfBandIndex[sfreq].s[11] - sfb;
                            sfb = (sfb << 2) - sfb + j * sb;
                            temp = sfBandIndex[sfreq].s[11];
                            sb = sfBandIndex[sfreq].s[12] - temp;
                            i = (temp << 2) - temp + j * sb;

                            for (; sb > 0; sb--) {
                                is_pos[i] = is_pos[sfb];

                                if (lsf) {
                                    k[0][i] = k[0][sfb];
                                    k[1][i] = k[1][sfb];
                                } else {
                                    is_ratio[i] = is_ratio[sfb];
                                }
                                i++;
                            } // for (; sb > 0 ...
                        }
                        if (max_sfb <= 3) {
                            i = 2;
                            ss = 17;
                            sb = -1;
                            while (i >= 0) {
                                if (ro[1][i][ss] != 0.0f) {
                                    sb = (i << 4) + (i << 1) + ss;
                                    i = -1;
                                } else {
                                    ss--;
                                    if (ss < 0) {
                                        i--;
                                        ss = 17;
                                    }
                                } // if (ro ...
                            } // while (i>=0)
                            i = 0;
                            while (sfBandIndex[sfreq].l[i] <= sb)
                                i++;
                            sfb = i;
                            i = sfBandIndex[sfreq].l[i];
                            for (; sfb < 8; sfb++) {
                                sb = sfBandIndex[sfreq].l[sfb + 1] - sfBandIndex[sfreq].l[sfb];
                                for (; sb > 0; sb--) {
                                    is_pos[i] = scalefac[1].l[sfb];
                                    if (is_pos[i] != 7)
                                        if (lsf)
                                            i_stereo_k_values(is_pos[i], io_type, i);
                                        else
                                            is_ratio[i] = TAN12[is_pos[i]];
                                    i++;
                                } // for (; sb>0 ...
                            } // for (; sfb<8 ...
                        } // for (j=0 ...
                    } else { // if (gr_info.mixed_block_flag)
                        for (int j = 0; j < 3; j++) {
                            int sfbcnt;
                            sfbcnt = -1;
                            for (sfb = 12; sfb >= 0; sfb--) {
                                temp = sfBandIndex[sfreq].s[sfb];
                                lines = sfBandIndex[sfreq].s[sfb + 1] - temp;
                                i = (temp << 2) - temp + (j + 1) * lines - 1;

                                while (lines > 0) {
                                    if (ro[1][i / 18][i % 18] != 0.0f) {
                                        // MDM: in java, array access is very slow.
                                        // Is quicker to compute div and mod values.
                                        sfbcnt = sfb;
                                        sfb = -10;
                                        lines = -10;
                                    }
                                    lines--;
                                    i--;
                                } // while (lines > 0) */

                            } // for (sfb=12 ...
                            sfb = sfbcnt + 1;
                            while (sfb < 12) {
                                temp = sfBandIndex[sfreq].s[sfb];
                                sb = sfBandIndex[sfreq].s[sfb + 1] - temp;
                                i = (temp << 2) - temp + j * sb;
                                for (; sb > 0; sb--) {
                                    is_pos[i] = scalefac[1].s[j][sfb];
                                    if (is_pos[i] != 7)
                                        if (lsf)
                                            i_stereo_k_values(is_pos[i], io_type, i);
                                        else
                                            is_ratio[i] = TAN12[is_pos[i]];
                                    i++;
                                } // for (; sb>0 ...
                                sfb++;
                            } // while (sfb<12)

                            temp = sfBandIndex[sfreq].s[10];
                            temp2 = sfBandIndex[sfreq].s[11];
                            sb = temp2 - temp;
                            sfb = (temp << 2) - temp + j * sb;
                            sb = sfBandIndex[sfreq].s[12] - temp2;
                            i = (temp2 << 2) - temp2 + j * sb;

                            for (; sb > 0; sb--) {
                                is_pos[i] = is_pos[sfb];

                                if (lsf) {
                                    k[0][i] = k[0][sfb];
                                    k[1][i] = k[1][sfb];
                                } else {
                                    is_ratio[i] = is_ratio[sfb];
                                }
                                i++;
                            } // for (; sb>0 ...
                        } // for (sfb=12
                    } // for (j=0 ...
                } else { // if (gr_info.window_switching_flag ...
                    i = 31;
                    ss = 17;
                    sb = 0;
                    while (i >= 0) {
                        if (ro[1][i][ss] != 0.0f) {
                            sb = (i << 4) + (i << 1) + ss;
                            i = -1;
                        } else {
                            ss--;
                            if (ss < 0) {
                                i--;
                                ss = 17;
                            }
                        }
                    }
                    i = 0;
                    while (sfBandIndex[sfreq].l[i] <= sb)
                        i++;

                    sfb = i;
                    i = sfBandIndex[sfreq].l[i];
                    for (; sfb < 21; sfb++) {
                        sb = sfBandIndex[sfreq].l[sfb + 1] - sfBandIndex[sfreq].l[sfb];
                        for (; sb > 0; sb--) {
                            is_pos[i] = scalefac[1].l[sfb];
                            if (is_pos[i] != 7)
                                if (lsf)
                                    i_stereo_k_values(is_pos[i], io_type, i);
                                else
                                    is_ratio[i] = TAN12[is_pos[i]];
                            i++;
                        }
                    }
                    sfb = sfBandIndex[sfreq].l[20];
                    for (sb = 576 - sfBandIndex[sfreq].l[21]; (sb > 0) && (i < 576); sb--) {
                        is_pos[i] = is_pos[sfb]; // error here : i >=576

                        if (lsf) {
                            k[0][i] = k[0][sfb];
                            k[1][i] = k[1][sfb];
                        } else {
                            is_ratio[i] = is_ratio[sfb];
                        }
                        i++;
                    } // if (gr_info.mixed_block_flag)
                } // if (gr_info.window_switching_flag ...
            } // if (i_stereo)

            i = 0;
            for (sb = 0; sb < SBLIMIT; sb++)
                for (ss = 0; ss < SSLIMIT; ss++) {
                    if (is_pos[i] == 7) {
                        if (ms_stereo) {
                            lr[0][sb][ss] = (ro[0][sb][ss] + ro[1][sb][ss]) * 0.707106781f;
                            lr[1][sb][ss] = (ro[0][sb][ss] - ro[1][sb][ss]) * 0.707106781f;
                        } else {
                            lr[0][sb][ss] = ro[0][sb][ss];
                            lr[1][sb][ss] = ro[1][sb][ss];
                        }
                    } else if (i_stereo) {

                        if (lsf) {
                            lr[0][sb][ss] = ro[0][sb][ss] * k[0][i];
                            lr[1][sb][ss] = ro[0][sb][ss] * k[1][i];
                        } else {
                            lr[1][sb][ss] = ro[0][sb][ss] / (1 + is_ratio[i]);
                            lr[0][sb][ss] = lr[1][sb][ss] * is_ratio[i];
                        }
                    }
                    i++;
                }
        } // channels == 2
    }

    /**
     *
     */
    private void antialias(int ch, int gr) {
        int sb18, ss, sb18lim;
        gr_info_s gr_info = (si.ch[ch].gr[gr]);
        // 31 alias-reduction operations between each pair of sub-bands
        // with 8 butterflies between each pair

        if ((gr_info.window_switching_flag != 0) && (gr_info.block_type == 2) &&
                !(gr_info.mixed_block_flag != 0))
            return;

        if ((gr_info.window_switching_flag != 0) && (gr_info.mixed_block_flag != 0) &&
                (gr_info.block_type == 2)) {
            sb18lim = 18;
        } else {
            sb18lim = 558;
        }

        for (sb18 = 0; sb18 < sb18lim; sb18 += 18) {
            for (ss = 0; ss < 8; ss++) {
                int src_idx1 = sb18 + 17 - ss;
                int src_idx2 = sb18 + 18 + ss;
                float bu = out_1d[src_idx1];
                float bd = out_1d[src_idx2];
                out_1d[src_idx1] = (bu * cs[ss]) - (bd * ca[ss]);
                out_1d[src_idx2] = (bd * cs[ss]) + (bu * ca[ss]);
            }
        }
    }

    // MDM: tsOutCopy and rawout do not need initializing, so the arrays
    // can be reused.
    float[] tsOutCopy = new float[18];
    float[] rawout = new float[36];

    /**
     *
     */
    private void hybrid(int ch, int gr) {
        int bt;
        int sb18;
        gr_info_s gr_info = (si.ch[ch].gr[gr]);
        float[] tsOut;

        float[][] prvblk;

        for (sb18 = 0; sb18 < 576; sb18 += 18) {
            bt = ((gr_info.window_switching_flag != 0) && (gr_info.mixed_block_flag != 0) &&
                    (sb18 < 36)) ? 0 : gr_info.block_type;

            tsOut = out_1d;
            // Modif E.B 02/22/99
            System.arraycopy(tsOut, 0 + sb18, tsOutCopy, 0, 18);

            inv_mdct(tsOutCopy, rawout, bt);


            System.arraycopy(tsOutCopy, 0, tsOut, 0 + sb18, 18);
            // Fin Modif

            // overlap addition
            prvblk = prevblck;

            tsOut[0 + sb18] = rawout[0] + prvblk[ch][sb18 + 0];
            prvblk[ch][sb18 + 0] = rawout[18];
            tsOut[1 + sb18] = rawout[1] + prvblk[ch][sb18 + 1];
            prvblk[ch][sb18 + 1] = rawout[19];
            tsOut[2 + sb18] = rawout[2] + prvblk[ch][sb18 + 2];
            prvblk[ch][sb18 + 2] = rawout[20];
            tsOut[3 + sb18] = rawout[3] + prvblk[ch][sb18 + 3];
            prvblk[ch][sb18 + 3] = rawout[21];
            tsOut[4 + sb18] = rawout[4] + prvblk[ch][sb18 + 4];
            prvblk[ch][sb18 + 4] = rawout[22];
            tsOut[5 + sb18] = rawout[5] + prvblk[ch][sb18 + 5];
            prvblk[ch][sb18 + 5] = rawout[23];
            tsOut[6 + sb18] = rawout[6] + prvblk[ch][sb18 + 6];
            prvblk[ch][sb18 + 6] = rawout[24];
            tsOut[7 + sb18] = rawout[7] + prvblk[ch][sb18 + 7];
            prvblk[ch][sb18 + 7] = rawout[25];
            tsOut[8 + sb18] = rawout[8] + prvblk[ch][sb18 + 8];
            prvblk[ch][sb18 + 8] = rawout[26];
            tsOut[9 + sb18] = rawout[9] + prvblk[ch][sb18 + 9];
            prvblk[ch][sb18 + 9] = rawout[27];
            tsOut[10 + sb18] = rawout[10] + prvblk[ch][sb18 + 10];
            prvblk[ch][sb18 + 10] = rawout[28];
            tsOut[11 + sb18] = rawout[11] + prvblk[ch][sb18 + 11];
            prvblk[ch][sb18 + 11] = rawout[29];
            tsOut[12 + sb18] = rawout[12] + prvblk[ch][sb18 + 12];
            prvblk[ch][sb18 + 12] = rawout[30];
            tsOut[13 + sb18] = rawout[13] + prvblk[ch][sb18 + 13];
            prvblk[ch][sb18 + 13] = rawout[31];
            tsOut[14 + sb18] = rawout[14] + prvblk[ch][sb18 + 14];
            prvblk[ch][sb18 + 14] = rawout[32];
            tsOut[15 + sb18] = rawout[15] + prvblk[ch][sb18 + 15];
            prvblk[ch][sb18 + 15] = rawout[33];
            tsOut[16 + sb18] = rawout[16] + prvblk[ch][sb18 + 16];
            prvblk[ch][sb18 + 16] = rawout[34];
            tsOut[17 + sb18] = rawout[17] + prvblk[ch][sb18 + 17];
            prvblk[ch][sb18 + 17] = rawout[35];
        }
    }

    /**
     *
     */
    private void do_downmix() {
        for (int sb = 0; sb < SSLIMIT; sb++) {
            for (int ss = 0; ss < SSLIMIT; ss += 3) {
                lr[0][sb][ss] = (lr[0][sb][ss] + lr[1][sb][ss]) * 0.5f;
                lr[0][sb][ss + 1] = (lr[0][sb][ss + 1] + lr[1][sb][ss + 1]) * 0.5f;
                lr[0][sb][ss + 2] = (lr[0][sb][ss + 2] + lr[1][sb][ss + 2]) * 0.5f;
            }
        }
    }

    /**
     * Fast INV_MDCT.
     */
    public void inv_mdct(float[] in, float[] out, int block_type) {
        float[] win_bt;
        int i;

        float tmpf_0, tmpf_1, tmpf_2, tmpf_3, tmpf_4, tmpf_5, tmpf_6, tmpf_7, tmpf_8, tmpf_9;
        float tmpf_10, tmpf_11, tmpf_12, tmpf_13, tmpf_14, tmpf_15, tmpf_16, tmpf_17;

        tmpf_0 = tmpf_1 = tmpf_2 = tmpf_3 = tmpf_4 = tmpf_5 = tmpf_6 = tmpf_7 = tmpf_8 = tmpf_9 =
                tmpf_10 = tmpf_11 = tmpf_12 = tmpf_13 = tmpf_14 = tmpf_15 = tmpf_16 = tmpf_17 = 0.0f;

        if (block_type == 2) {

            out[0] = 0.0f;
            out[1] = 0.0f;
            out[2] = 0.0f;
            out[3] = 0.0f;
            out[4] = 0.0f;
            out[5] = 0.0f;
            out[6] = 0.0f;
            out[7] = 0.0f;
            out[8] = 0.0f;
            out[9] = 0.0f;
            out[10] = 0.0f;
            out[11] = 0.0f;
            out[12] = 0.0f;
            out[13] = 0.0f;
            out[14] = 0.0f;
            out[15] = 0.0f;
            out[16] = 0.0f;
            out[17] = 0.0f;
            out[18] = 0.0f;
            out[19] = 0.0f;
            out[20] = 0.0f;
            out[21] = 0.0f;
            out[22] = 0.0f;
            out[23] = 0.0f;
            out[24] = 0.0f;
            out[25] = 0.0f;
            out[26] = 0.0f;
            out[27] = 0.0f;
            out[28] = 0.0f;
            out[29] = 0.0f;
            out[30] = 0.0f;
            out[31] = 0.0f;
            out[32] = 0.0f;
            out[33] = 0.0f;
            out[34] = 0.0f;
            out[35] = 0.0f;

            int six_i = 0;

            for (i = 0; i < 3; i++) {
                // 12 point IMDCT
                // Begin 12 point IDCT
                // Input aliasing for 12 pt IDCT
                in[15 + i] += in[12 + i];
                in[12 + i] += in[9 + i];
                in[9 + i] += in[6 + i];
                in[6 + i] += in[3 + i];
                in[3 + i] += in[0 + i];

                // Input aliasing on odd indices (for 6 point IDCT)
                in[15 + i] += in[9 + i];
                in[9 + i] += in[3 + i];

                // 3 point IDCT on even indices
                float pp1, pp2, sum;
                pp2 = in[12 + i] * 0.500000000f;
                pp1 = in[6 + i] * 0.866025403f;
                sum = in[0 + i] + pp2;
                tmpf_1 = in[0 + i] - in[12 + i];
                tmpf_0 = sum + pp1;
                tmpf_2 = sum - pp1;

                // End 3 point IDCT on even indices
                // 3 point IDCT on odd indices (for 6 point IDCT)
                pp2 = in[15 + i] * 0.500000000f;
                pp1 = in[9 + i] * 0.866025403f;
                sum = in[3 + i] + pp2;
                tmpf_4 = in[3 + i] - in[15 + i];
                tmpf_5 = sum + pp1;
                tmpf_3 = sum - pp1;
                // End 3 point IDCT on odd indices
                // Twiddle factors on odd indices (for 6 point IDCT)

                tmpf_3 *= 1.931851653f;
                tmpf_4 *= 0.707106781f;
                tmpf_5 *= 0.517638090f;

                // Output butterflies on 2 3 point IDCT's (for 6 point IDCT)
                float save = tmpf_0;
                tmpf_0 += tmpf_5;
                tmpf_5 = save - tmpf_5;
                save = tmpf_1;
                tmpf_1 += tmpf_4;
                tmpf_4 = save - tmpf_4;
                save = tmpf_2;
                tmpf_2 += tmpf_3;
                tmpf_3 = save - tmpf_3;

                // End 6 point IDCT
                // Twiddle factors on indices (for 12 point IDCT)

                tmpf_0 *= 0.504314480f;
                tmpf_1 *= 0.541196100f;
                tmpf_2 *= 0.630236207f;
                tmpf_3 *= 0.821339815f;
                tmpf_4 *= 1.306562965f;
                tmpf_5 *= 3.830648788f;

                // End 12 point IDCT

                // Shift to 12 point modified IDCT, multiply by window type 2
                tmpf_8 = -tmpf_0 * 0.793353340f;
                tmpf_9 = -tmpf_0 * 0.608761429f;
                tmpf_7 = -tmpf_1 * 0.923879532f;
                tmpf_10 = -tmpf_1 * 0.382683432f;
                tmpf_6 = -tmpf_2 * 0.991444861f;
                tmpf_11 = -tmpf_2 * 0.130526192f;

                tmpf_0 = tmpf_3;
                tmpf_1 = tmpf_4 * 0.382683432f;
                tmpf_2 = tmpf_5 * 0.608761429f;

                tmpf_3 = -tmpf_5 * 0.793353340f;
                tmpf_4 = -tmpf_4 * 0.923879532f;
                tmpf_5 = -tmpf_0 * 0.991444861f;

                tmpf_0 *= 0.130526192f;

                out[six_i + 6] += tmpf_0;
                out[six_i + 7] += tmpf_1;
                out[six_i + 8] += tmpf_2;
                out[six_i + 9] += tmpf_3;
                out[six_i + 10] += tmpf_4;
                out[six_i + 11] += tmpf_5;
                out[six_i + 12] += tmpf_6;
                out[six_i + 13] += tmpf_7;
                out[six_i + 14] += tmpf_8;
                out[six_i + 15] += tmpf_9;
                out[six_i + 16] += tmpf_10;
                out[six_i + 17] += tmpf_11;

                six_i += 6;
            }
        } else {
            // 36 point IDCT
            // input aliasing for 36 point IDCT
            in[17] += in[16];
            in[16] += in[15];
            in[15] += in[14];
            in[14] += in[13];
            in[13] += in[12];
            in[12] += in[11];
            in[11] += in[10];
            in[10] += in[9];
            in[9] += in[8];
            in[8] += in[7];
            in[7] += in[6];
            in[6] += in[5];
            in[5] += in[4];
            in[4] += in[3];
            in[3] += in[2];
            in[2] += in[1];
            in[1] += in[0];

            // 18 point IDCT for odd indices
            // input aliasing for 18 point IDCT
            in[17] += in[15];
            in[15] += in[13];
            in[13] += in[11];
            in[11] += in[9];
            in[9] += in[7];
            in[7] += in[5];
            in[5] += in[3];
            in[3] += in[1];

            float tmp0, tmp1, tmp2, tmp3, tmp4, tmp0_, tmp1_, tmp2_, tmp3_;
            float tmp0o, tmp1o, tmp2o, tmp3o, tmp4o, tmp0_o, tmp1_o, tmp2_o, tmp3_o;

            // Fast 9 Point Inverse Discrete Cosine Transform
            //
            // By  Francois-Raymond Boyer
            //         mailto:boyerf@iro.umontreal.ca
            //         http://www.iro.umontreal.ca/~boyerf
            //
            // The code has been optimized for Intel processors
            //  (takes a lot of time to convert float to and from iternal FPU representation)
            //
            // It is a simple "factorization" of the IDCT matrix.

            // 9 point IDCT on even indices

            // 5 points on odd indices (not realy an IDCT)
            float i00 = in[0] + in[0];
            float iip12 = i00 + in[12];

            tmp0 = iip12 + in[4] * 1.8793852415718f + in[8] * 1.532088886238f + in[16] * 0.34729635533386f;
            tmp1 = i00 + in[4] - in[8] - in[12] - in[12] - in[16];
            tmp2 = iip12 - in[4] * 0.34729635533386f - in[8] * 1.8793852415718f + in[16] * 1.532088886238f;
            tmp3 = iip12 - in[4] * 1.532088886238f + in[8] * 0.34729635533386f - in[16] * 1.8793852415718f;
            tmp4 = in[0] - in[4] + in[8] - in[12] + in[16];

            // 4 points on even indices
            float i66_ = in[6] * 1.732050808f; // Sqrt[3]

            tmp0_ = in[2] * 1.9696155060244f + i66_ + in[10] * 1.2855752193731f + in[14] * 0.68404028665134f;
            tmp1_ = (in[2] - in[10] - in[14]) * 1.732050808f;
            tmp2_ = in[2] * 1.2855752193731f - i66_ - in[10] * 0.68404028665134f + in[14] * 1.9696155060244f;
            tmp3_ = in[2] * 0.68404028665134f - i66_ + in[10] * 1.9696155060244f - in[14] * 1.2855752193731f;

            // 9 point IDCT on odd indices
            // 5 points on odd indices (not realy an IDCT)
            float i0 = in[0 + 1] + in[0 + 1];
            float i0p12 = i0 + in[12 + 1];

            tmp0o = i0p12 + in[4 + 1] * 1.8793852415718f + in[8 + 1] * 1.532088886238f + in[16 + 1] * 0.34729635533386f;
            tmp1o = i0 + in[4 + 1] - in[8 + 1] - in[12 + 1] - in[12 + 1] - in[16 + 1];
            tmp2o = i0p12 - in[4 + 1] * 0.34729635533386f - in[8 + 1] * 1.8793852415718f + in[16 + 1] * 1.532088886238f;
            tmp3o = i0p12 - in[4 + 1] * 1.532088886238f + in[8 + 1] * 0.34729635533386f - in[16 + 1] * 1.8793852415718f;
            tmp4o = (in[0 + 1] - in[4 + 1] + in[8 + 1] - in[12 + 1] + in[16 + 1]) * 0.707106781f; // Twiddled

            // 4 points on even indices
            float i6_ = in[6 + 1] * 1.732050808f; // Sqrt[3]

            tmp0_o = in[2 + 1] * 1.9696155060244f + i6_ + in[10 + 1] * 1.2855752193731f + in[14 + 1] * 0.68404028665134f;
            tmp1_o = (in[2 + 1] - in[10 + 1] - in[14 + 1]) * 1.732050808f;
            tmp2_o = in[2 + 1] * 1.2855752193731f - i6_ - in[10 + 1] * 0.68404028665134f + in[14 + 1] * 1.9696155060244f;
            tmp3_o = in[2 + 1] * 0.68404028665134f - i6_ + in[10 + 1] * 1.9696155060244f - in[14 + 1] * 1.2855752193731f;

            // Twiddle factors on odd indices
            // and
            // Butterflies on 9 point IDCT's
            // and
            // twiddle factors for 36 point IDCT

            float e, o;
            e = tmp0 + tmp0_;
            o = (tmp0o + tmp0_o) * 0.501909918f;
            tmpf_0 = e + o;
            tmpf_17 = e - o;
            e = tmp1 + tmp1_;
            o = (tmp1o + tmp1_o) * 0.517638090f;
            tmpf_1 = e + o;
            tmpf_16 = e - o;
            e = tmp2 + tmp2_;
            o = (tmp2o + tmp2_o) * 0.551688959f;
            tmpf_2 = e + o;
            tmpf_15 = e - o;
            e = tmp3 + tmp3_;
            o = (tmp3o + tmp3_o) * 0.610387294f;
            tmpf_3 = e + o;
            tmpf_14 = e - o;
            tmpf_4 = tmp4 + tmp4o;
            tmpf_13 = tmp4 - tmp4o;
            e = tmp3 - tmp3_;
            o = (tmp3o - tmp3_o) * 0.871723397f;
            tmpf_5 = e + o;
            tmpf_12 = e - o;
            e = tmp2 - tmp2_;
            o = (tmp2o - tmp2_o) * 1.183100792f;
            tmpf_6 = e + o;
            tmpf_11 = e - o;
            e = tmp1 - tmp1_;
            o = (tmp1o - tmp1_o) * 1.931851653f;
            tmpf_7 = e + o;
            tmpf_10 = e - o;
            e = tmp0 - tmp0_;
            o = (tmp0o - tmp0_o) * 5.736856623f;
            tmpf_8 = e + o;
            tmpf_9 = e - o;

            // end 36 point IDCT */
            // shift to modified IDCT
            win_bt = win[block_type];

            out[0] = -tmpf_9 * win_bt[0];
            out[1] = -tmpf_10 * win_bt[1];
            out[2] = -tmpf_11 * win_bt[2];
            out[3] = -tmpf_12 * win_bt[3];
            out[4] = -tmpf_13 * win_bt[4];
            out[5] = -tmpf_14 * win_bt[5];
            out[6] = -tmpf_15 * win_bt[6];
            out[7] = -tmpf_16 * win_bt[7];
            out[8] = -tmpf_17 * win_bt[8];
            out[9] = tmpf_17 * win_bt[9];
            out[10] = tmpf_16 * win_bt[10];
            out[11] = tmpf_15 * win_bt[11];
            out[12] = tmpf_14 * win_bt[12];
            out[13] = tmpf_13 * win_bt[13];
            out[14] = tmpf_12 * win_bt[14];
            out[15] = tmpf_11 * win_bt[15];
            out[16] = tmpf_10 * win_bt[16];
            out[17] = tmpf_9 * win_bt[17];
            out[18] = tmpf_8 * win_bt[18];
            out[19] = tmpf_7 * win_bt[19];
            out[20] = tmpf_6 * win_bt[20];
            out[21] = tmpf_5 * win_bt[21];
            out[22] = tmpf_4 * win_bt[22];
            out[23] = tmpf_3 * win_bt[23];
            out[24] = tmpf_2 * win_bt[24];
            out[25] = tmpf_1 * win_bt[25];
            out[26] = tmpf_0 * win_bt[26];
            out[27] = tmpf_0 * win_bt[27];
            out[28] = tmpf_1 * win_bt[28];
            out[29] = tmpf_2 * win_bt[29];
            out[30] = tmpf_3 * win_bt[30];
            out[31] = tmpf_4 * win_bt[31];
            out[32] = tmpf_5 * win_bt[32];
            out[33] = tmpf_6 * win_bt[33];
            out[34] = tmpf_7 * win_bt[34];
            out[35] = tmpf_8 * win_bt[35];
        }
    }

    @SuppressWarnings("unused")
    private int counter = 0;
    private static final int SSLIMIT = 18;
    private static final int SBLIMIT = 32;

    /**
     * L3TABLE
     */
    static class SBI {
        public int[] l;
        public int[] s;

        public SBI() {
            l = new int[23];
            s = new int[14];
        }

        public SBI(int[] thel, int[] thes) {
            l = thel;
            s = thes;
        }
    }

    static class gr_info_s {
        public int part2_3_length = 0;
        public int big_values = 0;
        public int global_gain = 0;
        public int scalefac_compress = 0;
        public int window_switching_flag = 0;
        public int block_type = 0;
        public int mixed_block_flag = 0;
        public int[] table_select;
        public int[] subblock_gain;
        public int region0_count = 0;
        public int region1_count = 0;
        public int preflag = 0;
        public int scalefac_scale = 0;
        public int count1table_select = 0;

        /**
         * Dummy Constructor
         */
        public gr_info_s() {
            table_select = new int[3];
            subblock_gain = new int[3];
        }
    }

    static class temporaire {
        public int[] scfsi;
        public gr_info_s[] gr;

        /**
         * Dummy Constructor
         */
        public temporaire() {
            scfsi = new int[4];
            gr = new gr_info_s[2];
            gr[0] = new gr_info_s();
            gr[1] = new gr_info_s();
        }
    }

    static class III_side_info_t {

        public int main_data_begin = 0;
        public int private_bits = 0;
        public temporaire[] ch;

        /**
         * Dummy Constructor
         */
        public III_side_info_t() {
            ch = new temporaire[2];
            ch[0] = new temporaire();
            ch[1] = new temporaire();
        }
    }

    static class temporaire2 {
        /** [cb] */
        public int[] l;
        /** [window][cb] */
        public int[][] s;

        /**
         * Dummy Constructor
         */
        public temporaire2() {
            l = new int[23];
            s = new int[3][13];
        }
    }

    private static final int[][] slen = {
            {0, 0, 0, 0, 3, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4},
            {0, 1, 2, 3, 0, 1, 2, 3, 1, 2, 3, 1, 2, 3, 2, 3}
    };

    public static final int[] pretab = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 3, 3, 3, 2, 0
    };

    private SBI[] sfBandIndex; // Init in the constructor.

    public static final float[] two_to_negative_half_pow = {
            1.0000000000E+00f, 7.0710678119E-01f, 5.0000000000E-01f, 3.5355339059E-01f,
            2.5000000000E-01f, 1.7677669530E-01f, 1.2500000000E-01f, 8.8388347648E-02f,
            6.2500000000E-02f, 4.4194173824E-02f, 3.1250000000E-02f, 2.2097086912E-02f,
            1.5625000000E-02f, 1.1048543456E-02f, 7.8125000000E-03f, 5.5242717280E-03f,
            3.9062500000E-03f, 2.7621358640E-03f, 1.9531250000E-03f, 1.3810679320E-03f,
            9.7656250000E-04f, 6.9053396600E-04f, 4.8828125000E-04f, 3.4526698300E-04f,
            2.4414062500E-04f, 1.7263349150E-04f, 1.2207031250E-04f, 8.6316745750E-05f,
            6.1035156250E-05f, 4.3158372875E-05f, 3.0517578125E-05f, 2.1579186438E-05f,
            1.5258789062E-05f, 1.0789593219E-05f, 7.6293945312E-06f, 5.3947966094E-06f,
            3.8146972656E-06f, 2.6973983047E-06f, 1.9073486328E-06f, 1.3486991523E-06f,
            9.5367431641E-07f, 6.7434957617E-07f, 4.7683715820E-07f, 3.3717478809E-07f,
            2.3841857910E-07f, 1.6858739404E-07f, 1.1920928955E-07f, 8.4293697022E-08f,
            5.9604644775E-08f, 4.2146848511E-08f, 2.9802322388E-08f, 2.1073424255E-08f,
            1.4901161194E-08f, 1.0536712128E-08f, 7.4505805969E-09f, 5.2683560639E-09f,
            3.7252902985E-09f, 2.6341780319E-09f, 1.8626451492E-09f, 1.3170890160E-09f,
            9.3132257462E-10f, 6.5854450798E-10f, 4.6566128731E-10f, 3.2927225399E-10f
    };


    public static final float[] t_43 = create_t_43();

    static private float[] create_t_43() {
        float[] t43 = new float[8192];
        final double d43 = (4.0 / 3.0);

        for (int i = 0; i < 8192; i++) {
            t43[i] = (float) Math.pow(i, d43);
        }
        return t43;
    }

    public static final float[][] io = {
            {
                1.0000000000E+00f, 8.4089641526E-01f, 7.0710678119E-01f, 5.9460355751E-01f,
                5.0000000001E-01f, 4.2044820763E-01f, 3.5355339060E-01f, 2.9730177876E-01f,
                2.5000000001E-01f, 2.1022410382E-01f, 1.7677669530E-01f, 1.4865088938E-01f,
                1.2500000000E-01f, 1.0511205191E-01f, 8.8388347652E-02f, 7.4325444691E-02f,
                6.2500000003E-02f, 5.2556025956E-02f, 4.4194173826E-02f, 3.7162722346E-02f,
                3.1250000002E-02f, 2.6278012978E-02f, 2.2097086913E-02f, 1.8581361173E-02f,
                1.5625000001E-02f, 1.3139006489E-02f, 1.1048543457E-02f, 9.2906805866E-03f,
                7.8125000006E-03f, 6.5695032447E-03f, 5.5242717285E-03f, 4.6453402934E-03f
            }, {
                1.0000000000E+00f, 7.0710678119E-01f, 5.0000000000E-01f, 3.5355339060E-01f,
                2.5000000000E-01f, 1.7677669530E-01f, 1.2500000000E-01f, 8.8388347650E-02f,
                6.2500000001E-02f, 4.4194173825E-02f, 3.1250000001E-02f, 2.2097086913E-02f,
                1.5625000000E-02f, 1.1048543456E-02f, 7.8125000002E-03f, 5.5242717282E-03f,
                3.9062500001E-03f, 2.7621358641E-03f, 1.9531250001E-03f, 1.3810679321E-03f,
                9.7656250004E-04f, 6.9053396603E-04f, 4.8828125002E-04f, 3.4526698302E-04f,
                2.4414062501E-04f, 1.7263349151E-04f, 1.2207031251E-04f, 8.6316745755E-05f,
                6.1035156254E-05f, 4.3158372878E-05f, 3.0517578127E-05f, 2.1579186439E-05f
            }
    };


    public static final float[] TAN12 = {
            0.0f, 0.26794919f, 0.57735027f, 1.0f,
            1.73205081f, 3.73205081f, 9.9999999e10f, -3.73205081f,
            -1.73205081f, -1.0f, -0.57735027f, -0.26794919f,
            0.0f, 0.26794919f, 0.57735027f, 1.0f
    };

    // REVIEW: in java, the array lookup may well be slower than
    // the actual calculation
    // 576 / 18
    private static int[][] reorder_table; // SZD: will be generated on demand

    /**
     * Loads the data for the reorder
     */
    static int[] reorder(int[] scalefac_band) { // SZD: converted from LAME
        int j = 0;
        int[] ix = new int[576];
        for (int sfb = 0; sfb < 13; sfb++) {
            int start = scalefac_band[sfb];
            int end = scalefac_band[sfb + 1];
            for (int window = 0; window < 3; window++)
                for (int i = start; i < end; i++)
                    ix[3 * i + window] = j++;
        }
        return ix;
    }

    private static final float[] cs = {
            0.857492925712f, 0.881741997318f, 0.949628649103f, 0.983314592492f,
            0.995517816065f, 0.999160558175f, 0.999899195243f, 0.999993155067f
    };

    private static final float[] ca = {
            -0.5144957554270f, -0.4717319685650f, -0.3133774542040f, -0.1819131996110f,
            -0.0945741925262f, -0.0409655828852f, -0.0141985685725f, -0.00369997467375f
    };

    /**
     * INV_MDCT
     */
    public static final float[][] win = {
            {
                -1.6141214951E-02f, -5.3603178919E-02f, -1.0070713296E-01f, -1.6280817573E-01f,
                -4.9999999679E-01f, -3.8388735032E-01f, -6.2061144372E-01f, -1.1659756083E+00f,
                -3.8720752656E+00f, -4.2256286556E+00f, -1.5195289984E+00f, -9.7416483388E-01f,
                -7.3744074053E-01f, -1.2071067773E+00f, -5.1636156596E-01f, -4.5426052317E-01f,
                -4.0715656898E-01f, -3.6969460527E-01f, -3.3876269197E-01f, -3.1242222492E-01f,
                -2.8939587111E-01f, -2.6880081906E-01f, -5.0000000266E-01f, -2.3251417468E-01f,
                -2.1596714708E-01f, -2.0004979098E-01f, -1.8449493497E-01f, -1.6905846094E-01f,
                -1.5350360518E-01f, -1.3758624925E-01f, -1.2103922149E-01f, -2.0710679058E-01f,
                -8.4752577594E-02f, -6.4157525656E-02f, -4.1131172614E-02f, -1.4790705759E-02f
            }, {
                -1.6141214951E-02f, -5.3603178919E-02f, -1.0070713296E-01f, -1.6280817573E-01f,
                -4.9999999679E-01f, -3.8388735032E-01f, -6.2061144372E-01f, -1.1659756083E+00f,
                -3.8720752656E+00f, -4.2256286556E+00f, -1.5195289984E+00f, -9.7416483388E-01f,
                -7.3744074053E-01f, -1.2071067773E+00f, -5.1636156596E-01f, -4.5426052317E-01f,
                -4.0715656898E-01f, -3.6969460527E-01f, -3.3908542600E-01f, -3.1511810350E-01f,
                -2.9642226150E-01f, -2.8184548650E-01f, -5.4119610000E-01f, -2.6213228100E-01f,
                -2.5387916537E-01f, -2.3296291359E-01f, -1.9852728987E-01f, -1.5233534808E-01f,
                -9.6496400054E-02f, -3.3423828516E-02f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f
            }, {
                -4.8300800645E-02f, -1.5715656932E-01f, -2.8325045177E-01f, -4.2953747763E-01f,
                -1.2071067795E+00f, -8.2426483178E-01f, -1.1451749106E+00f, -1.7695290101E+00f,
                -4.5470225061E+00f, -3.4890531002E+00f, -7.3296292804E-01f, -1.5076514758E-01f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f
            }, {
                0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f, 0.0000000000E+00f,
                0.0000000000E+00f, 0.0000000000E+00f, -1.5076513660E-01f, -7.3296291107E-01f,
                -3.4890530566E+00f, -4.5470224727E+00f, -1.7695290031E+00f, -1.1451749092E+00f,
                -8.3137738100E-01f, -1.3065629650E+00f, -5.4142014250E-01f, -4.6528974900E-01f,
                -4.1066990750E-01f, -3.7004680800E-01f, -3.3876269197E-01f, -3.1242222492E-01f,
                -2.8939587111E-01f, -2.6880081906E-01f, -5.0000000266E-01f, -2.3251417468E-01f,
                -2.1596714708E-01f, -2.0004979098E-01f, -1.8449493497E-01f, -1.6905846094E-01f,
                -1.5350360518E-01f, -1.3758624925E-01f, -1.2103922149E-01f, -2.0710679058E-01f,
                -8.4752577594E-02f, -6.4157525656E-02f, -4.1131172614E-02f, -1.4790705759E-02f
            }
    };

    // END OF INV_MDCT

    static class Sftable {
        public int[] l;
        public int[] s;

        public Sftable() {
            l = new int[5];
            s = new int[3];
        }

        public Sftable(int[] thel, int[] thes) {
            l = thel;
            s = thes;
        }
    }

    public Sftable sftable;

    public static final int[][][] nr_of_sfb_block = {
            {{6, 5, 5, 5}, {9, 9, 9, 9}, {6, 9, 9, 9}},
            {{6, 5, 7, 3}, {9, 9, 12, 6}, {6, 9, 12, 6}},
            {{11, 10, 0, 0}, {18, 18, 0, 0}, {15, 18, 0, 0}},
            {{7, 7, 7, 0}, {12, 12, 12, 0}, {6, 15, 12, 0}},
            {{6, 6, 6, 3}, {12, 9, 9, 6}, {6, 12, 9, 6}},
            {{8, 8, 5, 0}, {15, 12, 9, 0}, {6, 18, 9, 0}}
    };
}
