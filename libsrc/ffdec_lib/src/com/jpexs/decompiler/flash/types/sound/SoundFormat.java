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
package com.jpexs.decompiler.flash.types.sound;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Sound format.
 *
 * @author JPEXS
 */
public class SoundFormat {

    public int formatId;

    public int samplingRate;

    public boolean stereo;

    //int[] rateMap = {5512, 11025, 22050, 44100};
    public static final int FORMAT_UNCOMPRESSED_NATIVE_ENDIAN = 0;

    public static final int FORMAT_ADPCM = 1;

    public static final int FORMAT_MP3 = 2;

    public static final int FORMAT_UNCOMPRESSED_LITTLE_ENDIAN = 3;

    public static final int FORMAT_NELLYMOSER16KHZ = 4;

    public static final int FORMAT_NELLYMOSER8KHZ = 5;

    public static final int FORMAT_NELLYMOSER = 6;

    public static final int FORMAT_SPEEX = 11;

    public SoundFormat() {

    }

    public SoundExportFormat getNativeExportFormat() {
        switch (formatId) {
            case FORMAT_UNCOMPRESSED_NATIVE_ENDIAN:
            case FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
                return SoundExportFormat.WAV;
            case FORMAT_MP3:
                return SoundExportFormat.MP3;
            case FORMAT_ADPCM:
            case FORMAT_NELLYMOSER16KHZ:
            case FORMAT_NELLYMOSER8KHZ:
            case FORMAT_NELLYMOSER:
            case FORMAT_SPEEX:
                return SoundExportFormat.FLV;
            default:
                return SoundExportFormat.FLV;
        }
    }

    public SoundFormat(int formatId, int samplingRate, boolean stereo) {
        this.formatId = formatId;
        this.samplingRate = samplingRate;
        this.stereo = stereo;
        ensureFormat();
    }

