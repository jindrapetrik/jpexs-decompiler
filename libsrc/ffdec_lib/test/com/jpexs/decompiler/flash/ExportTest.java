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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ExportTest extends FileTestBase {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration._debugCopy.set(false);
    }

    public static final String TESTDATADIR = "testdata/decompile";

    public static Handler loggerHandler;

    @BeforeClass
    public void addLogger() {
        Logger logger = Logger.getLogger("");
        loggerHandler = new Handler() {
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
        };

        logger.addHandler(loggerHandler);
    }

    @AfterClass
    public void removeLogger() {
        if (loggerHandler != null) {
            Logger logger = Logger.getLogger("");
            logger.removeHandler(loggerHandler);
        }
    }

    @Test(dataProvider = "provideFiles")
    public void testDecompileAS(String filePath) {
        testDecompile(filePath, ScriptExportMode.AS);
    }

    @Test(dataProvider = "provideFiles")
    public void testDecompilePcode(String filePath) {
        testDecompile(filePath, ScriptExportMode.PCODE);
    }

    public void testDecompile(String filePath, ScriptExportMode exportMode) {
        try {
            File f = new File(filePath);
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false);
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

            }, fdir.getAbsolutePath(), new ScriptExportSettings(exportMode, false), false, null);
        } catch (Exception ex) {
            fail("Exception during decompilation: " + filePath + " " + ex.getMessage());
        }
    }

    @Override
    public String[] getTestDataDirs() {
        return new String[]{TESTDATADIR, FREE_ACTIONSCRIPT_AS2, FREE_ACTIONSCRIPT_AS3};
    }
}
