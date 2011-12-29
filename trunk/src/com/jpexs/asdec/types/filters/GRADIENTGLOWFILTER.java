/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Glow filter with gradient instead of single color
 *
 * @author JPEXS
 */
public class GRADIENTGLOWFILTER extends FILTER {
    /**
     * Gradient colors
     */
    public RGBA gradientColors[];
    /**
     * Gradient ratios
     */
    public int gradientRatio[];
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Radian angle of the gradient glow
     */
    public double angle;
    /**
     * Distance of the gradient glow
     */
    public double distance;
    /**
     * Strength of the gradient glow
     */
    public float strength;
    /**
     * Inner glow mode
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
     * OnTop mode
     */
    public boolean onTop;
    /**
     * Number of blur passes
     */
    public int passes;

    /**
     * Constructor
     */
    public GRADIENTGLOWFILTER() {
        super(4);
    }
}
