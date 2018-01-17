/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class As3ScriptReplacerTest {

    //Commented out yet... there's no Flex SDK on build server
    //@Test
    public void testReplace() throws IOException, InterruptedException, Exception {
        MxmlcAs3ScriptReplacer replacer = new MxmlcAs3ScriptReplacer(Configuration.flexSdkLocation.get());
        SWF swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")), false);
        String replacement = "package classes\n"
                + "{\n"
                + "\n"
                + "	public dynamic class TestClass1\n"
                + "	{\n"
                + "		public var attrib:int = 5;\n"
                + "		public var sons:Array;\n"
                + "\n"
                + "		public function testHello()\n"
                + "		{\n"
                + "			trace(\"helloA\");\n"
                + "		}\n"
                + "\n"
                + "		public function method(i:int):int\n"
                + "		{\n"
                + "			trace(\"methodB\");\n"
                + "			return 7;\n"
                + "		}\n"
                + "	}\n"
                + "}";
        List<String> classNames = new ArrayList<>();
        classNames.add("classes.TestClass1");
        List<ScriptPack> packs = swf.getScriptPacksByClassNames(classNames);
        for (ScriptPack sp : packs) {
            replacer.replaceScript(sp, replacement);
            return;
        }
    }
}
