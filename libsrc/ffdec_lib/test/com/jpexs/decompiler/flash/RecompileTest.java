/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.NotSameException;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class RecompileTest extends FileTestBase {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
    }

    public static final String TESTDATADIR = "testdata/recompile";

    @Test(dataProvider = "provideFiles")
    public void testRecompile(String fileName) {
        try {
            try (FileInputStream fis = new FileInputStream(TESTDATADIR + File.separator + fileName)) {
                Configuration.debugCopy.set(true);
                SWF swf = new SWF(new BufferedInputStream(fis), false);
                swf.saveTo(new ByteArrayOutputStream());
            }
        } catch (NotSameException ex) {
            fail("File is different after recompiling: " + fileName);
        } catch (IOException | InterruptedException ex) {
            fail("Exception during decompilation: " + fileName + " " + ex.getMessage());
        }
    }

    @Test(dataProvider = "provideFiles")
    public void testTagEditing(String fileName) throws IOException, InterruptedException {
        try {
            Configuration.debugCopy.set(false);
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(TESTDATADIR + File.separator + fileName)), false);
            for (Tag tag : swf.tags) {
                if (!(tag instanceof TagStub)) {
                    Tag tag2 = tag.cloneTag();
                    if (tag2 instanceof TagStub) {
                        fail("Recompile failed. Tag: " + tag.getId() + " " + tag.getName());
                    }
                }
            }
        } catch (Exception ex) {
            fail("Exception during decompilation: " + fileName + " " + ex.getMessage());
        }
    }

    @Override
    public String getTestDataDir() {
        return TESTDATADIR;
    }
}
