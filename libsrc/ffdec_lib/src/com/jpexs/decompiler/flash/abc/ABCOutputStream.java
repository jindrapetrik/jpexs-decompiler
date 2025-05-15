/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.io.OutputStream;
import macromedia.asc.util.Decimal128;

/**
 * ABC output stream.
 *
 * @author JPEXS
 */
public class ABCOutputStream extends OutputStream {

    /**
     * Output stream
     */
    private final OutputStream os;

    /**
     * Current position
     */
    private long position = 0L;

    /**
     * Constructs ABC output stream.
     *
     * @param os Output stream
     */
    public ABCOutputStream(OutputStream os) {
        this.os = os;
    }

    /**
     * Returns current position.
     *
     * @return Current position
     */
    public long getPosition() {
        return position;
    }

    /**
     * Writes a byte to the output stream.
     *
     * @param b The byte to write.
     * @throws IOException On I/O error
     */
    @Override
    public void write(int b) throws IOException {
        os.write(b);
        position++;
    }

    /**
     * Writes a byte array to the output stream.
     *
     * @param data The data to write.
     * @throws IOException On I/O error
     */
    @Override
    public void write(byte[] data) throws IOException {
        os.write(data);
        position += data.length;
    }

    /**
     * Writes a byte array to the output stream.
     *
     * @param b The data to write.
     * @param off The start offset in the data.
     * @param len The number of bytes to write.
     * @throws IOException On I/O error
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
        position += len;
    }

    /**
     * Writes U30 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeU30(long value) throws IOException {
        writeS32(value);
        /*boolean loop = true;
         boolean underZero=value<0;

         if(underZero){
         value = value & 0xFFFFFFFF;
         }else{
         value = value & 0x7FFFFFFF;
         }
         do {
         int ret = (int) (value & 0x7F);
         if (value < 0x80) {
         loop = false;
         }
         if (value > 0x7F) {
         ret += 0x80;
         }
         write(ret);
         value = value >> 7;
         } while (loop);
         */
    }

    /**
     * Writes U32 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeU32(long value) throws IOException {
        boolean loop = true;
        value &= 0xFFFFFFFFL;
        do {
            int ret = (int) (value & 0x7F);
            if (value < 0x80) {
                loop = false;
            }
            if (value > 0x7F) {
                ret += 0x80;
            }
            write(ret);
            value >>= 7;
        } while (loop);
    }

    /**
     * Writes S24 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeS24(long value) throws IOException {
        int ret = (int) (value & 0xff);
        write(ret);
        value >>= 8;
        ret = (int) (value & 0xff);
        write(ret);
        value >>= 8;
        ret = (int) (value & 0xff);
        write(ret);
    }

    /**
     * Writes S32 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeS32(long value) throws IOException {
        boolean belowZero = value < 0;
        /*if (belowZero) {
         value = -value;
         }*/
        int bitcount = 0;
        boolean loop = true;
        //value = value & 0xFFFFFFFF;
        do {
            bitcount += 7;
            int ret = (int) (value & 0x7F);
            if (value < 0x80) {
                if (belowZero) { //&& bitcount < 35
                    ret += 0x80;
                } else {
                    loop = false;
                }
            } else {
                ret += 0x80;
            }

            if (bitcount == 35) {
                ret &= 0xf;
            }
            write(ret);
            if (bitcount == 35) {
                break;
            }
            value >>= 7;
        } while (loop);
    }

    /**
     * Writes long to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeLong(long value) throws IOException {
        byte[] writeBuffer = new byte[8];
        writeBuffer[7] = (byte) (value >>> 56);
        writeBuffer[6] = (byte) (value >>> 48);
        writeBuffer[5] = (byte) (value >>> 40);
        writeBuffer[4] = (byte) (value >>> 32);
        writeBuffer[3] = (byte) (value >>> 24);
        writeBuffer[2] = (byte) (value >>> 16);
        writeBuffer[1] = (byte) (value >>> 8);
        writeBuffer[0] = (byte) (value);
        write(writeBuffer);
    }

    /**
     * Writes double to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    /**
     * Writes float to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeFloat(float value) throws IOException {
        int val = Float.floatToIntBits(value);
        write(val & 0xFF);
        write((val >> 8) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 24) & 0xFF);
    }

    /**
     * Writes float4 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeFloat4(Float4 value) throws IOException {
        writeFloat(value.values[0]);
        writeFloat(value.values[1]);
        writeFloat(value.values[2]);
        writeFloat(value.values[3]);
    }

    /**
     * Writes U8 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeU8(int value) throws IOException {
        write(value);
    }

    /**
     * Writes U16 to the output stream.
     *
     * @param value Value to write
     * @throws IOException On I/O error
     */
    public void writeU16(int value) throws IOException {
        write(value & 0xff);
        write((value >> 8) & 0xff);
    }

    /**
     * Writes String to the output stream.
     *
     * @param s String to write
     * @throws IOException On I/O error
     */
    public void writeString(String s) throws IOException {
        byte[] sbytes = Utf8Helper.getBytes(s);
        writeU30(sbytes.length);
        write(sbytes);
    }

    /**
     * Writes Namespace to the output stream.
     *
     * @param ns Namespace to write
     * @throws IOException On I/O error
     */
    public void writeNamespace(Namespace ns) throws IOException {
        write(ns.kind);
        boolean found = false;
        for (int k = 0; k < Namespace.nameSpaceKinds.length; k++) {
            if (Namespace.nameSpaceKinds[k] == ns.kind) {
                writeU30(ns.name_index);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Invalid ns kind:" + ns.kind);
        }
    }

    /**
     * Writes Multiname to the output stream.
     *
     * @param m Multiname to write
     * @throws IOException On I/O error
     */
    public void writeMultiname(Multiname m) throws IOException {
        writeU8(m.kind);
        if ((m.kind == Multiname.QNAME) || (m.kind == Multiname.QNAMEA)) {
            writeU30(m.namespace_index);
            writeU30(m.name_index);
        } else if ((m.kind == Multiname.MULTINAME) || (m.kind == Multiname.MULTINAMEA)) {
            writeU30(m.name_index);
            writeU30(m.namespace_set_index);
        } else if ((m.kind == Multiname.RTQNAME) || (m.kind == Multiname.RTQNAMEA)) {
            writeU30(m.name_index);
        } else if ((m.kind == Multiname.MULTINAMEL) || (m.kind == Multiname.MULTINAMELA)) {
            writeU30(m.namespace_set_index);
        } else if (m.kind == Multiname.TYPENAME) {
            writeU30(m.qname_index);
            writeU30(m.params.length);
            for (int i = 0; i < m.params.length; i++) {
                writeU30(m.params[i]);
            }
        }
        // kind==0x11,0x12 nothing CONSTANT_RTQNameL and CONSTANT_RTQNameLA.
    }

    /**
     * Writes MethodInfo to the output stream.
     *
     * @param mi MethodInfo to write
     * @throws IOException On I/O error
     */
    public void writeMethodInfo(MethodInfo mi) throws IOException {
        writeU30(mi.param_types.length);
        writeU30(mi.ret_type);
        for (int i = 0; i < mi.param_types.length; i++) {
            writeU30(mi.param_types[i]);
        }
        writeU30(mi.name_index);
        write(mi.flags);
        if ((mi.flags & MethodInfo.FLAG_HAS_OPTIONAL) == MethodInfo.FLAG_HAS_OPTIONAL) {
            writeU30(mi.optional.length);
            for (int i = 0; i < mi.optional.length; i++) {
                writeU30(mi.optional[i].value_index);
                write(mi.optional[i].value_kind);
            }
        }

        if ((mi.flags & MethodInfo.FLAG_HAS_PARAMNAMES) == MethodInfo.FLAG_HAS_PARAMNAMES) { // if has_paramnames
            for (int i = 0; i < mi.paramNames.length; i++) {
                writeU30(mi.paramNames[i]);
            }
        }
    }

    /**
     * Writes Trait to the output stream.
     *
     * @param t Trait to write
     * @throws IOException On I/O error
     */
    public void writeTrait(Trait t) throws IOException {
        writeU30(t.name_index);
        write((t.kindFlags << 4) + t.kindType);
        if (t instanceof TraitSlotConst) {
            TraitSlotConst t1 = (TraitSlotConst) t;
            writeU30(t1.slot_id);
            writeU30(t1.type_index);
            writeU30(t1.value_index);
            if (t1.value_index != 0) {
                write(t1.value_kind);
            }
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter t2 = (TraitMethodGetterSetter) t;
            writeU30(t2.disp_id);
            writeU30(t2.method_info);
        }
        if (t instanceof TraitClass) {
            TraitClass t3 = (TraitClass) t;
            writeU30(t3.slot_id);
            writeU30(t3.class_info);
        }
        if (t instanceof TraitFunction) {
            TraitFunction t4 = (TraitFunction) t;
            writeU30(t4.slot_id);
            writeU30(t4.method_info);
        }
        if ((t.kindFlags & Trait.ATTR_Metadata) == Trait.ATTR_Metadata) {
            writeU30(t.metadata.length);
            for (int i = 0; i < t.metadata.length; i++) {
                writeU30(t.metadata[i]);
            }
        }
    }

    /**
     * Writes Traits to the output stream.
     *
     * @param t Traits to write
     * @throws IOException On I/O error
     */
    public void writeTraits(Traits t) throws IOException {
        writeU30(t.traits.size());
        for (int i = 0; i < t.traits.size(); i++) {
            writeTrait(t.traits.get(i));
        }
    }

    /**
     * Writes InstanceInfo to the output stream.
     *
     * @param ii InstanceInfo to write
     * @throws IOException On I/O error
     */
    public void writeInstanceInfo(InstanceInfo ii) throws IOException {
        writeU30(ii.name_index);
        writeU30(ii.super_index);
        write(ii.flags);
        if ((ii.flags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
            writeU30(ii.protectedNS);
        }
        writeU30(ii.interfaces.length);
        for (int i = 0; i < ii.interfaces.length; i++) {
            writeU30(ii.interfaces[i]);
        }
        writeU30(ii.iinit_index);
        writeTraits(ii.instance_traits);
    }

    /**
     * Writes Decimal to the output stream.
     *
     * @param value Decimal to write
     * @throws IOException On I/O error
     */
    public void writeDecimal(Decimal128 value) throws IOException {
        write(value.toIEEE());
    }

    /**
     * Gets U30 byte length.
     *
     * @param value Value
     * @return Length
     */
    public static int getU30ByteLength(long value) {
        boolean belowZero = value < 0;
        int bitcount = 0;
        int result = 0;
        boolean loop = true;
        do {
            bitcount += 7;
            if (value < 0x80 && !belowZero) {
                loop = false;
            }

            result++;
            if (bitcount == 35) {
                break;
            }
            value >>= 7;
        } while (loop);
        return result;
    }
}
