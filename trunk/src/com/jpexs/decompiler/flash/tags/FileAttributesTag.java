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

public class FileAttributesTag extends Tag {

    public boolean useDirectBlit;
    public boolean useGPU;
    public boolean hasMetadata;
    public boolean actionScript3;
    public boolean useNetwork;
    public boolean noCrossDomainCache;
    private boolean reserved1;
    private boolean reserved2;
    
    @SWFType(value=BasicType.UB,count=24)
    private int reserved3;
    public static final int ID = 69;

    public FileAttributesTag() {
        super(null, ID, "FileAttributes", new byte[]{}, 0);
    }

    public FileAttributesTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "FileAttributes", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        reserved1 = sis.readUB(1)==1; // reserved
        // UB[1] == 0  (reserved)
        useDirectBlit = sis.readUB(1) != 0;
        useGPU = sis.readUB(1) != 0;
        hasMetadata = sis.readUB(1) != 0;
        actionScript3 = sis.readUB(1) != 0;
        noCrossDomainCache = sis.readUB(1) != 0;
        reserved2 = sis.readUB(1)==1; // reserved
        useNetwork = sis.readUB(1) != 0;
        // UB[24] == 0 (reserved)
        reserved3 = (int) sis.readUB(24); //reserved
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
            sos.writeUB(1, reserved1?1:0); //reserved
            sos.writeUB(1, useDirectBlit ? 1 : 0);
            sos.writeUB(1, useGPU ? 1 : 0);
            sos.writeUB(1, hasMetadata ? 1 : 0);
            sos.writeUB(1, actionScript3 ? 1 : 0);
            sos.writeUB(1, noCrossDomainCache ? 1 : 0);
            sos.writeUB(1, reserved2?1:0); //reserved
            sos.writeUB(1, useNetwork ? 1 : 0);
            sos.writeUB(24, reserved3); //reserved
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }
}
