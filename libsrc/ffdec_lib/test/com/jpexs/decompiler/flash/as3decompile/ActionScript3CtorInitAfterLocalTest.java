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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * ASC may emit {@code setlocal} for an uninitialized ctor local before
 * {@code initproperty} of a Trait_Const. Those stores must still become trait
 * initializers; leaving {@code const x; x = ...;} in the constructor is
 * rejected by amxmlc.
 */
public class ActionScript3CtorInitAfterLocalTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("ctorlocal", "testdata/as3_ctor_init_after_local/bin/as3_ctor_init_after_local.swf");
    }

    @Test
    public void testConstInitPromotedDespiteLeadingLocal() {
        Configuration.decompilationTimeoutFile.set(5 * 60);
        Configuration.decompilationTimeoutSingleMethod.set(60);

        ScriptPack scriptPack = null;
        ABC abc = null;
        SWF swf = getSwf("ctorlocal");
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                abc = ((DoABC2Tag) t).getABC();
                scriptPack = abc.findScriptPackByPath("tests_classes.TestCtorInitAfterLocal", Arrays.asList(abc));
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
        String actual = cleanPCode(writer.toString());

        // Legal recompile form (amxmlc accepts const with initializer).
        assertTrue(actual.contains("private const scriptXml:XML = <script>"), actual);
        // Must not leave an assignment to the const in the constructor.
        assertFalse(actual.contains("scriptXml = <script>"), actual);
    }
}
