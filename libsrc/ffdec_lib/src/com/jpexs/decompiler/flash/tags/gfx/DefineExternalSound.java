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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.TagInfo;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * DefineExternalSound tag - external sound.
 *
 * @author JPEXS
 */
public class DefineExternalSound extends CharacterTag implements SoundTag {

    public static final int ID = 1006;

    public static final String NAME = "DefineExternalSound";

    public int characterId;

    public int soundFormat;

    public int bits;

    public int channels;

    public long sampleRate;

    public long sampleCount;

    public long seekSample;

    public String exportName;

    public String fileName;

    public static final int SOUND_FORMAT_WAV = 0;

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeUI16(soundFormat);
        sos.writeUI16(bits);
        sos.writeUI16(channels);
        sos.writeUI32(sampleRate);
        sos.writeUI32(sampleCount);
        sos.writeUI32(seekSample);
        sos.writeNetString(exportName);
        sos.writeNetString(fileName);
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineExternalSound(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    public DefineExternalSound(SWF swf) {
        super(swf, ID, NAME, null);
        exportName = "";
        fileName = "";
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        soundFormat = sis.readUI16("soundFormat");
        bits = sis.readUI16("bits");
        channels = sis.readUI16("channels");
        sampleRate = sis.readUI32("sampleRate");
        sampleCount = sis.readUI32("sampleCount");
        seekSample = sis.readUI32("seekSample");
        exportName = sis.readNetString("exportName");
        fileName = sis.readNetString("fileName");

    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    @Override
    public SoundExportFormat getExportFormat() {
        return SoundExportFormat.WAV; //?
    }

    @Override
    public boolean importSupported() {
        return false;
    }

    @Override
    public int getSoundRate() {
        switch ((int) sampleRate) {
            case 5512:
                return 0;
            case 11025:
                return 1;
            case 22050:
                return 2;
            case 44100:
                return 3;
        }
        return -1; //?
    }

    @Override
    public boolean getSoundType() {
        return channels == 2;
    }

    @Override
    public List<ByteArrayRange> getRawSoundData() {
        List<ByteArrayRange> ret = new ArrayList<>();
        Path soundPath = getSwf().getFile() == null ? null : Paths.get(getSwf().getFile()).getParent().resolve(Paths.get(fileName));
        if (soundPath == null || !soundPath.toFile().exists()) {
            ret.add(new ByteArrayRange(new byte[]{}));
            return ret;
        }
        try (FileInputStream fis = new FileInputStream(soundPath.toFile()); AudioInputStream audioIs = AudioSystem.getAudioInputStream(new BufferedInputStream(fis))) {
            ret.add(new ByteArrayRange(Helper.readStream(audioIs)));
            return ret;
        } catch (IOException | UnsupportedAudioFileException iex) {
            ret.add(new ByteArrayRange(new byte[]{}));
            return ret;
        }

    }

    @Override
    public int getSoundFormatId() {
        return SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN;
    }

    @Override
    public long getTotalSoundSampleCount() {
        return sampleCount;
    }

    @Override
    public boolean getSoundSize() {
        return bits == 16;
    }

    @Override
    public SoundFormat getSoundFormat() {
        final int[] rateMap = {5512, 11025, 22050, 44100};
        return new SoundFormat(getSoundFormatId(), rateMap[getSoundRate()], getSoundType());
    }

    @Override
    public void setSoundSize(boolean soundSize) {
        if (soundSize) {
            bits = 16;
        } else {
            bits = 8;
        }
    }

    @Override
    public void setSoundType(boolean soundType) {
        if (soundType) {
            channels = 2;
        } else {
            channels = 1;
        }
    }

    @Override
    public void setSoundSampleCount(long soundSampleCount) {
        this.sampleCount = soundSampleCount;
    }

    @Override
    public void setSoundCompression(int soundCompression) {
        //unsupported
    }

    @Override
    public void setSoundRate(int soundRate) {
        final int[] rateMap = {5512, 11025, 22050, 44100};
        this.sampleRate = rateMap[soundRate];
    }

    @Override
    public void getTagInfo(TagInfo tagInfo) {
        super.getTagInfo(tagInfo);
        SoundFormat soundFormat = getSoundFormat();
        tagInfo.addInfo("general", "exportName", exportName);
        tagInfo.addInfo("general", "fileName", fileName);
        tagInfo.addInfo("general", "codecName", soundFormat.getFormatName());
        tagInfo.addInfo("general", "exportFormat", soundFormat.getNativeExportFormat());
        tagInfo.addInfo("general", "samplingRate", soundFormat.samplingRate);
        tagInfo.addInfo("general", "stereo", soundFormat.stereo);
        tagInfo.addInfo("general", "sampleCount", sampleCount);
    }

    @Override
    public String getFlaExportName() {
        return "sound" + getCharacterId();
    }

    @Override
    public int getInitialLatency() {
        return 0;
    }
}
