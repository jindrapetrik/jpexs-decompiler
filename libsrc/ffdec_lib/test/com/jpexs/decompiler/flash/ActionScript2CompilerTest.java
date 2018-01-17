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

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.Assert;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2CompilerTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    private void testCompilation(String sourceAsToCompile, String expectedPCode) {
        try {
            SWF swf = new SWF();
            ASMSource asm = new DoActionTag(swf);

            ActionScript2Parser par = new ActionScript2Parser(swf.version);
            try {
                asm.setActions(par.actionsFromString(sourceAsToCompile));
            } catch (ActionParseException | CompilationException ex) {
                fail("Unable to parse: " + sourceAsToCompile + "/" + asm.toString(), ex);
            }

            asm.setActionBytes(Action.actionsToBytes(asm.getActions(), true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            asm.getASMSource(ScriptExportMode.PCODE, writer, null);
            String actualResult = normalizeLabels(writer.toString());
            actualResult = cleanPCode(actualResult);
            String expectedResult = cleanPCode(expectedPCode);

            Assert.assertEquals(actualResult, expectedResult);
        } catch (IOException | InterruptedException ex) {
            fail();
        }

    }

    private String normalizeLabels(String actions) {
        int labelCnt = 1;
        while (true) {
            Pattern pattern = Pattern.compile("^([a-z][0-9a-z]+):", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(actions);
            if (matcher.find()) {
                String str = matcher.group(1);
                actions = actions.replaceAll(str, "label_" + labelCnt++);
            } else {
                break;
            }
        }
        return actions;
    }

    @Test
    public void variableInInnerFuncMustNotBeStoredInRegister() {
        testCompilation("function outfunc() {\n"
                + "	var v1 = function (){\n"
                + "		var a = 1;\n"
                + "		var v3 = 2;\n"
                + "		\n"
                + "		var v4 = function (){\n"
                + "			var v5 = a + 3;\n"
                + "		}\n"
                + "	}\n"
                + "}", "ConstantPool \"v1\" \"a\"\n"
                + "DefineFunction \"outfunc\" 0 {\n"
                + "Push \"v1\"\n"
                + "DefineFunction2 \"\" 0 3 false false true false true false true false false {\n"
                + "Push \"a\" 1\n"
                + "DefineLocal\n" //critical
                + "Push 2\n"
                + "StoreRegister 1\n"
                + "Pop\n"
                + "DefineFunction2 \"\" 0 2 false false true false true false true false false {\n"
                + "Push \"a\"\n"
                + "GetVariable\n"
                + "Push 3\n"
                + "Add2\n"
                + "StoreRegister 1\n"
                + "Pop\n"
                + "}\n"
                + "StoreRegister 2\n"
                + "Pop\n"
                + "}\n"
                + "DefineLocal\n"
                + "}");
    }

    @Test
    public void parameterInInnerFuncMustNotBeStoredInRegister() {
        testCompilation("function outfunc() {\n"
                + "	var g = function (a,p2){\n"
                + "		var v1 = a + 1 + p2;\n"
                + "		\n"
                + "		var f = function (){\n"
                + "			var v2 = a + 2;\n"
                + "		}\n"
                + "	}\n"
                + "}", "ConstantPool \"g\" \"a\"\n"
                + "DefineFunction \"outfunc\" 0 {\n"
                + "Push \"g\"\n"
                + "DefineFunction2 \"\" 2 4 false false true false true false true false false 0 \"a\" 1 \"p2\" {\n"
                + "Push \"a\"\n"
                + "GetVariable\n" //critical
                + "Push 1\n"
                + "Add2\n"
                + "Push register1\n"
                + "Add2\n"
                + "StoreRegister 2\n"
                + "Pop\n"
                + "DefineFunction2 \"\" 0 2 false false true false true false true false false {\n"
                + "Push \"a\"\n"
                + "GetVariable\n"
                + "Push 2\n"
                + "Add2\n"
                + "StoreRegister 1\n"
                + "Pop\n"
                + "}\n"
                + "StoreRegister 3\n"
                + "Pop\n"
                + "}\n"
                + "DefineLocal\n"
                + "}");
    }
}
