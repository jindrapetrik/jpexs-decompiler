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
public class ActionScript2ClassesTest extends ActionScript2TestBase {

    private final String BASE_TEST_PACKAGE = "com.jpexs.flash.test.testcases";

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    private void compareSrc(String testClassName, String expectedClassContents) {
        DoInitActionTag dia = getClassSource(BASE_TEST_PACKAGE + "." + testClassName);
        assertNotNull(dia);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            Action.actionsToSource(dia, dia.getActions(), "", writer);
        } catch (InterruptedException ex) {
            fail();
        }
        String actualResult = cleanPCode(writer.toString());
        String expectedResult = cleanPCode("class " + BASE_TEST_PACKAGE + "." + testClassName + "\r\n"
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
    public void testVarsMethods() {
        compareSrc("TestVarsMethods", "var instVar = 1;\r\n"
                + "static var statVar = 2;\r\n"
                + "function TestVarsMethods()\r\n"
                + "{\r\n"
                + "trace(\"constructor\");\r\n"
                + "}\r\n"
                + "function instMethod()\r\n"
                + "{\r\n"
                + "trace(\"instance method\");\r\n"
                + "}\r\n"
                + "static function statMethod()\r\n"
                + "{\r\n"
                + "trace(\"static method\");\r\n"
                + "}\r\n"
        );
    }

    @Test
    public void testMaintainOrder() {
        compareSrc("TestMaintainOrder", "var a = 1;\r\n"
                + "static var b = 2;\r\n"
                + "var c = 3;\r\n"
                + "static var d = 4;\r\n"
                + "static var e = 5;\r\n"
                + "var f = 6;\r\n"
                + "var g = 7;\r\n"
                + "var _x1 = \"after method m\";\r\n"
                + "function TestMaintainOrder()\r\n"
                + "{\r\n"
                + "}\r\n"
                + "function h()\r\n"
                + "{\r\n"
                + "trace(\"8\");\r\n"
                + "}\r\n"
                + "function i()\r\n"
                + "{\r\n"
                + "trace(\"9\");\r\n"
                + "}\r\n"
                + "static function j()\r\n"
                + "{\r\n"
                + "trace(\"10\");\r\n"
                + "}\r\n"
                + "static function k()\r\n"
                + "{\r\n"
                + "trace(\"11\");\r\n"
                + "}\r\n"
                + "function l()\r\n"
                + "{\r\n"
                + "trace(\"12\");\r\n"
                + "}\r\n"
                + "static function m()\r\n"
                + "{\r\n"
                + "trace(\"13\");\r\n"
                + "}\r\n"
                + "function _x2()\r\n"
                + "{\r\n"
                + "trace(\"after _x1\");\r\n"
                + "}\r\n");
    }
}
