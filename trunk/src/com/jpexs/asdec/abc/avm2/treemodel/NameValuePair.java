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


package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import java.util.HashMap;
import java.util.List;

public class NameValuePair extends TreeItem {

   public TreeItem name;
   public TreeItem value;

   public NameValuePair(TreeItem name, TreeItem value) {
      super(name.instruction, NOPRECEDENCE);
      this.name = name;
      this.value = value;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      if (name instanceof StringTreeItem) {
         return ((StringTreeItem) name).value + ":" + value.toString(constants, localRegNames, fullyQualifiedNames);
      }
      return name.toString(constants, localRegNames, fullyQualifiedNames) + ":" + value.toString(constants, localRegNames, fullyQualifiedNames);
   }
}
