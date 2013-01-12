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
import com.jpexs.asdec.types.ZONERECORD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DefineFontAlignZonesTag extends Tag {

   public int fontID;
   public int CSMTableHint;
   public List<ZONERECORD> zoneTable;

   public DefineFontAlignZonesTag(byte[] data, int version, long pos) throws IOException {
      super(73, "DefineFontAlignZones",data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      fontID = sis.readUI16();
      CSMTableHint = (int) sis.readUB(2);
      sis.readUB(6);
      zoneTable = new ArrayList<ZONERECORD>();
      while (sis.available() > 0) {
         ZONERECORD zr = sis.readZONERECORD();
         zoneTable.add(zr);
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
      try {
         sos.writeUI16(fontID);
         sos.writeUB(2, CSMTableHint);
         sos.writeUB(6, 0);
         for (ZONERECORD z : zoneTable) {
            sos.writeZONERECORD(z);
         }
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }

}
