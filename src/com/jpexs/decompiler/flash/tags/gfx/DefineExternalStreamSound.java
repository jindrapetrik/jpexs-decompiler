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
import com.jpexs.helpers.ByteArrayRange;
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
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineExternalStreamSound(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineExternalStreamSound", data);
        soundFormat = sis.readUI16("soundFormat");
        bits = sis.readUI16("bits");
        channels = sis.readUI16("channels");
        sampleRate = sis.readUI32("sampleRate");
        sampleCount = sis.readUI32("sampleCount");
        seekSample = sis.readUI32("seekSample");
        startFrame = sis.readUI32("startFrame");
        lastFrame = sis.readUI32("lastFrame");
        int fileNameLen = sis.readUI8("fileNameLen");
        fileName = new String(sis.readBytesEx(fileNameLen, "fileName"));

    }
}
