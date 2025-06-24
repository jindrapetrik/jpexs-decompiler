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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DefineSound tag - defines sound.
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
     * @param swf SWF
     */
    public DefineSoundTag(SWF swf) {
        super(swf, ID, NAME, null);
        soundId = swf.getNextCharacterId();
        soundData = ByteArrayRange.EMPTY;
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
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
     * @throws IOException On I/O error
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
            if (getInitialLatency() > 0 || isMp3HigherThan160Kbps()) {
                return SoundExportFormat.WAV;
            }
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

    @Override
    public void setSoundSize(boolean soundSize) {
        this.soundSize = soundSize;
    }

    @Override
    public void setSoundType(boolean soundType) {
        this.soundType = soundType;
    }

    @Override
    public void setSoundSampleCount(long soundSampleCount) {
        this.soundSampleCount = soundSampleCount;
    }

    @Override
    public void setSoundCompression(int soundCompression) {
        this.soundFormat = soundCompression;
    }

    @Override
    public void setSoundRate(int soundRate) {
        this.soundRate = soundRate;
    }

    @Override
    public String getFlaExportName() {
        return "sound" + getCharacterId();
    }

    @Override
    public int getInitialLatency() {
        if (soundFormat == SoundFormat.FORMAT_MP3) {
            SWFInputStream sis;
            try {
                sis = new SWFInputStream(null, soundData.getRangeData(0, 2));
                return sis.readSI16("seekSamples");
            } catch (IOException ex) {
                //ignore
            }
        }
        return 0;
    }

    private boolean isMp3HigherThan160Kbps() {
        if (soundFormat != SoundFormat.FORMAT_MP3) {
            return false;
        }
        try {
            SWFInputStream sis = new SWFInputStream(swf, soundData.getRangeData());
            MP3SOUNDDATA s = new MP3SOUNDDATA(sis, false);
            if (!s.frames.isEmpty()) {
                MP3FRAME frame = s.frames.get(0);
                int bitRate = frame.getBitRate() / 1000;
                if (bitRate > 160) {
                    return true;
                }
            }
        } catch (IOException ex) {
            //ignore
        }
        return false;
    }
}
