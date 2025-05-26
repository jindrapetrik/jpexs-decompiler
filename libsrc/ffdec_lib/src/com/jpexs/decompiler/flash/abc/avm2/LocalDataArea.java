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
package com.jpexs.decompiler.flash.abc.avm2;

import java.util.HashMap;
import java.util.Stack;

/**
 * Local data area for AVM2 method execution.
 *
 * @author JPEXS
 */
public class LocalDataArea {

    /**
     * Name of the method that is currently being executed.
     */
    public String methodName;

    /**
     * Operand stack.
     */
    public Stack<Object> operandStack = new Stack<>();

    /**
     * Scope stack.
     */
    public Stack<Object> scopeStack = new Stack<>();

    /**
     * Local registers values - maps register index to value.
     */
    public HashMap<Integer, Object> localRegisters = new HashMap<>();

    /**
     * Offset of jump
     */
    public Long jump;

    /**
     * Return value of the method.
     */
    public Object returnValue;

    /**
     * Runtime info.
     */
    public AVM2RuntimeInfo runtimeInfo;

    /**
     * Domain memory.
     */
    private byte[] domainMemory;

    /**
     * Constructs a new LocalDataArea.
     */
    public LocalDataArea() {
    }

    /**
     * Gets domain memory.
     *
     * @return Domain memory bytes.
     */
    public byte[] getDomainMemory() {
        if (domainMemory == null) {
            domainMemory = new byte[1024]; // in flash player this is the default size
        }

        return domainMemory;
    }

    /**
     * Gets runtime.
     *
     * @return Runtime.
     */
    public AVM2Runtime getRuntime() {
        return runtimeInfo == null ? AVM2Runtime.UNKNOWN : runtimeInfo.runtime;
    }

    /**
     * Gets debug flag.
     *
     * @return True if debug flag is set.
     */
    public boolean isDebug() {
        return runtimeInfo != null && runtimeInfo.debug;
    }

    /**
     * Clears the local data area.
     */
    public void clear() {
        operandStack.clear();
        scopeStack.clear();
        localRegisters.clear();
        jump = null;
        returnValue = null;
    }
}
