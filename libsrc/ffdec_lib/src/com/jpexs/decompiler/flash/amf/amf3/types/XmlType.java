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
package com.jpexs.decompiler.flash.amf.amf3.types;

/**
 * AMF3 XML document type.
 */
public class XmlType implements Amf3ValueType {

    /**
     * Data
     */
    private String data;

    /**
     * Constructor.
     * @param data Data
     */
    public XmlType(String data) {
        this.data = data;
    }

    /**
     * Gets data.
     * @return Data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets data.
     * @param data Data
     */
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

}
