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
 * The Bevel filter creates a smooth bevel on display list objects.
 *
 * @author JPEXS
 */
public class BEVELFILTER extends FILTER {

   /**
    * Color of the shadow
    */
   public RGBA shadowColor;
   /**
    * Color of the highlight
    */
   public RGBA highlightColor;
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
   public BEVELFILTER() {
      super(3);
   }
}
