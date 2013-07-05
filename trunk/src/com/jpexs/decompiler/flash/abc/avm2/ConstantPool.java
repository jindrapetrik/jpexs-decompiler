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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.types.Decimal;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConstantPool {

    public long constant_int[];
    public long constant_uint[];
    public double constant_double[];
    /* Only for some minor versions */
    public Decimal constant_decimal[];
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
            if (Double.compare(constant_double[i], value) == 0) {
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

    public int forceGetStringId(String val) {
        int id = getStringId(val);
        if (id == 0) {
            id = addString(val);
        }
        return id;
    }

    public int forceGetIntId(long val) {
        int id = getIntId(val);
        if (id == 0) {
            id = addInt(val);
        }
        return id;
    }

    public int forceGetUIntId(long val) {
        int id = getUIntId(val);
        if (id == 0) {
            id = addUInt(val);
        }
        return id;
    }

    public int forceGetDoubleId(double val) {
        int id = getDoubleId(val);
        if (id == 0) {
            id = addDouble(val);
        }
        return id;
    }

    public void dump(OutputStream os) {
        PrintStream output;
        try {
            output = new PrintStream(os, false, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConstantPool.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
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
            output.println("Multiname[" + i + "]=" + constant_multiname[i].toString(this, new ArrayList<String>()));
        }
    }
}
