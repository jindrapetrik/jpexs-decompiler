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
package com.jpexs.decompiler.flash.action.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstantPool {

    public List<String> constants = new ArrayList<>();

    public ConstantPool() {
    }

    public ConstantPool(List<String> constants) {
        this.constants = constants;
    }

    public void setNew(List<String> constants) {
        this.constants = constants;
    }

    @Override
    public String toString() {
        return "x " + constants.toString();
    }

    public boolean isEmpty() {
        return constants.isEmpty();
    }
}
