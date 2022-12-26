/*
 * 11/19/04 1.0 moved to LGPL.
 * 02/23/99 JavaConversion by E.B
 * Don Cross, April 1993.
 * RIFF file format classes.
 * See Chapter 8 of "Multimedia Programmer's Reference" in
 * the Microsoft Windows SDK.
 *
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

package javazoom.jl.converter;

/**
 * Class allowing WaveFormat Access
 */
public class WaveFile extends RiffFile {
    public static final int MAX_WAVE_CHANNELS = 2;

    static class WaveFormat_ChunkData {
        public short wFormatTag = 0;       // Format category (PCM=1)
        public short nChannels = 0;        // Number of channels (mono=1, stereo=2)
        public int nSamplesPerSec = 0;   // Sampling rate [Hz]
        public int nAvgBytesPerSec = 0;
        public short nBlockAlign = 0;
        public short nBitsPerSample = 0;

        public WaveFormat_ChunkData() {
            wFormatTag = 1;     // PCM
            Config(44100, (short) 16, (short) 1);
        }

        public void Config(int NewSamplingRate, short NewBitsPerSample, short NewNumChannels) {
            nSamplesPerSec = NewSamplingRate;
            nChannels = NewNumChannels;
            nBitsPerSample = NewBitsPerSample;
            nAvgBytesPerSec = (nChannels * nSamplesPerSec * nBitsPerSample) / 8;
            nBlockAlign = (short) ((nChannels * nBitsPerSample) / 8);
        }
    }


    class WaveFormat_Chunk {
        public RiffChunkHeader header;
        public WaveFormat_ChunkData data;

        public WaveFormat_Chunk() {
            header = new RiffChunkHeader();
            data = new WaveFormat_ChunkData();
            header.ckID = FourCC("fmt ");
            header.ckSize = 16;
        }

        public int VerifyValidity() {
            boolean ret = header.ckID == FourCC("fmt ") &&

                    (data.nChannels == 1 || data.nChannels == 2) &&

                    data.nAvgBytesPerSec == (data.nChannels *
                            data.nSamplesPerSec *
                            data.nBitsPerSample) / 8 &&

                    data.nBlockAlign == (data.nChannels *
                            data.nBitsPerSample) / 8;
            if (ret) return 1;
            else return 0;
        }
    }

    public static class WaveFileSample {
        public short[] chan;

        public WaveFileSample() {
            chan = new short[WaveFile.MAX_WAVE_CHANNELS];
        }
    }

    private WaveFormat_Chunk wave_format;
    private RiffChunkHeader pcm_data;
    private long pcm_data_offset = 0;  // offset of 'pcm_data' in output file
    private int num_samples = 0;


    /**
     * Constructs a new WaveFile instance.
     */
    public WaveFile() {
        pcm_data = new RiffChunkHeader();
        wave_format = new WaveFormat_Chunk();
        pcm_data.ckID = FourCC("data");
        pcm_data.ckSize = 0;
        num_samples = 0;
    }

    /**
     *
     */
    public int OpenForWrite(String Filename, int SamplingRate, short BitsPerSample, short NumChannels) {
        // Verify parameters...
        if ((Filename == null) ||
                (BitsPerSample != 8 && BitsPerSample != 16) ||
                NumChannels < 1 || NumChannels > 2) {
            return DDC_INVALID_CALL;
        }

        wave_format.data.Config(SamplingRate, BitsPerSample, NumChannels);

        int retcode = Open(Filename, RFM_WRITE);

        if (retcode == DDC_SUCCESS) {
            byte[] theWave = {(byte) 'W', (byte) 'A', (byte) 'V', (byte) 'E'};
            retcode = Write(theWave, 4);

            if (retcode == DDC_SUCCESS) {
                // Ecriture de wave_format
                retcode = Write(wave_format.header, 8);
                retcode = Write(wave_format.data.wFormatTag, 2);
                retcode = Write(wave_format.data.nChannels, 2);
                retcode = Write(wave_format.data.nSamplesPerSec, 4);
                retcode = Write(wave_format.data.nAvgBytesPerSec, 4);
                retcode = Write(wave_format.data.nBlockAlign, 2);
                retcode = Write(wave_format.data.nBitsPerSample, 2);


                if (retcode == DDC_SUCCESS) {
                    pcm_data_offset = CurrentFilePosition();
                    retcode = Write(pcm_data, 8);
                }
            }
        }

        return retcode;
    }

    /**
     * Write 16-bit audio
     */
    public int WriteData(short[] data, int numData) {
        int extraBytes = numData * 2;
        pcm_data.ckSize += extraBytes;
        return super.Write(data, extraBytes);
    }

    /**
     *
     */
    public int Close() {
        int rc = DDC_SUCCESS;

        if (fmode == RFM_WRITE)
            rc = Backpatch(pcm_data_offset, pcm_data, 8);
        if (rc == DDC_SUCCESS)
            rc = super.Close();
        return rc;
    }

    // [Hz]
    public int SamplingRate() {
        return wave_format.data.nSamplesPerSec;
    }

    public short BitsPerSample() {
        return wave_format.data.nBitsPerSample;
    }

    public short NumChannels() {
        return wave_format.data.nChannels;
    }

    public int NumSamples() {
        return num_samples;
    }


    /**
     * Open for write using another wave file's parameters...
     */
    public int OpenForWrite(String Filename, WaveFile OtherWave) {
        return OpenForWrite(Filename,
                OtherWave.SamplingRate(),
                OtherWave.BitsPerSample(),
                OtherWave.NumChannels());
    }

    /**
     *
     */
    public long CurrentFilePosition() {
        return super.CurrentFilePosition();
    }

}