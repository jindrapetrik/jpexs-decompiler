/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.jpexs.asdec.abc;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.UnknownInstructionCode;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.InitPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.*;
import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import com.jpexs.asdec.helpers.Highlighting;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class ABC {

   public int major_version = 0;
   public int minor_version = 0;
   public ConstantPool constants;
   public MethodInfo method_info[];
   public MetadataInfo metadata_info[];
   public InstanceInfo instance_info[];
   public ClassInfo class_info[];
   public ScriptInfo script_info[];
   public MethodBody bodies[];
   public long stringOffsets[];
   public static String IDENT_STRING = "   ";
   public static final int MINORwithDECIMAL = 17;
   private int fixNamesStrategy = 0;
   private final int STRATEGY_FIX_NAMES = 1;
   private final int STRATEGY_IGNORE = 2;
   public static final boolean AUTOINIT_STATIC_VARIABLES = false;

   private void fixNameWithStrategy(int index) {
      if (fixNamesStrategy == 0) {
         if (!isValidName(index, false)) {
            int val = JOptionPane.showOptionDialog(null, "Decompiler found unusual name '" + constants.constant_string[index] + "' for a variable/class which can cause problems when decompiling.\r\nDo you want the decompiler to fix it?", "Decompilation", 0, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Yes", "Yes to all", "No", "No to all"}, "Yes to all");
            if (val == JOptionPane.CLOSED_OPTION) {
               val = 2; //NO
            }
            if ((val == 0) || (val == 1)) //YES,YES TO ALL
            {
               isValidName(index, true);
            }
            if (val == 1) //YES TO ALL
            {
               fixNamesStrategy = STRATEGY_FIX_NAMES;
            }
            if (val == 3) { //NO TO ALL
               fixNamesStrategy = STRATEGY_IGNORE;
            }
         }
      } else if (fixNamesStrategy == STRATEGY_FIX_NAMES) {
         isValidName(index, true);
      } else if (fixNamesStrategy == STRATEGY_IGNORE) {
      }
   }

   public ABC(InputStream is) throws IOException {
      ABCInputStream ais = new ABCInputStream(is);
      minor_version = ais.readU16();
      major_version = ais.readU16();
      constants = new ConstantPool();
      //constant integers
      int constant_int_pool_count = ais.readU30();
      constants.constant_int = new long[constant_int_pool_count];
      for (int i = 1; i < constant_int_pool_count; i++) { //index 0 not used. Values 1..n-1
         constants.constant_int[i] = ais.readS32();
      }

      //constant unsigned integers
      int constant_uint_pool_count = ais.readU30();
      constants.constant_uint = new long[constant_uint_pool_count];
      for (int i = 1; i < constant_uint_pool_count; i++) { //index 0 not used. Values 1..n-1
         constants.constant_uint[i] = ais.readU32();
      }

      //constant double
      int constant_double_pool_count = ais.readU30();
      constants.constant_double = new double[constant_double_pool_count];
      for (int i = 1; i < constant_double_pool_count; i++) { //index 0 not used. Values 1..n-1
         constants.constant_double[i] = ais.readDouble();
      }


      //constant decimal
      if (minor_version >= MINORwithDECIMAL) {
         int constant_decimal_pool_count = ais.readU30();
         constants.constant_decimal = new Decimal[constant_decimal_pool_count];
         for (int i = 1; i < constant_decimal_pool_count; i++) { //index 0 not used. Values 1..n-1
            constants.constant_decimal[i] = ais.readDecimal();
         }
      } else {
         constants.constant_decimal = new Decimal[0];
      }

      //constant string
      int constant_string_pool_count = ais.readU30();
      constants.constant_string = new String[constant_string_pool_count];
      stringOffsets = new long[constant_string_pool_count];
      constants.constant_string[0] = "";
      for (int i = 1; i < constant_string_pool_count; i++) { //index 0 not used. Values 1..n-1
         long pos = ais.getPosition();
         constants.constant_string[i] = ais.readString();
         stringOffsets[i] = pos;
      }

      //constant namespace
      int constant_namespace_pool_count = ais.readU30();
      constants.constant_namespace = new Namespace[constant_namespace_pool_count];
      for (int i = 1; i < constant_namespace_pool_count; i++) { //index 0 not used. Values 1..n-1
         constants.constant_namespace[i] = ais.readNamespace();
         fixNameWithStrategy(constants.constant_namespace[i].name_index);
      }

      //constant namespace set
      int constant_namespace_set_pool_count = ais.readU30();
      constants.constant_namespace_set = new NamespaceSet[constant_namespace_set_pool_count];
      for (int i = 1; i < constant_namespace_set_pool_count; i++) { //index 0 not used. Values 1..n-1
         constants.constant_namespace_set[i] = new NamespaceSet();
         int namespace_count = ais.readU30();
         constants.constant_namespace_set[i].namespaces = new int[namespace_count];
         for (int j = 0; j < namespace_count; j++) {
            constants.constant_namespace_set[i].namespaces[j] = ais.readU30();
         }
      }





      //constant multiname
      int constant_multiname_pool_count = ais.readU30();
      constants.constant_multiname = new Multiname[constant_multiname_pool_count];
      for (int i = 1; i < constant_multiname_pool_count; i++) { //index 0 not used. Values 1..n-1
         constants.constant_multiname[i] = ais.readMultiname();
         fixNameWithStrategy(constants.constant_multiname[i].name_index);
      }


      //method info
      int methods_count = ais.readU30();
      method_info = new MethodInfo[methods_count];
      for (int i = 0; i < methods_count; i++) {
         method_info[i] = ais.readMethodInfo();
      }

      //metadata info
      int metadata_count = ais.readU30();
      metadata_info = new MetadataInfo[metadata_count];
      for (int i = 0; i < metadata_count; i++) {
         int name_index = ais.readU30();
         int values_count = ais.readU30();
         int keys[] = new int[values_count];
         for (int v = 0; v < values_count; v++) {
            keys[v] = ais.readU30();
         }
         int values[] = new int[values_count];
         for (int v = 0; v < values_count; v++) {
            values[v] = ais.readU30();
         }
         metadata_info[i] = new MetadataInfo(name_index, keys, values);
      }

      int class_count = ais.readU30();
      instance_info = new InstanceInfo[class_count];
      for (int i = 0; i < class_count; i++) {
         instance_info[i] = ais.readInstanceInfo();
      }
      class_info = new ClassInfo[class_count];
      for (int i = 0; i < class_count; i++) {
         class_info[i] = new ClassInfo();
         class_info[i].cinit_index = ais.readU30();
         class_info[i].static_traits = ais.readTraits();
      }
      int script_count = ais.readU30();
      script_info = new ScriptInfo[script_count];
      for (int i = 0; i < script_count; i++) {
         script_info[i] = new ScriptInfo();
         script_info[i].init_index = ais.readU30();
         script_info[i].traits = ais.readTraits();
      }

      int bodies_count = ais.readU30();
      bodies = new MethodBody[bodies_count];
      for (int i = 0; i < bodies_count; i++) {
         bodies[i] = new MethodBody();
         bodies[i].method_info = ais.readU30();
         bodies[i].max_stack = ais.readU30();
         bodies[i].max_regs = ais.readU30();
         bodies[i].init_scope_depth = ais.readU30();
         bodies[i].max_scope_depth = ais.readU30();
         int code_length = ais.readU30();
         bodies[i].codeBytes = new byte[code_length];
         for (int j = 0; j < code_length; j++) {
            bodies[i].codeBytes[j] = (byte) ais.read();
         }
         try {
            bodies[i].code = new AVM2Code(new ByteArrayInputStream(bodies[i].codeBytes));
         } catch (UnknownInstructionCode re) {
            bodies[i].code = new AVM2Code();
            System.err.println(re.toString());
         }
         int ex_count = ais.readU30();
         bodies[i].exceptions = new ABCException[ex_count];
         for (int j = 0; j < ex_count; j++) {
            bodies[i].exceptions[j] = new ABCException();
            bodies[i].exceptions[j].start = ais.readU30();
            bodies[i].exceptions[j].end = ais.readU30();
            bodies[i].exceptions[j].target = ais.readU30();
            bodies[i].exceptions[j].type_index = ais.readU30();
            bodies[i].exceptions[j].name_index = ais.readU30();
         }
         bodies[i].traits = ais.readTraits();
      }
   }

   public void saveToStream(OutputStream os) throws IOException {
      ABCOutputStream aos = new ABCOutputStream(os);
      aos.writeU16(minor_version);
      aos.writeU16(major_version);

      aos.writeU30(constants.constant_int.length);
      for (int i = 1; i < constants.constant_int.length; i++) {
         aos.writeS32(constants.constant_int[i]);
      }
      aos.writeU30(constants.constant_uint.length);
      for (int i = 1; i < constants.constant_uint.length; i++) {
         aos.writeU32(constants.constant_uint[i]);
      }

      aos.writeU30(constants.constant_double.length);
      for (int i = 1; i < constants.constant_double.length; i++) {
         aos.writeDouble(constants.constant_double[i]);
      }

      if (minor_version >= MINORwithDECIMAL) {
         aos.writeU30(constants.constant_decimal.length);
         for (int i = 1; i < constants.constant_decimal.length; i++) {
            aos.writeDecimal(constants.constant_decimal[i]);
         }
      }

      aos.writeU30(constants.constant_string.length);
      for (int i = 1; i < constants.constant_string.length; i++) {
         aos.writeString(constants.constant_string[i]);
      }

      aos.writeU30(constants.constant_namespace.length);
      for (int i = 1; i < constants.constant_namespace.length; i++) {
         aos.writeNamespace(constants.constant_namespace[i]);
      }

      aos.writeU30(constants.constant_namespace_set.length);
      for (int i = 1; i < constants.constant_namespace_set.length; i++) {
         aos.writeU30(constants.constant_namespace_set[i].namespaces.length);
         for (int j = 0; j < constants.constant_namespace_set[i].namespaces.length; j++) {
            aos.writeU30(constants.constant_namespace_set[i].namespaces[j]);
         }
      }

      aos.writeU30(constants.constant_multiname.length);
      //System.out.println("Writing "+constants.constant_multiname.length+" multinames");
      for (int i = 1; i < constants.constant_multiname.length; i++) {
         aos.writeMultiname(constants.constant_multiname[i]);
      }

      aos.writeU30(method_info.length);
      for (int i = 0; i < method_info.length; i++) {
         aos.writeMethodInfo(method_info[i]);
      }

      aos.writeU30(metadata_info.length);
      for (int i = 0; i < metadata_info.length; i++) {
         aos.writeU30(metadata_info[i].name_index);
         aos.writeU30(metadata_info[i].values.length);
         for (int j = 0; j < metadata_info[i].values.length; j++) {
            aos.writeU30(metadata_info[i].keys[j]);
         }
         for (int j = 0; j < metadata_info[i].values.length; j++) {
            aos.writeU30(metadata_info[i].values[j]);
         }
      }

      aos.writeU30(class_info.length);
      for (int i = 0; i < instance_info.length; i++) {
         aos.writeInstanceInfo(instance_info[i]);
      }
      for (int i = 0; i < class_info.length; i++) {
         aos.writeU30(class_info[i].cinit_index);
         aos.writeTraits(class_info[i].static_traits);
      }
      aos.writeU30(script_info.length);
      for (int i = 0; i < script_info.length; i++) {
         aos.writeU30(script_info[i].init_index);
         aos.writeTraits(script_info[i].traits);
      }

      aos.writeU30(bodies.length);
      for (int i = 0; i < bodies.length; i++) {
         aos.writeU30(bodies[i].method_info);
         aos.writeU30(bodies[i].max_stack);
         aos.writeU30(bodies[i].max_regs);
         aos.writeU30(bodies[i].init_scope_depth);
         aos.writeU30(bodies[i].max_scope_depth);
         byte codeBytes[] = bodies[i].code.getBytes();
         aos.writeU30(codeBytes.length);
         try {
            aos.write(codeBytes);
         } catch (NotSameException ex) {
            System.out.println(bodies[i].code.toString(constants));
            System.exit(0);
            return;
         }
         aos.writeU30(bodies[i].exceptions.length);
         for (int j = 0; j < bodies[i].exceptions.length; j++) {
            aos.writeU30(bodies[i].exceptions[j].start);
            aos.writeU30(bodies[i].exceptions[j].end);
            aos.writeU30(bodies[i].exceptions[j].target);
            aos.writeU30(bodies[i].exceptions[j].type_index);
            aos.writeU30(bodies[i].exceptions[j].name_index);
         }
         aos.writeTraits(bodies[i].traits);
      }
   }

   private void parseImportFromMultiname(List imports, Multiname m) {
      if (m != null) {
         Namespace ns = m.getNamespace(constants);
         String name = m.getName(constants);
         if (ns != null) {
            String newimport = ns.getName(constants);
            if (!newimport.equals("")) {
               newimport += "." + name;
               if (newimport.contains(":")) {
                  return;
               }
               if (!imports.contains(newimport)) {
                  imports.add(newimport);
               }
            }
         }
      }
   }

   private List getImports(int instanceIndex) {
      List<String> imports = new ArrayList<String>();

      //constructor

      //parseImportFromMultiname(imports, constants.constant_multiname[instance_info[instanceIndex].name_index]);

      if (instance_info[instanceIndex].super_index > 0) {
         parseImportFromMultiname(imports, constants.constant_multiname[instance_info[instanceIndex].super_index]);
      }
      for (int i : instance_info[instanceIndex].interfaces) {
         parseImportFromMultiname(imports, constants.constant_multiname[i]);
      }
      //static
      for (Trait t : class_info[instanceIndex].static_traits.traits) {
         //parseImportFromMultiname(imports, t.getMultiName(constants));
         if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            if (tm.method_info != 0) {
               MethodBody body = findBody(tm.method_info);
               if (body != null) {
                  for (AVM2Instruction ins : body.code.code) {
                     for (int k = 0; k < ins.definition.operands.length; k++) {
                        if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                           int multinameIndex = ins.operands[k];
                           parseImportFromMultiname(imports, constants.constant_multiname[multinameIndex]);
                        }
                     }
                  }
               }
               for (int p = 0; p < method_info[tm.method_info].param_types.length; p++) {
                  if (method_info[tm.method_info].param_types[p] != 0) {
                     parseImportFromMultiname(imports, constants.constant_multiname[method_info[tm.method_info].param_types[p]]);
                  }
                  if (method_info[tm.method_info].ret_type != 0) {
                     parseImportFromMultiname(imports, constants.constant_multiname[method_info[tm.method_info].ret_type]);
                  }
               }
            }
         }

      }
      //instance
      for (Trait t : instance_info[instanceIndex].instance_traits.traits) {
         //parseImportFromMultiname(imports, t.getMultiName(constants));
         if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            if (tm.method_info != 0) {
               MethodBody body = findBody(tm.method_info);
               if (body != null) {
                  for (AVM2Instruction ins : body.code.code) {
                     for (int k = 0; k < ins.definition.operands.length; k++) {
                        if (ins.definition.operands[k] == AVM2Code.DAT_MULTINAME_INDEX) {
                           int multinameIndex = ins.operands[k];
                           parseImportFromMultiname(imports, constants.constant_multiname[multinameIndex]);
                        }
                     }
                  }
               }
               for (int p = 0; p < method_info[tm.method_info].param_types.length; p++) {
                  if (method_info[tm.method_info].param_types[p] != 0) {
                     parseImportFromMultiname(imports, constants.constant_multiname[method_info[tm.method_info].param_types[p]]);
                  }
                  if (method_info[tm.method_info].ret_type != 0) {
                     parseImportFromMultiname(imports, constants.constant_multiname[method_info[tm.method_info].ret_type]);
                  }
               }
            }
         }
      }
      return imports;
   }

   public MethodBody findBody(int methodInfo) {
      int pos = findBodyIndex(methodInfo);
      if (pos == -1) {
         return null;
      } else {
         return bodies[pos];
      }
   }

   public int findBodyIndex(int methodInfo) {
      if (methodInfo == -1) {
         return -1;
      }
      for (int b = 0; b < bodies.length; b++) {
         if (bodies[b].method_info == methodInfo) {
            return b;
         }
      }
      return -1;
   }

   public MethodBody findBodyByClassAndName(String className, String methodName) {
      for (int i = 0; i < instance_info.length; i++) {
         if (className.equals(constants.constant_multiname[instance_info[i].name_index].getName(constants))) {
            for (Trait t : instance_info[i].instance_traits.traits) {
               if (t instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                  if (methodName.equals(t2.getMethodName(constants))) {
                     for (MethodBody body : bodies) {
                        if (body.method_info == t2.method_info) {
                           return body;
                        }
                     }
                  }
               }
            }
            //break;
         }
      }
      for (int i = 0; i < class_info.length; i++) {
         if (className.equals(constants.constant_multiname[instance_info[i].name_index].getName(constants))) {
            for (Trait t : class_info[i].static_traits.traits) {
               if (t instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                  if (methodName.equals(t2.getMethodName(constants))) {
                     for (MethodBody body : bodies) {
                        if (body.method_info == t2.method_info) {
                           return body;
                        }
                     }
                  }
               }
            }
            //break;
         }
      }


      return null;
   }

   private String addTabs(String s, int tabs) {
      String parts[] = s.split("\r\n");
      if (!s.contains("\r\n")) {
         parts = s.split("\n");
      }
      String ret = "";
      for (int i = 0; i < parts.length; i++) {
         for (int t = 0; t < tabs; t++) {
            ret += IDENT_STRING;
         }
         ret += parts[i];
         if (i < parts.length - 1) {
            ret += "\r\n";
         }
      }
      return ret;
   }

   public int findMethodIdByTraitId(int classIndex, int traitId) {
      if (traitId < class_info[classIndex].static_traits.traits.length) {
         if (class_info[classIndex].static_traits.traits[traitId] instanceof TraitMethodGetterSetter) {
            return ((TraitMethodGetterSetter) class_info[classIndex].static_traits.traits[traitId]).method_info;
         } else {
            return -1;
         }
      } else if (traitId < class_info[classIndex].static_traits.traits.length + instance_info[classIndex].instance_traits.traits.length) {
         traitId -= class_info[classIndex].static_traits.traits.length;
         if (instance_info[classIndex].instance_traits.traits[traitId] instanceof TraitMethodGetterSetter) {
            return ((TraitMethodGetterSetter) instance_info[classIndex].instance_traits.traits[traitId]).method_info;
         } else {
            return -1;
         }
      } else {
         traitId -= class_info[classIndex].static_traits.traits.length + instance_info[classIndex].instance_traits.traits.length;
         if (traitId == 0) {
            return instance_info[classIndex].iinit_index;
         } else if (traitId == 1) {
            return class_info[classIndex].cinit_index;
         } else {
            return -1;
         }
      }
   }

   public String classToString(int i, boolean highlight, boolean pcode) {
      String ret = "";
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream out = new PrintStream(baos);
      String packageName = instance_info[i].getName(constants).getNamespace(constants).getName(constants);
      out.println("package " + packageName);
      out.println("{");

      //imports
      List<String> imports = getImports(i);
      for (String imp : imports) {
         out.println(IDENT_STRING + "import " + imp + ";");
      }
      out.println();

      //class header
      String classHeader = instance_info[i].getClassHeaderStr(constants);
      if (classHeader.startsWith("private ")) {
         classHeader = "public " + classHeader.substring("private ".length());
      }
      out.println(IDENT_STRING + classHeader);
      out.println(IDENT_STRING + "{");

      String toPrint="";

      //if (class_info[i].cinit_index != 0) {
      if (AUTOINIT_STATIC_VARIABLES) {
         int bodyIndex = findBodyIndex(class_info[i].cinit_index);
         List<TreeItem> initializer = bodies[bodyIndex].code.toTree(true, i, this, constants, method_info, bodies[bodyIndex]);
         for (TreeItem ti : initializer) {
            if (ti instanceof SetPropertyTreeItem) {
               int multinameIndex = ((SetPropertyTreeItem) ti).propertyName.multinameIndex;
               TreeItem value = ((SetPropertyTreeItem) ti).value;
               for (Trait t : class_info[i].static_traits.traits) {
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
               for (Trait t : class_info[i].static_traits.traits) {
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
      int bodyIndex = findBodyIndex(class_info[i].cinit_index);
      if (bodyIndex != -1) {
         bodyStr = addTabs(bodies[bodyIndex].toString(pcode,true,i,this, constants, method_info, highlight), 3);
      }
     // if (!bodyStr.equals("")) {
         toPrint = IDENT_STRING + IDENT_STRING + "{\r\n" + bodyStr + "\r\n" + IDENT_STRING + IDENT_STRING + "}";
         if (highlight) {
            toPrint = Highlighting.hilighTrait(toPrint, class_info[i].static_traits.traits.length + instance_info[i].instance_traits.traits.length + 1);
         }
         out.println(toPrint);
      //}
      //}

      //constructor
      //if (instance_info[i].iinit_index != 0) {
      String modifier = "";
      Multiname m = constants.constant_multiname[instance_info[i].name_index];
      if (m != null) {
         Namespace ns = m.getNamespace(constants);
         if (ns != null) {
            modifier = ns.getPrefix(constants) + " ";
            if (modifier.equals(" ")) {
               modifier = "";
            }
         }
      }
      String constructorParams = "";

      bodyStr = "";
      bodyIndex = findBodyIndex(instance_info[i].iinit_index);
      if (bodyIndex != -1) {
         bodyStr = addTabs(bodies[bodyIndex].toString(pcode, false, i, this, constants, method_info, highlight), 3);
         constructorParams = method_info[instance_info[i].iinit_index].getParamStr(constants, bodies[bodyIndex], this);
      } else {
         constructorParams = method_info[instance_info[i].iinit_index].getParamStr(constants, null, this);
      }
      toPrint = IDENT_STRING + IDENT_STRING + modifier + "function " + constants.constant_multiname[instance_info[i].name_index].getName(constants) + "(" + constructorParams + ") {\r\n" + bodyStr + "\r\n" + IDENT_STRING + IDENT_STRING + "}";
      if (highlight) {
         toPrint = Highlighting.hilighTrait(toPrint, class_info[i].static_traits.traits.length + instance_info[i].instance_traits.traits.length);
      }
      out.println(toPrint);
      //}

      //static variables,constants & methods
      for (int ti = 0; ti < class_info[i].static_traits.traits.length; ti++) {
         Trait t = class_info[i].static_traits.traits[ti];
         toPrint = "";
         if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            bodyStr = "";
            bodyIndex = findBodyIndex(tm.method_info);
            if (bodyIndex != -1) {
               bodyStr = addTabs(bodies[bodyIndex].toString(pcode, true, i, this, constants, method_info, highlight), 3);
            }
            toPrint = IDENT_STRING + IDENT_STRING + tm.convert(constants, method_info, this, true) + " {\r\n" + bodyStr + "\r\n" + IDENT_STRING + IDENT_STRING + "}";
         }
         if (t instanceof TraitSlotConst) {
            TraitSlotConst ts = (TraitSlotConst) t;

            toPrint = IDENT_STRING + IDENT_STRING + ts.convert(constants, method_info, this, true) + ";";
         }
         if (highlight) {
            toPrint = Highlighting.hilighTrait(toPrint, ti);
         } else {
            toPrint = Highlighting.stripHilights(toPrint);
         }
         out.println(toPrint);
      }
      for (int ti = 0; ti < instance_info[i].instance_traits.traits.length; ti++) {
         Trait t = instance_info[i].instance_traits.traits[ti];
         toPrint = "";
         if (t instanceof TraitSlotConst) {
            TraitSlotConst ts = (TraitSlotConst) t;
            toPrint = IDENT_STRING + IDENT_STRING + ts.convert(constants, method_info, this, false) + ";";
         }

         if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
            bodyStr = "";
            bodyIndex = findBodyIndex(tm.method_info);
            if (bodyIndex != -1) {
               bodyStr = addTabs(bodies[bodyIndex].toString(pcode, false, i, this, constants, method_info, highlight), 3);
            }
            toPrint = IDENT_STRING + IDENT_STRING + tm.convert(constants, method_info, this, false) + " {\r\n" + bodyStr + "\r\n" + IDENT_STRING + IDENT_STRING + "}";
         }
         if (highlight) {
            toPrint = Highlighting.hilighTrait(toPrint, class_info[i].static_traits.traits.length + ti);
         } else {
            toPrint = Highlighting.stripHilights(toPrint);
         }
         out.println(toPrint);
      }


      out.println(IDENT_STRING + "}");//class
      out.println("}");//package
      out.flush();

      return baos.toString();
   }

   public void export(String directory, boolean pcode) throws IOException {
      for (int i = 0; i < instance_info.length; i++) {
         String packageName = instance_info[i].getName(constants).getNamespace(constants).getName(constants);
         String className = instance_info[i].getName(constants).getName(constants);
         Main.startWork("Exporting " + (i + 1) + "/" + instance_info.length + " " + packageName + "." + className + "...");
         File outDir = new File(directory + File.separatorChar + packageName.replace('.', File.separatorChar));
         if (!outDir.exists()) {
            outDir.mkdirs();
         }
         String fileName = outDir.toString() + File.separator + className + ".as";
         FileOutputStream fos = new FileOutputStream(fileName);
         fos.write(classToString(i, false, pcode).getBytes());
         fos.close();
      }

   }

   public void dump(OutputStream os) {
      PrintStream output = new PrintStream(os);
      constants.dump(output);
      for (int i = 0; i < method_info.length; i++) {
         output.println("MethodInfo[" + i + "]:" + method_info[i].toString(constants));
      }
      for (int i = 0; i < metadata_info.length; i++) {
         output.println("MetadataInfo[" + i + "]:" + metadata_info[i].toString(constants));
      }
      for (int i = 0; i < instance_info.length; i++) {
         output.println("InstanceInfo[" + i + "]:" + instance_info[i].toString(this));
      }
      for (int i = 0; i < class_info.length; i++) {
         output.println("ClassInfo[" + i + "]:" + class_info[i].toString(this));
      }
      for (int i = 0; i < script_info.length; i++) {
         output.println("ScriptInfo[" + i + "]:" + script_info[i].toString(this));
      }
      for (int i = 0; i < bodies.length; i++) {
         output.println("MethodBody[" + i + "]:"); //+ bodies[i].toString(this, constants, method_info));
      }
   }
   public static final String[] reservedWords = {
      "as", "break", "case", "catch", "class", "const", "continue", "default", "delete", "do", "each", "else",
      "extends", "false", "finally", "for", "function", "if", "implements", "import", "in", "instanceof",
      "interface", "internal", "is", "native", "new", "null", "package", "private", "protected", "public",
      "return", "super", "switch", "this", "throw", "true", "try", "typeof", "use", "var", /*"void",*/ "while",
      "with", "dynamic", "default", "final", "in"};
   public int unknownCount = 0;

   public boolean isValidName(int index, boolean autoFix) {
      if (index <= 0) {
         return true;
      }
      String s = constants.constant_string[index];
      boolean isValid = true;
      boolean isReserved = false;
      for (String rw : reservedWords) {
         if (rw.equals(s.trim())) {
            isValid = false;
            isReserved = true;
            break;
         }
      }
      if (isValid) {
         for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) {
               isValid = false;
               break;
            }
         }
      }

      if (!isValid) {
         if (autoFix) {
            if (isReserved) {
               constants.constant_string[index] = "name_" + s.replace(" ", "_");
            } else {
               unknownCount++;
               constants.constant_string[index] = "_name" + unknownCount;
            }
         }
      }
      return isValid;
   }

   public List<Usage> findMultinameUsage(int multinameIndex) {
      List<Usage> ret = new ArrayList<Usage>();
      List<Integer> bodyIndices = new ArrayList<Integer>();
      List<Integer> subTraitBodyIndices = new ArrayList<Integer>();
      List<Integer> subTraitIndexIndices = new ArrayList<Integer>();
      for (int i = 0; i < bodies.length; i++) {
         for (int t = 0; t < bodies[i].traits.traits.length; t++) {
            Trait tr = bodies[i].traits.traits[t];
            if (tr.name_index == multinameIndex) {
               subTraitBodyIndices.add(i);
               subTraitIndexIndices.add(t);
            }
         }
         loopbody:
         for (AVM2Instruction ins : bodies[i].code.code) {
            for (int op = 0; op < ins.definition.operands.length; op++) {
               if (ins.definition.operands[op] == AVM2Code.DAT_MULTINAME_INDEX) {
                  if (ins.operands[op] == multinameIndex) {
                     bodyIndices.add(i);
                     break loopbody;
                  }
               }
            }
         }
      }


      for (int c = 0; c < class_info.length; c++) {
         if (instance_info[c].name_index == multinameIndex) {
            ret.add(new Usage(this, -1, c, -1, -1, false, Usage.TYPE_CLASS_NAME));
         }
         for (int t = 0; t < class_info[c].static_traits.traits.length; t++) {
            Trait tr = class_info[c].static_traits.traits[t];
            if (tr.name_index == multinameIndex) {
               ret.add(new Usage(this, -1, c, t, -1, true, Usage.TYPE_TRAIT_NAME));
            }
         }
         for (int t = 0; t < instance_info[c].instance_traits.traits.length; t++) {
            Trait tr = instance_info[c].instance_traits.traits[t];
            if (tr.name_index == multinameIndex) {
               ret.add(new Usage(this, -1, c, t, -1, false, Usage.TYPE_TRAIT_NAME));
            }
         }
      }
      for (int bodyIndex : bodyIndices) {
         for (int c = 0; c < class_info.length; c++) {
            if (class_info[c].cinit_index == bodyIndex) {
               ret.add(new Usage(this, bodyIndex, c, -1, -1, true, Usage.TYPE_INITIALIZER));
            }
            if (instance_info[c].iinit_index == bodyIndex) {
               ret.add(new Usage(this, bodyIndex, c, -1, -1, false, Usage.TYPE_INITIALIZER));
            }
            for (int t = 0; t < class_info[c].static_traits.traits.length; t++) {
               Trait tr = class_info[c].static_traits.traits[t];
               if (tr instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) tr;
                  if (tmgs.method_info == bodies[bodyIndex].method_info) {
                     ret.add(new Usage(this, bodyIndex, c, t, -1, true, Usage.TYPE_TRAIT_BODY));
                  }
               }
            }
            for (int t = 0; t < instance_info[c].instance_traits.traits.length; t++) {
               Trait tr = instance_info[c].instance_traits.traits[t];
               if (tr instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) tr;
                  if (tmgs.method_info == bodies[bodyIndex].method_info) {
                     ret.add(new Usage(this, bodyIndex, c, t, -1, false, Usage.TYPE_TRAIT_BODY));
                  }
               }
            }
         }
      }


      for (int b = 0; b < subTraitBodyIndices.size(); b++) {
         int bodyIndex = subTraitBodyIndices.get(b);
         for (int c = 0; c < class_info.length; c++) {
            if (class_info[c].cinit_index == bodyIndex) {
               ret.add(new Usage(this, bodyIndex, c, -1, subTraitIndexIndices.get(b), true, Usage.TYPE_INITIALIZER_SUBTRAIT_NAME));
            }
            if (instance_info[c].iinit_index == bodyIndex) {
               ret.add(new Usage(this, bodyIndex, c, -1, subTraitIndexIndices.get(b), false, Usage.TYPE_INITIALIZER_SUBTRAIT_NAME));
            }
            for (int t = 0; t < class_info[c].static_traits.traits.length; t++) {
               Trait tr = class_info[c].static_traits.traits[t];
               if (tr instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) tr;
                  if (tmgs.method_info == bodies[bodyIndex].method_info) {
                     ret.add(new Usage(this, bodyIndex, c, t, subTraitIndexIndices.get(b), true, Usage.TYPE_SUBTRAIT_NAME));
                  }
               }
            }
            for (int t = 0; t < instance_info[c].instance_traits.traits.length; t++) {
               Trait tr = instance_info[c].instance_traits.traits[t];
               if (tr instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) tr;
                  if (tmgs.method_info == bodies[bodyIndex].method_info) {
                     ret.add(new Usage(this, bodyIndex, c, t, subTraitIndexIndices.get(b), false, Usage.TYPE_SUBTRAIT_NAME));
                  }
               }
            }
         }
      }
      return ret;
   }
}
