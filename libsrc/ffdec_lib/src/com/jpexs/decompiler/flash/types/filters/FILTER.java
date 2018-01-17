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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ConcreteClasses;
import com.jpexs.helpers.SerializableImage;
import java.io.Serializable;

/**
 * Bitmap filter
 *
 * @author JPEXS
 */
@ConcreteClasses({BLURFILTER.class, GRADIENTBEVELFILTER.class, GLOWFILTER.class, GRADIENTGLOWFILTER.class, COLORMATRIXFILTER.class, CONVOLUTIONFILTER.class, BEVELFILTER.class, DROPSHADOWFILTER.class})
public abstract class FILTER implements Serializable {

    /**
     * Identificator of type of the filter
     */
    @SWFType(BasicType.UI8)
    public int id;

    /**
     * Constructor
     *
     * @param id Type identificator
     */
    public FILTER(int id) {
        this.id = id;
    }

    public abstract SerializableImage apply(SerializableImage src);

    public abstract double getDeltaX();

    public abstract double getDeltaY();
}
