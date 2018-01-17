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
package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractVectorType<T> implements WithSubValues, Amf3ValueType {

    private boolean fixed;
    private List<T> values;

    public boolean isFixed() {
        return fixed;
    }

    public AbstractVectorType(boolean fixed, List<T> values) {
        this.values = values;
        this.fixed = fixed;
    }

    public List<T> getValues() {
        return values;
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        ret.addAll(values);
        return ret;
    }

    public abstract String getTypeName();

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

}
