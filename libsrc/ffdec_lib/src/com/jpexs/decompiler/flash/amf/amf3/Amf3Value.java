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

import com.jpexs.decompiler.flash.amf.amf3.types.Amf3ValueType;
import com.jpexs.decompiler.flash.amf.amf3.types.BasicType;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3ParseException;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import java.io.IOException;

public class Amf3Value {

    @SWFField
    @Multiline
    private String data = null;

    public Amf3Value() {
        setValue(null);
    }

    public Amf3Value(Object value) {
        setValue(value);
    }

    public void setValue(Object value) {
        if (!isValueValid(value)) {
            throw new IllegalArgumentException("Invalid Amf value: " + value.getClass().getSimpleName());
        }
        this.data = value == null ? "" : Amf3Exporter.amfToString(value, "  ", "\n");
    }

    public static boolean isValueValid(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Long) {
            return true;
        }
        if (value instanceof Double) {
            return true;
        }
        if (value instanceof String) {
            return true;
        }
        if (value instanceof Boolean) {
            return true;
        }
        if (value instanceof Amf3ValueType) {
            return true;
        }
        return false;
    }

    public Object getValue() {
        Amf3Importer imp = new Amf3Importer();
        try {
            return imp.stringToAmf(data);
        } catch (IOException | Amf3ParseException ex) {
            return BasicType.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return data;
    }
}
