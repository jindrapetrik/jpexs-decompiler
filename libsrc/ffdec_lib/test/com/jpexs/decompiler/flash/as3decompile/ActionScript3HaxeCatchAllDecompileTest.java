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
 * Haxe catches, compiled as exceptions without type and name, must keep their
 * body.
 *
 * @see testdata/haxe_catch_all/README.md
 */
public class ActionScript3HaxeCatchAllDecompileTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("haxe_catch_all", "testdata/haxe_catch_all/bin/haxe_catch_all.swf");
    }

    @Test
    public void testHaxeCatchAll() {
        decompileMethod("haxe_catch_all", "testHaxeCatchAll",
                "var _loc3_:* = null;\r\n"
                + "var _loc4_:* = null;\r\n"
                + "try\r\n"
                + "{\r\n"
                + "out += param1;\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "_loc3_ = _loc_e_;\r\n"
                + "_loc4_ = Exception.caught(_loc3_).unwrap();\r\n"
                + "out += \"err \" + Std.string(_loc4_);\r\n"
                + "}\r\n"
                + "out += \"end\";\r\n",
                false);
    }

    @Test
    public void testHaxeCatchAllInLoop() {
        decompileMethod("haxe_catch_all", "testHaxeCatchAllInLoop",
                "var _loc4_:* = null as String;\r\n"
                + "var _loc5_:* = null;\r\n"
                + "var _loc3_:int = 0;\r\n"
                + "while(_loc3_ < int(param1.length))\r\n"
                + "{\r\n"
                + "_loc4_ = param1[_loc3_];\r\n"
                + "_loc3_++;\r\n"
                + "if(_loc4_ == \"x\")\r\n"
                + "{\r\n"
                + "try\r\n"
                + "{\r\n"
                + "out += _loc4_;\r\n"
                + "return;\r\n"
                + "}\r\n"
                + "catch(_loc_e_:*)\r\n"
                + "{\r\n"
                + "_loc5_ = _loc_e_;\r\n"
                + "out += \"err\";\r\n"
                + "}\r\n"
                + "}\r\n"
                + "}\r\n",
                false);
    }
}
