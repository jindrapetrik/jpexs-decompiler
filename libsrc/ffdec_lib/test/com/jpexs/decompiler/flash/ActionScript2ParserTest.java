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

import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.CompilationException;
import java.io.IOException;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2ParserTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
    }

    private void parseAS2(String script) {
        try {
            ActionScript2Parser par = new ActionScript2Parser(SWF.DEFAULT_VERSION);
            par.actionsFromString(script);
        } catch (IOException | CompilationException | ParseException ex) {
            fail("Unable to parse: " + script, ex);
        }
    }

    @Test
    private void testAS2Parse1() {
        parseAS2(
                "var x = true;\n"
                + "while(x) { }");
    }

    @Test
    private void testAS2Parse2() {
        parseAS2(
                "function test(a, b, c)\n"
                + "{\n"
                + "   return a != 0?b * 2:c;\n"
                + "}");
    }

    @Test
    private void testAS2Parse3() {
        parseAS2(
                "for(;i < 10;i++) { }");
    }

    @Test
    private void testAS2Parse4() {
        parseAS2(
                "class cl1\n"
                + "{\n"
                + "   function stop()\n"
                + "   {\n"
                + "   }\n"
                + "}");
    }

    @Test
    private void testAS2Parse5() {
        parseAS2(
                "if(!test.T1)\n"
                + "{\n"
                + "   test.T1 = function()\n"
                + "   {\n"
                + "      super();\n"
                + "   }.Initialize = function(obj)\n"
                + "   {\n"
                + "      var x = 1;\n"
                + "   };\n"
                + "}");
    }

    @Test
    private void testAS2SimpleConstantPoolOrder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ActionPush.MAX_CONSTANT_INDEX_TYPE8; i++) {
            sb.append("trace(\"" + i + "\");\n");
        }
        parseAS2(sb.toString());
    }

    @Test
    private void testAS2LargeConstantPoolOrder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ActionPush.MAX_CONSTANT_INDEX_TYPE8 + 100; i++) {
            sb.append("trace(\"" + i + "\");\n");
        }
        parseAS2(sb.toString());
    }
}
