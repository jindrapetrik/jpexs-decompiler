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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class LocalDataArea {

    public List<String> constantPool;

    public Stack<Object> stack = new Stack<>();

    public List<ActionScriptFunction> functions = new ArrayList<>();

    public Map<String, Object> localVariables = new HashMap<>();

    public List<ActionScriptWith> withs = new ArrayList<>();

    public Map<Integer, Object> localRegisters = new HashMap<>();

    public Object target;

    public Stage stage;

    public Long jump;

    public Object returnValue;

    public String executionException;

    public boolean checkStackSize = true;

    public int undefinedCount = 0;

    public LocalDataArea(Stage stage) {
        this.stage = stage;
        this.target = this.stage;
    }

    public LocalDataArea(Stage stage, boolean preserveVariableOrder) {
        this.stage = stage;
        target = this.stage;
        if (preserveVariableOrder) {
            localVariables = new LinkedHashMap<>();
        }
    }

    public boolean stackIsEmpty() {
        if (!checkStackSize) {
            return false;
        }
        return stack.isEmpty();
    }

    public boolean stackHasMinSize(int count) {
        if (!checkStackSize) {
            return true;
        }
        return stack.size() >= count;
    }

    public void clear() {
        constantPool = null;
        stack.clear();
        localVariables.clear();
        localRegisters.clear();
        withs.clear();
        functions.clear();
        stage.clear();
        jump = null;
        returnValue = null;
        executionException = null;
        target = stage;
        undefinedCount = 0;
    }

    public synchronized Object push(Object val) {
        return stack.push(val);
    }

    public synchronized Object peek() {
        if (!checkStackSize && stack.isEmpty()) {
            undefinedCount++;
            stack.push(Undefined.INSTANCE);
            return Undefined.INSTANCE;
        }
        return stack.peek();
    }

    public synchronized Object pop() {
        boolean isEmpty = stack.isEmpty();
        if (!checkStackSize && stack.isEmpty()) {
            undefinedCount++;
            return Undefined.INSTANCE;
        }
        return stack.pop();
    }

    public synchronized Double popAsNumber() {
        return EcmaScript.toNumberAs2(pop());
    }

    public synchronized String popAsString() {
        return EcmaScript.toString(pop());
    }
}
