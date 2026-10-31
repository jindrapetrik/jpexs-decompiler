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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2HexTest {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
    }

    private String decompileHex(String hex) throws InterruptedException {
        SWF swf = new SWF();
        DoActionTag tag = new DoActionTag(swf);
        tag.setActionBytes(Helper.hexToByteArray(hex.replaceAll("\\s+", "")));
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, tag.getActions(), "", writer, swf.getCharset());
        writer.finishHilights();
        return writer.toString().replace("\r\n", "\n");
    }

    @Test
    public void testPushReadOusideBounds() throws InterruptedException {
        assertEquals(decompileHex("96 02 00 00 41 26 07 00").trim(), 
                "trace(\"A&\\x07\");\n" +
                "stop();"
        );
    }
}
