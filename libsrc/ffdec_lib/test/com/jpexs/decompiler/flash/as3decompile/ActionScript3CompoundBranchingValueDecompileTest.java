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
 * Compound assignments to unqualified properties whose value branches must not
 * leave the dup temporary behind.
 *
 * @see testdata/as3_compound_branching_value/README.md
 */
public class ActionScript3CompoundBranchingValueDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("compound_branching_value", "testdata/as3_compound_branching_value/bin/as3_compound_branching_value.swf");
    }

    @Test
    public void testCompoundBranchingValue() {
        decompileMethod("compound_branching_value", "testCompoundBranchingValue",
                "_s += (i == 0 ? \"\" : \", \") + \"x\";\r\n"
                + "sCount += f ? 1 : 2;\r\n"
                + "n += i > 2 && i < 10 ? i : -i;\r\n"
                + "return _s;\r\n",
                false);
    }
}
