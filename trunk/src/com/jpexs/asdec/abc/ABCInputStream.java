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

import com.jpexs.asdec.abc.types.*;
import com.jpexs.asdec.abc.types.traits.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ABCInputStream extends InputStream {

   private static final int CLASS_PROTECTED_NS = 8;
   private static final int ATTR_METADATA = 4;
   private InputStream is;
   private long bytesRead = 0;
   private ByteArrayOutputStream bufferOs = null;
   public static boolean DEBUG_READ = false;

   public void startBuffer() {
      bufferOs = new ByteArrayOutputStream();
   }

   public byte[] stopBuffer() {
      if (bufferOs == null) {
         return new byte[0];
      }
      byte ret[] = bufferOs.toByteArray();
      bufferOs = null;
      return ret;
   }

   public ABCInputStream(InputStream is) {
      this.is = is;
   }

   @Override
   public int read() throws IOException {
      bytesRead++;
      int i = is.read();
      if (DEBUG_READ) {
         System.out.println("Read:0x" + Integer.toHexString(i));
      }
      if (bufferOs != null) {
         if (i != -1) {
            bufferOs.write(i);
         }
      }
      return i;
   }

   @Override
   public int read(byte[] b) throws IOException {
      int currBytesRead = is.read(b);
      bytesRead += currBytesRead;
      if (DEBUG_READ) {
         StringBuilder sb = new StringBuilder("Read[");
         sb.append(currBytesRead);
         sb.append('/');
         sb.append(b.length);
         sb.append("]: ");
         for (int jj = 0; jj < currBytesRead; jj++) {
            sb.append("0x");
            sb.append(Integer.toHexString(b[jj]));
            sb.append(' ');
         }
         System.out.println(sb.toString());
      }
      if (bufferOs != null) {
         if (currBytesRead > 0) {
            bufferOs.write(b, 0, currBytesRead);
         }
      }
      return currBytesRead;
   }

   ;

    public int readU8() throws IOException {
      return read();
   }

   public int readU32() throws IOException {
      int i;
      int ret = 0;
      int bytePos = 0;
      int byteCount = 0;
      boolean nextByte;
      do {
         i = read();
         nextByte = (i >> 7) == 1;
         i = i & 0x7f;
         ret = ret + (i << bytePos);
         byteCount++;
         bytePos += 7;
      } while (nextByte);
      return ret;
   }

   public int readU30() throws IOException {
      return readU32();
   }

   public int readS24() throws IOException {
      int ret = (read()) + (read() << 8) + (read() << 16);

      if ((ret >> 23) == 1) {
         ret = ret | 0xff000000;
      }

      return ret;
   }

   public int readU16() throws IOException {
      return (read()) + (read() << 8);
   }

   public long readS32() throws IOException {
      int i;
      long ret = 0;
      int bytePos = 0;
      int byteCount = 0;
      boolean nextByte;
      do {
         i = read();
         nextByte = (i >> 7) == 1;
         i = i & 0x7f;
         ret = ret + (i << bytePos);
         byteCount++;
         bytePos += 7;
         if (bytePos == 35) {
            if ((ret >> 31) == 1) {
               ret = -(ret & 0x7fffffff);
            }
            break;
         }
      } while (nextByte);
      return ret;
   }

   @Override
   public int available() throws IOException {
      return is.available();
   }

   public final long readLong() throws IOException {
      byte readBuffer[] = safeRead(8);
      return (((long) readBuffer[7] << 56)
              + ((long) (readBuffer[6] & 255) << 48)
              + ((long) (readBuffer[5] & 255) << 40)
              + ((long) (readBuffer[4] & 255) << 32)
              + ((long) (readBuffer[3] & 255) << 24)
              + ((readBuffer[2] & 255) << 16)
              + ((readBuffer[1] & 255) << 8)
              + ((readBuffer[0] & 255)));
   }

   public double readDouble() throws IOException {
      long el = readLong();
      double ret = Double.longBitsToDouble(el);
      return ret;
   }

   private byte[] safeRead(int count) throws IOException {
      byte ret[] = new byte[count];
      for (int i = 0; i < count; i++) {
         ret[i] = (byte) read();
      }
      return ret;
   }

   public Namespace readNamespace() throws IOException {
      int kind = read();
      int name_index = 0;
      for (int k = 0; k < Namespace.nameSpaceKinds.length; k++) {
         if (Namespace.nameSpaceKinds[k] == kind) {
            name_index = readU30();
            break;
         }
      }
      return new Namespace(kind, name_index);
   }

   public Multiname readMultiname() throws IOException {
      int kind = readU8();
      int namespace_index = -1;
      int name_index = -1;
      int namespace_set_index = -1;
      int qname_index = -1;
      List<Integer> params = new ArrayList<Integer>();

      if ((kind == 7) || (kind == 0xd)) { // CONSTANT_QName and CONSTANT_QNameA.
         namespace_index = readU30();
         name_index = readU30();
      } else if ((kind == 0xf) || (kind == 0x10)) { //CONSTANT_RTQName and CONSTANT_RTQNameA
         name_index = readU30();
      } else if ((kind == 0x11) || (kind == 0x12))//kind==0x11,0x12 nothing CONSTANT_RTQNameL and CONSTANT_RTQNameLA.
      {
      } else if ((kind == 9) || (kind == 0xe)) { // CONSTANT_Multiname and CONSTANT_MultinameA.
         name_index = readU30();
         namespace_set_index = readU30();
      } else if ((kind == 0x1B) || (kind == 0x1C)) { //CONSTANT_MultinameL and CONSTANT_MultinameLA
         namespace_set_index = readU30();
      } else if (kind == 0x1D) {
         //Constant_TypeName
         qname_index = readU30(); //Multiname index!!!
         int paramsLength = readU30();
         for (int i = 0; i < paramsLength; i++) {
            params.add(readU30()); //multiname indices!
         }
      } else {
         System.err.println("Unknown kind of Multiname:0x" + Integer.toHexString(kind));
         System.exit(1);
      }

      return new Multiname(kind, name_index, namespace_index, namespace_set_index, qname_index, params);
   }

   public MethodInfo readMethodInfo() throws IOException {
      int param_count = readU30();
      int ret_type = readU30();
      int param_types[] = new int[param_count];
      for (int i = 0; i < param_count; i++) {
         param_types[i] = readU30();
      }
      int name_index = readU30();
      int flags = read();

      //// 1=need_arguments, 2=need_activation, 4=need_rest 8=has_optional (16=ignore_rest, 32=explicit,) 64=setsdxns, 128=has_paramnames

      ValueKind optional[] = new ValueKind[0];
      if ((flags & 8) == 8) { //if has_optional
         int optional_count = readU30();
         optional = new ValueKind[optional_count];
         for (int i = 0; i < optional_count; i++) {
            optional[i] = new ValueKind(readU30(), read());
         }
      }

      int param_names[] = new int[param_count];
      if ((flags & 128) == 128) { //if has_paramnames
         for (int i = 0; i < param_count; i++) {
            param_names[i] = readU30();
         }
      }
      return new MethodInfo(param_types, ret_type, name_index, flags, optional, param_names);
   }

   public Trait readTrait() throws IOException {
      long pos = getPosition();
      startBuffer();
      int name_index = readU30();
      int kind = read();
      int kindType = 0xf & kind;
      int kindFlags = kind >> 4;
      Trait trait;

      switch (kindType) {
         case 0: //slot
         case 6: //const
            TraitSlotConst t1 = new TraitSlotConst();
            t1.slot_id = readU30();
            t1.type_index = readU30();
            t1.value_index = readU30();
            if (t1.value_index != 0) {
               t1.value_kind = read();
            }
            trait = t1;
            break;
         case 1: //method
         case 2: //getter
         case 3: //setter
            TraitMethodGetterSetter t2 = new TraitMethodGetterSetter();
            t2.disp_id = readU30();
            t2.method_info = readU30();
            trait = t2;
            break;
         case 4: //class
            TraitClass t3 = new TraitClass();
            t3.slot_id = readU30();
            t3.class_info = readU30();
            trait = t3;
            break;
         case 5: //function
            TraitFunction t4 = new TraitFunction();
            t4.slot_index = readU30();
            t4.method_info = readU30();
            trait = t4;
            break;
         default:
            throw new IOException("Unknown trait kind:" + kind);
      }
      trait.fileOffset = pos;
      trait.kindType = kindType;
      trait.kindFlags = kindFlags;
      trait.name_index = name_index;
      if ((kindFlags & ATTR_METADATA) != 0) {
         int metadata_count = readU30();
         trait.metadata = new int[metadata_count];
         for (int i = 0; i < metadata_count; i++) {
            trait.metadata[i] = readU30();
         }
      }
      trait.bytes = stopBuffer();
      return trait;
   }

   public Traits readTraits() throws IOException {
      int count = readU30();
      Traits traits = new Traits();
      traits.traits = new Trait[count];
      for (int i = 0; i < count; i++) {
         traits.traits[i] = readTrait();
      }
      return traits;
   }

   public byte[] readBytes(int count) throws IOException {
      byte ret[] = new byte[count];
      for (int i = 0; i < count; i++) {
         ret[i] = (byte) read();
      }
      return ret;
   }

   public Decimal readDecimal() throws IOException {
      byte data[] = readBytes(16);
      return new Decimal(data);
   }

   public InstanceInfo readInstanceInfo() throws IOException {
      InstanceInfo ret = new InstanceInfo();
      ret.name_index = readU30();
      ret.super_index = readU30();
      ret.flags = read();
      if ((ret.flags & CLASS_PROTECTED_NS) != 0) {
         ret.protectedNS = readU30();
      }
      int interfaces_count = readU30();
      ret.interfaces = new int[interfaces_count];
      for (int i = 0; i < interfaces_count; i++) {
         ret.interfaces[i] = readU30();
      }
      ret.iinit_index = readU30();
      ret.instance_traits = readTraits();
      return ret;
   }

   public String readString() throws IOException {
      int length = readU30();
      return new String(safeRead(length), "utf8");
   }


   /*public void markStart(){
    bytesRead=0;
    }*/
   public long getPosition() {
      return bytesRead;
   }
}
