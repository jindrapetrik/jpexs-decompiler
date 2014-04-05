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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import java.io.ByteArrayInputStream;
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
public class SoundStreamHeadTag extends CharacterIdTag implements SoundStreamHeadTypeTag {

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
    @SWFType(value = BasicType.UI16)
    public int streamSoundSampleCount;
    @Conditional(value = "streamSoundCompression", options = {2})
    public int latencySeek;
    @Internal
    private int virtualCharacterId = 0;
    public static final int ID = 18;

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
    public int getCharacterId() {
        return virtualCharacterId;
    }

    @Override
    public void setVirtualCharacterId(int ch) {
        virtualCharacterId = ch;
    }

    @Override
    public long getSoundSampleCount() {
        return streamSoundSampleCount;
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
    public SoundStreamHeadTag(SWF swf, byte[] headerData, byte[] data, long pos) throws IOException {
        super(swf, ID, "SoundStreamHead", headerData, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        reserved = (int) sis.readUB(4);
        playBackSoundRate = (int) sis.readUB(2);
        playBackSoundSize = sis.readUB(1) == 1;
        playBackSoundType = sis.readUB(1) == 1;
        streamSoundCompression = (int) sis.readUB(4);
        streamSoundRate = (int) sis.readUB(2);
        streamSoundSize = sis.readUB(1) == 1;
        streamSoundType = sis.readUB(1) == 1;
        streamSoundSampleCount = sis.readUI16();
        if (streamSoundCompression == 2) {
            latencySeek = sis.readSI16();
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

    public static void populateSoundStreamBlocks(List<? extends ContainerItem> tags, Tag head, List<SoundStreamBlockTag> output) {
        boolean found = false;
        for (ContainerItem t : tags) {
            if (t == head) {
                found = true;
                continue;
            }
            if (!found) {
                continue;
            }
            if (t instanceof SoundStreamBlockTag) {
                output.add((SoundStreamBlockTag) t);
            }
            if (t instanceof SoundStreamHeadTypeTag) {
                break;
            }
            if (t instanceof Container) {
                populateSoundStreamBlocks(((Container) t).getSubItems(), head, output);
            }
        }
    }

    @Override
    public List<SoundStreamBlockTag> getBlocks() {
        List<SoundStreamBlockTag> ret = new ArrayList<>();
        populateSoundStreamBlocks(swf.tags, this, ret);
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
    public byte[] getRawSoundData() {
        List<SoundStreamBlockTag> blocks = getBlocks();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (SoundStreamBlockTag block : blocks) {
                if (streamSoundCompression == SoundFormat.FORMAT_MP3) {
                    baos.write(block.data, 4, block.data.length - 4);
                } else {
                    baos.write(block.data);
                }
            }
        } catch (IOException ex) {
            return null;
        }
        return baos.toByteArray();
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
