package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.ABCOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ABCStreamTest {

    @Test
    public void testU30() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ABCOutputStream aos = new ABCOutputStream(baos);) {
            long number = 1531;
            aos.writeU30(number);
            aos.close();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    ABCInputStream ais = new ABCInputStream(bais);) {
                assertEquals(number, ais.readU30());
                assertEquals(0, bais.available());
            }
        } catch (IOException ex) {
            fail();
        }
    }
}
