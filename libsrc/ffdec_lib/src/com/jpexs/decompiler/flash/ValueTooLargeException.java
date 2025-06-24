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
package com.jpexs.decompiler.flash;

/**
 * Exception thrown when a value is too large for a specific type.
 *
 * @author JPEXS
 */
public class ValueTooLargeException extends IllegalArgumentException {

    /**
     * Type of the value
     */
    private final String type;
    /**
     * Value that is too large
     */
    private final Object value;

    /**
     * Constructs a new ValueTooLargeException with the specified type and
     * value.
     *
     * @param type Type of the value
     * @param value Value that is too large
     */
    public ValueTooLargeException(String type, Object value) {
        super("Value is too large for " + type + ": " + value);
        this.type = type;
        this.value = value;
    }

    /**
     * Gets the type of the value.
     *
     * @return Type of the value
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the value that is too large.
     *
     * @return Value that is too large
     */
    public Object getValue() {
        return value;
    }

}
