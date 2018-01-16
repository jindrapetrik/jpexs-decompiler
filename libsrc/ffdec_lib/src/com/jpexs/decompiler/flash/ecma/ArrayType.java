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
package com.jpexs.decompiler.flash.ecma;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ArrayType extends ObjectType {

    public static final ObjectType EMPTY_ARRAY = new ArrayType();

    public List<Object> values;

    public ArrayType(List<Object> values) {
        this.values = values;
    }

    private ArrayType() {
        this.values = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                s.append(",");
            }
            s.append(EcmaScript.toString(values.get(i)));
        }
        return s.toString();
    }

    @Override
    public Object getAttribute(String name) {
        if ("length".equals(name)) {
            return (Long) (long) values.size();
        }
        if (name != null && name.matches("0|[1-9][0-9]*")) {
            Long index = Long.parseLong(name);
            int iindex = (int) (long) index;
            if (iindex >= 0 && iindex < values.size()) {
                return values.get(iindex);
            }
        }
        return super.getAttribute(name);
    }

}
