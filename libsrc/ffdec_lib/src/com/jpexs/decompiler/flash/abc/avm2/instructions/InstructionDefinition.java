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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2VerifyErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GlobalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitWithSlot;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Reference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * AVM2 Instruction definition.
 *
 * @author JPEXS
 */
public abstract class InstructionDefinition implements Serializable {

    /**
     * Serial version UID
     */
    public static final long serialVersionUID = 1L;

    /**
     * Operands
     */
    public int[] operands;

    /**
     * Instruction name
     */
    public String instructionName = "";

    /**
     * Instruction code
     */
    public int instructionCode = 0;

    /**
     * Can throw exception
     */
    public boolean canThrow;

    /**
     * Flags
     */
    public AVM2InstructionFlag[] flags;

    /**
     * Constructs new instance
     *
     * @param instructionCode Instruction code
     * @param instructionName Instruction name
     * @param operands Operands
     * @param canThrow Can throw exception
     * @param flags Flags
     */
    public InstructionDefinition(int instructionCode, String instructionName, int[] operands, boolean canThrow, AVM2InstructionFlag... flags) {
        this.instructionCode = instructionCode;
        this.instructionName = instructionName;
        this.operands = operands;
        this.canThrow = canThrow;
        this.flags = flags;
    }

    /**
     * Checks if instruction has flag
     *
     * @param flag Flag
     * @return True if instruction has flag
     */
    public boolean hasFlag(AVM2InstructionFlag flag) {
        for (AVM2InstructionFlag f : flags) {
            if (f == flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * To string
     *
     * @return String representation
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(instructionName);
        for (int i = 0; i < operands.length; i++) {
            s.append(AVM2Code.operandTypeToString(operands[i], true));
        }
        return s.toString();
    }

    /**
     * Verify instruction
     *
     * @param lda Local data area
     * @param constants Constant pool
     * @param ins Instruction
     * @throws AVM2VerifyErrorException On verification error
     */
    public void verify(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2VerifyErrorException {
        for (int i = 0; i < operands.length; i++) {
            int operand = operands[i];
            if (operand == AVM2Code.DAT_MULTINAME_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getMultinameCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getMultinameCount()});
                }
            } else if (operand == AVM2Code.DAT_DOUBLE_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getDoubleCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getDoubleCount()});
                }
            } else if (operand == AVM2Code.DAT_INT_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getIntCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getIntCount()});
                }
            } else if (operand == AVM2Code.DAT_UINT_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getUIntCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getUIntCount()});
                }
            } else if (operand == AVM2Code.DAT_STRING_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getStringCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_NAMESPACE_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getNamespaceCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_FLOAT_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getFloatCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_FLOAT4_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getFloat4Count()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            } else if (operand == AVM2Code.DAT_DECIMAL_INDEX) {
                int idx = ins.operands[i];
                if (idx <= 0 || idx >= constants.getDecimalCount()) {
                    throw new AVM2VerifyErrorException(AVM2VerifyErrorException.CPOOL_INDEX_OUT_OF_RANGE, lda.isDebug(), new Object[]{idx, constants.getStringCount()});
                }
            }
        }
    }

    /**
     * Checks if instruction cannot be statically computed.
     *
     * @return True = cannot be statically computed, false = can be statically
     * computed
     */
    public boolean isNotCompileTimeSupported() {
        return false;
    }

    /**
     * Executes instruction.
     *
     * @param lda Local data area
     * @param constants Constant pool
     * @param ins Instruction
     * @return True if instruction was executed, false if not
     * @throws AVM2ExecutionException On execution error
     */
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        //throw new UnsupportedOperationException("Instruction " + instructionName + " not implemented");
        return false;
    }

    /**
     * Throws illegal opcode exception.
     *
     * @param lda Local data area
     * @param ins Instruction
     * @throws AVM2VerifyErrorException On verification error
     */
    protected void illegalOpCode(LocalDataArea lda, AVM2Instruction ins) throws AVM2VerifyErrorException {
        throw new AVM2VerifyErrorException(AVM2VerifyErrorException.ILLEGAL_OPCODE, lda.isDebug(), new Object[]{lda.methodName, instructionCode, ins.getAddress()});
    }

    /**
     * Translates instruction to high level code.
     *
     * @param localData Local data area
     * @param stack Translate stack
     * @param ins Instruction
     * @param output Output
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) throws InterruptedException {
    }

    /**
     * Translates instruction to high level code.
     *
     * @param swfVersion SWF version
     * @param switchParts Switch parts
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param setLocalPosToGetLocalPos Set local position to get local position
     * @param lineStartItem Line start item
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param localRegs Local registers
     * @param stack Translate stack
     * @param scopeStack Scope stack
     * @param localScopeStack Local scope stack
     * @param ins Instruction
     * @param output Output
     * @param body Method body
     * @param abc ABC
     * @param localRegNames Local register names
     * @param localRegTypes Local register types
     * @param fullyQualifiedNames Fully qualified names
     * @param path Path
     * @param localRegsAssignmentIps Local registers assignment IPs
     * @param ip IP
     * @param code AVM2 code
     * @param thisHasDefaultToPrimitive This has default to primitive
     * @param bottomSetLocals Bottom set locals
     * @throws InterruptedException On interrupt
     */
    public void translate(int swfVersion, Set<GraphPart> switchParts, List<MethodBody> callStack, AbcIndexing abcIndex, Map<Integer, Set<Integer>> setLocalPosToGetLocalPos, Reference<GraphSourceItem> lineStartItem, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, ScopeStack localScopeStack, AVM2Instruction ins, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, HashMap<Integer, GraphTargetItem> localRegTypes, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, AVM2Code code, boolean thisHasDefaultToPrimitive, LinkedIdentityHashSet<SetLocalAVM2Item> bottomSetLocals) throws InterruptedException {
        AVM2LocalData localData = new AVM2LocalData();
        localData.allSwitchParts = switchParts;
        localData.isStatic = isStatic;
        localData.scriptIndex = scriptIndex;
        localData.classIndex = classIndex;
        localData.lineStartInstruction = lineStartItem.getVal();
        localData.localRegs = localRegs;
        localData.scopeStack = scopeStack;
        localData.localScopeStack = localScopeStack;
        localData.methodBody = body;
        localData.callStack = callStack;
        localData.abc = abc;
        localData.abcIndex = abcIndex;
        localData.localRegNames = localRegNames;
        localData.localRegTypes = localRegTypes;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        localData.localRegAssignmentIps = localRegsAssignmentIps;
        localData.ip = ip;
        localData.code = code;
        localData.thisHasDefaultToPrimitive = thisHasDefaultToPrimitive;
        localData.setLocalPosToGetLocalPos = setLocalPosToGetLocalPos;
        localData.bottomSetLocals = bottomSetLocals;
        localData.swfVersion = swfVersion;
        translate(localData, stack, ins, output, path);
        lineStartItem.setVal(localData.lineStartInstruction);
    }

    /**
     * Gets number of pops from stack.
     *
     * @param ins Instruction
     * @param abc ABC
     * @return Number of pops from stack
     */
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    /**
     * Gets number of pushes to stack.
     *
     * @param ins Instruction
     * @param abc ABC
     * @return Number of pushes to stack
     */
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    /**
     * Resolves multiname.
     *
     * @param localData Local data area
     * @param constants Constant pool
     * @param multinameIndex Multiname index
     */
    protected void resolveMultiname(LocalDataArea localData, AVM2ConstantPool constants, int multinameIndex) {
        if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
            Multiname multiname = constants.getMultiname(multinameIndex);
            if (multiname.needsName()) {
                Object name = localData.operandStack.pop();
            }
            if (multiname.needsNs()) {
                Object ns = localData.operandStack.pop();
            }
        }
    }

    /**
     * Resolves multiname.
     *
     * @param localData Local data area
     * @param property Property
     * @param stack Translate stack
     * @param constants Constant pool
     * @param multinameIndex Multiname index
     * @param ins Instruction
     * @return Resolved multiname
     */
    protected FullMultinameAVM2Item resolveMultiname(AVM2LocalData localData, boolean property, TranslateStack stack, AVM2ConstantPool constants, int multinameIndex, AVM2Instruction ins) {
        GraphTargetItem ns = null;
        GraphTargetItem name = null;
        if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
            Multiname multiname = constants.getMultiname(multinameIndex);
            if (multiname.needsName()) {
                name = stack.pop();
            }
            if (multiname.needsNs()) {
                ns = stack.pop();
            }
        }

        return new FullMultinameAVM2Item(property, ins, localData.lineStartInstruction, multinameIndex, localData.abc.constants.getMultiname(multinameIndex).getName(localData.getConstants(), new ArrayList<>(), true, true), name, ns);
    }

    /**
     * Gets required stack size for multiname.
     *
     * @param constants Constant pool
     * @param multinameIndex Multiname index
     * @return Required stack size
     */
    protected int getMultinameRequiredStackSize(AVM2ConstantPool constants, int multinameIndex) {
        int res = 0;
        if (multinameIndex > 0 && multinameIndex < constants.getMultinameCount()) {
            //Note: In official compiler, the stack can be wrong(greater) for some MULTINAMEL/A, e.g. increments
            /*
             var arr=[1,2,3];
             return arr[2]++;
             */
            if (constants.getMultiname(multinameIndex).needsName()) {
                res++;
            }
            if (constants.getMultiname(multinameIndex).needsNs()) {
                res++;
            }
        }

        return res;
    }

    /**
     * Gets stack delta. Stack push count - stack pop count.
     *
     * @param ins Instruction
     * @param abc ABC
     * @return Stack delta
     */
    public int getStackDelta(AVM2Instruction ins, ABC abc) {
        return getStackPushCount(ins, abc) - getStackPopCount(ins, abc);
    }

    /**
     * Gets scope stack delta. Scope stack push count - scope stack pop count.
     *
     * @param ins Instruction
     * @param abc ABC
     * @return Scope stack delta
     */
    public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
        return 0;
    }

    /**
     * Checks if instruction is exit instruction. (e.g. return, throw)
     *
     * @return True if instruction is exit instruction
     */
    public boolean isExitInstruction() {
        return false;
    }

    /**
     * Gets item IP.
     *
     * @param localData Local data
     * @param item Item
     * @return Item IP or -1 if not found
     */
    public static int getItemIp(AVM2LocalData localData, GraphTargetItem item) {
        GraphSourceItem src = item.getSrc();
        if (src == null) {
            return -1;
        }
        return localData.code.adr2pos(src.getAddress());
    }

    /**
     * Searches for slot name.
     *
     * @param slotIndex Slot index
     * @param localData Local data
     * @param obj Object
     * @param realObj Real object
     * @return Slot multiname or null if not found
     */
    protected static Multiname searchSlotName(int slotIndex, AVM2LocalData localData, GraphTargetItem obj, Reference<GraphTargetItem> realObj) {
        return searchSlotName(slotIndex, localData, obj, -1, realObj);
    }

    /**
     * Searches for slot name.
     *
     * @param slotIndex Slot index
     * @param localData Local data
     * @param obj Object
     * @param multiNameIndex Multiname index
     * @param realObj Real object
     * @return Slot multiname or null if not found
     */
    private static Multiname searchSlotName(int slotIndex, AVM2LocalData localData, GraphTargetItem obj, int multiNameIndex, Reference<GraphTargetItem> realObj) {
        if ((obj instanceof ExceptionAVM2Item) && (multiNameIndex == -1 || ((ExceptionAVM2Item) obj).exception.name_index == multiNameIndex)) {
            return localData.getConstants().getMultiname(((ExceptionAVM2Item) obj).exception.name_index);
        }

        if (obj instanceof FindPropertyAVM2Item) {
            FindPropertyAVM2Item findProp = (FindPropertyAVM2Item) obj;

            for (GraphTargetItem item : localData.localScopeStack) {
                Multiname ret = searchSlotName(slotIndex, localData, item, ((FullMultinameAVM2Item) findProp.propertyName).multinameIndex, realObj);
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        }

        Traits traits = null;
        if (obj instanceof NewActivationAVM2Item) {
            traits = localData.methodBody.traits;
        } else if (obj instanceof ThisAVM2Item && localData.classIndex > -1) {            
            traits = localData.abc.instance_info.get(localData.classIndex).instance_traits;
        } else if (obj instanceof ClassAVM2Item && localData.classIndex > -1) {
            traits = localData.abc.class_info.get(localData.classIndex).static_traits;
        } else if (obj instanceof GlobalAVM2Item) {
            traits = localData.abc.script_info.get(localData.scriptIndex).traits;
        }
        if (traits != null) {
            for (int t = 0; t < traits.traits.size(); t++) {
                Trait trait = traits.traits.get(t);
                if (trait instanceof TraitWithSlot) {
                    if (multiNameIndex == -1 || trait.name_index == multiNameIndex) {
                        if (((TraitWithSlot) trait).getSlotIndex() == slotIndex) {
                            realObj.setVal(obj);
                            return trait.getName(localData.abc);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Handles set property.
     *
     * @param init Init
     * @param localData Local data
     * @param stack Translate stack
     * @param ins Instruction
     * @param output Output
     * @param path Path
     */
    public void handleSetProperty(boolean init, AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        GraphTargetItem value = stack.pop();
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);
        GraphTargetItem obj = stack.pop();
        //assembled/TestIncrement
        if ((value instanceof IncrementAVM2Item) || (value instanceof DecrementAVM2Item)) {
            boolean isIncrement = (value instanceof IncrementAVM2Item);
            if (value.value instanceof DuplicateItem) {
                GraphTargetItem duplicated = value.value.value;
                if (!stack.isEmpty()) {
                    if (stack.peek() == duplicated) {
                        GraphTargetItem notCoerced = duplicated.getNotCoerced();
                        if (notCoerced instanceof GetLexAVM2Item) {
                            GetLexAVM2Item getLex = (GetLexAVM2Item) notCoerced;
                            if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)
                                    && (obj instanceof FindPropertyAVM2Item)) {
                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                } else {
                                    stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                }
                                return;
                            }
                        }
                        if (notCoerced instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) notCoerced;
                            if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)) {

                                if (getProp.object instanceof DuplicateItem) { //assembled/TestIncrement3
                                    if (getProp.object.value == obj) {
                                        getProp.object = obj;
                                    }
                                }

                                if (Objects.equals(obj, getProp.object)) {
                                    stack.pop();
                                    if (isIncrement) {
                                        stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                    } else {
                                        stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        if ((value instanceof IncrementAVM2Item) || (value instanceof DecrementAVM2Item)) {
            boolean isIncrement = (value instanceof IncrementAVM2Item);

            boolean hasConvert = value.value instanceof ConvertAVM2Item;
            if (value.value.getNotCoercedNoDup() instanceof GetLexAVM2Item) {
                GetLexAVM2Item getLex = (GetLexAVM2Item) value.value.getNotCoercedNoDup();
                if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)
                        && (obj instanceof FindPropertyAVM2Item)) {
                    if (hasConvert) {
                        if (isIncrement) {
                            output.add(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        } else {
                            output.add(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        }
                    } else {
                        if (isIncrement) {
                            output.add(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        } else {
                            output.add(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        }
                    }
                    return;
                }
            }

            if (value.value.getNotCoercedNoDup() instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value.getNotCoercedNoDup();
                if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)) {

                    if (getProp.object instanceof DuplicateItem) {
                        if (getProp.object.value == obj) {
                            getProp.object = obj;
                        }
                    }
                    if (Objects.equals(getProp.object, obj)) {
                        if (hasConvert) {
                            if (isIncrement) {
                                output.add(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            } else {
                                output.add(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            }
                        } else {
                            if (isIncrement) {
                                output.add(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            } else {
                                output.add(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            }
                        }
                        return;
                    }
                }
            }
        }
        //assembled/TestIncrement2
        if (value instanceof DuplicateItem) {
            GraphTargetItem duplicated = value.value;
            if ((duplicated instanceof IncrementAVM2Item) || (duplicated instanceof DecrementAVM2Item)) {
                boolean isIncrement = (duplicated instanceof IncrementAVM2Item);
                if (!stack.isEmpty()) {
                    if (stack.peek() == duplicated) {
                        GraphTargetItem incrementedProp = duplicated.value;
                        if (incrementedProp instanceof GetLexAVM2Item) {
                            GetLexAVM2Item getLex = (GetLexAVM2Item) incrementedProp;
                            if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)
                                    && (obj instanceof FindPropertyAVM2Item)) {
                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                } else {
                                    stack.push(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                                }
                                return;
                            }
                        }
                        if (incrementedProp instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) incrementedProp;
                            if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)
                                    && (Objects.equals(getProp.object, obj))) {
                                stack.pop();
                                if (isIncrement) {
                                    stack.push(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                } else {
                                    stack.push(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (value instanceof LocalRegAVM2Item) {
            LocalRegAVM2Item valueLocalReg = (LocalRegAVM2Item) value;
            LocalRegAVM2Item nameLocalReg = null;
            if (multiname.name instanceof LocalRegAVM2Item) {
                nameLocalReg = (LocalRegAVM2Item) multiname.name;
            }
            if (obj instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item objLocalReg = (LocalRegAVM2Item) obj;

                if (!output.isEmpty()) {
                    if (output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item valueSetLocalReg = (SetLocalAVM2Item) output.get(output.size() - 1);
                        if ((valueSetLocalReg.value instanceof IncrementAVM2Item)
                                || (valueSetLocalReg.value instanceof DecrementAVM2Item)) {
                            boolean isIncrement = (valueSetLocalReg.value instanceof IncrementAVM2Item);
                            if (valueSetLocalReg.value.value instanceof DuplicateItem) {
                                GraphTargetItem duplicated = valueSetLocalReg.value.value.value;
                                if (!stack.isEmpty() && stack.peek() == duplicated) {
                                    GraphTargetItem notCoerced = duplicated.getNotCoerced();
                                    if (notCoerced instanceof GetPropertyAVM2Item) {
                                        GetPropertyAVM2Item getProperty = (GetPropertyAVM2Item) notCoerced;
                                        FullMultinameAVM2Item propertyName = ((FullMultinameAVM2Item) getProperty.propertyName);
                                        SetLocalAVM2Item nameSetLocalReg = null;
                                        if (propertyName.name instanceof SetLocalAVM2Item) {
                                            nameSetLocalReg = (SetLocalAVM2Item) propertyName.name;
                                        }
                                        if (getProperty.object instanceof SetLocalAVM2Item) {
                                            SetLocalAVM2Item objSetLocalReg = (SetLocalAVM2Item) getProperty.object;
                                            
                                            if ((valueLocalReg.regIndex == valueSetLocalReg.regIndex)
                                                    && (propertyName.multinameIndex == multinameIndex)
                                                    && ((nameLocalReg == null && nameSetLocalReg == null) || (nameLocalReg != null && nameSetLocalReg != null && nameLocalReg.regIndex == nameSetLocalReg.regIndex))
                                                    && (objLocalReg.regIndex == objSetLocalReg.regIndex)) {
                                                if (nameSetLocalReg != null) {
                                                    propertyName.name = nameSetLocalReg.value;
                                                }
                                                getProperty.object = objSetLocalReg.value;
                                                output.remove(output.size() - 1);
                                                stack.pop();
                                                if (isIncrement) {
                                                    stack.push(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty));
                                                } else {
                                                    stack.push(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty));
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!stack.isEmpty()) {
                    GraphTargetItem checked = checkIncDec(false, multinameIndex, ins, localData, stack.peek(), valueLocalReg, nameLocalReg, objLocalReg);
                    if (checked != null) {
                        stack.pop();
                        stack.push(checked);
                        return;
                    }
                }
                if (!output.isEmpty()) {
                    GraphTargetItem checked = checkIncDec(true, multinameIndex, ins, localData, output.get(output.size() - 1), valueLocalReg, nameLocalReg, objLocalReg);
                    if (checked != null) {
                        output.remove(output.size() - 1);
                        output.add(checked);
                        return;
                    }
                }
            }
        }

        if (obj.getThroughDuplicate() instanceof ConstructAVM2Item) {
            ConstructAVM2Item c = (ConstructAVM2Item) obj.getThroughDuplicate();
            if (c.object instanceof ApplyTypeAVM2Item) {
                ApplyTypeAVM2Item at = (ApplyTypeAVM2Item) c.object;
                c.args.clear();
                List<GraphTargetItem> vals = new ArrayList<>();
                vals.add(value);
                c.object = new InitVectorAVM2Item(c.getInstruction(), c.getLineStartIns(), at.params.get(0), vals);
                return;
            } else if (c.object instanceof InitVectorAVM2Item) {
                InitVectorAVM2Item iv = (InitVectorAVM2Item) c.object;
                iv.arguments.add(value);
                return;
            }
        }

        Reference<Boolean> isStatic = new Reference<>(false);
        Reference<GraphTargetItem> type = new Reference<>(null);
        Reference<GraphTargetItem> callType = new Reference<>(null);
        GetPropertyIns.resolvePropertyType(localData, obj, multiname, isStatic, type, callType);

        SetTypeAVM2Item result;
        if (init) {
            result = new InitPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, value, type.getVal(), callType.getVal(), isStatic.getVal());
        } else {
            result = new SetPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, value, type.getVal(), callType.getVal(), isStatic.getVal());
        }
        SetPropertyIns.handleCompound(localData, obj, multiname, value, output, result);
        SetTypeIns.handleResult(value, stack, output, localData, (GraphTargetItem) result, -1, type.getVal());
    }

    /**
     * Checks if increment or decrement.
     *
     * @param standalone Standalone
     * @param multinameIndex Multiname index
     * @param ins Instruction
     * @param localData Local data
     * @param item Item
     * @param valueLocalReg Value local register
     * @param nameLocalReg Name local register
     * @param objLocalReg Object local register
     * @return Increment or decrement item or null if not found
     */
    private GraphTargetItem checkIncDec(boolean standalone, int multinameIndex, AVM2Instruction ins, AVM2LocalData localData, GraphTargetItem item,
            LocalRegAVM2Item valueLocalReg, LocalRegAVM2Item nameLocalReg, LocalRegAVM2Item objLocalReg) {
        if (item instanceof SetLocalAVM2Item) {
            SetLocalAVM2Item valueSetLocalReg = (SetLocalAVM2Item) item;
            if ((valueSetLocalReg.value instanceof IncrementAVM2Item) || (valueSetLocalReg.value instanceof DecrementAVM2Item)) {
                boolean isIncrement = (valueSetLocalReg.value instanceof IncrementAVM2Item);
                boolean hasConvert = valueSetLocalReg.value.value instanceof ConvertAVM2Item; //in air there is convert added when postincrement

                if (valueSetLocalReg.value.value.getNotCoercedNoDup() instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item getProperty = (GetPropertyAVM2Item) valueSetLocalReg.value.value.getNotCoercedNoDup();
                    FullMultinameAVM2Item propertyName = ((FullMultinameAVM2Item) getProperty.propertyName);
                    SetLocalAVM2Item nameSetLocalReg = null;
                    if (propertyName.name instanceof SetLocalAVM2Item) {
                        nameSetLocalReg = (SetLocalAVM2Item) propertyName.name;
                    }
                    if (getProperty.object instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item objSetLocalReg = (SetLocalAVM2Item) getProperty.object;

                        if ((valueLocalReg.regIndex == valueSetLocalReg.regIndex)
                                && (propertyName.multinameIndex == multinameIndex)
                                && ((nameLocalReg == null && nameSetLocalReg == null) || (nameLocalReg != null && nameSetLocalReg != null && nameLocalReg.regIndex == nameSetLocalReg.regIndex))
                                && (objLocalReg.regIndex == objSetLocalReg.regIndex)) {
                            if (nameSetLocalReg != null) {
                                propertyName.name = nameSetLocalReg.value;
                            }
                            getProperty.object = objSetLocalReg.value;

                            if (isIncrement) {
                                if (hasConvert && standalone) {
                                    return new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                                }
                                return new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                            } else {
                                if (hasConvert && standalone) {
                                    return new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                                }
                                return new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
