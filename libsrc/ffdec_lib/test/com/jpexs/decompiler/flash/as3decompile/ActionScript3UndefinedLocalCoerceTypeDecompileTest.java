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
 * A local declared as undefined, or as a numeric default (0, NaN), takes the
 * type its assignments coerce or convert to, and stays as it is when they do not.
 *
 * @see testdata/as3_undefined_local_coerce_type/README.md
 */
public class ActionScript3UndefinedLocalCoerceTypeDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("undefined_local_coerce_type", "testdata/as3_undefined_local_coerce_type/bin/as3_undefined_local_coerce_type.swf");
    }

    @Test
    public void testUndefinedLocalCoerceType() {
        decompileMethod("undefined_local_coerce_type", "testUndefinedLocalCoerceType",
                "var chain:Vector.<String> = undefined;\r\n"
                + "var untyped:* = undefined;\r\n"
                + "if(sChains.length > 0)\r\n"
                + "{\r\n"
                + "chain = sChains.pop();\r\n"
                + "chain[0] = name;\r\n"
                + "}\r\n"
                + "else\r\n"
                + "{\r\n"
                + "chain = new <String>[name];\r\n"
                + "}\r\n"
                + "untyped = chain.length;\r\n"
                + "return chain.length + untyped;\r\n",
                false);
    }

    @Test
    public void testNumericDefaultLocalCoerceType() {
        decompileMethod("undefined_local_coerce_type", "testNumericDefaultLocalCoerceType",
                "var cv:uint = 0;\r\n"
                + "var k:int = 0;\r\n"
                + "var result:uint = 0;\r\n"
                + "for(k = 1; k < mask.length; )\r\n"
                + "{\r\n"
                + "cv = mask.charCodeAt(k) - \"x\".charCodeAt(0);\r\n"
                + "if(cv > 2)\r\n"
                + "{\r\n"
                + "cv = 3;\r\n"
                + "}\r\n"
                + "result |= 1 << cv;\r\n"
                + "k++;\r\n"
                + "}\r\n"
                + "return result;\r\n",
                false);
    }
}
