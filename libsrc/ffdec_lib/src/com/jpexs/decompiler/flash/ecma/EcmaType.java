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

/**
 * ECMA type enumeration.
 *
 * @author JPEXS
 */
public enum EcmaType {

    /**
     * Null
     */
    NULL(null),
    /**
     * String
     */
    STRING("String"),
    /**
     * Number
     */
    NUMBER("Number"),
    /**
     * Undefined
     */
    UNDEFINED(null),
    /**
     * Object
     */
    OBJECT("Object"),
    /**
     * Boolean
     */
    BOOLEAN("Boolean"),
    /**
     * Float
     */
    FLOAT("float"),
    /**
     * Float 4
     */
    FLOAT4("float4");
    
    private final String clsName;

    private EcmaType(String clsName) {
        this.clsName = clsName;
    }

    /**
     * Gets class name.
     * @return Class name
     */
    public String getClassName() {
        return clsName;
    }

    /**
     * Gets property of object.
     * @param val Object
     * @param propName Property name
     * @return Property value
     */
    public Object getProperty(Object val, String propName) {
        String cls = getClassName();
        if (cls == null) {
            return null;
        }
        if ("String".equals(cls)) {
            switch (propName) {
                case "length":
                    return EcmaScript.toString(val).length();
            }
        }
        return null;
    }
}
