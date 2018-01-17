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
package com.jpexs.decompiler.flash.abc.avm2;

import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class LocalDataArea {

    public String methodName;

    public Stack<Object> operandStack = new Stack<>();

    public Stack<Object> scopeStack = new Stack<>();

    public HashMap<Integer, Object> localRegisters = new HashMap<>();

    public Long jump;

    public Object returnValue;

    public AVM2RuntimeInfo runtimeInfo;

    private byte[] domainMemory;

    public LocalDataArea() {
    }

    public byte[] getDomainMemory() {
        if (domainMemory == null) {
            domainMemory = new byte[1024]; // in flash player this is the default size
        }

        return domainMemory;
    }

    public AVM2Runtime getRuntime() {
        return runtimeInfo == null ? AVM2Runtime.UNKNOWN : runtimeInfo.runtime;
    }

    public boolean isDebug() {
        return runtimeInfo != null && runtimeInfo.debug;
    }

    public void clear() {
        operandStack.clear();
        scopeStack.clear();
        localRegisters.clear();
        jump = null;
        returnValue = null;
    }
}
