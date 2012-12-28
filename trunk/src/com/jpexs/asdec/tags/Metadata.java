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

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metadata extends Tag {

   private String xmlMetadata;

   public Metadata(byte[] data, long pos) {
      super(77, data, pos);
      try {
         xmlMetadata = new String(data, "UTF-8");
      } catch (UnsupportedEncodingException e) {
         //ignore
      }
   }

   @Override
   public String toString() {
      return "Metadata";
   }
}
