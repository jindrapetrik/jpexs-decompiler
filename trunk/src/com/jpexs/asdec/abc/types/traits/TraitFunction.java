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
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.helpers.Helper;
import java.util.Stack;

public class TraitFunction extends Trait {

   public int slot_index;
   public int method_info;

   @Override
   public String toString(ABC abc) {
      return "Function " + abc.constants.constant_multiname[name_index].toString(abc.constants) + " slot=" + slot_index + " method_info=" + method_info + " metadata=" + Helper.intArrToString(metadata);
   }

   @Override
   public String convertHeader(ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight) {
      String modifier = getModifiers(abc, isStatic) + " ";
      if (modifier.equals(" ")) {
         modifier = "";
      }
      MethodBody body = abc.findBody(method_info);
      return modifier + "function " + abc.constants.constant_multiname[name_index].getName(abc.constants) + "(" + abc.method_info[method_info].getParamStr(abc.constants, body, abc) + ") : " + abc.method_info[method_info].getReturnTypeStr(abc.constants);
   }

   @Override
   public String convert(ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight) {
      String header = convertHeader(abc, isStatic, pcode, classIndex, highlight);
      String bodyStr = "";
      int bodyIndex = abc.findBodyIndex(method_info);
      if (bodyIndex != -1) {
         bodyStr = ABC.addTabs(abc.bodies[bodyIndex].toString(pcode, isStatic, classIndex, abc, abc.constants, abc.method_info, new Stack<TreeItem>(), false, highlight), 3);
      }
      return ABC.IDENT_STRING + ABC.IDENT_STRING + header + (abc.instance_info[classIndex].isInterface() ? ";" : " {\r\n" + bodyStr + "\r\n" + ABC.IDENT_STRING + ABC.IDENT_STRING + "}");

   }
}
