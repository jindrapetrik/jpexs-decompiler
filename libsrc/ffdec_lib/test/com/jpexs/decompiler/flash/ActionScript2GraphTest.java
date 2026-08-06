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
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * ActionScript 1/2 graph decompilation tests.
 *
 * @author JPEXS
 */
public class ActionScript2GraphTest {

    @BeforeClass
    public void init() {
        Configuration.autoDeobfuscate.set(false);
    }
    
    private String compileAndDecompile(String source) throws Exception {
        SWF swf = new SWF();
        DoActionTag tag = new DoActionTag(swf);
        ActionScript2Parser parser = new ActionScript2Parser(swf, tag);
        List<Action> compiledActions = parser.actionsFromString(source, Utf8Helper.charsetName);
        byte[] actionBytes = Action.actionsToBytes(compiledActions, true, swf.version);
        ActionList actions = ActionListReader.readActionListTimeout(tag, new ArrayList<>(),
                new SWFInputStream(swf, actionBytes), swf.version, 0, actionBytes.length, "", 1);
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToSource(new HashMap<>(), tag, actions, "", writer, swf.getCharset());
        writer.finishHilights();
        return writer.toString().trim().replace("\r\n", "\n");
    }

    @Test
    public void testStringSwitchCasesContinuingOuterLoop() throws Exception {
        String result = compileAndDecompile("function testLoop() {"
                + "var result = this.createValue();"
                + "while (true) {"
                + "switch (this.getValue()) {"
                + "case \"a\":"
                + "this.advance();"
                + "if (this.getValue().slice(0, 2) !== \"xy\") {"
                + "handleError();"
                + "}"
                + "result = [\"a\", result, this.advance().slice(2)];"
                + "continue;"
                + "case \"b\":"
                + "this.advance();"
                + "result = [\"a\", result, this.readValue()];"
                + "this.finish(\"b\");"
                + "continue;"
                + "case \"c\":"
                + "result = [this.advance(), result, this.readList()];"
                + "this.finish(\"c\");"
                + "continue;"
                + "}"
                + "break;"
                + "}"
                + "return result;"
                + "}");
        Assert.assertTrue(result.contains("function testLoop()"), result);
        Assert.assertTrue(result.contains("case \"a\":"), result);
        Assert.assertTrue(result.contains("case \"b\":"), result);
        Assert.assertTrue(result.contains("case \"c\":"), result);
        Assert.assertTrue(result.contains("this.readValue()"), result);
        Assert.assertTrue(result.contains("this.readList()"), result);
        Assert.assertEquals(result.split("continue;", -1).length - 1, 3, result);
        Assert.assertFalse(Pattern.compile("\\bloop\\d+\\s*:").matcher(result).find(), result);
        Assert.assertFalse(Pattern.compile("(?m)^\\s*addr[0-9a-f]+\\s*:").matcher(result).find(), result);
        Assert.assertFalse(Pattern.compile("\\b(?:break|continue)\\s+\\w+").matcher(result).find(), result);
        Assert.assertFalse(result.contains("§§goto"), result);
    }
}
