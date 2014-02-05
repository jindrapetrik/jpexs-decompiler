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
    
    @SWFType(value=BasicType.UB,count=2)
    public int useFlashType;
    
    @SWFType(value=BasicType.UB,count=3)
    public int gridFit;
    
    @SWFType(value=BasicType.FLOAT)  //F32 = FLOAT
    public float thickness;
    
    @SWFType(value=BasicType.FLOAT)  //F32 = FLOAT
    public float sharpness;
    
    public static final int ID = 74;

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
            sos.writeUI16(textID);
            sos.writeUB(2, useFlashType);
            sos.writeUB(3, gridFit);
            sos.writeUB(3, 0);
            sos.writeFLOAT(thickness); //F32 = FLOAT
            sos.writeFLOAT(sharpness); //F32 = FLOAT
            sos.writeUI8(0);
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
    public CSMTextSettingsTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "CSMTextSettings", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        textID = sis.readUI16();
        useFlashType = (int) sis.readUB(2);
        gridFit = (int) sis.readUB(3);
        sis.readUB(3); //reserved
        thickness = sis.readFLOAT(); //F32 = FLOAT
        sharpness = sis.readFLOAT(); //F32 = FLOAT
        sis.readUI8(); //reserved
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
