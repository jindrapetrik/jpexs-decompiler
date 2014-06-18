/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFLimitedInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.sound.MP3FRAME;
import com.jpexs.decompiler.flash.types.sound.MP3SOUNDDATA;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 *
 * @author JPEXS
 */
public class DefineSoundTag extends CharacterTag implements SoundTag {

    @SWFType(BasicType.UI16)
    public int soundId;

    @SWFType(value = BasicType.UB, count = 4)
    public int soundFormat;

    @SWFType(value = BasicType.UB, count = 2)
    public int soundRate;

    public boolean soundSize;
    public boolean soundType;

    @SWFType(BasicType.UI32)
    public long soundSampleCount;

    public byte[] soundData;
    public static final int ID = 14;

    @Override
    public int getCharacterId() {
        return soundId;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(soundId);
            sos.writeUB(4, soundFormat);
            sos.writeUB(2, soundRate);
            sos.writeUB(1, soundSize ? 1 : 0);
            sos.writeUB(1, soundType ? 1 : 0);
            sos.writeUI32(soundSampleCount);
            sos.write(soundData);

        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param data Data bytes
     * @param pos
     * @throws IOException
     */
    public DefineSoundTag(SWFLimitedInputStream sis, long pos, int length) throws IOException {
        super(sis.swf, ID, "DefineSound", pos, length);
        soundId = sis.readUI16();
        soundFormat = (int) sis.readUB(4);
        soundRate = (int) sis.readUB(2);
        soundSize = sis.readUB(1) == 1;
        soundType = sis.readUB(1) == 1;
        soundSampleCount = sis.readUI32();
        soundData = sis.readBytesEx(sis.available());
    }

    @Override
    public String getExportFormat() {
        if (soundFormat == SoundFormat.FORMAT_MP3) {
            return "mp3";
        }
        if (soundFormat == SoundFormat.FORMAT_ADPCM) {
            return "wav";
        }
        if (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN) {
            return "wav";
        }
        if (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_NATIVE_ENDIAN) {
            return "wav";
        }
        if (soundFormat == SoundFormat.FORMAT_NELLYMOSER || soundFormat == SoundFormat.FORMAT_NELLYMOSER16KHZ || soundFormat == SoundFormat.FORMAT_NELLYMOSER8KHZ) {
            return "wav";
        }
        return "flv";
    }

    private void loadID3v2(InputStream in) {
        int size = -1;
        try {
            // Read ID3v2 header (10 bytes).
            in.mark(10);
            size = readID3v2Header(in);
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
                byte[] rawid3v2 = new byte[size];
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
            int majorVersion = id3header[0];
            int revision = id3header[1];
            in.read(id3header, 0, 4);
            size = (int) (id3header[0] << 21) + (id3header[1] << 14) + (id3header[2] << 7) + (id3header[3]);
        }
        return (size + 10);
    }

    @Override
    public boolean setSound(InputStream is, int newSoundFormat) {
        int newSoundRate = -1;
        boolean newSoundSize = false;
        boolean newSoundType = false;
        long newSoundSampleCount = -1;
        byte newSoundData[];
        switch (newSoundFormat) {
            case SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
                try (AudioInputStream audioIs = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                    AudioFormat fmt = audioIs.getFormat();
                    newSoundType = fmt.getChannels() == 2;
                    newSoundSize = fmt.getSampleSizeInBits() == 16;
                    newSoundSampleCount = audioIs.getFrameLength();
                    newSoundData = Helper.readStream(audioIs);
                    newSoundRate = (int) Math.round(fmt.getSampleRate());
                    switch (newSoundRate) {
                        case 5512:
                            newSoundRate = 0;
                            break;
                        case 11025:
                            newSoundRate = 1;
                            break;
                        case 22050:
                            newSoundRate = 2;
                            break;
                        case 44100:
                            newSoundRate = 3;
                            break;
                        default:
                            return false;
                    }
                } catch (UnsupportedAudioFileException | IOException ex) {
                    return false;
                }
                break;
            case SoundFormat.FORMAT_MP3:
                BufferedInputStream bis = new BufferedInputStream(is);
                loadID3v2(bis);
                byte mp3data[] = Helper.readStream(bis);

                final int ID3_V1_LENTGH = 128;
                final int ID3_V1_EXT_LENGTH = 227;

                if (mp3data.length > ID3_V1_LENTGH) {
                    //ID3v1
                    if (mp3data[mp3data.length - ID3_V1_LENTGH] == 'T' && mp3data[mp3data.length - ID3_V1_LENTGH + 1] == 'A' && mp3data[mp3data.length - ID3_V1_LENTGH + 2] == 'G') {
                        mp3data = Arrays.copyOf(mp3data, mp3data.length - ID3_V1_LENTGH);
                        if (mp3data.length > ID3_V1_EXT_LENGTH) {
                            //ID3v1 extended
                            if (mp3data[mp3data.length - ID3_V1_EXT_LENGTH] == 'T' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 1] == 'A' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 2] == 'G' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 3] == '+') {
                                mp3data = Arrays.copyOf(mp3data, mp3data.length - ID3_V1_EXT_LENGTH);
                            }
                        }
                    }
                }
                try {
                    MP3SOUNDDATA snd = new MP3SOUNDDATA(mp3data, swf.version, true);
                    if (!snd.frames.isEmpty()) {
                        MP3FRAME fr = snd.frames.get(0);
                        newSoundRate = fr.getSamplingRate();
                        switch (newSoundRate) {
                            case 11025:
                                newSoundRate = 1;
                                break;
                            case 22050:
                                newSoundRate = 2;
                                break;
                            case 44100:
                                newSoundRate = 3;
                                break;
                            default:
                                return false;
                        }
                        newSoundSize = true;
                        newSoundType = fr.isStereo();
                        int len = snd.sampleCount();
                        if (fr.isStereo()) {
                            len = len / 2;
                        }
                        newSoundSampleCount = len;

                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
                    sos.writeSI16(0); //Latency - how to calculate it?
                    sos.write(mp3data);
                    newSoundData = baos.toByteArray();
                } catch (IOException ex) {
                    return false;
                }
                break;
            default:
                return false;
        }
        if (newSoundData != null) {
            this.soundSize = newSoundSize;
            this.soundRate = newSoundRate;
            this.soundSampleCount = newSoundSampleCount;
            this.soundData = newSoundData;
            this.soundType = newSoundType;
            this.soundFormat = newSoundFormat;
            setModified(true);
            return true;
        }
        return false;

    }

    @Override
    public boolean importSupported() {
        return true;
    }

    @Override
    public int getSoundRate() {
        return soundRate;
    }

    @Override
    public boolean getSoundType() {
        return soundType;
    }

    @Override
    public byte[] getRawSoundData() {
        if (soundFormat == SoundFormat.FORMAT_MP3) {
            return Arrays.copyOfRange(soundData, 2, soundData.length);
        }
        return soundData;
    }

    @Override
    public int getSoundFormatId() {
        return soundFormat;
    }

    @Override
    public long getTotalSoundSampleCount() {
        return soundSampleCount;
    }

    @Override
    public boolean getSoundSize() {
        return soundSize;
    }

    @Override
    public SoundFormat getSoundFormat() {
        final int[] rateMap = {5512, 11025, 22050, 44100};
        return new SoundFormat(getSoundFormatId(), rateMap[getSoundRate()], getSoundType());
    }

}
