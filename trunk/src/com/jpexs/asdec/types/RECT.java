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


package com.jpexs.asdec.types;

/**
 * A rectangle value represents a rectangular region.
 *
 * @author JPEXS
 */
public class RECT {

   /**
    * X minimum position for rectangle in twips
    */
   public int Xmin;
   /**
    * X maximum position for rectangle in twips
    */
   public int Xmax;
   /**
    * Y minimum position for rectangle in twips
    */
   public int Ymin;
   /**
    * Y maximum position for rectangle in twips
    */
   public int Ymax;
   public int nbits;

   public RECT(int Xmin, int Xmax, int Ymin, int Ymax) {
      this.Xmin = Xmin;
      this.Xmax = Xmax;
      this.Ymin = Ymin;
      this.Ymax = Ymax;
   }

   public RECT() {
   }

   @Override
   public String toString() {
      return "[RECT x=" + Xmin + "-" + Xmax + ", y=" + Ymin + "-" + Ymax + "]";
   }

   public int getWidth() {
      return Xmax - Xmin;
   }

   public int getHeight() {
      return Ymax - Ymin;
   }
}
