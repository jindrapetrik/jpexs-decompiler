/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.types.filters;

import com.jpexs.asdec.types.RGBA;

/**
 * Bevel filter with gradient instead of single color
 *
 * @author JPEXS
 */
public class GRADIENTBEVELFILTER extends FILTER {
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
     * Radian angle of the gradient bevel
     */
    public double angle;
    /**
     * Distance of the gradient bevel
     */
    public double distance;
    /**
     * Strength of the gradient bevel
     */
    public float strength;
    /**
     * Inner bevel mode
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

    public GRADIENTBEVELFILTER() {
        super(7);
    }
}
