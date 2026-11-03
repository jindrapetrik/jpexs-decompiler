/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.SWFCompression;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * The library.swf of an exported SWC must be uncompressed, as compc writes it.
 */
public class SwfToSwcExporterCompressionTest {

    @Test
    public void testLibraryUncompressed() throws IOException, InterruptedException {
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3_new/bin/as3_new.flex.swf")), false);
        swf.compression = SWFCompression.LZMA;
        File swcFile = File.createTempFile("ffdec-swc-compression", ".swc");
        try {
            new SwfToSwcExporter().exportSwf(swf, swcFile, false);
            assertEquals(readLibrarySignature(swcFile), "FWS");
        } finally {
            swcFile.delete();
        }
    }

    private static String readLibrarySignature(File swcFile) throws IOException {
        try (InputStream is = new FileInputStream(swcFile); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("library.swf")) {
                    byte[] signature = new byte[3];
                    int read = 0;
                    while (read < 3) {
                        int r = zis.read(signature, read, 3 - read);
                        if (r < 0) {
                            break;
                        }
                        read += r;
                    }
                    return new String(signature, 0, read, "ISO-8859-1");
                }
            }
        }
        return null;
    }
}
