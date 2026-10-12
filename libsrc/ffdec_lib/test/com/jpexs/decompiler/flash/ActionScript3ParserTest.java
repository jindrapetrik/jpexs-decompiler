/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceExceptionItem;
import com.jpexs.decompiler.flash.importers.FFDecAs3ScriptReplacer;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ActionScript3ParserTest {

    private void parse(String source) throws Exception {
        ABC abc = new DoABC2Tag(new SWF()).getABC();
        AbcIndexing index = new AbcIndexing();
        index.selectAbc(abc);
        ActionScript3Parser parser = new ActionScript3Parser(index);
        parser.scriptTraitsFromString(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                source, "Test.as", 0, new Reference<>(null), abc,
                new Reference<>(false), new ArrayList<>(), new Reference<>(null));
    }

    @DataProvider(name = "duplicateFields")
    public Object[][] duplicateFields() {
        return new Object[][]{
            {"public var value:int;", "public var value:String;"},
            {"public const value:int = 1;", "public const value:int = 2;"},
            {"public var value:int;", "public const value:int = 2;"},
            {"public const value:int = 1;", "public var value:int;"},
            {"public var value:int;", "private var value:int;"},
            {"var value:int;", "internal var value:int;"},
            {"protected var value:int;", "public var value:int;"},
            {"public static var value:int;", "private static var value:int;"},
            {"public static const value:int = 1;", "public static var value:int;"},
            {"public namespace custom = \"urn:custom\"; custom var value:int;", "custom var value:int;"},
            {"§§namespace(\"urn:custom\") var value:int;", "§§namespace(\"urn:custom\") const value:int = 1;"}
        };
    }

    @Test(dataProvider = "duplicateFields")
    public void testDuplicateFields(String first, String second) throws Exception {
        try {
            parse("package {\npublic class Test {\n" + first + "\n" + second + "\n}\n}");
            fail("Duplicate field must be rejected");
        } catch (AVM2ParseException ex) {
            assertEquals(ex.text, "Duplicate class field \"value\"");
            assertEquals(ex.line, 4L);
        }
    }

    @DataProvider(name = "validFields")
    public Object[][] validFields() {
        return new Object[][]{
            {"public var first:int; public var second:int;"},
            {"public var value:int; public static var value:int;"},
            {"public const value:int = 1; public static const value:int = 2;"},
            {"public namespace first = \"urn:first\"; public namespace second = \"urn:second\";"
                + "first var value:int; second var value:int; public var value:int;"},
            {"§§namespace(\"urn:first\") var value:int; §§namespace(\"urn:second\") var value:int;"},
            {"public var value:int; public function run():void { var value:int; }"},
            {"public function get value():int { return 0; } public function set value(v:int):void {}"}
        };
    }

    @Test(dataProvider = "validFields")
    public void testValidFields(String fields) throws Exception {
        parse("package { public class Test { " + fields + " } }");
    }

    @Test
    public void testFieldsInDifferentClasses() throws Exception {
        parse("package { public class Test { public var value:int; } }"
                + "class Other { public var value:int; }");
    }

    @Test
    public void testReplacementWithDuplicateFieldKeepsOriginalScript() throws Exception {
        SWF swf;
        try (FileInputStream input = new FileInputStream("testdata/as3/as3.swf")) {
            swf = new SWF(input, false);
        }
        ScriptPack pack = swf.getScriptPacksByClassNames(Collections.singletonList("classes.TestClass1")).get(0);
        ABC abc = pack.abc;
        ByteArrayOutputStream original = new ByteArrayOutputStream();
        abc.saveToStream(original);
        try {
            new FFDecAs3ScriptReplacer(false).replaceScript(pack,
                    "package classes {\npublic class TestClass1 {\n"
                    + "public var value:int;\npublic var value:int;\n}\n}", new ArrayList<>());
            fail("Replacement with duplicate field must be rejected");
        } catch (As3ScriptReplaceException ex) {
            assertEquals(ex.getExceptionItems().size(), 1);
            As3ScriptReplaceExceptionItem error = ex.getExceptionItems().get(0);
            assertEquals(error.getMessage(), "Duplicate class field \"value\"");
            assertEquals(error.getLine(), 4);
        }
        ByteArrayOutputStream after = new ByteArrayOutputStream();
        abc.saveToStream(after);
        assertEquals(after.toByteArray(), original.toByteArray());
        assertFalse(abc.script_info.get(pack.scriptIndex).deleted);
        assertFalse(abc.script_info.get(pack.scriptIndex).isModified());
    }
}
