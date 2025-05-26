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

import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.Date;

/**
 * AMF3 date type.
 */
public class DateType implements Amf3ValueType {

    private double val;

    /**
     * Constructor.
     * @param val Date value
     */
    public DateType(double val) {
        this.val = val;
    }

    /**
     * Gets date value.
     * @return Date value
     */
    public double getVal() {
        return val;
    }

    /**
     * Sets date value.
     * @param val Date value
     */
    public void setVal(double val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

    /**
     * Converts this to date.
     * @return Date
     */
    public Date toDate() {
        return new Date((long) val);
    }
}
