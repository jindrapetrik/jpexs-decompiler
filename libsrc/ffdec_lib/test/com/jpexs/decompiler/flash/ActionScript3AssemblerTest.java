/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.AVM2DeobfuscatorJumps;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABCDefineTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
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
        Configuration.deobfuscationMode.set(1);
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

        MethodBody b = new MethodBody();
        AVM2Code code = ASM3Parser.parse(new StringReader(str), getABC().constants, null, b, new MethodInfo());
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
    public void replaceIstruction() throws Exception {
        MethodBody b = compilePCode("pushbyte 1\r\n"
                + "setlocal_1\r\n"
                + "jump label1\r\n" //remove this                
                + "jump label1\r\n"
                + "pushtrue\r\n"
                + "pop\r\n"
                + "label1:pushfalse\r\n");
        b.getCode().replaceInstruction(getBaseAddr() + 2, new AVM2Instruction(0, new DeobfuscatePopIns(), new int[]{}), b);
    }

    @Test
    public void replaceIstruction2() throws Exception {
        MethodBody b = compilePCode("pushbyte 1\r\n"
                + "setlocal_1\r\n"
                + "jump label1\r\n"
                + "pushtrue\r\n"
                + "jump label1\r\n" //remove this               
                + "pop\r\n"
                + "label1:pushfalse\r\n");
        b.getCode().replaceInstruction(getBaseAddr() + 4, new AVM2Instruction(0, new DeobfuscatePopIns(), new int[]{}), b);
    }
}
