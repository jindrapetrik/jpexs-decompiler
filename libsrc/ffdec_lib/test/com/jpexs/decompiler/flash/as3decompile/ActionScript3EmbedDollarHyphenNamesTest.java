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
import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * amxmlc [Embed] invents {@code name$md5-int} class names (illegal hyphen).
 * FFDec renames to the stem before {@code $} always — not tied to
 * "Deobfuscate identifiers", since these are compiler-generated, not obfuscated.
 */
public class ActionScript3EmbedDollarHyphenNamesTest extends ActionScript3DecompileTestBase {

    private static final Pattern EMBED_DOLLAR_HYPHEN = Pattern.compile(
            "^([A-Za-z_][A-Za-z0-9_]*)\\$[0-9a-fA-F]+-[0-9]+(ByteArray)?$");

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        addSwf("embeddollar", "testdata/as3_embed_dollar_hyphen_names/bin/as3_embed_dollar_hyphen_names.swf");
    }

    @Test
    public void testSuggestEmbedDollarHyphenReplacement() {
        assertEquals(IdentifiersDeobfuscation.suggestEmbedDollarHyphenReplacement(
                "loading_screen_swf$b0c2140383d3a66fbee48dad03141fd5-2045836190"),
                "loading_screen_swf");
        assertEquals(IdentifiersDeobfuscation.suggestEmbedDollarHyphenReplacement(
                "loading_screen_swf$b0c2140383d3a66fbee48dad03141fd5-2045836190ByteArray"),
                "loading_screen_swf_ByteArray");
        assertEquals(IdentifiersDeobfuscation.suggestEmbedDollarHyphenReplacement("NormalClass"), null);
    }

    @Test
    public void testSwfContainsDollarHyphenClassName() {
        SWF swf = getSwf("embeddollar");
        int matches = 0;
        for (Tag t : swf.getTags()) {
            if (!(t instanceof DoABC2Tag)) {
                continue;
            }
            ABC abc = ((DoABC2Tag) t).getABC();
            for (InstanceInfo ii : abc.instance_info) {
                if (ii.name_index == 0) {
                    continue;
                }
                String name = abc.constants.getMultiname(ii.name_index).getName(
                        new LinkedHashSet<String>(), abc, abc.constants, new ArrayList<>(), true, true);
                if (EMBED_DOLLAR_HYPHEN.matcher(name).matches()) {
                    matches++;
                    assertTrue(name.startsWith("loading_screen_swf$"), name);
                }
            }
        }
        assertTrue(matches >= 1, "expected a name$md5-int class from amxmlc [Embed]");
    }

    @Test
    public void testEmbedDollarHyphenUsesStemWithoutDeobfuscateOption() {
        // Default / identifiers-off: still rename embed $md5-int (not obfuscation)
        Configuration.autoDeobfuscateIdentifiers.set(false);
        Configuration.decompilationTimeoutFile.set(5 * 60);
        Configuration.decompilationTimeoutSingleMethod.set(60);
        IdentifiersDeobfuscation.clearCache();

        SWF swf = getSwf("embeddollar");
        ScriptPack embedPack = null;
        ABC abc = null;
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                abc = ((DoABC2Tag) t).getABC();
                // Pack path already uses stem (printable); raw $md5-int lives only in ABC
                embedPack = abc.findScriptPackByPath("loading_screen_swf_ByteArray", Arrays.asList(abc));
                if (embedPack != null) {
                    break;
                }
            }
        }
        assertNotNull(embedPack, "expected amxmlc-generated embed ByteArray class pack");

        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        try {
            embedPack.toSource(swf.getAbcIndex(), writer, abc.script_info.get(embedPack.scriptIndex).traits.traits,
                    new ConvertData(), ScriptExportMode.AS, false, false, false);
        } catch (InterruptedException ex) {
            fail(ex.getMessage());
        }
        writer.finishHilights();
        String actual = cleanPCode(writer.toString());

        assertTrue(actual.contains("class loading_screen_swf_ByteArray"), actual);
        assertFalse(actual.contains("§loading_screen_swf$"), actual);
        assertFalse(actual.contains("_SafeCls_"), actual);
        assertFalse(actual.contains("_SafeStr_"), actual);
        // [Embed(source=...)] must use stem filename, not raw $md5-int
        assertFalse(Pattern.compile("Embed\\(source=\"[^\"]*\\$[0-9a-fA-F]+-[0-9]+").matcher(actual).find(), actual);
        assertTrue(actual.contains("Embed(source=\"/_assets/1_loading_screen_swf_ByteArray.bin\""), actual);
    }
}
