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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import java.io.*;

/**
 * Defines a series of ActionScript 3 bytecodes to be executed
 */
public class DoABCTag extends Tag implements Comparable<DoABCTag> {

   /**
    * ActionScript 3 bytecodes
    */
   public ABC abc;
   /**
    * A 32-bit flags value, which may contain the following bits set:
    * kDoAbcLazyInitializeFlag = 1: Indicates that the ABC block should not be
    * executed immediately, but only parsed. A later finddef may cause its
    * scripts to execute.
    */
   public long flags;
   /**
    * The name assigned to the bytecode.
    */
   public String name;

   @Override
   public String getName() {
      return "DoABC (" + name + ")";
   }

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public DoABCTag(byte[] data, int version, long pos) throws IOException {
      super(82, "DoABC", data, pos);
      InputStream is = new ByteArrayInputStream(data);
      SWFInputStream sis = new SWFInputStream(is, version);
      flags = sis.readUI32();
      name = sis.readString();
      abc = new ABC(is);
   }

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         OutputStream os = bos;
         if (Main.DEBUG_COPY) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
         }
         SWFOutputStream sos = new SWFOutputStream(os, version);
         sos.writeUI32(flags);
         sos.writeString(name);
         abc.saveToStream(sos);
         sos.close();
         return bos.toByteArray();
      } catch (IOException e) {
      }
      return new byte[0];
   }

   public int compareTo(DoABCTag n) {
      int lastCmp = name.compareTo(n.name);
      return (lastCmp != 0 ? lastCmp
              : name.compareTo(n.name));
   }
}
