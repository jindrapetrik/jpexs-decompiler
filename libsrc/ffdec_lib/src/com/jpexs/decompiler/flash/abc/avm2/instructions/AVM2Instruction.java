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
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
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

/**
 *
 * @author JPEXS
 */
public class AVM2Instruction implements Cloneable, GraphSourceItem {

    public InstructionDefinition definition;

    public int[] operands;

    private long address;

    public String comment;

    private boolean ignored = false;

    private int line;

    private String file;

    @Override
    public long getFileOffset() {
        return -1;
    }

    @Override
    public long getLineOffset() {
        return getAddress();
    }

    public void setFileLine(String file, int line) {
        this.file = file;
        this.line = line;
    }

    public AVM2Instruction(long offset, int insructionCode, int[] operands) {
        this(offset, AVM2Code.instructionSet[insructionCode], operands);
    }

    public AVM2Instruction(long address, InstructionDefinition definition, int[] operands) {
        this.definition = definition;
        this.operands = operands != null && operands.length > 0 ? operands : null;
        this.address = address;
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
                    case AVM2Code.OPT_S16:
                        aos.writeU30(operands[i]);
                        break;
                    case AVM2Code.OPT_U8:
                        aos.writeU8(operands[i]);
                        break;
                    case AVM2Code.OPT_S8:
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

    @Override
    public int getBytesLength() {
        int cnt = 1;
        for (int i = 0; i < definition.operands.length; i++) {
            int opt = definition.operands[i] & 0xff00;
            switch (opt) {
                case AVM2Code.OPT_S24:
                    cnt += 3;
                    break;
                case AVM2Code.OPT_U30:
                case AVM2Code.OPT_S16:
                    cnt += ABCOutputStream.getU30ByteLength(operands[i]);
                    break;
                case AVM2Code.OPT_U8:
                    cnt++;
                    break;
                case AVM2Code.OPT_S8:
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
                    ret.add(address + operands[i] + getBytesLength());
                    break;
                case AVM2Code.DAT_CASE_BASEOFFSET:
                    ret.add(address + operands[i]);
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    for (int j = i + 1; j < operands.length; j++) {
                        ret.add(address + operands[j]);
                    }
                    break;
            }
        }

        return ret;
    }

    public Object getParam(AVM2ConstantPool constants, int idx) {
        //if (idx < 0 || idx >= definition.operands.length) {
        //    return null;
        //}

        switch (definition.operands[idx]) {
            case AVM2Code.DAT_MULTINAME_INDEX:
                return constants.getMultiname(operands[idx]);
            case AVM2Code.DAT_STRING_INDEX:
                return constants.getString(operands[idx]);
            case AVM2Code.DAT_INT_INDEX:
                return constants.getInt(operands[idx]);
            case AVM2Code.DAT_UINT_INDEX:
                return constants.getUInt(operands[idx]);
            case AVM2Code.DAT_DOUBLE_INDEX:
                return constants.getDouble(operands[idx]);
            case AVM2Code.DAT_OFFSET:
                return address + operands[idx] + getBytesLength();
            case AVM2Code.DAT_CASE_BASEOFFSET:
                return address + operands[idx];
            case AVM2Code.OPT_CASE_OFFSETS:
                return (long) operands[idx]; // offsets: offset + operands[i];
            default:
                return (long) operands[idx];
        }
    }

    public Long getParamAsLong(AVM2ConstantPool constants, int idx) {
        return (Long) getParam(constants, idx);
    }

    public String getParams(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < definition.operands.length; i++) {
            switch (definition.operands[i]) {
                case AVM2Code.DAT_NAMESPACE_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(Multiname.namespaceToString(constants, operands[i]));
                    }
                    break;
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
                case AVM2Code.DAT_FLOAT_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(constants.getFloat(operands[i]));
                    }
                    break;
                case AVM2Code.DAT_FLOAT4_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        Float4 f4 = constants.getFloat4(operands[i]);
                        s.append(" ").append(f4.values[0]);
                        s.append(" ").append(f4.values[1]);
                        s.append(" ").append(f4.values[2]);
                        s.append(" ").append(f4.values[3]);
                    }
                    break;
                case AVM2Code.DAT_DECIMAL_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        s.append(constants.getDecimal(operands[i]));
                    }
                    break;
                case AVM2Code.DAT_OFFSET:
                    s.append(" ");
                    s.append("ofs");
                    s.append(Helper.formatAddress(address + operands[i] + getBytesLength()));
                    break;
                case AVM2Code.DAT_CASE_BASEOFFSET:
                    s.append(" ");
                    s.append("ofs");
                    s.append(Helper.formatAddress(address + operands[i]));
                    break;
                case AVM2Code.OPT_CASE_OFFSETS:
                    s.append(" ");
                    s.append(operands[i]);
                    for (int j = i + 1; j < operands.length; j++) {
                        s.append(" ");
                        s.append("ofs");
                        s.append(Helper.formatAddress(address + operands[j]));
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
        if (isIgnored()) {
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
        writer.appendNoHilight(Helper.formatAddress(address) + " " + String.format("%-30s", Helper.byteArrToString(getBytes())) + definition.instructionName);
        writer.appendNoHilight(getParams(localData.constantsAvm2, localData.fullyQualifiedNames) + getComment());
        return writer;
    }

    public String toStringNoAddress(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String s = definition.instructionName;
        s += getParams(constants, fullyQualifiedNames) + getComment();
        return s;
    }

    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        //int expectedSize = stack.size() - getStackPopCount(localData, stack) + getStackPushCount(localData, stack);
        definition.translate(aLocalData, stack, this, output, null);
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
        return definition instanceof JumpIns;
    }

    @Override
    public boolean isBranch() {
        return (definition instanceof IfTypeIns) || (definition instanceof LookupSwitchIns);
    }

    @Override
    public boolean isExit() {
        return (definition instanceof ReturnValueIns) || (definition instanceof ReturnVoidIns) || (definition instanceof ThrowIns);
    }

    @Override
    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public long getTargetAddress() {
        return address + 4 /*getBytesLength()*/ + operands[0];
    }

    public void setTargetOffset(int offset) {
        operands[0] = offset;
    }

    @Override
    public List<Integer> getBranches(GraphSource code) {
        List<Integer> ret = new ArrayList<>();
        if (definition instanceof IfTypeIns) {

            ret.add(code.adr2pos(getTargetAddress()));
            if (!(definition instanceof JumpIns)) {
                ret.add(code.adr2pos(address + getBytesLength()));
            }
        }
        if (definition instanceof LookupSwitchIns) {
            ret.add(code.adr2pos(address + operands[0]));
            for (int k = 2; k < operands.length; k++) {
                ret.add(code.adr2pos(address + operands[k]));
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

    /**
     * Set operand value the right way - update offsets neccessarily. Because
     * some operand types are variable length (like U30)
     *
     * @param operandIndex
     * @param newValue
     * @param code
     * @param body
     */
    public void setOperand(int operandIndex, int newValue, AVM2Code code, MethodBody body) {
        int oldByteCount = getBytesLength();
        operands[operandIndex] = newValue;
        int newByteCount = getBytesLength();
        int byteDelta = newByteCount - oldByteCount;
        if (byteDelta != 0) {
            code.updateInstructionByteCountByAddr(address, byteDelta, body);
        }
        body.setModified();
    }

    /**
     * Set operand values the right way - update offsets neccessarily. Because
     * some operand types are variable length (like U30)
     *
     * @param operands
     * @param code
     * @param body
     */
    public void setOperands(int operands[], AVM2Code code, MethodBody body) {
        int oldByteCount = getBytesLength();
        this.operands = operands;
        int newByteCount = getBytesLength();
        int byteDelta = newByteCount - oldByteCount;
        if (byteDelta != 0) {
            code.updateInstructionByteCountByAddr(address, byteDelta, body);
        }
        body.setModified();
    }

}
