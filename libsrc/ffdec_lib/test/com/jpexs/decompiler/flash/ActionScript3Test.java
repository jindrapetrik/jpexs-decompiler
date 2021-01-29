/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3Test extends ActionScriptTestBase {

    private Map<String, SWF> swfMap = new HashMap<>();

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        swfMap.put("standard", new SWF(new BufferedInputStream(new FileInputStream("testdata/flashdevelop/bin/flashdevelop.swf")), false));
        swfMap.put("assembled", new SWF(new BufferedInputStream(new FileInputStream("testdata/custom/bin/custom.swf")), false));
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);

        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        Configuration.showMethodBodyId.set(false);
    }

    private void decompileMethod(String swfIdentifier, String methodName, String expectedResult, boolean isStatic) {
        String className = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

        int clsIndex = -1;
        int scriptIndex = -1;

        ABC abc = null;
        SWF swf = swfMap.get(swfIdentifier);
        List<ABC> abcs = new ArrayList<>();
        for (ABCContainerTag abcTag : swf.getAbcList()) {
            abcs.add(abcTag.getABC());
        }
        ScriptPack scriptPack = null;
        for (ABC a : abcs) {
            scriptPack = a.findScriptPackByPath("tests." + className, abcs);
            if (scriptPack != null) {
                break;
            }
        }
        assertNotNull(scriptPack);
        abc = scriptPack.abc;
        scriptIndex = scriptPack.scriptIndex;

        clsIndex = abc.findClassByName(new DottedChain(new String[]{"tests", className}, ""));

        assertTrue(clsIndex > -1);
        assertTrue(scriptIndex > -1);

        int bodyIndex = abc.findMethodBodyByName(clsIndex, "run");

        assertTrue(bodyIndex > -1);
        HighlightedTextWriter writer;
        try {
            List<Traits> ts = new ArrayList<>();
            ts.add(abc.instance_info.get(clsIndex).instance_traits);
            abc.bodies.get(bodyIndex).convert(new ConvertData(), "run", ScriptExportMode.AS, isStatic, abc.bodies.get(bodyIndex).method_info, scriptIndex, clsIndex, abc, null, new ScopeStack(scriptIndex), 0, new NulWriter(), new ArrayList<>(), ts, true);
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            abc.bodies.get(bodyIndex).toString("run", ScriptExportMode.AS, abc, null, writer, new ArrayList<>());
        } catch (InterruptedException ex) {
            fail();
            return;
        }
        String actualResult = cleanPCode(writer.toString());
        expectedResult = cleanPCode(expectedResult);
        assertEquals(actualResult, expectedResult);
    }

    private void decompileScriptPack(String path, String expectedResult) {

        DoABC2Tag tag = null;
        ABC abc = null;
        ScriptPack scriptPack = null;
        for (Tag t : swfMap.get("standard").getTags()) {
            if (t instanceof DoABC2Tag) {
                tag = (DoABC2Tag) t;
                abc = tag.getABC();
                scriptPack = abc.findScriptPackByPath(path, Arrays.asList(abc));
                if (scriptPack != null) {
                    break;
                }
            }
        }
        assertNotNull(abc);
        assertNotNull(scriptPack);
        HighlightedTextWriter writer = null;
        try {
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            scriptPack.toSource(writer, abc.script_info.get(scriptPack.scriptIndex).traits.traits, new ConvertData(), ScriptExportMode.AS, false);
        } catch (InterruptedException ex) {
            fail();
        }
        String actualResult = cleanPCode(writer.toString());
        expectedResult = cleanPCode(expectedResult);
        assertEquals(actualResult, expectedResult);
    }

    @Test
    public void testStandardArguments() {
        decompileMethod("standard", "testArguments", "return arguments[0];\r\n",
                false);
    }

    @Test
    public void testStandardCatchFinally() {
        decompileMethod("standard", "testCatchFinally", "var a:* = 5;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "a = 9;\r\n"
                + "trace(\"intry\");\r\n"
                + "}\r\n"
                + "catch(e:*)\r\n"
                + "{\r\n"
                + "trace(\"incatch\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"infinally\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardChain2() {
        decompileMethod("standard", "testChain2", "var g:Array = null;\r\n"
                + "var h:Boolean = false;\r\n"
                + "var extraLine:Boolean = false;\r\n"
                + "var r:int = 7;\r\n"
                + "var t:int = 0;\r\n"
                + "t = this.getInt();\r\n"
                + "if(t + 1 < g.length)\r\n"
                + "{\r\n"
                + "t++;\r\n"
                + "h = true;\r\n"
                + "}\r\n"
                + "if(t >= 0)\r\n"
                + "{\r\n"
                + "trace(\"ch\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardChainedAssignments() {
        decompileMethod("standard", "testChainedAssignments", "var a:int = 0;\r\n"
                + "var b:int = 0;\r\n"
                + "var c:int = 0;\r\n"
                + "var d:int = 0;\r\n"
                + "d = c = b = a = 5;\r\n"
                + "var e:TestClass2 = TestClass2.createMe(\"test\");\r\n"
                + "e.attrib1 = e.attrib2 = e.attrib3 = this.getCounter();\r\n"
                + "this.traceIt(e.toString());\r\n",
                false);
    }

    @Test
    public void testStandardComplexExpressions() {
        decompileMethod("standard", "testComplexExpressions", "var i:int = 0;\r\n"
                + "var j:int = 0;\r\n"
                + "j = i = i + (i = i + i++);\r\n",
                false);
    }

    @Test
    public void testStandardContinueLevels() {
        decompileMethod("standard", "testContinueLevels", "var b:* = undefined;\r\n"
                + "var c:* = undefined;\r\n"
                + "var d:* = undefined;\r\n"
                + "var e:* = undefined;\r\n"
                + "var a:* = 5;\r\n"
                + "loop3:\r\n"
                + "switch(a)\r\n"
                + "{\r\n"
                + "case 57 * a:\r\n"
                + "trace(\"fiftyseven multiply a\");\r\n"
                + "b = 0;\r\n"
                + "while(b < 50)\r\n"
                + "{\r\n"
                + "if(b == 10)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "if(b == 15)\r\n"
                + "{\r\n"
                + "break loop3;\r\n"
                + "}\r\n"
                + "b = b + 1;\r\n"
                + "}\r\n"
                + "break;\r\n"
                + "case 13:\r\n"
                + "trace(\"thirteen\");\r\n"
                + "case 14:\r\n"
                + "trace(\"fourteen\");\r\n"
                + "break;\r\n"
                + "case 89:\r\n"
                + "trace(\"eightynine\");\r\n"
                + "break;\r\n"
                + "default:\r\n"
                + "trace(\"default clause\");\r\n"
                + "}\r\n"
                + "loop1:\r\n"
                + "for(c = 0; c < 8; c = c + 1)\r\n"
                + "{\r\n"
                + "for(d = 0; d < 25; d++)\r\n"
                + "{\r\n"
                + "e = 0;\r\n"
                + "if(e < 50)\r\n"
                + "{\r\n"
                + "if(e == 9)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "if(e == 20)\r\n"
                + "{\r\n"
                + "continue loop1;\r\n"
                + "}\r\n"
                + "if(e == 8)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "break loop1;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"hello\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardDecl2() {
        decompileMethod("standard", "testDecl2", "var k:int = 0;\r\n"
                + "var i:int = 5;\r\n"
                + "i = i + 7;\r\n"
                + "if(i == 5)\r\n"
                + "{\r\n"
                + "if(i < 8)\r\n"
                + "{\r\n"
                + "k = 6;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "k = 7;\r\n",
                false);
    }

    @Test
    public void testStandardDeclarations() {
        decompileMethod("standard", "testDeclarations", "var vall:* = undefined;\r\n"
                + "var vstr:String = null;\r\n"
                + "var vint:int = 0;\r\n"
                + "var vuint:uint = 0;\r\n"
                + "var vclass:TestClass1 = null;\r\n"
                + "var vnumber:Number = NaN;\r\n"
                + "var vobject:Object = null;\r\n"
                + "vall = 6;\r\n"
                + "vstr = \"hello\";\r\n"
                + "vuint = 7;\r\n"
                + "vint = -4;\r\n"
                + "vclass = new TestClass1();\r\n"
                + "vnumber = 0.5;\r\n"
                + "vnumber = 6;\r\n"
                + "vobject = vclass;\r\n",
                false);
    }

    @Test
    public void testStandardDefaultNotLastGrouped() {
        decompileMethod("standard", "testDefaultNotLastGrouped", "var k:* = 10;\r\n"
                + "switch(k)\r\n"
                + "{\r\n"
                + "case \"six\":\r\n"
                + "default:\r\n"
                + "trace(\"def and 6\");\r\n"
                + "case \"five\":\r\n"
                + "trace(\"def and 6 and 5\");\r\n"
                + "break;\r\n"
                + "case \"four\":\r\n"
                + "trace(\"4\");\r\n"
                + "}\r\n"
                + "trace(\"after switch\");\r\n",
                false);
    }

    @Test
    public void testStandardDoWhile() {
        decompileMethod("standard", "testDoWhile", "var a:* = 8;\r\n"
                + "do\r\n"
                + "{\r\n"
                + "trace(\"a=\" + a);\r\n"
                + "a++;\r\n"
                + "}\r\n"
                + "while(a < 20);\r\n",
                false);
    }

    @Test
    public void testStandardDoWhile2() {
        decompileMethod("standard", "testDoWhile2", "var k:int = 5;\r\n"
                + "do\r\n"
                + "{\r\n"
                + "k++;\r\n"
                + "if(k == 7)\r\n"
                + "{\r\n"
                + "k = 5 * k;\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "k = 5 - k;\r\n"
                + "}\r\n"
                + "k--;\r\n"
                + "}\r\n"
                + "while(k < 9);\r\n"
                + "return 2;\r\n",
                false);
    }

    @Test
    public void testStandardExpressions() {
        decompileMethod("standard", "testExpressions", "var arr:Array = null;\r\n"
                + "var i:int = 5;\r\n"
                + "var j:int = 5;\r\n"
                + "if((i = i = i / 2) == 1 || i == 2)\r\n"
                + "{\r\n"
                + "arguments.concat(i);\r\n"
                + "}\r\n"
                + "else if(i == 0)\r\n"
                + "{\r\n"
                + "i = j++;\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "arr[0]();\r\n"
                + "}\r\n"
                + "return i == 0;\r\n",
                false);
    }

    @Test
    public void testStandardFinallyZeroJump() {
        decompileMethod("standard", "testFinallyZeroJump", "var str:String = param1;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"error is :\" + e.message);\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"hi \");\r\n"
                + "if(5 == 4)\r\n"
                + "{\r\n"
                + "return str;\r\n"
                + "}\r\n"
                + "return \"hu\" + str;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardFor() {
        decompileMethod("standard", "testFor", "for(var a:* = 0; a < 10; a++)\r\n"
                + "{\r\n"
                + "trace(\"a=\" + a);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForAnd() {
        decompileMethod("standard", "testForAnd", "var x:Boolean = false;\r\n"
                + "var len:int = 5;\r\n"
                + "var a:int = 4;\r\n"
                + "var b:int = 7;\r\n"
                + "var c:int = 9;\r\n"
                + "for(var i:uint = 0; i < len; x = a > 4 && b < 2 || c > 10)\r\n"
                + "{\r\n"
                + "c = 1;\r\n"
                + "if(c == 2)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "if(c == 7)\r\n"
                + "{\r\n"
                + "trace(\"B\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"C\");\r\n"
                + "}\r\n"
                + "trace(\"D\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForBreak() {
        decompileMethod("standard", "testForBreak", "for(var a:* = 0; a < 10; a++)\r\n"
                + "{\r\n"
                + "if(a == 5)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"hello:\" + a);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForContinue() {
        decompileMethod("standard", "testForContinue", "for(var a:* = 0; a < 10; a = a + 1)\r\n"
                + "{\r\n"
                + "if(a == 9)\r\n"
                + "{\r\n"
                + "if(a == 5)\r\n"
                + "{\r\n"
                + "trace(\"part1\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"a=\" + a);\r\n"
                + "if(a == 7)\r\n"
                + "{\r\n"
                + "trace(\"part2\");\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"part3\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"part4\");\r\n"
                + "}\r\n"
                + "trace(\"part5\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForEach() {
        decompileMethod("standard", "testForEach", "var list:Array = null;\r\n"
                + "var item:* = undefined;\r\n"
                + "list = new Array();\r\n"
                + "list[0] = \"first\";\r\n"
                + "list[1] = \"second\";\r\n"
                + "list[2] = \"third\";\r\n"
                + "for each(item in list)\r\n"
                + "{\r\n"
                + "trace(\"item #\" + item);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForEachObjectArray() {
        decompileMethod("standard", "testForEachObjectArray", "var list:Array = null;\r\n"
                + "var test:Array = null;\r\n"
                + "list = new Array();\r\n"
                + "list[0] = \"first\";\r\n"
                + "list[1] = \"second\";\r\n"
                + "list[2] = \"third\";\r\n"
                + "test = new Array();\r\n"
                + "test[0] = 0;\r\n"
                + "for each(test[0] in list)\r\n"
                + "{\r\n"
                + "trace(\"item #\" + test[0]);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForEachObjectAttribute() {
        decompileMethod("standard", "testForEachObjectAttribute", "var list:Array = null;\r\n"
                + "list = new Array();\r\n"
                + "list[0] = \"first\";\r\n"
                + "list[1] = \"second\";\r\n"
                + "list[2] = \"third\";\r\n"
                + "for each(this.testPriv in list)\r\n"
                + "{\r\n"
                + "trace(\"item #\" + this.testPriv);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForGoto() {
        decompileMethod("standard", "testForGoto", "var c:int = 0;\r\n"
                + "var len:int = 5;\r\n"
                + "for(var i:uint = 0; i < len; i++)\r\n"
                + "{\r\n"
                + "c = 1;\r\n"
                + "if(c == 2)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "}\r\n"
                + "else if(c == 3)\r\n"
                + "{\r\n"
                + "trace(\"B\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"C\");\r\n"
                + "}\r\n"
                + "trace(\"exit\");\r\n",
                false);
    }

    @Test
    public void testStandardForIn() {
        decompileMethod("standard", "testForIn", "var dic:Dictionary = null;\r\n"
                + "var item:* = null;\r\n"
                + "for(item in dic)\r\n"
                + "{\r\n"
                + "trace(item);\r\n"
                + "}\r\n"
                + "for each(item in dic)\r\n"
                + "{\r\n"
                + "trace(item);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardForXml() {
        decompileMethod("standard", "testForXml", "var c:int = 0;\r\n"
                + "var name:String = \"ahoj\";\r\n"
                + "var myXML:XML = <order id=\"604\">\r\n"
                + "<book isbn=\"12345\">\r\n"
                + "<title>{name}</title>\r\n"
                + "</book>\r\n"
                + "</order>;\r\n"
                + "var k:* = null;\r\n"
                + "var len:int = 5;\r\n"
                + "var a:int = 5;\r\n"
                + "var b:int = 6;\r\n"
                + "for(var i:int = 0; i < len; k = myXML.book.(@isbn == \"12345\"))\r\n"
                + "{\r\n"
                + "c = 1;\r\n"
                + "if(c == 2)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "}\r\n"
                + "else if(c == 3)\r\n"
                + "{\r\n"
                + "trace(\"B\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"C\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardGotos() {
        decompileMethod("standard", "testGotos", "var a:Boolean = true;\r\n"
                + "var b:Boolean = false;\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "}\r\n"
                + "else if(b)\r\n"
                + "{\r\n"
                + "trace(\"B\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "return 7;\r\n"
                + "}\r\n"
                + "trace(\"x\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"z\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "}\r\n"
                + "return 89;\r\n",
                false);
    }

    @Test
    public void testStandardGotos2() {
        decompileMethod("standard", "testGotos2", "var a:Boolean = true;\r\n"
                + "var b:Boolean = false;\r\n"
                + "var c:Boolean = true;\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "if(b)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "if(c)\r\n"
                + "{\r\n"
                + "trace(\"B\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"E\");\r\n"
                + "}\r\n"
                + "return 5;\r\n",
                false);
    }

    @Test
    public void testStandardGotos3() {
        decompileMethod("standard", "testGotos3", "var i:int = 0;\r\n"
                + "var a:int = 5;\r\n"
                + "if(a > 5)\r\n"
                + "{\r\n"
                + "for(i = 0; i < 5; i++)\r\n"
                + "{\r\n"
                + "if(i > 3)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "if(i == 4)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"B\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"C\");\r\n"
                + "}\r\n"
                + "trace(\"return\");\r\n",
                false);
    }

    @Test
    public void testStandardGotos4() {
        decompileMethod("standard", "testGotos4", "var a:int = 5;\r\n"
                + "if(a > 3)\r\n"
                + "{\r\n"
                + "if(a < 7)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "}\r\n"
                + "catch(error:Error)\r\n"
                + "{\r\n"
                + "}\r\n"
                + "trace(\"B\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"return\");\r\n",
                false);
    }

    @Test
    public void testStandardGotos5() {
        decompileMethod("standard", "testGotos5", "var j:int = 0;\r\n"
                + "var s:String = \"A\";\r\n"
                + "for(var i:int = 0; i < 10; i++)\r\n"
                + "{\r\n"
                + "if(s == \"B\")\r\n"
                + "{\r\n"
                + "if(s == \"C\")\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"D\");\r\n"
                + "j = 0;\r\n"
                + "while(j < 29)\r\n"
                + "{\r\n"
                + "trace(\"E\");\r\n"
                + "j++;\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardGotos6() {
        decompileMethod("standard", "testGotos6", "var a:Boolean = true;\r\n"
                + "var s:String = \"a\";\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "switch(s)\r\n"
                + "{\r\n"
                + "case \"a\":\r\n"
                + "trace(\"is A\");\r\n"
                + "break;\r\n"
                + "case \"b\":\r\n"
                + "trace(\"is B\");\r\n"
                + "case \"c\":\r\n"
                + "trace(\"is BC\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"D\");\r\n"
                + "}\r\n"
                + "trace(\"finish\");\r\n",
                false);
    }

    @Test
    public void testStandardGotos7() {
        decompileMethod("standard", "testGotos7", "for(var i:int = 0; i < 10; i++)\r\n"
                + "{\r\n"
                + "switch(i)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "trace(\"zero\");\r\n"
                + "continue;\r\n"
                + "case 5:\r\n"
                + "trace(\"five\");\r\n"
                + "break;\r\n"
                + "case 10:\r\n"
                + "trace(\"ten\");\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "if(i == 7)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"one\");\r\n"
                + "default:\r\n"
                + "trace(\"def\");\r\n"
                + "}\r\n"
                + "trace(\"before loop end\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardHello() {
        decompileMethod("standard", "testHello", "trace(\"hello\");\r\n",
                false);
    }

    @Test
    public void testStandardIf() {
        decompileMethod("standard", "testIf", "var a:* = 5;\r\n"
                + "if(a == 7)\r\n"
                + "{\r\n"
                + "trace(\"onTrue\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardIfElse() {
        decompileMethod("standard", "testIfElse", "var a:* = 5;\r\n"
                + "if(a == 7)\r\n"
                + "{\r\n"
                + "trace(\"onTrue\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"onFalse\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardIfInIf() {
        decompileMethod("standard", "testIfInIf", "var k:int = 5;\r\n"
                + "if(k > 5 && k < 20)\r\n"
                + "{\r\n"
                + "trace(\"A\");\r\n"
                + "if(k < 4)\r\n"
                + "{\r\n"
                + "return 1;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else if(k > 4 && k < 10)\r\n"
                + "{\r\n"
                + "trace(\"B\");\r\n"
                + "if(k < 7)\r\n"
                + "{\r\n"
                + "return 2;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"C\");\r\n"
                + "return 7;\r\n",
                false);
    }

    @Test
    public void testStandardInc2() {
        decompileMethod("standard", "testInc2", "var a:* = [1];\r\n"
                + "a[this.getInt()]++;\r\n"
                + "var d:* = a[this.getInt()]++;\r\n"
                + "var e:* = ++a[this.getInt()];\r\n"
                + "var b:* = 1;\r\n"
                + "b++;\r\n"
                + "var c:* = 1;\r\n"
                + "b = c++;\r\n",
                false);
    }

    @Test
    public void testStandardIncDec() {
        decompileMethod("standard", "testIncDec", "var a:* = 5;\r\n"
                + "var b:* = 0;\r\n"
                + "trace(\"++var\");\r\n"
                + "b = ++a;\r\n"
                + "trace(\"var++\");\r\n"
                + "b = a++;\r\n"
                + "trace(\"--var\");\r\n"
                + "b = --a;\r\n"
                + "trace(\"var--\");\r\n"
                + "b = a--;\r\n"
                + "var c:* = [1,2,3,4,5];\r\n"
                + "trace(\"++arr\");\r\n"
                + "b = ++c[2];\r\n"
                + "trace(\"arr++\");\r\n"
                + "b = c[2]++;\r\n"
                + "trace(\"--arr\");\r\n"
                + "b = --c[2];\r\n"
                + "trace(\"arr--\");\r\n"
                + "b = c[2]--;\r\n"
                + "var d:* = new TestClass1();\r\n"
                + "trace(\"++property\");\r\n"
                + "trace(++d.attrib);\r\n"
                + "trace(\"property++\");\r\n"
                + "trace(d.attrib++);\r\n"
                + "trace(\"--property\");\r\n"
                + "trace(--d.attrib);\r\n"
                + "trace(\"property--\");\r\n"
                + "trace(d.attrib--);\r\n"
                + "trace(\"arr[e++]\");\r\n"
                + "var chars:Array = new Array(36);\r\n"
                + "var index:uint = 0;\r\n"
                + "chars[index++] = 5;\r\n"
                + "trace(\"arr[++e]\");\r\n"
                + "chars[++index] = 5;\r\n",
                false);
    }

    @Test
    public void testStandardInlineFunctions() {
        decompileMethod("standard", "testInlineFunctions", "var first:String = null;\r\n"
                + "first = \"value1\";\r\n"
                + "var traceParameter:Function = function(aParam:String):String\r\n"
                + "{\r\n"
                + "var second:String = null;\r\n"
                + "second = \"value2\";\r\n"
                + "second = second + \"cc\";\r\n"
                + "var traceParam2:Function = function(bParam:String):String\r\n"
                + "{\r\n"
                + "trace(bParam + \",\" + aParam);\r\n"
                + "return first + second + aParam + bParam;\r\n"
                + "};\r\n"
                + "trace(second);\r\n"
                + "traceParam2(aParam);\r\n"
                + "return first;\r\n"
                + "};\r\n"
                + "traceParameter(\"hello\");\r\n",
                false);
    }

    @Test
    public void testStandardInnerFunctions() {
        decompileMethod("standard", "testInnerFunctions", "var s:int = 0;\r\n"
                + "var innerFunc:Function = function(b:String):*\r\n"
                + "{\r\n"
                + "trace(b);\r\n"
                + "};\r\n"
                + "var k:int = 5;\r\n"
                + "if(k == 6)\r\n"
                + "{\r\n"
                + "s = 8;\r\n"
                + "}\r\n"
                + "innerFunc(a);\r\n",
                false);
    }

    @Test
    public void testStandardInnerIf() {
        decompileMethod("standard", "testInnerIf", "var a:* = 5;\r\n"
                + "var b:* = 4;\r\n"
                + "if(a == 5)\r\n"
                + "{\r\n"
                + "if(b == 6)\r\n"
                + "{\r\n"
                + "trace(\"b==6\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"b!=6\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "else if(b == 7)\r\n"
                + "{\r\n"
                + "trace(\"b==7\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"b!=7\");\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n",
                false);
    }

    @Test
    public void testStandardInnerTry() {
        decompileMethod("standard", "testInnerTry", "try\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"try body 1\");\r\n"
                + "}\r\n"
                + "catch(e:DefinitionError)\r\n"
                + "{\r\n"
                + "trace(\"catched DefinitionError\");\r\n"
                + "}\r\n"
                + "trace(\"after try 1\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"catched Error\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"finally block\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardLogicalComputing() {
        decompileMethod("standard", "testLogicalComputing", "var b:Boolean = false;\r\n"
                + "var i:* = 5;\r\n"
                + "var j:* = 7;\r\n"
                + "if(i > j)\r\n"
                + "{\r\n"
                + "j = 9;\r\n"
                + "b = true;\r\n"
                + "}\r\n"
                + "b = (i == 0 || i == 1) && j == 0;\r\n",
                false);
    }

    @Test
    public void testStandardManualConvert() {
        decompileMethod("standard", "testManualConvert", "trace(\"String(this).length\");\r\n"
                + "trace(String(this).length);\r\n",
                false);
    }

    @Test
    public void testStandardMissingDefault() {
        decompileMethod("standard", "testMissingDefault", "var jj:int = 1;\r\n"
                + "switch(jj)\r\n"
                + "{\r\n"
                + "case 1:\r\n"
                + "jj = 1;\r\n"
                + "break;\r\n"
                + "case 2:\r\n"
                + "jj = 2;\r\n"
                + "break;\r\n"
                + "default:\r\n"
                + "jj = 3;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardMultipleCondition() {
        decompileMethod("standard", "testMultipleCondition", "var a:* = 5;\r\n"
                + "var b:* = 8;\r\n"
                + "var c:* = 9;\r\n"
                + "if((a <= 4 || b <= 8) && c == 7)\r\n"
                + "{\r\n"
                + "trace(\"onTrue\");\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "trace(\"onFalse\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardNamedAnonFunctions() {
        decompileMethod("standard", "testNamedAnonFunctions", "var test:* = new function testFunc(param1:*, param2:int, param3:Array):Boolean\r\n"
                + "{\r\n"
                + "return (param1 as TestClass2).attrib1 == 5;\r\n"
                + "};\r\n",
                false);
    }

    @Test
    public void testStandardNames() {
        decompileMethod("standard", "testNames", "var ns:* = this.getNamespace();\r\n"
                + "var name:* = this.getName();\r\n"
                + "var a:* = ns::unnamespacedFunc();\r\n"
                + "var b:* = ns::[name];\r\n"
                + "trace(b.c);\r\n"
                + "var c:* = myInternal::neco;\r\n",
                false);
    }

    @Test
    public void testStandardParamNames() {
        decompileMethod("standard", "testParamNames", "return firstp + secondp + thirdp;\r\n",
                false);
    }

    @Test
    public void testStandardParamsCount() {
        decompileMethod("standard", "testParamsCount", "return firstp;\r\n",
                false);
    }

    @Test
    public void testStandardPrecedence() {
        decompileMethod("standard", "testPrecedence", "var a:* = 0;\r\n"
                + "a = (5 + 6) * 7;\r\n"
                + "a = 5 * (2 + 3);\r\n"
                + "a = 5 + 6 * 7;\r\n"
                + "a = 5 * 2 + 2;\r\n"
                + "a = 5 * (25 % 3);\r\n"
                + "a = 5 % (24 * 307);\r\n"
                + "a = 1 / (2 / 3);\r\n"
                + "a = 1 / (2 * 3);\r\n"
                + "a = 1 * 2 * 3;\r\n"
                + "a = 1 * 2 / 3;\r\n"
                + "trace(\"a=\" + a);\r\n",
                false);
    }

    @Test
    public void testStandardPrecedenceX() {
        decompileMethod("standard", "testPrecedenceX", "var a:* = 5;\r\n"
                + "var b:* = 2;\r\n"
                + "var c:* = 3;\r\n"
                + "var d:* = a << (b >>> c);\r\n"
                + "var e:* = a << b >>> c;\r\n",
                false);
    }

    @Test
    public void testStandardProperty() {
        decompileMethod("standard", "testProperty", "var d:* = new TestClass1();\r\n"
                + "var k:* = 7 + 8;\r\n"
                + "if(k == 15)\r\n"
                + "{\r\n"
                + "d.method(d.attrib * 5);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardRegExp() {
        decompileMethod("standard", "testRegExp", "var a1:* = /[a-z\\r\\n0-9\\\\]+/i;\r\n"
                + "var a2:* = /[a-z\\r\\n0-9\\\\]+/i;\r\n"
                + "var b1:* = /[0-9AB]+/;\r\n"
                + "var b2:* = /[0-9AB]+/;\r\n",
                false);
    }

    @Test
    public void testStandardRest() {
        decompileMethod("standard", "testRest", "trace(\"firstRest:\" + restval[0]);\r\n"
                + "return firstp;\r\n",
                false);
    }

    @Test
    public void testStandardStrictEquals() {
        decompileMethod("standard", "testStrictEquals", "var k:int = 6;\r\n"
                + "if(this.f() !== this.f())\r\n"
                + "{\r\n"
                + "trace(\"is eight\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardStringConcat() {
        decompileMethod("standard", "testStringConcat", "var k:int = 8;\r\n"
                + "this.traceIt(\"hello\" + 5 * 6);\r\n"
                + "this.traceIt(\"hello\" + (k - 1));\r\n"
                + "this.traceIt(\"hello\" + 5 + 6);\r\n",
                false);
    }

    @Test
    public void testStandardStrings() {
        decompileMethod("standard", "testStrings", "trace(\"hello\");\r\n"
                + "trace(\"quotes:\\\"hello!\\\"\");\r\n"
                + "trace(\"backslash: \\\\ \");\r\n"
                + "trace(\"single quotes: \\'hello!\\'\");\r\n"
                + "trace(\"new line \\r\\n hello!\");\r\n",
                false);
    }

    @Test
    public void testStandardSwitch() {
        decompileMethod("standard", "testSwitch", "var a:* = 5;\r\n"
                + "switch(a)\r\n"
                + "{\r\n"
                + "case 57 * a:\r\n"
                + "trace(\"fiftyseven multiply a\");\r\n"
                + "break;\r\n"
                + "case 13:\r\n"
                + "trace(\"thirteen\");\r\n"
                + "case 14:\r\n"
                + "trace(\"fourteen\");\r\n"
                + "break;\r\n"
                + "case 89:\r\n"
                + "trace(\"eightynine\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardSwitchComma() {
        decompileMethod("standard", "testSwitchComma", "var b:int = 5;\r\n"
                + "var a:String = \"A\";\r\n"
                + "switch(a)\r\n"
                + "{\r\n"
                + "case \"A\":\r\n"
                + "trace(\"is A\");\r\n"
                + "break;\r\n"
                + "case \"B\":\r\n"
                + "trace(\"is B\");\r\n"
                + "case TestSwitchComma.X, \"C\":\r\n"
                + "trace(\"is C\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardSwitchDefault() {
        decompileMethod("standard", "testSwitchDefault", "var a:* = 5;\r\n"
                + "switch(a)\r\n"
                + "{\r\n"
                + "case 57 * a:\r\n"
                + "trace(\"fiftyseven multiply a\");\r\n"
                + "break;\r\n"
                + "case 13:\r\n"
                + "trace(\"thirteen\");\r\n"
                + "case 14:\r\n"
                + "trace(\"fourteen\");\r\n"
                + "break;\r\n"
                + "case 89:\r\n"
                + "trace(\"eightynine\");\r\n"
                + "break;\r\n"
                + "default:\r\n"
                + "trace(\"default clause\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardTernarOperator() {
        decompileMethod("standard", "testTernarOperator", "var a:* = 5;\r\n"
                + "var b:* = 4;\r\n"
                + "var c:* = 4;\r\n"
                + "var d:* = 78;\r\n"
                + "var e:* = a == b?c == d?1:7:3;\r\n"
                + "trace(\"e=\" + e);\r\n",
                false);
    }

    @Test
    public void testStandardTry() {
        decompileMethod("standard", "testTry", "var i:int = 0;\r\n"
                + "i = 7;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "trace(\"try body\");\r\n"
                + "}\r\n"
                + "catch(e:DefinitionError)\r\n"
                + "{\r\n"
                + "trace(\"catched DefinitionError\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"Error message:\" + e.message);\r\n"
                + "trace(\"Stacktrace:\" + e.getStackTrace());\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"Finally part\");\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n",
                false);
    }

    @Test
    public void testStandardTryReturn() {
        decompileMethod("standard", "testTryReturn", "var i:int = 0;\r\n"
                + "var b:Boolean = false;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "i = 0;\r\n"
                + "b = true;\r\n"
                + "if(i > 0)\r\n"
                + "{\r\n"
                + "while(this.testDoWhile2())\r\n"
                + "{\r\n"
                + "if(b)\r\n"
                + "{\r\n"
                + "return 5;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n"
                + "i++;\r\n"
                + "return 2;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "}\r\n"
                + "return 4;\r\n",
                false);
    }

    @Test
    public void testStandardTryReturn2() {
        decompileMethod("standard", "testTryReturn2", "var c:Boolean = false;\r\n"
                + "trace(\"before\");\r\n"
                + "var a:Boolean = true;\r\n"
                + "var b:Boolean = false;\r\n"
                + "c = true;\r\n"
                + "var d:Boolean = false;\r\n"
                + "var e:Boolean = true;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(a)\r\n"
                + "{\r\n"
                + "return \"A\";\r\n"
                + "}\r\n"
                + "if(b)\r\n"
                + "{\r\n"
                + "return \"B\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "if(c)\r\n"
                + "{\r\n"
                + "return \"C\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "if(d)\r\n"
                + "{\r\n"
                + "return \"D\";\r\n"
                + "}\r\n"
                + "if(e)\r\n"
                + "{\r\n"
                + "return \"E\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return \"X\";\r\n",
                false);
    }

    @Test
    public void testStandardUsagesTry() {
        decompileMethod("standard", "testUsagesTry", "var k:int = 5;\r\n"
                + "switch(k)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "trace(\"1\");\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "trace(\"2\");\r\n"
                + "}\r\n"
                + "var a:Boolean = true;\r\n"
                + "var b:Boolean = true;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(b)\r\n"
                + "{\r\n"
                + "return \"B\";\r\n"
                + "}\r\n"
                + "trace(\"A\");\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "trace(\"E\");\r\n"
                + "}\r\n"
                + "finally\r\n"
                + "{\r\n"
                + "trace(\"finally\");\r\n"
                + "}\r\n"
                + "trace(\"after\");\r\n"
                + "return \"X\";\r\n",
                false);
    }

    @Test
    public void testStandardVector() {
        decompileMethod("standard", "testVector", "var v:Vector.<String> = new Vector.<String>();\r\n"
                + "v.push(\"hello\");\r\n"
                + "v[0] = \"hi\";\r\n"
                + "v[5 * 8 - 39] = \"hi2\";\r\n"
                + "trace(v[0]);\r\n",
                false);
    }

    @Test
    public void testStandardVector2() {
        decompileMethod("standard", "testVector2", "var a:Vector.<Vector.<int>> = new Vector.<Vector.<int>>();\r\n"
                + "var b:Vector.<int> = new <int>[10,20,30];\r\n",
                false);
    }

    @Test
    public void testStandardWhileAnd() {
        decompileMethod("standard", "testWhileAnd", "var a:int = 5;\r\n"
                + "var b:int = 10;\r\n"
                + "while(a < 10 && b > 1)\r\n"
                + "{\r\n"
                + "a++;\r\n"
                + "b--;\r\n"
                + "}\r\n"
                + "a = 7;\r\n"
                + "b = 9;\r\n",
                false);
    }

    @Test
    public void testStandardWhileContinue() {
        decompileMethod("standard", "testWhileContinue", "var a:* = 5;\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "if(a == 9)\r\n"
                + "{\r\n"
                + "if(a == 8)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "if(a == 9)\r\n"
                + "{\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "trace(\"hello 1\");\r\n"
                + "}\r\n"
                + "trace(\"hello2\");\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardWhileTry() {
        decompileMethod("standard", "testWhileTry", "while(true)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "while(true)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testStandardWhileTry2() {
        decompileMethod("standard", "testWhileTry2", "var j:* = undefined;\r\n"
                + "for(var i:* = 0; i < 100; i++)\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "for(j = 0; j < 20; j++)\r\n"
                + "{\r\n"
                + "trace(\"a\");\r\n"
                + "}\r\n"
                + "}\r\n"
                + "catch(e:EOFError)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "catch(e:Error)\r\n"
                + "{\r\n"
                + "continue;\r\n"
                + "}\r\n"
                + "trace(\"after_try\");\r\n"
                + "}\r\n"
                + "trace(\"end\");\r\n",
                false);
    }

    @Test
    public void testStandardXml() {
        decompileMethod("standard", "testXml", "var g:XML = null;\r\n"
                + "var name:String = \"ahoj\";\r\n"
                + "var myXML:XML = <order id=\"604\">\r\n"
                + "<book isbn=\"12345\">\r\n"
                + "<title>{name}</title>\r\n"
                + "</book>\r\n"
                + "</order>;\r\n"
                + "var k:* = myXML.@id;\r\n"
                + "var all:String = myXML.@*.toXMLString();\r\n"
                + "k = myXML.book;\r\n"
                + "k = myXML.book.(@isbn == \"12345\");\r\n"
                + "g = <script>\r\n"
                + "<![CDATA[\r\n"
                + "function() {\r\n"
                + "\r\n"
                + "FBAS = {\r\n"
                + "\r\n"
                + "setSWFObjectID: function( swfObjectID ) {\r\n"
                + "FBAS.swfObjectID = swfObjectID;\r\n"
                + "},\r\n"
                + "\r\n"
                + "init: function( opts ) {\r\n"
                + "FB.init( FB.JSON.parse( opts ) );\r\n"
                + "\r\n"
                + "FB.Event.subscribe( 'auth.sessionChange', function( response ) {\r\n"
                + "FBAS.updateSwfSession( response.session );\r\n"
                + "} );\r\n"
                + "},\r\n"
                + "\r\n"
                + "setCanvasAutoResize: function( autoSize, interval ) {\r\n"
                + "FB.Canvas.setAutoResize( autoSize, interval );\r\n"
                + "},\r\n"
                + "\r\n"
                + "setCanvasSize: function( width, height ) {\r\n"
                + "FB.Canvas.setSize( { width: width, height: height } );\r\n"
                + "},\r\n"
                + "\r\n"
                + "login: function( opts ) {\r\n"
                + "FB.login( FBAS.handleUserLogin, FB.JSON.parse( opts ) );\r\n"
                + "},\r\n"
                + "\r\n"
                + "addEventListener: function( event ) {\r\n"
                + "FB.Event.subscribe( event, function( response ) {\r\n"
                + "FBAS.getSwf().handleJsEvent( event, FB.JSON.stringify( response ) );\r\n"
                + "} );\r\n"
                + "},\r\n"
                + "\r\n"
                + "handleUserLogin: function( response ) {\r\n"
                + "if( response.session == null ) {\r\n"
                + "FBAS.updateSwfSession( null );\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "\r\n"
                + "if( response.perms != null ) {\r\n"
                + "// user is logged in and granted some permissions.\r\n"
                + "// perms is a comma separated list of granted permissions\r\n"
                + "FBAS.updateSwfSession( response.session, response.perms );\r\n"
                + "} else {\r\n"
                + "FBAS.updateSwfSession( response.session );\r\n"
                + "}\r\n"
                + "},\r\n"
                + "\r\n"
                + "logout: function() {\r\n"
                + "FB.logout( FBAS.handleUserLogout );\r\n"
                + "},\r\n"
                + "\r\n"
                + "handleUserLogout: function( response ) {\r\n"
                + "swf = FBAS.getSwf();\r\n"
                + "swf.logout();\r\n"
                + "},\r\n"
                + "\r\n"
                + "ui: function( params ) {\r\n"
                + "obj = FB.JSON.parse( params );\r\n"
                + "method = obj.method;\r\n"
                + "cb = function( response ) { FBAS.getSwf().uiResponse( FB.JSON.stringify( response ), method ); }\r\n"
                + "FB.ui( obj, cb );\r\n"
                + "},\r\n"
                + "\r\n"
                + "getSession: function() {\r\n"
                + "session = FB.getSession();\r\n"
                + "return FB.JSON.stringify( session );\r\n"
                + "},\r\n"
                + "\r\n"
                + "getLoginStatus: function() {\r\n"
                + "FB.getLoginStatus( function( response ) {\r\n"
                + "if( response.session ) {\r\n"
                + "FBAS.updateSwfSession( response.session );\r\n"
                + "} else {\r\n"
                + "FBAS.updateSwfSession( null );\r\n"
                + "}\r\n"
                + "} );\r\n"
                + "},\r\n"
                + "\r\n"
                + "getSwf: function getSwf() {\r\n"
                + "return document.getElementById( FBAS.swfObjectID );\r\n"
                + "},\r\n"
                + "\r\n"
                + "updateSwfSession: function( session, extendedPermissions ) {\r\n"
                + "swf = FBAS.getSwf();\r\n"
                + "extendedPermissions = ( extendedPermissions == null ) ? '' : extendedPermissions;\r\n"
                + "\r\n"
                + "if( session == null ) {\r\n"
                + "swf.sessionChange( null );\r\n"
                + "} else {\r\n"
                + "swf.sessionChange( FB.JSON.stringify( session ), FB.JSON.stringify( extendedPermissions.split( ',' ) ) );\r\n"
                + "}\r\n"
                + "}\r\n"
                + "};\r\n"
                + "}\r\n"
                + "]]>\r\n"
                + "</script>;\r\n",
                false);
    }

    @Test
    public void testAssembledDoubleDup() {
        decompileMethod("assembled", "testDoubleDup", "var _loc10_:Rectangle = myprop(_loc5_);\r\n"
                + "_loc10_.mymethod(-_loc10_.width,-_loc10_.height);\r\n",
                false);
    }

    @Test
    public void testAssembledDup() {
        decompileMethod("assembled", "testDup", "return 1 - (var _loc1_:Number = 1 - _loc1_ / _loc4_) * _loc1_;\r\n",
                false);
    }

    @Test
    public void testAssembledDupAssignment() {
        decompileMethod("assembled", "testDupAssignment", "var _loc1_:int = 0;\r\n"
                + "var _loc2_:int = 10;\r\n"
                + "if(_loc1_ = _loc2_)\r\n"
                + "{\r\n"
                + "trace(_loc2_);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testAssembledForEach() {
        decompileMethod("assembled", "testForEach", "var _loc5_:* = undefined;\r\n"
                + "var _loc2_:* = 0;\r\n"
                + "var _loc3_:int = 0;\r\n"
                + "for each(var _loc4_ in _loc5_)\r\n"
                + "{\r\n"
                + "if(_loc4_ != null)\r\n"
                + "{\r\n"
                + "_loc2_ = _loc4_;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "_loc3_ = 0;\r\n",
                false);
    }

    @Test
    public void testAssembledForEachCoerced() {
        decompileMethod("assembled", "testForEachCoerced", "for each(var _loc6_ in someprop)\r\n"
                + "{\r\n"
                + "_loc6_.methodname(_loc1_,_loc2_,_loc5_);\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testAssembledIncrement() {
        decompileMethod("assembled", "testIncrement", "super();\r\n"
                + "b = a++;\r\n",
                false);
    }

    @Test
    public void testAssembledIncrement2() {
        decompileMethod("assembled", "testIncrement2", "if(++loadCount == 2)\r\n"
                + "{\r\n"
                + "somemethod();\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testAssembledIncrement3() {
        decompileMethod("assembled", "testIncrement3", "_loc1_.length--;\r\n",
                false);
    }

    @Test
    public void testAssembledSetSlotDup() {
        decompileMethod("assembled", "testSetSlotDup", "var _loc5_:int = 5;\r\n"
                + "myname.somemethod(\"okay\",myslot = _loc5_);\r\n"
                + "myname.start();\r\n",
                false);
    }

    @Test
    public void testAssembledSetSlotFindProperty() {
        decompileMethod("assembled", "testSetSlotFindProperty", "return var myprop:int = 50;\r\n",
                false);
    }

    @Test
    public void testAssembledSwitch() {
        decompileMethod("assembled", "testSwitch", "switch(int(somevar))\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "var _loc2_:String = \"X\";\r\n"
                + "return;\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "_loc2_ = \"A\";\r\n"
                + "break;\r\n"
                + "case 3:\r\n"
                + "_loc2_ = \"B\";\r\n"
                + "break;\r\n"
                + "case 4:\r\n"
                + "_loc2_ = \"C\";\r\n"
                + "}\r\n"
                + "_loc2_ = \"after\";\r\n",
                false);
    }

    @Test
    public void testAssembledSwitchDefault() {
        decompileMethod("assembled", "testSwitchDefault", "switch(5)\r\n"
                + "{\r\n"
                + "case 6:\r\n"
                + "var _loc2_:int = 6;\r\n"
                + "case 0:\r\n"
                + "_loc2_ = 0;\r\n"
                + "break;\r\n"
                + "case 1:\r\n"
                + "_loc2_ = 1;\r\n"
                + "case 5:\r\n"
                + "_loc2_ = 5;\r\n"
                + "break;\r\n"
                + "case 3:\r\n"
                + "_loc2_ = 3;\r\n"
                + "break;\r\n"
                + "default:\r\n"
                + "_loc2_ = 100;\r\n"
                + "}\r\n",
                false);
    }

    @Test
    public void testOptionalParameters() {
        String methodName = "testOptionalParameters";
        String className = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);

        int clsIndex = -1;
        DoABC2Tag tag = null;
        ABC abc = null;
        for (Tag t : swfMap.get("standard").getTags()) {
            if (t instanceof DoABC2Tag) {
                tag = (DoABC2Tag) t;
                abc = tag.getABC();
                clsIndex = abc.findClassByName(new DottedChain(new String[]{"tests", className}, ""));
                if (clsIndex > -1) {
                    break;
                }
            }
        }
        assertTrue(clsIndex > -1);

        int methodInfo = abc.findMethodInfoByName(clsIndex, "run");
        int bodyIndex = abc.findMethodBodyByName(clsIndex, "run");
        assertTrue(methodInfo > -1);
        assertTrue(bodyIndex > -1);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        abc.method_info.get(methodInfo).getParamStr(writer, abc.constants, abc.bodies.get(bodyIndex), abc, new ArrayList<>());
        String actualResult = writer.toString().replaceAll("[ \r\n]", "");
        String expectedResult = "p1:Event=null,p2:Number=1,p3:Number=-1,p4:Number=-1.1,p5:Number=-1.1,p6:String=\"a\"";
        expectedResult = expectedResult.replaceAll("[ \r\n]", "");
        assertEquals(actualResult, expectedResult);
    }

    @Test
    public void testMyPackage1TestClass() {
        decompileScriptPack("tests_classes.mypackage1.TestClass", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public class TestClass implements tests_classes.mypackage1.TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         trace(\"pkg1hello\");\n"
                + "         return \"pkg1hello\";\n"
                + "      }\n"
                + "      \n"
                + "      public function testMethod1() : void\n"
                + "      {\n"
                + "         var a:tests_classes.mypackage1.TestInterface = this;\n"
                + "         a.testMethod1();\n"
                + "         var b:tests_classes.mypackage2.TestInterface = this;\n"
                + "         b = new tests_classes.mypackage2.TestClass();\n"
                + "      }\n"
                + "      \n"
                + "      public function testMethod2() : void\n"
                + "      {\n"
                + "         var a:tests_classes.mypackage1.TestInterface = this;\n"
                + "         a.testMethod1();\n"
                + "         var b:tests_classes.mypackage2.TestInterface = this;\n"
                + "         b = new tests_classes.mypackage2.TestClass();\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1TestClass2() {
        decompileScriptPack("tests_classes.mypackage1.TestClass2", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public class TestClass2\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass2()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         var a:tests_classes.mypackage1.TestClass = null;\n"
                + "         var b:tests_classes.mypackage2.TestClass = null;\n"
                + "         var c:tests_classes.mypackage3.TestClass = null;\n"
                + "         a = new tests_classes.mypackage1.TestClass();\n"
                + "         b = new tests_classes.mypackage2.TestClass();\n"
                + "         c = new tests_classes.mypackage3.TestClass();\n"
                + "         var res:String = a.testCall() + b.testCall() + c.testCall() + this.testCall2() + myNamespace::testCall3();\n"
                + "         trace(res);\n"
                + "         return res;\n"
                + "      }\n"
                + "      \n"
                + "      myNamespace function testCall2() : String\n"
                + "      {\n"
                + "         return \"1\";\n"
                + "      }\n"
                + "      \n"
                + "      myNamespace function testCall3() : String\n"
                + "      {\n"
                + "         return myNamespace::testCall2();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall2() : String\n"
                + "      {\n"
                + "         return \"2\";\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1TestInterface() {
        decompileScriptPack("tests_classes.mypackage1.TestInterface", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public interface TestInterface extends tests_classes.mypackage2.TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      function testMethod1() : void;\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage1MyNamespace() {
        decompileScriptPack("tests_classes.mypackage1.myNamespace", "package tests_classes.mypackage1\n"
                + "{\n"
                + "   public namespace myNamespace = \"https://www.free-decompiler.com/flash/test/namespace\";\n"
                + "}");
    }

    @Test
    public void testMyPackage2TestClass() {
        decompileScriptPack("tests_classes.mypackage2.TestClass", "package tests_classes.mypackage2\n"
                + "{\n"
                + "   public class TestClass implements TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         trace(\"pkg2hello\");\n"
                + "         return \"pkg2hello\";\n"
                + "      }\n"
                + "      \n"
                + "      public function testMethod2() : void\n"
                + "      {\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage2TestInterface() {
        decompileScriptPack("tests_classes.mypackage2.TestInterface", "package tests_classes.mypackage2\n"
                + "{\n"
                + "   public interface TestInterface\n"
                + "   {\n"
                + "       \n"
                + "      function testMethod2() : void;\n"
                + "   }\n"
                + "}");
    }

    @Test
    public void testMyPackage3TestClass() {
        decompileScriptPack("tests_classes.mypackage3.TestClass", "package tests_classes.mypackage3\n"
                + "{\n"
                + "   public class TestClass\n"
                + "   {\n"
                + "       \n"
                + "      public function TestClass()\n"
                + "      {\n"
                + "         super();\n"
                + "      }\n"
                + "      \n"
                + "      public function testCall() : String\n"
                + "      {\n"
                + "         trace(\"pkg3hello\");\n"
                + "         return \"pkg3hello\";\n"
                + "      }\n"
                + "   }\n"
                + "}");
    }
}
