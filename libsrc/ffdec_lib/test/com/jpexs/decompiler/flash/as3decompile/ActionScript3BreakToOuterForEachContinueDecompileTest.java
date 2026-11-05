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
 * An inner loop that ends the body of a for each loop must break, its natural
 * exit being the continue part of the outer loop.
 *
 * @see testdata/as3_break_to_outer_foreach_continue/README.md
 */
public class ActionScript3BreakToOuterForEachContinueDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("break_to_outer_foreach_continue", "testdata/as3_break_to_outer_foreach_continue/bin/as3_break_to_outer_foreach_continue.swf");
    }

    @Test
    public void testBreakToOuterForEachContinue() {
        decompileMethod("break_to_outer_foreach_continue", "testBreakToOuterForEachContinue",
                "for each(var n:String in names)\r\n"
                + "{\r\n"
                + "for each(var k:String in keys)\r\n"
                + "{\r\n"
                + "if(k == n)\r\n"
                + "{\r\n"
                + "out += k;\r\n"
                + "break;\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }
}
