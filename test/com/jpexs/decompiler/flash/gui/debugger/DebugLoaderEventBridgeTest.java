/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.debugger;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DebugLoaderEventBridgeTest {

    private static final String PACKAGE = DebuggerTools.DEBUGGER_PACKAGE + ".pkg012345";

    @DataProvider(name = "operations")
    public Object[][] operations() {
        return new Object[][]{
            {AVM2Instructions.CallProperty, 0}, {AVM2Instructions.CallProperty, 5},
            {AVM2Instructions.CallPropVoid, 2}, {AVM2Instructions.CallPropVoid, 5},
            {AVM2Instructions.GetProperty, 0}
        };
    }

    @Test(dataProvider = "operations")
    public void preservesBranchesExceptionsAndOriginalBytecode(int opcode, int arguments) {
        ABC abc = new ABC(null);
        int name = abc.constants.getPublicQnameId("addEventListener", true);
        MethodBody body = new MethodBody();
        AVM2Code code = body.getCode();
        AVM2Instruction operation = new AVM2Instruction(0, opcode,
                opcode == AVM2Instructions.GetProperty ? new int[]{name} : new int[]{name, arguments});
        code.code.add(new AVM2Instruction(0, AVM2Instructions.Jump, new int[]{0}));
        code.code.add(operation);
        code.code.add(new AVM2Instruction(0, AVM2Instructions.Jump, new int[]{0}));
        code.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null));
        code.markOffsets();
        AVM2Instruction forward = code.code.get(0);
        AVM2Instruction backward = code.code.get(2);
        forward.operands[0] = (int) (operation.getAddress() - forward.getAddress() - forward.getBytesLength());
        backward.operands[0] = (int) (operation.getAddress() - backward.getAddress() - backward.getBytesLength());
        ABCException exception = new ABCException();
        exception.start = (int) operation.getAddress();
        exception.end = (int) backward.getAddress();
        exception.target = (int) code.code.get(3).getAddress();
        body.exceptions = new ABCException[]{exception};
        body.max_stack = arguments + 4;
        abc.bodies.add(body);
        body.setModified();
        byte[] original = body.getCodeBytes().clone();
        int originalStack = body.max_stack;
        int originalEnd = exception.end;
        int originalTarget = exception.target;

        assertTrue(DebugLoaderEventBridge.inject(abc, PACKAGE));
        assertEquals(code.code.size(), 14);
        assertEquals(body.max_stack, originalStack + 2);
        assertEquals(forward.getTargetAddress(), code.code.get(1).getAddress());
        assertEquals(backward.getTargetAddress(), code.code.get(1).getAddress());
        assertEquals(exception.start, code.code.get(1).getAddress());
        assertEquals(exception.end, backward.getAddress());
        assertEquals(exception.target, code.code.get(13).getAddress());
        // The original member lookup is retained, including its namespaces.
        assertEquals(code.code.get(4).operands, new int[]{name});
        byte[] instrumented = body.getCodeBytes().clone();

        assertTrue(DebugLoaderEventBridge.inject(abc, PACKAGE));
        assertEquals(body.getCodeBytes(), instrumented);
        assertEquals(body.max_stack, originalStack + 2);
        assertTrue(DebugLoaderEventBridge.remove(abc, PACKAGE));
        assertEquals(body.getCodeBytes(), original);
        assertEquals(body.max_stack, originalStack);
        assertEquals(exception.end, originalEnd);
        assertEquals(exception.target, originalTarget);
        assertFalse(DebugLoaderEventBridge.remove(abc, PACKAGE));
    }

    @Test
    public void instrumentsBothMethodsOncePerBodyAndLeavesPrivateMethodsAlone() {
        ABC abc = new ABC(null);
        MethodBody body = new MethodBody();
        int add = abc.constants.getPublicQnameId("addEventListener", true);
        int remove = abc.constants.getPublicQnameId("removeEventListener", true);
        int privateAdd = abc.constants.getQnameId("addEventListener", Namespace.KIND_PRIVATE, "private", true);
        AVM2Code code = body.getCode();
        code.code.add(new AVM2Instruction(0, AVM2Instructions.CallPropVoid, new int[]{add, 5}));
        code.code.add(new AVM2Instruction(0, AVM2Instructions.CallPropVoid, new int[]{remove, 3}));
        code.code.add(new AVM2Instruction(0, AVM2Instructions.CallPropVoid, new int[]{privateAdd, 2}));
        code.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null));
        code.markOffsets();
        body.max_stack = 6;
        abc.bodies.add(body);
        body.setModified();
        byte[] original = body.getCodeBytes().clone();

        assertTrue(DebugLoaderEventBridge.inject(abc, PACKAGE));
        assertEquals(code.code.size(), 24);
        assertEquals(body.max_stack, 8);
        assertEquals(code.code.get(22).operands, new int[]{privateAdd, 2});
        assertTrue(DebugLoaderEventBridge.remove(abc, PACKAGE));
        assertEquals(body.getCodeBytes(), original);
        assertEquals(body.max_stack, 6);
    }

    @Test
    public void requiresHelperInTheCompiledDebuggerResource() {
        ABC abc = new ABC(null);
        assertFalse(DebugLoaderEventBridge.isAvailable(abc, PACKAGE));
        InstanceInfo instance = new InstanceInfo();
        instance.name_index = abc.constants.getQnameId("DebugLoader", Namespace.KIND_PACKAGE, PACKAGE, true);
        abc.instance_info.add(instance);
        ClassInfo clazz = new ClassInfo();
        abc.class_info.add(clazz);
        assertFalse(DebugLoaderEventBridge.isAvailable(abc, PACKAGE));
        TraitMethodGetterSetter helper = new TraitMethodGetterSetter();
        helper.name_index = abc.constants.getPublicQnameId("bridgeEventListener", true);
        clazz.static_traits.traits.add(helper);
        assertTrue(DebugLoaderEventBridge.isAvailable(abc, PACKAGE));
    }

    @DataProvider(name = "missingListenerNames")
    public Object[][] missingListenerNames() {
        Object[][] cases = new Object[24][2];
        int index = 0;
        for (int kind = 0; kind < 8; kind++) {
            for (int opcode : new int[]{AVM2Instructions.GetProperty,
                AVM2Instructions.CallProperty, AVM2Instructions.CallPropVoid}) {
                cases[index++] = new Object[]{kind, opcode};
            }
        }
        return cases;
    }

    @Test(dataProvider = "missingListenerNames")
    public void skipsMissingNamesAndStillInstrumentsReachableListeners(int kind, int opcode) {
        ABC abc = new ABC(null);
        int listener = abc.constants.getPublicQnameId("addEventListener", true);
        int string = abc.constants.getStringId("addEventListener", true);
        int publicNamespace = abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true);
        int invalid;
        switch (kind) {
            case 0:
                invalid = 0; // The implicit wildcard entry is null.
                break;
            case 1:
                invalid = abc.constants.addMultiname(null);
                break;
            case 2:
                invalid = 0x100000;
                break;
            case 3:
                invalid = abc.constants.addMultiname(Multiname.createQName(false, string, 0));
                break;
            case 4:
                invalid = abc.constants.addMultiname(Multiname.createMultiname(false, string, 0));
                break;
            case 5:
                invalid = abc.constants.addMultiname(Multiname.createQName(false, 0x100000, publicNamespace));
                break;
            case 6:
                invalid = abc.constants.addMultiname(Multiname.createQName(false, string, 0x100000));
                break;
            default:
                invalid = abc.constants.addMultiname(Multiname.createMultiname(false, string, 0x100000));
                break;
        }
        MethodBody body = new MethodBody();
        AVM2Code code = body.getCode();
        AVM2Instruction bad = new AVM2Instruction(0, opcode,
                opcode == AVM2Instructions.GetProperty ? new int[]{invalid} : new int[]{invalid, 2});
        code.code.add(new AVM2Instruction(0, AVM2Instructions.Jump, new int[]{0}));
        code.code.add(bad); // Obfuscators can put invalid references in dead code.
        code.code.add(new AVM2Instruction(0, AVM2Instructions.CallPropVoid, new int[]{listener, 2}));
        code.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null));
        code.markOffsets();
        code.code.get(0).operands[0] = bad.getBytesLength();
        body.max_stack = 8;
        body.setModified();
        abc.bodies.add(body);
        byte[] original = body.getCodeBytes().clone();

        assertTrue(DebugLoaderEventBridge.inject(abc, PACKAGE));
        assertSame(code.code.get(1), bad);
        assertEquals(code.code.size(), 14);
        assertEquals(code.code.get(0).getTargetAddress(), code.code.get(2).getAddress());
        assertTrue(DebugLoaderEventBridge.remove(abc, PACKAGE));
        assertEquals(body.getCodeBytes(), original);
        assertEquals(body.max_stack, 8);
    }

    @Test
    public void skipsMissingNamespaceWhenAnotherNamespaceIsPublic() {
        ABC abc = new ABC(null);
        int publicNamespace = abc.constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true);
        int set = abc.constants.addNamespaceSet(new NamespaceSet(new int[]{0, publicNamespace}));
        int listener = abc.constants.addMultiname(Multiname.createMultiname(false,
                abc.constants.getStringId("removeEventListener", true), set));
        MethodBody body = new MethodBody();
        body.getCode().code.add(new AVM2Instruction(0, AVM2Instructions.CallPropVoid, new int[]{listener, 2}));
        body.getCode().markOffsets();
        body.max_stack = 3;
        body.setModified();
        abc.bodies.add(body);
        byte[] original = body.getCodeBytes().clone();

        assertTrue(DebugLoaderEventBridge.inject(abc, PACKAGE));
        assertTrue(DebugLoaderEventBridge.remove(abc, PACKAGE));
        assertEquals(body.getCodeBytes(), original);
    }
}
