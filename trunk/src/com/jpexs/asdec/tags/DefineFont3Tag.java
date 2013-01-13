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
import com.jpexs.asdec.abc.CopyOutputStream;
import com.jpexs.asdec.tags.base.FontTag;
import com.jpexs.asdec.types.KERNINGRECORD;
import com.jpexs.asdec.types.LANGCODE;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.SHAPE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DefineFont3Tag extends Tag implements FontTag {

   public int fontId;
   public boolean fontFlagsHasLayout;
   public boolean fontFlagsShiftJIS;
   public boolean fontFlagsSmallText;
   public boolean fontFlagsANSI;
   public boolean fontFlagsWideOffsets;
   public boolean fontFlagsWideCodes;
   public boolean fontFlagsItalic;
   public boolean fontFlagsBold;
   public LANGCODE languageCode;
   public String fontName;
   public int numGlyphs;
   public long offsetTable[];
   public SHAPE glyphShapeTable[];
   public int codeTable[];
   public int fontAscent;
   public int fontDescent;
   public int fontLeading;
   public int fontAdvanceTable[];
   public RECT fontBoundsTable[];
   public KERNINGRECORD fontKerningTable[];

   public DefineFont3Tag(byte[] data, int version, long pos) throws IOException {
      super(75, "DefineFont3", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      fontId = sis.readUI16();
      fontFlagsHasLayout = sis.readUB(1) == 1;
      fontFlagsShiftJIS = sis.readUB(1) == 1;
      fontFlagsSmallText = sis.readUB(1) == 1;
      fontFlagsANSI = sis.readUB(1) == 1;
      fontFlagsWideOffsets = sis.readUB(1) == 1;
      fontFlagsWideCodes = sis.readUB(1) == 1;
      fontFlagsItalic = sis.readUB(1) == 1;
      fontFlagsBold = sis.readUB(1) == 1;
      languageCode = sis.readLANGCODE();
      int fontNameLen = sis.readUI8();
      fontName = new String(sis.readBytes(fontNameLen));
      numGlyphs = sis.readUI16();
      offsetTable = new long[numGlyphs];
      for (int i = 0; i < numGlyphs; i++) {
         if (fontFlagsWideOffsets) {
            offsetTable[i] = sis.readUI32();
         } else {
            offsetTable[i] = sis.readUI16();
         }
      }
      long codeTableOffset;
      if (fontFlagsWideOffsets) {
         codeTableOffset = sis.readUI32();
      } else {
         codeTableOffset = sis.readUI16();
      }
      glyphShapeTable = new SHAPE[numGlyphs];
      for (int i = 0; i < numGlyphs; i++) {
         glyphShapeTable[i] = sis.readSHAPE(1);
      }
      codeTable = new int[numGlyphs];
      for (int i = 0; i < numGlyphs; i++) {
         if (fontFlagsWideCodes) {
            codeTable[i] = sis.readUI16();
         } else {
            codeTable[i] = sis.readUI8();
         }
      }
      if (fontFlagsHasLayout) {
         fontAscent = sis.readSI16();
         fontDescent = sis.readSI16();
         fontLeading = sis.readSI16();
         fontAdvanceTable = new int[numGlyphs];
         for (int i = 0; i < numGlyphs; i++) {
            fontAdvanceTable[i] = sis.readSI16();
         }
         fontBoundsTable = new RECT[numGlyphs];
         for (int i = 0; i < numGlyphs; i++) {
            fontBoundsTable[i] = sis.readRECT();
         }
         int kerningCount = sis.readUI16();
         fontKerningTable = new KERNINGRECORD[kerningCount];
         for (int i = 0; i < kerningCount; i++) {
            fontKerningTable[i] = sis.readKERNINGRECORD(fontFlagsWideCodes);
         }
      }
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
      sos = new SWFOutputStream(new CopyOutputStream(sos, new ByteArrayInputStream(data)), 10);
      try {
         sos.writeUI16(fontId);
         sos.writeUB(1, fontFlagsHasLayout ? 1 : 0);
         sos.writeUB(1, fontFlagsShiftJIS ? 1 : 0);
         sos.writeUB(1, fontFlagsSmallText ? 1 : 0);
         sos.writeUB(1, fontFlagsANSI ? 1 : 0);
         sos.writeUB(1, fontFlagsWideOffsets ? 1 : 0);
         sos.writeUB(1, fontFlagsWideCodes ? 1 : 0);
         sos.writeUB(1, fontFlagsItalic ? 1 : 0);
         sos.writeUB(1, fontFlagsBold ? 1 : 0);
         sos.writeLANGCODE(languageCode);
         sos.writeUI8(fontName.getBytes().length);
         sos.write(fontName.getBytes());
         sos.writeUI16(numGlyphs);

         ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
         SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
         for (int i = 0; i < numGlyphs; i++) {
            if (fontFlagsWideOffsets) {
               sos2.writeUI32(offsetTable[i]);
            } else {
               sos2.writeUI16((int) offsetTable[i]);
            }
         }
         byte ba2[] = baos2.toByteArray();
         ByteArrayOutputStream baos3 = new ByteArrayOutputStream();

         SWFOutputStream sos3 = new SWFOutputStream(baos3, version);
         for (int i = 0; i < numGlyphs; i++) {
            sos3.writeSHAPE(glyphShapeTable[i], 1);
         }
         byte ba3[] = baos3.toByteArray();
         sos.write(ba2);
         //codetableoffset 881         
         if (fontFlagsWideOffsets) {
            long offset = ba2.length + ba3.length + 4;
            sos.writeUI32(offset);
         } else {
            long offset = ba2.length + ba3.length + 2;
            sos.writeUI16((int) offset);
         }
         sos.write(ba3);


         for (int i = 0; i < numGlyphs; i++) {
            if (fontFlagsWideCodes) {
               sos.writeUI16(codeTable[i]);
            } else {
               sos.writeUI8(codeTable[i]);
            }
         }
         if (fontFlagsHasLayout) {
            sos.writeSI16(fontAscent);
            sos.writeSI16(fontDescent);
            sos.writeSI16(fontLeading);
            for (int i = 0; i < numGlyphs; i++) {
               sos.writeSI16(fontAdvanceTable[i]);
            }
            for (int i = 0; i < numGlyphs; i++) {
               sos.writeRECT(fontBoundsTable[i]);
            }
            sos.writeUI16(fontKerningTable.length);
            for (int k = 0; k < fontKerningTable.length; k++) {
               sos.writeKERNINGRECORD(fontKerningTable[k], fontFlagsWideCodes);
            }
         }

      } catch (IOException e) {
      }
      return baos.toByteArray();
   }
}
