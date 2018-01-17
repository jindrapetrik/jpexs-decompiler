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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.helpers.Helper;

/**
 *
 * @author JPEXS
 */
public class ValueKind {

    public static final int CONSTANT_DecimalOrFloat = 0x02; //decimal or float depending on ABC version

    public static final int CONSTANT_Int = 0x03;// integer

    public static final int CONSTANT_UInt = 0x04;// uinteger

    public static final int CONSTANT_Double = 0x06;// double

    public static final int CONSTANT_Utf8 = 0x01;// string

    public static final int CONSTANT_True = 0x0B;// -

    public static final int CONSTANT_False = 0x0A;// -

    public static final int CONSTANT_Null = 0x0C;// -

    public static final int CONSTANT_Undefined = 0x00;// -

    public static final int CONSTANT_Namespace = 0x08;// namespace

    public static final int CONSTANT_PackageNamespace = 0x16;// namespace

    public static final int CONSTANT_PackageInternalNs = 0x17;// Namespace

    public static final int CONSTANT_ProtectedNamespace = 0x18;// Namespace

    public static final int CONSTANT_ExplicitNamespace = 0x19;// Namespace

    public static final int CONSTANT_StaticProtectedNs = 0x1A;// Namespace

    public static final int CONSTANT_PrivateNs = 0x05;// namespace

    public static final int CONSTANT_Float4 = 0x1E;// float4

    private static final int[] optionalKinds = new int[]{0x03, 0x04, 0x06, 0x02, 0x01, 0x0B, 0x0A, 0x0C, 0x00, 0x08, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x05};

    private static final String[] optionalKindNames = new String[]{"Int", "UInt", "Double", "Decimal", "Utf8", "True", "False", "Null", "Undefined", "Namespace", "PackageNamespace", "PackageInternalNs", "ProtectedNamespace", "ExplicitNamespace", "StaticProtectedNs", "PrivateNamespace"};

    public int value_index;

    public int value_kind;

    public ValueKind() {
    }

    public ValueKind(int value_index, int value_kind) {
        this.value_index = value_index;
        this.value_kind = value_kind;
    }

    public static int nsKindToValueKind(int nsKind) {
        switch (nsKind) {
            case Namespace.KIND_EXPLICIT:
                return CONSTANT_ExplicitNamespace;
            case Namespace.KIND_NAMESPACE:
                return CONSTANT_Namespace;
            case Namespace.KIND_PACKAGE:
                return CONSTANT_PackageNamespace;
            case Namespace.KIND_PACKAGE_INTERNAL:
                return CONSTANT_PackageInternalNs;
            case Namespace.KIND_PRIVATE:
                return CONSTANT_PrivateNs;
            case Namespace.KIND_PROTECTED:
                return CONSTANT_ProtectedNamespace;
            case Namespace.KIND_STATIC_PROTECTED:
                return CONSTANT_StaticProtectedNs;
        }
        return 0;
    }

    @Override
    public String toString() {
        String s = "";
        s += value_index + ":";
        boolean found = false;
        for (int i = 0; i < optionalKinds.length; i++) {
            if (optionalKinds[i] == value_kind) {
                s += optionalKindNames[i];
                found = true;
                break;
            }
        }
        if (!found) {
            s += "?";
        }
        return s;
    }

    public boolean isNamespace() {
        switch (value_kind) {
            case CONSTANT_Namespace:
            case CONSTANT_PackageInternalNs:
            case CONSTANT_ProtectedNamespace:
            case CONSTANT_ExplicitNamespace:
            case CONSTANT_StaticProtectedNs:
            case CONSTANT_PrivateNs:
                return true;
            default:
                return false;
        }
    }

    public String toString(AVM2ConstantPool constants) {
        String ret = "?";
        switch (value_kind) {
            case CONSTANT_Int:
                ret = "" + constants.getInt(value_index);
                break;
            case CONSTANT_UInt:
                ret = "" + constants.getUInt(value_index);
                break;
            case CONSTANT_Double:
                ret = "" + constants.getDouble(value_index);
                break;
            case CONSTANT_DecimalOrFloat:
                ret = "" + constants.getDecimal(value_index);
                break;
            case CONSTANT_Utf8:
                ret = "\"" + Helper.escapeActionScriptString(constants.getString(value_index)) + "\"";
                break;
            case CONSTANT_True:
                ret = "true";
                break;
            case CONSTANT_False:
                ret = "false";
                break;
            case CONSTANT_Null:
                ret = "null";
                break;
            case CONSTANT_Undefined:
                ret = "undefined";
                break;
            case CONSTANT_Namespace:
            case CONSTANT_PackageInternalNs:
            case CONSTANT_ProtectedNamespace:
            case CONSTANT_ExplicitNamespace:
            case CONSTANT_StaticProtectedNs:
            case CONSTANT_PrivateNs:
                ret = "\"" + constants.getNamespace(value_index).getName(constants).toRawString() + "\"";  //assume not null name
                break;
        }
        return ret;
    }

    public String toASMString(AVM2ConstantPool constants) {
        String ret = "?";
        switch (value_kind) {
            case CONSTANT_Int:
                ret = "Integer(" + constants.getInt(value_index) + ")";
                break;
            case CONSTANT_UInt:
                ret = "UInteger(" + constants.getUInt(value_index) + ")";
                break;
            case CONSTANT_Double:
                ret = "Double(" + constants.getDouble(value_index) + ")";
                break;
            case CONSTANT_DecimalOrFloat:
                ret = "Decimal(" + constants.getDecimal(value_index) + ")";
                break;
            case CONSTANT_Utf8:
                ret = "Utf8(\"" + Helper.escapeActionScriptString(constants.getString(value_index)) + "\")";
                break;
            case CONSTANT_True:
                ret = "True";
                break;
            case CONSTANT_False:
                ret = "False";
                break;
            case CONSTANT_Null:
                ret = "Null";
                break;
            case CONSTANT_Undefined:
                ret = "Undefined";
                break;
            case CONSTANT_Namespace:
            case CONSTANT_PackageInternalNs:
            case CONSTANT_ProtectedNamespace:
            case CONSTANT_ExplicitNamespace:
            case CONSTANT_StaticProtectedNs:
            case CONSTANT_PrivateNs:
                ret = constants.getNamespace(value_index).getKindStr() + "(\"" + constants.getNamespace(value_index).getName(constants).toRawString() + "\")"; //assume not null name
                break;
        }
        return ret;
    }
}
