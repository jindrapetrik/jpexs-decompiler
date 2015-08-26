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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.StoreTypeAction;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DecrementActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.IncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ActionStoreRegister extends Action implements StoreTypeAction {

    public int registerNumber;

    public ActionStoreRegister(int registerNumber) {
        super(0x87, 1);
        this.registerNumber = registerNumber;
    }

    public ActionStoreRegister(int actionLength, SWFInputStream sis) throws IOException {
        super(0x87, actionLength);
        registerNumber = sis.readUI8("registerNumber");
    }

    public ActionStoreRegister(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x87, 0);
        registerNumber = (int) lexLong(lexer);
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUI8(registerNumber);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return 1;
    }

    @Override
    public String toString() {
        return "StoreRegister " + registerNumber;
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem value = stack.pop();
        RegisterNumber rn = new RegisterNumber(registerNumber);
        if (regNames.containsKey(registerNumber)) {
            rn.name = regNames.get(registerNumber);
        }
        value.getMoreSrc().add(new GraphSourceItemPos(this, 0));
        if (variables.containsKey("__register" + registerNumber)) {
            if (variables.get("__register" + registerNumber) instanceof TemporaryRegister) {
                variables.remove("__register" + registerNumber);
            }
        }
        boolean define = !variables.containsKey("__register" + registerNumber);
        if (regNames.containsKey(registerNumber)) {
            define = false;
        }
        variables.put("__register" + registerNumber, value);
        if (value instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) value).value instanceof RegisterNumber) {
                if (((RegisterNumber) ((DirectValueActionItem) value).value).number == registerNumber) {
                    stack.push(value);
                    return;
                }
            }
        }
        if (value instanceof StoreRegisterActionItem) {
            if (((StoreRegisterActionItem) value).register.number == registerNumber) {
                stack.push(value);
                return;
            }
        }

        if (value instanceof IncrementActionItem) {
            GraphTargetItem obj = ((IncrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                stack.push(new PostIncrementActionItem(this, obj));
                stack.push(obj);
                return;
            }
        }
        if (value instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty() && stack.peek().valueEquals(obj)) {
                stack.pop();
                stack.push(new PostDecrementActionItem(this, obj));
                stack.push(obj);
                return;
            }
        }
        stack.push(new StoreRegisterActionItem(this, rn, value, define));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }

    @Override
    public String getVariableName(TranslateStack stack, ConstantPool cpool) {
        return "__register" + registerNumber;
    }
}
