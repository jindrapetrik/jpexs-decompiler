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
 * Represents 32-bit red, green, blue and alpha value
 *
 * @author JPEXS
 */
public class RGBA {

   /**
    * Red color value
    */
   public int red;
   /**
    * Green color value
    */
   public int green;
   /**
    * Blue color value
    */
   public int blue;
   /**
    * Alpha value defining opacity
    */
   public int alpha;
   
   public String toHexRGB(){
      String rh=Integer.toHexString(red);
      if(rh.length()<2){
         rh="0"+rh;
      }
      String gh=Integer.toHexString(green);
      if(gh.length()<2){
         gh="0"+gh;
      }
      String bh=Integer.toHexString(blue);
      if(bh.length()<2){
         bh="0"+bh;
      }
      return "#"+rh+gh+bh;
   }
}
