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
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 *
 * @author JPEXS
 */
public class ActionDefineFunctionPushRegisterCleanerTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2_definefunc_regs/as2_definefunc_regs.swf")), false);
    }

    private void compareSrc(String testClassName, String expectedClassContents) {
        DoInitActionTag dia = getClassSource(testClassName);
        assertNotNull(dia);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            Action.actionsToSource(dia, dia.getActions(), "", writer);
        } catch (InterruptedException ex) {
            fail();
        }
        String actualResult = cleanPCode(writer.toString());
        String expectedResult = cleanPCode("class " + testClassName + "\r\n"
                + "{\r\n"
                + expectedClassContents + "\r\n"
                + "}");
        assertEquals(actualResult, expectedResult);
    }

    private DoInitActionTag getClassSource(String classFullName) {
        for (Tag t : swf.getTags()) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag dia = (DoInitActionTag) t;
                String exportName = swf.getExportName(dia.spriteId);
                if (exportName != null && exportName.startsWith(SWF.AS2_PKG_PREFIX)) {
                    String exportClassName = exportName.substring(SWF.AS2_PKG_PREFIX.length());
                    if (exportClassName.equals(classFullName)) {
                        return dia;
                    }
                }
            }
        }
        return null;
    }

    @Test
    public void testMyClass() {
        compareSrc("MyClass", "function MyClass()\n"
                + "{\r\n"
                + "}\r\n"
                + "function testNoReturn()\r\n"
                + "{\r\n"
                + "var _loc2_ = 5;\r\n"
                + "var _loc1_ = _loc2_ + 27;\r\n"
                + "trace(\"hi:\" + _loc1_);\r\n"
                + "}\r\n"
                + "function testSimpleReturn()\r\n"
                + "{\r\n"
                + "var a = 5;\r\n"
                + "var _loc1_ = 30;\r\n"
                + "return \"bagr\" + _loc1_;\r\n"
                + "}\r\n"
                + "function testReturns()\r\n"
                + "{\r\n"
                + "var _loc2_ = 10;\r\n"
                + "if(_loc2_ > 2)\r\n"
                + "{\r\n"
                + "_loc2_ = _loc2_ + 1;\r\n"
                + "var _loc1_ = 0;\r\n"
                + "while(_loc1_ < 100)\r\n"
                + "{\r\n"
                + "if(_loc2_ + _loc1_ == 27)\r\n"
                + "{\r\n"
                + "return _loc2_ + 7;\r\n"
                + "}\r\n"
                + "_loc1_ = _loc1_ + 27;\r\n"
                + "_loc1_ = _loc1_ + 1;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else if(_loc2_ == 4)\r\n"
                + "{\r\n"
                + "return 4;\r\n"
                + "}\r\n"
                + "return 3;\r\n"
                + "}\r\n"
                + "function testSomeReturns()\r\n"
                + "{\r\n"
                + "var _loc1_ = 5;\r\n"
                + "if(_loc1_ < 10)\r\n"
                + "{\r\n"
                + "return _loc1_;\r\n"
                + "}\r\n"
                + "}\r\r"
        );
    }
}
