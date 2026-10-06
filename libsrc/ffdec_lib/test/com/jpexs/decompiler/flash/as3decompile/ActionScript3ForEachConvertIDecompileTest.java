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
 * Regression tests for convert_i / coerce after for-each nextvalue.
 *
 * @see testdata/as3_foreach_convert_i/README.md
 */
public class ActionScript3ForEachConvertIDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("foreach_convert_i", "testdata/as3_foreach_convert_i/bin/as3_foreach_convert_i.swf");
    }

    @Test
    public void testForEachConvertI() {
        decompileMethod("foreach_convert_i", "testForEachConvertI",
                "var values:Vector.<Number> = new <Number>[1.9,2.1,3.7];\r\n"
                + "var total:int = 0;\r\n"
                + "for each(var n:int in values)\r\n"
                + "{\r\n"
                + "total += n;\r\n"
                + "}\r\n"
                + "return total;\r\n",
                false);
    }

    @Test
    public void testForEachConvertIUse() {
        decompileMethod("foreach_convert_i", "testForEachConvertIUse",
                "var values:Vector.<Number> = new <Number>[1.9,2.1,3.7];\r\n"
                + "var total:int = 0;\r\n"
                + "for each(var n in values)\r\n"
                + "{\r\n"
                + "total += int(n);\r\n"
                + "}\r\n"
                + "return total;\r\n",
                false);
    }
}
