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
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
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
     * @param maxTempIndex Max temp index
     * @param usedDeobfuscations Used deobfuscations
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
    public void translate(Reference<Integer> maxTempIndex, Set<String> usedDeobfuscations, int swfVersion, Set<GraphPart> switchParts, List<MethodBody> callStack, AbcIndexing abcIndex, Map<Integer, Set<Integer>> setLocalPosToGetLocalPos, Reference<GraphSourceItem> lineStartItem, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, ScopeStack localScopeStack, AVM2Instruction ins, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, HashMap<Integer, GraphTargetItem> localRegTypes, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, AVM2Code code, boolean thisHasDefaultToPrimitive, LinkedIdentityHashSet<SetLocalAVM2Item> bottomSetLocals) throws InterruptedException {
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
        localData.usedDeobfuscations = usedDeobfuscations;
        localData.maxTempIndex = maxTempIndex;
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
    protected FullMultinameAVM2Item resolveMultiname(AVM2LocalData localData, boolean property, TranslateStack stack, AVM2ConstantPool constants, int multinameIndex, AVM2Instruction ins, List<GraphTargetItem> output) {
        stack.allowSwap(output);
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

        return new FullMultinameAVM2Item(property, ins, localData.lineStartInstruction, multinameIndex, localData.abc.constants.getMultiname(multinameIndex).getName(localData.usedDeobfuscations, localData.abc, localData.getConstants(), new ArrayList<>(), true, true), name, ns);
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
        if (obj instanceof CommaExpressionItem) {
            CommaExpressionItem ce = (CommaExpressionItem) obj;
            if (!ce.commands.isEmpty()) {
                obj = ce.commands.get(ce.commands.size() - 1);
            }
        }
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
    @SuppressWarnings("unchecked")
    public void handleSetProperty(boolean init, AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        stack.allowSwap(output);
        int multinameIndex = ins.operands[0];
        GraphTargetItem value = stack.pop();
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins, output);
        GraphTargetItem obj = stack.pop();

        /*
        if ((value instanceof IncrementAVM2Item) || (value instanceof DecrementAVM2Item)) {
            boolean isIncrement = (value instanceof IncrementAVM2Item);

            boolean hasConvert = value.value instanceof ConvertAVM2Item;
            if (value.value.getNotCoercedNoDup() instanceof GetLexAVM2Item) {
                GetLexAVM2Item getLex = (GetLexAVM2Item) value.value.getNotCoercedNoDup();
                if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)
                        && (obj instanceof FindPropertyAVM2Item)) {                    
                    if (hasConvert) {
                        if (isIncrement) {
                            stack.addToOutput(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        } else {
                            stack.addToOutput(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        }
                    } else {
                        if (isIncrement) {
                            stack.addToOutput(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        } else {
                            stack.addToOutput(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getLex));
                        }
                    }
                    return;
                }
            }

            if (value.value.getNotCoercedNoDup() instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value.getNotCoercedNoDup();
                if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname)) {

                    if ((getProp.object instanceof DuplicateItem) || (getProp.object instanceof DuplicateSourceItem)) {
                        if (getProp.object.value == obj.getThroughDuplicate()) {
                            getProp.object = obj.getThroughDuplicate();
                        }
                    }
                    if (Objects.equals(getProp.object.getThroughDuplicate(), obj.getThroughDuplicate())) {
                        if (hasConvert) {
                            if (isIncrement) {
                                stack.addToOutput(new PostIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            } else {
                                stack.addToOutput(new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            }
                        } else {
                            if (isIncrement) {
                                stack.addToOutput(new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            } else {
                                stack.addToOutput(new PreDecrementAVM2Item(ins, localData.lineStartInstruction, getProp));
                            }
                        }
                        return;
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
            if (obj instanceof CommaExpressionItem) {
                CommaExpressionItem ce = (CommaExpressionItem) obj;
                if (ce.commands.size() == 2) {
                    if (ce.commands.get(0) instanceof SetLocalAVM2Item && ce.commands.get(1) instanceof LocalRegAVM2Item) {
                        SetLocalAVM2Item valueSetLocalReg = (SetLocalAVM2Item) ce.commands.get(0);
                        LocalRegAVM2Item objLocalReg = (LocalRegAVM2Item) ce.commands.get(1);    
                        if ((valueSetLocalReg.value instanceof IncrementAVM2Item)
                                || (valueSetLocalReg.value instanceof DecrementAVM2Item)) {
                            boolean isIncrement = (valueSetLocalReg.value instanceof IncrementAVM2Item);
                            if (valueSetLocalReg.value.value instanceof DuplicateItem) {
                                GraphTargetItem duplicated = valueSetLocalReg.value.value.value;
                                //if (!output.isEmpty() && output.get(output.size() - 1) instanceof PushItem && ((PushItem) output.get(output.size() - 1)).value == duplicated) {
                                if (!stack.isEmpty() && stack.peek().getThroughDuplicate() == duplicated) {
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
                                                //output.remove(output.size() - 1);
                                                //output.remove(output.size() - 1);
                                                //stack.moveToStack(output);
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
            }*/
        //TestIncDec5 no result AIR
        /*
         var _temp_5:* = a;
         _temp_5.attrib = _temp_5.attrib + 1;
         */
        if (value instanceof IncrementAVM2Item
                || value instanceof DecrementAVM2Item) {
            boolean isIncrement = value instanceof IncrementAVM2Item;
            if (value.value instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value;
                if (getProp.object instanceof DuplicateItem
                        && obj instanceof DuplicateSourceItem) {
                    if (getProp.object.getThroughDuplicate() == obj.getThroughDuplicate()) {
                        DuplicateItem d = (DuplicateItem) getProp.object;
                        if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                            SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                            if (st.tempIndex == d.tempIndex) {
                                output.remove(output.size() - 1);
                                getProp.object = st.value;
                                if (isIncrement) {
                                    stack.addToOutput(new PreIncrementAVM2Item(value.getSrc(), value.lineStartItem, getProp));
                                } else {
                                    stack.addToOutput(new PreDecrementAVM2Item(value.getSrc(), value.lineStartItem, getProp));
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }

        //TestIncDec6 no result AIR
        //In air, this is *POST* inc/decrement
        /*
        var _temp_5:* = a;
         _temp_5.attrib = _temp_5.attrib + 1;
         */
        if (value instanceof IncrementAVM2Item
                || value instanceof DecrementAVM2Item) {
            boolean isIncrement = value instanceof IncrementAVM2Item;
            if (value.value instanceof ConvertAVM2Item) {
                if (value.value.value instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value.value;
                    if (getProp.object instanceof DuplicateItem
                            && obj instanceof DuplicateSourceItem) {
                        if (getProp.object.getThroughDuplicate() == obj.getThroughDuplicate()) {
                            DuplicateItem d = (DuplicateItem) getProp.object;
                            if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                                SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                                if (st.tempIndex == d.tempIndex) {
                                    output.remove(output.size() - 1);
                                    getProp.object = st.value;
                                    if (isIncrement) {
                                        stack.addToOutput(new PostIncrementAVM2Item(value.getSrc(), value.lineStartItem, getProp));
                                    } else {
                                        stack.addToOutput(new PostDecrementAVM2Item(value.getSrc(), value.lineStartItem, getProp));
                                    }
                                    return;
                                }
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

                //TestIncDec3 with result
                /*
                //var _temp_5:* = §§findproperty(trace);
                var _loc4_:*;
                var _loc2_:*;
                var _loc3_:int;
                var _temp_4:* = _loc4_ = (_loc2_ = a)[_loc3_ = 2] + 1;
                _loc2_[_loc3_] = _loc4_;
                trace(_temp_4);
                 */
                if (!stack.isEmpty() && stack.peek() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) stack.peek();
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocValue.value.value;
                            if (getProp.object instanceof SetLocalAVM2Item) {
                                SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                    if (fm.name instanceof SetLocalAVM2Item) {
                                        SetLocalAVM2Item setLocName = (SetLocalAVM2Item) fm.name;
                                        if (valueLocalReg.regIndex == setLocValue.regIndex
                                                && objLocalReg.regIndex == setLocObj.regIndex
                                                && nameLocalReg.regIndex == setLocName.regIndex) {
                                            getProp.object = setLocObj.value;
                                            fm.name = setLocName.value;
                                            stack.pop();
                                            if (isIncrement) {
                                                stack.push(new PreIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            } else {
                                                stack.push(new PreDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //TestIncDec3 no result
                /*
                var _loc2_:* = a;
                var _loc3_:int;
                var _loc4_:* = _loc2_[_loc3_ = 2] + 1;
                _loc2_[_loc3_] = _loc4_;
                 */
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) output.get(output.size() - 1);
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocValue.value.value;
                            if (getProp.object instanceof SetLocalAVM2Item) {
                                SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                    if (fm.name instanceof SetLocalAVM2Item) {
                                        SetLocalAVM2Item setLocName = (SetLocalAVM2Item) fm.name;
                                        if (valueLocalReg.regIndex == setLocValue.regIndex
                                                && objLocalReg.regIndex == setLocObj.regIndex
                                                && nameLocalReg.regIndex == setLocName.regIndex) {
                                            getProp.object = setLocObj.value;
                                            fm.name = setLocName.value;
                                            output.remove(output.size() - 1);
                                            if (isIncrement) {
                                                stack.addToOutput(new PreIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            } else {
                                                stack.addToOutput(new PreDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //TestIncDec4 with result
                /*
                var _temp_4:* = §§findproperty(trace);
                var _loc2_:*;
                var _loc3_:int;
                var _temp_3:* = Number((_loc2_ = a)[_loc3_ = 2]);
                var _loc4_:* = _temp_3 + 1;
                _loc2_[_loc3_] = _loc4_;
                trace(_temp_3);
                 */
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) output.get(output.size() - 1);
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof DuplicateItem) {
                            DuplicateItem d = (DuplicateItem) setLocValue.value.value;
                            if (output.size() >= 2
                                    && output.get(output.size() - 2) instanceof PushItem
                                    && output.get(output.size() - 2).value instanceof DuplicateSourceItem) {
                                DuplicateSourceItem ds = (DuplicateSourceItem) output.get(output.size() - 2).value;
                                if (ds.tempIndex == d.tempIndex) {
                                    if (output.size() >= 3 && output.get(output.size() - 3) instanceof SetTemporaryItem) {
                                        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 3);
                                        if (st.tempIndex == d.tempIndex) {
                                            if (st.value instanceof ConvertAVM2Item) {
                                                if (st.value.value instanceof GetPropertyAVM2Item) {
                                                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) st.value.value;
                                                    if (getProp.object instanceof SetLocalAVM2Item) {
                                                        SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                                        if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                            FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                                            if (fm.name instanceof SetLocalAVM2Item) {
                                                                SetLocalAVM2Item setLocName = (SetLocalAVM2Item) fm.name;
                                                                if (valueLocalReg.regIndex == setLocValue.regIndex
                                                                        && objLocalReg.regIndex == setLocObj.regIndex
                                                                        && nameLocalReg.regIndex == setLocName.regIndex) {
                                                                    getProp.object = setLocObj.value;
                                                                    fm.name = setLocName.value;
                                                                    output.remove(output.size() - 1);
                                                                    output.remove(output.size() - 1);
                                                                    output.remove(output.size() - 1);
                                                                    stack.moveToStack(output);
                                                                    if (isIncrement) {
                                                                        stack.push(new PostIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                                                    } else {
                                                                        stack.push(new PostDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
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
                                }
                            }
                        }
                    }
                }

                //TestIncDec4 no result AIR
                /*
                var _loc2_:* = a;
                var _loc3_:int;
                var _loc4_:* = Number(_loc2_[_loc3_ = 2]) + 1;
                _loc2_[_loc3_] = _loc4_;
                 */
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) output.get(output.size() - 1);
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof ConvertAVM2Item) {
                            if (setLocValue.value.value.value instanceof GetPropertyAVM2Item) {
                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocValue.value.value.value;
                                if (getProp.object instanceof SetLocalAVM2Item) {
                                    SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                        if (fm.name instanceof SetLocalAVM2Item) {
                                            SetLocalAVM2Item setLocName = (SetLocalAVM2Item) fm.name;
                                            if (valueLocalReg.regIndex == setLocValue.regIndex
                                                    && objLocalReg.regIndex == setLocObj.regIndex
                                                    && nameLocalReg.regIndex == setLocName.regIndex) {
                                                getProp.object = setLocObj.value;
                                                fm.name = setLocName.value;
                                                output.remove(output.size() - 1);
                                                stack.moveToStack(output);
                                                if (isIncrement) {
                                                    stack.addToOutput(new PostIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                                } else {
                                                    stack.addToOutput(new PostDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
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

                //TestIncDec5 with result
                /*
                var _temp_4:* = §§findproperty(trace);
                var _loc3_:*;
                var _loc2_:*;
                var _temp_3:* = _loc3_ = (_loc2_ = a).attrib + 1;
                _loc2_.attrib = _loc3_;
                trace(_temp_3);
                 */
                if (!stack.isEmpty() && stack.peek() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) stack.peek();
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocValue.value.value;
                            if (getProp.object instanceof SetLocalAVM2Item) {
                                SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                    if (fm.compareSame(multiname)) {
                                        if (valueLocalReg.regIndex == setLocValue.regIndex
                                                && objLocalReg.regIndex == setLocObj.regIndex) {
                                            getProp.object = setLocObj.value;
                                            stack.pop();
                                            if (isIncrement) {
                                                stack.push(new PreIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            } else {
                                                stack.push(new PreDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //TestIncDec5 no result
                /*
                var _loc2_:* = a;
                var _loc3_:* = _loc2_.attrib + 1;
                _loc2_.attrib = _loc3_;
                 */
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) output.get(output.size() - 1);
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof GetPropertyAVM2Item) {
                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocValue.value.value;
                            if (getProp.object instanceof SetLocalAVM2Item) {
                                SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                    if (fm.compareSame(multiname)) {
                                        if (valueLocalReg.regIndex == setLocValue.regIndex
                                                && objLocalReg.regIndex == setLocObj.regIndex) {
                                            getProp.object = setLocObj.value;
                                            output.remove(output.size() - 1);
                                            stack.moveToStack(output);
                                            if (isIncrement) {
                                                stack.addToOutput(new PreIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            } else {
                                                stack.addToOutput(new PreDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //TestIncDec6 with result
                /*
                var _temp_3:* = §§findproperty(trace);
                var _loc2_:*;
                var _temp_2:* = Number((_loc2_ = a).attrib);
                var _loc3_:* = _temp_2 + 1;
                _loc2_.attrib = _loc3_;
                trace(_temp_2);
                 */
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) output.get(output.size() - 1);
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof DuplicateItem) {
                            DuplicateItem d = (DuplicateItem) setLocValue.value.value;
                            if (output.size() >= 2
                                    && output.get(output.size() - 2) instanceof PushItem
                                    && output.get(output.size() - 2).value instanceof DuplicateSourceItem) {
                                DuplicateSourceItem ds = (DuplicateSourceItem) output.get(output.size() - 2).value;
                                if (ds.tempIndex == d.tempIndex) {
                                    if (output.size() >= 3 && output.get(output.size() - 3) instanceof SetTemporaryItem) {
                                        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 3);
                                        if (st.tempIndex == d.tempIndex) {
                                            if (st.value instanceof ConvertAVM2Item) {
                                                if (st.value.value instanceof GetPropertyAVM2Item) {
                                                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) st.value.value;
                                                    if (getProp.object instanceof SetLocalAVM2Item) {
                                                        SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                                        if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                            FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                                            if (fm.compareSame(multiname)) {
                                                                if (objLocalReg.regIndex == setLocObj.regIndex
                                                                        && valueLocalReg.regIndex == setLocValue.regIndex) {
                                                                    getProp.object = setLocObj.value;
                                                                    output.remove(output.size() - 1);
                                                                    output.remove(output.size() - 1);
                                                                    output.remove(output.size() - 1);
                                                                    stack.moveToStack(output);
                                                                    if (isIncrement) {
                                                                        stack.push(new PostIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                                                    } else {
                                                                        stack.push(new PostDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
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
                                }
                            }
                        }
                    }
                }

                //TestIncDec10 with result
                /*
                var _temp_3:* = §§findproperty(trace);
                var _loc1_:*;
                var _temp_2:* = (_loc1_ = this).attrib;
                var _loc2_:* = _temp_2 + 1;
                _loc1_.attrib = _loc2_;
                trace(_temp_2);
                 */
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocValue = (SetLocalAVM2Item) output.get(output.size() - 1);
                    if (setLocValue.value instanceof IncrementAVM2Item
                            || setLocValue.value instanceof DecrementAVM2Item) {
                        boolean isIncrement = setLocValue.value instanceof IncrementAVM2Item;
                        if (setLocValue.value.value instanceof DuplicateItem) {
                            DuplicateItem d = (DuplicateItem) setLocValue.value.value;
                            if (output.size() >= 2
                                    && output.get(output.size() - 2) instanceof PushItem
                                    && output.get(output.size() - 2).value instanceof DuplicateSourceItem) {
                                DuplicateSourceItem ds = (DuplicateSourceItem) output.get(output.size() - 2).value;
                                if (ds.tempIndex == d.tempIndex) {
                                    if (output.size() >= 3 && output.get(output.size() - 3) instanceof SetTemporaryItem) {
                                        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 3);
                                        if (st.tempIndex == d.tempIndex) {
                                            if (st.value instanceof GetPropertyAVM2Item) {
                                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) st.value;
                                                if (getProp.object instanceof SetLocalAVM2Item) {
                                                    SetLocalAVM2Item setLocObj = (SetLocalAVM2Item) getProp.object;
                                                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                                        if (fm.compareSame(multiname)) {
                                                            if (objLocalReg.regIndex == setLocObj.regIndex
                                                                    && valueLocalReg.regIndex == setLocValue.regIndex) {
                                                                getProp.object = setLocObj.value;
                                                                output.remove(output.size() - 1);
                                                                output.remove(output.size() - 1);
                                                                output.remove(output.size() - 1);
                                                                stack.moveToStack(output);
                                                                if (isIncrement) {
                                                                    stack.push(new PostIncrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
                                                                } else {
                                                                    stack.push(new PostDecrementAVM2Item(setLocValue.value.getSrc(), setLocValue.value.getLineStartItem(), getProp));
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
                            }
                        }
                    }
                }

                /*stack.moveToStack(output);
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
                }*/
            }
        }

        //TestIncDec9 with result AIR
        /*
         //var _temp_3:* = trace;
         //var _temp_2:* = global;
         var _temp_1:* = attrib + 1;
         attrib = _temp_1;
         _temp_3(_temp_1);
         */
        if (value instanceof DuplicateItem) {
            if (!stack.isEmpty() && stack.peek() instanceof DuplicateSourceItem) {
                DuplicateItem d = (DuplicateItem) value;
                DuplicateSourceItem ds = (DuplicateSourceItem) stack.peek();
                if (d.tempIndex == ds.tempIndex) {
                    if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                        if (st.tempIndex == d.tempIndex) {
                            if (st.value instanceof IncrementAVM2Item
                                    || st.value instanceof DecrementAVM2Item) {
                                boolean isIncrement = st.value instanceof IncrementAVM2Item;

                                boolean ok = false;
                                //assembled.TestIncrement2
                                if (st.value.value instanceof GetLexAVM2Item) {
                                    GetLexAVM2Item getLex = (GetLexAVM2Item) st.value.value;
                                    if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)) {
                                        ok = true;
                                    }
                                } else if (st.value.value instanceof GetPropertyAVM2Item) {
                                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) st.value.value;
                                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                        if (fm.compareSame(multiname)) {
                                            ok = true;
                                        }
                                    }
                                }
                                if (ok) {
                                    output.remove(output.size() - 1);
                                    stack.pop();
                                    stack.moveToStack(output);
                                    if (isIncrement) {
                                        stack.push(new PreIncrementAVM2Item(st.value.getSrc(), st.value.getLineStartItem(), st.value.value));
                                    } else {
                                        stack.push(new PreDecrementAVM2Item(st.value.getSrc(), st.value.getLineStartItem(), st.value.value));
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        //TestIncDec9 no result AIR
        //attrib = attrib + 1;
        if (value instanceof IncrementAVM2Item
                || value instanceof DecrementAVM2Item) {
            boolean isIncrement = value instanceof IncrementAVM2Item;
            if (value.value instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value;
                if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                    if (fm.compareSame(multiname)) {
                        if (isIncrement) {
                            stack.addToOutput(new PreIncrementAVM2Item(value.getSrc(), value.getLineStartItem(), getProp));
                        } else {
                            stack.addToOutput(new PreDecrementAVM2Item(value.getSrc(), value.getLineStartItem(), getProp));
                        }
                        return;
                    }
                }
            }
        }

        //TestInc10 with result AIR
        /*
         //var _temp_4:* = trace;
         //var _temp_3:* = global;
         var _temp_1:* = attrib;
         attrib = _temp_1 + 1;
         _temp_4(_temp_1);
         */
        if (value instanceof IncrementAVM2Item
                || value instanceof DecrementAVM2Item) {
            boolean isIncrement = value instanceof IncrementAVM2Item;
            if (value.value instanceof DuplicateItem) {
                DuplicateItem d = (DuplicateItem) value.value;
                if (!output.isEmpty() && output.get(output.size() - 1) instanceof PushItem
                        && output.get(output.size() - 1).value instanceof DuplicateSourceItem) {
                    DuplicateSourceItem ds = (DuplicateSourceItem) output.get(output.size() - 1).value;
                    if (d.tempIndex == ds.tempIndex) {
                        if (output.size() >= 2 && output.get(output.size() - 2) instanceof SetTemporaryItem) {
                            SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 2);
                            if (st.tempIndex == d.tempIndex) {
                                if (st.value instanceof ConvertAVM2Item) {
                                    boolean ok = false;
                                    //assembled.TestIncrement
                                    if (st.value.value instanceof GetLexAVM2Item) {
                                        GetLexAVM2Item getLex = (GetLexAVM2Item) st.value.value;
                                        if (localData.abc.constants.getMultiname(multinameIndex).equals(getLex.propertyName)) {
                                            ok = true;
                                        }
                                    } else if (st.value.value instanceof GetPropertyAVM2Item) {
                                        GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) st.value.value;
                                        if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                            FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                                            if (fm.compareSame(multiname)) {
                                                ok = true;
                                            }
                                        }
                                    }

                                    if (ok) {
                                        output.remove(output.size() - 1);
                                        output.remove(output.size() - 1);
                                        stack.moveToStack(output);
                                        if (isIncrement) {
                                            stack.push(new PostIncrementAVM2Item(value.getSrc(), value.getLineStartItem(), st.value.value));
                                        } else {
                                            stack.push(new PostDecrementAVM2Item(value.getSrc(), value.getLineStartItem(), st.value.value));
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

        //TestInc10 no result AIR
        //attrib = attrib + 1;
        if (value instanceof IncrementAVM2Item
                || value instanceof DecrementAVM2Item) {
            boolean isIncrement = value instanceof IncrementAVM2Item;
            if (value.value instanceof ConvertAVM2Item) {
                if (value.value.value instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) value.value.value;
                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                        if (fm.compareSame(multiname)) {
                            if (isIncrement) {
                                stack.addToOutput(new PostIncrementAVM2Item(value.getSrc(), value.getLineStartItem(), getProp));
                            } else {
                                stack.addToOutput(new PostDecrementAVM2Item(value.getSrc(), value.getLineStartItem(), getProp));
                            }
                            return;
                        }
                    }
                }
            }
        }

        if (multiname.name instanceof CommaExpressionItem) {
            CommaExpressionItem ce = (CommaExpressionItem) multiname.name;
            if (ce.commands.size() == 2) {
                if (ce.commands.get(0) instanceof SetLocalAVM2Item && ce.commands.get(1) instanceof LocalRegAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) ce.commands.get(0);
                    LocalRegAVM2Item localReg = (LocalRegAVM2Item) ce.commands.get(1);
                    if (setLocal.regIndex == localReg.regIndex) {
                        GraphSourceItem src = setLocal.getSrc();
                        if (src != null) {
                            if (localData.getSetLocalUsages(localData.code.adr2pos(src.getAddress())).size() == 1) {
                                multiname.name = setLocal.value;
                            }
                        }
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
                if (obj instanceof DuplicateItem) {
                    if (!stack.isEmpty()
                            && stack.peek() instanceof DuplicateSourceItem
                            && stack.peek().getThroughDuplicate() == obj.getThroughDuplicate()) {
                        if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                            SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                            DuplicateSourceItem ds = (DuplicateSourceItem) stack.peek();
                            if (st.tempIndex == ds.tempIndex) {
                                output.remove(output.size() - 1);
                                stack.moveToStack(output);
                            }
                        }
                        stack.push(stack.pop().value);
                    }
                }
                return;
            } else if (c.object instanceof InitVectorAVM2Item) {
                InitVectorAVM2Item iv = (InitVectorAVM2Item) c.object;
                iv.arguments.add(value);
                if (obj instanceof DuplicateItem) {
                    if (!stack.isEmpty()
                            && stack.peek() instanceof DuplicateSourceItem
                            && stack.peek().getThroughDuplicate() == obj.getThroughDuplicate()) {
                        if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                            SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                            DuplicateSourceItem ds = (DuplicateSourceItem) stack.peek();
                            if (st.tempIndex == ds.tempIndex) {
                                output.remove(output.size() - 1);
                            }
                        }
                        stack.push(stack.pop().value);
                    }
                }
                return;
            }
        }

        Reference<Boolean> isStatic = new Reference<>(false);
        Reference<GraphTargetItem> type = new Reference<>(null);
        Reference<GraphTargetItem> callType = new Reference<>(null);
        GetPropertyIns.resolvePropertyType(localData, obj, multiname, isStatic, type, callType);

        //obj = obj.getThroughDuplicate();
        SetTypeAVM2Item result;
        if (init) {
            result = new InitPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, value, type.getVal(), callType.getVal(), isStatic.getVal());
        } else {
            result = new SetPropertyAVM2Item(ins, localData.lineStartInstruction, obj, multiname, value, type.getVal(), callType.getVal(), isStatic.getVal());
        }
        SetPropertyIns.handleCompound(localData, obj, multiname, value, output, stack, result);
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
                    if (getProperty.object.getThroughDuplicate() instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item objSetLocalReg = (SetLocalAVM2Item) getProperty.object.getThroughDuplicate();

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
                                //TestIncDec3 with result
                                return new PreIncrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                            } else {
                                if (hasConvert && standalone) {
                                    return new PostDecrementAVM2Item(ins, localData.lineStartInstruction, getProperty);
                                }
                                //TestIncDec3 with result
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
