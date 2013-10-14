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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.List;

public class MethodInfo {

    public int[] param_types;
    public int ret_type;
    public int name_index; //0=no name
    // 1=need_arguments, 2=need_activation, 4=need_rest 8=has_optional 16=ignore_rest, 32=explicit, 64=setsdxns, 128=has_paramnames
    public int flags;
    public ValueKind[] optional;
    public int[] paramNames;
    private MethodBody body;

    public void setFlagIgnore_Rest() {
        flags |= 16;
    }

    public void setFlagExplicit() {
        flags |= 32;
    }

    public void setFlagNeed_Arguments() {
        flags |= 1;
    }

    public void setFlagSetsdxns() {
        flags |= 64;
    }

    public void setFlagSetsdxns(boolean val) {
        if (val) {
            setFlagSetsdxns();
        } else {
            unsetFlagSetsdxns();
        }
    }

    public void unsetFlagSetsdxns() {
        if (flagSetsdxns()) {
            flags -= 64;
        }
    }

    public void setFlagNeed_activation() {
        flags |= 2;
    }

    public void setFlagNeed_activation(boolean val) {
        if (val) {
            setFlagNeed_activation();
        } else {
            unsetFlagNeed_activation();
        }
    }

    public void unsetFlagNeed_activation() {
        if (flagNeed_activation()) {
            flags -= 2;
        }
    }

    public void setFlagNeed_rest() {
        flags |= 4;
    }

    public void unsetFlagNeed_rest() {
        if (flagNeed_rest()) {
            flags -= 4;
        }
    }

    public void setFlagNeed_rest(boolean val) {
        if (val) {
            setFlagNeed_rest();
        } else {
            unsetFlagNeed_rest();
        }
    }

    public void setFlagHas_optional() {
        flags |= 8;
    }

    public void unsetFlagHas_optional() {
        if (flagHas_optional()) {
            flags -= 8;
        }
    }

    public void setFlagHas_optional(boolean val) {
        if (val) {
            setFlagHas_optional();
        } else {
            unsetFlagHas_optional();
        }
    }

    public void setFlagHas_paramnames() {
        flags |= 128;
    }

    public void unsetFlagHas_paramnames() {
        if (flagHas_paramnames()) {
            flags -= 128;
        }
    }

    public void setFlagHas_paramnames(boolean val) {
        if (val) {
            setFlagHas_paramnames();
        } else {
            unsetFlagHas_paramnames();
        }
    }

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

    public boolean flagIgnore_rest() {
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

    public MethodInfo(int[] param_types, int ret_type, int name_index, int flags, ValueKind[] optional, int[] paramNames) {
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

    public String toString(ConstantPool constants, List<String> fullyQualifiedNames) {
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
                param_typesStr += constants.constant_multiname[param_types[i]].toString(constants, fullyQualifiedNames);
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
            ret_typeStr += constants.constant_multiname[ret_type].toString(constants, fullyQualifiedNames);
        }

        return "param_types=" + param_typesStr + " ret_type=" + ret_typeStr + " name=\"" + constants.constant_string[name_index] + "\" flags=" + flags + " optional=" + optionalStr + " paramNames=" + paramNamesStr;
    }

    public String getName(ConstantPool constants) {
        if (name_index == 0) {
            return "UNKNOWN";
        }
        return constants.constant_string[name_index];
    }

    public HilightedTextWriter getParamStr(HilightedTextWriter writer, ConstantPool constants, MethodBody body, ABC abc, List<String> fullyQualifiedNames) {
        HashMap<Integer, String> localRegNames = new HashMap<>();
        if (body != null) {
            localRegNames = body.code.getLocalRegNamesFromDebug(abc);
        }
        for (int i = 0; i < param_types.length; i++) {
            if (i > 0) {
                writer.appendNoHilight(", ");
            }
            if (!localRegNames.isEmpty()) {
                writer.appendNoHilight(localRegNames.get(i + 1));
            } else if ((paramNames.length > i) && (paramNames[i] != 0) && Configuration.PARAM_NAMES_ENABLE) {
                writer.appendNoHilight(constants.constant_string[paramNames[i]]);
            } else {
                writer.appendNoHilight("param" + (i + 1));
            }
            writer.appendNoHilight(":");
            if (param_types[i] == 0) {
                writer.hilightSpecial("*", "param", i);
            } else {
                writer.hilightSpecial(constants.constant_multiname[param_types[i]].getName(constants, fullyQualifiedNames), "param", i);
            }
            if (optional != null) {
                if (i >= param_types.length - optional.length) {
                    int optionalIndex = i - (param_types.length - optional.length);
                    writer.appendNoHilight("=");
                    writer.hilightSpecial(optional[optionalIndex].toString(constants), "optional", optionalIndex);
                }
            }
        }
        if (flagNeed_rest()) {
            String restAdd = "";
            if ((param_types != null) && (param_types.length > 0)) {
                restAdd += ", ";
            }
            restAdd += "... ";
            if (!localRegNames.isEmpty()) {
                restAdd += localRegNames.get(param_types.length + 1);
            } else {
                restAdd += "rest";
            }
            writer.hilightSpecial(restAdd, "flag.NEED_REST");
        }
        return writer;
    }

    public HilightedTextWriter getReturnTypeStr(HilightedTextWriter writer, ConstantPool constants, List<String> fullyQualifiedNames) {
        return writer.hilightSpecial(ret_type == 0 ? "*" : constants.constant_multiname[ret_type].getName(constants, fullyQualifiedNames), "returns");
    }

    public void setBody(MethodBody body) {
        this.body = body;
    }

    public MethodBody getBody() {
        return body;
    }
}
