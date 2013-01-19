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


package com.jpexs.asdec.types.shaperecords;

/**
 *
 * @author JPEXS
 */
public class CurvedEdgeRecord implements SHAPERECORD {

   public int typeFlag = 1;
   public int straightFlag = 0;
   public int numBits;
   public int controlDeltaX;
   public int controlDeltaY;
   public int anchorDeltaX;
   public int anchorDeltaY;

   @Override
   public String toString() {
      return "[CurvedEdgeRecord numBits=" + numBits + ", controlDeltaX=" + controlDeltaX + ", controlDeltaY=" + controlDeltaY + ", anchorDeltaX=" + anchorDeltaX + ". anchorDeltaY=" + anchorDeltaY + "]";
   }
}
