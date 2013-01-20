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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes portions of a SWF file available for import by other SWF files
 *
 * @author JPEXS
 */
public class ExportAssetsTag extends Tag {

   /**
    * HashMap with assets
    */
   public List<Integer> tags;
   public List<String> names;

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public ExportAssetsTag(byte[] data, int version, long pos) throws IOException {
      super(56, "ExportAssets", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      int count = sis.readUI16();
      tags = new ArrayList<Integer>();
      names = new ArrayList<String>();
      for (int i = 0; i < count; i++) {
         int characterId = sis.readUI16();
         tags.add(characterId);
         String name = sis.readString();
         names.add(name);
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
         sos.writeUI16(tags.size());
         for (int i = 0; i < tags.size(); i++) {
            sos.writeUI16(tags.get(i));
            sos.writeString(names.get(i));
         }
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }
}
