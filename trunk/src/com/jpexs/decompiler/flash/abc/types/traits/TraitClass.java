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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetLexIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.AsTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceIns;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TraitClass extends Trait {

   public int slot_id;
   public int class_info;
   private static final String[] builtInClasses = {"ArgumentError", "arguments", "Array", "Boolean", "Class", "Date", "DefinitionError", "Error", "EvalError", "Function", "int", "JSON", "Math", "Namespace", "Number", "Object", "QName", "RangeError", "ReferenceError", "RegExp", "SecurityError", "String", "SyntaxError", "TypeError", "uint", "URIError", "VerifyError", "XML", "XMLList"};

   private static boolean isBuiltInClass(String name) {
      for (String g : builtInClasses) {
         if (g.equals(name)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString(ABC abc, List<String> fullyQualifiedNames) {
      return "Class " + abc.constants.constant_multiname[name_index].toString(abc.constants, fullyQualifiedNames) + " slot=" + slot_id + " class_info=" + class_info + " metadata=" + Helper.intArrToString(metadata);
   }

   private boolean parseUsagesFromNS(List<DoABCTag> abcTags, ABC abc, List imports, List<String> uses, int namespace_index, String ignorePackage, String name) {
      Namespace ns = abc.constants.constant_namespace[namespace_index];
      if (name.equals("")) {
         name = "*";
      }
      String newimport = ns.getName(abc.constants);
      /*if (ns.kind == Namespace.KIND_NAMESPACE)*/ {
         String oldimport = newimport;
         newimport = null;
         for (DoABCTag abcTag : abcTags) {
            String newname = abcTag.abc.nsValueToName(oldimport);
            if (newname.equals("-")) {
               return true;
            }
            if (!newname.equals("")) {
               newimport = newname;
               break;
            }
         }
         if (newimport != null) {
            if (!imports.contains(newimport)) {
               if (newimport.contains(":")) {
                  return true;
               }
               String pkg = "";
               if (newimport.contains(".")) {
                  pkg = newimport.substring(0, newimport.lastIndexOf("."));
               }
               String usname = newimport;
               if (usname.contains(".")) {
                  usname = usname.substring(usname.lastIndexOf(".") + 1);
               }
               if (!pkg.equals(ignorePackage)) {
                  imports.add(newimport);
               }
               if (!uses.contains(usname)) {
                  uses.add(usname);
               }
            }
            return true;
         }
      }
      return false;
   }

   private void parseImportsUsagesFromNS(List<DoABCTag> abcTags, ABC abc, List imports, List<String> uses, int namespace_index, String ignorePackage, String name) {
      Namespace ns = abc.constants.constant_namespace[namespace_index];
      if (name.equals("")) {
         name = "*";
      }
      String newimport = ns.getName(abc.constants);
      if (parseUsagesFromNS(abcTags, abc, imports, uses, namespace_index, ignorePackage, name)) {
         return;
      } else if ((ns.kind != Namespace.KIND_PACKAGE) && (ns.kind != Namespace.KIND_PACKAGE_INTERNAL)) {
         return;
      }
      if (newimport.equals("-")) {
         newimport = "";
      }
      //if (!newimport.equals("")) {
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
      //}
   }

   private void parseUsagesFromMultiname(List<DoABCTag> abcTags, ABC abc, List<String> imports, List<String> uses, Multiname m, String ignorePackage, List<String> fullyQualifiedNames) {
      if (m != null) {
         if (m.kind == Multiname.TYPENAME) {
            if (m.qname_index != 0) {
               parseUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[m.qname_index], ignorePackage, fullyQualifiedNames);
            }
            for (Integer i : m.params) {
               if (i != 0) {
                  parseUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[i], ignorePackage, fullyQualifiedNames);
               }
            }
            return;
         }
         Namespace ns = m.getNamespace(abc.constants);
         String name = m.getName(abc.constants, fullyQualifiedNames);
         NamespaceSet nss = m.getNamespaceSet(abc.constants);
         if (ns != null) {
            parseUsagesFromNS(abcTags, abc, imports, uses, m.namespace_index, ignorePackage, name);
         }
         if (nss != null) {
            if (nss.namespaces.length == 1) {
               parseUsagesFromNS(abcTags, abc, imports, uses, nss.namespaces[0], ignorePackage, name);
            }
         }
      }
   }

   private void parseImportsUsagesFromMultiname(List<DoABCTag> abcTags, ABC abc, List<String> imports, List<String> uses, Multiname m, String ignorePackage, List<String> fullyQualifiedNames) {
      if (m != null) {
         if (m.kind == Multiname.TYPENAME) {
            if (m.qname_index != 0) {
               parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[m.qname_index], ignorePackage, fullyQualifiedNames);
            }
            for (Integer i : m.params) {
               if (i != 0) {
                  parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[i], ignorePackage, fullyQualifiedNames);
               }
            }
            return;
         }
         Namespace ns = m.getNamespace(abc.constants);
         String name = m.getName(abc.constants, fullyQualifiedNames);
         NamespaceSet nss = m.getNamespaceSet(abc.constants);
         if (ns != null) {
            parseImportsUsagesFromNS(abcTags, abc, imports, uses, m.namespace_index, ignorePackage, name);
         }
         if (nss != null) {
            if (nss.namespaces.length == 1) {
               parseImportsUsagesFromNS(abcTags, abc, imports, uses, nss.namespaces[0], ignorePackage, name);
            }
         }
      }
   }

   private void parseImportsUsagesFromMethodInfo(List<DoABCTag> abcTags, ABC abc, int method_index, List<String> imports, List<String> uses, String ignorePackage, List<String> fullyQualifiedNames, List<Integer> visitedMethods) {
      if (method_index > abc.method_info.length) {
         return;
      }
      visitedMethods.add(method_index);
      if (abc.method_info[method_index].ret_type != 0) {
         parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[abc.method_info[method_index].ret_type], ignorePackage, fullyQualifiedNames);
      }
      for (int t : abc.method_info[method_index].param_types) {
         if (t != 0) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[t], ignorePackage, fullyQualifiedNames);
         }
      }
      MethodBody body = abc.findBody(method_index);
      if (body != null) {
         parseImportsUsagesFromTraits(abcTags, abc, body.traits, imports, uses, ignorePackage, fullyQualifiedNames);
         for (ABCException ex : body.exceptions) {
            parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[ex.type_index], ignorePackage, fullyQualifiedNames);
         }
         for (AVM2Instruction ins : body.code.code) {
            if (ins.definition instanceof NewFunctionIns) {
               if (ins.operands[0] != method_index) {
                  if (!visitedMethods.contains(ins.operands[0])) {
                     parseImportsUsagesFromMethodInfo(abcTags, abc, ins.operands[0], imports, uses, ignorePackage, fullyQualifiedNames, visitedMethods);
                  }
               }
            }
            if ((ins.definition instanceof FindPropertyStrictIns)
                    || (ins.definition instanceof FindPropertyIns)
                    || (ins.definition instanceof GetLexIns)
                    || (ins.definition instanceof CoerceIns)
                    || (ins.definition instanceof AsTypeIns)) {
               int m = ins.operands[0];
               if (m != 0) {
                  parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[m], ignorePackage, fullyQualifiedNames);
               }
            } else {
               for (int k = 0; k < ins.definition.operands.length; k++) {

                  if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                     int multinameIndex = ins.operands[k];
                     parseUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[multinameIndex], ignorePackage, fullyQualifiedNames);
                  }
               }
            }
         }
      }
   }

   private void parseImportsUsagesFromTraits(List<DoABCTag> abcTags, ABC abc, Traits ts, List<String> imports, List<String> uses, String ignorePackage, List<String> fullyQualifiedNames) {
      for (Trait t : ts.traits) {
         parseImportsUsagesFromTrait(abcTags, abc, t, imports, uses, ignorePackage, fullyQualifiedNames);
      }
   }

   private void parseImportsUsagesFromTrait(List<DoABCTag> abcTags, ABC abc, Trait t, List<String> imports, List<String> uses, String ignorePackage, List<String> fullyQualifiedNames) {
      if (t instanceof TraitMethodGetterSetter) {
         TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
         parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[tm.name_index], ignorePackage, fullyQualifiedNames);
         if (tm.method_info != 0) {
            parseImportsUsagesFromMethodInfo(abcTags, abc, tm.method_info, imports, uses, ignorePackage, fullyQualifiedNames, new ArrayList<Integer>());
         }
      }
      parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, t.getName(abc), ignorePackage, fullyQualifiedNames);
      if (t instanceof TraitSlotConst) {
         TraitSlotConst ts = (TraitSlotConst) t;
         parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[ts.name_index], ignorePackage, fullyQualifiedNames);
         parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[ts.type_index], ignorePackage, fullyQualifiedNames);
      }
   }

   private List getImportsUsages(List<DoABCTag> abcTags, ABC abc, List<String> imports, List<String> uses, List<String> fullyQualifiedNames) {
      //constructor


      String packageName = abc.instance_info[class_info].getName(abc.constants).getNamespace(abc.constants).getName(abc.constants);

      parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[abc.instance_info[class_info].name_index], packageName, fullyQualifiedNames);

      if (abc.instance_info[class_info].super_index > 0) {
         parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[abc.instance_info[class_info].super_index], packageName, fullyQualifiedNames);
      }
      for (int i : abc.instance_info[class_info].interfaces) {
         parseImportsUsagesFromMultiname(abcTags, abc, imports, uses, abc.constants.constant_multiname[i], packageName, fullyQualifiedNames);
      }

      //static
      parseImportsUsagesFromTraits(abcTags, abc, abc.class_info[class_info].static_traits, imports, uses, packageName, fullyQualifiedNames);

      //static initializer
      parseImportsUsagesFromMethodInfo(abcTags, abc, abc.class_info[class_info].cinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<Integer>());

      //instance
      parseImportsUsagesFromTraits(abcTags, abc, abc.instance_info[class_info].instance_traits, imports, uses, packageName, fullyQualifiedNames);


      //instance initializer
      parseImportsUsagesFromMethodInfo(abcTags, abc, abc.instance_info[class_info].iinit_index, imports, uses, packageName, fullyQualifiedNames, new ArrayList<Integer>());
      return imports;
   }

   @Override
   public String convertHeader(String path, List<DoABCTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight, List<String> fullyQualifiedNames) {
      String classHeader = abc.instance_info[class_info].getClassHeaderStr(abc, fullyQualifiedNames);
      return classHeader;
   }

   @Override
   public String convert(String path, List<DoABCTag> abcTags, ABC abc, boolean isStatic, boolean pcode, int classIndex, boolean highlight, List<String> fullyQualifiedNames) {
      if (!highlight) {
         Highlighting.doHighlight = false;
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(baos);

      String packageName = abc.instance_info[class_info].getName(abc.constants).getNamespace(abc.constants).getName(abc.constants);

      List<String> namesInThisPackage = new ArrayList<String>();
      for (DoABCTag tag : abcTags) {
         for (ScriptInfo si : tag.abc.script_info) {
            String spath = si.getPath(tag.abc);
            String pkg = "";
            String name = spath;
            if (spath.contains(".")) {
               pkg = spath.substring(0, spath.lastIndexOf("."));
               name = spath.substring(spath.lastIndexOf(".") + 1);
            }
            if (pkg.equals(packageName)) {
               namesInThisPackage.add(name);
            }
         }
      }
      //imports
      List<String> imports = new ArrayList<String>();
      List<String> uses = new ArrayList<String>();
      getImportsUsages(abcTags, abc, imports, uses, new ArrayList<String>());

      fullyQualifiedNames = new ArrayList<String>();

      List<String> importnames = new ArrayList<String>();
      importnames.addAll(namesInThisPackage);
      for (String ipath : imports) {
         String name = ipath;
         String pkg = "";
         if (name.contains(".")) {
            pkg = name.substring(0, name.lastIndexOf("."));
            name = name.substring(name.lastIndexOf(".") + 1);
         }
         if (importnames.contains(name) || ((!pkg.equals("")) && isBuiltInClass(name))) {
            fullyQualifiedNames.add(name);
         } else {
            importnames.add(name);
         }
      }
      /*List<String> imports2 = new ArrayList<String>();
       for (String path : imports) {
       String name = path;
       String pkg = "";
       if (name.contains(".")) {
       pkg = name.substring(0, name.lastIndexOf("."));
       name = name.substring(name.lastIndexOf(".") + 1);
       }

       if ((!packageName.equals(pkg)) && (!fullyQualifiedNames.contains(name))) {
       imports2.add(path);
       }
       }
       imports = imports2;*/

      for (String imp : imports) {
         if (!imp.startsWith(".")) {
            out.println(ABC.IDENT_STRING + "import " + imp + ";");
         }
      }
      out.println();
      for (String us : uses) {
         out.println(ABC.IDENT_STRING + "use namespace " + us + ";");
      }
      out.println();

      //class header     
      String classHeader = abc.instance_info[class_info].getClassHeaderStr(abc, fullyQualifiedNames);
      if (classHeader.startsWith("private ")) {
         classHeader = classHeader.substring("private ".length());
      }
      out.println(ABC.IDENT_STRING + classHeader);
      out.println(ABC.IDENT_STRING + "{");

      String toPrint;
      List<String> outTraits = new LinkedList<String>();


      int bodyIndex;
      String bodyStr = "";
      bodyIndex = abc.findBodyIndex(abc.class_info[class_info].cinit_index);
      if (bodyIndex != -1) {
         bodyStr = abc.bodies[bodyIndex].toString(packageName + "." + abc.instance_info[class_info].getName(abc.constants).getName(abc.constants, fullyQualifiedNames) + ".staticinitializer", pcode, true, class_info, abc, abc.constants, abc.method_info, new Stack<GraphTargetItem>(), true, highlight, fullyQualifiedNames, abc.class_info[class_info].static_traits);
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
            bodyStr = ABC.addTabs(abc.bodies[bodyIndex].toString(packageName + "." + abc.instance_info[class_info].getName(abc.constants).getName(abc.constants, fullyQualifiedNames) + ".initializer", pcode, false, class_info, abc, abc.constants, abc.method_info, new Stack<GraphTargetItem>(), false, highlight, fullyQualifiedNames, abc.instance_info[class_info].instance_traits), 3);
            constructorParams = abc.method_info[abc.instance_info[class_info].iinit_index].getParamStr(abc.constants, abc.bodies[bodyIndex], abc, fullyQualifiedNames);
         } else {
            constructorParams = abc.method_info[abc.instance_info[class_info].iinit_index].getParamStr(abc.constants, null, abc, fullyQualifiedNames);
         }
         toPrint = ABC.IDENT_STRING + ABC.IDENT_STRING + modifier + "function " + abc.constants.constant_multiname[abc.instance_info[class_info].name_index].getName(abc.constants, new ArrayList<String>()/*do not want full names here*/) + "(" + constructorParams + ") {\r\n" + bodyStr + "\r\n" + ABC.IDENT_STRING + ABC.IDENT_STRING + "}";
         if (highlight) {
            toPrint = Highlighting.hilighTrait(toPrint, abc.class_info[class_info].static_traits.traits.length + abc.instance_info[class_info].instance_traits.traits.length);
         }
         outTraits.add(toPrint);
      }
      //}

      //static variables,constants & methods
      outTraits.add(abc.class_info[class_info].static_traits.convert(packageName + "." + abc.instance_info[class_info].getName(abc.constants).getName(abc.constants, fullyQualifiedNames), abcTags, abc, true, pcode, false, class_info, highlight, fullyQualifiedNames));

      outTraits.add(abc.instance_info[class_info].instance_traits.convert(packageName + "." + abc.instance_info[class_info].getName(abc.constants).getName(abc.constants, fullyQualifiedNames), abcTags, abc, false, pcode, false, class_info, highlight, fullyQualifiedNames));

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
