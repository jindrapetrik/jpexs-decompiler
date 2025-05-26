/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

/**
 * Exception.
 *
 * @author JPEXS
 */
public class ABCException implements Serializable, Cloneable {

    /**
     * Start offset
     */
    public int start;

    /**
     * End offset
     */
    public int end;

    /**
     * Target offset
     */
    public int target;

    /**
     * Type index - index to multiname constant pool
     */
    public int type_index;

    /**
     * Name index - index to multiname constant pool
     */
    public int name_index;

    /**
     * Default exception name
     */
    public static final String DEFAULT_EXCEPTION_NAME = "_loc_e_";

    /**
     * Constructs ABCException
     */
    public ABCException() {
    }

    /**
     * To string.
     * @return String
     */
    @Override
    public String toString() {
        return "Exception: startServer=" + Helper.formatAddress(start) + " end=" + Helper.formatAddress(end) + " target=" + target + " type_index=" + type_index + " name_index=" + name_index;
    }

    /**
     * To string.
     * @param constants AVM2 constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @return String
     */
    public String toString(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        return "Exception: startServer=" + Helper.formatAddress(start) + " end=" + Helper.formatAddress(end) + " target=" + target + " type=\"" + getTypeName(constants, fullyQualifiedNames) + "\" name=\"" + getVarName(constants, fullyQualifiedNames) + "\"";
    }

    /**
     * To string.
     * @param constants AVM2 constant pool
     * @param code AVM2 code
     * @param fullyQualifiedNames Fully qualified names
     * @return String
     */
    public String toString(AVM2ConstantPool constants, AVM2Code code, List<DottedChain> fullyQualifiedNames) {
        try {
            return "Exception: startServer=" + code.adr2pos(start) + ":" + code.code.get(code.adr2pos(start)).toStringNoAddress(constants, fullyQualifiedNames) + " end=" + code.adr2pos(end) + ":" + code.code.get(code.adr2pos(end)).toStringNoAddress(constants, fullyQualifiedNames) + " target=" + code.adr2pos(target) + ":" + code.code.get(code.adr2pos(target)).toStringNoAddress(constants, fullyQualifiedNames) + " type=\"" + getTypeName(constants, fullyQualifiedNames) + "\" name=\"" + getVarName(constants, fullyQualifiedNames) + "\"";
        } catch (ConvertException ex) {
            return "";
        }
    }

    /**
     * Checks if exception is finally.
     * @return True if exception is finally
     */
    public boolean isFinally() {
        return (name_index == 0) && (type_index == 0);
    }

    /**
     * Gets variable name.
     * @param constants AVM2 constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @return Variable name
     */
    public String getVarName(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        if (name_index == 0) {
            return DEFAULT_EXCEPTION_NAME;
        }
        return constants.getMultiname(name_index).getName(constants, fullyQualifiedNames, false, true);
    }

    /**
     * Gets type name.
     * @param constants AVM2 constant pool
     * @param fullyQualifiedNames Fully qualified names
     * @return Type name
     */
    public String getTypeName(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        if (type_index == 0) {
            return "*";
        }
        return constants.getMultiname(type_index).getName(constants, fullyQualifiedNames, false, true);
    }

    /**
     * Clones exception.
     * @return Cloned exception
     */
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
