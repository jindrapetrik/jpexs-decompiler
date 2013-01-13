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
import com.jpexs.asdec.tags.base.BoundedTag;
import com.jpexs.asdec.tags.base.CharacterTag;
import com.jpexs.asdec.types.MORPHFILLSTYLEARRAY;
import com.jpexs.asdec.types.MORPHLINESTYLEARRAY;
import com.jpexs.asdec.types.RECT;
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
public class DefineMorphShapeTag extends CharacterTag implements BoundedTag {

   public int characterId;
   public RECT startBounds;
   public RECT endBounds;
   public MORPHFILLSTYLEARRAY morphFillStyles;
   public MORPHLINESTYLEARRAY morphLineStyles;
   public SHAPE startEdges;
   public SHAPE endEdges;

   @Override
   public int getCharacterID() {
      return characterId;
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
         sos.writeUI16(characterId);
         sos.writeRECT(startBounds);
         sos.writeRECT(endBounds);
         ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
         SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
         sos2.writeMORPHFILLSTYLEARRAY(morphFillStyles, 1);
         sos2.writeMORPHLINESTYLEARRAY(morphLineStyles, 1);
         sos2.writeSHAPE(startEdges, 1);
         byte d[] = baos2.toByteArray();
         sos.writeUI32(d.length);
         sos.write(d);
         sos.writeSHAPE(endEdges, 1);

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
   public DefineMorphShapeTag(byte data[], int version, long pos) throws IOException {
      super(46, "DefineMorphShape", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      characterId = sis.readUI16();
      startBounds = sis.readRECT();
      endBounds = sis.readRECT();
      long offset = sis.readUI32(); //ignore
      morphFillStyles = sis.readMORPHFILLSTYLEARRAY();
      morphLineStyles = sis.readMORPHLINESTYLEARRAY(1);
      startEdges = sis.readSHAPE(1);
      endEdges = sis.readSHAPE(1);
   }

   @Override
   public RECT getRect() {
      RECT rect = new RECT();
      rect.Xmin = Math.min(startBounds.Xmin, endBounds.Xmin);
      rect.Ymin = Math.min(startBounds.Ymin, endBounds.Ymin);
      rect.Xmax = Math.max(startBounds.Xmax, endBounds.Xmax);
      rect.Ymax = Math.max(startBounds.Ymax, endBounds.Ymax);
      return rect;
   }
}
