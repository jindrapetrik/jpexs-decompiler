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
import com.jpexs.asdec.types.LANGCODE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class DefineFontInfo2Tag extends Tag {

   public int fontID;
   public String fontName;
   public boolean fontFlagsSmallText;
   public boolean fontFlagsShiftJIS;
   public boolean fontFlagsANSI;
   public boolean fontFlagsItalic;
   public boolean fontFlagsBold;
   public boolean fontFlagsWideCodes;
   public LANGCODE languageCode;
   public int codeTable[];

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
         sos.writeUI16(fontID);
         sos.writeUI8(fontName.getBytes().length);
         sos.write(fontName.getBytes());
         sos.writeUB(2, 0);
         sos.writeUB(1, fontFlagsSmallText ? 1 : 0);
         sos.writeUB(1, fontFlagsShiftJIS ? 1 : 0);
         sos.writeUB(1, fontFlagsANSI ? 1 : 0);
         sos.writeUB(1, fontFlagsItalic ? 1 : 0);
         sos.writeUB(1, fontFlagsBold ? 1 : 0);
         sos.writeUB(1, fontFlagsWideCodes ? 1 : 0);
         sos.writeLANGCODE(languageCode);
         for (int i = 0; i < codeTable.length; i++) {
            sos.writeUI16(codeTable[i]);
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
   public DefineFontInfo2Tag(byte data[], int version, long pos) throws IOException {
      super(62, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      fontID = sis.readUI16();
      int fontNameLen = sis.readUI8();
      fontName = new String(sis.readBytes(fontNameLen));
      sis.readUB(2);//reserved
      fontFlagsSmallText = sis.readUB(1) == 1;
      fontFlagsShiftJIS = sis.readUB(1) == 1;
      fontFlagsANSI = sis.readUB(1) == 1;
      fontFlagsItalic = sis.readUB(1) == 1;
      fontFlagsBold = sis.readUB(1) == 1;
      fontFlagsWideCodes = sis.readUB(1) == 1;
      languageCode = sis.readLANGCODE();
      codeTable = new int[sis.available() / 2];
      for (int i = 0; i < codeTable.length; i++) {
         codeTable[i] = sis.readUI16();
      }
   }

   /**
    * Returns string representation of the object
    *
    * @return String representation of the object
    */
   @Override
   public String toString() {
      return "DefineFontInfo2";
   }
}
