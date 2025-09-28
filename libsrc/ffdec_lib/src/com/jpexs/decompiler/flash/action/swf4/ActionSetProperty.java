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
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DecrementActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.IncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.SetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.TemporaryRegisterMark;
import com.jpexs.decompiler.flash.action.model.operations.PreDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreIncrementActionItem;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SetProperty action - Sets a property of an object.
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
public class ActionSetProperty extends Action {

    /**
     * Constructor.
     */
    public ActionSetProperty() {
        super(0x23, 0, Utf8Helper.charsetName);
    }

    @Override
    public String toString() {
        return "SetProperty";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (!lda.stackHasMinSize(3)) {
            return false;
        }

        Object value = lda.pop();
        int index = (int) (double) lda.popAsNumber();
        String target = lda.popAsString();
        Object member = lda.stage.getMember(target);
        if (member instanceof ActionScriptObject) {
            ((ActionScriptObject) member).setProperty(index, value);
        }
        return true;
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop().getThroughDuplicate();
        GraphTargetItem index = stack.pop().getThroughDuplicate();
        GraphTargetItem target = stack.pop().getThroughDuplicate();
        int indexInt = 0;
        if (index instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) index).value instanceof Long) {
                indexInt = (int) (long) (Long) ((DirectValueActionItem) index).value;
            } else if (((DirectValueActionItem) index).value instanceof Double) {
                indexInt = (int) Math.round((Double) ((DirectValueActionItem) index).value);
            } else if (((DirectValueActionItem) index).value instanceof Float) {
                indexInt = (int) Math.round((Float) ((DirectValueActionItem) index).value);
            } else if (((DirectValueActionItem) index).isString()) {
                try {
                    indexInt = Integer.parseInt(((DirectValueActionItem) index).toString());
                } catch (NumberFormatException nfe) {
                    Logger.getLogger(ActionGetProperty.class.getName()).log(Level.SEVERE, "Invalid property index: {0}", index.toString());
                }
            }
        } else {
            Logger.getLogger(ActionGetProperty.class.getName()).log(Level.SEVERE, "Invalid property index: {0}", index.getClass().getSimpleName());
        }
        if (value.getThroughDuplicate() instanceof IncrementActionItem) {
            GraphTargetItem obj = ((IncrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();

                if (obj instanceof GetPropertyActionItem) {
                    ((GetPropertyActionItem) obj).useGetPropertyFunction = false;
                }
                stack.push(new PostIncrementActionItem(this, lineStartAction, obj));
                return;
            } else {
                IncrementActionItem dec = (IncrementActionItem) value.getThroughDuplicate();
                if (dec.object instanceof GetPropertyActionItem) {
                    GetPropertyActionItem gp = (GetPropertyActionItem) dec.object;
                    if (gp.target.valueEquals(target) && gp.propertyIndex == indexInt) {
                        output.add(new PostIncrementActionItem(this, lineStartAction, gp));
                        return;
                    }
                }
            }
        }
        if (value.getThroughDuplicate() instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                if (obj instanceof GetPropertyActionItem) {
                    ((GetPropertyActionItem) obj).useGetPropertyFunction = false;
                }
                stack.push(new PostDecrementActionItem(this, lineStartAction, obj));
                return;
            } else {
                DecrementActionItem dec = (DecrementActionItem) value.getThroughDuplicate();
                if (dec.object instanceof GetPropertyActionItem) {
                    GetPropertyActionItem gp = (GetPropertyActionItem) dec.object;
                    if (gp.target.valueEquals(target) && gp.propertyIndex == indexInt) {
                        output.add(new PostDecrementActionItem(this, lineStartAction, gp));
                        return;
                    }
                }
            }
        }

        GraphTargetItem ret = new SetPropertyActionItem(this, lineStartAction, target, indexInt, value);

        if (value instanceof StoreRegisterActionItem) {
            StoreRegisterActionItem sr = (StoreRegisterActionItem) value;
            if (sr.define) {
                value = sr.getValue();
                ((SetPropertyActionItem) ret).setValue(value);
                if (value instanceof IncrementActionItem) {
                    if (((IncrementActionItem) value).object instanceof GetPropertyActionItem) {
                        if (((GetPropertyActionItem) ((IncrementActionItem) value).object).valueEquals(((SetPropertyActionItem) ret).getObject())) {
                            ((GetPropertyActionItem) ((IncrementActionItem) value).object).useGetPropertyFunction = false;
                            ret = new PreIncrementActionItem(this, lineStartAction, ((IncrementActionItem) value).object);
                        }
                    }
                } else if (value instanceof DecrementActionItem) {
                    if (((DecrementActionItem) value).object instanceof GetPropertyActionItem) {
                        if (((GetPropertyActionItem) ((DecrementActionItem) value).object).valueEquals(((SetPropertyActionItem) ret).getObject())) {
                            ((GetPropertyActionItem) ((DecrementActionItem) value).object).useGetPropertyFunction = false;
                            ret = new PreDecrementActionItem(this, lineStartAction, ((DecrementActionItem) value).object);
                        }
                    }
                } else {
                    sr.temporary = true;
                    ((SetPropertyActionItem) ret).setValue(sr);
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
        return 3;
    }
}
