/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2;

import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.Namespace;
import com.jpexs.asdec.abc.types.NamespaceSet;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;


public class ConstantPool {
    public long constant_int[];
    public long constant_uint[];
    public double constant_double[];
    public String constant_string[];
    public Namespace constant_namespace[];
    public NamespaceSet constant_namespace_set[];
    public Multiname constant_multiname[];

    public int addInt(long value) {
        constant_int = Arrays.copyOf(constant_int, constant_int.length + 1);
        constant_int[constant_int.length - 1] = value;
        return constant_int.length - 1;
    }

    public int addUInt(long value) {
        constant_uint = Arrays.copyOf(constant_uint, constant_uint.length + 1);
        constant_uint[constant_uint.length - 1] = value;
        return constant_uint.length - 1;
    }

    public int addDouble(double value) {
        constant_double = Arrays.copyOf(constant_double, constant_double.length + 1);
        constant_double[constant_double.length - 1] = value;
        return constant_double.length - 1;
    }

    public int addString(String value) {
        constant_string = Arrays.copyOf(constant_string, constant_string.length + 1);
        constant_string[constant_string.length - 1] = value;
        return constant_string.length - 1;
    }

    public int getIntId(long value) {
        for (int i = 1; i < constant_int.length; i++) {
            if (constant_int[i] == value) {
                return i;
            }
        }
        return 0;
    }

    public int getUIntId(long value) {
        for (int i = 1; i < constant_uint.length; i++) {
            if (constant_uint[i] == value) {
                return i;
            }
        }
        return 0;
    }

    public int getDoubleId(double value) {
        for (int i = 1; i < constant_double.length; i++) {
            if (constant_double[i] == value) {
                return i;
            }
        }
        return 0;
    }

    public int getStringId(String s) {
        for (int i = 1; i < constant_string.length; i++) {
            if (constant_string[i].equals(s)) {
                return i;
            }
        }
        return 0;
    }

    public int getMultiNameId(String s) {
        for (int i = 1; i < constant_multiname.length; i++) {
            if (constant_multiname[i].getName(this).equals(s)) {
                return i;
            }
        }
        return 0;
    }

    public void dump(OutputStream os) {
        PrintStream output = new PrintStream(os);
        String s = "";
        for (int i = 1; i < constant_int.length; i++) {
            output.println("INT[" + i + "]=" + constant_int[i]);
        }
        for (int i = 1; i < constant_uint.length; i++) {
            output.println("UINT[" + i + "]=" + constant_uint[i]);
        }
        for (int i = 1; i < constant_double.length; i++) {
            output.println("Double[" + i + "]=" + constant_double[i]);
        }
        for (int i = 1; i < constant_string.length; i++) {
            output.println("String[" + i + "]=" + constant_string[i]);
        }
        for (int i = 1; i < constant_namespace.length; i++) {
            output.println("Namespace[" + i + "]=" + constant_namespace[i].toString(this));
        }
        for (int i = 1; i < constant_namespace_set.length; i++) {
            output.println("NamespaceSet[" + i + "]=" + constant_namespace_set[i].toString(this));
        }

        for (int i = 1; i < constant_multiname.length; i++) {
            output.println("Multiname[" + i + "]=" + constant_multiname[i].toString(this));
        }
    }


}
