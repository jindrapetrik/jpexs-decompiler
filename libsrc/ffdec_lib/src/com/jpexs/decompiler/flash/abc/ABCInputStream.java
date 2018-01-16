/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.types.Decimal;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecial;
import com.jpexs.decompiler.flash.dumpview.DumpInfoSpecialType;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class ABCInputStream implements AutoCloseable {

    private static final int CLASS_PROTECTED_NS = 8;

    private static final int ATTR_METADATA = 4;

    private final MemoryInputStream is;

    private ByteArrayOutputStream bufferOs = null;

    public static final boolean DEBUG_READ = false;

    public DumpInfo dumpInfo;

    private byte[] stringDataBuffer = new byte[256];

    public void startBuffer() {
        if (bufferOs == null) {
            bufferOs = new ByteArrayOutputStream();
        } else {
            bufferOs.reset();
        }
    }

    public byte[] stopBuffer() {
        if (bufferOs == null) {
            return SWFInputStream.BYTE_ARRAY_EMPTY;
        }
        byte[] ret = bufferOs.toByteArray();
        bufferOs.reset();
        return ret;
    }

    public ABCInputStream(MemoryInputStream is) {
        this.is = is;
    }

    /**
     * Sets position in bytes in the stream
     *
     * @param pos Number of bytes
     * @throws java.io.IOException
     */
    public void seek(long pos) throws IOException {
        is.seek(pos);
    }

    public DumpInfo newDumpLevel(String name, String type) {
        return newDumpLevel(name, type, DumpInfoSpecialType.NONE);
    }

    public DumpInfo newDumpLevel(String name, String type, DumpInfoSpecialType specialType) {
        if (dumpInfo != null) {
            long startByte = is.getPos();
            DumpInfo di = specialType == DumpInfoSpecialType.NONE
                    ? new DumpInfo(name, type, null, startByte, 0, 0, 0)
                    : new DumpInfoSpecial(name, type, null, startByte, 0, 0, 0, specialType);
            di.parent = dumpInfo;
            dumpInfo.getChildInfos().add(di);
            dumpInfo = di;
        }

        return dumpInfo;
    }

    public void endDumpLevel() {
        endDumpLevel(null);
    }

    public void endDumpLevel(Object value) {
        if (dumpInfo != null) {
            dumpInfo.lengthBytes = is.getPos() - dumpInfo.startByte;
            dumpInfo.previewValue = value;
            dumpInfo = dumpInfo.parent;
        }
    }

    public void endDumpLevelUntil(DumpInfo di) {
        if (di != null) {
            while (dumpInfo != null && dumpInfo != di) {
                endDumpLevel();
            }
        }
    }

    private int readInternal() throws IOException {
        int i = is.read();
        if (i == -1) {
            throw new EndOfStreamException();
        }
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

    public int read(String name) throws IOException {
        newDumpLevel(name, "byte");
        int ret = readInternal();
        endDumpLevel(ret);
        return ret;
    }

    private int read(byte[] b) throws IOException {
        int currBytesRead = is.read(b);
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

    public int readU8(String name) throws IOException {
        newDumpLevel(name, "U8");
        int ret = readInternal();
        endDumpLevel(ret);
        return ret;
    }

    private long readU32Internal() throws IOException {
        int i;
        long ret = 0;
        int bytePos = 0;
        int byteCount = 0;
        boolean nextByte;
        do {
            i = readInternal();
            nextByte = (i >> 7) == 1;
            i &= 0x7f;
            ret += (((long) i) << bytePos);
            byteCount++;
            bytePos += 7;
        } while (nextByte && byteCount < 5);
        return ret;
    }

    public long readU32(String name) throws IOException {
        newDumpLevel(name, "U32");
        long ret = readU32Internal();
        endDumpLevel(ret);
        return ret;
    }

    private int readU30Internal() throws IOException {
        long u32 = readU32Internal();
        //no bits above bit 30
        return (int) (u32 & 0x3FFFFFFF);
    }

    public int readU30(String name) throws IOException {
        newDumpLevel(name, "U30");
        int ret = readU30Internal();
        endDumpLevel(ret);
        return ret;
    }

    public int readS24(String name) throws IOException {
        newDumpLevel(name, "S24");
        int ret = (readInternal()) + (readInternal() << 8) + (readInternal() << 16);

        if ((ret >> 23) == 1) {
            ret |= 0xff000000;
        }

        endDumpLevel(ret);
        return ret;
    }

    public int readU16(String name) throws IOException {
        newDumpLevel(name, "U16");
        int ret = (readInternal()) + (readInternal() << 8);
        endDumpLevel(ret);
        return ret;
    }

    public long readS32(String name) throws IOException {
        int i;
        long ret = 0;
        int bytePos = 0;
        int byteCount = 0;
        boolean nextByte;
        newDumpLevel(name, "S32");
        do {
            i = readInternal();
            nextByte = (i >> 7) == 1;
            i &= 0x7f;
            ret += (i << bytePos);
            byteCount++;
            bytePos += 7;
            if (bytePos == 35) {
                if ((ret >> 31) == 1) {
                    ret = -(ret & 0x7fffffff);
                }
                break;
            }
        } while (nextByte && byteCount < 5);
        endDumpLevel(ret);
        return ret;
    }

    public int available() throws IOException {
        return is.available();
    }

    private long readLong() throws IOException {
        safeRead(8, stringDataBuffer);
        byte[] readBuffer = stringDataBuffer;
        return (((long) readBuffer[7] << 56)
                + ((long) (readBuffer[6] & 255) << 48)
                + ((long) (readBuffer[5] & 255) << 40)
                + ((long) (readBuffer[4] & 255) << 32)
                + ((long) (readBuffer[3] & 255) << 24)
                + ((readBuffer[2] & 255) << 16)
                + ((readBuffer[1] & 255) << 8)
                + ((readBuffer[0] & 255)));
    }

    public double readDouble(String name) throws IOException {
        newDumpLevel(name, "Double");
        long el = readLong();
        double ret = Double.longBitsToDouble(el);
        endDumpLevel(ret);
        return ret;
    }

    private void safeRead(int count, byte[] data) throws IOException {
        for (int i = 0; i < count; i++) {
            data[i] = (byte) readInternal();
        }
    }

    public Namespace readNamespace(String name) throws IOException {
        newDumpLevel(name, "Namespace");
        int kind = read("kind");
        int name_index = 0;
        for (int k = 0; k < Namespace.nameSpaceKinds.length; k++) {
            if (Namespace.nameSpaceKinds[k] == kind) {
                name_index = readU30("name_index");
                break;
            }
        }
        endDumpLevel();
        return new Namespace(kind, name_index);
    }

    public Multiname readMultiname(String name) throws IOException {
        int kind = readU8("kind");
        Multiname result = null;

        newDumpLevel(name, "Multiname");
        if ((kind == Multiname.QNAME) || (kind == Multiname.QNAMEA)) {
            int namespace_index = readU30("namespace_index");
            int name_index = readU30("name_index");
            result = Multiname.createQName(kind == Multiname.QNAMEA, name_index, namespace_index);
        } else if ((kind == Multiname.RTQNAME) || (kind == Multiname.RTQNAMEA)) {
            int name_index = readU30("name_index");
            result = Multiname.createRTQName(kind == Multiname.RTQNAMEA, name_index);
        } else if ((kind == Multiname.RTQNAMEL) || (kind == Multiname.RTQNAMELA)) {
            result = Multiname.createRTQNameL(kind == Multiname.RTQNAMELA);
        } else if ((kind == Multiname.MULTINAME) || (kind == Multiname.MULTINAMEA)) {
            int name_index = readU30("name_index");
            int namespace_set_index = readU30("namespace_set_index");
            result = Multiname.createMultiname(kind == Multiname.MULTINAMEA, name_index, namespace_set_index);
        } else if ((kind == Multiname.MULTINAMEL) || (kind == Multiname.MULTINAMELA)) {
            int namespace_set_index = readU30("namespace_set_index");
            result = Multiname.createMultinameL(kind == Multiname.MULTINAMELA, namespace_set_index);
        } else if (kind == Multiname.TYPENAME) {
            int qname_index = readU30("qname_index"); // Multiname index!!!
            int paramsLength = readU30("paramsLength");
            int[] params = new int[paramsLength];
            for (int i = 0; i < paramsLength; i++) {
                params[i] = readU30("param"); // multiname indices!
            }
            result = Multiname.createTypeName(qname_index, params);
        } else {
            throw new IOException("Unknown kind of Multiname:0x" + Integer.toHexString(kind));
        }

        endDumpLevel();
        return result;
    }

    public MethodInfo readMethodInfo(String name) throws IOException {
        newDumpLevel(name, "method_info");
        int param_count = readU30("param_count");
        int ret_type = readU30("ret_type");
        int[] param_types = new int[param_count];
        for (int i = 0; i < param_count; i++) {
            param_types[i] = readU30("param_type");
        }
        int name_index = readU30("name_index");
        int flags = read("flags");

        // 1=need_arguments, 2=need_activation, 4=need_rest 8=has_optional (16=ignore_rest, 32=explicit,) 64=setsdxns, 128=has_paramnames
        ValueKind[] optional = new ValueKind[0];
        if ((flags & 8) == 8) { // if has_optional
            int optional_count = readU30("optional_count");
            optional = new ValueKind[optional_count];
            for (int i = 0; i < optional_count; i++) {
                optional[i] = new ValueKind(readU30("value_index"), read("value_kind"));
            }
        }

        int[] param_names = new int[param_count];
        if ((flags & 128) == 128) { // if has_paramnames
            for (int i = 0; i < param_count; i++) {
                param_names[i] = readU30("param_name");
            }
        }

        endDumpLevel();
        return new MethodInfo(param_types, ret_type, name_index, flags, optional, param_names);
    }

    public Trait readTrait(String name) throws IOException {
        newDumpLevel(name, "Trait");
        long pos = getPosition();
        startBuffer();
        int name_index = readU30("name_index");
        int kind = read("kind");
        int kindType = 0xf & kind;
        int kindFlags = kind >> 4;
        Trait trait;

        switch (kindType) {
            case 0: // slot
            case 6: // const
                TraitSlotConst t1 = new TraitSlotConst();
                t1.slot_id = readU30("slot_id");
                t1.type_index = readU30("type_index");
                t1.value_index = readU30("value_index");
                if (t1.value_index != 0) {
                    t1.value_kind = read("value_kind");
                }
                trait = t1;
                break;
            case 1: // method
            case 2: // getter
            case 3: // setter
                TraitMethodGetterSetter t2 = new TraitMethodGetterSetter();
                t2.disp_id = readU30("disp_id");
                t2.method_info = readU30("method_info");
                trait = t2;
                break;
            case 4: // class
                TraitClass t3 = new TraitClass();
                t3.slot_id = readU30("slot_id");
                t3.class_info = readU30("class_info");
                trait = t3;
                break;
            case 5: // function
                TraitFunction t4 = new TraitFunction();
                t4.slot_id = readU30("slot_id");
                t4.method_info = readU30("method_info");
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
            int metadata_count = readU30("metadata_count");
            trait.metadata = new int[metadata_count];
            for (int i = 0; i < metadata_count; i++) {
                trait.metadata[i] = readU30("metadata");
            }
        }
        trait.bytes = stopBuffer();
        endDumpLevel();
        return trait;
    }

    public Traits readTraits(String name) throws IOException {
        newDumpLevel(name, "Traits");
        int count = readU30("count");
        Traits traits = new Traits(count);
        for (int i = 0; i < count; i++) {
            traits.traits.add(readTrait("trait"));
        }
        endDumpLevel();
        return traits;
    }

    private byte[] readBytesInternal(int count) throws IOException {
        byte[] ret = new byte[count];
        for (int i = 0; i < count; i++) {
            ret[i] = (byte) readInternal();
        }
        return ret;
    }

    public byte[] readBytes(int count, String name, DumpInfoSpecialType specialType) throws IOException {
        newDumpLevel(name, "Bytes", specialType);
        byte[] ret = readBytesInternal(count);
        endDumpLevel();
        return ret;
    }

    public Decimal readDecimal(String name) throws IOException {
        newDumpLevel(name, "Decimal");
        byte[] data = readBytesInternal(16);
        endDumpLevel();
        return new Decimal(data);
    }

    public Float readFloat(String name) throws IOException {
        newDumpLevel(name, "Float");
        int intBits = (readInternal()) + (readInternal() << 8);
        float ret = Float.intBitsToFloat(intBits);
        endDumpLevel(ret);
        return ret;
    }

    public Float4 readFloat4(String name) throws IOException {
        newDumpLevel(name, "Float4");
        float f1 = readFloat("value1");
        float f2 = readFloat("value2");
        float f3 = readFloat("value3");
        float f4 = readFloat("value4");
        Float4 ret = new Float4(f1, f2, f3, f4);
        endDumpLevel(ret);
        return ret;
    }

    public InstanceInfo readInstanceInfo(String name) throws IOException {
        newDumpLevel(name, "instance_info");
        InstanceInfo ret = new InstanceInfo(null); // do not create Traits in constructor
        ret.name_index = readU30("name_index");
        ret.super_index = readU30("super_index");
        ret.flags = readInternal();
        if ((ret.flags & CLASS_PROTECTED_NS) != 0) {
            ret.protectedNS = readU30("protectedNS");
        }
        int interfaces_count = readU30("interfaces_count");
        ret.interfaces = new int[interfaces_count];
        for (int i = 0; i < interfaces_count; i++) {
            ret.interfaces[i] = readU30("interface");
        }
        ret.iinit_index = readU30("iinit_index");
        ret.instance_traits = readTraits("instance_traits");
        endDumpLevel();
        return ret;
    }

    public String readString(String name) throws IOException {
        newDumpLevel(name, "String");
        int length = readU30Internal();

        // avoid creating new byte array every time
        if (stringDataBuffer.length < length) {
            int newLength = stringDataBuffer.length * 2;
            while (newLength < length) {
                newLength *= 2;
            }

            stringDataBuffer = new byte[newLength];
        }

        safeRead(length, stringDataBuffer);
        String r = new String(stringDataBuffer, 0, length, Utf8Helper.charset);
        endDumpLevel(r);
        return r;
    }


    /*public void markStart(){
     bytesRead=0;
     }*/
    public long getPosition() {
        return is.getPos();
    }

    @Override
    public void close() {
    }
}
