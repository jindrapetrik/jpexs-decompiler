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
 * Defines a transform that can be applied to the color space of a graphic
 * object.
 *
 * @author JPEXS
 */
public class CXFORM {

   /**
    * Has color addition values
    */
   public boolean hasAddTerms;
   /**
    * Has color multiply values
    */
   public boolean hasMultTerms;
   public int nbits;
   /**
    * Red multiply value
    */
   public int redMultTerm;
   /**
    * Green multiply value
    */
   public int greenMultTerm;
   /**
    * Blue multiply value
    */
   public int blueMultTerm;
   /**
    * Red addition value
    */
   public int redAddTerm;
   /**
    * Green addition value
    */
   public int greenAddTerm;
   /**
    * Blue addition value
    */
   public int blueAddTerm;
}
