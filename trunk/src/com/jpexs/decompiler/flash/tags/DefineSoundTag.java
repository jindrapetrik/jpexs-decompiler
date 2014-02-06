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
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineSoundTag extends CharacterTag {

    @SWFType(BasicType.UI16)
    public int soundId;

    @SWFType(value = BasicType.UB, count = 4)
    public int soundFormat;
    public static final int FORMAT_UNCOMPRESSED_NATIVE_ENDIAN = 0;
    public static final int FORMAT_ADPCM = 1;
    public static final int FORMAT_MP3 = 2;
    public static final int FORMAT_UNCOMPRESSED_LITTLE_ENDIAN = 3;
    public static final int FORMAT_NELLYMOSER16KHZ = 4;
    public static final int FORMAT_NELLYMOSER8KHZ = 5;
    public static final int FORMAT_NELLYMOSER = 6;
    public static final int FORMAT_SPEEX = 11;

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
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
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
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineSoundTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineSound", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        soundId = sis.readUI16();
        soundFormat = (int) sis.readUB(4);
        soundRate = (int) sis.readUB(2);
        soundSize = sis.readUB(1) == 1;
        soundType = sis.readUB(1) == 1;
        soundSampleCount = sis.readUI32();
        soundData = sis.readBytesEx(sis.available());
    }

    public String getExportFormat() {
        if (soundFormat == DefineSoundTag.FORMAT_MP3) {
            return "mp3";
        }
        if (soundFormat == DefineSoundTag.FORMAT_ADPCM) {
            return "wav";
        }
        return "flv";
    }
}
