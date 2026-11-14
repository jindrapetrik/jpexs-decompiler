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
 * A returnvoid in a function that returns a value must print with a value.
 *
 * @see testdata/haxe_returnvoid_typed/README.md
 */
public class ActionScript3HaxeReturnVoidTypedDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("haxe_returnvoid_typed", "testdata/haxe_returnvoid_typed/bin/haxe_returnvoid_typed.swf");
    }

    @Test
    public void testHaxeReturnVoidTyped() {
        decompileMethod("haxe_returnvoid_typed", "testHaxeReturnVoidTyped",
                "switch(kind.index)\r\n"
                + "{\r\n"
                + "case 0:\r\n"
                + "return param1 * 3;\r\n"
                + "case 1:\r\n"
                + "return param1 * param1;\r\n"
                + "case 2:\r\n"
                + "return param1;\r\n"
                + "default:\r\n"
                + "return undefined;\r\n"
                + "}\r\n",
                false);
    }
}
