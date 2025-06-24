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

/**
 * Represents a with block in ActionScript.
 *
 * @author JPEXS
 */
public class ActionScriptWith {

    /**
     * Object that is used in the with block.
     */
    protected ActionScriptObject obj;

    /**
     * Start address of the with block.
     */
    protected long startAddr;

    /**
     * Length of the with block.
     */
    protected long length;

    /**
     * Constructs a new ActionScriptWith object.
     *
     * @param obj Object that is used in the with block.
     * @param startAddr Start address of the with block.
     * @param length Length of the with block.
     */
    public ActionScriptWith(ActionScriptObject obj, long startAddr, long length) {
        this.obj = obj;
        this.startAddr = startAddr;
        this.length = length;
    }
}
