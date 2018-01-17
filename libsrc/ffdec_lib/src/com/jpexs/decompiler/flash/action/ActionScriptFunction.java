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
package com.jpexs.decompiler.flash.action;

import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class ActionScriptFunction extends ActionScriptObject {

    protected long functionOffset;

    protected long functionLength;

    protected String functionName;

    protected List<String> paramNames;

    protected Map<Integer, String> funcRegNames;

    public String getFunctionName() {
        return functionName;
    }

    public Map<Integer, String> getFuncRegNames() {
        return funcRegNames;
    }

    public ActionScriptFunction(long functionOffset, long functionLength, String functionName, List<String> paramNames, Map<Integer, String> funcRegNames) {
        this.functionOffset = functionOffset;
        this.functionLength = functionLength;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.funcRegNames = funcRegNames;
    }

    public long getFunctionLength() {
        return functionLength;
    }

    public long getFunctionOffset() {
        return functionOffset;
    }
}
