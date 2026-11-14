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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Static vars that Haxe initializes in the script initializer must import the
 * types their values use.
 *
 * @see testdata/haxe_static_init_imports/README.md
 */
public class ActionScript3HaxeStaticInitImportsTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("haxe_static_init_imports", "testdata/haxe_static_init_imports/bin/haxe_static_init_imports.swf");
    }

    @Test
    public void testHaxeStaticInitImports() {
        SWF swf = getSwf("haxe_static_init_imports");
        List<ABC> abcs = new ArrayList<>();
        for (ABCContainerTag abcTag : swf.getAbcList()) {
            abcs.add(abcTag.getABC());
        }
        ScriptPack scriptPack = null;
        for (ABC a : abcs) {
            scriptPack = a.findScriptPackByPath("tests.TestHaxeStaticInitImports", abcs);
            if (scriptPack != null) {
                break;
            }
        }
        assertNotNull(scriptPack);
        ABC abc = scriptPack.abc;
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            scriptPack.toSource(swf.getAbcIndex(), writer, abc.script_info.get(scriptPack.scriptIndex).traits.traits, new ConvertData(), ScriptExportMode.AS, false, false, false);
        } catch (InterruptedException ex) {
            fail();
        }
        writer.finishHilights();
        assertEquals(cleanPCode(writer.toString()), cleanPCode("package tests\n"
                + "{\n"
                + "   import other.Consts;\n"
                + "   \n"
                + "   public class TestHaxeStaticInitImports\n"
                + "   {\n"
                + "      \n"
                + "      public static var p:Pair = new Pair(Consts.B);\n"
                + "      \n"
                + "      public static var q:int = Consts.B + 1;\n"
                + "      \n"
                + "      public function TestHaxeStaticInitImports()\n"
                + "      {\n"
                + "      }\n"
                + "   }\n"
                + "}\n"));
    }
}
