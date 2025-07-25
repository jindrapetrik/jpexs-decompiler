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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.StoreTypeAction;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.CompoundableBinaryOpAs12;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DecrementActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.IncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.TemporaryRegisterMark;
import com.jpexs.decompiler.flash.action.model.operations.PreDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreIncrementActionItem;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SetVariable action - Sets a variable value.
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
public class ActionSetVariable extends Action implements StoreTypeAction {

    /**
     * Constructor.
     */
    public ActionSetVariable() {
        super(0x1D, 0, Utf8Helper.charsetName);
    }

    @Override
    public String toString() {
        return "SetVariable";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (!lda.stackHasMinSize(2)) {
            return false;
        }

        Object value = lda.pop();
        lda.localVariables.put(EcmaScript.toString(lda.pop()), value);
        return true;
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop().getThroughDuplicate();
        GraphTargetItem name = stack.pop();
        String nameStr;
        if (name instanceof DirectValueActionItem) {
            nameStr = name.toStringNoQuotes(LocalData.empty);
        } else {
            nameStr = EcmaScript.toString(name.getResult());
        }
        variables.put(nameStr, value);
        if (value instanceof IncrementActionItem) {
            GraphTargetItem obj = ((IncrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                if (obj instanceof GetVariableActionItem) {
                    ((GetVariableActionItem) obj).printObfuscatedName = true;
                }
                stack.push(new PostIncrementActionItem(this, lineStartAction, obj));
                return;
            }
        }
        if (value instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                if (obj instanceof GetVariableActionItem) {
                    ((GetVariableActionItem) obj).printObfuscatedName = true;
                }
                stack.push(new PostDecrementActionItem(this, lineStartAction, obj));
                return;
            }
        }
        if (value instanceof IncrementActionItem) {
            if (((IncrementActionItem) value).object instanceof GetVariableActionItem) {
                if (((GetVariableActionItem) ((IncrementActionItem) value).object).name.valueEquals(name)) {

                    if (((IncrementActionItem) value).object instanceof GetVariableActionItem) {
                        ((GetVariableActionItem) ((IncrementActionItem) value).object).printObfuscatedName = true;
                    }
                    output.add(new PostIncrementActionItem(this, lineStartAction, ((IncrementActionItem) value).object));
                    return;
                }
            }
        }
        if (value instanceof DecrementActionItem) {
            if (((DecrementActionItem) value).object instanceof GetVariableActionItem) {
                if (((GetVariableActionItem) ((DecrementActionItem) value).object).name.valueEquals(name)) {
                    if (((DecrementActionItem) value).object instanceof GetVariableActionItem) {
                        ((GetVariableActionItem) ((DecrementActionItem) value).object).printObfuscatedName = true;
                    }
                    output.add(new PostDecrementActionItem(this, lineStartAction, ((DecrementActionItem) value).object));
                    return;
                }
            }
        }

        SetVariableActionItem setVar = new SetVariableActionItem(this, lineStartAction, name, value);
        GraphTargetItem ret = setVar;

        GraphTargetItem inside = value.getNotCoercedNoDup();
        if (inside instanceof StoreRegisterActionItem) {
            inside = inside.value;
        }
        if (inside instanceof CompoundableBinaryOpAs12) {
            if (!name.hasSideEffect()) {
                CompoundableBinaryOp binaryOp = (CompoundableBinaryOp) inside;
                if (binaryOp.getLeftSide() instanceof GetVariableActionItem) {
                    GetVariableActionItem getVar = (GetVariableActionItem) binaryOp.getLeftSide();
                    if (GraphTargetItem.objectsValueEquals(name, getVar.name)) {
                        setVar.setCompoundValue(binaryOp.getRightSide());
                        setVar.setCompoundOperator(binaryOp.getOperator());
                    }
                }
            }
        }

        if (value instanceof StoreRegisterActionItem) {
            StoreRegisterActionItem sr = (StoreRegisterActionItem) value;
            if (sr.define) {
                value = sr.getValue();
                ((SetVariableActionItem) ret).setValue(value);
                if (value instanceof IncrementActionItem) {
                    if (((IncrementActionItem) value).object instanceof GetVariableActionItem) {
                        if (((GetVariableActionItem) ((IncrementActionItem) value).object).name.valueEquals(name)) {
                            ((GetVariableActionItem) ((IncrementActionItem) value).object).printObfuscatedName = true;
                            ret = new PreIncrementActionItem(this, lineStartAction, ((IncrementActionItem) value).object);
                        }
                    }
                } else if (value instanceof DecrementActionItem) {
                    if (((DecrementActionItem) value).object instanceof GetVariableActionItem) {
                        if (((GetVariableActionItem) ((DecrementActionItem) value).object).name.valueEquals(name)) {
                            ((GetVariableActionItem) ((DecrementActionItem) value).object).printObfuscatedName = true;
                            ret = new PreDecrementActionItem(this, lineStartAction, ((DecrementActionItem) value).object);
                        }
                    }
                } else {
                    sr.temporary = true;
                    ((SetVariableActionItem) ret).setValue(sr);
                }

                TemporaryRegister tr = new TemporaryRegister(sr.register.number, ret);
                variables.put("__register" + sr.register.number, tr);
                output.add(new TemporaryRegisterMark(tr));
                return;
            }
        }

        output.add(ret);
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }

    @Override
    public String getVariableName(Set<String> usedDeobfuscations, TranslateStack stack, ConstantPool cpool, SWF swf) {
        if (stack.size() < 2) {
            return null;
        }
        return stack.get(stack.size() - 2).toStringNoQuotes(LocalData.create(cpool, swf, usedDeobfuscations));
    }
}
