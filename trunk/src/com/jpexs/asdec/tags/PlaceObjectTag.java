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


package com.jpexs.asdec.tags;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.types.CXFORM;
import com.jpexs.asdec.types.MATRIX;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds character to the display list
 *
 * @author JPEXS
 */
public class PlaceObjectTag extends Tag {

   /**
    * ID of character to place
    */
   public int characterId;
   /**
    * Depth of character
    */
   public int depth;
   /**
    * Transform matrix data
    */
   public MATRIX matrix;
   /**
    * Color transform data
    */
   public CXFORM colorTransform;

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream os = baos;
      SWFOutputStream sos = new SWFOutputStream(os, version);
      try {
         sos.writeUI16(characterId);
         sos.writeUI16(depth);
         sos.writeMatrix(matrix);
         if (colorTransform != null) {
            sos.writeCXFORM(colorTransform);
         }
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public PlaceObjectTag(byte data[], int version, long pos) throws IOException {
      super(4, "PlaceObject", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      characterId = sis.readUI16();
      depth = sis.readUI16();
      matrix = sis.readMatrix();
      if (sis.available() > 0) {
         colorTransform = sis.readCXFORM();
      }
   }

   public PlaceObjectTag(int characterId, int depth, MATRIX matrix, CXFORM colorTransform) {
      super(4, "PlaceObject", new byte[0], 0);
      this.characterId = characterId;
      this.depth = depth;
      this.matrix = matrix;
      this.colorTransform = colorTransform;
   }

   @Override
   public Set<Integer> getNeededCharacters() {
      Set<Integer> ret = new HashSet<Integer>();
      ret.add(characterId);
      return ret;
   }
}
