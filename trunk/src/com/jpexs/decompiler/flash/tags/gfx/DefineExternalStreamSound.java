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
public class DefineExternalStreamSound extends Tag {

    public static final int ID = 1007;
    public int soundFormat;
    public int bits;
    public int channels;
    public long sampleRate;
    public long sampleCount;
    public long seekSample;
    public long startFrame;
    public long lastFrame;
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
            sos.writeUI16(soundFormat);
            sos.writeUI16(bits);
            sos.writeUI16(channels);
            sos.writeUI32(sampleRate);
            sos.writeUI32(sampleCount);
            sos.writeUI32(seekSample);
            sos.writeUI32(startFrame);
            sos.writeUI32(lastFrame);
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
     * @param pos
     * @throws IOException
     */
    public DefineExternalStreamSound(SWF swf, byte[] data, long pos) throws IOException {
        super(swf, ID, "DefineExternalStreamSound", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        soundFormat = sis.readUI16();
        bits = sis.readUI16();
        channels = sis.readUI16();
        sampleRate = sis.readUI32();
        sampleCount = sis.readUI32();
        seekSample = sis.readUI32();
        startFrame = sis.readUI32();
        lastFrame = sis.readUI32();
        int fileNameLen = sis.readUI8();
        fileName = new String(sis.readBytesEx(fileNameLen));

    }
}
