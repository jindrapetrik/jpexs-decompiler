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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.Tag;
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
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
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
     * @param sis
     * @param length
     * @param pos
     * @throws IOException
     */
    public DefineExternalSound(SWFInputStream sis, long pos, int length) throws IOException {
        super(sis.getSwf(), ID, "DefineExternalSound", pos, length);
        characterId = sis.readUI16("characterId");
        soundFormat = sis.readUI16("soundFormat");
        bits = sis.readUI16("bits");
        channels = sis.readUI16("channels");
        sampleRate = sis.readUI32("sampleRate");
        sampleCount = sis.readUI32("sampleCount");
        seekSample = sis.readUI32("seekSample");
        int exportNameLen = sis.readUI8("exportNameLen");
        exportName = new String(sis.readBytesEx(exportNameLen, "exportName"));
        int fileNameLen = sis.readUI8("fileNameLen");
        fileName = new String(sis.readBytesEx(fileNameLen, "fileName"));

    }
}
