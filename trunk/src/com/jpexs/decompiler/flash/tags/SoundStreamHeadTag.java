/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class SoundStreamHeadTag extends CharacterTag implements SoundStreamHeadTypeTag {

    public int playBackSoundRate;
    public int playBackSoundSize;
    public int playBackSoundType;
    public int streamSoundCompression;
    public int streamSoundRate;
    public int streamSoundSize;
    public int streamSoundType;
    public int streamSoundSampleCount;
    public int latencySeek;
    private int virtualCharacterId = 0;

    @Override
    public String getExportFormat() {
        if (streamSoundCompression == DefineSoundTag.FORMAT_MP3) {
            return "mp3";
        }
        if (streamSoundCompression == DefineSoundTag.FORMAT_ADPCM) {
            return "wav";
        }
        return "flv";
    }

    @Override
    public int getCharacterID() {
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
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUB(4, 0);//reserved
            sos.writeUB(2, playBackSoundRate);
            sos.writeUB(1, playBackSoundSize);
            sos.writeUB(1, playBackSoundType);
            sos.writeUB(4, streamSoundCompression);
            sos.writeUB(2, streamSoundRate);
            sos.writeUB(1, streamSoundSize);
            sos.writeUB(1, streamSoundType);
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
     * @param data Data bytes
     * @param version SWF version
     * @throws IOException
     */
    public SoundStreamHeadTag(byte data[], int version, long pos) throws IOException {
        super(18, "SoundStreamHead", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        sis.readUB(4);//reserved
        playBackSoundRate = (int) sis.readUB(2);
        playBackSoundSize = (int) sis.readUB(1);
        playBackSoundType = (int) sis.readUB(1);
        streamSoundCompression = (int) sis.readUB(4);
        streamSoundRate = (int) sis.readUB(2);
        streamSoundSize = (int) sis.readUB(1);
        streamSoundType = (int) sis.readUB(1);
        streamSoundSampleCount = sis.readUI16();
        if (streamSoundCompression == 2) {
            latencySeek = sis.readSI16();
        }
    }

    @Override
    public int getSoundFormat() {
        return streamSoundCompression;
    }

    @Override
    public int getSoundRate() {
        return streamSoundRate;
    }

    @Override
    public int getSoundSize() {
        return streamSoundSize;
    }

    @Override
    public int getSoundType() {
        return streamSoundType;
    }
}
