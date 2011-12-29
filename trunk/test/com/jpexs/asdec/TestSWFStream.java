package com.jpexs.asdec;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestSWFStream extends TestCase {

    @Test
    public void testNeededBits() {
        assertEquals(2, SWFOutputStream.getNeededBitsU(3));
        assertEquals(8, SWFOutputStream.getNeededBitsU(255));
        assertEquals(3, SWFOutputStream.getNeededBitsS(3));
        assertEquals(9, SWFOutputStream.getNeededBitsS(255));
        assertEquals(2, SWFOutputStream.getNeededBitsS(-2));
        assertEquals(11, SWFOutputStream.getNeededBitsS(-597));
        assertEquals(21, SWFOutputStream.getNeededBitsF(15.5f));
        assertEquals(14, SWFOutputStream.getNeededBitsF(0.1f));
        assertEquals(19, SWFOutputStream.getNeededBitsF(-2.8891602f));
    }

    @Test
    public void testFB() throws IOException {
        double f = 5.25;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, 10);
        sos.writeFB(20, f);
        sos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SWFInputStream sis = new SWFInputStream(bais, 10);
        assertTrue(Double.compare(f, sis.readFB(20)) == 0);
        sis.close();
    }

    @Test
    public void testUB() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, 10);
        sos.writeUB(5, 1);
        sos.writeUB(6, 2);
        sos.writeUB(7, 3);
        sos.writeUB(8, 4);
        sos.writeUB(9, 5);
        sos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SWFInputStream sis = new SWFInputStream(bais, 10);
        assertEquals(1, sis.readUB(5));
        assertEquals(2, sis.readUB(6));
        assertEquals(3, sis.readUB(7));
        assertEquals(4, sis.readUB(8));
        assertEquals(5, sis.readUB(9));
        sis.close();
    }

    @Test
    public void testSB() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, 10);
        sos.writeSB(5, -1);
        sos.writeSB(6, 2);
        sos.writeSB(7, -3);
        sos.writeSB(8, 4);
        sos.writeSB(9, -5);
        sos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SWFInputStream sis = new SWFInputStream(bais, 10);
        assertEquals(-1, sis.readSB(5));
        assertEquals(2, sis.readSB(6));
        assertEquals(-3, sis.readSB(7));
        assertEquals(4, sis.readSB(8));
        assertEquals(-5, sis.readSB(9));
        sis.close();
    }


    @Test
    public void testFLOATAndDouble() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, 10);
        float f = 5.25f;
        sos.writeFLOAT(f);
        sos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SWFInputStream sis = new SWFInputStream(bais, 10);
        assertEquals(f, sis.readFLOAT());
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, 10);
        f = 5.25f;
        sos.writeFLOAT16(f);
        sos.close();
        bais = new ByteArrayInputStream(baos.toByteArray());
        sis = new SWFInputStream(bais, 10);
        assertEquals(f, sis.readFLOAT16());
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, 10);
        double d = 5.25;
        sos.writeDOUBLE(d);
        sos.close();
        bais = new ByteArrayInputStream(baos.toByteArray());
        sis = new SWFInputStream(bais, 10);
        assertEquals(d, sis.readDOUBLE());
        sis.close();
    }

    @Test
    public void testFIXEDandFIXED8() throws IOException {
        //example from specification
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{(byte) 0x00, (byte) 0x80, (byte) 0x07, (byte) 0x00});
        SWFInputStream sis = new SWFInputStream(bais, 10);
        assertTrue(Double.compare(7.5, sis.readFIXED()) == 0);
        sis.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, 10);
        double dd = 5.25;
        sos.writeFIXED(dd);
        sos.close();
        bais = new ByteArrayInputStream(baos.toByteArray());
        sis = new SWFInputStream(bais, 10);
        assertTrue(Double.compare(dd, sis.readFIXED()) == 0);
        sis.close();

        baos = new ByteArrayOutputStream();
        sos = new SWFOutputStream(baos, 10);
        float ff = 5.25f;
        sos.writeFIXED8(ff);
        sos.close();
        bais = new ByteArrayInputStream(baos.toByteArray());
        sis = new SWFInputStream(bais, 10);
        assertEquals(ff, sis.readFIXED8());
        sis.close();
    }
}
