/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash;

//import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class SWFStreamTest {

    @BeforeClass
    public void init() {
        //Main.initLogging(false);
    }

    @Test
    public void testNeededBits() {
        assertEquals(SWFOutputStream.getNeededBitsU(3), 2);
        assertEquals(SWFOutputStream.getNeededBitsU(255), 8);
        assertEquals(SWFOutputStream.getNeededBitsS(3), 3);
        assertEquals(SWFOutputStream.getNeededBitsS(255), 9);
        assertEquals(SWFOutputStream.getNeededBitsS(-2), 3);
        assertEquals(SWFOutputStream.getNeededBitsS(-597), 11);
        assertEquals(SWFOutputStream.getNeededBitsF(15.5f), 21);
        assertEquals(SWFOutputStream.getNeededBitsF(0.1f), 17);
        assertEquals(SWFOutputStream.getNeededBitsF(-2.8891602f), 19);
    }

    @Test
    public void testFB() throws IOException {
        double f = 5.25;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION)) {
            sos.writeFB(20, f);
        }
        try (SWFInputStream sis = new SWFInputStream(null, baos.toByteArray())) {
            assertTrue(Double.compare(f, sis.readFB(20, "test")) == 0);
        }
    }

    @Test
    public void testUB() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION)) {
            sos.writeUB(5, 1);
            sos.writeUB(6, 2);
            sos.writeUB(7, 3);
            sos.writeUB(8, 4);
            sos.writeUB(9, 5);
        }
        try (SWFInputStream sis = new SWFInputStream(null, baos.toByteArray())) {
            assertEquals(1, sis.readUB(5, "test"));
            assertEquals(2, sis.readUB(6, "test"));
            assertEquals(3, sis.readUB(7, "test"));
            assertEquals(4, sis.readUB(8, "test"));
            assertEquals(5, sis.readUB(9, "test"));
        }
    }

    @Test
    public void testSB() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION)) {
            sos.writeSB(5, -1);
            sos.writeSB(6, 2);
            sos.writeSB(7, -3);
            sos.writeSB(8, 4);
            sos.writeSB(9, -5);
        }
        try (SWFInputStream sis = new SWFInputStream(null, baos.toByteArray())) {
            assertEquals(-1, sis.readSB(5, "test"));
            assertEquals(2, sis.readSB(6, "test"));
            assertEquals(-3, sis.readSB(7, "test"));
            assertEquals(4, sis.readSB(8, "test"));
            assertEquals(-5, sis.readSB(9, "test"));
        }
    }

    @Test
    public void testFLOATAndDouble() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        float f = 5.25f;
        sos.writeFLOAT(f);
        sos.close();
        SWFInputStream sis = new SWFInputStream(null, baos.toByteArray());
        assertEquals(f, sis.readFLOAT("test"));
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        f = 5.25f;
        sos.writeFLOAT16(f);
        sos.close();
        sis = new SWFInputStream(null, baos.toByteArray());
        assertEquals(f, sis.readFLOAT16("test"));
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        double d = 5.25;
        sos.writeDOUBLE(d);
        sos.close();
        sis = new SWFInputStream(null, baos.toByteArray());
        assertEquals(d, sis.readDOUBLE("test"));
        sis.close();
    }

    @Test
    public void testFIXEDandFIXED8() throws IOException {
        //example from specification
        byte[] data = new byte[]{(byte) 0x00, (byte) 0x80, (byte) 0x07, (byte) 0x00};
        SWFInputStream sis = new SWFInputStream(null, data);
        assertTrue(Double.compare(7.5, sis.readFIXED("test")) == 0);
        sis.close();

        double[] dds = new double[]{5.25, 65535.25};
        for (double dd : dds) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
            sos.writeFIXED(dd);
            sos.close();
            sis = new SWFInputStream(null, baos.toByteArray());
            double dd2 = sis.readFIXED("test");
            assertTrue(Double.compare(dd, dd2) == 0, "Written and read value not equals. Written: " + dd + " read: " + dd2);
            sis.close();
        }

        float[] ffs = new float[]{5.25f, -5.25f, 127.75f, -128f};
        for (float ff : ffs) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
            sos.writeFIXED8(ff);
            sos.close();
            sis = new SWFInputStream(null, baos.toByteArray());
            float ff2 = sis.readFIXED8("test");
            assertEquals(ff, ff2, "Written and read value not equals. Written: " + ff + " read: " + ff2);
            sis.close();
        }
    }

    //@Test
    public void testAllFIXED8() throws IOException {
        for (int i = 0; i < 65536; i++) {
            byte[] data = new byte[]{(byte) (i % 256), (byte) (i / 256)};
            SWFInputStream sis = new SWFInputStream(null, data);
            float d = sis.readFIXED8("test");
            sis.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
            sos.writeFIXED8(d);
            sos.close();
            byte[] data2 = baos.toByteArray();

            assertTrue(data[0] == data2[0] && data[1] == data2[1]);
        }
    }

    @Test
    public void testRECT() throws IOException {
        RECT rect;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION)) {
            rect = new RECT(-0x80000000, 0x7FFFFFFF, -0x80000000, 0x7FFFFFFF);
            sos.writeRECT(rect);
        }
        try (SWFInputStream sis = new SWFInputStream(null, baos.toByteArray())) {
            RECT readRECT = sis.readRECT("test");
            assertEquals(readRECT.Xmin, -0x3FFFFFFF);
            assertEquals(readRECT.Xmax, 0x3FFFFFFF);
            assertEquals(readRECT.Ymin, -0x3FFFFFFF);
            assertEquals(readRECT.Ymax, 0x3FFFFFFF);
        }
    }
}
