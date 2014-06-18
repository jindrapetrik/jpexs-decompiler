/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayInputStream;
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
    public void init(){
        Main.initLogging(false);
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
        try (SWFInputStream sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION)) {
            assertTrue(Double.compare(f, sis.readFB(20)) == 0);
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
        try (SWFInputStream sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION)) {
            assertEquals(1, sis.readUB(5));
            assertEquals(2, sis.readUB(6));
            assertEquals(3, sis.readUB(7));
            assertEquals(4, sis.readUB(8));
            assertEquals(5, sis.readUB(9));
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
        try (SWFInputStream sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION)) {
            assertEquals(-1, sis.readSB(5));
            assertEquals(2, sis.readSB(6));
            assertEquals(-3, sis.readSB(7));
            assertEquals(4, sis.readSB(8));
            assertEquals(-5, sis.readSB(9));
        }
    }

    @Test
    public void testFLOATAndDouble() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        float f = 5.25f;
        sos.writeFLOAT(f);
        sos.close();
        SWFInputStream sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION);
        assertEquals(f, sis.readFLOAT());
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        f = 5.25f;
        sos.writeFLOAT16(f);
        sos.close();
        sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION);
        assertEquals(f, sis.readFLOAT16());
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        double d = 5.25;
        sos.writeDOUBLE(d);
        sos.close();
        sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION);
        assertEquals(d, sis.readDOUBLE());
        sis.close();
    }

    @Test
    public void testFIXEDandFIXED8() throws IOException {
        //example from specification
        byte[] data = new byte[]{(byte) 0x00, (byte) 0x80, (byte) 0x07, (byte) 0x00};
        SWFInputStream sis = new SWFInputStream(data, SWF.DEFAULT_VERSION);
        assertTrue(Double.compare(7.5, sis.readFIXED()) == 0);
        sis.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        double dd = 5.25;
        sos.writeFIXED(dd);
        sos.close();
        sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION);
        assertTrue(Double.compare(dd, sis.readFIXED()) == 0);
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION);
        float ff = 5.25f;
        sos.writeFIXED8(ff);
        sos.close();
        sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION);
        assertEquals(ff, sis.readFIXED8());
        sis.close();
    }

    @Test
    public void testRECT() throws IOException {
        RECT rect;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION)) {
            rect = new RECT(-0x80000000, 0x7FFFFFFF, -0x80000000, 0x7FFFFFFF);
            sos.writeRECT(rect);
        }
        try (SWFInputStream sis = new SWFInputStream(baos.toByteArray(), SWF.DEFAULT_VERSION)) {
            RECT readRECT = sis.readRECT();
            assertEquals(readRECT.Xmin, -0x3FFFFFFF);
            assertEquals(readRECT.Xmax, 0x3FFFFFFF);
            assertEquals(readRECT.Ymin, -0x3FFFFFFF);
            assertEquals(readRECT.Ymax, 0x3FFFFFFF);
        }
    }
}
