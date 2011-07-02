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

package com.jpexs.asdec.types;

/**
 * Represents a standard 2x3 transformation matrix of the sort commonly used in 2D graphics
 *
 * @author JPEXS
 */
public class MATRIX {
    /**
     * Has scale values
     */
    public boolean hasScale;
    /**
     * X scale value
     */
    public double scaleX;
    /**
     * Y scale value
     */
    public double scaleY;
    /**
     * Has rotate and skew values
     */
    public boolean hasRotate;
    /**
     * First rotate and skew value
     */
    public double rotateSkew0;
    /**
     * Second rotate and skew value
     */
    public double rotateSkew1;
    /**
     * X translate value in twips
     */
    public long translateX;
    /**
     * Y translate value in twips
     */
    public long translateY;

    /**
     * Nbits used for store translate values
     */
    public int translateNBits;
    /**
     * Nbits used for store scale values
     */
    public int scaleNBits;
    /**
     * Nbits used for store rotate values
     */
    public int rotateNBits;
}
