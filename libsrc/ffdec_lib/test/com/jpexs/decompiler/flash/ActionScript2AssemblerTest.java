/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript2AssemblerTest extends ActionScript2TestBase {

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(false);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as2/as2.swf")), false);
    }

    private String recompilePcode(String pcode) {
        try {
            List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            return writer.toString();
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }

        return null;
    }

    private String decompilePcode(String pcode) {
        try {
            List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);

            try {
                Action.actionsToSource(doa, doa.getActions(), "", writer);
            } catch (InterruptedException ex) {
                fail();
            }

            return writer.toString();
        } catch (IOException | ActionParseException ex) {
            fail();
        }

        return null;
    }

    @Test
    public void testModifiedConstantPools() {
        String actionsString = "ConstantPool \"ok\"\n"
                + "Jump loc001f\n"
                + "loc000d:Push \"ok\" false\n"
                + "SetVariable\n"
                + "Jump loc002f\n"
                + "loc001f:ConstantPool \"wrong\"\n"
                + "Jump loc000d\n"
                + "loc002f:";
        try {
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false);

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(doa, doa.getActions(), "", writer);
            String actualResult = writer.toString();
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            String decompiled = writer.toString();

            assertEquals(actualResult.trim(), "ok = false;");
            assertTrue(decompiled.contains("Push \"ok\" false") || decompiled.contains("Push constant0 false"));
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }
    }

    @Test
    public void testNegativeFloatValue() {
        String actionsString = "Push -0.25";
        String decompiled = recompilePcode(actionsString);
        assertTrue(decompiled.contains("Push -0.25"));
    }

    @Test
    public void testDeclaredRegister() {
        String res = decompilePcode("ConstantPool\n"
                + "DefineFunction2 \"test\" 1 3 false true true false true false true false false 2 \"p\"  {\n"
                + "Push register2 \"type\"\n"
                + "GetMember\n"
                + "StoreRegister 0\n"
                + "Push 1\n"
                + "StrictEquals\n"
                + "If loc003a\n"
                + "Jump loc004a\n"
                + "loc003a:Push \"Hello\"\n"
                + "Trace\n"
                + "Jump loc004a\n"
                + "}\n"
                + "loc004a:");
        res = cleanPCode(res);
        assertEquals(res, "function test(p)\n"
                + "{\n"
                + "var _loc0_ = null;\n"
                + "if((_loc0_ = p.type) === 1)\n"
                + "{\n"
                + "trace(\"Hello\");\n"
                + "}\n"
                + "}");
    }
}
