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

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.tags.base.FontTag;
import com.jpexs.asdec.types.SHAPE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineFontTag extends Tag implements FontTag {

   public int fontId;
   public int offsetTable[];
   public SHAPE glyphShapeTable[];

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
         sos.writeUI16(fontId);
         for (int offset : offsetTable) {
            sos.writeUI16(offset);
         }
         for (SHAPE shape : glyphShapeTable) {
            sos.writeSHAPE(shape, 1);
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
   public DefineFontTag(byte data[], int version, long pos) throws IOException {
      super(10, "DefineFont", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      fontId = sis.readUI16();
      int firstOffset = sis.readUI16();
      int nGlyphs = firstOffset / 2;
      offsetTable = new int[nGlyphs];
      glyphShapeTable = new SHAPE[nGlyphs];
      offsetTable[0] = firstOffset;
      for (int i = 1; i < nGlyphs; i++) {
         offsetTable[i] = sis.readUI16();
      }
      for (int i = 0; i < nGlyphs; i++) {
         glyphShapeTable[i] = sis.readSHAPE(1);
      }
   }

   @Override
   public int getFontId() {
      return fontId;
   }

   @Override
   public SHAPE[] getGlyphShapeTable() {
      return glyphShapeTable;
   }
}
