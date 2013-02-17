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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import java.util.List;

public class Multiname {

   public static final int QNAME = 7;
   public static final int QNAMEA = 0x0d;
   public static final int RTQNAME = 0x0f;
   public static final int RTQNAMEA = 0x10;
   public static final int RTQNAMEL = 0x11;
   public static final int RTQNAMELA = 0x12;
   public static final int MULTINAME = 0x09;
   public static final int MULTINAMEA = 0x0e;
   public static final int MULTINAMEL = 0x1b;
   public static final int MULTINAMELA = 0x1c;
   public static final int TYPENAME = 0x1d;
   private static final int multinameKinds[] = new int[]{QNAME, QNAMEA, MULTINAME, MULTINAMEA, RTQNAME, RTQNAMEA, MULTINAMEL, RTQNAMEL, RTQNAMELA, MULTINAMELA, TYPENAME};
   private static final String multinameKindNames[] = new String[]{"Qname", "QnameA", "Multiname", "MultinameA", "RTQname", "RTQnameA", "MultinameL", "RTQnameL", "RTQnameLA", "MultinameLA", "TypeName"};
   public int kind = -1;
   public int name_index = -1;
   public int namespace_index = -1;
   public int namespace_set_index = -1;
   public int qname_index = -1; //for TypeName
   public List<Integer> params; //for TypeName

   public Multiname(int kind, int name_index, int namespace_index, int namespace_set_index, int qname_index, List<Integer> params) {
      this.kind = kind;
      this.name_index = name_index;
      this.namespace_index = namespace_index;
      this.namespace_set_index = namespace_set_index;
      this.qname_index = qname_index;
      this.params = params;
   }

   public boolean isAttribute() {
      if (kind == QNAMEA) {
         return true;
      }
      if (kind == MULTINAMEA) {
         return true;
      }
      if (kind == RTQNAMEA) {
         return true;
      }
      if (kind == RTQNAMELA) {
         return true;
      }
      if (kind == MULTINAMELA) {
         return true;
      }
      return false;
   }

   public boolean isRuntime() {
      if (kind == RTQNAME) {
         return true;
      }
      if (kind == RTQNAMEA) {
         return true;
      }
      if (kind == MULTINAMEL) {
         return true;
      }
      if (kind == MULTINAMELA) {
         return true;
      }
      return false;
   }

   public boolean needsName() {
      if (kind == RTQNAMEL) {
         return true;
      }
      if (kind == RTQNAMELA) {
         return true;
      }
      if (kind == MULTINAMEL) {
         return true;
      }
      if (kind == MULTINAMELA) {
         return true;
      }
      return false;
   }

   public boolean needsNs() {
      if (kind == RTQNAME) {
         return true;
      }
      if (kind == RTQNAMEA) {
         return true;
      }
      if (kind == RTQNAMEL) {
         return true;
      }
      if (kind == RTQNAMELA) {
         return true;
      }
      return false;
   }

   public String getKindStr() {
      String kindStr = "?";
      for (int k = 0; k < multinameKinds.length; k++) {
         if (multinameKinds[k] == kind) {
            kindStr = multinameKindNames[k];
            break;
         }
      }
      return kindStr;
   }

   @Override
   public String toString() {
      String kindStr = getKindStr();
      return "kind=" + kindStr + " name_index=" + name_index + " namespace_index=" + namespace_index + " namespace_set_index=" + namespace_set_index + " qname_index=" + qname_index + " params_size:" + params.size();

   }

   public String toString(ConstantPool constants, List<String> fullyQualifiedNames) {
      String kindStr = "?";
      for (int k = 0; k < multinameKinds.length; k++) {
         if (multinameKinds[k] == kind) {
            kindStr = multinameKindNames[k] + " ";
            break;
         }
      }
      String nameStr = "";
      if (name_index > 0) {
         nameStr = constants.constant_string[name_index];
      }
      if (name_index == 0) {
         nameStr = "*";
      }
      String namespaceStr = "";
      if (namespace_index > 0) {
         namespaceStr = constants.constant_namespace[namespace_index].toString(constants);
      }
      if (!namespaceStr.equals("")) {
         namespaceStr = namespaceStr + ".";
      }
      if (namespace_index == 0) {
         namespaceStr = "*.";
      }
      String namespaceSetStr = "";
      if (namespace_set_index > 0) {
         namespaceSetStr = " <NS:" + constants.constant_namespace_set[namespace_set_index].toString(constants) + ">";
      }
      String typeNameStr = "";
      if (kind == TYPENAME) {
         typeNameStr = typeNameToStr(constants, fullyQualifiedNames);
      }

      return namespaceStr + nameStr + namespaceSetStr + typeNameStr;

   }

   private String typeNameToStr(ConstantPool constants, List<String> fullyQualifiedNames) {
      String typeNameStr = constants.constant_multiname[qname_index].getName(constants, fullyQualifiedNames);
      if (!params.isEmpty()) {
         typeNameStr += ".<";
         for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
               typeNameStr += ",";
            }
            if (params.get(i) == 0) {
               typeNameStr += "*";
            } else {
               typeNameStr += constants.constant_multiname[params.get(i)].getName(constants, fullyQualifiedNames);
            }
         }
         typeNameStr += ">";
      }
      return typeNameStr;
   }

   public String getName(ConstantPool constants, List<String> fullyQualifiedNames) {
      if (kind == TYPENAME) {
         return typeNameToStr(constants, fullyQualifiedNames);
      }
      if (name_index == -1) {
         return "";
      }
      if (name_index == 0) {
         return (isAttribute() ? "@*" : "*");
      } else {
         String name = constants.constant_string[name_index];
         if ((fullyQualifiedNames != null) && fullyQualifiedNames.contains(name)) {
            return getNameWithNamespace(constants);
         }
         return (isAttribute() ? "@" : "") + name;
      }
   }

   public String getNameWithNamespace(ConstantPool constants) {
      String ret = "";
      Namespace ns = getNamespace(constants);
      if (ns != null) {
         String nsname = ns.getName(constants);
         if (!nsname.equals("")) {
            ret += nsname + ".";
         }
      }
      ret += getName(constants, null);
      return ret;
   }

   public Namespace getNamespace(ConstantPool constants) {
      if ((namespace_index == 0) || (namespace_index == -1)) {
         return null;
      } else {
         return constants.constant_namespace[namespace_index];
      }
   }

   public NamespaceSet getNamespaceSet(ConstantPool constants) {
      if (namespace_set_index == 0) {
         return null;
      } else if (namespace_set_index == -1) {
         return null;
      } else {
         return constants.constant_namespace_set[namespace_set_index];
      }
   }
}
