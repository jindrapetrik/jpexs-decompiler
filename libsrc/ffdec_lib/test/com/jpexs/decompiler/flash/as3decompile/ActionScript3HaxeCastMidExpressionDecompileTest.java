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
package com.jpexs.decompiler.flash.as3decompile;

import com.jpexs.decompiler.flash.ActionScript3DecompileTestBase;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A cast checked inside an expression must not leave pushes and pops.
 *
 * @see testdata/haxe_cast_mid_expression/README.md
 */
public class ActionScript3HaxeCastMidExpressionDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("haxe_cast_mid_expression", "testdata/haxe_cast_mid_expression/bin/haxe_cast_mid_expression.swf");
    }

    @Test
    public void testHaxeCastMidExpression() {
        decompileMethod("haxe_cast_mid_expression", "testHaxeCastMidExpression",
                "if(param1 is String)\r\n"
                + "{\r\n"
                + "var _temp_1:* = param1;\r\n"
                + "if(!(_temp_1 is String))\r\n"
                + "{\r\n"
                + "throw \"Class cast error\";\r\n"
                + "}\r\n"
                + "return \"\\\"\" + TestHaxeCastMidExpression.esc(_temp_1) + \"\\\"\";\r\n"
                + "}\r\n"
                + "return \"?\";\r\n",
                false);
    }
}
