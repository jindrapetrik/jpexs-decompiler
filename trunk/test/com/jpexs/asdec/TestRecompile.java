package com.jpexs.asdec;

import com.jpexs.asdec.abc.NotSameException;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;

public class TestRecompile extends TestCase {
    public static final String TESTDATADIR = "testdata";

    private void testRecompileOne(String filename) {
        try {
            SWF swf = new SWF(new FileInputStream(TESTDATADIR + File.separator + filename));
            Main.DEBUG_COPY = true;
            swf.saveTo(new ByteArrayOutputStream());
        } catch (IOException ex) {
            fail();
        } catch (NotSameException ex) {
            fail("File is different after recompiling: " + filename);
        }
    }

    @Test
    public void testRecompile() {
        File dir=new File(TESTDATADIR);
        File files[]=dir.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".swf");
            }
        });
        for(File f:files){
            testRecompileOne(f.getAbsolutePath());
        }
    }

}
