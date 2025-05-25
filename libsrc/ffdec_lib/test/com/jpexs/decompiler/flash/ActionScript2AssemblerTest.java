/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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
            List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            writer.finishHilights();
            return writer.toString();
        } catch (IOException | ActionParseException | InterruptedException ex) {
            fail();
        }

        return null;
    }

    private String decompilePcode(String pcode) {
        try {
            List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);

            try {
                Action.actionsToSource(new HashMap<>(),doa, doa.getActions(), "", writer, swf.getCharset());
            } catch (InterruptedException ex) {
                fail();
            }
            writer.finishHilights();
            return writer.toString();
        } catch (IOException | ActionParseException ex) {
            fail();
        }

        return null;
    }

    private String decompileClassPcode(String pcode) {
        try {
            List<Action> actions = ASMParser.parse(0, true, pcode, swf.version, false, swf.getCharset());

            DoInitActionTag doi = getFirstInitActionTag();
            doi.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);

            try {
                Action.actionsToSource(new HashMap<>(), doi, doi.getActions(), "", writer, swf.getCharset());
            } catch (InterruptedException ex) {
                fail();
            }
            writer.finishHilights();
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
            List<Action> actions = ASMParser.parse(0, true, actionsString, swf.version, false, swf.getCharset());

            DoActionTag doa = getFirstActionTag();
            doa.setActionBytes(Action.actionsToBytes(actions, true, swf.version));
            HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
            Action.actionsToSource(new HashMap<>(),doa, doa.getActions(), "", writer, swf.getCharset());
            writer.finishHilights();
            String actualResult = writer.toString();
            writer = new HighlightedTextWriter(new CodeFormatting(), false);
            doa.getASMSource(ScriptExportMode.PCODE, writer, null);
            writer.finishHilights();
            String decompiled = writer.toString();

            assertEquals(actualResult.trim(), "ok = false;");
            assertTrue(decompiled.contains("Push \"ok\", false") || decompiled.contains("Push constant0, false"));
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
                + "var _loc0_;\n"
                + "if((_loc0_ = p.type) === 1)\n"
                + "{\n"
                + "trace(\"Hello\");\n"
                + "}\n"
                + "}");
    }

    @Test
    public void testDefineFuncRegCleaner() {
        String res = decompilePcode("ConstantPool \"Math\" \"randi\" \"random\" \"b\" \"a\" \"floor\" \"randf\" \"randa\" \"arguments\" \"rande\" \"sign\" \"abs\"\n"
                + "Push constant0\n"
                + "GetVariable\n"
                + "Push constant10\n"
                + "definefunction \"\" 1 \"a\" { \n"
                + "push register1 constant4\n"
                + "getvariable\n"
                + "storeregister 1\n"
                + "pop\n"
                + "push register1 0.0\n"
                + "Equals2\n"
                + "If locC\n"
                + "push register1 1 constant0\n"
                + "getvariable\n"
                + "push constant11\n"
                + "callmethod\n"
                + "push register1\n"
                + "divide\n"
                + "jump locB\n"
                + "locC:\n"
                + "push 0.0\n"
                + "locB: jump locA\n"
                + "push undefined\n"
                + "locA: storeregister 0\n"
                + "pop\n"
                + "storeregister 1\n"
                + "pop\n"
                + "push register0\n"
                + "return\n"
                + "}\n"
                + "loc00a5:SetMember");
        res = cleanPCode(res);
        assertEquals(res, "Math.sign = function(a)\n"
                + "{\n"
                + "var _loc1_ = a;\n"
                + "if(_loc1_ != 0)\n"
                + "{\n"
                + "return Math.abs(_loc1_) / _loc1_;\n"
                + "}\n"
                + "return 0;\n"
                + "};");
    }

    @Test
    public void testSwitchVariable() {
        String res = decompilePcode("ConstantPool \"t\"\n"
                + "Push \"t\"\n"
                + "GetVariable\n"
                + "Push 0.0\n"
                + "StrictEquals\n"
                + "If loc0038\n"
                + "Push \"t\"\n"
                + "GetVariable\n"
                + "Push 1\n"
                + "StrictEquals\n"
                + "If loc0041\n"
                + "Jump loc004a\n"
                + "loc0038:Push 0\n"
                + "Return\n"
                + "loc0041:Push 1\n"
                + "Return\n"
                + "loc004a:Push 3\n"
                + "Return");
        res = cleanPCode(res);
        assertEquals(res, "switch(t)\n"
                + "{\n"
                + "case 0:\n"
                + "return 0;\n"
                + "case 1:\n"
                + "return 1;\n"
                + "default:\n"
                + "return 3;\n"
                + "}");
    }

    @Test
    public void testClassSpecial() {
        String res = decompileClassPcode("ConstantPool\n"
                + "Push \"_global\"\n"
                + "GetVariable\n"
                + "Push \"Guide\"\n"
                + "GetMember\n"
                + "Not\n"
                + "Not\n"
                + "If loc00d9\n"
                + "Push \"_global\"\n"
                + "GetVariable\n"
                + "Push \"Guide\"\n"
                + "DefineFunction \"\" 0  {\n"
                + "Push \"hello\"\n"
                + "Trace\n"
                + "}\n"
                + "StoreRegister 1\n"
                + "SetMember\n"
                + "Push \"_global\"\n"
                + "GetVariable\n"
                + "Push \"Guide\"\n"
                + "GetMember\n"
                + "Push \"prototype\" 0.0 \"MovieClip\"\n"
                + "NewObject\n"
                + "StoreRegister 2\n"
                + "SetMember\n"
                + "Push 1 null \"_global\"\n"
                + "GetVariable\n"
                + "Push \"Guide\"\n"
                + "GetMember\n"
                + "Push \"prototype\"\n"
                + "GetMember\n"
                + "Push 3 \"ASSetPropFlags\"\n"
                + "CallFunction\n"
                + "loc00d9:Pop");
        res = cleanPCode(res);
        assertEquals(res, "class Guide\n"
                + "{\n"
                + "function Guide()\n"
                + "{\n"
                + "trace(\"hello\");\n"
                + "}\n"
                + "}");
    }

    @Test
    public void testNonStandardForIn() {
        String res = decompileClassPcode("ConstantPool\n"
                + "Push \"x\" 0 \"Object\"\n"
                + "NewObject\n"
                + "StoreRegister 1\n"
                + "SetVariable\n"
                + "Push \"_global\"\n"
                + "GetVariable\n"
                + "Push \"x\"\n"
                + "GetMember\n"
                + "StoreRegister 2\n"
                + "Enumerate2\n"
                + "loc003d:StoreRegister 0\n"
                + "Push null\n"
                + "Equals2\n"
                + "If loc0066\n"
                + "Push register1 register0 register2 register0\n"
                + "GetMember\n"
                + "SetMember\n"
                + "Jump loc003d\n"
                + "loc0066:Pop\n"
                + "Push \"after\"\n"
                + "Trace");
        res = cleanPCode(res);
        assertEquals(res, "var _loc1_;\n"
                + "x = _loc1_ = new Object();\n"
                + "var _loc2_ = _global.x;\n"
                + "for(var _loc0_ in _loc2_)\n"
                + "{\n"
                + "_loc1_[_loc0_] = _loc2_[_loc0_];\n"
                + "}\n"
                + "trace(\"after\");");
    }
}
