/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.helpers.Helper;


public class MethodInfo {

    public int param_types[];
    public int ret_type;
    public int name_index; //0=no name
    // 1=need_arguments, 2=need_activation, 4=need_rest 8=has_optional 16=ignore_rest, 32=explicit, 64=setsdxns, 128=has_paramnames
    public int flags;
    public ValueKind optional[];
    public int paramNames[];

    public boolean flagNeed_arguments() {
        return (flags & 1) == 1;
    }

    public boolean flagNeed_activation() {
        return (flags & 2) == 2;
    }

    public boolean flagNeed_rest() {
        return (flags & 4) == 4;
    }

    public boolean flagHas_optional() {
        return (flags & 8) == 8;
    }

    public boolean flagIgnore_restHas_optional() {
        return (flags & 16) == 16;
    }

    public boolean flagExplicit() {
        return (flags & 32) == 32;
    }

    public boolean flagSetsdxns() {
        return (flags & 64) == 64;
    }

    public boolean flagHas_paramnames() {
        return (flags & 128) == 128;
    }

    public MethodInfo(int param_types[], int ret_type, int name_index, int flags, ValueKind optional[], int paramNames[]) {
        this.param_types = param_types;
        this.ret_type = ret_type;
        this.name_index = name_index;
        this.flags = flags;
        this.optional = optional;
        this.paramNames = paramNames;
    }

    @Override
    public String toString() {
        String optionalStr = "[";
        if (optional != null) {
            for (int i = 0; i < optional.length; i++) {
                if (i > 0) {
                    optionalStr += ",";
                }
                optionalStr += optional[i].toString();
            }
        }
        optionalStr += "]";
        return "MethodInfo: param_types=" + Helper.intArrToString(param_types) + " ret_type=" + ret_type + " name_index=" + name_index + " flags=" + flags + " optional=" + optionalStr + " paramNames=" + Helper.intArrToString(paramNames);
    }

    public String toString(ConstantPool constants) {
        String optionalStr = "[";
        if (optional != null) {
            for (int i = 0; i < optional.length; i++) {
                if (i > 0) {
                    optionalStr += ",";
                }
                optionalStr += optional[i].toString(constants);
            }
        }
        optionalStr += "]";

        String param_typesStr = "";
        for (int i = 0; i < param_types.length; i++) {
            if (i > 0) {
                param_typesStr += ",";
            }
            if (param_types[i] == 0) {
                param_typesStr += "*";
            } else {
                param_typesStr += constants.constant_multiname[param_types[i]].toString(constants);
            }
        }

        String paramNamesStr = "";
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                paramNamesStr += ",";
            }
            paramNamesStr += constants.constant_string[paramNames[i]];
        }

        String ret_typeStr = "";
        if (ret_type == 0) {
            ret_typeStr += "*";
        } else {
            ret_typeStr += constants.constant_multiname[ret_type].toString(constants);
        }

        return "param_types=" + param_typesStr + " ret_type=" + ret_typeStr + " name=\"" + constants.constant_string[name_index] + "\" flags=" + flags + " optional=" + optionalStr + " paramNames=" + paramNamesStr;
    }


    public String getName(ConstantPool constants) {
        if (name_index == 0) return "UNKNOWN";
        return constants.constant_string[name_index];
    }

    public String getParamStr(ConstantPool constants) {
        String paramStr = "";
        for (int i = 0; i < param_types.length; i++) {
            if (i > 0) {
                paramStr += ", ";
            }
            if ((paramNames.length > i) && (paramNames[i] != 0)) {
                paramStr += constants.constant_string[paramNames[i]];
            } else {
                paramStr += "param" + (i + 1);
            }
            paramStr += ":";
            if (param_types[i] == 0) {
                paramStr += "*";
            } else {
                paramStr += constants.constant_multiname[param_types[i]].getName(constants);
            }
            if (optional != null) {
                if (i >= param_types.length - optional.length) {
                    //System.out.println("param_types.length:"+param_types.length);
                    //System.out.println("optional.lengt:"+optional.length);
                    paramStr += "=" + optional[i - (param_types.length - optional.length)].toString(constants);
                }
            }
        }
        return paramStr;
    }

    public String getReturnTypeStr(ConstantPool constants) {
        if (ret_type == 0) return "*";
        return constants.constant_multiname[ret_type].getName(constants);
    }
}

