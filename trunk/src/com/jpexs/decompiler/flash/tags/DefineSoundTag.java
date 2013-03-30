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
public class DefineSoundTag extends CharacterTag {

    public int soundId;
    public int soundFormat;
    public static final int FORMAT_UNCOMPRESSED_NATIVE_ENDIAN = 0;
    public static final int FORMAT_ADPCM = 1;
    public static final int FORMAT_MP3 = 2;
    public static final int FORMAT_UNCOMPRESSED_LITTLE_ENDIAN = 3;
    public static final int FORMAT_NELLYMOSER16KHZ = 4;
    public static final int FORMAT_NELLYMOSER8KHZ = 5;
    public static final int FORMAT_NELLYMOSER = 6;
    public static final int FORMAT_SPEEX = 11;
    public int soundRate;
    public int soundSize;
    public int soundType;
    public long soundSampleCount;
    public byte soundData[];

    @Override
    public int getCharacterID() {
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
            sos.writeUB(1, soundSize);
            sos.writeUB(1, soundType);
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
     * @throws IOException
     */
    public DefineSoundTag(byte data[], int version, long pos) throws IOException {
        super(14, "DefineSound", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        soundId = sis.readUI16();
        soundFormat = (int) sis.readUB(4);
        soundRate = (int) sis.readUB(2);
        soundSize = (int) sis.readUB(1);
        soundType = (int) sis.readUB(1);
        soundSampleCount = sis.readUI32();
        soundData = sis.readBytes(sis.available());
    }
}
