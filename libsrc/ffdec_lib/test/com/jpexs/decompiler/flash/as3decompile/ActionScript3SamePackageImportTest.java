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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.IOException;
import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * File-private helpers are emitted outside the package block and must import
 * same-package public types to be recompilable.
 */
public class ActionScript3SamePackageImportTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("samepkg", "testdata/as3_samepackage_imports/bin/as3_samepackage_imports.swf");
    }

    @Test
    public void testFilePrivateClassImportsSamePackageType() {
        Configuration.decompilationTimeoutFile.set(5 * 60);
        Configuration.decompilationTimeoutSingleMethod.set(60);

        ScriptPack scriptPack = null;
        ABC abc = null;
        SWF swf = getSwf("samepkg");
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                abc = ((DoABC2Tag) t).getABC();
                scriptPack = abc.findScriptPackByPath("tests_classes.samepkg.Outer", Arrays.asList(abc));
                if (scriptPack != null) {
                    break;
                }
            }
        }
        assertNotNull(scriptPack);

        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            scriptPack.toSource(swf.getAbcIndex(), writer, abc.script_info.get(scriptPack.scriptIndex).traits.traits, new ConvertData(), ScriptExportMode.AS, false, false, false);
        } catch (InterruptedException ex) {
            fail(ex.getMessage());
        }
        writer.finishHilights();

        String expected = "package tests_classes.samepkg\n"
                + "{\n"
                + "   public class Outer\n"
                + "   {\n"
                + "       \n"
                + "      \n"
                + "      public function Outer()\n"
                + "      {\n"
                + "         super();\n"
                + "         var helper:Helper = new Helper(new SharedType());\n"
                + "         helper.touch();\n"
                + "      }\n"
                + "   }\n"
                + "}\n"
                + "\n"
                + "import tests_classes.samepkg.Outer;\n"
                + "import tests_classes.samepkg.SharedType;\n"
                + "\n"
                + "class Helper\n"
                + "{\n"
                + "   \n"
                + "   private var shared:SharedType;\n"
                + "   \n"
                + "   public function Helper(param1:SharedType)\n"
                + "   {\n"
                + "      super();\n"
                + "      this.shared = param1;\n"
                + "   }\n"
                + "   \n"
                + "   public function touch() : void\n"
                + "   {\n"
                + "      trace(this.shared.tag());\n"
                + "   }\n"
                + "}";

        assertEquals(cleanPCode(writer.toString()), cleanPCode(expected));
    }
}
