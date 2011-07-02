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
 * Two-dimensional discrete convolution filter.
 *
 * @author JPEXS
 */
public class CONVOLUTIONFILTER extends FILTER {
    /**
     * Horizontal matrix size
     */
    public int matrixX;
    /**
     * Vertical matrix size
     */
    public int matrixY;
    /**
     * Divisor applied to the matrix values
     */
    public float divisor;
    /**
     * Bias applied to the matrix values
     */
    public float bias;
    /**
     * Matrix values
     */
    public float matrix[][];
    /**
     * Default color for pixels outside the image
     */
    public RGBA defaultColor;
    /**
     * Clamp mode
     */
    public boolean clamp;
    /**
     * Preserve the alpha
     */
    public boolean preserveAlpha;

    /**
     * Constructor
     */
    public CONVOLUTIONFILTER() {
        super(5);
    }
}
