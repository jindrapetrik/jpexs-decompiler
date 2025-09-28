/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
                Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
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
            Action.actionsToSource(new HashMap<>(), doa, doa.getActions(), "", writer, swf.getCharset());
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
    public void testJumpAfterFunctionFix() {
        String res = decompilePcode("ConstantPool \"F\", \"A\", \"f\", \"B\", \"C\"\n"
                + "DefineFunction \"f\", 0 {\n"
                + "Push \"F\"\n"
                + "Trace\n"
                + "Push 1, 1\n"
                + "Equals2\n"
                + "If loc005b\n"
                + "Push \"G\"\n"
                + "Trace\n"
                + "}\n"
                + "Push \"A\"\n"
                + "Trace\n"
                + "Push 0\n"
                + "Push \"f\"\n"
                + "CallFunction\n"
                + "Pop\n"
                + "Push \"B\"\n"
                + "Trace\n"
                + "Push 1\n"
                + "loc005b:Push 2\n"
                + "Equals2\n"
                + "If loc006e\n"
                + "Push \"C\"\n"
                + "Trace\n"
                + "loc006e:Stop");
        res = cleanPCode(res);
        assertEquals(res, "function f()\n"
                + "{\n"
                + "trace(\"F\");\n"
                + "if(1 != 1)\n"
                + "{\n"
                + "trace(\"G\");\n"
                + "}\n"
                + "}\n"
                + "trace(\"A\");\n"
                + "f();\n"
                + "trace(\"B\");\n"
                + "if(1 != 2)\n"
                + "{\n"
                + "trace(\"C\");\n"
                + "}\n"
                + "stop();");
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

    @Test
    public void testStopPartEndFunction() {
        String res = decompilePcode("DefineFunction \"test\", 1, \"param1\"  {\n"
                + "Push \"param1\"\n"
                + "GetVariable\n"
                + "StoreRegister 1\n"
                + "Pop\n"
                + "Push \"Start\"\n"
                + "Trace\n"
                + "Push register1, 1\n"
                + "Equals\n"
                + "Not\n"
                + "If loc007a\n"
                + "Push register1, 2\n"
                + "Equals\n"
                + "Not\n"
                + "If loc0075\n"
                + "Push register1, 3\n"
                + "Equals\n"
                + "Not\n"
                + "If loc006e\n"
                + "Push \"C\"\n"
                + "Trace\n"
                + "Jump loc00a5\n"
                + "loc006e:Push \"B\"\n"
                + "Trace\n"
                + "loc0075:Jump loc0081\n"
                + "loc007a:Push \"E\"\n"
                + "Trace\n"
                + "loc0081:Push register1, 4\n"
                + "Equals\n"
                + "Not\n"
                + "If loc009e\n"
                + "Push \"A\"\n"
                + "Trace\n"
                + "Jump loc00a5\n"
                + "loc009e:Push \"D\"\n"
                + "Trace\n"
                + "}\n"
                + "loc00a5:Push \"end\"\n"
                + "Trace\n");
        res = cleanPCode(res);
        assertEquals(res, "function test(param1)\n"
                + "{\n"
                + "var _loc1_ = param1;\n"
                + "trace(\"Start\");\n"
                + "if(_loc1_ == 1)\n"
                + "{\n"
                + "if(_loc1_ == 2)\n"
                + "{\n"
                + "if(_loc1_ == 3)\n"
                + "{\n"
                + "trace(\"C\");\n"
                + "return;\n"
                + "}\n"
                + "trace(\"B\");\n"
                + "}\n"
                + "}\n"
                + "else\n"
                + "{\n"
                + "trace(\"E\");\n"
                + "}\n"
                + "if(_loc1_ == 4)\n"
                + "{\n"
                + "trace(\"A\");\n"
                + "}\n"
                + "else\n"
                + "{\n"
                + "trace(\"D\");\n"
                + "}\n"
                + "}\n"
                + "trace(\"end\");");
    }

    @Test
    public void testReturnAsJumpAfterFunction() {
        String res = decompilePcode("DefineFunction \"test\", 0 {\n"
                + "Push register1\n"
                + "Push \"A\"\n"
                + "Equals2\n"
                + "Not\n"
                + "If loc002d\n"
                + "Push \"in A\"\n"
                + "Trace\n"
                + "Jump loc0053\n"
                + "loc002d:Push register1\n"
                + "Push \"B\"\n"
                + "Equals2\n"
                + "Not\n"
                + "If loc004e\n"
                + "Push \"in B\"\n"
                + "Trace\n"
                + "Jump loc0053\n"
                + "loc004e:Jump loc005f\n"
                + "loc0053:Jump loc0058\n"
                + "loc0058:Push \"C\"\n"
                + "Trace\n"
                + "}\n"
                + "loc005f:\n");
        res = cleanPCode(res);
        assertEquals(res, "function test()\n"
                + "{\n"
                + "if(_loc1_ == \"A\")\n"
                + "{\n"
                + "trace(\"in A\");\n"
                + "}\n"
                + "else if(_loc1_ == \"B\")\n"
                + "{\n"
                + "trace(\"in B\");\n"
                + "}\n"
                + "else\n"
                + "{\n"
                + "return;\n"
                + "}\n"
                + "trace(\"C\");\n"
                + "}");
    }

    @Test
    public void testBreakReturnAsJumpAfterFunction2() {
        String res = decompilePcode("ConstantPool \"a\", \"v\", \"b\", \"ret\"\n"
                + "DefineFunction \"f\", 0 {\n"
                + "Push \"a\"\n"
                + "Push 3\n"
                + "Push 2\n"
                + "Push 1\n"
                + "Push 3\n"
                + "InitArray\n"
                + "DefineLocal\n"
                + "Push \"a\"\n"
                + "GetVariable\n"
                + "Enumerate2\n"
                + "loc0046:StoreRegister 0\n"
                + "Push null\n"
                + "Equals2\n"
                + "If loc00cb\n"
                + "Push \"v\"\n"
                + "Push register0\n"
                + "SetVariable\n"
                + "Push \"v\"\n"
                + "GetVariable\n"
                + "Trace\n"
                + "Push \"b\"\n"
                + "Push 0\n"
                + "DefineLocal\n"
                + "loc0074:Push \"b\"\n"
                + "GetVariable\n"
                + "Push 10\n"
                + "Less2\n"
                + "Not\n"
                + "If loc00c6\n"
                + "Push \"b\"\n"
                + "GetVariable\n"
                + "Push 4\n"
                + "Equals2\n"
                + "Not\n"
                + "If loc00b4\n"
                + "Push \"ret\"\n"
                + "Trace\n"
                + "loc00a4:Push null\n"
                + "Equals2\n"
                + "Not\n"
                + "If loc00a4\n"
                + "Jump loc00cb\n"
                + "loc00b4:Push \"b\"\n"
                + "Push \"b\"\n"
                + "GetVariable\n"
                + "Increment\n"
                + "SetVariable\n"
                + "Jump loc0074\n"
                + "loc00c6:Jump loc0046\n"
                + "}\n"
                + "loc00cb:");
        res = cleanPCode(res);
        assertEquals(res, "function f()\n"
                + "{\n"
                + "var a = [1,2,3];\n"
                + "for(v in a)\n"
                + "{\n"
                + "trace(v);\n"
                + "var b = 0;\n"
                + "while(b < 10)\n"
                + "{\n"
                + "if(b == 4)\n"
                + "{\n"
                + "trace(\"ret\");\n"
                + "return;\n" //critical - no level2 break, but return
                + "}\n"
                + "b++;\n"
                + "}\n"
                + "}\n"
                + "}");
    }
}
