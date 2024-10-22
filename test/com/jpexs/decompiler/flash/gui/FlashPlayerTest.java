/*
 *  Copyright (C) 2010-2024 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Runtime;
import com.jpexs.decompiler.flash.abc.avm2.AVM2RuntimeInfo;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.Stage;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionAnd;
import com.jpexs.decompiler.flash.action.swf4.ActionAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.action.swf4.ActionDivide;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf4.ActionMBAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionMBCharToAscii;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringExtract;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionMultiply;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionOr;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionStringAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionStringExtract;
import com.jpexs.decompiler.flash.action.swf4.ActionStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionStringLess;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf4.ActionToInteger;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionBitAnd;
import com.jpexs.decompiler.flash.action.swf5.ActionBitLShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitOr;
import com.jpexs.decompiler.flash.action.swf5.ActionBitRShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitURShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitXor;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionStackSwap;
import com.jpexs.decompiler.flash.action.swf5.ActionToNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionToString;
import com.jpexs.decompiler.flash.action.swf5.ActionTypeOf;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf6.ActionStringGreater;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerTest {

    private final Random random = new Random();

    private final AVM2RuntimeInfo adobeRuntime = new AVM2RuntimeInfo(AVM2Runtime.ADOBE_FLASH, 19, true);

    //@Test
    public void testAs3Files() throws IOException, InterruptedException {
        List<String> files = new ArrayList<>();
        File dir = new File("..\\swf");
        if (dir.exists()) {
            File[] fs = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".swf") && !name.toLowerCase().endsWith(".recompiled.swf");
                }
            });
            for (File f : fs) {
                files.add(dir.getAbsolutePath() + File.separator + f.getName());
            }
        }

        for (String file : files) {
            SWF swf = new SWF(new BufferedInputStream(new FileInputStream(file)), true);
            List<ABCContainerTag> abcs = swf.getAbcList();
            for (ABCContainerTag abcContainer : abcs) {
                ABC abc = abcContainer.getABC();

                for (MethodBody body : abc.bodies) {
                    if (body.method_info != 55) {
                        continue;
                    }

                    AVM2Code code = body.getCode();

                    AdobeFlashExecutor adobeExecutor = new AdobeFlashExecutor();

                    adobeExecutor.loadTestSwf();
                    ABC testAbc = adobeExecutor.as3TestSwfAbcTag.getABC();
                    testAbc.constants.ensureStringCapacity(1000000);
                    testAbc.constants.ensureMultinameCapacity(1000000);

                    List<AS3ExecuteTask> tasks = new ArrayList<>();
                    {
                        AS3ExecuteTask task = new AS3ExecuteTask();
                        task.description = swf.getFileTitle() + " " + body.method_info;
                        while (code.code.size() > 9) {
                            code.code.remove(code.code.size() - 1);
                        }
                        task.code = code;
                        tasks.add(task);
                    }

                    adobeExecutor.executeAvm2(tasks);

                    for (AS3ExecuteTask task : tasks) {

                        String ffdecExecuteResult;
                        try {
                            Object res = task.code.execute(new HashMap<>(), testAbc.constants, adobeRuntime);
                            ffdecExecuteResult = "Result:" + EcmaScript.toString(res) + " Type:" + EcmaScript.typeString(res);
                        } catch (AVM2ExecutionException ex) {
                            ffdecExecuteResult = "Error:" + ex.getMessage();
                        }

                        task.ffdecResult = ffdecExecuteResult;
                    }

                    for (AS3ExecuteTask task : tasks) {
                        System.out.println("Flash result (" + task.description + "): " + task.flashResult);
                        System.out.println("FFDec execute result: " + task.ffdecResult);
                        if (!task.ffdecResult.equals(task.flashResult)) {
                            System.out.println(code.toASMSource(testAbc, testAbc.constants));
                        }

                        assertEquals(task.ffdecResult, task.flashResult);
                    }
                }
            }
        }
    }

    //@Test
    public void testAs3Pushes() throws IOException, InterruptedException {
        AdobeFlashExecutor adobeExecutor = new AdobeFlashExecutor();

        adobeExecutor.loadTestSwf();
        ABC abc = adobeExecutor.as3TestSwfAbcTag.getABC();
        abc.constants.ensureStringCapacity(1000000);
        abc.constants.ensureMultinameCapacity(1000000);
        AVM2Instruction[] pushes = getAs3Pushes(abc);

        List<AS3ExecuteTask> tasks = new ArrayList<>();
        for (int p1 = 0; p1 < pushes.length; p1++) {
            AVM2Code ccode = new AVM2Code();
            List<AVM2Instruction> code = ccode.code;
            code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
            code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
            code.add(pushes[p1].clone());
            code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
            ccode.markOffsets();

            AS3ExecuteTask task = new AS3ExecuteTask();
            task.description = p1 + ", " + pushes[p1].definition.instructionName;
            task.code = ccode;
            tasks.add(task);
        }

        adobeExecutor.executeAvm2(tasks);

        for (AS3ExecuteTask task : tasks) {
            String ffdecExecuteResult;
            try {
                Object res = task.code.execute(new HashMap<>(), abc.constants, adobeRuntime);
                ffdecExecuteResult = "Result:" + EcmaScript.toString(res) + " Type:" + EcmaScript.typeString(res);
            } catch (AVM2ExecutionException ex) {
                ffdecExecuteResult = "Error:" + ex.getMessage();
            }

            task.ffdecResult = ffdecExecuteResult;
        }

        for (AS3ExecuteTask task : tasks) {
            System.out.println("Flash result (" + task.description + "): " + task.flashResult);
            System.out.println("FFDec execute result: " + task.ffdecResult);
            assertEquals(task.ffdecResult, task.flashResult);
        }
    }

    //@Test
    public void testAs3() throws IOException, InterruptedException {
        AdobeFlashExecutor adobeExecutor = new AdobeFlashExecutor();

        for (int i = 0; i < 256; i++) {
            System.out.println("Instruction code: " + Integer.toHexString(i) + " (" + i + ")");
            List<AS3ExecuteTask> tasks = new ArrayList<>();
            adobeExecutor.loadTestSwf();
            ABC abc = adobeExecutor.as3TestSwfAbcTag.getABC();
            AVM2Instruction[] pushes = getAs3Pushes(abc);
            for (int p1 = 0; p1 < pushes.length; p1++) {
                for (int p2 = 0; p2 < pushes.length; p2++) {
                    // todo: the following instructions are not implemented
                    if (i == AVM2Instructions.GetSuper
                            || i == AVM2Instructions.SetSuper
                            || i == AVM2Instructions.DXNS
                            || i == AVM2Instructions.DXNSLate
                            || i == AVM2Instructions.Kill
                            || i == AVM2Instructions.LookupSwitch
                            || i == AVM2Instructions.PushWith
                            || i == AVM2Instructions.NextName
                            || i == AVM2Instructions.HasNext
                            || i == AVM2Instructions.NextValue
                            || i == AVM2Instructions.PushScope
                            || i == AVM2Instructions.PushNamespace
                            || i == AVM2Instructions.HasNext2
                            || i == AVM2Instructions.NewFunction
                            || i == AVM2Instructions.Call
                            || i == AVM2Instructions.Construct
                            || i == AVM2Instructions.CallMethod
                            || i == AVM2Instructions.CallStatic
                            || i == AVM2Instructions.CallSuper
                            || i == AVM2Instructions.CallProperty
                            || i == AVM2Instructions.ConstructSuper
                            || i == AVM2Instructions.ConstructProp
                            || i == AVM2Instructions.CallPropLex
                            || i == AVM2Instructions.CallSuperVoid
                            || i == AVM2Instructions.CallPropVoid
                            || i == AVM2Instructions.ApplyType
                            || i == AVM2Instructions.NewObject
                            || i == AVM2Instructions.NewArray
                            || i == AVM2Instructions.NewActivation
                            || i == AVM2Instructions.NewClass
                            || i == AVM2Instructions.GetDescendants
                            || i == AVM2Instructions.NewCatch
                            || i == AVM2Instructions.FindPropGlobal
                            || i == AVM2Instructions.FindPropertyStrict
                            || i == AVM2Instructions.FindProperty
                            || i == AVM2Instructions.FindDef
                            || i == AVM2Instructions.GetLex
                            || i == AVM2Instructions.SetProperty
                            || i == AVM2Instructions.GetGlobalScope
                            || i == AVM2Instructions.GetScopeObject
                            || i == AVM2Instructions.GetProperty
                            || i == AVM2Instructions.GetOuterScope
                            || i == AVM2Instructions.InitProperty
                            || i == AVM2Instructions.DeleteProperty
                            || i == AVM2Instructions.GetSlot
                            || i == AVM2Instructions.SetSlot
                            || i == AVM2Instructions.GetGlobalSlot
                            || i == AVM2Instructions.SetGlobalSlot
                            || i == AVM2Instructions.CheckFilter
                            || i == AVM2Instructions.AsType // todo: fix
                            || i == AVM2Instructions.AsTypeLate
                            || i == AVM2Instructions.InstanceOf
                            || i == AVM2Instructions.IsType
                            || i == AVM2Instructions.IsTypeLate
                            || i == AVM2Instructions.In) {
                        continue;
                    }

                    AVM2Code ccode = new AVM2Code();
                    List<AVM2Instruction> code = ccode.code;
                    code.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.PushScope, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{10}));
                    code.add(new AVM2Instruction(0, AVM2Instructions.SetLocal0, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{11}));
                    code.add(new AVM2Instruction(0, AVM2Instructions.SetLocal1, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{0}));
                    code.add(pushes[p1].clone());
                    code.add(pushes[p2].clone());

                    InstructionDefinition ins = AVM2Code.instructionSet[i];
                    int[] params = null;
                    boolean ifType = false;
                    if (ins.operands.length > 0) {
                        params = new int[ins.operands.length];
                        if (!(ins instanceof IfTypeIns)) {
                            for (int param = 0; param < params.length; param++) {
                                params[param] = 1;
                            }
                        }

                        ifType = ins instanceof IfTypeIns;
                        if (ifType) {
                            params[0] = 3;
                        }
                    }

                    code.add(new AVM2Instruction(0, ins, params));
                    if (ifType) {
                        code.add(new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{1}));
                        code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
                    }

                    code.add(new AVM2Instruction(0, AVM2Instructions.Dup, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.TypeOf, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.Add, null));
                    code.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
                    ccode.markOffsets();

                    AS3ExecuteTask task = new AS3ExecuteTask();
                    task.description = "Instruction code: " + Integer.toHexString(i) + " (" + i + ") " + AVM2Code.instructionSet[i].instructionName + ", "
                            + "p1: " + p1 + ", " + pushes[p1].definition.instructionName + ", "
                            + "p2: " + p2 + ", " + pushes[p2].definition.instructionName;
                    task.code = ccode;

                    String ffdecExecuteResult;
                    try {
                        Object res = ccode.execute(new HashMap<>(), abc.constants, adobeRuntime);
                        ffdecExecuteResult = "Result:" + EcmaScript.toString(res) + " Type:" + EcmaScript.typeString(res);
                    } catch (AVM2ExecutionException ex) {
                        ffdecExecuteResult = "Error:" + ex.getMessage();
                    }

                    task.ffdecResult = ffdecExecuteResult;
                    tasks.add(task);
                }
            }

            adobeExecutor.executeAvm2(tasks);

            StringBuilder expeced = new StringBuilder();
            StringBuilder current = new StringBuilder();
            for (AS3ExecuteTask task : tasks) {
                if (!task.flashResult.equals(task.ffdecResult)) {
                    System.out.println("Flash result (" + task.description + "): " + task.flashResult);
                    System.out.println("FFDec execute result: " + task.ffdecResult);
                    expeced.append(task.flashResult).append(Helper.newLine);
                    current.append(task.ffdecResult).append(Helper.newLine);
                }

                /*if (!task.ffdecResult.equals(task.flashResult)) {
                 String ffdecExecuteResult;
                 try {
                 Object res = task.code.execute(new HashMap<>(), abc.constants, adobeRuntime);
                 ffdecExecuteResult = "Result:" + EcmaScript.toString(res) + " Type:" + EcmaScript.typeString(res);
                 } catch (AVM2ExecutionException ex) {
                 ffdecExecuteResult = "Error:" + ex.getMessage();
                 }
                 }*/
                assertEquals(task.ffdecResult, task.flashResult);
            }

            //Helper.writeFile("expected\\" + i + ".txt", Utf8Helper.getBytes(expeced.toString()));
            //Helper.writeFile("current\\" + i + ".txt", Utf8Helper.getBytes(current.toString()));
        }
    }

    private AVM2Instruction[] getAs3Pushes(ABC abc) {
        AVM2Instruction[] pushes = new AVM2Instruction[]{
            new AVM2Instruction(0, AVM2Instructions.PushUndefined, null), // 0
            new AVM2Instruction(0, AVM2Instructions.PushNull, null), // 1
            new AVM2Instruction(0, AVM2Instructions.PushTrue, null), // 2
            new AVM2Instruction(0, AVM2Instructions.PushFalse, null), // 3
            new AVM2Instruction(0, AVM2Instructions.PushNan, null), // 4
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{0}), // 5
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("", true)}), // 6
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("-2147483649", true)}), // 7
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("-2147483648", true)}), // 8
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("-2147483647", true)}), // 9
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("-1", true)}), // 10
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("0", true)}), // 11
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("1", true)}), // 12
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("2147483647", true)}), // 13
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("2147483648", true)}), // 14
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("4294967295", true)}), // 15
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("4294967296", true)}), // 16
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("1test", true)}), // 17
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("test", true)}), // 18
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("test2", true)}), // 18
            new AVM2Instruction(0, AVM2Instructions.PushString, new int[]{abc.constants.getStringId("test3", true)}), // 18
            new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{0}), // 19
            new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{abc.constants.getDoubleId(-1, true)}), // 20
            new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{abc.constants.getDoubleId(-0.5, true)}), // 21
            new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{abc.constants.getDoubleId(0, true)}), // 22
            new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{abc.constants.getDoubleId(0.5, true)}), // 23
            new AVM2Instruction(0, AVM2Instructions.PushDouble, new int[]{abc.constants.getDoubleId(1, true)}), // 24
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-2147483648}), // 25
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-2147483647}), // 26
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-1073741824}), // 27
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-1073741823}), // 28
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-536870912}), // 29
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-536870911}), // 30
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-268435456}), // 31
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-134217728}), // 32
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-134217727}), // 33
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-67108864}), // 34
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-67108863}), // 35
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-33554432}), // 36
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-33554431}), // 37
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-16777216}), // 38
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-16777215}), // 39
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-8388608}), // 40
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-8388607}), // 41
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-4194304}), // 42
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-4194303}), // 43
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-2097152}), // 44
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-2097151}), // 45
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-1048576}), // 46
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-1048575}), // 47
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-524288}), // 48
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-524287}), // 49
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-262144}), // 50
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-262143}), // 51
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-131072}), // 52
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-131071}), // 53
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-65536}), // 54
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-65535}), // 55
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-32768}), // 56
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-32767}), // 57
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{-1}), // 58
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{0}), // 59
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{1}), // 60
            /*new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{127}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{128}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{255}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{256}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{511}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{512}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{1023}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{1024}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{2047}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{2048}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{4095}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{4096}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{8191}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{8192}), // 62
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{16383}), // 61
             new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{16384}), // 62*/
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{32767}), // 61
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{32768}), // 62
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{268435455}), // 63
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{268435456}), // 64
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{536870911}), // 65
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{536870912}), // 66
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{1073741823}), // 67
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{1073741824}), // 68
            new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{2147483647}), // 69
            new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{0}), // 70
            //new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(-2147483649L, true)}), // 71
            new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(-2147483648, true)}), // 72
            new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(-1, true)}), // 73
            new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(0, true)}), // 74
            new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(1, true)}), // 75
            new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(2147483647, true)}), // 76
            //new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{abc.constants.getIntId(2147483648L, true)}), // 77
            //new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getIntId(4294967295L, true)}), // 78
            //new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getIntId(4294967296L, true)}), // 79
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{0}), // 80
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(-2147483649L, true)}), // 81
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(-2147483648, true)}), // 82
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(-1, true)}), // 83
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(0, true)}), // 84
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(1, true)}), // 85
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(2147483647, true)}), // 86
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(2147483648L, true)}), // 87
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(4294967295L, true)}), // 88
            new AVM2Instruction(0, AVM2Instructions.PushUInt, new int[]{abc.constants.getUIntId(4294967296L, true)}), // 89
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-2147483648}), // 90
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-256}), // 91
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-255}), // 92
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-128}), // 93
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-127}), // 94
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-2}), // 95
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{-1}), // 96
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{0}), // 97
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{1}), // 98
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{2}), // 99
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{127}), // 100
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{128}), // 101
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{255}), // 102
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{256}), // 103
            new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{2147483647}),}; // 104
        return pushes;
    }

    //@Test
    public void testAs2() throws InterruptedException {
        AdobeFlashExecutor adobeExecutor = new AdobeFlashExecutor();

        List<AS2ExecuteTask> tasks = new ArrayList<>();
        Object[] pushes = getAs2Pushes();
        for (int i = 0; i < 13 + 23 + 2; i++) {
            for (int p1 = 0; p1 < 15; p1++) {
                int p2Count = 1;
                if (i >= 13) {
                    p2Count = pushes.length;
                }

                for (int p2 = 0; p2 < p2Count; p2++) {

                    List<Action> newActions = new ArrayList<>();
                    Action opAction = getOpAction(i);

                    if (i >= 13 + 23) {
                        newActions.add(new ActionPush("mystring_\u00E1rv\u00EDzt\u0171r\u0151_t\u00FCk\u00F6rf\u00FAr\u00F3g\u00E9p", Utf8Helper.charsetName));
                    }

                    Object p1o = pushes[p1];
                    Object p2o = null;
                    if (i >= 13) {
                        p2o = pushes[p2];
                        newActions.add(new ActionPush(p2o, Utf8Helper.charsetName));
                    }

                    newActions.add(new ActionPush(p1o, Utf8Helper.charsetName));

                    newActions.add(opAction);
                    newActions.add(new ActionPushDuplicate(Utf8Helper.charsetName));
                    newActions.add(new ActionTypeOf());
                    newActions.add(new ActionStackSwap(Utf8Helper.charsetName));
                    newActions.add(new ActionStringAdd());

                    AS2ExecuteTask task = new AS2ExecuteTask();
                    String desc = i + " " + opAction.toString() + " p1:"
                            + (p1o instanceof String ? "'" + p1o + "'" : p1o) + " p2:"
                            + (p2o instanceof String ? "'" + p2o + "'" : p2o) + " r3:" + "mystring";
                    task.description = desc;
                    task.actions = newActions;

                    List<GraphTargetItem> output = new ArrayList<>();
                    ActionLocalData localData = new ActionLocalData(null, false, new HashMap<>());
                    TranslateStack stack = new TranslateStack("");
                    for (Action a : newActions) {
                        a.translate(localData, stack, output, 0, "");
                    }

                    String ffdecTranslateResult;
                    try {
                        Object res = stack.pop().getResult();
                        ffdecTranslateResult = "Result:" + EcmaScript.toString(res) + " Type:" + EcmaScript.typeString(res);
                    } catch (Exception e) {
                        ffdecTranslateResult = "Error:" + e.getMessage();
                    }

                    String ffdecExecuteResult;
                    try {
                        LocalDataArea lda = new LocalDataArea(new Stage(null));
                        for (Action a : newActions) {
                            if (!a.execute(lda)) {
                                fail();
                            }
                        }

                        Object res = lda.pop();
                        ffdecExecuteResult = "Result:" + EcmaScript.toString(res) + " Type:" + EcmaScript.typeString(res);
                    } catch (Exception e) {
                        ffdecExecuteResult = "Error:" + e.getMessage();
                    }

                    assertEquals(ffdecTranslateResult, ffdecExecuteResult);

                    task.ffdecResult = ffdecExecuteResult;
                    tasks.add(task);
                }
            }
        }

        adobeExecutor.executeActionLists(tasks);

        StringBuilder expeced = new StringBuilder();
        StringBuilder current = new StringBuilder();
        for (AS2ExecuteTask task : tasks) {
            if (!task.flashResult.equals(task.ffdecResult)) {
                System.out.println("Flash result (" + task.description + "): " + task.flashResult);
                System.out.println("FFDec result: " + task.ffdecResult);
                expeced.append(task.description).append(task.flashResult).append(Helper.newLine);
                current.append(task.description).append(task.ffdecResult).append(Helper.newLine);
            }

            boolean checkOnlyStart = false;
            /*if (flashResult.length() > 10) {
             boolean onlyNumber = true;
             for (int k = 0; k < 10; k++) {
             char ch = flashResult.charAt(flashResult.length() - k - 1);
             if (ch < '0' || ch > '9') {
             onlyNumber = false;
             break;
             }
             }

             if (onlyNumber) {
             flashResult = flashResult.substring(0, flashResult.length() - 1);
             checkOnlyStart = true;
             }
             }*/

 /*if (!ffdecResult.equals(flashResult)) {
             LocalDataArea lda = new LocalDataArea();
             for (Action a : task.actions) {
             if (!a.execute(lda)) {
             fail();
             }
             }

             Object res = lda.stack.pop();
             }*/
            if (checkOnlyStart) {
                assertTrue(((String) task.ffdecResult).startsWith(task.flashResult));
            } else {
                assertEquals(task.ffdecResult, task.flashResult);
            }
        }

        //Helper.writeFile("expected.txt", Utf8Helper.getBytes(expeced.toString()));
        //Helper.writeFile("current.txt", Utf8Helper.getBytes(current.toString()));
    }

    private Object[] getAs2Pushes() {
        int r1 = random.nextInt(500) - 255;
        int r2 = random.nextInt(100);
        Object[] pushes = new Object[]{
            Undefined.INSTANCE, Null.INSTANCE,
            false, true,
            Double.NaN,
            "", "-2147483649", "-2147483648", "-2147483647", "-1", "0", "1", "2147483647", "2147483648", "4294967295", "4294967296", "1test", "test", "test2", "test3", "0.0", "1.0", "-1.0",
            -1.0, -0.5, 0, 0.5, 1.0,
            -2147483648, -2147483647, -1073741824, -1073741823, -536870912, -536870911, -268435456, -134217728, -134217727, -67108864, -67108863, -33554432,
            -33554431, -16777216, -16777215, -8388608, -8388607, -4194304, -4194303, -2097152, -2097151, -1048576, -1048575, -524288, -524287, -262144, -262143, -131072, -131071, -65536, -65535, -32768, -32767, -1, 0, 1, 32767, 32768, 268435455,
            -100, 100,
            //r1, r2
            -225, 66
        };
        return pushes;
    }

    private Action getOpAction(int idx) {
        Action result;
        if (idx < 13) {
            result = getUnaryOpAction(idx);
            assertEquals(1, result.getStackPopCount(null, null));
            assertEquals(1, result.getStackPushCount(null, null));
        } else if (idx < 13 + 23) {
            result = getBinaryOpAction(idx - 13);
            assertEquals(2, result.getStackPopCount(null, null));
            assertEquals(1, result.getStackPushCount(null, null));
        } else {
            result = getTernaryOpAction(idx - 13 - 23);
            assertEquals(3, result.getStackPopCount(null, null));
            assertEquals(1, result.getStackPushCount(null, null));
        }

        return result;
    }

    private Action getUnaryOpAction(int idx) {
        switch (idx) {
            case 0:
                return new ActionAsciiToChar();
            case 1:
                return new ActionCharToAscii();
            case 2:
                return new ActionDecrement();
            case 3:
                return new ActionIncrement();
            case 4:
                return new ActionNot();
            case 5:
                return new ActionToInteger();
            case 6:
                return new ActionToNumber();
            case 7:
                return new ActionToString();
            case 8:
                return new ActionTypeOf();
            case 9:
                return new ActionStringLength();
            case 10:
                return new ActionMBAsciiToChar();
            case 11:
                return new ActionMBCharToAscii();
            case 12:
                return new ActionMBStringLength();
        }

        throw new Error("Invalid index");
    }

    private Action getBinaryOpAction(int idx) {
        switch (idx) {
            case 0:
                return new ActionAnd();
            case 1:
                return new ActionAdd();
            case 2:
                return new ActionAdd2();
            case 3:
                return new ActionBitAnd();
            case 4:
                return new ActionBitLShift();
            case 5:
                return new ActionBitOr();
            case 6:
                return new ActionBitRShift();
            case 7:
                return new ActionBitURShift();
            case 8:
                return new ActionBitXor();
            case 9:
                return new ActionDivide();
            case 10:
                return new ActionEquals(Utf8Helper.charsetName);
            case 11:
                return new ActionEquals2();
            case 12:
                return new ActionGreater();
            case 13:
                return new ActionLess();
            case 14:
                return new ActionLess2();
            case 15:
                return new ActionModulo();
            case 16:
                return new ActionMultiply();
            case 17:
                return new ActionOr();
            case 18:
                return new ActionStringAdd();
            case 19:
                return new ActionStrictEquals();
            case 20:
                return new ActionStringGreater();
            case 21:
                return new ActionStringLess();
            case 22:
                return new ActionSubtract();
        }

        throw new Error("Invalid index");
    }

    private Action getTernaryOpAction(int idx) {
        switch (idx) {
            case 0:
                return new ActionStringExtract();
            case 1:
                return new ActionMBStringExtract();
        }

        throw new Error("Invalid index");
    }
}
