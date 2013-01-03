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

public class FileAttributesTag extends Tag {

   private boolean useDirectBlit;
   private boolean useGPU;
   private boolean hasMetadata;
   private boolean actionScript3;
   private boolean useNetwork;

   public FileAttributesTag(byte[] data, int version, long pos) throws IOException {
      super(69, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      sis.readUB(1); // reserved
      // UB[1] == 0  (reserved)
      useDirectBlit = sis.readUB(1) != 0;
      useGPU = sis.readUB(1) != 0;
      hasMetadata = sis.readUB(1) != 0;
      actionScript3 = sis.readUB(1) != 0;
      sis.readUB(2); // reserved
      useNetwork = sis.readUB(1) != 0;
      // UB[24] == 0 (reserved)
      sis.readUB(24); //reserved
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
         sos.writeUB(1, 0); //reserved
         if(useDirectBlit){
            sos.writeUB(1, 1);
         }
         if(useGPU){
            sos.writeUB(1, 1);
         }
         if(hasMetadata){
            sos.writeUB(1, 1);
         }
         if(actionScript3){
            sos.writeUB(1, 1);
         }
         sos.writeUB(2, 0); //reserved
         if(useNetwork){
            sos.writeUB(1, 1);
         }
         sos.writeUB(24, 0); //reserved
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }

   @Override
   public String toString() {
      return "FileAttributes";
   }
}
