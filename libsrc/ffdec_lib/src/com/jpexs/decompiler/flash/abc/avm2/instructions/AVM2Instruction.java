/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.NumberContext;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ThrowIns;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AVM2 instruction.
 *
 * @author JPEXS
 */
public class AVM2Instruction implements Cloneable, GraphSourceItem {

    /**
     * Definition
     */
    public InstructionDefinition definition;

    /**
     * Operands
     */
    public int[] operands;

    /**
     * Address
     */
    private long address;

    /**
     * Comment
     */
    public String comment;

    /**
     * Ignored
     */
    private boolean ignored = false;

    /**
     * Line
     */
    private int line;

    /**
     * File
     */
    private String file;

    /**
     * Virtual address - used for deobfuscation
     */
    private long virtualAddress = -1;

    /**
     * Old style names for getlocal and setlocal
     */
    private static final Map<String, String> oldStyleNames = new HashMap<>();

    static {
        for (int i = 0; i <= 3; i++) {
            oldStyleNames.put("getlocal" + i, "getlocal_" + i);
            oldStyleNames.put("setlocal" + i, "setlocal_" + i);
        }
    }

    /**
     * Gets file offset.
     *
     * @return File offset
     */
    @Override
    public long getFileOffset() {
        return -1;
    }

    /**
     * Gets the line offset.
     *
     * @return Line offset
     */
    @Override
    public long getLineOffset() {
        if (virtualAddress > -1) {
            return virtualAddress;
        }
        return getAddress();
    }

    /**
     * Sets file and line.
     *
     * @param file File
     * @param line Line
     */
    public void setFileLine(String file, int line) {
        this.file = file;
        this.line = line;
    }

    /**
     * Constructs a new AVM2 instruction.
     *
     * @param offset Offset
     * @param instructionCode Instruction code
     * @param operands Operands
     */
    public AVM2Instruction(long offset, int instructionCode, int[] operands) {
        this(offset, AVM2Code.instructionSet[instructionCode], operands);
    }

    /**
     * Constructs a new AVM2 instruction.
     *
     * @param address Address
     * @param definition Definition
     * @param operands Operands
     */
    public AVM2Instruction(long address, InstructionDefinition definition, int[] operands) {
        this.definition = definition;
        this.operands = operands != null && operands.length > 0 ? operands : null;
        this.address = address;
    }

    /**
     * Gets the bytes.
     *
     * @return Bytes
     */
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

    /**
     * Gets the length of the bytes.
     *
     * @return Length of the bytes
     */
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

    /**
     * Gets the offsets.
     *
     * @return Offsets
     */
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

    /**
     * Gets the parameter.
     *
     * @param constants Constants
     * @param idx Index of operand
     * @return Parameter
     */
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
            case AVM2Code.DAT_DECIMAL_INDEX:
                return constants.getDecimal(operands[idx]);
            case AVM2Code.DAT_FLOAT_INDEX:
                return constants.getFloat(operands[idx]);
            case AVM2Code.DAT_FLOAT4_INDEX:
                return constants.getFloat4(operands[idx]);
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

    /**
     * Gets the parameter as a long.
     *
     * @param constants Constants
     * @param idx Index of operand
     * @return Parameter as a long
     */
    public Long getParamAsLong(AVM2ConstantPool constants, int idx) {
        return (Long) getParam(constants, idx);
    }

