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
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * A SWC exported for some classes only must define those classes only.
 */
public class SwfToSwcExporterSelectClassTest {

    @Test
    public void testSelectClass() throws IOException, InterruptedException {
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3_new/bin/as3_new.flex.swf")), false);
        File swcFile = File.createTempFile("ffdec-selectclass", ".swc");
        try {
            new SwfToSwcExporter().exportSwf(swf, swcFile, false, Arrays.asList("tests_classes.mypackage1.TestClass"));
            String catalog = readCatalog(swcFile);
            assertNotNull(catalog);
            assertTrue(catalog.contains("<def id=\"tests_classes.mypackage1:TestClass\" />"));
            assertFalse(catalog.contains("<def id=\"tests_classes.mypackage1:TestClass2\" />"));
            assertFalse(catalog.contains("<def id=\"tests:TestHello\" />"));
        } finally {
            swcFile.delete();
        }
    }

    private static String readCatalog(File swcFile) throws IOException {
        try (InputStream is = new FileInputStream(swcFile); ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("catalog.xml")) {
                    return new String(Helper.readStream(zis), "UTF-8");
                }
            }
        }
        return null;
    }
}
