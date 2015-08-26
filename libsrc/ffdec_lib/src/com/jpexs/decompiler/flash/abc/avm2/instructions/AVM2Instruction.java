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
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AVM2Instruction implements Cloneable, GraphSourceItem {

    public InstructionDefinition definition;

    public int[] operands;

    public long offset;

    public String comment;

    public boolean ignored = false;

    public long mappedOffset = -1;

    public int changeJumpTo = -1;

    private int line;

    private String file;

    public void setFileLine(String file, int line) {
        this.file = file;
        this.line = line;
    }

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
            // ignored
        }
        return bos.toByteArray();
    }

    public int getBytesLength() {
        int cnt = 1;
        for (int i = 0; i < definition.operands.length; i++) {
            int opt = definition.operands[i] & 0xff00;
            switch (opt) {
                case AVM2Code.OPT_S24:
                    cnt += 3;
                    break;
                case AVM2Code.OPT_U30:
                    cnt += ABCOutputStream.getU30ByteLength(operands[i]);
                    break;
                case AVM2Code.OPT_U8:
                    cnt++;
                    break;
                case AVM2Code.OPT_BYTE:
                    cnt++;
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    cnt += ABCOutputStream.getU30ByteLength(operands[i]); //case count
                    for (int j = i + 1; j < operands.length; j++) {
                        cnt += 3;
                    }

                    break;
            }
        }

        return cnt;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(definition.instructionName);
        if (operands != null) {
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
                    ret.add(offset + operands[i] + getBytesLength());
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
                    s.add(constants.getInt(operands[i]));
                    break;
                case AVM2Code.DAT_UINT_INDEX:
                    s.add(constants.getUInt(operands[i]));
                    break;
                case AVM2Code.DAT_DOUBLE_INDEX:
                    s.add(constants.getDouble(operands[i]));
                    break;
                case AVM2Code.DAT_OFFSET:
                    s.add(offset + operands[i] + getBytesLength());
                    break;
                case AVM2Code.DAT_CASE_BASEOFFSET:
                    s.add(offset + operands[i]);
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    s.add((long) operands[i]);
                    for (int j = i + 1; j < operands.length; j++) {
                        s.add(offset + operands[j]);
                    }
                    break;
                default:
                    s.add((long) operands[i]);
            }

        }
        return s;
    }

    public String getParams(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < definition.operands.length; i++) {
            switch (definition.operands[i]) {
                case AVM2Code.DAT_MULTINAME_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        Multiname multiname = constants.getMultiname(operands[i]);
                        if (multiname != null) {
                            s.append(multiname.toString(constants, fullyQualifiedNames));
                        } else {
                            s.append("Multiname not found.");
                        }
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
                    String str;
                    if (operands[i] == 0 || (str = constants.getString(operands[i])) == null) {
                        s.append(" null");
                    } else {
                        s.append(" \"");
                        s.append(Helper.escapeActionScriptString(str));
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
                    s.append(Helper.formatAddress(offset + operands[i] + getBytesLength()));
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
        writer.appendNoHilight(Helper.formatAddress(offset) + " " + String.format("%-30s", Helper.byteArrToString(getBytes())) + definition.instructionName);
        writer.appendNoHilight(getParams(localData.constantsAvm2, localData.fullyQualifiedNames) + getComment());
        return writer;
    }

    public String toStringNoAddress(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String s = definition.instructionName;
        s += getParams(constants, fullyQualifiedNames) + getComment();
        return s;
    }

    public List<Object> replaceWith;

    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        //int expectedSize = stack.size() - getStackPopCount(localData, stack) + getStackPushCount(localData, stack);
        definition.translate(aLocalData.isStatic,
                aLocalData.scriptIndex,
                aLocalData.classIndex,
                aLocalData.localRegs,
                stack,
                aLocalData.scopeStack,
                aLocalData.constants, this, aLocalData.methodInfo, output, aLocalData.methodBody, aLocalData.abc, aLocalData.localRegNames, aLocalData.fullyQualifiedNames, null, aLocalData.localRegAssignmentIps, aLocalData.ip, aLocalData.refs, aLocalData.code);
        /*if (stack.size() != expectedSize) {
         throw new Error("HONFIKA stack size mismatch");
         }*/
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        return getStackPopCount(aLocalData);
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        return getStackPushCount(aLocalData);
    }

    public int getStackPopCount(AVM2LocalData aLocalData) {
        return definition.getStackPopCount(this, aLocalData.abc);
    }

    public int getStackPushCount(AVM2LocalData aLocalData) {
        return definition.getStackPushCount(this, aLocalData.abc);
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
                ret.add(code.adr2pos(offset + getBytesLength() + operands[0]));
            }
            if (!(definition instanceof JumpIns)) {
                if (fixedBranch == -1 || fixedBranch == 1) {
                    ret.add(code.adr2pos(offset + getBytesLength()));
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

    @Override
    public AVM2Instruction clone() {
        try {
            AVM2Instruction ret = (AVM2Instruction) super.clone();
            if (operands != null) {
                ret.operands = Arrays.copyOf(operands, operands.length);
            }
            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getFile() {
        return file;
    }
}
