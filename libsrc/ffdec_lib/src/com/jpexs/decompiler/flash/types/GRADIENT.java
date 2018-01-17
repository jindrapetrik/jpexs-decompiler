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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class GRADIENT implements Serializable {

    /**
     * Spread mode
     */
    @SWFType(value = BasicType.UB, count = 2)
    public int spreadMode;

    public static final int SPREAD_PAD_MODE = 0;

    public static final int SPREAD_REFLECT_MODE = 1;

    public static final int SPREAD_REPEAT_MODE = 2;

    public static final int SPREAD_RESERVED = 3;

    /**
     * Interpolation mode
     */
    @SWFType(value = BasicType.UB, count = 2)
    public int interpolationMode;

    public static final int INTERPOLATION_RGB_MODE = 0;

    public static final int INTERPOLATION_LINEAR_RGB_MODE = 1;

    public static final int INTERPOLATION_RESERVED1 = 2;

    public static final int INTERPOLATION_RESERVED2 = 3;

    @SWFArray(value = "record")
    public GRADRECORD[] gradientRecords = new GRADRECORD[0];
}
