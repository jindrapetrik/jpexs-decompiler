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

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class MethodInfo {

    @Internal
    public boolean deleted;

    public void delete(ABC abc, boolean d) {
        this.deleted = true;
        MethodBody body = abc.findBody(this);
        if (body != null) {
            for (AVM2Instruction ins : body.getCode().code) {
                if (ins.definition instanceof NewFunctionIns) {
                    if (ins.operands[0] < abc.method_info.size() && !abc.method_info.get(ins.operands[0]).deleted) {
                        abc.method_info.get(ins.operands[0]).delete(abc, d);
                    }
                }
            }
        }
    }

    public int[] param_types = new int[]{};

    public int ret_type;

    public int name_index; //0=no name
    // 1=need_arguments, 2=need_activation, 4=need_rest 8=has_optional 16=ignore_rest, 32=explicit, 64=setsdxns, 128=has_paramnames

    public static int FLAG_NEED_ARGUMENTS = 1;

    public static int FLAG_NEED_ACTIVATION = 2;

    public static int FLAG_NEED_REST = 4;

    public static int FLAG_HAS_OPTIONAL = 8;

    public static int FLAG_IGNORE_REST = 16;

    public static int FLAG_EXPLICIT = 32;

    public static int FLAG_SETSDXNS = 64;

    public static int FLAG_HAS_PARAMNAMES = 128;

    public int flags;

    public ValueKind[] optional = new ValueKind[0];

    public int[] paramNames = new int[0];

    public void setFlagIgnore_Rest() {
        flags |= FLAG_IGNORE_REST;
    }

    public void setFlagExplicit() {
        flags |= FLAG_EXPLICIT;
    }

    public void setFlagNeed_Arguments() {
        flags |= FLAG_NEED_ARGUMENTS;
    }

    public void setFlagSetsdxns() {
        flags |= FLAG_SETSDXNS;
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
            flags -= FLAG_SETSDXNS;
        }
    }

    public void setFlagNeed_activation() {
        flags |= FLAG_NEED_ACTIVATION;
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
            flags -= FLAG_NEED_ACTIVATION;
        }
    }

    public void setFlagNeed_rest() {
        flags |= FLAG_NEED_REST;
    }

    public void unsetFlagNeed_rest() {
        if (flagNeed_rest()) {
            flags -= FLAG_NEED_REST;
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
        flags |= FLAG_HAS_OPTIONAL;
    }

    public void unsetFlagHas_optional() {
        if (flagHas_optional()) {
            flags -= FLAG_HAS_OPTIONAL;
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
        flags |= FLAG_HAS_PARAMNAMES;
    }

    public void unsetFlagHas_paramnames() {
        if (flagHas_paramnames()) {
            flags -= FLAG_HAS_PARAMNAMES;
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
        return (flags & FLAG_NEED_ARGUMENTS) == FLAG_NEED_ARGUMENTS;
    }

    public boolean flagNeed_activation() {
        return (flags & FLAG_NEED_ACTIVATION) == FLAG_NEED_ACTIVATION;
    }

    public boolean flagNeed_rest() {
        return (flags & FLAG_NEED_REST) == FLAG_NEED_REST;
    }

    public boolean flagHas_optional() {
        return (flags & FLAG_HAS_OPTIONAL) == FLAG_HAS_OPTIONAL;
    }

    public boolean flagIgnore_rest() {
        return (flags & FLAG_IGNORE_REST) == FLAG_IGNORE_REST;
    }

    public boolean flagExplicit() {
        return (flags & FLAG_EXPLICIT) == FLAG_EXPLICIT;
    }

    public boolean flagSetsdxns() {
        return (flags & FLAG_SETSDXNS) == FLAG_SETSDXNS;
    }

    public boolean flagHas_paramnames() {
        return (flags & FLAG_HAS_PARAMNAMES) == FLAG_HAS_PARAMNAMES;
    }

    public MethodInfo() {
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
        StringBuilder ret = new StringBuilder();
        ret.append("MethodInfo: param_types=");
        Helper.intArrToStringBuilder(param_types, ret);
        ret.append(" ret_type=").append(ret_type)
                .append(" name_index=").append(name_index)
                .append(" flags=").append(flags)
                .append(" optional=[");
        if (optional != null) {
            for (int i = 0; i < optional.length; i++) {
                if (i > 0) {
                    ret.append(",");
                }
                ret.append(optional[i].toString());
            }
        }
        ret.append("]");
        ret.append(" paramNames=");
        Helper.intArrToStringBuilder(paramNames, ret);
        return ret.toString();
    }

    public String toString(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        StringBuilder optionalStr = new StringBuilder();
        optionalStr.append("[");
        if (optional != null) {
            for (int i = 0; i < optional.length; i++) {
                if (i > 0) {
                    optionalStr.append(",");
                }
                optionalStr.append(optional[i].toString(constants));
            }
        }
        optionalStr.append("]");

        StringBuilder param_typesStr = new StringBuilder();
        for (int i = 0; i < param_types.length; i++) {
            if (i > 0) {
                param_typesStr.append(",");
            }
            if (param_types[i] == 0) {
                param_typesStr.append("*");
            } else {
                param_typesStr.append(constants.getMultiname(param_types[i]).toString(constants, fullyQualifiedNames));
            }
        }

        StringBuilder paramNamesStr = new StringBuilder();
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) {
                paramNamesStr.append(",");
            }
            paramNamesStr.append(constants.getString(paramNames[i]));
        }

        String ret_typeStr;
        if (ret_type == 0) {
            ret_typeStr = "*";
        } else {
            ret_typeStr = constants.getMultiname(ret_type).toString(constants, fullyQualifiedNames);
        }

        return "param_types=" + param_typesStr + " ret_type=" + ret_typeStr + " name=\"" + constants.getString(name_index) + "\" flags=" + flags + " optional=" + optionalStr + " paramNames=" + paramNamesStr;
    }

    public String getName(AVM2ConstantPool constants) {
        if (name_index == 0) {
            return "UNKNOWN";
        }
        return constants.getString(name_index);
    }

    public int getMaxReservedReg() {
        return param_types.length + (flagNeed_rest() ? 1 : 0) + (flagNeed_arguments() ? 1 : 0);
    }

    public GraphTextWriter getParamStr(GraphTextWriter writer, AVM2ConstantPool constants, MethodBody body, ABC abc, List<DottedChain> fullyQualifiedNames) {
        Map<Integer, String> localRegNames = new HashMap<>();
        if (body != null && Configuration.getLocalNamesFromDebugInfo.get()) {
            localRegNames = body.getCode().getLocalRegNamesFromDebug(abc);
        }

        for (int i = 0; i < param_types.length; i++) {
            if (i > 0) {
                writer.appendNoHilight(", ");
            }
            DottedChain ptype = DottedChain.ALL;
            if (param_types[i] > 0) {
                ptype = constants.getMultiname(param_types[i]).getNameWithNamespace(constants, true);
            }

            HighlightData pdata = new HighlightData();
            pdata.declaration = true;
            pdata.declaredType = ptype;
            pdata.regIndex = i + 1;
            if (!localRegNames.isEmpty()) {
                pdata.localName = localRegNames.get(i + 1); //assuming it is a slot
                writer.hilightSpecial(IdentifiersDeobfuscation.printIdentifier(true, localRegNames.get(i + 1)), HighlightSpecialType.PARAM_NAME, i, pdata);
            } else if ((paramNames.length > i) && (paramNames[i] != 0) && Configuration.paramNamesEnable.get()) {
                pdata.localName = constants.getString(paramNames[i]);
                writer.hilightSpecial(IdentifiersDeobfuscation.printIdentifier(true, constants.getString(paramNames[i])), HighlightSpecialType.PARAM_NAME, i, pdata);
            } else {
                pdata.localName = "param" + (i + 1);
                writer.hilightSpecial(pdata.localName, HighlightSpecialType.PARAM_NAME, i, pdata);
            }
            writer.appendNoHilight(":");
            if (param_types[i] == 0) {
                writer.hilightSpecial("*", HighlightSpecialType.PARAM, i);
            } else {
                writer.hilightSpecial(constants.getMultiname(param_types[i]).getName(constants, fullyQualifiedNames, false, true), HighlightSpecialType.PARAM, i);
            }
            if (optional != null) {
                if (i >= param_types.length - optional.length) {
                    int optionalIndex = i - (param_types.length - optional.length);
                    writer.appendNoHilight(" = ");
                    writer.hilightSpecial(optional[optionalIndex].toString(constants), HighlightSpecialType.OPTIONAL, optionalIndex);
                }
            }
        }
        if (flagNeed_rest()) {
            String restAdd = "";
            if ((param_types != null) && (param_types.length > 0)) {
                restAdd = ", ";
            }
            restAdd += "... ";
            String restName;
            if (!localRegNames.isEmpty()) {
                restName = localRegNames.get(param_types.length + 1);
            } else {
                restName = "rest";
            }

            HighlightData pdata = new HighlightData();
            pdata.declaration = true;
            pdata.declaredType = DottedChain.ALL;
            pdata.regIndex = param_types.length + 1;
            pdata.localName = restName;
            writer.append(restAdd);
            writer.hilightSpecial(restName, HighlightSpecialType.FLAG_NEED_REST, 0, pdata);
        }
        return writer;
    }

    public GraphTextWriter getReturnTypeStr(GraphTextWriter writer, AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String rname = "*";
        if (ret_type > 0) {
            Multiname multiname = constants.getMultiname(ret_type);
            if (multiname.kind != Multiname.TYPENAME && multiname.name_index > 0 && constants.getString(multiname.name_index).equals("void")) {
                rname = "void";
            } else {
                rname = multiname.getName(constants, fullyQualifiedNames, false, true);
            }
        }
        return writer.hilightSpecial(rname, HighlightSpecialType.RETURNS);
    }

    public String getReturnTypeRaw(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String rname = "*";
        if (ret_type > 0) {
            Multiname multiname = constants.getMultiname(ret_type);
            if (multiname.kind != Multiname.TYPENAME && multiname.name_index > 0 && constants.getString(multiname.name_index).equals("void")) {
                rname = "void";
            } else {
                rname = multiname.getName(constants, fullyQualifiedNames, false, true);
            }
        }
        return rname;
    }
}
