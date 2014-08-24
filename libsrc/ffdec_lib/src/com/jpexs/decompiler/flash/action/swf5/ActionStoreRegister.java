/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.swf5;

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
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.ByteArrayOutputStream;
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

    public ActionStoreRegister(FlasmLexer lexer) throws IOException, ParseException {
        super(0x87, 0);
        registerNumber = (int) lexLong(lexer);
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI8(registerNumber);
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return surroundWithAction(baos.toByteArray(), version);
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
        value.moreSrc.add(new GraphSourceItemPos(this, 0));
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
            if (!stack.isEmpty()) {
                if (stack.peek().valueEquals(obj)) {
                    stack.pop();
                    stack.push(new PostIncrementActionItem(this, obj));
                    stack.push(obj);
                    return;
                }
            }
        }
        if (value instanceof DecrementActionItem) {
            GraphTargetItem obj = ((DecrementActionItem) value).object;
            if (!stack.isEmpty()) {
                if (stack.peek().valueEquals(obj)) {
                    stack.pop();
                    stack.push(new PostDecrementActionItem(this, obj));
                    stack.push(obj);
                    return;
                }
            }
        }
        stack.push(new StoreRegisterActionItem(this, rn, value, define));
    }

    @Override
    public String getVariableName(TranslateStack stack, ConstantPool cpool) {
        return "__register" + registerNumber;
    }
}
