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

import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for AMF3 vector types
 *
 * @param <T> Type of vector values
 */
public abstract class AbstractVectorType<T> implements WithSubValues, Amf3ValueType {

    /**
     * Fixed flag
     */
    private boolean fixed;
    /**
     * Values
     */
    private List<T> values;

    /**
     * Checks if the vector is fixed
     *
     * @return True if the vector is fixed
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Constructs new AbstractVectorType.
     *
     * @param fixed Fixed flag
     * @param values Values
     */
    public AbstractVectorType(boolean fixed, List<T> values) {
        this.values = values;
        this.fixed = fixed;
    }

    /**
     * Gets values.
     *
     * @return Values
     */
    public List<T> getValues() {
        return values;
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        ret.addAll(values);
        return ret;
    }

    /**
     * Gets type name.
     *
     * @return Type name
     */
    public abstract String getTypeName();

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

}
