/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2UninitializedFieldsTest extends ActionScript2TestBase {

    private final String BASE_TEST_PACKAGE = "com.jpexs";

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2_initfields/as2_initfields.swf")), false);
    }

    private void compareSrc(String testClassName, String expectedClassContents) {
        DoInitActionTag dia = getClassSource(BASE_TEST_PACKAGE + "." + testClassName);
        assertNotNull(dia);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            Action.actionsToSource(swf.getUninitializedAs2ClassTraits(), dia, dia.getActions(), "", writer, Utf8Helper.charsetName);
        } catch (InterruptedException ex) {
            fail();
        }
        writer.finishHilights();
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
    public void testUninitializedFields() {
        compareSrc("MyClass", "   var _v3;\n"
                + "   var c;\n"
                + "   var d;\n"
                + "   var f;\n"
                + "   var v;\n"
                + "   var v2;\n"
                + "   static var _sv3;\n"
                + "   static var sv;\n"
                + "   static var sv2;\n"
                + "   var init_v = 2;\n"
                + "   static var sinit_v = 3;\n"
                + "   function MyClass()\n"
                + "   {\n"
                + "   }\n"
                + "   function testVar()\n"
                + "   {\n"
                + "      this.v = 1;\n"
                + "   }\n"
                + "   function getV2()\n"
                + "   {\n"
                + "      return this.v2;\n"
                + "   }\n"
                + "   static function getSV2()\n"
                + "   {\n"
                + "      return com.jpexs.MyClass.sv2;\n"
                + "   }\n"
                + "   function callF()\n"
                + "   {\n"
                + "      this.f();\n"
                + "   }\n"
                + "   function constructC()\n"
                + "   {\n"
                + "      new this.c();\n"
                + "   }\n"
                + "   function deleteD()\n"
                + "   {\n"
                + "      delete this.d;\n"
                + "   }\n"
                + "   function set v3(val)\n"
                + "   {\n"
                + "      this._v3 = val;\n"
                + "   }\n"
                + "   function get v3()\n"
                + "   {\n"
                + "      return this._v3;\n"
                + "   }\n"
                + "   static function set sv3(val)\n"
                + "   {\n"
                + "      com.jpexs.MyClass._sv3 = val;\n"
                + "   }\n"
                + "   static function get sv3()\n"
                + "   {\n"
                + "      return com.jpexs.MyClass._sv3;\n"
                + "   }\n"
                + "   function setV3()\n"
                + "   {\n"
                + "      this.v = this.v3;\n"
                + "      com.jpexs.MyClass.sv = com.jpexs.MyClass.sv3;\n"
                + "   }");
    }
}
