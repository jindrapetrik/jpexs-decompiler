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
package com.jpexs.decompiler.flash.amf.amf0.types;

import com.jpexs.decompiler.flash.amf.amf3.types.Amf3ValueType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AMF0 date type.
 */
public class DateType implements Amf3ValueType, ComplexObject {

    private int timezone;
    
    private double val;

    /**
     * Constructor.
     * @param val Date value
     * @param timezone Time zone
     */
    public DateType(double val, int timezone) {
        this.val = val;
        this.timezone = timezone;
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

    /**
     * Converts this to date.
     * @return Date
     */
    public Date toDate() {
        return new Date((long) val);
    }

    /**
     * Gets timezone
     * @return Timezone
     */
    public int getTimezone() {
        return timezone;
    }   
    
    @Override
    public String toString() {    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        return sdf.format(toDate()) + " timezone " + timezone;
    }

    @Override
    public List<Object> getSubValues() {
        return new ArrayList<>();
    }    
    
}
