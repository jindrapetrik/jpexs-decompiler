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
 * Exception thrown when an unsupported value type is encountered.
 */
public class UnsupportedValueTypeException extends RuntimeException {

    private Integer marker = null;
    private Class cls = null;

    /**
     * Constructor.
     * @param cls Unsupported class.
     */
    public UnsupportedValueTypeException(Class cls) {
        super("Unsupported type of value - class: " + cls.getSimpleName());
        this.cls = cls;
    }

    /**
     * Constructor.
     * @param marker Unsupported marker.
     */
    public UnsupportedValueTypeException(int marker) {
        super("Unsupported type of value - marker: 0x" + Integer.toHexString(marker));
        this.marker = marker;
    }

    /**
     * Gets marker.
     * @return Marker.
     */
    public Integer getMarker() {
        return marker;
    }

    /**
     * Gets unsupported class.
     * @return Unsupported class.
     */
    public Class getUnsupportedClass() {
        return this.cls;
    }

}
