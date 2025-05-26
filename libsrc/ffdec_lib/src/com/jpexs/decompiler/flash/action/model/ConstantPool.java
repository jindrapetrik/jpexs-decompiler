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
package com.jpexs.decompiler.flash.action.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Constant pool for ActionScript 1/2.
 *
 * @author JPEXS
 */
public class ConstantPool {

    /**
     * List of constants.
     */
    public List<String> constants = new ArrayList<>();

    /**
     * Constructor.
     */
    public ConstantPool() {
    }

    /**
     * Constructor.
     *
     * @param constants List of constants
     */
    public ConstantPool(List<String> constants) {
        this.constants = constants;
    }

    /**
     * Sets new constants.
     * @param constants List of constants
     */
    public void setNew(List<String> constants) {
        this.constants = constants;
    }

    @Override
    public String toString() {
        return "x " + constants.toString();
    }

    /**
     * Checks if constant pool is empty.
     * @return True if empty, false otherwise
     */
    public boolean isEmpty() {
        return constants.isEmpty();
    }
}
