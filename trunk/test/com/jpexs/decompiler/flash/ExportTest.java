/*
 * Copyright (C) 2013 JPEXS
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ExportTest {

    public static final String TESTDATADIR = "testdata/decompile";

    @BeforeClass
    public void addLogger() {
        Configuration.setConfig("autoDeobfuscate", Boolean.TRUE);
        Logger logger = Logger.getLogger("");
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel() == Level.SEVERE) {
                    fail("Error during decompilation", record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

    @DataProvider(name = "swfFiles")
    public Object[][] createData() {
        File dir = new File(TESTDATADIR);
        if (!dir.exists()) {
            return new Object[0][0];
        }
        File files[] = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".swf");
            }
        });
        Object[][] ret = new Object[files.length][1];
        for (int i = 0; i < files.length; i++) {
            ret[i][0] = files[i];
        }
        return ret;
    }

    @Test(dataProvider = "swfFiles")
    public void testDecompile(File f) {
        try {
            SWF swf = new SWF(new FileInputStream(f), false);
            Configuration.DEBUG_COPY = true;
            File fdir = new File(TESTDATADIR + File.separator + "output" + File.separator + f.getName());
            fdir.mkdirs();

            swf.exportActionScript(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable thrown) {
                    return AbortRetryIgnoreHandler.ABORT;
                }
            }, fdir.getAbsolutePath(), false, false);
        } catch (Exception ex) {
            fail();
        }
    }
}
