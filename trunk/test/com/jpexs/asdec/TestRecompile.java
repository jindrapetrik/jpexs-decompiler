package com.jpexs.asdec;

import com.jpexs.asdec.abc.NotSameException;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestRecompile extends TestCase {
    public static final String TESTDATADIR = "testdata";

    private void testRecompile(String filename) {
        try {
            SWF swf = new SWF(new FileInputStream(TESTDATADIR + File.separator + filename));
            Main.DEBUG_COPY = true;
            swf.saveTo(new ByteArrayOutputStream());
        } catch (IOException ex) {
            fail();
        } catch (NotSameException ex) {
            //ex.printStackTrace();
            fail("File is different after recompiling: " + filename);
        }
    }

    @Test
    public void testRecompile1() {
        testRecompile("01.swf");
    }

    @Test
    public void testRecompile2() {
        testRecompile("02.swf");
    }

    @Test
    public void testRecompile3() {
        testRecompile("03.swf");
    }
}
