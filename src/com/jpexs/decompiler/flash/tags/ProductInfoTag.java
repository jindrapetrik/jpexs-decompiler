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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProductInfoTag extends Tag {

    @SWFType(BasicType.UI32)
    public long productID;
    @SWFType(BasicType.UI32)
    public long edition;
    @SWFType(BasicType.UI8)
    public int majorVersion;
    @SWFType(BasicType.UI8)
    public int minorVersion;
    @SWFType(BasicType.UI32)
    public long buildLow;
    @SWFType(BasicType.UI32)
    public long buildHigh;
    @SWFType(BasicType.UI32)
    public long compilationDateLow;
    @SWFType(BasicType.UI32)
    public long compilationDateHigh;
    public static final int ID = 41;

    public ProductInfoTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "ProductInfo", data);
        /*
         * 0: Unknown
         * 1: Macromedia Flex for J2EE
         * 2: Macromedia Flex for .NET
         * 3: Adobe Flex
         */
        productID = sis.readUI32("productID");

        /*
         * 0: Developer Edition
         * 1: Full Commercial Edition
         * 2: Non Commercial Edition
         * 3: Educational Edition
         * 4: Not For Resale (NFR) Edition
         * 5: Trial Edition
         * 6: None
         */
        edition = sis.readUI32("edition");
        majorVersion = sis.readUI8("majorVersion");
        minorVersion = sis.readUI8("minorVersion");
        buildLow = sis.readUI32("buildLow");
        buildHigh = sis.readUI32("buildHigh");
        compilationDateLow = sis.readUI32("compilationDateLow");
        compilationDateHigh = sis.readUI32("compilationDateHigh");
    }

    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI32(productID);
            sos.writeUI32(edition);
            sos.writeUI8(majorVersion);
            sos.writeUI8(minorVersion);
            sos.writeUI32(buildLow);
            sos.writeUI32(buildHigh);
            sos.writeUI32(compilationDateLow);
            sos.writeUI32(compilationDateHigh);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

}
