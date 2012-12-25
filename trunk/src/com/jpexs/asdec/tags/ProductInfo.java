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
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ProductInfo extends Tag {

   private long productID;
   private long edition;
   private int majorVersion;
   private int minorVersion;
   private long buildLow;
   private long buildHigh;
   private long compilationDate;

   public ProductInfo(byte[] data, int version, long pos) throws IOException {
      super(41, data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      /*
       * 0: Unknown
       * 1: Macromedia Flex for J2EE
       * 2: Macromedia Flex for .NET
       * 3: Adobe Flex
       */
      productID = sis.readUI32();

      /*
       * 0: Developer Edition
       * 1: Full Commercial Edition
       * 2: Non Commercial Edition
       * 3: Educational Edition
       * 4: Not For Resale (NFR) Edition
       * 5: Trial Edition
       * 6: None
       */
      edition = sis.readUI32();
      majorVersion = sis.readUI8();
      minorVersion = sis.readUI8();
      buildLow = sis.readUI32();
      buildHigh = sis.readUI32();
      compilationDate = sis.readUI32() & 0xffffffffL;
      compilationDate |= sis.readUI32() << 32;
   }

   @Override
   public String toString() {
      return "ProductInfo";
   }
}
