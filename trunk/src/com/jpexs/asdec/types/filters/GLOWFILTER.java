/*
 *  Copyright (C) 2010 JPEXS
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
 * Glow filter
 *
 * @author JPEXS
 */
public class GLOWFILTER extends FILTER {
    /**
     * Color of the shadow
     */
    public RGBA glowColor;
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Strength of the glow
     */
    public float strength;
    /**
     * Inner glow mode
     */
    public boolean innerGlow;
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
    public GLOWFILTER() {
        super(2);
    }
}
