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
package com.jpexs.decompiler.flash.abc.types;

public class Float4 {

    public float[] values = new float[4];

    public Float4(float value1, float value2, float value3, float value4) {
        this.values = new float[]{value1, value2, value3, value4};
    }

    public Float4(float[] values) {
        if (values == null || values.length < 4) {
            throw new IllegalArgumentException("Invalid values size");
        }
        this.values[0] = values[0];
        this.values[1] = values[1];
        this.values[2] = values[2];
        this.values[3] = values[3];
    }

}
