/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.ConvertException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.List;

public class ABCException implements Serializable, Cloneable {

    public int start;

    public int end;

    public int target;

    public int type_index;

    public int name_index;

    @Override
    public String toString() {
        return "Exception: startServer=" + Helper.formatAddress(start) + " end=" + Helper.formatAddress(end) + " target=" + target + " type_index=" + type_index + " name_index=" + name_index;
    }

    public String toString(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        return "Exception: startServer=" + Helper.formatAddress(start) + " end=" + Helper.formatAddress(end) + " target=" + target + " type=\"" + getTypeName(constants, fullyQualifiedNames) + "\" name=\"" + getVarName(constants, fullyQualifiedNames) + "\"";
    }

    public String toString(AVM2ConstantPool constants, AVM2Code code, List<DottedChain> fullyQualifiedNames) {
        try {
            return "Exception: startServer=" + code.adr2pos(start) + ":" + code.code.get(code.adr2pos(start)).toStringNoAddress(constants, fullyQualifiedNames) + " end=" + code.adr2pos(end) + ":" + code.code.get(code.adr2pos(end)).toStringNoAddress(constants, fullyQualifiedNames) + " target=" + code.adr2pos(target) + ":" + code.code.get(code.adr2pos(target)).toStringNoAddress(constants, fullyQualifiedNames) + " type=\"" + getTypeName(constants, fullyQualifiedNames) + "\" name=\"" + getVarName(constants, fullyQualifiedNames) + "\"";
        } catch (ConvertException ex) {
            return "";
        }
    }

    public boolean isFinally() {
        return (name_index == 0) && (type_index == 0);
    }

    public String getVarName(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        if (name_index == 0) {
            return "";
        }
        return constants.getMultiname(name_index).getName(constants, fullyQualifiedNames, false);
    }

    public String getTypeName(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        if (type_index == 0) {
            return "*";
        }
        return constants.getMultiname(type_index).getName(constants, fullyQualifiedNames, false);
    }

    @Override
    public ABCException clone() {
        try {
            ABCException ret = (ABCException) super.clone();
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}
