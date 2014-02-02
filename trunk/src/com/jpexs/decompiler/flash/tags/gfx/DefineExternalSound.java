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
package com.jpexs.decompiler.flash.tags.gfx;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineExternalSound extends Tag {

    public static final int ID = 1006;
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
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
        try {
            sos.writeUI16(characterId);
            sos.writeUI16(soundFormat);
            sos.writeUI16(bits);
            sos.writeUI16(channels);
            sos.writeUI32(sampleRate);
            sos.writeUI32(sampleCount);
            sos.writeUI32(seekSample);
            byte[] exportNameBytes = exportName.getBytes();
            sos.writeUI8(exportNameBytes.length);
            sos.write(exportNameBytes);
            byte[] fileNameBytes = fileName.getBytes();
            sos.writeUI8(fileNameBytes.length);
            sos.write(fileNameBytes);
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineExternalSound(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineExternalSound", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterId = sis.readUI16();
        soundFormat = sis.readUI16();
        bits = sis.readUI16();
        channels = sis.readUI16();
        sampleRate = sis.readUI32();
        sampleCount = sis.readUI32();
        seekSample = sis.readUI32();
        int exportNameLen = sis.readUI8();
        exportName = new String(sis.readBytesEx(exportNameLen));
        int fileNameLen = sis.readUI8();
        fileName = new String(sis.readBytesEx(fileNameLen));

    }
}