    /**
     * Gets all parameters as string.
     *
     * @param constants Constants
     * @param fullyQualifiedNames Fully qualified names
     * @return All parameters as string
     */
    public String getParams(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < definition.operands.length; i++) {
            if (i > 0) {
                s.append(",");
            }
            switch (definition.operands[i]) {
                case AVM2Code.DAT_NAMESPACE_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            s.append(Multiname.namespaceToString(constants, operands[i]));
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_MULTINAME_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            Multiname multiname = constants.getMultiname(operands[i]);
                            if (multiname != null) {
                                s.append(multiname.toString(constants, fullyQualifiedNames));
                            }
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
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
                    try {
                        if (operands[i] == 0 || (str = constants.getString(operands[i])) == null) {
                            s.append(" null");
                        } else {
                            s.append(" \"");
                            s.append(Helper.escapePCodeString(str));
                            s.append("\"");
                        }
                    } catch (IndexOutOfBoundsException iob) {
                        s.append(" Unknown(").append(operands[i]).append(")");
                    }
                    break;
                case AVM2Code.DAT_INT_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            s.append(constants.getInt(operands[i]));
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_UINT_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            s.append(constants.getUInt(operands[i]));
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_DOUBLE_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            s.append(EcmaScript.toString(constants.getDouble(operands[i])));
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_FLOAT_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            s.append(EcmaScript.toString(constants.getFloat(operands[i])));
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_FLOAT4_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        try {
                            Float4 f4 = constants.getFloat4(operands[i]);
                            s.append(" ").append(EcmaScript.toString(f4.values[0]));
                            s.append(" ").append(EcmaScript.toString(f4.values[1]));
                            s.append(" ").append(EcmaScript.toString(f4.values[2]));
                            s.append(" ").append(EcmaScript.toString(f4.values[3]));
                        } catch (IndexOutOfBoundsException iob) {
                            s.append(" Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_DECIMAL_INDEX:
                    if (operands[i] == 0) {
                        s.append(" null");
                    } else {
                        s.append(" ");
                        try {
                            s.append(constants.getDecimal(operands[i]).toActionScriptString());
                        } catch (IndexOutOfBoundsException iob) {
                            s.append("Unknown(").append(operands[i]).append(")");
                        }
                    }
                    break;
                case AVM2Code.DAT_NUMBER_CONTEXT:
                    NumberContext nc = new NumberContext(operands[i]);
                    s.append(" ");
                    s.append(nc.toString());
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
                    if (Configuration.useOldStyleLookupSwitchAs3PCode.get()) {
                        s.append(" ");
                        s.append(operands[i]);
                    } else {
                        s.append(" [");
                    }
                    boolean first = !Configuration.useOldStyleLookupSwitchAs3PCode.get();
                    for (int j = i + 1; j < operands.length; j++) {
                        if (!first) {
                            s.append(", ");
                        }
                        first = false;
                        s.append("ofs");
                        s.append(Helper.formatAddress(address + operands[j]));
                    }
                    if (!Configuration.useOldStyleLookupSwitchAs3PCode.get()) {
                        s.append("]");
                    }
                    break;
                default:
                    s.append(" ");
                    s.append(operands[i]);
            }

        }
        return s.toString();
    }

    /**
     * Gets the comment.
     *
     * @return Comment
     */
    public String getComment() {
        if (isIgnored()) {
            return " ;ignored";
        }
        if ((comment == null) || comment.isEmpty()) {
            return "";
        }
        return " ;" + comment;
    }

    /**
     * Checks whether this item is ignored.
     *
     * @return True if this item is ignored, false otherwise
     */
    @Override
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * To string.
     *
     * @return String
     */
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

    /**
     * To string.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     */
    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData) {
        writer.appendNoHilight(Helper.formatAddress(address) + " " + String.format("%-30s", Helper.byteArrToString(getBytes())) + getCustomizedInstructionName());
        writer.appendNoHilight(getParams(localData.constantsAvm2, localData.fullyQualifiedNames) + getComment());
        return writer;
    }

    /**
     * Gets the customized instruction name.
     *
     * @return Customized instruction name
     */
    private String getCustomizedInstructionName() {
        if (Configuration.useOldStyleGetSetLocalsAs3PCode.get() && oldStyleNames.containsKey(definition.instructionName)) {
            return oldStyleNames.get(definition.instructionName);
        }
        return definition.instructionName;
    }

    /**
     * To string without address.
     *
     * @param constants Constants
     * @param fullyQualifiedNames Fully qualified names
     * @return String without address
     */
    public String toStringNoAddress(AVM2ConstantPool constants, List<DottedChain> fullyQualifiedNames) {
        String s = getCustomizedInstructionName();
        if (Configuration.padAs3PCodeInstructionName.get()) {
            for (int i = s.length(); i < 19; i++) {
                s += " ";
            }
        }
        s += getParams(constants, fullyQualifiedNames) + getComment();
        return s.trim();
    }

