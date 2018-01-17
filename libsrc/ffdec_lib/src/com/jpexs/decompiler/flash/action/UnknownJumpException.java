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

import com.jpexs.decompiler.flash.action.model.ActionItem;
import java.util.List;
import java.util.Stack;

/**
 * Raised when actual address has been referenced with an unknown jump
 *
 * @author JPEXS
 */
public class UnknownJumpException extends RuntimeException {

    /**
     * Actual stack
     */
    public Stack stack;

    /**
     * Actual address
     */
    public long addr;

    /**
     * Output of the method before raising the exception
     */
    public List<ActionItem> output;

    /**
     * Constructor
     *
     * @param stack Actual stack
     * @param addr Actual address
     * @param output Output of the method before raising the exception
     */
    public UnknownJumpException(Stack stack, long addr, List<ActionItem> output) {
        this.stack = stack;
        this.addr = addr;
        this.output = output;
    }

    /**
     * Returns a string representation of the object
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Unknown jump to " + addr;
    }
}
