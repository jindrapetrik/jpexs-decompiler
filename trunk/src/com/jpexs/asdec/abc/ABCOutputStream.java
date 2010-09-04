/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc;

import com.jpexs.asdec.abc.types.InstanceInfo;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.Namespace;
import com.jpexs.asdec.abc.types.traits.*;

import java.io.IOException;
import java.io.OutputStream;

public class ABCOutputStream extends OutputStream {

    private OutputStream os;

    public ABCOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

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

    public void writeU32(long value) throws IOException {
        boolean loop = true;
        value = value & 0xFFFFFFFF;
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
    }

    public void writeS24(long value) throws IOException {
        int ret = (int) (value & 0xff);
        write(ret);
        value = value >> 8;
        ret = (int) (value & 0xff);
        write(ret);
        value = value >> 8;
        ret = (int) (value & 0xff);
        write(ret);
        value = value >> 8;
    }

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
                ret = ret & 0xf;
            }
            write(ret);
            if (bitcount == 35) {
                break;
            }
            value = value >> 7;
        } while (loop);
    }

    public void writeLong(long value) throws IOException {
        byte writeBuffer[] = new byte[8];
        writeBuffer[7] = (byte) (value >>> 56);
        writeBuffer[6] = (byte) (value >>> 48);
        writeBuffer[5] = (byte) (value >>> 40);
        writeBuffer[4] = (byte) (value >>> 32);
        writeBuffer[3] = (byte) (value >>> 24);
        writeBuffer[2] = (byte) (value >>> 16);
        writeBuffer[1] = (byte) (value >>> 8);
        writeBuffer[0] = (byte) (value >>> 0);
        write(writeBuffer);
    }

    public void writeDouble(double value) throws IOException {
        writeLong(Double.doubleToLongBits(value));
    }

    public void writeU8(int value) throws IOException {
        write(value);
    }

    public void writeU16(int value) throws IOException {
        write(value & 0xff);
        write((value >> 8) & 0xff);
    }

    public void writeString(String s) throws IOException {
        byte sbytes[] = s.getBytes("utf8");
        writeU30(sbytes.length);
        write(sbytes);
    }

    public void writeNamespace(Namespace ns) throws IOException {
        write(ns.kind);
        for (int k = 0; k < Namespace.nameSpaceKinds.length; k++) {
            if (Namespace.nameSpaceKinds[k] == ns.kind) {
                writeU30(ns.name_index);
                break;
            }
        }
    }

    public void writeMultiname(Multiname m) throws IOException {
        write(m.kind);
        if ((m.kind == 7) || (m.kind == 0xd)) { // CONSTANT_QName and CONSTANT_QNameA.
            writeU30(m.namespace_index);
            writeU30(m.name_index);
        }
        if ((m.kind == 9) || (m.kind == 0xe)) { // CONSTANT_Multiname and CONSTANT_MultinameA.
            writeU30(m.name_index);
            writeU30(m.namespace_set_index);
        }
        if ((m.kind == 0xf) || (m.kind == 0x10)) { //CONSTANT_RTQName and CONSTANT_RTQNameA
            writeU30(m.name_index);
        }
        if ((m.kind == 0x1B) || (m.kind == 0x1C)) { //CONSTANT_MultinameL and CONSTANT_MultinameLA
            writeU30(m.namespace_set_index);
        }
        //kind==0x11,0x12 nothing CONSTANT_RTQNameL and CONSTANT_RTQNameLA.
    }

    public void writeMethodInfo(MethodInfo mi) throws IOException {
        writeU30(mi.param_types.length);
        writeU30(mi.ret_type);
        for (int i = 0; i < mi.param_types.length; i++) {
            writeU30(mi.param_types[i]);
        }
        writeU30(mi.name_index);
        write(mi.flags);
        if ((mi.flags & 8) == 8) {
            writeU30(mi.optional.length);
            for (int i = 0; i < mi.optional.length; i++) {
                writeU30(mi.optional[i].value_index);
                write(mi.optional[i].value_kind);
            }
        }

        if ((mi.flags & 128) == 128) { //if has_paramnames
            for (int i = 0; i < mi.paramNames.length; i++) {
                writeU30(mi.paramNames[i]);
            }
        }
    }

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
            writeU30(t4.slot_index);
            writeU30(t4.method_info);
        }
        if ((t.kindFlags & 4) == 4) {
            writeU30(t.metadata.length);
            for (int i = 0; i < t.metadata.length; i++) {
                writeU30(t.metadata[i]);
            }
        }
    }

    public void writeTraits(Traits t) throws IOException {
        writeU30(t.traits.length);
        for (int i = 0; i < t.traits.length; i++) {
            writeTrait(t.traits[i]);
        }
    }

    public void writeInstanceInfo(InstanceInfo ii) throws IOException {
        writeU30(ii.name_index);
        writeU30(ii.super_index);
        write(ii.flags);
        if ((ii.flags & 8) == 8) {
            writeU30(ii.protectedNS);
        }
        writeU30(ii.interfaces.length);
        for (int i = 0; i < ii.interfaces.length; i++) {
            writeU30(ii.interfaces[i]);
        }
        writeU30(ii.iinit_index);
        writeTraits(ii.instance_traits);
    }
}