    /**
     * Translate the item to target items.
     *
     * @param localData Local data
     * @param stack Stack
     * @param output Output list
     * @param staticOperation Unused
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) throws InterruptedException {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        //int expectedSize = stack.size() - getStackPopCount(localData, stack) + getStackPushCount(localData, stack);
        definition.translate(aLocalData, stack, this, output, null);
        /*if (stack.size() != expectedSize) {
         throw new Error("HONFIKA stack size mismatch");
         }*/
    }

    /**
     * Gets the number of stack items that are popped by this item.
     *
     * @param localData Local data
     * @param stack Stack
     * @return Number of stack items that are popped by this item
     */
    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        return getStackPopCount(aLocalData);
    }

    /**
     * Gets the number of stack items that are popped by this item.
     *
     * @param aLocalData Local data
     * @return Number of stack items that are popped by this item
     */
    public int getStackPopCount(AVM2LocalData aLocalData) {
        return definition.getStackPopCount(this, aLocalData.abc);
    }

    /**
     * Gets the number of stack items that are pushed by this item.
     *
     * @param localData Local data
     * @param stack Stack
     * @return Number of stack items that are pushed by this item
     */
    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        return getStackPushCount(aLocalData);
    }

    /**
     * Gets the number of stack items that are pushed by this item.
     *
     * @param aLocalData Local data
     * @return Number of stack items that are pushed by this item
     */
    public int getStackPushCount(AVM2LocalData aLocalData) {
        return definition.getStackPushCount(this, aLocalData.abc);
    }

    /**
     * Checks whether this item is a jump.
     *
     * @return True if this item is a jump, false otherwise
     */
    @Override
    public boolean isJump() {
        return definition instanceof JumpIns;
    }

    /**
     * Checks whether this item is a branch.
     *
     * @return True if this item is a branch, false otherwise
     */
    @Override
    public boolean isBranch() {
        return (definition instanceof IfTypeIns) || (definition instanceof LookupSwitchIns);
    }

    /**
     * Checks whether this item is an exit (throw, return, etc.).
     *
     * @return True if this item is an exit, false otherwise
     */
    @Override
    public boolean isExit() {
        return (definition instanceof ReturnValueIns) || (definition instanceof ReturnVoidIns) || (definition instanceof ThrowIns);
    }

    /**
     * Gets the address.
     *
     * @return Address
     */
    @Override
    public long getAddress() {
        return address;
    }

    /**
     * Sets the address.
     *
     * @param address Address
     */
    public void setAddress(long address) {
        this.address = address;
    }

    /**
     * Gets the target address of jump.
     *
     * @return Target address.
     */
    public long getTargetAddress() {
        return address + 4 /*getBytesLength()*/ + operands[0];
    }

    /**
     * Sets the target offset of jump.
     *
     * @param offset Offset
     */
    public void setTargetOffset(int offset) {
        operands[0] = offset;
    }

    /**
     * Gets branches
     *
     * @param code Code
     * @return List of IPs to branch to
     */
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

    /**
     * Checks whether the loops are ignored.
     *
     * @return True if the loops are ignored, false otherwise
     */
    @Override
    public boolean ignoredLoops() {
        return false;
    }

    /**
     * Sets whether this item is ignored.
     *
     * @param ignored True if this item is ignored, false otherwise
     * @param pos Sub position
     */
    @Override
    public void setIgnored(boolean ignored, int pos) {
        this.ignored = ignored;
    }

    /**
     * Checks whether this item is a DeobfuscatePop instruction. It is a special
     * instruction for deobfuscation.
     *
     * @return True if this item is a DeobfuscatePop instruction, false
     * otherwise
     */
    @Override
    public boolean isDeobfuscatePop() {
        return definition instanceof DeobfuscatePopIns;
    }

    /**
     * Clone the instruction.
     *
     * @return Cloned instruction
     */
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

    /**
     * Gets the line in the high level source code.
     *
     * @return Line
     */
    @Override
    public int getLine() {
        return line;
    }

    /**
     * Gets the high level source code file name.
     *
     * @return File name
     */
    @Override
    public String getFile() {
        return file;
    }

    /**
     * Set operand value the right way - update offsets necessarily. Because
     * some operand types are variable length (like U30).
     *
     * @param operandIndex Index of operand
     * @param newValue New value
     * @param code Code
     * @param body Method body
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
     * Set operand values the right way - update offsets necessarily. Because
     * some operand types are variable length (like U30).
     *
     * @param operands Operands
     * @param code Code
     * @param body Method body
     */
    public void setOperands(int[] operands, AVM2Code code, MethodBody body) {
        int oldByteCount = getBytesLength();
        this.operands = operands;
        int newByteCount = getBytesLength();
        int byteDelta = newByteCount - oldByteCount;
        if (byteDelta != 0) {
            code.updateInstructionByteCountByAddr(address, byteDelta, body);
        }
        body.setModified();
    }

    /**
     * Gets virtual address. A virtual address can be used for storing original
     * address before applying deobfuscation.
     *
     * @return Virtual address
     */
    @Override
    public long getVirtualAddress() {
        return virtualAddress;
    }

    /**
     * Sets virtual address. A virtual address can be used for storing original
     * address before applying deobfuscation.
     *
     * @param virtualAddress Virtual address
     */
    @Override
    public void setVirtualAddress(long virtualAddress) {
        this.virtualAddress = virtualAddress;
    }
}
