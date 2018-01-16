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
import com.jpexs.decompiler.flash.abc.avm2.AVM2Runtime;
import com.jpexs.decompiler.flash.abc.avm2.AVM2RuntimeInfo;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author JPEXS
 */
public class ActionScript3ExecuteTest {

    private SWF swf;

    private ABC abc;

    @BeforeClass
    public void init() throws IOException, InterruptedException {
        //Main.initLogging(false);
        swf = new SWF(new BufferedInputStream(new FileInputStream("testdata/run_as3/run.swf")), false);
        /*try (InputStream is = new BufferedInputStream(new FileInputStream("testdata/run_as3/run.swf"))) {
         swf = new SWF(is, false);
         }*/
        DoABC2Tag tag = null;
        for (Tag t : swf.getTags()) {
            if (t instanceof DoABC2Tag) {
                tag = (DoABC2Tag) t;
                break;
            }
        }
        assertNotNull(tag);
        this.abc = tag.getABC();
        Configuration.autoDeobfuscate.set(false);
        Configuration.decompile.set(true);
        Configuration.registerNameFormat.set("_loc%d_");
        Configuration.showMethodBodyId.set(false);
    }

    //@Test
    public void testRun() throws IOException, InterruptedException {
        MethodBody body = abc.findBodyByClassAndName("Run", "run");
        Object result;
        try {
            result = body.getCode().execute(new HashMap<>(), abc.constants, new AVM2RuntimeInfo(AVM2Runtime.ADOBE_FLASH, 19, true));
            assertEquals(result, "Test");
        } catch (AVM2ExecutionException ex) {
            fail();
        }
    }

    //@Test
    public void testAddMethod() throws IOException, InterruptedException {
        int classId = abc.findClassByName("Run");
        MethodBody runBody = abc.findBodyByClassAndName("Run", "runInstance");
        runBody.max_stack = 10;

        AVM2Code ccode = new AVM2Code();
        List<AVM2Instruction> code = ccode.code;
        code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
        code.add(new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("", true)}));

        for (int testMethodId = 1; testMethodId < 10; testMethodId++) {
            AVM2Code ccode2 = new AVM2Code();
            List<AVM2Instruction> code2 = ccode2.code;
            code2.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
            code2.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
            code2.add(new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId(testMethodId + ""/* + "\r\n"*/, true)}));
            code2.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));

            boolean isStatic = testMethodId % 2 == 0;
            int multinameId = addMethod(classId, "test" + testMethodId, isStatic, ccode2);
            if (isStatic) {
                code.add(new AVM2Instruction(0, AVM2Instructions.FindPropertyStrict, new int[]{multinameId}));
            } else {
                code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
            }

            code.add(new AVM2Instruction(0, AVM2Instructions.CallProperty, new int[]{multinameId, 0}));
            code.add(new AVM2Instruction(0, AVM2Instructions.Add, null));
            code.add(new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("\r\n", true)}));
            code.add(new AVM2Instruction(0, AVM2Instructions.Add, null));
        }

        code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
        runBody.setCode(ccode);
        runBody.markOffsets();
    }

    private int addMethod(int classId, String name, boolean isStatic, AVM2Code code) {
        TraitMethodGetterSetter methodTrait = abc.addMethod(classId, name, isStatic);
        MethodInfo methodInfo = abc.method_info.get(methodTrait.method_info);
        MethodBody methodBody = abc.findBody(methodInfo);
        methodBody.max_stack = 10;
        methodBody.max_regs = 10;
        methodBody.init_scope_depth = 3;
        methodBody.max_scope_depth = 10;

        methodBody.setCode(code);
        methodBody.markOffsets();

        return methodTrait.name_index;
    }
}
