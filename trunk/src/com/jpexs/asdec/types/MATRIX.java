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
package com.jpexs.asdec.types;

/**
 * Represents a standard 2x3 transformation matrix of the sort commonly used in
 * 2D graphics
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
   public int scaleX;
   /**
    * Y scale value
    */
   public int scaleY;
   /**
    * Has rotate and skew values
    */
   public boolean hasRotate;
   /**
    * First rotate and skew value
    */
   public int rotateSkew0;
   /**
    * Second rotate and skew value
    */
   public int rotateSkew1;
   /**
    * X translate value in twips
    */
   public int translateX;
   /**
    * Y translate value in twips
    */
   public int translateY;   

   public int bitsTranslate;
   public int bitsRotate;
   public int bitsScale;
   @Override
   public String toString() {
      return "[MATRIX scale:"+scaleX+","+scaleY+", rotate:"+rotateSkew0+","+rotateSkew1+", translate:"+translateX+","+translateY+"]";
   }
   
   
}
