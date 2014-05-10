/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.sound;

import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
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

    public static final int EXPORT_WAV = 0;
    public static final int EXPORT_MP3 = 1;
    public static final int EXPORT_FLV = 2;

    public SoundFormat() {

    }

    public int getNativeExportFormat() {
        switch (formatId) {
            case FORMAT_UNCOMPRESSED_NATIVE_ENDIAN:
            case FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
            case FORMAT_ADPCM:
                return EXPORT_WAV;
            case FORMAT_MP3:
                return EXPORT_MP3;
            case FORMAT_NELLYMOSER16KHZ:
            case FORMAT_NELLYMOSER8KHZ:
            case FORMAT_NELLYMOSER:
            case FORMAT_SPEEX:
                return EXPORT_FLV;
            default:
                return EXPORT_FLV;
        }
    }

    public SoundFormat(int formatId, int samplingRate, boolean stereo) {
        this.formatId = formatId;
        this.samplingRate = samplingRate;
        this.stereo = stereo;
        ensureFormat();
    }

    public byte[] decode(byte data[]) {
        try {
            return getDecoder().decode(data);
        } catch (IOException ex) {
            return null;
        }
    }

    public boolean decode(InputStream is, OutputStream os) {
        try {
            getDecoder().decode(is, os);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean play(byte data[]) {
        return play(new ByteArrayInputStream(data));
    }

    public boolean play(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!decode(is, baos)) {
            return false;
        }

        AudioFormat audioFormat = new AudioFormat(samplingRate, 16, stereo ? 2 : 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                audioFormat);
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(audioFormat);
            byte data[] = baos.toByteArray();
            line.write(data, 0, data.length);
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

    private static void writeLE(OutputStream os, long val, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            os.write((int) (val & 0xff));
            val >>= 8;
        }
    }

    public boolean createWav(InputStream is, OutputStream os) {
        ensureFormat();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        decode(is, baos);
        try {
            createWavFromPcmData(os, samplingRate, true, stereo, baos.toByteArray());
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

        int sampleRate = soundRateHz;//rateMap[soundRate];
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
