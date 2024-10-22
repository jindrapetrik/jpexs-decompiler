/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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

import java.util.List;
import java.util.Map;

/**
 * Represents an ActionScript function.
 *
 * @author JPEXS
 */
public class ActionScriptFunction extends ActionScriptObject {

    /**
     * Offset of the function
     */
    protected long functionOffset;

    /**
     * Length of the function
     */
    protected long functionLength;

    /**
     * Name of the function
     */
    protected String functionName;

    /**
     * Names of the parameters
     */
    protected List<String> paramNames;

    /**
     * Names of the registers - map of register index to register name
     */
    protected Map<Integer, String> funcRegNames;

    /**
     * Gets function name
     *
     * @return Function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Gets register names
     *
     * @return Register names
     */
    public Map<Integer, String> getFuncRegNames() {
        return funcRegNames;
    }

    /**
     * Constructs a new ActionScriptFunction
     *
     * @param functionOffset Offset of the function
     * @param functionLength Length of the function
     * @param functionName Name of the function
     * @param paramNames Names of the parameters
     * @param funcRegNames Names of the registers
     */
    public ActionScriptFunction(long functionOffset, long functionLength, String functionName, List<String> paramNames, Map<Integer, String> funcRegNames) {
        this.functionOffset = functionOffset;
        this.functionLength = functionLength;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.funcRegNames = funcRegNames;
    }

    /**
     * Gets function length
     *
     * @return Function length
     */
    public long getFunctionLength() {
        return functionLength;
    }

    /**
     * Gets function offset
     *
     * @return Function offset
     */
    public long getFunctionOffset() {
        return functionOffset;
    }
}
