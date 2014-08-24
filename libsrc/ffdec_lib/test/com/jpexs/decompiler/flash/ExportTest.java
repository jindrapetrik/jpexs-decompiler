/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
//import com.jpexs.decompiler.flash.gui.Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ExportTest {

    @BeforeClass
    public void init() {
        //Main.initLogging(false);
    }

    public static final String TESTDATADIR = "testdata/decompile";

    @BeforeClass
    public void addLogger() {
        Configuration.autoDeobfuscate.set(true);
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
        File[] files = dir.listFiles(new FilenameFilter() {
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
    public void testDecompileAS(File f) {
        testDecompile(f, ScriptExportMode.AS);
    }

    @Test(dataProvider = "swfFiles")
    public void testDecompilePcode(File f) {
        testDecompile(f, ScriptExportMode.PCODE);
    }

    public void testDecompile(File f, ScriptExportMode exportMode) {
        try {
            SWF swf = new SWF(new FileInputStream(f), false);
            Configuration.debugCopy.set(true);
            String folderName = exportMode == ScriptExportMode.AS ? "output" : "outputp";
            File fdir = new File(TESTDATADIR + File.separator + folderName + File.separator + f.getName());
            fdir.mkdirs();

            swf.exportActionScript(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable thrown) {
                    return AbortRetryIgnoreHandler.ABORT;
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }

            }, fdir.getAbsolutePath(), exportMode, false);
        } catch (Exception ex) {
            fail();
        }
    }
}
