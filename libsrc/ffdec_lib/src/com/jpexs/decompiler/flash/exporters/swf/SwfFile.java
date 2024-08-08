/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.SWF;
import java.io.IOException;

/**
 * Swf file. ???
 *
 * @author JPEXS
 */
public class SwfFile {

    /**
     * Gets SWF
     * @return SWF
     */
    public SWF getSwf() {
        return null;
    }

    /**
     * Saves SWF to file
     *
     * @param fileName File name
     * @throws IOException On I/O error
     */
    public void saveTo(String fileName) throws IOException {
        /*SWF swf = getSwf();
         try (FileOutputStream fos = new FileOutputStream(fileName)) {
         swf.saveTo(fos, SWFCompression.ZLIB);
         }*/
    }
}
