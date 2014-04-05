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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
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
public class CSMTextSettingsTag extends Tag {

    @SWFType(BasicType.UI16)
    public int textID;

    @SWFType(value = BasicType.UB, count = 2)
    public int useFlashType;

    @SWFType(value = BasicType.UB, count = 3)
    public int gridFit;

    @Reserved
    @SWFType(value = BasicType.UB, count = 3)
    public int reserved;

    @SWFType(value = BasicType.FLOAT)  //F32 = FLOAT
    public float thickness;

    @SWFType(value = BasicType.FLOAT)  //F32 = FLOAT
    public float sharpness;

    @Reserved
    @SWFType(BasicType.UI8)
    public int reserved2;

    public static final int ID = 74;

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
            sos.writeUI16(textID);
            sos.writeUB(2, useFlashType);
            sos.writeUB(3, gridFit);
            sos.writeUB(3, reserved);
            sos.writeFLOAT(thickness); //F32 = FLOAT
            sos.writeFLOAT(sharpness); //F32 = FLOAT
            sos.writeUI8(reserved2);
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
    public CSMTextSettingsTag(SWF swf, byte[] headerData, byte[] data, long pos) throws IOException {
        super(swf, ID, "CSMTextSettings", headerData, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version);
        textID = sis.readUI16();
        useFlashType = (int) sis.readUB(2);
        gridFit = (int) sis.readUB(3);
        reserved = (int) sis.readUB(3);
        thickness = sis.readFLOAT(); //F32 = FLOAT
        sharpness = sis.readFLOAT(); //F32 = FLOAT
        reserved2 = sis.readUI8(); //reserved
    }

    /**
     * Returns string representation of the object
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "CSMTextSettings";
    }
}
