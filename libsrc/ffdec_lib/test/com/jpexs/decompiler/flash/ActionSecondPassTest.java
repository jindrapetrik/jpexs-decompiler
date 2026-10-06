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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.HashMap;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests global coordination of ActionScript 1/2 second passes.
 *
 * @author JPEXS
 */
public class ActionSecondPassTest {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
        Configuration.simplifyExpressions.set(false);
        Configuration.decompile.set(true);
    }

    @Test(timeOut = 5000)
    public void deeplyNestedWithIsProcessedInTwoGlobalPasses() throws Exception {
        final int depth = 16;
        StringBuilder source = new StringBuilder("var o = {};\nvar value = 1;\n");
        for (int i = 0; i < depth; i++) {
            source.append("with(o) {\n");
        }
        source.append("switch(value) {\n")
                .append("case 1:\n")
                .append("trace(\"one\");\n")
                .append("break;\n")
                .append("case 2:\n")
                .append("trace(\"two\");\n")
                .append("break;\n")
                .append("default:\n")
                .append("trace(\"other\");\n")
                .append("}\n");
        for (int i = 0; i < depth; i++) {
            source.append("}\n");
        }

        SWF swf = new SWF();
        DoActionTag tag = new DoActionTag(swf);
        ActionScript2Parser parser = new ActionScript2Parser(swf, tag);
        tag.setActions(parser.actionsFromString(source.toString(), Utf8Helper.charsetName));
        tag.setActionBytes(Action.actionsToBytes(tag.getActions(), true, swf.version));

        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, tag.getActions(), "", writer, Utf8Helper.charsetName);
        writer.finishHilights();
        String result = writer.toString();

        Assert.assertEquals(countOccurrences(result, "with(o)"), depth);
        Assert.assertTrue(result.contains("switch(value)"));
        Assert.assertTrue(result.contains("case 1:"));
    }

    private int countOccurrences(String value, String searched) {
        int count = 0;
        int position = 0;
        while ((position = value.indexOf(searched, position)) >= 0) {
            count++;
            position += searched.length();
        }
        return count;
    }
}
