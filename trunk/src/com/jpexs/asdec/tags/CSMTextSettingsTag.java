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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author JPEXS
 */
public class CSMTextSettingsTag extends Tag {

   public int textID;
   public int useFlashType;
   public int gridFit;
   public double thickness;
   public double sharpness;

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
         sos.writeUI16(textID);
         sos.writeUB(2, useFlashType);
         sos.writeUB(3, gridFit);
         sos.writeUB(3, 0);
         sos.writeFIXED(thickness); //TODO:write F32
         sos.writeFIXED(sharpness); //TODO:write F32
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
   public CSMTextSettingsTag(byte data[], int version, long pos) throws IOException {
      super(74, "CSMTextSettings", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      textID = sis.readUI16();
      useFlashType = (int) sis.readUB(2);
      gridFit = (int) sis.readUB(3);
      sis.readUB(3); //reserved
      thickness = sis.readFIXED(); //TODO: read F32
      sharpness = sis.readFIXED(); //TODO: read F32
      sis.readUI8(); //reserved
   }

   /**
    * Returns string representation of the object
    *
    * @return String representation of the object
    */
   @Override
   public String toString() {
      return "CSMTextSettings";
   }
}
