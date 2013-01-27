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
package com.jpexs.asdec.abc;

import com.jpexs.asdec.EventListener;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.UnknownInstructionCode;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.types.*;
import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import com.jpexs.asdec.abc.types.traits.Traits;
import com.jpexs.asdec.abc.usages.*;
import com.jpexs.asdec.tags.DoABCTag;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
   private int bodyIdxFromMethodIdx[];
   public long stringOffsets[];
   public static String IDENT_STRING = "   ";
   public static final int MINORwithDECIMAL = 17;
   protected HashSet<EventListener> listeners = new HashSet<EventListener>();
   private static Logger logger = Logger.getLogger(ABC.class.getName());

   public void addEventListener(EventListener listener) {
      listeners.add(listener);
   }

   public void removeEventListener(EventListener listener) {
      listeners.remove(listener);
   }

   protected void informListeners(String event, Object data) {
      for (EventListener listener : listeners) {
         listener.handleEvent(event, data);
      }
   }

   public int removeTraps() {
      int rem=0;
      for(MethodBody body:bodies){
         rem+=body.removeTraps(constants);
      }
      return rem;
   }
   
   public int removeDeadCode() {
      int rem=0;
      for(MethodBody body:bodies){
         rem+=body.removeDeadCode(constants);
      }
      return rem;
   }
   
   public int deobfuscateIdentifiers() {
      int ret = 0;
      for (int i = 1; i < instance_info.length; i++) {
         if (instance_info[i].name_index != 0) {
            if (deobfuscateName(constants.constant_multiname[instance_info[i].name_index].name_index, true)) {
               ret++;
            }
         }
         if (instance_info[i].super_index != 0) {
            if (deobfuscateName(constants.constant_multiname[instance_info[i].super_index].name_index, true)) {
               ret++;
            }
         }
      }
      for (int i = 1; i < constants.constant_multiname.length; i++) {
         if (deobfuscateName(constants.constant_multiname[i].name_index, false)) {
            ret++;
         }
      }
      for (int i = 1; i < constants.constant_namespace.length; i++) {
         if (deobfuscateNameSpace(constants.constant_namespace[i].name_index)) {
            ret++;
         }
      }
      return ret;
   }

   public ABC(InputStream is) throws IOException {
      ABCInputStream ais = new ABCInputStream(is);
      minor_version = ais.readU16();
      major_version = ais.readU16();
      logger.log(Level.FINE, "ABC minor_version: {0}, major_version: {1}", new Object[]{minor_version, major_version});
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
      }


      //method info
      int methods_count = ais.readU30();
      method_info = new MethodInfo[methods_count];
      bodyIdxFromMethodIdx = new int[methods_count];
      for (int i = 0; i < methods_count; i++) {
         method_info[i] = ais.readMethodInfo();
         bodyIdxFromMethodIdx[i] = -1;
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
         ClassInfo ci = new ClassInfo();
         ci.cinit_index = ais.readU30();
         ci.static_traits = ais.readTraits();
         class_info[i] = ci;
      }
      int script_count = ais.readU30();
      script_info = new ScriptInfo[script_count];
      for (int i = 0; i < script_count; i++) {
         ScriptInfo si = new ScriptInfo();
         si.init_index = ais.readU30();
         si.traits = ais.readTraits();
         script_info[i] = si;
      }

      int bodies_count = ais.readU30();
      bodies = new MethodBody[bodies_count];
      for (int i = 0; i < bodies_count; i++) {
         MethodBody mb = new MethodBody();
         mb.method_info = ais.readU30();
         mb.max_stack = ais.readU30();
         mb.max_regs = ais.readU30();
         mb.init_scope_depth = ais.readU30();
         mb.max_scope_depth = ais.readU30();
         int code_length = ais.readU30();
         mb.codeBytes = new byte[code_length];
         ais.read(mb.codeBytes);
         try {
            mb.code = new AVM2Code(new ByteArrayInputStream(mb.codeBytes));
         } catch (UnknownInstructionCode re) {
            mb.code = new AVM2Code();
            System.err.println(re.toString());
         }
         mb.code.compact();
         int ex_count = ais.readU30();
         mb.exceptions = new ABCException[ex_count];
         for (int j = 0; j < ex_count; j++) {
            ABCException abce = new ABCException();
            abce.start = ais.readU30();
            abce.end = ais.readU30();
            abce.target = ais.readU30();
            abce.type_index = ais.readU30();
            abce.name_index = ais.readU30();
            mb.exceptions[j] = abce;
         }
         mb.traits = ais.readTraits();
         bodies[i] = mb;
         method_info[mb.method_info].setBody(mb);
         bodyIdxFromMethodIdx[mb.method_info] = i;
      }
      loadNamespaceMap();
      /* for(ScriptInfo si:script_info){         
       System.out.println("--------------------------------------------");
       System.out.println(findBody(si.init_index).toString(true, false, -1, this, constants, method_info,new Stack<TreeItem>(),false,false));
       System.out.println("sitrait:"+si.traits.toString(this));
       }*/
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

   public MethodBody findBody(int methodInfo) {
      if (methodInfo < 0) {
         return null;
      }
      return method_info[methodInfo].getBody();
   }

   public int findBodyIndex(int methodInfo) {
      if (methodInfo == -1) {
         return -1;
      }
      return bodyIdxFromMethodIdx[methodInfo];
   }

   public MethodBody findBodyByClassAndName(String className, String methodName) {
      for (int i = 0; i < instance_info.length; i++) {
         if (className.equals(constants.constant_multiname[instance_info[i].name_index].getName(constants, new ArrayList<String>()))) {
            for (Trait t : instance_info[i].instance_traits.traits) {
               if (t instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                  if (methodName.equals(t2.getName(this).getName(constants, new ArrayList<String>()))) {
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
         if (className.equals(constants.constant_multiname[instance_info[i].name_index].getName(constants, new ArrayList<String>()))) {
            for (Trait t : class_info[i].static_traits.traits) {
               if (t instanceof TraitMethodGetterSetter) {
                  TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
                  if (methodName.equals(t2.getName(this).getName(constants, new ArrayList<String>()))) {
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

   public static String addTabs(String s, int tabs) {
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

   public Trait findTraitByTraitId(int classIndex, int traitId) {
      if (traitId < class_info[classIndex].static_traits.traits.length) {
         return class_info[classIndex].static_traits.traits[traitId];
      } else if (traitId < class_info[classIndex].static_traits.traits.length + instance_info[classIndex].instance_traits.traits.length) {
         traitId -= class_info[classIndex].static_traits.traits.length;
         return instance_info[classIndex].instance_traits.traits[traitId];
      } else {
         return null; //Can be class or instance initializer
      }
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
   /* Map from multiname index of namespace value to namespace name**/
   private HashMap<String, String> namespaceMap;

   private void loadNamespaceMap() {
      namespaceMap = new HashMap<String, String>();
      for (ScriptInfo si : script_info) {
         for (Trait t : si.traits.traits) {
            if (t instanceof TraitSlotConst) {
               TraitSlotConst s = ((TraitSlotConst) t);
               if (s.isNamespace()) {
                  String key = constants.constant_namespace[s.value_index].getName(constants);
                  String val = constants.constant_multiname[s.name_index].getNameWithNamespace(constants);
                  namespaceMap.put(key, val);
               }
            }
         }
      }
   }

   public String builtInNs(String ns) {
      if (ns.equals("http://www.adobe.com/2006/actionscript/flash/proxy")) {
         return "flash.utils.flash_proxy";
      }
      if (ns.equals("http://adobe.com/AS3/2006/builtin")) {
         return "-";
      }
      return null;
   }

   public String nsValueToName(String value) {
      if (namespaceMap.containsKey(value)) {
         return namespaceMap.get(value);
      } else {
         String ns = builtInNs(value);
         if (ns == null) {
            return "";
         } else {
            return ns;
         }
      }
   }

   public void export(String directory, boolean pcode, List<DoABCTag> abcList) throws IOException {
      export(directory, pcode, abcList, "");
   }

   public void export(String directory, boolean pcode, List<DoABCTag> abcList, String abcStr) throws IOException {
      for (int i = 0; i < script_info.length; i++) {
         String path = script_info[i].getPath(this);
         String packageName = path.substring(0, path.lastIndexOf("."));
         if (packageName.equals("")) {
            path = path.substring(1);
         }
         String cnt = "";
         if (script_info.length > 1) {
            cnt = "script " + (i + 1) + "/" + script_info.length + " ";
         }
         String exStr = "Exporting " + abcStr + cnt + path + " ...";
         informListeners("export", exStr);
         script_info[i].export(this, abcList, directory, pcode);
      }
   }

   public void dump(OutputStream os) {
      PrintStream output = new PrintStream(os);
      constants.dump(output);
      for (int i = 0; i < method_info.length; i++) {
         output.println("MethodInfo[" + i + "]:" + method_info[i].toString(constants, new ArrayList<String>()));
      }
      for (int i = 0; i < metadata_info.length; i++) {
         output.println("MetadataInfo[" + i + "]:" + metadata_info[i].toString(constants));
      }
      for (int i = 0; i < instance_info.length; i++) {
         output.println("InstanceInfo[" + i + "]:" + instance_info[i].toString(this, new ArrayList<String>()));
      }
      for (int i = 0; i < class_info.length; i++) {
         output.println("ClassInfo[" + i + "]:" + class_info[i].toString(this, new ArrayList<String>()));
      }
      for (int i = 0; i < script_info.length; i++) {
         output.println("ScriptInfo[" + i + "]:" + script_info[i].toString(this, new ArrayList<String>()));
      }
      for (int i = 0; i < bodies.length; i++) {
         output.println("MethodBody[" + i + "]:"); //+ bodies[i].toString(this, constants, method_info));
      }
   }
   public static final String[] reservedWords = {
      "as", "break", "case", "catch", "class", "const", "continue", "default", "delete", "do", "each", "else",
      "extends", "false", "finally", "for", "function", "get","if", "implements", "import", "in", "instanceof",
      "interface", "internal", "is", "native", "new", "null","override", "package", "private", "protected", "public",
      "return", "set","super", "switch", "this", "throw", "true", "try", "typeof", "use", "var", /*"void",*/ "while",
      "with", "dynamic", "default", "final", "in"};
   public static final String validFirstCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
   public static final String validNextCharacters = validFirstCharacters + "0123456789";
   public static final String validNsCharacters = ".:$";
   public static final String fooCharacters = "bcdfghjklmnpqrstvwz";
   public static final String fooJoinCharacters = "aeiouy";
   private HashMap<String, String> deobfuscated = new HashMap<String, String>();
   private Random rnd = new Random();
   private final int DEFAULT_FOO_SIZE = 10;

   private String fooString(String orig, boolean firstUppercase, int rndSize) {
      boolean exists;
      String ret;
      loopfoo:
      do {
         exists = false;
         int len = 3 + rnd.nextInt(rndSize - 3);
         ret = "";
         for (int i = 0; i < len; i++) {
            String c = "";
            if ((i % 2) == 0) {
               c = "" + fooCharacters.charAt(rnd.nextInt(fooCharacters.length()));
            } else {
               c = "" + fooJoinCharacters.charAt(rnd.nextInt(fooJoinCharacters.length()));
            }
            if (i == 0 && firstUppercase) {
               c = c.toUpperCase();
            }
            ret += c;
         }
         for (int i = 1; i < constants.constant_string.length; i++) {
            if (constants.constant_string[i].equals(ret)) {
               exists = true;
               rndSize = rndSize + 1;
               continue loopfoo;
            }
         }
         if (isReserved(ret)) {
            exists = true;
            rndSize = rndSize + 1;
            continue;
         }
         if (deobfuscated.containsValue(ret)) {
            exists = true;
            rndSize = rndSize + 1;
            continue;
         }
      } while (exists);
      deobfuscated.put(orig, ret);
      return ret;
   }

   private boolean isReserved(String s) {
      for (String rw : reservedWords) {
         if (rw.equals(s.trim())) {
            return true;
         }
      }
      return false;
   }

   public boolean deobfuscateNameSpace(int strIndex) {
      if (strIndex <= 0) {
         return false;
      }
      String s = constants.constant_string[strIndex];
      boolean isValid = true;
      if (isReserved(s)) {
         isValid = false;
      }

      if (isValid) {
         for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) {
               isValid = false;
               break;
            }
         }
      }
      Pattern pat = Pattern.compile("^([" + Pattern.quote(validFirstCharacters) + "]" + "[" + Pattern.quote(validFirstCharacters + validNextCharacters + validNsCharacters) + "]*)*$");
      if (!pat.matcher(s).matches()) {
         isValid = false;
      }
      if (!isValid) {
         constants.constant_string[strIndex] = fooString(constants.constant_string[strIndex], false, DEFAULT_FOO_SIZE);
      }
      return !isValid;
   }

   public boolean deobfuscateName(int strIndex, boolean firstUppercase) {
      if (strIndex <= 0) {
         return false;
      }
      String s = constants.constant_string[strIndex];
      boolean isValid = true;
      if (isReserved(s)) {
         isValid = false;
      }


      Pattern pat = Pattern.compile("^[" + Pattern.quote(validFirstCharacters) + "]" + "[" + Pattern.quote(validFirstCharacters + validNextCharacters) + "]*$");
      if (!pat.matcher(s).matches()) {
         isValid = false;
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
         constants.constant_string[strIndex] = fooString(constants.constant_string[strIndex], firstUppercase, DEFAULT_FOO_SIZE);
      }
      return !isValid;
   }

   private void checkMultinameUsedInMethod(int multinameIndex, int methodInfo, List<MultinameUsage> ret, int classIndex, int traitIndex, boolean isStatic, boolean isInitializer, Traits traits, int parentTraitIndex) {
      for (int p = 0; p < method_info[methodInfo].param_types.length; p++) {
         if (method_info[methodInfo].param_types[p] == multinameIndex) {
            ret.add(new MethodParamsMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
            break;
         }
      }
      if (method_info[methodInfo].ret_type == multinameIndex) {
         ret.add(new MethodReturnTypeMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
      }
      MethodBody body = findBody(methodInfo);
      if (body != null) {
         findMultinameUsageInTraits(body.traits, multinameIndex, isStatic, classIndex, ret, traitIndex);
         for (ABCException e : body.exceptions) {
            if ((e.name_index == multinameIndex) || (e.type_index == multinameIndex)) {
               ret.add(new MethodBodyMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
               return;
            }
         }
         for (AVM2Instruction ins : body.code.code) {
            for (int o = 0; o < ins.definition.operands.length; o++) {
               if (ins.definition.operands[o] == AVM2Code.DAT_MULTINAME_INDEX) {
                  if (ins.operands[o] == multinameIndex) {
                     ret.add(new MethodBodyMultinameUsage(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex));
                     return;
                  }
               }
            }
         }
      }
   }

   private void findMultinameUsageInTraits(Traits traits, int multinameIndex, boolean isStatic, int classIndex, List<MultinameUsage> ret, int parentTraitIndex) {
      for (int t = 0; t < traits.traits.length; t++) {
         if (traits.traits[t] instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) traits.traits[t];
            if (tsc.name_index == multinameIndex) {
               ret.add(new ConstVarNameMultinameUsage(multinameIndex, classIndex, t, isStatic, traits, parentTraitIndex));
            }
            if (tsc.type_index == multinameIndex) {
               ret.add(new ConstVarTypeMultinameUsage(multinameIndex, classIndex, t, isStatic, traits, parentTraitIndex));
            }
         }
         if (traits.traits[t] instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) traits.traits[t];
            if (tmgs.name_index == multinameIndex) {
               ret.add(new MethodNameMultinameUsage(multinameIndex, classIndex, t, isStatic, false, traits, parentTraitIndex));
            }
            checkMultinameUsedInMethod(multinameIndex, tmgs.method_info, ret, classIndex, t, isStatic, false, traits, parentTraitIndex);
         }
      }
   }

   public List<MultinameUsage> findMultinameUsage(int multinameIndex) {
      List<MultinameUsage> ret = new ArrayList<MultinameUsage>();
      if (multinameIndex == 0) {
         return ret;
      }
      for (int c = 0; c < instance_info.length; c++) {
         if (instance_info[c].name_index == multinameIndex) {
            ret.add(new ClassNameMultinameUsage(multinameIndex, c));
         }
         if (instance_info[c].super_index == multinameIndex) {
            ret.add(new ExtendsMultinameUsage(multinameIndex, c));
         }
         for (int i = 0; i < instance_info[c].interfaces.length; i++) {
            if (instance_info[c].interfaces[i] == multinameIndex) {
               ret.add(new ImplementsMultinameUsage(multinameIndex, c));
            }
         }
         checkMultinameUsedInMethod(multinameIndex, instance_info[c].iinit_index, ret, c, 0, false, true, null, -1);
         checkMultinameUsedInMethod(multinameIndex, class_info[c].cinit_index, ret, c, 0, true, true, null, -1);
         findMultinameUsageInTraits(instance_info[c].instance_traits, multinameIndex, false, c, ret, -1);
         findMultinameUsageInTraits(class_info[c].static_traits, multinameIndex, true, c, ret, -1);
      }
      loopm:
      for (int m = 1; m < constants.constant_multiname.length; m++) {
         if (constants.constant_multiname[m].kind == Multiname.TYPENAME) {
            if (constants.constant_multiname[m].qname_index == multinameIndex) {
               ret.add(new TypeNameMultinameUsage(m));
               continue;
            }
            for (int mp : constants.constant_multiname[m].params) {
               if (mp == multinameIndex) {
                  ret.add(new TypeNameMultinameUsage(m));
                  continue loopm;
               }
            }
         }
      }
      return ret;
   }

   public void autoFillAllBodyParams() {
      for (int i = 0; i < bodies.length; i++) {
         bodies[i].autoFillStats(this);
      }
   }
}
