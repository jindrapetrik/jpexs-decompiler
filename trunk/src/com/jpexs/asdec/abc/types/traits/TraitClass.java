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
import com.jpexs.asdec.abc.types.ABCException;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.Namespace;
import com.jpexs.asdec.abc.types.NamespaceSet;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;
import com.jpexs.asdec.tags.DoABCTag;
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

   private void parseImportsUsagesFromNS(List<DoABCTag> abcTags, ABC abc, List imports, List<String> uses, int namespace_index, String ignorePackage, String name) {
      Namespace ns = abc.constants.constant_namespace[namespace_index];
      if (name.equals("")) {
         name = "*";
      }
      String newimport = ns.getName(abc.constants);
      if (ns.kind == Namespace.KIND_NAMESPACE) {
         String oldimport = newimport;
         newimport = null;
         for (DoABCTag abcTag : abcTags) {
            String newname = abcTag.abc.nsValueToName(oldimport);
            if (newname.equals("-")) {
               return;
            }
            if (!newname.equals("")) {
               newimport = newname;
               break;
            }
         }
         if (newimport != null) {
            if (!imports.contains(newimport)) {
               if (newimport.contains(":")) {
                  return;
               }
               String pkg = "";
               if (newimport.contains(".")) {
                  pkg = newimport.substring(0, newimport.lastIndexOf("."));
               }
               String usname=newimport;
               if(usname.contains(".")){
                  usname=usname.substring(usname.lastIndexOf(".")+1);
               }
               if (!pkg.equals(ignorePackage)) {
                  imports.add(newimport);                  
               }
               uses.add(usname);
            }
         }
         return;
      } else if (ns.kind != Namespace.KIND_PACKAGE) {
         return;
      }
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

   private void parseImportsUsagesFromMultiname(List<DoABCTag> abcTags, ABC abc, List<String> imports, List<String> uses, Multiname m, String ignorePackage) {
      if (m != null) {
         Namespace ns = m.getNamespace(abc.constants);
         String name = m.getName(abc.constants);
         NamespaceSet nss = m.getNamespaceSet(abc.constants);
         if (ns != null) {
            parseImportsUsagesFromNS(abcTags, abc, imports,uses, m.namespace_index, ignorePackage, name);
         }
         if (nss != null) {
            for (int ni : nss.namespaces) {
               parseImportsUsagesFromNS(abcTags, abc, imports,uses, ni, ignorePackage, name);
            }
         }
      }
   }

   private void parseImportsUsagesFromMethodInfo(List<DoABCTag> abcTags, ABC abc, int method_index, List<String> imports, List<String> uses, String ignorePackage) {
      if (abc.method_info[method_index].ret_type != 0) {
         parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[abc.method_info[method_index].ret_type], ignorePackage);
      }
      for (int t : abc.method_info[method_index].param_types) {
         if (t != 0) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[t], ignorePackage);
         }
      }
      MethodBody body = abc.findBody(method_index);
      if (body != null) {
         for (ABCException ex : body.exceptions) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[ex.type_index], ignorePackage);
         }
         for (AVM2Instruction ins : body.code.code) {
            if (ins.definition instanceof NewFunctionIns) {
               parseImportsUsagesFromMethodInfo(abcTags, abc, ins.operands[0], imports,uses, ignorePackage);
            }
            for (int k = 0; k < ins.definition.operands.length; k++) {
               if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                  int multinameIndex = ins.operands[k];
                  parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[multinameIndex], ignorePackage);
               }
            }
         }
      }
   }

   private void parseImportsUsagesFromTrait(List<DoABCTag> abcTags, ABC abc, Trait t, List<String> imports, List<String> uses, String ignorePackage) {
      if (t instanceof TraitMethodGetterSetter) {
         TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
         if (tm.method_info != 0) {
            parseImportsUsagesFromMethodInfo(abcTags, abc, tm.method_info, imports,uses, ignorePackage);
         }
      }
      parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, t.getName(abc), ignorePackage);
      if (t instanceof TraitSlotConst) {
         TraitSlotConst ts = (TraitSlotConst) t;
         parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[ts.type_index], ignorePackage);
      }
   }

   private List getImportsUsages(List<DoABCTag> abcTags, ABC abc,List<String> imports,List<String> uses) {
      //constructor

      //parseImportFromMultiname(imports, constants.constant_multiname[instance_info[instanceIndex].name_index]);

      String packageName = abc.instance_info[class_info].getName(abc.constants).getNamespace(abc.constants).getName(abc.constants);

      if (abc.instance_info[class_info].super_index > 0) {
         parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[abc.instance_info[class_info].super_index], packageName);
      }
      for (int i : abc.instance_info[class_info].interfaces) {
         parseImportsUsagesFromMultiname(abcTags, abc, imports,uses, abc.constants.constant_multiname[i], packageName);
      }

      //static
      for (Trait t : abc.class_info[class_info].static_traits.traits) {
         parseImportsUsagesFromTrait(abcTags, abc, t, imports, uses, packageName);
      }

      //static initializer
      parseImportsUsagesFromMethodInfo(abcTags, abc, abc.class_info[class_info].cinit_index, imports, uses, packageName);

      //instance
      for (Trait t : abc.instance_info[class_info].instance_traits.traits) {
         parseImportsUsagesFromTrait(abcTags, abc, t, imports, uses, packageName);
      }

      //instance initializer
      parseImportsUsagesFromMethodInfo(abcTags, abc, abc.instance_info[class_info].iinit_index, imports, uses, packageName);
      return imports;
   }

   @Override
   public String convert(List<DoABCTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight) {
      if (!highlight) {
         Highlighting.doHighlight = false;
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(baos);

      //imports
      List<String> imports = new ArrayList<String>();
      List<String> uses = new ArrayList<String>();
      getImportsUsages(abcTags, abc,imports,uses);
      for (String imp : imports) {
         out.println(ABC.IDENT_STRING + "import " + imp + ";");
      }
      out.println();
      for (String us : uses) {
         out.println(ABC.IDENT_STRING + "use namespace " + us + ";");
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
      outTraits.add(abc.class_info[class_info].static_traits.convert(abcTags, abc, true, pcode, false, class_info, highlight));

      outTraits.add(abc.instance_info[class_info].instance_traits.convert(abcTags, abc, false, pcode, false, class_info, highlight));

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
