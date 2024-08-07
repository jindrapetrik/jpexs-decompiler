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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.List;

/**
 * Constant index in the constant pool.
 *
 * @author JPEXS
 */
public class ConstantIndex implements Serializable {

    /**
     * Index in the constant pool.
     */
    public int index;

    /**
     * Constructs a new constant index.
     *
     * @param index Index in the constant pool
     */
    public ConstantIndex(int index) {
        this.index = index;
    }

    /**
     * To string, no quotes.
     *
     * @param constantPool Constant pool
     * @param resolve Resolve constant pool
     * @return String representation
     */
    public String toStringNoQ(List<String> constantPool, boolean resolve) {
        if (resolve) {
            if (constantPool != null && index < constantPool.size()) {
                return constantPool.get(index);
            }
        }

        return "constant" + index;
    }

    /**
     * To string. With quotes.
     *
     * @param constantPool Constant pool
     * @param resolve Resolve constant pool
     * @return String representation
     */
    public String toString(List<String> constantPool, boolean resolve) {
        if (resolve) {
            if (constantPool != null && index < constantPool.size()) {
                return "\"" + Helper.escapeActionScriptString(constantPool.get(index)) + "\"";
            }
        }

        return "constant" + index;
    }
}
