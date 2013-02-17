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
package com.jpexs.decompiler.flash.types.shaperecords;

/**
 *
 * @author JPEXS
 */
public class StraightEdgeRecord extends SHAPERECORD {

   public int typeFlag = 1;
   public int straightFlag = 1;
   public int numBits;
   public boolean generalLineFlag;
   public boolean vertLineFlag;
   public int deltaX;
   public int deltaY;

   @Override
   public String toString() {
      return "[StraightEdgeRecord numBits=" + numBits + ", generalLineFlag=" + generalLineFlag + ", vertLineFlag=" + vertLineFlag + ", deltaX=" + deltaX + ", deltaY=" + deltaY + "]";
   }

   @Override
   public String toSWG(int oldX, int oldY) {
      if (generalLineFlag) {
         return "L " + twipToPixel(oldX + deltaX) + " " + twipToPixel(oldY + deltaY);
      } else if (vertLineFlag) {
         return "V " + twipToPixel(oldY + deltaY);
      } else {
         return "H " + twipToPixel(oldX + deltaX);
      }
   }

   @Override
   public int changeX(int x) {
      if (generalLineFlag) {
         return x + deltaX;
      } else if (vertLineFlag) {
         return x;
      } else {
         return x + deltaX;
      }
   }

   @Override
   public int changeY(int y) {
      if (generalLineFlag) {
         return y + deltaY;
      } else if (vertLineFlag) {
         return y + deltaY;
      } else {
         return y;
      }
   }

   @Override
   public void flip() {
      deltaX = -deltaX;
      deltaY = -deltaY;
   }

   @Override
   public boolean isMove() {
      return true;
   }
}
