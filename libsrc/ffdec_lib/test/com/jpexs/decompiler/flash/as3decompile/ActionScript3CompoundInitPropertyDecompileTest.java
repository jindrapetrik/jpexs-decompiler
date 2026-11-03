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
 * Compound assignments that Haxe compiles with initproperty must not print
 * their dup temporary.
 *
 * @see testdata/haxe_compound_initproperty/README.md
 */
public class ActionScript3CompoundInitPropertyDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("compound_initproperty", "testdata/haxe_compound_initproperty/bin/haxe_compound_initproperty.swf");
    }

    @Test
    public void testCompoundInitProperty() {
        decompileMethod("compound_initproperty", "testCompoundInitProperty",
                "p.x += 1.5;\r\n"
                + "p.y -= p.x;\r\n"
                + "p.x *= p.y;\r\n",
                false);
    }
}
