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
 * A for-each variable declared before the loop takes the type of the coerce
 * after nextvalue.
 *
 * @see testdata/as3_foreach_hoisted_var_type/README.md
 */
public class ActionScript3ForEachHoistedVarTypeDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("foreach_hoisted_var_type", "testdata/as3_foreach_hoisted_var_type/bin/as3_foreach_hoisted_var_type.swf");
    }

    @Test
    public void testForEachHoistedVarType() {
        decompileMethod("foreach_hoisted_var_type", "testForEachHoistedVarType",
                "var node:XML = null;\r\n"
                + "var _loc4_:int = 0;\r\n"
                + "var _loc3_:XMLList = list;\r\n"
                + "for each(node in _loc3_)\r\n"
                + "{\r\n"
                + "return node.@value;\r\n"
                + "}\r\n"
                + "return null;\r\n",
                false);
    }
}
