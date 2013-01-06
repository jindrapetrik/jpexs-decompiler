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
package com.jpexs.asdec.tags;

import java.io.IOException;

/**
 * Extends the functionality of the PlaceObject2Tag
 *
 * @author JPEXS
 */
public class EndTag extends Tag {

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      return new byte[0];
   }

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public EndTag(byte data[], int version, long pos) throws IOException {
      super(0, data, pos);

   }

   public EndTag() {
      super(0, new byte[0], 0);
   }
   
   

   /**
    * Returns string representation of the object
    *
    * @return String representation of the object
    */
   @Override
   public String toString() {
      return "End";
   }
}
