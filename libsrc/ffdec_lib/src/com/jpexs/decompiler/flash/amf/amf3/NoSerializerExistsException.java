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
package com.jpexs.decompiler.flash.amf.amf3;

/**
 * Exception thrown when no deserializer exists for a given class.
 */
public class NoSerializerExistsException extends Exception {

    private final String className;
    private final Object incompleteData;

    /**
     * Constructor.
     * @param className Class name
     * @param incompleteData Incomplete data
     * @param cause Cause
     */
    public NoSerializerExistsException(String className, Object incompleteData, Throwable cause) {
        super("Cannot read AMF - no deserializer defined for class \"" + className + "\".", cause);
        this.className = className;
        this.incompleteData = incompleteData;
    }

    /**
     * Gets the class name.
     * @return Class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the incomplete data.
     * @return Incomplete data
     */
    public Object getIncompleteData() {
        return incompleteData;
    }

}
