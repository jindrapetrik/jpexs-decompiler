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
import com.jpexs.asdec.tags.base.BoundedTag;
import com.jpexs.asdec.tags.base.CharacterTag;
import com.jpexs.asdec.types.MATRIX;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.TEXTRECORD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 * @author JPEXS
 */
public class DefineTextTag extends CharacterTag implements BoundedTag {

   public int characterID;
   public RECT textBounds;
   public MATRIX textMatrix;
   public int glyphBits;
   public int advanceBits;
   public List<TEXTRECORD> textRecords;

   @Override
   public int getCharacterID() {
      return characterID;
   }

   public DefineTextTag(int characterID, RECT textBounds, MATRIX textMatrix, int glyphBits, int advanceBits, List<TEXTRECORD> textRecords) {
      super(11, "DefineText", new byte[0], 0);
      this.characterID = characterID;
      this.textBounds = textBounds;
      this.textMatrix = textMatrix;
      this.glyphBits = glyphBits;
      this.advanceBits = advanceBits;
      this.textRecords = textRecords;
   }

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
         sos.writeUI16(characterID);
         sos.writeRECT(textBounds);
         sos.writeMatrix(textMatrix);
         sos.writeUI8(glyphBits);
         sos.writeUI8(advanceBits);
         for (TEXTRECORD tr : textRecords) {
            sos.writeTEXTRECORD(tr, false, glyphBits, advanceBits);
         }
         sos.writeUI8(0);
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
   public DefineTextTag(byte data[], int version, long pos) throws IOException {
      super(11, "DefineText", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      characterID = sis.readUI16();
      textBounds = sis.readRECT();
      textMatrix = sis.readMatrix();
      glyphBits = sis.readUI8();
      advanceBits = sis.readUI8();
      textRecords = new ArrayList<TEXTRECORD>();
      TEXTRECORD tr;
      while ((tr = sis.readTEXTRECORD(false, glyphBits, advanceBits)) != null) {
         textRecords.add(tr);
      }
   }

   @Override
   public RECT getRect(HashMap<Integer,CharacterTag> characters) {
      return textBounds;
   }

   @Override
   public Set<Integer> getNeededCharacters() {
      Set<Integer> ret = new HashSet<Integer>();
      for (TEXTRECORD tr : textRecords) {
         if (tr.styleFlagsHasFont) {
            ret.add(tr.fontId);
         }
      }
      return ret;
   }
}