    public boolean play(SWFInputStream sis) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!SoundFormat.this.decode(sis, baos)) {
            return false;
        }

        AudioFormat audioFormat = new AudioFormat(samplingRate, 16, stereo ? 2 : 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                audioFormat);
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(audioFormat);
            byte[] outData = baos.toByteArray();
            line.write(outData, 0, outData.length);
            line.drain();
            line.stop();
            return true;
        } catch (LineUnavailableException ex) {
            return false;
        }
    }

    private void ensureFormat() {
        switch (formatId) {
            case FORMAT_NELLYMOSER16KHZ:
                samplingRate = 16000;
                break;
            case FORMAT_NELLYMOSER8KHZ:
                samplingRate = 8000;
                break;
            case FORMAT_NELLYMOSER:
                samplingRate = 22050;
                break;
        }
    }

    public SoundDecoder getDecoder() {
        ensureFormat();
        switch (formatId) {
            case FORMAT_UNCOMPRESSED_NATIVE_ENDIAN:
            case FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
                return new NoDecoder(this);
            case FORMAT_ADPCM:
                return new AdpcmDecoder(this);
            case FORMAT_MP3:
                return new MP3Decoder(this);
            case FORMAT_NELLYMOSER16KHZ:
                return new NellyMoserDecoder(this);
            case FORMAT_NELLYMOSER8KHZ:
                return new NellyMoserDecoder(this);
            case FORMAT_NELLYMOSER:
                return new NellyMoserDecoder(this);
            case FORMAT_SPEEX:
                return null; //I haven't seen any Speex audio in the wild
            default:
                return null;
        }
    }

    public String getFormatName() {
        switch (formatId) {
            case FORMAT_UNCOMPRESSED_NATIVE_ENDIAN:
                return "Uncompressed native endian";

            case FORMAT_ADPCM:
                return "ADPCM";

            case FORMAT_MP3:
                return "MP3";

            case FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
                return "Uncompressed little endian";

            case FORMAT_NELLYMOSER16KHZ:
                return "NellyMoser 16kHz";

            case FORMAT_NELLYMOSER8KHZ:
                return "NellyMoser 8kHz";

            case FORMAT_NELLYMOSER:
                return "NellyMoser";

            case FORMAT_SPEEX:
                return "Speex";
        }

        return null;
    }

    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    public byte[] decode(SWFInputStream sis) {
        try {
            return getDecoder().decode(sis);
        } catch (IOException ex) {
            return null;
        }
    }

    public boolean decode(SWFInputStream sis, OutputStream os) {
        try {
            getDecoder().decode(sis, os);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public byte[] decode(SOUNDINFO soundInfo, List<ByteArrayRange> dataRanges, int skipSamples) throws IOException {
        ensureFormat();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SoundDecoder decoder = getDecoder();
        for (ByteArrayRange dataRange : dataRanges) {
            SWFInputStream sis = new SWFInputStream(null, dataRange.getArray(), 0, dataRange.getPos() + dataRange.getLength());
            sis.seek(dataRange.getPos());
            decoder.decode(sis, baos);
        }
        byte[] decodedData = baos.toByteArray();
        if (skipSamples > 0) {
            byte[] data = decodedData;
            if (data.length > 0) {
                data = Arrays.copyOfRange(
                        data,
                        skipSamples * 2 * (stereo ? 2 : 1),
                        data.length
                );
            }
            return data;
        }

        return decodedData;
    }

    private byte[] resample(byte[] decodedData) throws IOException {
        if (samplingRate == 44100) {
            return decodedData;
        }
        boolean resamplingFromStereo = true;

        ByteArrayOutputStream baosResampled = new ByteArrayOutputStream();
        for (int i = 0; i < decodedData.length; i += (resamplingFromStereo ? 4 : 2)) {
            if (i + 1 >= decodedData.length) {
                break;
            }
            int left = ((decodedData[i] & 0xff) + ((decodedData[i + 1] & 0xff) << 8)) << 16 >> 16;
            int right = left;
            if (resamplingFromStereo) {
                if (i + 3 >= decodedData.length) {
                    break;
                }
                right = ((decodedData[i + 2] & 0xff) + ((decodedData[i + 3] & 0xff) << 8)) << 16 >> 16;
            }

            int nextLeft = left;
            int nextRight = right;
            int nextI = i + (resamplingFromStereo ? 4 : 2);
            if (nextI < decodedData.length) {
                nextLeft = ((decodedData[nextI] & 0xff) + ((decodedData[nextI + 1] & 0xff) << 8)) << 16 >> 16;
                nextRight = nextLeft;
                if (resamplingFromStereo) {
                    if (nextI + 3 >= decodedData.length) {
                        //ignore
                    } else {
                        nextRight = ((decodedData[nextI + 2] & 0xff) + ((decodedData[nextI + 3] & 0xff) << 8)) << 16 >> 16;
                    }
                }
            }

            writeLE(baosResampled, left, 2);
            writeLE(baosResampled, right, 2);
            if (samplingRate == 5512) {
                writeLE(baosResampled, left + (nextLeft - left) / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) / 8, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 2 / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) * 2 / 8, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 3 / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) * 3 / 8, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 4 / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) * 4 / 8, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 5 / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) * 5 / 8, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 6 / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) * 6 / 8, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 7 / 8, 2);
                writeLE(baosResampled, right + (nextRight - right) * 7 / 8, 2);
            }
            if (samplingRate == 11025) {
                writeLE(baosResampled, left + (nextLeft - left) / 4, 2);
                writeLE(baosResampled, right + (nextRight - right) / 4, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 2 / 4, 2);
                writeLE(baosResampled, right + (nextRight - right) * 2 / 4, 2);
                writeLE(baosResampled, left + (nextLeft - left) * 3 / 4, 2);
                writeLE(baosResampled, right + (nextRight - right) * 3 / 4, 2);
            }
            if (samplingRate == 22050) {
                writeLE(baosResampled, (left + nextLeft) / 2, 2);
                writeLE(baosResampled, (right + nextRight) / 2, 2);
            }
        }
        return baosResampled.toByteArray();
    }

    public boolean createWav(SOUNDINFO soundInfo, List<ByteArrayRange> dataRanges, OutputStream os, int skipSamples, boolean resample) throws IOException {

        byte[] decodedData = decode(soundInfo, dataRanges, skipSamples);
        boolean convertedStereo = stereo;

        ByteArrayOutputStream baosFiltered;
        if (soundInfo == null) {
            baosFiltered = new ByteArrayOutputStream();
            baosFiltered.write(decodedData);
        } else {
            int inPoint = (soundInfo.hasInPoint ? (int) Math.round(soundInfo.inPoint * samplingRate / 44100.0) : 0);
            int outPoint = (soundInfo.hasOutPoint ? (int) Math.round(soundInfo.outPoint * samplingRate / 44100.0) : Integer.MAX_VALUE);
            baosFiltered = new ByteArrayOutputStream();
            int inPointBytes = inPoint * 2 /*16bit*/ * (stereo ? 2 : 1);
            //Q: Use skipSamples value?

            int outPointBytes = soundInfo.hasOutPoint ? outPoint * 2 /*16bit*/ * (stereo ? 2 : 1) : decodedData.length;
            for (int i = inPointBytes; i < outPointBytes; i += (stereo ? 4 : 2)) {
                if (i + 1 >= decodedData.length) {
                    break;
                }
                int left = ((decodedData[i] & 0xff) + ((decodedData[i + 1] & 0xff) << 8)) << 16 >> 16;
                int right = left;
                if (stereo) {
                    if (i + 3 >= decodedData.length) {
                        break;
                    }
                    right = ((decodedData[i + 2] & 0xff) + ((decodedData[i + 3] & 0xff) << 8)) << 16 >> 16;
                }

                if (soundInfo.hasEnvelope) {
                    for (int e = 0; e < soundInfo.envelopeRecords.length - 1; e++) {
                        int envPosBytes = inPointBytes + (int) (soundInfo.envelopeRecords[e].pos44 * samplingRate / 44100.0 * 2 * (stereo ? 2 : 1));
                        int envNextPosBytes = inPointBytes + (int) (soundInfo.envelopeRecords[e + 1].pos44 * samplingRate / 44100.0 * 2 * (stereo ? 2 : 1));
                        if (i >= envPosBytes && i <= envNextPosBytes) {
                            double pos = (i - envPosBytes) / (double) (envNextPosBytes - envPosBytes);

                            int leftLevel = (int) (soundInfo.envelopeRecords[e].leftLevel + (soundInfo.envelopeRecords[e + 1].leftLevel - soundInfo.envelopeRecords[e].leftLevel) * pos);
                            int rightLevel = (int) (soundInfo.envelopeRecords[e].rightLevel + (soundInfo.envelopeRecords[e + 1].rightLevel - soundInfo.envelopeRecords[e].rightLevel) * pos);
                            double leftMultiplier = leftLevel / 32768.0;
                            double rightMultiplier = rightLevel / 32768.0;

                            left = (int) Math.round(left * leftMultiplier);
                            right = (int) Math.round(right * rightMultiplier);
                            break;
                        }
                    }
                }

                writeLE(baosFiltered, left, 2);
                writeLE(baosFiltered, right, 2);
            }
            convertedStereo = true;
        }

        byte[] resampled = resample ? resample(baosFiltered.toByteArray()) : baosFiltered.toByteArray();

        try {
            createWavFromPcmData(os, resample ? 44100 : samplingRate, true, convertedStereo, resampled);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static void createWavFromPcmData(OutputStream fos, int soundRateHz, boolean sample16bit, boolean stereo, byte[] data) throws IOException {
        ByteArrayOutputStream subChunk1Data = new ByteArrayOutputStream();
        int audioFormat = 1; //PCM
        writeLE(subChunk1Data, audioFormat, 2);
        int numChannels = stereo ? 2 : 1;
        writeLE(subChunk1Data, numChannels, 2);

        int sampleRate = soundRateHz;
        writeLE(subChunk1Data, sampleRate, 4);
        int bitsPerSample = sample16bit ? 16 : 8;
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;
        writeLE(subChunk1Data, byteRate, 4);
        int blockAlign = numChannels * bitsPerSample / 8;
        writeLE(subChunk1Data, blockAlign, 2);
        writeLE(subChunk1Data, bitsPerSample, 2);

        ByteArrayOutputStream chunks = new ByteArrayOutputStream();
        chunks.write(Utf8Helper.getBytes("fmt "));
        byte[] subChunk1DataBytes = subChunk1Data.toByteArray();
        writeLE(chunks, subChunk1DataBytes.length, 4);
        chunks.write(subChunk1DataBytes);

        chunks.write(Utf8Helper.getBytes("data"));
        writeLE(chunks, data.length, 4);
        chunks.write(data);

        fos.write(Utf8Helper.getBytes("RIFF"));
        byte[] chunkBytes = chunks.toByteArray();
        writeLE(fos, 4 + chunkBytes.length, 4);
        fos.write(Utf8Helper.getBytes("WAVE"));
        fos.write(chunkBytes);
    }
}
