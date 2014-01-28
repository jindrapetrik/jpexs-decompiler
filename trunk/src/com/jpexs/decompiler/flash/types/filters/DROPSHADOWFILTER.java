/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.SerializableImage;

/**
 * Drop shadow filter based on the same median filter as the blur filter
 *
 * @author JPEXS
 */
public class DROPSHADOWFILTER extends FILTER {

    /**
     * Color of the shadow
     */
    public RGBA dropShadowColor;
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Radian angle of the drop shadow
     */
    public double angle;
    /**
     * Distance of the drop shadow
     */
    public double distance;
    /**
     * Strength of the drop shadow
     */
    public float strength;
    /**
     * Inner shadow mode
     */
    public boolean innerShadow;
    /**
     * Knockout mode
     */
    public boolean knockout;
    /**
     * Composite source
     */
    public boolean compositeSource;
    /**
     * Number of blur passes
     */
    public int passes;

    /**
     * Constructor
     */
    public DROPSHADOWFILTER() {
        super(0);
    }

    @Override
    public SerializableImage apply(SerializableImage src) {
        return Filtering.dropShadow(src, (int) blurX, (int) blurY, (int) (angle * 180 / Math.PI), distance, dropShadowColor.toColor(), innerShadow, passes, strength, knockout);
    }
}
