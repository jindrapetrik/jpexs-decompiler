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
package com.jpexs.decompiler.flash.ecma;

import com.jpexs.decompiler.graph.model.Callable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ECMA Object type.
 *
 * @author JPEXS
 */
public class ObjectType implements Callable {

    /**
     * Empty object
     */
    public static final ObjectType EMPTY_OBJECT = new ObjectType();

    /**
     * Attributes
     */
    protected Map<String, Object> attributes;

    /**
     * Sets attribute.
     * @param name Name
     * @param value Value
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Gets attribute names.
     * @return Attribute names
     */
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * Constructor.
     */
    protected ObjectType() {
        this.attributes = new HashMap<>();
    }

    /**
     * Constructor.
     * @param attributes Attributes
     */
    public ObjectType(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets attribute.
     * @param name Name
     * @return Attribute
     */
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

    /**
     * Gets type name.
     * @return Type name
     */
    public String getTypeName() {
        return "Object";
    }

    /**
     * Converts to primitive.
     * @return Primitive
     */
    public Object toPrimitive() {
        return toString();
    }

    /**
     * Converts to number.
     * @return Number
     */
    public Object valueOf() {
        return EcmaScript.toNumber(toString());
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

    @Override
    public Object call(List<Object> args) {
        return Undefined.INSTANCE; //?
    }

}
