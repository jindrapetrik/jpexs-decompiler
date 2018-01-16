/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConvertException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ActionScript3AssemblerTest extends ActionScriptTestBase {

    private SWF swf;

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        Configuration.autoDeobfuscate.set(true);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/as3/as3.swf")), false);
    }

    private int getBaseAddr() {
        return 2; //getlocal_0 + pushscope
    }

    private ABC getABC() {
        return new ABC(new ABCContainerTag() {

            @Override
            public ABC getABC() {
                return null;
            }

            @Override
            public SWF getSwf() {
                return swf;
            }

            @Override
            public int compareTo(ABCContainerTag o) {
                return 0;
            }
        });
    }

    private MethodBody compilePCode(String str) throws IOException, AVM2ParseException, InterruptedException {
        str = "code\r\n"
                + "getlocal_0\r\n"
                + "pushscope\r\n"
                + str
                + "returnvoid\r\n";

        MethodBody b = new MethodBody(getABC(), new Traits(), new byte[0], new ABCException[0]);
        AVM2Code code = ASM3Parser.parse(getABC(), new StringReader(str), null, b, new MethodInfo());
        b.setCode(code);
        return b;
    }

    /*private String codeToStr(AVM2Code code) {
     HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
     code.toASMSource(getABC().constants, null, new MethodInfo(), new MethodBody(), ScriptExportMode.PCODE, writer);
     String ret = writer.toString();
     return ret.substring(ret.lastIndexOf("code\r\n") + 6);
     }*/
    @Test
    public void removeInstruction() throws Exception {
        MethodBody b = compilePCode("pushbyte 1\r\n"
                + "setlocal_1\r\n" //remove this
                + "jump label1\r\n"
                + "pushtrue\r\n"
                + "pop\r\n"
                + "label1:pushfalse\r\n");
        b.getCode().removeInstruction(getBaseAddr() + 1, b);
    }

    @Test
    public void removeInstruction2() throws Exception {
        MethodBody b = compilePCode("pushbyte 1\r\n"
                + "setlocal_1\r\n"
                + "jump label1\r\n"
                + "pushtrue\r\n"
                + "pop\r\n" //remove this
                + "label1:pushfalse\r\n");
        b.getCode().removeInstruction(getBaseAddr() + 4, b);
    }

    @Test
    public void replaceInstruction() throws Exception {
        MethodBody b = compilePCode("pushbyte 1\r\n"
                + "setlocal_1\r\n"
                + "jump label1\r\n" //remove this
                + "jump label1\r\n"
                + "pushtrue\r\n"
                + "pop\r\n"
                + "label1:pushfalse\r\n");
        b.getCode().replaceInstruction(getBaseAddr() + 2, new AVM2Instruction(0, DeobfuscatePopIns.getInstance(), new int[]{}), b);
    }

    @Test
    public void replaceInstruction2() throws Exception {
        MethodBody b = compilePCode("pushbyte 1\r\n"
                + "setlocal_1\r\n"
                + "jump label1\r\n"
                + "pushtrue\r\n"
                + "jump label1\r\n" //remove this
                + "pop\r\n"
                + "label1:pushfalse\r\n");
        b.getCode().replaceInstruction(getBaseAddr() + 4, new AVM2Instruction(0, DeobfuscatePopIns.getInstance(), new int[]{}), b);
    }

    @Test
    public void testAddressToPos() throws Exception {
        String str = "pushbyte 1\r\n"
                + "pushbyte 1\r\n"
                + "pushbyte 1\r\n";

        MethodBody b = new MethodBody(getABC(), new Traits(), new byte[0], new ABCException[0]);
        AVM2Code code = ASM3Parser.parse(getABC(), new StringReader(str), null, b, new MethodInfo());

        long to = code.getEndOffset();
        Map<Long, Integer> expected = new HashMap<>();
        Map<Long, Integer> expectedNearest = new HashMap<>();
        expected.put(-2L, -1);
        expectedNearest.put(-2L, 0);
        expected.put(-1L, -1);
        expectedNearest.put(-1L, 0);
        for (int i = 0; i < code.code.size(); i++) {
            AVM2Instruction ins = code.code.get(i);
            int length = ins.getBytesLength();
            expected.put(ins.getAddress(), i);
            expectedNearest.put(ins.getAddress(), i);
            Assert.assertEquals(code.pos2adr(i), ins.getAddress());
            for (int j = 1; j < length; j++) {
                expected.put(ins.getAddress() + j, -1);
                expectedNearest.put(ins.getAddress() + j, i + 1);
            }
        }

        Assert.assertEquals(code.pos2adr(code.code.size()), code.getEndOffset());
        expected.put(to, code.code.size());
        expectedNearest.put(to, code.code.size());
        expected.put(to + 1, -1);
        expectedNearest.put(to + 1, -1);
        expected.put(to + 2, -1);
        expectedNearest.put(to + 2, -1);

        for (Map.Entry<Long, Integer> e : expected.entrySet()) {
            int pos;
            try {
                pos = code.adr2pos(e.getKey());
            } catch (ConvertException ex) {
                pos = -1;
            }

            Assert.assertEquals((long) pos, (int) e.getValue());
        }

        for (Map.Entry<Long, Integer> e : expectedNearest.entrySet()) {
            int pos;
            try {
                pos = code.adr2pos(e.getKey(), true);
            } catch (ConvertException ex) {
                pos = -1;
            }

            Assert.assertEquals((long) pos, (int) e.getValue());
        }
    }

    @Test
    public void testInstructionStackSizes() throws Exception {
        ABC abc = new ABC(null);
        Multiname multiname = Multiname.createRTQNameL(false);
        abc.constants.addMultiname(multiname);
        AVM2Instruction ins = new AVM2Instruction(0, null, new int[]{1, 20});
        for (InstructionDefinition def : AVM2Code.instructionSet) {
            if (def == null) {
                continue;
            }

            int popCount = 0;
            int pushCount = 0;
            int delta = 0;
            boolean popException = false;
            boolean pushException = false;
            boolean deltaException = false;
            try {
                popCount = def.getStackPopCount(ins, abc);
            } catch (UnsupportedOperationException ex) {
                popException = true;
            }

            try {
                pushCount = def.getStackPushCount(ins, abc);
            } catch (UnsupportedOperationException ex) {
                pushException = true;
            }

            try {
                delta = def.getStackDelta(ins, abc);
            } catch (UnsupportedOperationException ex) {
                deltaException = true;
            }

            if (popException && pushException && deltaException) {
                continue;
            }

            if (popException || pushException || deltaException) {
                Assert.fail(def.instructionName + " exception mismatch.");
            }

            if (pushCount - popCount != delta) {
                Assert.fail(def.instructionName + " stack mismatch.");
            }
        }
    }
}
