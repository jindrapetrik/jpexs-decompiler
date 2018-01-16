/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.sound.MP3FRAME;
import com.jpexs.decompiler.flash.types.sound.MP3SOUNDDATA;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class DefineSoundTag extends CharacterTag implements SoundTag {

    public static final int ID = 14;

    public static final String NAME = "DefineSound";

    @SWFType(BasicType.UI16)
    public int soundId;

    @SWFType(value = BasicType.UB, count = 4)
    @EnumValue(value = SoundFormat.FORMAT_UNCOMPRESSED_NATIVE_ENDIAN, text = "Uncompressed, native-endian")
    @EnumValue(value = SoundFormat.FORMAT_ADPCM, text = "ADPCM")
    @EnumValue(value = SoundFormat.FORMAT_MP3, text = "MP3", minSwfVersion = 4)
    @EnumValue(value = SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN, text = "Uncompressed, little-endian", minSwfVersion = 4)
    @EnumValue(value = SoundFormat.FORMAT_NELLYMOSER16KHZ, text = "Nellymoser 16 kHz", minSwfVersion = 10)
    @EnumValue(value = SoundFormat.FORMAT_NELLYMOSER8KHZ, text = "Nellymoser 8 kHz", minSwfVersion = 10)
    @EnumValue(value = SoundFormat.FORMAT_NELLYMOSER, text = "Nellymoser", minSwfVersion = 6)
    @EnumValue(value = SoundFormat.FORMAT_SPEEX, text = "Speex", minSwfVersion = 10)
    public int soundFormat;

    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = 0, text = "5.5 kHz")
    @EnumValue(value = 1, text = "11 kHz")
    @EnumValue(value = 2, text = "22 kHz")
    @EnumValue(value = 3, text = "44 kHz")
    public int soundRate;

    public boolean soundSize;

    public boolean soundType;

    @SWFType(BasicType.UI32)
    public long soundSampleCount;

    public ByteArrayRange soundData;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineSoundTag(SWF swf) {
        super(swf, ID, NAME, null);
        soundId = swf.getNextCharacterId();
        soundData = ByteArrayRange.EMPTY;
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineSoundTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        soundId = sis.readUI16("soundId");
        soundFormat = (int) sis.readUB(4, "soundFormat");
        soundRate = (int) sis.readUB(2, "soundRate");
        soundSize = sis.readUB(1, "soundSize") == 1;
        soundType = sis.readUB(1, "soundType") == 1;
        soundSampleCount = sis.readUI32("soundSampleCount");
        soundData = sis.readByteRangeEx(sis.available(), "soundData");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws java.io.IOException
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(soundId);
        sos.writeUB(4, soundFormat);
        sos.writeUB(2, soundRate);
        sos.writeUB(1, soundSize ? 1 : 0);
        sos.writeUB(1, soundType ? 1 : 0);
        sos.writeUI32(soundSampleCount);
        sos.write(soundData);
    }

    @Override
    public int getCharacterId() {
        return soundId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.soundId = characterId;
    }

    @Override
    public SoundExportFormat getExportFormat() {
        if (soundFormat == SoundFormat.FORMAT_MP3) {
            return SoundExportFormat.MP3;
        }
        if (soundFormat == SoundFormat.FORMAT_ADPCM) {
            return SoundExportFormat.WAV;
        }
        if (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN) {
            return SoundExportFormat.WAV;
        }
        if (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_NATIVE_ENDIAN) {
            return SoundExportFormat.WAV;
        }
        if (soundFormat == SoundFormat.FORMAT_NELLYMOSER || soundFormat == SoundFormat.FORMAT_NELLYMOSER16KHZ || soundFormat == SoundFormat.FORMAT_NELLYMOSER8KHZ) {
            return SoundExportFormat.WAV;
        }
        return SoundExportFormat.FLV;
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
        byte[] newSoundData;
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
                byte[] mp3data = Helper.readStream(bis);

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
                    MP3SOUNDDATA snd = new MP3SOUNDDATA(new SWFInputStream(swf, mp3data), true);
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
            this.soundData = new ByteArrayRange(newSoundData);
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
    public List<ByteArrayRange> getRawSoundData() {
        List<ByteArrayRange> ret = new ArrayList<>();
        if (soundFormat == SoundFormat.FORMAT_MP3) {
            ret.add(soundData.getSubRange(2, soundData.getLength() - 2));
            return ret;
        }

        ret.add(soundData);
        return ret;
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

    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);
        SoundFormat soundFormat = getSoundFormat();
        tagInfo.addInfo("general", "codecName", soundFormat.getFormatName());
        tagInfo.addInfo("general", "exportFormat", soundFormat.getNativeExportFormat());
        tagInfo.addInfo("general", "samplingRate", soundFormat.samplingRate);
        tagInfo.addInfo("general", "stereo", soundFormat.stereo);
        tagInfo.addInfo("general", "sampleCount", soundSampleCount);
    }
}
