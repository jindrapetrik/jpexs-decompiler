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

import com.jpexs.decompiler.flash.abc.NotSameException;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.swf.SwfXmlExporter;
import com.jpexs.decompiler.flash.importers.SwfXmlImporter;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.helpers.Helper;
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
public class SwfXmlExportImportTest extends FileTestBase {

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
    public void testExportImportXml(String filePath) {
        try {
            File f = new File(filePath);
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false);
            String folderName = "xml";
            File fdir = new File(TESTDATADIR + File.separator + folderName + File.separator + f.getName());
            fdir.mkdirs();

            File outFile = new File(fdir + File.separator + Helper.makeFileName("swf.xml"));
            new SwfXmlExporter().exportXml(swf, outFile);
            String xml = Helper.readTextFile(outFile.getPath());

            SWF swf2 = new SWF();
            new SwfXmlImporter().importSwf(swf2, xml);

            if (swf.getTags().size() != swf2.getTags().size()) {
                throw new NotSameException(0);
            }

            for (int i = 0; i < swf.getTags().size(); i++) {
                Tag oldTag = swf.getTags().get(i);
                Tag newTag = swf2.getTags().get(i);
                if (oldTag.getClass() != newTag.getClass()) {
                    throw new NotSameException(0);
                }

                if (oldTag instanceof DefineSpriteTag) {
                    DefineSpriteTag oldSprite = (DefineSpriteTag) oldTag;
                    DefineSpriteTag newSprite = (DefineSpriteTag) newTag;
                    if (oldSprite.getTags().size() != newSprite.getTags().size()) {
                        throw new NotSameException(0);
                    }

                    for (int k = 0; k < oldSprite.getTags().size(); k++) {
                        compareTags(oldSprite.getTags().get(k), newSprite.getTags().get(k));
                    }
                } else if (!(oldTag instanceof FontTag)) {
                    compareTags(oldTag, newTag);
                }
            }
        } catch (Exception ex) {
            fail("Exception during SWF xml export/import: " + filePath + " " + ex.getMessage());
        }
    }

    private void compareTags(Tag tag1, Tag tag2) {
        if (tag1.getClass() != tag2.getClass()) {
            throw new NotSameException(0);
        }

        byte[] data1 = tag1.getData();
        byte[] data2 = tag2.getData();

        int length = Math.min(data1.length, data2.length);
        for (int j = 0; j < length; j++) {
            if (data1[j] != data2[j]) {
                throw new NotSameException(j);
            }
        }

        if (data1.length != data2.length) {
            throw new NotSameException(length);
        }
    }

    @Override
    public String[] getTestDataDirs() {
        return new String[]{TESTDATADIR, FREE_ACTIONSCRIPT_AS2, FREE_ACTIONSCRIPT_AS3};
    }
}
