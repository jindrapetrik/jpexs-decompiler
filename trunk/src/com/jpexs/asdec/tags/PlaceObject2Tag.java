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

import com.jpexs.asdec.tags.base.Container;
import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.types.CLIPACTIONS;
import com.jpexs.asdec.types.CXFORMWITHALPHA;
import com.jpexs.asdec.types.MATRIX;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends the functionality of the PlaceObject2Tag
 *
 * @author JPEXS
 */
public class PlaceObject2Tag extends Tag implements Container {

   /**
    * @since SWF 5 Has clip actions (sprite characters only)
    */
   public boolean placeFlagHasClipActions;
   /**
    * Has clip depth
    */
   public boolean placeFlagHasClipDepth;
   /**
    * Has name
    */
   public boolean placeFlagHasName;
   /**
    * Has ratio
    */
   public boolean placeFlagHasRatio;
   /**
    * Has color transform
    */
   public boolean placeFlagHasColorTransform;
   /**
    * Has matrix
    */
   public boolean placeFlagHasMatrix;
   /**
    * Places a character
    */
   public boolean placeFlagHasCharacter;
   /**
    * Defines a character to be moved
    */
   public boolean placeFlagMove;
   /**
    * Depth of character
    */
   public int depth;
   /**
    * If PlaceFlagHasCharacter, ID of character to place
    */
   public int characterId;
   /**
    * If PlaceFlagHasMatrix, Transform matrix data
    */
   public MATRIX matrix;
   /**
    * If PlaceFlagHasColorTransform, Color transform data
    */
   public CXFORMWITHALPHA colorTransform;
   /**
    * If PlaceFlagHasRatio, ratio
    */
   public int ratio;
   /**
    * If PlaceFlagHasName, Name of character
    */
   public String name;
   /**
    * If PlaceFlagHasClipDepth, Clip depth
    */
   public int clipDepth;
   /**
    * @since SWF 5 If PlaceFlagHasClipActions, Clip Actions Data
    */
   public CLIPACTIONS clipActions;

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      if (Main.DISABLE_DANGEROUS) {
         return super.getData(version);
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream os = baos;
      if (Main.DEBUG_COPY) {
         os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
      }
      SWFOutputStream sos = new SWFOutputStream(os, version);
      try {
         sos.writeUB(1, placeFlagHasClipActions ? 1 : 0);
         sos.writeUB(1, placeFlagHasClipDepth ? 1 : 0);
         sos.writeUB(1, placeFlagHasName ? 1 : 0);
         sos.writeUB(1, placeFlagHasRatio ? 1 : 0);
         sos.writeUB(1, placeFlagHasColorTransform ? 1 : 0);
         sos.writeUB(1, placeFlagHasMatrix ? 1 : 0);
         sos.writeUB(1, placeFlagHasCharacter ? 1 : 0);
         sos.writeUB(1, placeFlagMove ? 1 : 0);
         sos.writeUI16(depth);
         if (placeFlagHasCharacter) {
            sos.writeUI16(characterId);
         }
         if (placeFlagHasMatrix) {
            sos.writeMatrix(matrix);
         }
         if (placeFlagHasColorTransform) {
            sos.writeCXFORMWITHALPHA(colorTransform);
         }
         if (placeFlagHasRatio) {
            sos.writeUI16(ratio);
         }
         if (placeFlagHasName) {
            sos.writeString(name);
         }
         if (placeFlagHasClipDepth) {
            sos.writeUI16(clipDepth);
         }
         if (placeFlagHasClipActions) {
            sos.writeCLIPACTIONS(clipActions);
         }
         sos.close();
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
   public PlaceObject2Tag(byte data[], int version, long pos) throws IOException {
      super(26, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      placeFlagHasClipActions = sis.readUB(1) == 1;
      placeFlagHasClipDepth = sis.readUB(1) == 1;
      placeFlagHasName = sis.readUB(1) == 1;
      placeFlagHasRatio = sis.readUB(1) == 1;
      placeFlagHasColorTransform = sis.readUB(1) == 1;
      placeFlagHasMatrix = sis.readUB(1) == 1;
      placeFlagHasCharacter = sis.readUB(1) == 1;
      placeFlagMove = sis.readUB(1) == 1;
      depth = sis.readUI16();
      if (placeFlagHasCharacter) {
         characterId = sis.readUI16();
      }
      if (placeFlagHasMatrix) {
         matrix = sis.readMatrix();
      }
      if (placeFlagHasColorTransform) {
         colorTransform = sis.readCXFORMWITHALPHA();
      }
      if (placeFlagHasRatio) {
         ratio = sis.readUI16();
      }
      if (placeFlagHasName) {
         name = sis.readString();
      }
      if (placeFlagHasClipDepth) {
         clipDepth = sis.readUI16();
      }
      if (placeFlagHasClipActions) {
         clipActions = sis.readCLIPACTIONS();
      }
   }

   /**
    * Returns string representation of the object
    *
    * @return String representation of the object
    */
   @Override
   public String toString() {
      return "PlaceObject2Tag";
   }

   /**
    * Returns all sub-items
    *
    * @return List of sub-items
    */
   public List<Object> getSubItems() {
      List<Object> ret = new ArrayList<Object>();
      if (placeFlagHasClipActions) {
         ret.addAll(clipActions.clipActionRecords);
      }
      return ret;
   }

   /**
    * Returns number of sub-items
    *
    * @return Number of sub-items
    */
   public int getItemCount() {
      if (!placeFlagHasClipActions) {
         return 0;
      }
      return clipActions.clipActionRecords.size();
   }
}
