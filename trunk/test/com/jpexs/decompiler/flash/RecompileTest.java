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

import com.jpexs.decompiler.flash.abc.NotSameException;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class RecompileTest {

    public static final String TESTDATADIR = "testdata/recompile";

    private void testRecompileOne(String filename) {
        try {
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(TESTDATADIR + File.separator + filename)), false);
            Configuration.debugCopy.set(true);
            swf.saveTo(new ByteArrayOutputStream());
        } catch (IOException | InterruptedException ex) {
            fail();
        } catch (NotSameException ex) {
            fail("File is different after recompiling: " + filename);
        }
    }

    @Test
    public void testRecompile() {
        File dir = new File(TESTDATADIR);
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".swf");
            }
        });
        for (File f : files) {
            testRecompileOne(f.getName());
        }
    }
}
