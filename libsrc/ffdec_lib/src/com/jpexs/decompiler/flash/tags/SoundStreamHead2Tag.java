/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author JPEXS
 */
public class SoundStreamHead2Tag extends CharacterIdTag implements SoundStreamHeadTypeTag {

    @Reserved
    @SWFType(value = BasicType.UB, count = 4)
    public int reserved;

    @SWFType(value = BasicType.UB, count = 2)
    public int playBackSoundRate;

    public boolean playBackSoundSize;

    public boolean playBackSoundType;

    @SWFType(value = BasicType.UB, count = 4)
    public int streamSoundCompression;

    @SWFType(value = BasicType.UB, count = 2)
    public int streamSoundRate;

    public boolean streamSoundSize;

    public boolean streamSoundType;

    @SWFType(BasicType.UI16)
    public int streamSoundSampleCount;

    @SWFType(BasicType.SI16)
    @Conditional(value = "streamSoundCompression", options = {2})
    public int latencySeek;

    @Internal
    private int virtualCharacterId = 0;

    public static final int ID = 45;

    @Override
    public int getCharacterId() {
        return virtualCharacterId;
    }

    @Override
    public String getExportFormat() {
        if (streamSoundCompression == SoundFormat.FORMAT_MP3) {
            return "mp3";
        }
        if (streamSoundCompression == SoundFormat.FORMAT_ADPCM) {
            return "wav";
        }
        if (streamSoundCompression == SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN) {
            return "wav";
        }
        if (streamSoundCompression == SoundFormat.FORMAT_UNCOMPRESSED_NATIVE_ENDIAN) {
            return "wav";
        }
        if (streamSoundCompression == SoundFormat.FORMAT_NELLYMOSER || streamSoundCompression == SoundFormat.FORMAT_NELLYMOSER16KHZ || streamSoundCompression == SoundFormat.FORMAT_NELLYMOSER8KHZ) {
            return "wav";
        }
        return "flv";
    }

    @Override
    public long getSoundSampleCount() {
        return streamSoundSampleCount;
    }

    @Override
    public void setVirtualCharacterId(int ch) {
        virtualCharacterId = ch;
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
            sos.writeUB(4, reserved);
            sos.writeUB(2, playBackSoundRate);
            sos.writeUB(1, playBackSoundSize ? 1 : 0);
            sos.writeUB(1, playBackSoundType ? 1 : 0);
            sos.writeUB(4, streamSoundCompression);
            sos.writeUB(2, streamSoundRate);
            sos.writeUB(1, streamSoundSize ? 1 : 0);
            sos.writeUB(1, streamSoundType ? 1 : 0);
            sos.writeUI16(streamSoundSampleCount);
            if (streamSoundCompression == 2) {
                sos.writeSI16(latencySeek);
            }
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public SoundStreamHead2Tag(SWF swf) {
        super(swf, ID, "SoundStreamHead2", null);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public SoundStreamHead2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "SoundStreamHead2", data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        reserved = (int) sis.readUB(4, "reserved");
        playBackSoundRate = (int) sis.readUB(2, "playBackSoundRate");
        playBackSoundSize = sis.readUB(1, "playBackSoundSize") == 1;
        playBackSoundType = sis.readUB(1, "playBackSoundType") == 1;
        streamSoundCompression = (int) sis.readUB(4, "streamSoundCompression");
        streamSoundRate = (int) sis.readUB(2, "streamSoundRate");
        streamSoundSize = sis.readUB(1, "streamSoundSize") == 1;
        streamSoundType = sis.readUB(1, "streamSoundType") == 1;
        streamSoundSampleCount = sis.readUI16("streamSoundSampleCount");
        if (streamSoundCompression == 2) {
            latencySeek = sis.readSI16("latencySeek");
        }
    }

    @Override
    public int getSoundFormatId() {
        return streamSoundCompression;
    }

    @Override
    public int getSoundRate() {
        return streamSoundRate;
    }

    @Override
    public boolean getSoundSize() {
        return streamSoundSize;
    }

    @Override
    public boolean getSoundType() {
        return streamSoundType;
    }

    @Override
    public List<SoundStreamBlockTag> getBlocks() {
        List<SoundStreamBlockTag> ret = new ArrayList<>();
        SoundStreamHeadTag.populateSoundStreamBlocks(0, swf.tags, this, ret);
        return ret;

    }

    @Override
    public boolean importSupported() {
        return false;
    }

    @Override
    public boolean setSound(InputStream is, int newSoundFormat) {
        return false;
    }

    @Override
    public List<byte[]> getRawSoundData() {
        List<byte[]> ret = new ArrayList<>();
        List<SoundStreamBlockTag> blocks = getBlocks();
        for (SoundStreamBlockTag block : blocks) {
            if (streamSoundCompression == SoundFormat.FORMAT_MP3) {
                ret.add(block.streamSoundData.getRangeData(4, block.streamSoundData.getLength() - 4));
            } else {
                ret.add(block.streamSoundData.getRangeData());
            }
        }
        return ret;
    }

    @Override
    public long getTotalSoundSampleCount() {
        return getBlocks().size() * streamSoundSampleCount;
    }

    @Override
    public SoundFormat getSoundFormat() {
        final int[] rateMap = {5512, 11025, 22050, 44100};
        return new SoundFormat(getSoundFormatId(), rateMap[getSoundRate()], getSoundType());
    }
}
