/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.ABCOutputStream;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ThrowIns;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AVM2Instruction implements Serializable, GraphSourceItem {

    public static final long serialVersionUID = 1L;
    public InstructionDefinition definition;
    public int[] operands;
    public long offset;
    public String comment;
    public boolean ignored = false;
    public long mappedOffset = -1;
    public int changeJumpTo = -1;

    public AVM2Instruction(long offset, InstructionDefinition definition, int[] operands) {
        this.definition = definition;
        this.operands = operands != null && operands.length > 0 ? operands : null;
        this.offset = offset;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ABCOutputStream aos = new ABCOutputStream(bos);
            aos.write(definition.instructionCode);
            for (int i = 0; i < definition.operands.length; i++) {
                int opt = definition.operands[i] & 0xff00;
                switch (opt) {
                    case AVM2Code.OPT_S24:
                        aos.writeS24(operands[i]);
                        break;
                    case AVM2Code.OPT_U30:
                        aos.writeU30(operands[i]);
                        break;
                    case AVM2Code.OPT_U8:
                        aos.writeU8(operands[i]);
                        break;
                    case AVM2Code.OPT_BYTE:
                        aos.writeU8(0xff & operands[i]);
                        break;
                    case AVM2Code.OPT_CASE_OFFSETS:

                        aos.writeU30(operands[i]); //case count
                        for (int j = i + 1; j < operands.length; j++) {
                            aos.writeS24(operands[j]);
                        }
                        break;
                }
            }
        } catch (IOException ex) {
            //ignored
        }
        return bos.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(definition.instructionName);
        if (operands !=  null) {
            for (int i = 0; i < operands.length; i++) {
                s.append(" ");
                s.append(operands[i]);
            }
        }
        return s.toString();
    }

    public List<Long> getOffsets() {
        List<Long> ret = new ArrayList<>();
        String s = "";
        for (int i = 0; i < definition.operands.length; i++) {
            switch (definition.operands[i]) {
                case AVM2Code.DAT_OFFSET:
                    ret.add(offset + operands[i] + getBytes().length);
                    break;
                case AVM2Code.DAT_CASE_BASEOFFSET:
                    ret.add(offset + operands[i]);
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    for (int j = i + 1; j < operands.length; j++) {
                        ret.add(offset + operands[j]);
                    }
                    break;
            }

        }
        return ret;
    }

    public List<Object> getParamsAsList(AVM2ConstantPool constants) {
        List<Object> s = new ArrayList<>();
        for (int i = 0; i < definition.operands.length; i++) {
            switch (definition.operands[i]) {
                case AVM2Code.DAT_MULTINAME_INDEX:
                    s.add(constants.getMultiname(operands[i]));
                    break;
                case AVM2Code.DAT_STRING_INDEX:
                    s.add(constants.getString(operands[i]));
                    break;
                case AVM2Code.DAT_INT_INDEX:
                    s.add(Long.valueOf(constants.getInt(operands[i])));
                    break;
                case AVM2Code.DAT_UINT_INDEX:
                    s.add(new Long(constants.getUInt(operands[i])));
                    break;
                case AVM2Code.DAT_DOUBLE_INDEX:
                    s.add(Double.valueOf(constants.getDouble(operands[i])));
                    break;
                case AVM2Code.DAT_OFFSET:
                    s.add(new Long(offset + operands[i] + getBytes().length));
                    break;
                case AVM2Code.DAT_CASE_BASEOFFSET:
                    s.add(new Long(offset + operands[i]));
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    s.add(new Long(operands[i]));
                    for (int j = i + 1; j < operands.length; j++) {
                        s.add(new Long(offset + operands[j]));
                    }
                    break;
                default:
                    s.add(new Long(operands[i]));
            }

        }
        return s;
    }

    public String getParams(AVM2ConstantPool constants, List<String> fullyQualifiedNames) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < definition.operands.length; i++) {
            switch (definition.operands[i]) {
                case AVM2Code.DAT_MULTINAME_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(constants.getMultiname(operands[i]).toString(constants, fullyQualifiedNames));
                    }
                    /*s.append(" m[");
                     s.append(operands[i]);
                     s.append("]\"");
                     if (constants.constant_multiname[operands[i]] == null) {
                     s.append("");
                     } else {
                     s.append(Helper.escapeString(constants.constant_multiname[operands[i]].toString(constants, fullyQualifiedNames)));
                     }
                     s.append("\"");*/
                    break;
                case AVM2Code.DAT_STRING_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" \"");
                        s.append(Helper.escapeString(constants.getString(operands[i])));
                        s.append("\"");
                    }
                    break;
                case AVM2Code.DAT_INT_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(constants.getInt(operands[i]));
                    }
                    break;
                case AVM2Code.DAT_UINT_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(constants.getUInt(operands[i]));
                    }
                    break;
                case AVM2Code.DAT_DOUBLE_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(constants.getDouble(operands[i]));
                    }
                    break;
                case AVM2Code.DAT_OFFSET:
                    s.append(" ");
                    s.append("ofs");
                    s.append(Helper.formatAddress(offset + operands[i] + getBytes().length));
                    break;
                case AVM2Code.DAT_CASE_BASEOFFSET:
                    s.append(" ");
                    s.append("ofs");
                    s.append(Helper.formatAddress(offset + operands[i]));
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    s.append(" ");
                    s.append(operands[i]);
                    for (int j = i + 1; j < operands.length; j++) {
                        s.append(" ");
                        s.append("ofs");
                        s.append(Helper.formatAddress(offset + operands[j]));
                    }
                    break;
                default:
                    s.append(" ");
                    s.append(operands[i]);
            }

        }
        return s.toString();
    }

    public String getComment() {
        if (ignored) {
            return " ;ignored";
        }
        if ((comment == null) || comment.isEmpty()) {
            return "";
        }
        return " ;" + comment;
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData) {
        writer.appendNoHilight(Helper.formatAddress(offset) + " " + Helper.padSpaceRight(Helper.byteArrToString(getBytes()), 30) + definition.instructionName);
        writer.appendNoHilight(getParams(localData.constantsAvm2, localData.fullyQualifiedNames) + getComment());
        return writer;
    }

    public String toStringNoAddress(AVM2ConstantPool constants, List<String> fullyQualifiedNames) {
        String s = definition.instructionName;
        s += getParams(constants, fullyQualifiedNames) + getComment();
        return s;
    }
    public List<Object> replaceWith;

    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        definition.translate(aLocalData.isStatic,
                aLocalData.scriptIndex,
                aLocalData.classIndex,
                aLocalData.localRegs,
                stack,
                aLocalData.scopeStack,
                aLocalData.constants, this, aLocalData.methodInfo, output, aLocalData.methodBody, aLocalData.abc, aLocalData.localRegNames, aLocalData.fullyQualifiedNames, null, aLocalData.localRegAssignmentIps, aLocalData.ip, aLocalData.refs, aLocalData.code);
    }

    @Override
    public boolean isJump() {
        return (definition instanceof JumpIns) || (fixedBranch > -1);
    }

    @Override
    public boolean isBranch() {
        if (fixedBranch > -1) {
            return false;
        }
        return (definition instanceof IfTypeIns) || (definition instanceof LookupSwitchIns);
    }

    @Override
    public boolean isExit() {
        return (definition instanceof ReturnValueIns) || (definition instanceof ReturnVoidIns) || (definition instanceof ThrowIns);
    }

    @Override
    public long getOffset() {
        return mappedOffset > -1 ? mappedOffset : offset;
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        List<Integer> ret = new ArrayList<>();
        if (definition instanceof IfTypeIns) {

            if (fixedBranch == -1 || fixedBranch == 0) {
                ret.add(code.adr2pos(offset + getBytes().length + operands[0]));
            }
            if (!(definition instanceof JumpIns)) {
                if (fixedBranch == -1 || fixedBranch == 1) {
                    ret.add(code.adr2pos(offset + getBytes().length));
                }
            }
        }
        if (definition instanceof LookupSwitchIns) {
            if (fixedBranch == -1 || fixedBranch == 0) {
                ret.add(code.adr2pos(offset + operands[0]));
            }
            for (int k = 2; k < operands.length; k++) {
                if (fixedBranch == -1 || fixedBranch == k - 1) {
                    ret.add(code.adr2pos(offset + operands[k]));
                }
            }
        }
        return ret;
    }

    @Override
    public boolean ignoredLoops() {
        return false;
    }

    @Override
    public void setIgnored(boolean ignored, int pos) {
        this.ignored = ignored;
    }

    public void setFixBranch(int pos) {
        this.fixedBranch = pos;
    }
    private int fixedBranch = -1;

    public int getFixBranch() {
        return fixedBranch;
    }

    @Override
    public boolean isDeobfuscatePop() {
        return definition instanceof DeobfuscatePopIns;
    }
}
