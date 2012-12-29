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
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.asdec.abc.avm2.treemodel.InitPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.Namespace;
import com.jpexs.asdec.abc.types.NamespaceSet;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TraitClass extends Trait {

   public int slot_id;
   public int class_info;

   @Override
   public String toString(ABC abc) {
      return "Class " + abc.constants.constant_multiname[name_index].toString(abc.constants) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
   }

   private void parseImportFromNS(ABC abc, List imports, Namespace ns, String ignorePackage, String name) {
      if (name.equals("")) {
         name = "*";
      }
      if (ns.kind != Namespace.KIND_PACKAGE) {
         return;
      }
      String newimport = ns.getName(abc.constants);
      if (newimport.equals("-")) {
         newimport = "";
      }
      if (!newimport.equals("")) {
         newimport += "." + name;
         if (newimport.contains(":")) {
            return;
         }
         if (!imports.contains(newimport)) {
            String pkg = newimport.substring(0, newimport.lastIndexOf("."));
            if (!pkg.equals(ignorePackage)) {
               imports.add(newimport);
            }
         }
      }
   }

   private void parseImportFromMultiname(ABC abc, List imports, Multiname m, String ignorePackage) {
      if (m != null) {
         Namespace ns = m.getNamespace(abc.constants);
         String name = m.getName(abc.constants);
         NamespaceSet nss = m.getNamespaceSet(abc.constants);
         if (ns != null) {
            parseImportFromNS(abc, imports, ns, ignorePackage, name);
         }
         if (nss != null) {
            for (int ni : nss.namespaces) {
               parseImportFromNS(abc, imports, abc.constants.constant_namespace[ni], ignorePackage, name);
            }
         }
      }
   }

   private void parseImportsFromMethodInfo(ABC abc, int method_index, List imports, String ignorePackage) {
      if (abc.method_info[method_index].ret_type != 0) {
         parseImportFromMultiname(abc, imports, abc.constants.constant_multiname[abc.method_info[method_index].ret_type], ignorePackage);
      }
      for (int t : abc.method_info[method_index].param_types) {
         if (t != 0) {
            parseImportFromMultiname(abc, imports, abc.constants.constant_multiname[t], ignorePackage);
         }
      }
      MethodBody body = abc.findBody(method_index);
      if (body != null) {
         for (AVM2Instruction ins : body.code.code) {
            if (ins.definition instanceof NewFunctionIns) {
               parseImportsFromMethodInfo(abc, ins.operands[0], imports, ignorePackage);
            }
            for (int k = 0; k < ins.definition.operands.length; k++) {
               if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                  int multinameIndex = ins.operands[k];
                  parseImportFromMultiname(abc, imports, abc.constants.constant_multiname[multinameIndex], ignorePackage);
               }
            }
         }
      }
   }

   private List getImports(ABC abc) {
      List<String> imports = new ArrayList<String>();

      //constructor

      //parseImportFromMultiname(imports, constants.constant_multiname[instance_info[instanceIndex].name_index]);

      String packageName = abc.instance_info[class_info].getName(abc.constants).getNamespace(abc.constants).getName(abc.constants);

      if (abc.instance_info[class_info].super_index > 0) {
         parseImportFromMultiname(abc, imports, abc.constants.constant_multiname[abc.instance_info[class_info].super_index], packageName);
      }
      for (int i : abc.instance_info[class_info].interfaces) {
         parseImportFromMultiname(abc, imports, abc.constants.constant_multiname[i], packageName);
      }

      //static
      for (Trait t : abc.class_info[class_info].static_traits.traits) {
         //parseImportFromMultiname(imports, t.getMultiName(constants));
         if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            if (tm.method_info != 0) {
               parseImportsFromMethodInfo(abc, tm.method_info, imports, packageName);
            }
         }

      }

      //static initializer
      parseImportsFromMethodInfo(abc, abc.class_info[class_info].cinit_index, imports, packageName);

      //instance
      for (Trait t : abc.instance_info[class_info].instance_traits.traits) {
         //parseImportFromMultiname(imports, t.getMultiName(constants));
         if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            if (tm.method_info != 0) {
               parseImportsFromMethodInfo(abc, tm.method_info, imports, packageName);
            }
         }
      }

      //instance initializer
      parseImportsFromMethodInfo(abc, abc.instance_info[class_info].iinit_index, imports, packageName);
      return imports;
   }

   @Override
   public String convert(ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight) {
      if (!highlight) {
         Highlighting.doHighlight = false;
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(baos);

      //imports
      List<String> imports = getImports(abc);
      for (String imp : imports) {
         out.println(ABC.IDENT_STRING + "import " + imp + ";");
      }
      out.println();

      //class header
      String classHeader = abc.instance_info[class_info].getClassHeaderStr(abc);
      if (classHeader.startsWith("private ")) {
         classHeader = classHeader.substring("private ".length());
      }
      out.println(ABC.IDENT_STRING + classHeader);
      out.println(ABC.IDENT_STRING + "{");

      String toPrint;
      List<String> outTraits = new LinkedList<String>();

      //if (class_info[i].cinit_index != 0) {
      if (ABC.AUTOINIT_STATIC_VARIABLES) {
         int bodyIndex = abc.findBodyIndex(abc.class_info[class_info].cinit_index);
         List<TreeItem> initializer = abc.bodies[bodyIndex].code.toTree(true, class_info, abc, abc.constants, abc.method_info, abc.bodies[bodyIndex], abc.bodies[bodyIndex].code.getLocalRegNamesFromDebug(abc));
         for (TreeItem ti : initializer) {
            if (ti instanceof SetPropertyTreeItem) {
               int multinameIndex = ((SetPropertyTreeItem) ti).propertyName.multinameIndex;
               TreeItem value = ((SetPropertyTreeItem) ti).value;
               for (Trait t : abc.class_info[class_info].static_traits.traits) {
                  if (t.name_index == multinameIndex) {
                     if (t instanceof TraitSlotConst) {
                        ((TraitSlotConst) t).assignedValue = value;
                     }
                  }
               }
            }
            if (ti instanceof InitPropertyTreeItem) {
               int multinameIndex = ((InitPropertyTreeItem) ti).propertyName.multinameIndex;
               TreeItem value = ((InitPropertyTreeItem) ti).value;
               for (Trait t : abc.class_info[class_info].static_traits.traits) {
                  if (t.name_index == multinameIndex) {
                     if (t instanceof TraitSlotConst) {
                        ((TraitSlotConst) t).assignedValue = value;
                     }
                  }
               }
            }
         }
      }
      String bodyStr = "";
      int bodyIndex = abc.findBodyIndex(abc.class_info[class_info].cinit_index);
      if (bodyIndex != -1) {
         bodyStr = abc.bodies[bodyIndex].toString(pcode, true, class_info, abc, abc.constants, abc.method_info, new Stack<TreeItem>(), true, highlight);
      }
      if (Highlighting.stripHilights(bodyStr).equals("")) {
         toPrint = ABC.addTabs(bodyStr, 3);
      } else {
         toPrint = ABC.IDENT_STRING + ABC.IDENT_STRING + "{\r\n" + ABC.addTabs(bodyStr, 3) + "\r\n" + ABC.IDENT_STRING + ABC.IDENT_STRING + "}";
      }
      if (highlight) {
         toPrint = Highlighting.hilighTrait(toPrint, abc.class_info[class_info].static_traits.traits.length + abc.instance_info[class_info].instance_traits.traits.length + 1);
      }
      outTraits.add(toPrint);
      //}

      //constructor
      //if (instance_info[i].iinit_index != 0) {
      if (!abc.instance_info[class_info].isInterface()) {
         String modifier = "";
         Multiname m = abc.constants.constant_multiname[abc.instance_info[class_info].name_index];
         if (m != null) {
            Namespace ns = m.getNamespace(abc.constants);
            if (ns != null) {
               modifier = ns.getPrefix(abc) + " ";
               if (modifier.equals(" ")) {
                  modifier = "";
               }
               if (modifier.startsWith("private")) { //cannot have private constuctor
                  modifier = "";
               }
            }
         }
         String constructorParams;

         bodyStr = "";
         bodyIndex = abc.findBodyIndex(abc.instance_info[class_info].iinit_index);
         if (bodyIndex != -1) {
            bodyStr = ABC.addTabs(abc.bodies[bodyIndex].toString(pcode, false, class_info, abc, abc.constants, abc.method_info, new Stack<TreeItem>(), false, highlight), 3);
            constructorParams = abc.method_info[abc.instance_info[class_info].iinit_index].getParamStr(abc.constants, abc.bodies[bodyIndex], abc);
         } else {
            constructorParams = abc.method_info[abc.instance_info[class_info].iinit_index].getParamStr(abc.constants, null, abc);
         }
         toPrint = ABC.IDENT_STRING + ABC.IDENT_STRING + modifier + "function " + abc.constants.constant_multiname[abc.instance_info[class_info].name_index].getName(abc.constants) + "(" + constructorParams + ") {\r\n" + bodyStr + "\r\n" + ABC.IDENT_STRING + ABC.IDENT_STRING + "}";
         if (highlight) {
            toPrint = Highlighting.hilighTrait(toPrint, abc.class_info[class_info].static_traits.traits.length + abc.instance_info[class_info].instance_traits.traits.length);
         }
         outTraits.add(toPrint);
      }
      //}

      //static variables,constants & methods
      outTraits.add(abc.class_info[class_info].static_traits.convert(abc, true, pcode, false, class_info, highlight));

      outTraits.add(abc.instance_info[class_info].instance_traits.convert(abc, false, pcode, false, class_info, highlight));

      out.println(Helper.joinStrings(outTraits, "\r\n\r\n"));
      out.println(ABC.IDENT_STRING + "}");//class
      out.flush();
      Highlighting.doHighlight = true;
      return baos.toString();
   }

   public Multiname getName(ABC abc) {
      return abc.constants.constant_multiname[abc.instance_info[class_info].name_index];
   }
}
