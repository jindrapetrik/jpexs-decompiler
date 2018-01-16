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

import com.jpexs.decompiler.graph.model.Callable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ObjectType implements Callable {

    public static final ObjectType EMPTY_OBJECT = new ObjectType();

    protected Map<String, Object> attributes;

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    protected ObjectType() {
        this.attributes = new HashMap<>();
    }

    public ObjectType(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Object getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return Undefined.INSTANCE;
    }

    @Override
    public String toString() {
        return "[object " + getTypeName() + "]";
    }

    public String getTypeName() {
        return "Object";
    }

    public Object toPrimitive() {
        return toString();
    }

    @Override
    public Object call(String methodName, List<Object> args) {
        switch (methodName) {
            case "toString":
                return toString();
            case "valueOf":
                return valueOf();
            default:
                return Undefined.INSTANCE; //?
        }
    }

    public Object valueOf() {
        return EcmaScript.toNumber(toString());
    }

    @Override
    public Object call(List<Object> args) {
        return Undefined.INSTANCE; //?
    }

}
