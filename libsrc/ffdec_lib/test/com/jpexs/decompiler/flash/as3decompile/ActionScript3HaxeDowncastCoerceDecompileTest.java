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
 * A coerce to a subtype, which Haxe emits for an unchecked cast, must print as
 * an explicit cast.
 *
 * @see testdata/haxe_downcast_coerce/README.md
 */
public class ActionScript3HaxeDowncastCoerceDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("haxe_downcast_coerce", "testdata/haxe_downcast_coerce/bin/haxe_downcast_coerce.swf");
    }

    @Test
    public void testHaxeDowncastCoerce() {
        decompileMethod("haxe_downcast_coerce", "testHaxeDowncastCoerce",
                "var _loc3_:Circle = Circle(param1);\r\n"
                + "out = _loc3_.radius + radius(Circle(param2));\r\n"
                + "var _loc4_:Shape = _loc3_;\r\n"
                + "out += radius(Circle(_loc4_));\r\n",
                false);
    }
}
