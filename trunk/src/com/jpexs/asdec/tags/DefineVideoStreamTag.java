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
public class DefineVideoStreamTag extends Tag {

   public int characterId;
   public int numFrames;
   public int width;
   public int height;
   public int videoFlagsDeblocking;
   public boolean videoFlagsSmoothing;
   public int codecID;

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
         sos.writeUI16(numFrames);
         sos.writeUI16(width);
         sos.writeUI16(height);
         sos.writeUB(4, 0);
         sos.writeUB(3, videoFlagsDeblocking);
         sos.writeUB(1, videoFlagsSmoothing ? 1 : 0);
         sos.writeUI8(codecID);
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
   public DefineVideoStreamTag(byte data[], int version, long pos) throws IOException {
      super(60, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      characterId = sis.readUI16();
      numFrames = sis.readUI16();
      width = sis.readUI16();
      height = sis.readUI16();
      sis.readUB(4);
      videoFlagsDeblocking = (int) sis.readUB(3);
      videoFlagsSmoothing = sis.readUB(1) == 1;
      codecID = sis.readUI8();
   }

   /**
    * Returns string representation of the object
    *
    * @return String representation of the object
    */
   @Override
   public String toString() {
      return "DefineVideoStream";
   }
}
