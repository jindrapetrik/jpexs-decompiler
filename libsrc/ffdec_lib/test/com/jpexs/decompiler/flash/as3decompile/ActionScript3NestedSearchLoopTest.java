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
 * ASC merges inner while-search breaks with the outer for-each continue.
 * Decompilation must recover {@code while (cond)} without inventing
 * {@code loopN:} / {@code continue loopN}.
 */
public class ActionScript3NestedSearchLoopTest extends ActionScript3DecompileTestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("nestedsearch", "testdata/as3_nested_search_loop/bin/as3_nested_search_loop.swf");
    }

    @Test
    public void testNoLabeledContinueOnNestedSearch() {
        Configuration.decompilationTimeoutFile.set(5 * 60);
        Configuration.decompilationTimeoutSingleMethod.set(60);

        ScriptPack scriptPack = null;
        ABC abc = null;
        SWF swf = getSwf("nestedsearch");
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                abc = ((DoABC2Tag) t).getABC();
                scriptPack = abc.findScriptPackByPath("tests_classes.TestNestedSearchContinue", Arrays.asList(abc));
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

        assertFalse(actual.contains("loop0:"), actual);
        assertFalse(actual.contains("continue loop0"), actual);
        assertFalse(actual.contains("while(true)"), actual);

        // removeMatches: splice stays in the match branch of while (i < length)
        assertTrue(actual.contains("while(_loc2_ < items.length)"), actual);
        assertTrue(actual.contains("items.splice(_loc2_,1)"), actual);

        // fillCache: assignment stays before break inside while (i < length)
        assertTrue(actual.contains("while(_loc3_ < _loc4_.length)"), actual);
        assertTrue(actual.contains("cache[_loc2_.key] = _loc5_"), actual);
    }
}
