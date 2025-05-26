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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

/**
 * Morph line style.
 *
 * @author JPEXS
 */
public class MORPHLINESTYLE implements Serializable {

    /**
     * Start width
     */
    @SWFType(BasicType.UI16)
    public int startWidth;

    /**
     * End width
     */
    @SWFType(BasicType.UI16)
    public int endWidth;

    /**
     * Start color
     */
    public RGBA startColor;

    /**
     * End color
     */
    public RGBA endColor;

    /**
     * Gets start line style.
     * @return Start line style
     */
    public LINESTYLE getStartLineStyle() {
        LINESTYLE ret = new LINESTYLE();
        ret.color = startColor;
        ret.width = startWidth;
        return ret;
    }

    /**
     * Gets line style at given ratio.
     * @param ratio Ratio
     * @return Line style
     */
    public LINESTYLE getLineStyleAt(int ratio) {
        LINESTYLE ret = new LINESTYLE();
        ret.color = MORPHGRADIENT.morphColor(startColor, endColor, ratio);
        ret.width = startWidth + (endWidth - startWidth) * ratio / 65535;
        return ret;
    }

    /**
     * Gets end line style.
     * @return End line style
     */
    public LINESTYLE getEndLineStyle() {
        LINESTYLE ret = new LINESTYLE();
        ret.color = endColor;
        ret.width = endWidth;
        return ret;
    }
}
