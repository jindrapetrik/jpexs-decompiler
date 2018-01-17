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
package com.jpexs.decompiler.flash.amf.amf3;

public class NoSerializerExistsException extends Exception {

    private final String className;
    private final Object incompleteData;

    /*public NoSerializerExistsException(String className, Object incompleteData) {
        this(className, incompleteData, null);
    }*/
    public NoSerializerExistsException(String className, Object incompleteData, Throwable cause) {
        super("Cannot read AMF - no deserializer defined for class \"" + className + "\".", cause);
        this.className = className;
        this.incompleteData = incompleteData;
    }

    public String getClassName() {
        return className;
    }

    public Object getIncompleteData() {
        return incompleteData;
    }

}
