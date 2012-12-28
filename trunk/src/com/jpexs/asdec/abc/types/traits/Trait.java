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
package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.Namespace;
import com.jpexs.asdec.helpers.Helper;

public class Trait {

   public int name_index;
   public int kindType;
   public int kindFlags;
   public int metadata[] = new int[0];
   public long fileOffset;
   public byte bytes[];
   public static final int ATTR_Final = 0x1;
   public static final int ATTR_Override = 0x2;
   public static final int ATTR_Metadata = 0x4;
   public static final int TRAIT_SLOT = 0;
   public static final int TRAIT_METHOD = 1;
   public static final int TRAIT_GETTER = 2;
   public static final int TRAIT_SETTER = 3;
   public static final int TRAIT_CLASS = 4;
   public static final int TRAIT_FUNCTION = 5;
   public static final int TRAIT_CONST = 6;

   public String getModifiers(ABC abc, boolean isStatic) {
      String ret = "";
      if ((kindFlags & ATTR_Override) > 0) {
         ret += "override";
      }
      Multiname m = getMultiName(abc.constants);
      if (m != null) {
         int v = abc.nsValueToName(m.namespace_index);
         if (v > -1) {
            ret += " " + abc.constants.constant_multiname[v].getName(abc.constants);
         }
         Namespace ns = m.getNamespace(abc.constants);
         if (ns != null) {
            ret += " " + ns.getPrefix(abc);
         }
      }
      if (isStatic) {
         ret += " static";
      }
      if ((kindFlags & ATTR_Final) > 0) {
         if (!isStatic) {
            ret += " final";
         }
      }
      return ret.trim();
   }

   @Override
   public String toString() {
      return "name_index=" + name_index + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
   }

   public String toString(ABC abc) {
      return abc.constants.constant_multiname[name_index].toString(abc.constants) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
   }

   public String convert(MethodInfo[] methodInfo, ABC abc) {
      return convert(methodInfo, abc, false);
   }

   public String convert(MethodInfo[] methodInfo, ABC abc, boolean isStatic) {
      return abc.constants.constant_multiname[name_index].toString(abc.constants) + " kind=" + kindType + " metadata=" + Helper.intArrToString(metadata);
   }

   public Multiname getMultiName(ConstantPool constants) {
      if (name_index == 0) {
         return null;
      } else {
         return constants.constant_multiname[name_index];
      }
   }
}
