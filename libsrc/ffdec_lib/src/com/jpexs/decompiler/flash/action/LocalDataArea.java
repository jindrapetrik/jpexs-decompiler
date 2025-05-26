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
 * Local data area for ActionScript execution.
 *
 * @author JPEXS
 */
public class LocalDataArea {

    /**
     * Constant pool
     */
    public List<String> constantPool;

    /**
     * Stack
     */
    public Stack<Object> stack = new Stack<>();

    /**
     * Functions
     */
    public List<ActionScriptFunction> functions = new ArrayList<>();

    /**
     * Local variables
     */
    public Map<String, Object> localVariables = new HashMap<>();

    /**
     * Withs
     */
    public List<ActionScriptWith> withs = new ArrayList<>();

    /**
     * Local registers - map of register index to value
     */
    public Map<Integer, Object> localRegisters = new HashMap<>();

    /**
     * Target object
     */
    public Object target;

    /**
     * Stage
     */
    public Stage stage;

    /**
     * Jump
     */
    public Long jump;

    /**
     * Return value
     */
    public Object returnValue;

    /**
     * Execution exception
     */
    public String executionException;

    /**
     * Check stack size
     */
    public boolean checkStackSize = true;

    /**
     * Undefined count
     */
    public int undefinedCount = 0;

    /**
     * Constructs a new local data area.
     *
     * @param stage Stage
     */
    public LocalDataArea(Stage stage) {
        this.stage = stage;
        this.target = this.stage;
    }

    /**
     * Constructs a new local data area.
     *
     * @param stage Stage
     * @param preserveVariableOrder Preserve variable order
     */
    public LocalDataArea(Stage stage, boolean preserveVariableOrder) {
        this.stage = stage;
        target = this.stage;
        if (preserveVariableOrder) {
            localVariables = new LinkedHashMap<>();
        }
    }

    /**
     * Checks if the stack is empty.
     *
     * @return True if the stack is empty, otherwise false
     */
    public boolean stackIsEmpty() {
        if (!checkStackSize) {
            return false;
        }
        return stack.isEmpty();
    }

    /**
     * Checks if the stack has a minimum size.
     *
     * @param count Count
     * @return True if the stack has a minimum size, otherwise false
     */
    public boolean stackHasMinSize(int count) {
        if (!checkStackSize) {
            return true;
        }
        return stack.size() >= count;
    }

    /**
     * Clears the local data area.
     */
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

    /**
     * Pushes a value onto the stack.
     *
     * @param val Value
     * @return Value
     */
    public synchronized Object push(Object val) {
        return stack.push(val);
    }

    /**
     * Peeks at the top of the stack.
     *
     * @return Value
     */
    public synchronized Object peek() {
        if (!checkStackSize && stack.isEmpty()) {
            undefinedCount++;
            stack.push(Undefined.INSTANCE);
            return Undefined.INSTANCE;
        }
        return stack.peek();
    }

    /**
     * Pops a value from the stack.
     *
     * @return Value
     */
    public synchronized Object pop() {
        boolean isEmpty = stack.isEmpty();
        if (!checkStackSize && stack.isEmpty()) {
            undefinedCount++;
            return Undefined.INSTANCE;
        }
        return stack.pop();
    }

    /**
     * Pops a value from the stack as a number.
     *
     * @return Value
     */
    public synchronized Double popAsNumber() {
        return EcmaScript.toNumberAs2(pop());
    }

    /**
     * Pops a value from the stack as a string.
     *
     * @return Value
     */
    public synchronized String popAsString() {
        return EcmaScript.toString(pop());
    }
}
