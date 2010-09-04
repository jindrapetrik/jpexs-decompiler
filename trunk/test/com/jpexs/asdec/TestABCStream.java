package com.jpexs.asdec;

import com.jpexs.asdec.abc.ABCInputStream;
import com.jpexs.asdec.abc.ABCOutputStream;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestABCStream extends TestCase {
    @Test
    public void testU30() {
        try {
            long number = 1531;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ABCOutputStream aos = new ABCOutputStream(baos);
            aos.writeU30(number);
            aos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ABCInputStream ais = new ABCInputStream(bais);
            assertEquals(number, ais.readU30());
            assertEquals(0, bais.available());
            ais.close();
        } catch (IOException ex) {
            fail();
        }
    }
}
