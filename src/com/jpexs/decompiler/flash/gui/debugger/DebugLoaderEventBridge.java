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
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import java.util.ArrayList;
import java.util.List;

/**
 * Routes listener registration through DebugLoader without replacing LoaderInfo.
 * LoaderInfo.dispatchEvent cannot be used to forward URLLoader events.
 */
final class DebugLoaderEventBridge {

    private static final String METHOD = "bridgeEventListener";
    private static final int SEQUENCE_LENGTH = 11;

    private DebugLoaderEventBridge() {
    }

    static boolean isAvailable(ABC abc, String debuggerPackage) {
        int loaderName = abc.constants.getQnameId("DebugLoader", Namespace.KIND_PACKAGE, debuggerPackage, false);
        for (int i = 0; i < abc.instance_info.size(); i++) {
            if (abc.instance_info.get(i).name_index != loaderName) {
                continue;
            }
            for (Trait trait : abc.class_info.get(i).static_traits.traits) {
                Multiname name = abc.constants.getMultiname(trait.name_index);
                if (name != null && name.name_index > 0 && name.name_index < abc.constants.getStringCount()
                        && METHOD.equals(abc.constants.getString(name.name_index))) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean inject(ABC abc, String debuggerPackage) {
        // Normalize an already instrumented ABC before injecting again.
        boolean changed = remove(abc, debuggerPackage);
        int loaderName = abc.constants.getQnameId("DebugLoader", Namespace.KIND_PACKAGE, debuggerPackage, true);
        int bridgeName = abc.constants.getPublicQnameId(METHOD, true);
        int applyName = abc.constants.getPublicQnameId("apply", true);
        for (MethodBody body : abc.bodies) {
            AVM2Code code = body.getCode();
            boolean bodyChanged = false;
            for (int i = 0; i < code.code.size(); i++) {
                AVM2Instruction original = code.code.get(i);
                int opcode = original.definition.instructionCode;
                if (opcode != AVM2Instructions.CallProperty && opcode != AVM2Instructions.CallPropVoid
                        && opcode != AVM2Instructions.GetProperty) {
                    continue;
                }
                if (!isListenerName(abc, original.operands[0])) {
                    continue;
                }
                List<AVM2Instruction> replacement = new ArrayList<>();
                boolean reference = opcode == AVM2Instructions.GetProperty;
                // Preserve evaluation order, the original multiname lookup, all
                // optional arguments, and any values below the call on the stack.
                replacement.add(reference ? instruction(AVM2Instructions.PushNull)
                        : instruction(AVM2Instructions.NewArray, original.operands[1]));
                replacement.add(instruction(AVM2Instructions.Swap));
                replacement.add(instruction(AVM2Instructions.Dup));
                replacement.add(instruction(AVM2Instructions.GetProperty, original.operands[0]));
                replacement.add(instruction(AVM2Instructions.NewArray, 3));
                replacement.add(instruction(AVM2Instructions.GetLex, loaderName));
                replacement.add(instruction(AVM2Instructions.GetProperty, bridgeName));
                replacement.add(instruction(AVM2Instructions.Swap));
                replacement.add(instruction(AVM2Instructions.PushNull));
                replacement.add(instruction(AVM2Instructions.Swap));
                replacement.add(instruction(opcode == AVM2Instructions.CallPropVoid
                        ? AVM2Instructions.CallPropVoid : AVM2Instructions.CallProperty, applyName, 2));
                replace(code, body, i, 1, replacement);
                i += replacement.size() - 1;
                bodyChanged = true;
            }
            if (bodyChanged) {
                body.max_stack += 2;
                body.setModified();
                changed = true;
            }
        }
        return changed;
    }

    static boolean remove(ABC abc, String debuggerPackage) {
        int loaderName = abc.constants.getQnameId("DebugLoader", Namespace.KIND_PACKAGE, debuggerPackage, false);
        int bridgeName = abc.constants.getPublicQnameId(METHOD, false);
        int applyName = abc.constants.getPublicQnameId("apply", false);
        if (loaderName <= 0 || bridgeName <= 0 || applyName <= 0) {
            return false;
        }
        boolean changed = false;
        for (MethodBody body : abc.bodies) {
            AVM2Code code = body.getCode();
            boolean bodyChanged = false;
            for (int i = 0; i + SEQUENCE_LENGTH <= code.code.size(); i++) {
                AVM2Instruction first = code.code.get(i);
                boolean reference = first.definition.instructionCode == AVM2Instructions.PushNull;
                AVM2Instruction last = code.code.get(i + 10);
                int opcode = last.definition.instructionCode;
                if ((!reference && first.definition.instructionCode != AVM2Instructions.NewArray)
                        || !matches(code, i + 1, AVM2Instructions.Swap)
                        || !matches(code, i + 2, AVM2Instructions.Dup)
                        || code.code.get(i + 3).definition.instructionCode != AVM2Instructions.GetProperty
                        || !isListenerName(abc, code.code.get(i + 3).operands[0])
                        || !matches(code, i + 4, AVM2Instructions.NewArray, 3)
                        || !matches(code, i + 5, AVM2Instructions.GetLex, loaderName)
                        || !matches(code, i + 6, AVM2Instructions.GetProperty, bridgeName)
                        || !matches(code, i + 7, AVM2Instructions.Swap)
                        || !matches(code, i + 8, AVM2Instructions.PushNull)
                        || !matches(code, i + 9, AVM2Instructions.Swap)
                        || (opcode != AVM2Instructions.CallProperty && opcode != AVM2Instructions.CallPropVoid)
                        || !matches(code, i + 10, opcode, applyName, 2)
                        || (reference && opcode != AVM2Instructions.CallProperty)) {
                    continue;
                }
                int name = code.code.get(i + 3).operands[0];
                AVM2Instruction original = reference ? instruction(AVM2Instructions.GetProperty, name)
                        : instruction(opcode, name, first.operands[0]);
                List<AVM2Instruction> replacement = new ArrayList<>();
                replacement.add(original);
                replace(code, body, i, SEQUENCE_LENGTH, replacement);
                bodyChanged = true;
            }
            if (bodyChanged) {
                body.max_stack -= 2;
                body.setModified();
                changed = true;
            }
        }
        return changed;
    }

    private static boolean isListenerName(ABC abc, int index) {
        // Index zero is the implicit wildcard entry, not a Multiname object.
        // Dead obfuscation code can also reference missing pool entries.
        if (index <= 0 || index >= abc.constants.getMultinameCount()) {
            return false;
        }
        Multiname name = abc.constants.getMultiname(index);
        if (name == null || (name.kind != Multiname.QNAME && name.kind != Multiname.MULTINAME)
                || name.name_index <= 0 || name.name_index >= abc.constants.getStringCount()) {
            return false;
        }
        String value = abc.constants.getString(name.name_index);
        if (!"addEventListener".equals(value) && !"removeEventListener".equals(value)) {
            return false;
        }
        int[] namespaces;
        if (name.kind == Multiname.QNAME) {
            namespaces = new int[]{name.namespace_index};
        } else {
            if (name.namespace_set_index <= 0 || name.namespace_set_index >= abc.constants.getNamespaceSetCount()) {
                return false;
            }
            NamespaceSet set = abc.constants.getNamespaceSet(name.namespace_set_index);
            if (set == null || set.namespaces == null) {
                return false;
            }
            namespaces = set.namespaces;
        }
        for (int indexNs : namespaces) {
            if (indexNs <= 0 || indexNs >= abc.constants.getNamespaceCount()) {
                continue;
            }
            Namespace ns = abc.constants.getNamespace(indexNs);
            if (ns != null && ns.kind == Namespace.KIND_PACKAGE
                    && ns.name_index >= 0 && ns.name_index < abc.constants.getStringCount()
                    && "".equals(abc.constants.getString(ns.name_index))) {
                return true;
            }
        }
        return false;
    }

    private static AVM2Instruction instruction(int opcode, int... operands) {
        return new AVM2Instruction(0, opcode, operands.length == 0 ? null : operands);
    }

    private static boolean matches(AVM2Code code, int index, int opcode, int... operands) {
        AVM2Instruction instruction = code.code.get(index);
        if (instruction.definition.instructionCode != opcode) {
            return false;
        }
        return operands.length == 0 || java.util.Arrays.equals(instruction.operands, operands);
    }

    private static void replace(AVM2Code code, MethodBody body, int index, int count,
            List<AVM2Instruction> replacement) {
        // Keep branches and exception boundaries at the beginning of the replacement.
        // Inserting later instructions must move existing targets past the insertion.
        for (int i = count - 1; i > 0; i--) {
            code.removeInstruction(index + i, body);
        }
        code.replaceInstruction(index, replacement.get(0), body);
        for (int i = 1; i < replacement.size(); i++) {
            code.insertInstruction(index + i, replacement.get(i), true, body);
        }
    }
}
