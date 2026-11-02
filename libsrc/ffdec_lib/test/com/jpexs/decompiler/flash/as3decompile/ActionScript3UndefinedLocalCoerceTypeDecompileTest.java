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
 * A local declared as undefined takes the type its assignments coerce to, and
 * stays untyped when they do not coerce.
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
}
