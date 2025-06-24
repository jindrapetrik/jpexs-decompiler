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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * getlex instruction - get property of object, lexically.
 *
 * @author JPEXS
 */
public class GetLexIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public GetLexIns() {
        super(0x60, "getlex", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    public static void resolveLexType(
            AVM2LocalData localData,
            GraphTargetItem obj,
            int multinameIndex,
            Reference<Boolean> isStatic, Reference<GraphTargetItem> type, Reference<GraphTargetItem> callType) {
        type.setVal(TypeItem.UNKNOWN);
        callType.setVal(TypeItem.UNKNOWN);
        String multinameStr = localData.abc.constants.getMultiname(multinameIndex).getName(localData.abc.constants, new ArrayList<>(), true, true);
        for (int b = localData.callStack.size() - 1; b >= 0; b--) {
            MethodBody body = localData.callStack.get(b);
            for (Trait t : body.traits.traits) {
                if (t instanceof TraitSlotConst) {
                    TraitSlotConst tsc = (TraitSlotConst) t;
                    if (Objects.equals(
                            tsc.getName(localData.abc).getName(localData.abc.constants, new ArrayList<>(), true, true),
                            multinameStr
                    )) {
                        GraphTargetItem ty = AbcIndexing.multinameToType(tsc.type_index, localData.abc.constants);
                        type.setVal(ty);
                        callType.setVal(ty);
                        return;
                    }
                }
            }
        }

        if (localData.abcIndex != null) {
            String currentClassName = localData.classIndex == -1 ? null : localData.abc.instance_info.get(localData.classIndex).getName(localData.abc.constants).getNameWithNamespace(localData.abc.constants, true).toRawString();
            if (currentClassName != null) {
                Reference<Boolean> foundStatic = new Reference<>(null);
                localData.abcIndex.findPropertyTypeOrCallType(localData.abc, new TypeItem(currentClassName), multinameStr, localData.abc.constants.getMultiname(multinameIndex).namespace_index, true, true, true, type, callType, foundStatic);
            }

            if (type.getVal().equals(TypeItem.UNKNOWN)) {
                //TypeItem ti = new TypeItem(localData.abc.constants.getMultiname(multinameIndex).getNameWithNamespace(localData.abc.constants, true));
                GraphTargetItem ti = AbcIndexing.multinameToType(multinameIndex, localData.abc.constants);
                if (localData.abcIndex.findClass(ti, localData.abc, localData.scriptIndex) != null) {
                    type.setVal(ti);
                    callType.setVal(TypeItem.UNBOUNDED);
                    isStatic.setVal(true);
                    return;
                }
                Namespace ns = localData.abc.constants.getMultiname(multinameIndex).getNamespace(localData.abc.constants);
                if (ns != null) {
                    String rawNs = ns.getRawName(localData.abc.constants);
                    AbcIndexing.TraitIndex traitIndex = localData.abcIndex.findScriptProperty(multinameStr, DottedChain.parseWithSuffix(rawNs));
                    if (traitIndex != null) {
                        type.setVal(traitIndex.returnType);
                        callType.setVal(traitIndex.callReturnType);
                        isStatic.setVal(true);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        Multiname multiname = localData.getConstants().getMultiname(multinameIndex);
        Reference<Boolean> isStatic = new Reference<>(false);
        Reference<GraphTargetItem> type = new Reference<>(null);
        Reference<GraphTargetItem> callType = new Reference<>(null);
        GetLexIns.resolveLexType(localData, null, multinameIndex, isStatic, type, callType);
        stack.push(new GetLexAVM2Item(ins, localData.lineStartInstruction, multiname, localData.getConstants(), type.getVal(), callType.getVal(), isStatic.getVal()));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
