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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.NotSameException;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.TagStub;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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
        Configuration.simplifyExpressions.set(false);
    }

    public static final String TESTDATADIR = "testdata/recompile";

    @Test(dataProvider = "provideFiles")
    public void testAS3InstructionParsing(String filePath) {
        try {
            Configuration._debugCopy.set(false);
            try (FileInputStream fis = new FileInputStream(filePath)) {
                SWF swf = new SWF(new BufferedInputStream(fis), false);
                for (ABCContainerTag abcTag : swf.getAbcList()) {
                    ABC abc = abcTag.getABC();
                    for (MethodBody body : abc.bodies) {
                        AVM2Code code = body.getCode();
                    }

                    ((Tag) abcTag).setModified(true);
                }
            }
        } catch (Throwable ex) {
            fail("Exception during decompilation: " + filePath, ex);
        }
    }

    @Test(dataProvider = "provideFiles")
    public void testRecompile(String filePath) {
        try {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                Configuration._debugCopy.set(true);
                SWF swf = new SWF(new BufferedInputStream(fis), false);
                swf.saveTo(new ByteArrayOutputStream());
            }
        } catch (NotSameException ex) {
            fail("File is different after recompiling: " + filePath);
        } catch (IOException | InterruptedException ex) {
            fail("Exception during decompilation: " + filePath, ex);
        }
    }

    @Test(dataProvider = "provideFiles")
    public void testTagEditing(String filePath) throws IOException, InterruptedException {
        try {
            Configuration._debugCopy.set(false);
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(filePath)), false, false);
            for (Tag tag : swf.getTags()) {
                if (!(tag instanceof TagStub)) {
                    Tag tag2 = tag.cloneTag();
                    if (tag2 instanceof TagStub) {
                        fail("Recompile failed. Tag: " + tag.getId() + " " + tag.getName());
                    }
                }
            }
        } catch (Exception ex) {
            fail("Exception during decompilation: " + filePath, ex);
        }
    }

    @Override
    public String[] getTestDataDirs() {
        return new String[]{TESTDATADIR, FREE_ACTIONSCRIPT_AS2, FREE_ACTIONSCRIPT_AS3};
    }
}
