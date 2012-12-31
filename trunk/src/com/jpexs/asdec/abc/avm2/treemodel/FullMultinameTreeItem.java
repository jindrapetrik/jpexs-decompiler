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
package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.types.Namespace;
import java.util.HashMap;
import java.util.List;

public class FullMultinameTreeItem extends TreeItem {

   public int multinameIndex;
   public TreeItem name;
   public TreeItem namespace;

   public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, TreeItem name) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.multinameIndex = multinameIndex;
      this.name = name;
      this.namespace = null;
   }

   public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.multinameIndex = multinameIndex;
      this.name = null;
      this.namespace = null;
   }

   public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, TreeItem name, TreeItem namespace) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.multinameIndex = multinameIndex;
      this.name = name;
      this.namespace = namespace;
   }

   public boolean isRuntime() {
      return (name != null) || (namespace != null);
   }

   public boolean isXML(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String cname;
      if (name != null) {
         cname = name.toString(constants, localRegNames, fullyQualifiedNames);
      } else {
         cname = (constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames));
      }
      String cns = "";
      if (namespace != null) {
         cns = namespace.toString(constants, localRegNames, fullyQualifiedNames);
      } else {
         Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
         if ((ns != null) && (ns.name_index != 0)) {
            cns = ns.getName(constants);
         }
      }
      return cname.equals("XML")&&cns.equals("");
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      String ret = "";
      if (name != null) {
         ret = "[" + name.toString(constants, localRegNames, fullyQualifiedNames) + "]";
      } else {
         ret = hilight(constants.constant_multiname[multinameIndex].getName(constants, fullyQualifiedNames));
      }
      if (namespace != null) {
         ret = namespace.toString(constants, localRegNames, fullyQualifiedNames) + "::" + ret;
      } else {
         /*Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
          if ((ns != null)&&(ns.name_index!=0)) {
          ret =  hilight(ns.getName(constants) + "::")+ret;
          }*/
      }
      return ret;
   }

   public boolean compareSame(FullMultinameTreeItem other) {
      if (multinameIndex != other.multinameIndex) {
         return false;
      }
      TreeItem tiName = name;
      while (tiName instanceof LocalRegTreeItem) {
         tiName = ((LocalRegTreeItem) tiName).computedValue;
      }

      TreeItem tiName2 = other.name;
      while (tiName2 instanceof LocalRegTreeItem) {
         tiName2 = ((LocalRegTreeItem) tiName2).computedValue;
      }
      if (tiName != tiName2) {
         return false;
      }

      TreeItem tiNameSpace = namespace;
      while (tiNameSpace instanceof LocalRegTreeItem) {
         tiNameSpace = ((LocalRegTreeItem) tiNameSpace).computedValue;
      }

      TreeItem tiNameSpace2 = other.namespace;
      while (tiNameSpace2 instanceof LocalRegTreeItem) {
         tiNameSpace2 = ((LocalRegTreeItem) tiNameSpace2).computedValue;
      }
      if (tiNameSpace != tiNameSpace2) {
         return false;
      }
      return true;
   }
}
