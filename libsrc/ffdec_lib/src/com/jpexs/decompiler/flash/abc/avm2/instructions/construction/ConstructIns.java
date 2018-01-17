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
package com.jpexs.decompiler.flash.abc.avm2.instructions.construction;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXAttrAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXElemAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.RegExpAvm2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.XMLAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AddAVM2Item;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructIns extends InstructionDefinition {

    public ConstructIns() {
        super(0x42, "construct", new int[]{AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean isNotCompileTimeSupported() {
        return true;
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        int argCount = ins.getParamAsLong(constants, 0).intValue();
        List<Object> passArguments = new ArrayList<Object>();
        for (int i = argCount - 1; i >= 0; i--) {
            passArguments.set(i, lda.operandStack.pop());
        }

        Object obj = lda.operandStack.pop();
        //lda.executionException = "Cannot call constructor";

        ObjectType result = new ObjectType(new HashMap<>());
        //todo: call construct property of obj

        //push new instance
        lda.operandStack.push(result);
        return true;
    }

    public static boolean walkXML(GraphTargetItem item, List<GraphTargetItem> list) {
        boolean ret = true;
        if (item instanceof StringAVM2Item) {
            list.add(item);
        } else if (item instanceof AddAVM2Item) {
            ret = ret && walkXML(((AddAVM2Item) item).leftSide, list);
            ret = ret && walkXML(((AddAVM2Item) item).rightSide, list);
        } else if ((item instanceof EscapeXElemAVM2Item) || (item instanceof EscapeXAttrAVM2Item)) {
            list.add(item);
        } else {
            return false;
        }
        return ret;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) throws InterruptedException {
        int argCount = ins.operands[0];
        List<GraphTargetItem> args = new ArrayList<>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, stack.pop());
        }
        GraphTargetItem obj = stack.pop();

        boolean isXML = false;
        if (obj instanceof GetPropertyAVM2Item) {
            GetPropertyAVM2Item gpt = (GetPropertyAVM2Item) obj;
            if (gpt.object instanceof FindPropertyAVM2Item) {
                FindPropertyAVM2Item fpt = (FindPropertyAVM2Item) gpt.object;
                FullMultinameAVM2Item fptXmlMult = (FullMultinameAVM2Item) fpt.propertyName;
                FullMultinameAVM2Item gptXmlMult = (FullMultinameAVM2Item) gpt.propertyName;

                isXML = fptXmlMult.isXML(localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames)
                        && gptXmlMult.isXML(localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames);
            }
        }
        if (obj instanceof GetLexAVM2Item) {
            GetLexAVM2Item glt = (GetLexAVM2Item) obj;
            isXML = glt.propertyName.getName(localData.getConstants(), localData.fullyQualifiedNames, true, true).equals("XML");
        }

        if (isXML) {
            if (args.size() == 1) {
                GraphTargetItem arg = args.get(0);
                List<GraphTargetItem> xmlLines = new ArrayList<>();
                if (walkXML(arg, xmlLines)) {
                    stack.push(new XMLAVM2Item(ins, localData.lineStartInstruction, xmlLines));
                    return;
                }
            }
        }

        boolean isRegExp = false;
        if (obj instanceof GetPropertyAVM2Item) {
            GetPropertyAVM2Item gpt = (GetPropertyAVM2Item) obj;
            if (gpt.object instanceof FindPropertyAVM2Item) {
                FindPropertyAVM2Item fpt = (FindPropertyAVM2Item) gpt.object;
                FullMultinameAVM2Item fptRegExpMult = (FullMultinameAVM2Item) fpt.propertyName;
                FullMultinameAVM2Item gptRegExpMult = (FullMultinameAVM2Item) gpt.propertyName;

                isRegExp = fptRegExpMult.isTopLevel("RegExp", localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames)
                        && gptRegExpMult.isTopLevel("RegExp", localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames);
            }
        }
        if (obj instanceof GetLexAVM2Item) {
            GetLexAVM2Item glt = (GetLexAVM2Item) obj;
            isRegExp = glt.propertyName.getName(localData.getConstants(), localData.fullyQualifiedNames, true, true).equals("RegExp");
        }

        if (isRegExp && (args.size() >= 1) && (args.get(0) instanceof StringAVM2Item) && (args.size() == 1 || (args.size() == 2 && args.get(1) instanceof StringAVM2Item))) {
            String pattern = ((StringAVM2Item) args.get(0)).getValue();
            String modifiers = "";
            if (args.size() == 2) {
                modifiers = ((StringAVM2Item) args.get(1)).getValue();
            }
            stack.push(new RegExpAvm2Item(pattern, modifiers, ins, localData.lineStartInstruction));
            return;
        }

        stack.push(new ConstructAVM2Item(ins, localData.lineStartInstruction, obj, args));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return ins.operands[0] + 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
