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
 * A try in a loop after a compound condition continue must keep its try.
 *
 * @see testdata/as3_try_after_compound_continue/README.md
 */
public class ActionScript3TryAfterCompoundContinueDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("try_after_compound_continue", "testdata/as3_try_after_compound_continue/bin/as3_try_after_compound_continue.swf");
    }

    @Test
    public void testTryAfterCompoundContinue() {
        decompileMethod("try_after_compound_continue", "testTryAfterCompoundContinue",
                "for each(var s:String in args)\r\n"
                + "{\r\n"
                + "if(!(s != \"a\" && s != \"b\"))\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "if(s.length > 5)\r\n"
                + "{\r\n"
                + "throw new Error(\"long\");\r\n"
                + "}\r\n"
                + "out += s;\r\n"
                + "}\r\n"
                + "catch(error:Error)\r\n"
                + "{\r\n"
                + "out += error.message;\r\n"
                + "}\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }
}
