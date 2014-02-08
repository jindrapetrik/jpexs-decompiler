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
import com.jpexs.decompiler.flash.types.SOUNDINFO;
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
public class DefineButtonSoundTag extends CharacterTag {

    @SWFType(BasicType.UI16)
    public int buttonId;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar0;

    public SOUNDINFO buttonSoundInfo0;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar1;

    public SOUNDINFO buttonSoundInfo1;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar2;

    public SOUNDINFO buttonSoundInfo2;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar3;

    public SOUNDINFO buttonSoundInfo3;
    public static final int ID = 17;

    @Override
    public int getCharacterId() {
        return buttonId;
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
            sos.writeUI16(buttonId);
            sos.writeUI16(buttonSoundChar0);
            if (buttonSoundChar0 != 0) {
                sos.writeSOUNDINFO(buttonSoundInfo0);
            }
            sos.writeUI16(buttonSoundChar1);
            if (buttonSoundChar1 != 0) {
                sos.writeSOUNDINFO(buttonSoundInfo1);
            }
            sos.writeUI16(buttonSoundChar2);
            if (buttonSoundChar2 != 0) {
                sos.writeSOUNDINFO(buttonSoundInfo2);
            }
            sos.writeUI16(buttonSoundChar3);
            if (buttonSoundChar3 != 0) {
                sos.writeSOUNDINFO(buttonSoundInfo3);
            }
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
    public DefineButtonSoundTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineButtonSound", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        buttonId = sis.readUI16();
        buttonSoundChar0 = sis.readUI16();
        if (buttonSoundChar0 != 0) {
            buttonSoundInfo0 = sis.readSOUNDINFO();
        }
        buttonSoundChar1 = sis.readUI16();
        if (buttonSoundChar1 != 0) {
            buttonSoundInfo1 = sis.readSOUNDINFO();
        }
        buttonSoundChar2 = sis.readUI16();
        if (buttonSoundChar2 != 0) {
            buttonSoundInfo2 = sis.readSOUNDINFO();
        }
        buttonSoundChar3 = sis.readUI16();
        if (buttonSoundChar3 != 0) {
            buttonSoundInfo3 = sis.readSOUNDINFO();
        }
    }
}
