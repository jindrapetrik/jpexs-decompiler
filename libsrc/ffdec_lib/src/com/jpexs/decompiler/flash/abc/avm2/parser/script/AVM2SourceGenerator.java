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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecimalValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DoubleValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.Float4ValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetDescendantsAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.IfCondition;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing.ClassIndex;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AVM2 bytecode generator.
 *
 * @author JPEXS
 */
public class AVM2SourceGenerator implements SourceGenerator {

    /**
     * ABC indexing
     */
    public final AbcIndexing abcIndex;

    /**
     * Mark - exception start
     */
    public static final int MARK_E_START = 0;

    /**
     * Mark - exception end
     */
    public static final int MARK_E_END = 1;

    /**
     * Mark - exception target
     */
    public static final int MARK_E_TARGET = 2;

    /**
     * Mark - exception finally part
     */
    public static final int MARK_E_FINALLYPART = 3;

    private static int currentFinId = 1;

    private static int finId() {
        return currentFinId++;
    }

    private long uniqLast = 0;

    /**
     * Generate a new unique id
     * @return Unique id
     */
    public String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    /**
     * Resolve type.
     * @param localData Local data
     * @param item Item
     * @param abcIndex ABC indexing
     * @return Type index
     * @throws CompilationException On compilation error
     */
    public static int resolveType(SourceGeneratorLocalData localData, GraphTargetItem item, AbcIndexing abcIndex) throws CompilationException {
        int name_index = 0;
        GraphTargetItem typeItem = null;

        if (item instanceof UnresolvedAVM2Item) {
            String fullClass = localData.getFullClass();
            item = ((UnresolvedAVM2Item) item).resolve(localData, fullClass, new TypeItem(fullClass), new ArrayList<>(), new ArrayList<>(), abcIndex, new ArrayList<>(), new ArrayList<>());
        }
        if (item instanceof TypeItem) {
            typeItem = item;
        } else if (item instanceof ApplyTypeAVM2Item) {
            typeItem = ((ApplyTypeAVM2Item) item).object;
        } else if (item instanceof ImportedSlotConstItem) {
            typeItem = ((ImportedSlotConstItem) item).type;
        } else {
            throw new CompilationException("Invalid type:" + item + " (" + item.getClass().getName() + ")", 0/*??*/);
        }
        if (typeItem instanceof UnresolvedAVM2Item) {
            String fullClass = localData.getFullClass();
            typeItem = ((UnresolvedAVM2Item) typeItem).resolve(localData, fullClass, new TypeItem(fullClass), new ArrayList<>(), new ArrayList<>(), abcIndex, new ArrayList<>(), new ArrayList<>());
        }

        if (!(typeItem instanceof TypeItem)) {
            throw new CompilationException("Invalid type", 0/*??*/);
        }

        TypeItem type = (TypeItem) typeItem;

        DottedChain dname = type.fullTypeName;
        DottedChain pkg = dname.getWithoutLast();
        String name = dname.getLast();
        /*for (InstanceInfo ii : abc.getSelectedAbc().instance_info) {
         Multiname mname = abc.getSelectedAbc().constants.constant_multiname.get(ii.name_index);
         if (mname != null && baseName.equals(mname.getName(abc.getSelectedAbc().constants, null, true))) {
         Namespace ns = mname.getNamespace(abc.getSelectedAbc().constants);
         if (ns != null && ns.hasName(pkg, abc.getSelectedAbc().constants)) {
         name_index = ii.name_index;
         break;
         }
         }
         }*/
                
        ABC abc = abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abc.constants;
        AbcIndexing.ClassIndex ci = abcIndex.findClass(new TypeItem(dname), abc, localData != null ? localData.scriptIndex : null);
        if (ci != null) {
            Multiname m = ci.abc.instance_info.get(ci.index).getName(ci.abc.constants);
            if (m != null) {
                Namespace ns = ci.abc.instance_info.get(ci.index).getName(ci.abc.constants).getNamespace(ci.abc.constants);
                String n = m.getName(ci.abc.constants, new ArrayList<>(), true, true /*FIXME!!*/);
                String nsn = ns == null ? null : ns.getRawName(ci.abc.constants);
                name_index = constants.getQnameId(
                        n,
                        ns == null ? Namespace.KIND_PACKAGE : ns.kind,
                        nsn, true);
            }
        }

        /*String pkgRaw = pkg.toRawString();
        for (int i = 1; i < constants.getMultinameCount(); i++) {
            Multiname mname = constants.getMultiname(i);
            if (mname != null && name.equals(mname.getName(constants, null, true, true FIXME!!))) {
                if (mname.getNamespace(constants) != null && Objects.equals(pkgRaw, mname.getNamespace(constants).getRawName(constants))) {
                    name_index = i;
                    break;
                }
            }
        }*/
        if (name_index == 0) {
            if (pkg.isEmpty() && localData.currentScript != null /*FIXME!*/) {
                for (Trait t : localData.currentScript.traits.traits) {
                    if (t.getName(abc).getName(constants, null, true, true /*FIXME!!*/).equals(name)) {
                        name_index = t.name_index;
                        break;
                    }
                }
            }
            if (name_index == 0) {
                name_index = constants.getMultinameId(Multiname.createQName(false, constants.getStringId(name, true), constants.getNamespaceId(Namespace.KIND_PACKAGE, pkg, 0, true)), true);
            }
        }

        if (item instanceof ApplyTypeAVM2Item) {
            ApplyTypeAVM2Item atype = (ApplyTypeAVM2Item) item;
            int[] params = new int[atype.params.size()];
            int i = 0;
            for (GraphTargetItem s : atype.params) {
                params[i++] = (s instanceof NullAVM2Item) ? 0 : resolveType(localData, s, abcIndex);
            }
            return constants.getMultinameId(Multiname.createTypeName(name_index, params), true);
        }

        return name_index;
    }

    @Override
    public List<GraphSourceItem> generateDiscardValue(SourceGeneratorLocalData localData, GraphTargetItem item) throws CompilationException {
        List<GraphSourceItem> ret = item.toSource(localData, this);
        ret.add(ins(AVM2Instructions.Pop));
        return ret;
    }

    private AVM2Instruction ins(int instructionCode, int... operands) {
        return new AVM2Instruction(0, instructionCode, operands);
    }

    private AVM2Instruction ins(InstructionDefinition def, int... operands) {
        return new AVM2Instruction(0, def, operands);
    }

    private byte[] insToBytes(List<AVM2Instruction> code) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (AVM2Instruction instruction : code) {

                baos.write(instruction.getBytes());

            }
            return baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(AVM2SourceGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return SWFInputStream.BYTE_ARRAY_EMPTY;
    }

    /**
     * Converts list of source items to list of instructions
     * @param items List of source items
     * @return List of instructions
     */
    public ArrayList<AVM2Instruction> toInsList(List<GraphSourceItem> items) {
        ArrayList<AVM2Instruction> ret = new ArrayList<>();
        for (GraphSourceItem s : items) {
            if (s instanceof AVM2Instruction) {
                ret.add((AVM2Instruction) s);
            }
        }
        return ret;
    }

    private List<AVM2Instruction> nonempty(List<AVM2Instruction> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private List<GraphSourceItem> condition(SourceGeneratorLocalData localData, GraphTargetItem t, int offset) throws CompilationException {
        if (t instanceof IfCondition) {
            IfCondition ic = (IfCondition) t;
            return GraphTargetItem.toSourceMerge(localData, this, ic.getLeftSide(), ic.getRightSide(), ins(ic.getIfDefinition(), offset));
        }
        return GraphTargetItem.toSourceMerge(localData, this, t, ins(AVM2Instructions.IfTrue, offset));
    }

    private List<GraphSourceItem> notCondition(SourceGeneratorLocalData localData, GraphTargetItem t, int offset) throws CompilationException {
        if (t instanceof IfCondition) {
            IfCondition ic = (IfCondition) t;
            return GraphTargetItem.toSourceMerge(localData, this, ic.getLeftSide(), ic.getRightSide(), ins(ic.getIfNotDefinition(), offset));
        }
        return GraphTargetItem.toSourceMerge(localData, this, t, ins(AVM2Instructions.IfFalse, offset));
    }

    private List<GraphSourceItem> generateIf(SourceGeneratorLocalData localData, GraphTargetItem expression, List<GraphTargetItem> onTrueCmds, List<GraphTargetItem> onFalseCmds, boolean ternar) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        //ret.addAll(notCondition(localData, expression));
        List<AVM2Instruction> onTrue;
        List<AVM2Instruction> onFalse = null;
        if (ternar) {
            onTrue = toInsList(onTrueCmds.get(0).toSource(localData, this));
        } else {
            onTrue = generateToInsList(localData, onTrueCmds);
        }

        if (onFalseCmds != null && !onFalseCmds.isEmpty()) {
            if (ternar) {
                onFalse = toInsList(onFalseCmds.get(0).toSource(localData, this));
            } else {
                onFalse = generateToInsList(localData, onFalseCmds);
            }
        }
        AVM2Instruction ajmp = null;
        if (onFalse != null) {
            if (onTrueCmds.isEmpty() || 
                    !((onTrueCmds.get(onTrueCmds.size() - 1) instanceof ContinueItem)
                    || (onTrueCmds.get(onTrueCmds.size() - 1) instanceof BreakItem))
                    ) {
                ajmp = ins(AVM2Instructions.Jump, 0);
                onTrue.add(ajmp);
            }
        }

        byte[] onTrueBytes = insToBytes(onTrue);
        int onTrueLen = onTrueBytes.length;

        ret.addAll(notCondition(localData, expression, onTrueLen));
        ret.addAll(onTrue);

        if (onFalse != null) {
            byte[] onFalseBytes = insToBytes(onFalse);
            int onFalseLen = onFalseBytes.length;
            if (ajmp != null) {
                ajmp.operands[0] = onFalseLen;
            }
            ret.addAll(onFalse);
        }
        return ret;
    }

    private void fixSwitch(List<AVM2Instruction> code, int breakOffset, long loopId) {
        fixLoop(code, breakOffset, Integer.MAX_VALUE, loopId);
    }

    private void fixLoop(List<AVM2Instruction> code, int breakOffset, int continueOffset, long loopId) {
        int pos = 0;
        for (int a = 0; a < code.size(); a++) {
            AVM2Instruction ins = code.get(a);
            pos += ins.getBytesLength();
            if (ins.definition instanceof JumpIns) {
                if (ins.definition instanceof ContinueJumpIns) {
                    ContinueJumpIns cji = (ContinueJumpIns) ins.definition;
                    if (cji.getLoopId() == loopId) {
                        if (continueOffset != Integer.MAX_VALUE) {
                            ins.operands[0] = (-pos + continueOffset);
                            ins.definition = AVM2Code.instructionSet[AVM2Instructions.Jump];
                        }
                    }
                }
                if (ins.definition instanceof BreakJumpIns) {
                    BreakJumpIns bji = (BreakJumpIns) ins.definition;
                    if (bji.getLoopId() == loopId) {
                        ins.operands[0] = (-pos + breakOffset);
                        ins.definition = AVM2Code.instructionSet[AVM2Instructions.Jump];
                    }
                }
            }
        }
    }

    /**
     * Generate for (each) in loop.
     * @param localData Local data
     * @param loop Loop
     * @param collection Collection
     * @param assignable Assignable
     * @param commands Commands
     * @param each Each
     * @return List of source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generateForIn(SourceGeneratorLocalData localData, Loop loop, GraphTargetItem collection, AssignableAVM2Item assignable, List<GraphTargetItem> commands, final boolean each) throws CompilationException {
        localData.openedLoops.add(loop.id);
        List<GraphSourceItem> ret = new ArrayList<>();
        final Reference<Integer> counterReg = new Reference<>(0);
        final Reference<Integer> collectionReg = new Reference<>(0);

        if (assignable instanceof UnresolvedAVM2Item) {
            assignable = (AssignableAVM2Item) ((UnresolvedAVM2Item) assignable).resolved;
        }

        ret.addAll(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.PushByte, 0),
                AssignableAVM2Item.setTemp(localData, this, counterReg),
                collection,
                NameAVM2Item.generateCoerce(localData, this, TypeItem.UNBOUNDED),
                AssignableAVM2Item.setTemp(localData, this, collectionReg)
        ));

        GraphTargetItem assigned = new GraphTargetItem() {
            @Override
            public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
                return null;
            }

            @Override
            public boolean hasReturnValue() {
                return true;
            }

            @Override
            public GraphTargetItem returnType() {
                return TypeItem.UNBOUNDED;
            }

            @Override
            public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
                return toSourceMerge(localData, generator,
                        AssignableAVM2Item.getTemp(localData, generator, collectionReg),
                        AssignableAVM2Item.getTemp(localData, generator, counterReg),
                        ins(each ? AVM2Instructions.NextValue : AVM2Instructions.NextName)
                );
            }
        };
        assignable.setAssignedValue(assigned);

        List<AVM2Instruction> forBody = toInsList(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.Label),
                assignable.toSourceIgnoreReturnValue(localData, this)
        ));

        forBody.addAll(generateToInsList(localData, commands));
        int forBodyLen = insToBytes(forBody).length;

        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, forBodyLen);
        ret.add(forwardJump);

        List<AVM2Instruction> expr = new ArrayList<>();
        expr.add(ins(AVM2Instructions.HasNext2, collectionReg.getVal(), counterReg.getVal()));
        AVM2Instruction backIf = ins(AVM2Instructions.IfTrue, 0);
        expr.add(backIf);

        int exprLen = insToBytes(expr).length;
        backIf.operands[0] = -(exprLen + forBodyLen);

        fixLoop(forBody, forBodyLen + exprLen, forBodyLen, loop.id);
        ret.addAll(forBody);
        ret.addAll(expr);
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(collectionReg, counterReg)));
        return ret;
    }

    private List<AVM2Instruction> generateToInsList(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException {
        return toInsList(generate(localData, commands));
    }

    private List<AVM2Instruction> generateToActionList(SourceGeneratorLocalData localData, GraphTargetItem command) throws CompilationException {
        return toInsList(command.toSource(localData, this));
    }

    /**
     * Gets register variables.
     * @param localData Local data
     * @return Register variables
     */
    public HashMap<String, Integer> getRegisterVars(SourceGeneratorLocalData localData) {
        return localData.registerVars;
    }

    /**
     * Sets register variables.
     * @param localData Local data
     * @param value Value
     */
    public void setRegisterVars(SourceGeneratorLocalData localData, HashMap<String, Integer> value) {
        localData.registerVars = value;
    }

    /**
     * Sets in function.
     * @param localData Local data
     * @param value Value
     */
    public void setInFunction(SourceGeneratorLocalData localData, int value) {
        localData.inFunction = value;
    }

    /**
     * Gets in function.
     * @param localData Local data
     * @return In function
     */
    public int isInFunction(SourceGeneratorLocalData localData) {
        return localData.inFunction;
    }

    /**
     * Checks if in method.
     * @param localData Local data
     * @return True if in method
     */
    public boolean isInMethod(SourceGeneratorLocalData localData) {
        return localData.inMethod;
    }

    /**
     * Sets in method.
     * @param localData Local data
     * @param value Value
     */
    public void setInMethod(SourceGeneratorLocalData localData, boolean value) {
        localData.inMethod = value;
    }

    /**
     * Gets for in level.
     * @param localData Local data
     * @return For in level
     */
    public int getForInLevel(SourceGeneratorLocalData localData) {
        return localData.forInLevel;
    }

    /**
     * Sets for in level.
     * @param localData Local data
     * @param value Value
     */
    public void setForInLevel(SourceGeneratorLocalData localData, int value) {
        localData.forInLevel = value;
    }

    /**
     * Gets temp register.
     * @param localData Local data
     * @return Temp register
     */
    public int getTempRegister(SourceGeneratorLocalData localData) {
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        int tmpReg = 0;
        for (int i = 0; i < 256; i++) {
            if (!registerVars.containsValue(i)) {
                tmpReg = i;
                break;
            }
        }
        return tmpReg;
    }

    /**
     * Constructor.
     * @param abc ABC
     */
    public AVM2SourceGenerator(AbcIndexing abc) {
        this.abcIndex = abc;
    }

    /*public ABC getABC() {
     return abc;
     }*/

    /**
     * Generate class.
     * @param importedClasses Imported classes
     * @param cinitVariables Cinit variables
     * @param cinitNeedsActivation Cinit needs activation
     * @param cinit Cinit
     * @param openedNamespaces Opened namespaces
     * @param namespace Namespace
     * @param initScope Init scope
     * @param pkg Package
     * @param classInfo Class info
     * @param instanceInfo Instance info
     * @param localData Local data
     * @param isInterface Is interface
     * @param baseClassName Base class name
     * @param superName Super name
     * @param extendsVal Extends value
     * @param implementsStr Implements
     * @param iinit Instance initializer
     * @param iinitVariables Instance initializer variables
     * @param iinitNeedsActivation Instance initializer needs activation
     * @param commands Trait items
     * @param class_index Class index
     * @throws AVM2ParseException On parse error
     * @throws CompilationException On compilation error
     */
    public void generateClass(List<DottedChain> importedClasses, List<AssignableAVM2Item> cinitVariables, boolean cinitNeedsActivation, List<GraphTargetItem> cinit, List<NamespaceItem> openedNamespaces, int namespace, int initScope, DottedChain pkg, ClassInfo classInfo, InstanceInfo instanceInfo, SourceGeneratorLocalData localData, boolean isInterface, String baseClassName, String superName, GraphTargetItem extendsVal, List<GraphTargetItem> implementsStr, GraphTargetItem iinit, List<AssignableAVM2Item> iinitVariables, boolean iinitNeedsActivation, List<GraphTargetItem> commands, Reference<Integer> class_index) throws AVM2ParseException, CompilationException {
        List<GraphTargetItem> traitItems = new ArrayList<>();
        for (GraphTargetItem it : commands) {
            if ((it instanceof SlotAVM2Item) 
                    || (it instanceof ConstAVM2Item)
                    || (it instanceof ClassAVM2Item)
                    || (it instanceof InterfaceAVM2Item)
                    || (it instanceof MethodAVM2Item)
                    || (it instanceof GetterAVM2Item)
                    || (it instanceof SetterAVM2Item)
                    ) {
                traitItems.add(it);
            }
        }
        localData.currentClassBaseName = baseClassName;
        localData.pkg = pkg;
        localData.privateNs = abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_PRIVATE, pkg.toRawString().isEmpty() ? baseClassName : pkg.toRawString() + ":" + baseClassName, 0, true);
        localData.protectedNs = abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_PROTECTED, pkg.toRawString().isEmpty() ? baseClassName : pkg.toRawString() + ":" + baseClassName, 0, true);
        if (extendsVal == null && !isInterface) {
            extendsVal = new TypeItem(DottedChain.OBJECT);
        }
        ParsedSymbol s = null;
                
        
        if (Configuration.handleSkinPartsAutomatically.get()) {

            Map<String, Boolean> skinParts = new HashMap<>();
            for (GraphTargetItem t : traitItems) {
                String tname = null;
                List<Map.Entry<String, Map<String, String>>> tmetadata = null;
                if (t instanceof MethodAVM2Item) {
                    tname = ((MethodAVM2Item) t).functionName;
                    tmetadata = ((MethodAVM2Item) t).metadata;
                } else if (t instanceof SlotAVM2Item) {
                    tname = ((SlotAVM2Item) t).var;
                    tmetadata = ((SlotAVM2Item) t).metadata;
                } else if (t instanceof ConstAVM2Item) {
                    tname = ((ConstAVM2Item) t).var;
                    tmetadata = ((ConstAVM2Item) t).metadata;
                }
                if (tname != null && tmetadata != null) {
                    for (Map.Entry<String, Map<String, String>> en : tmetadata) {
                        if ("SkinPart".equals(en.getKey())) {
                            boolean req = false;
                            if (en.getValue().containsKey("required")) {
                                if ("true".equals(en.getValue().get("required"))) {
                                    req = true;
                                }
                            }
                            skinParts.put(tname, req);
                        }
                    }
                }
            }
            if (!skinParts.isEmpty()) {

                //Merge parts from _skinParts attribute of parent class
                GraphTargetItem parent = extendsVal;
                if (parent instanceof UnresolvedAVM2Item) {
                    parent = ((UnresolvedAVM2Item) parent).resolved;
                }
                if (parent instanceof TypeItem) {
                    ClassIndex ci = abcIndex.findClass(parent, abcIndex.getSelectedAbc(), localData != null ? localData.scriptIndex : null);
                    if (ci != null) {
                        int mi = ci.abc.class_info.get(ci.index).cinit_index;
                        MethodBody pcinit = ci.abc.findBody(mi);
                        ConvertData d = new ConvertData();

                        List<MethodBody> callStack = new ArrayList<>();
                        callStack.add(pcinit);
                        try {
                            pcinit.convert(callStack, null, d, "-", ScriptExportMode.AS, true, mi, -1, ci.index, ci.abc, null, new ScopeStack(), GraphTextWriter.TRAIT_CLASS_INITIALIZER, new NulWriter(), new ArrayList<>(), ci.abc.class_info.get(ci.index).static_traits, false, new HashSet<>());
                            //FIXME! Add skinparts from _skinParts attribute of parent class!!!
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AVM2SourceGenerator.class.getName()).log(Level.SEVERE, "Getting parent skinparts interrupted", ex);
                        }
                        for (Trait t : ci.abc.class_info.get(ci.index).static_traits.traits) {
                            if (t instanceof TraitSlotConst) {
                                TraitSlotConst tsc = (TraitSlotConst) t;
                                if (tsc.kindType == Trait.TRAIT_SLOT) {
                                    if ("_skinParts".equals(tsc.getName(ci.abc).getName(ci.abc.constants, new ArrayList<>(), true, true))) {
                                        if (d.assignedValues.containsKey(tsc)) {
                                            if (d.assignedValues.get(tsc).value instanceof NewObjectAVM2Item) {
                                                NewObjectAVM2Item no = (NewObjectAVM2Item) d.assignedValues.get(tsc).value;
                                                for (NameValuePair nvp : no.pairs) {
                                                    skinParts.put(EcmaScript.toString(nvp.name.getResult()), EcmaScript.toBoolean(nvp.value.getResult()));
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                /*
            Add
            override protected function get skinParts() : Object
                {
                   return _skinParts;
                }
                 */
                List<GraphTargetItem> getterBody = new ArrayList<>();
                UnresolvedAVM2Item sp = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, false, TypeItem.UNBOUNDED, 0, new DottedChain(new String[]{"_skinParts"}),
                        null, openedNamespaces, abcIndex);
                getterBody.add(new ReturnValueAVM2Item(null, null, sp));
                List<AssignableAVM2Item> subvars = new ArrayList<>();
                subvars.add(sp);
                List<List<NamespaceItem>> allopns = new ArrayList<>();
                allopns.add(openedNamespaces);

                GetterAVM2Item getter = new GetterAVM2Item(allopns, false, false, new ArrayList<>(), new NamespaceItem(pkg.isEmpty() ? baseClassName : pkg.toRawString() + ":" + baseClassName, Namespace.KIND_PROTECTED), isInterface, false, null, false, false, 0,
                        true, false, false, "skinParts", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        getterBody, subvars, new TypeItem("Object"));

                /*
            Add
            private static var _skinParts = {attr1:false, attr2:true};
                 */
                List<NameValuePair> pairs = new ArrayList<>();
                for (String tname : skinParts.keySet()) {
                    pairs.add(new NameValuePair(new StringAVM2Item(null, null, tname), skinParts.get(tname) ? new TrueItem(null, null) : new FalseItem(null, null)));
                }

                NewObjectAVM2Item sltVal = new NewObjectAVM2Item(null, null, pairs);

                SlotAVM2Item slt = new SlotAVM2Item(
                        new ArrayList<>(), new NamespaceItem(pkg.isEmpty() ? baseClassName : pkg.toRawString() + ":" + baseClassName, Namespace.KIND_PRIVATE),
                        null, true, "_skinParts", new TypeItem("Object"), sltVal, 0);

                traitItems.add(0, slt);
                traitItems.add(getter);

            }
        }

        Trait[] it = generateTraitsPhase1(importedClasses, openedNamespaces, baseClassName, superName, false, localData, traitItems, instanceInfo.instance_traits, class_index, false);
        Trait[] st = generateTraitsPhase1(importedClasses, openedNamespaces, baseClassName, superName, true, localData, traitItems, classInfo.static_traits, class_index, false);
        generateTraitsPhase2(importedClasses, pkg, traitItems, it, openedNamespaces, localData);
        generateTraitsPhase2(importedClasses, pkg, traitItems, st, openedNamespaces, localData);
        abcIndex.refreshSelected();
        generateTraitsPhase4(importedClasses, openedNamespaces, initScope, isInterface, baseClassName, superName, false, localData, traitItems, instanceInfo.instance_traits, it, new HashMap<>(), class_index, false);
        generateTraitsPhase4(importedClasses, openedNamespaces, initScope, isInterface, baseClassName, superName, true, localData, traitItems, classInfo.static_traits, st, new HashMap<>(), class_index, false);
        int init;
        if (iinit == null || isInterface) {
            instanceInfo.iinit_index = init = method(null, false, 0, false, isInterface, false, new ArrayList<>(), pkg, false, new ArrayList<>(), initScope + 1, false, 0, isInterface ? null : baseClassName, extendsVal != null ? extendsVal.toString() : null, true, localData, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), TypeItem.UNBOUNDED/*?? FIXME*/);
        } else {
            MethodAVM2Item m = (MethodAVM2Item) iinit;
            List<AssignableAVM2Item> allIinitVariables = new ArrayList<>();
            allIinitVariables.addAll(iinitVariables);
            allIinitVariables.addAll(m.subvariables);
            instanceInfo.iinit_index = init = method(null, false, pkg.toRawString().isEmpty() ? str(baseClassName + "/" + baseClassName) : str(pkg.toRawString() + ":" + baseClassName + "/" + baseClassName), false, false, false, new ArrayList<>(), pkg, m.needsActivation, allIinitVariables, initScope + 1, m.hasRest, m.line, baseClassName, extendsVal != null ? extendsVal.toString() : null, true, localData, m.paramTypes, m.paramNames, m.paramValues, m.body, TypeItem.UNBOUNDED/*?? FIXME*/);
        }

        //Class initializer
        int cinit_index = method(null, true, str(""), false, false, false, new ArrayList<>(), pkg, cinitNeedsActivation, cinitVariables, initScope + (implementsStr.isEmpty() ? 0 : 1), false, 0, isInterface ? null : baseClassName, superName, false, localData, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), commands, TypeItem.UNBOUNDED);
        MethodBody cinitBody = abcIndex.getSelectedAbc().findBody(cinit_index);

        
        for (int i = 0; i < cinitVariables.size(); i++) {
            AssignableAVM2Item an = cinitVariables.get(i);
            if (an instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item n = (UnresolvedAVM2Item) an;
                if (n.resolved == null) {
                    String fullClass = localData.getFullClass();
                    List<MethodBody> callStack = new ArrayList<>();
                    callStack.add(cinitBody);
                    GraphTargetItem res = n.resolve(localData, fullClass, new TypeItem(fullClass), new ArrayList<>(), new ArrayList<>(), abcIndex, callStack, cinitVariables);
                    if (res instanceof AssignableAVM2Item) {
                        cinitVariables.set(i, (AssignableAVM2Item) res);
                    } else {
                        cinitVariables.remove(i);
                        i--;
                    }
                }
            }
        }
        
        //List<AVM2Instruction> cinitcode = new ArrayList<>();
        List<AVM2Instruction> initcode = new ArrayList<>();
        for (GraphTargetItem ti : commands) {
            if ((ti instanceof SlotAVM2Item) || (ti instanceof ConstAVM2Item)) {
                GraphTargetItem val = null;
                boolean isStatic = false;
                int ns = -1;
                String tname = null;
                boolean isConst = false;
                GraphTargetItem type = null;
                if (ti instanceof SlotAVM2Item) {
                    val = ((SlotAVM2Item) ti).value;
                    isStatic = ((SlotAVM2Item) ti).isStatic();
                    ns = genNs(importedClasses, pkg, ((SlotAVM2Item) ti).pkg, openedNamespaces, localData, ((SlotAVM2Item) ti).line);
                    tname = ((SlotAVM2Item) ti).var;
                    type = ((SlotAVM2Item) ti).type;
                }
                if (ti instanceof ConstAVM2Item) {
                    val = ((ConstAVM2Item) ti).value;
                    isStatic = ((ConstAVM2Item) ti).isStatic();
                    ns = genNs(importedClasses, pkg, ((ConstAVM2Item) ti).pkg, openedNamespaces, localData, ((ConstAVM2Item) ti).line);
                    tname = ((ConstAVM2Item) ti).var;
                    isConst = true;
                    type = ((ConstAVM2Item) ti).type;
                    if (((ConstAVM2Item) ti).type.toString().equals("Namespace")) {
                        continue;
                    }
                }
                /*if (isStatic && val != null) {
                    cinitcode.add(ins(AVM2Instructions.FindProperty, traitName(ns, tname)));
                    localData.isStatic = true;
                    cinitcode.addAll(toInsList(val.toSource(localData, this)));
                    cinitcode.add(ins(isConst ? AVM2Instructions.InitProperty : AVM2Instructions.SetProperty, traitName(ns, tname)));
                }
                */
                if (!isStatic && val != null) {
                    //do not init basic values, that can be stored in trait
                    if (!(val instanceof IntegerValueAVM2Item) 
                            && !(val instanceof DoubleValueAVM2Item)
                            && !(val instanceof StringAVM2Item) 
                            && !(val instanceof BooleanAVM2Item) 
                            && !(val instanceof NullAVM2Item) 
                            && !(val instanceof UndefinedAVM2Item)
                            ) {
                        val = handleAndOrCoerce(val, type);
                        initcode.add(ins(AVM2Instructions.GetLocal0));
                        localData.isStatic = false;
                        initcode.addAll(toInsList(val.toSource(localData, this)));
                        initcode.add(ins(isConst ? AVM2Instructions.InitProperty : AVM2Instructions.SetProperty, traitName(ns, tname)));
                    }
                }
            }
        }
        MethodBody initBody = null;
        if (!isInterface) {
            initBody = abcIndex.getSelectedAbc().findBody(init);
            initBody.getCode().code.addAll(iinit == null ? 0 : 2, initcode); //after getlocal0,pushscope

            /*if (cinitBody.getCode().code.get(cinitBody.getCode().code.size() - 1).definition instanceof ReturnVoidIns) {
                cinitBody.getCode().code.addAll(2, cinitcode); //after getlocal0,pushscope
            }*/
        }
        cinitBody.markOffsets();
        cinitBody.autoFillStats(abcIndex.getSelectedAbc(), initScope + (implementsStr.isEmpty() ? 0 : 1), true);

        classInfo.cinit_index = cinit_index;
        if (initBody != null) {
            initBody.markOffsets();
            initBody.autoFillStats(abcIndex.getSelectedAbc(), initScope + 1, true);
        }
        instanceInfo.interfaces = new int[implementsStr.size()];
        for (int i = 0; i < implementsStr.size(); i++) {
            instanceInfo.interfaces[i] = superIntName(localData, implementsStr.get(i));
        }

    }

    /**
     * Generates class.
     * @param namespace Namespace
     * @param ci Class info
     * @param ii Instance info
     * @param initScope Init scope
     * @param pkg Package
     * @param localData Local data
     * @param cls Class
     * @param class_index Class index
     * @return Class index
     * @throws AVM2ParseException On parse error
     * @throws CompilationException On compilation error
     */
    public int generateClass(int namespace, ClassInfo ci, InstanceInfo ii, int initScope, DottedChain pkg, SourceGeneratorLocalData localData, AVM2Item cls, Reference<Integer> class_index) throws AVM2ParseException, CompilationException {
        /*ClassInfo ci = new ClassInfo();
         InstanceInfo ii = new InstanceInfo();
         abc.class_info.add(ci);
         abc.instance_info.add(ii);
         */
        if (cls instanceof ClassAVM2Item) {
            ClassAVM2Item cai = (ClassAVM2Item) cls;
            //TODO: iinit variables, iinit activation
            generateClass(cai.importedClasses, cai.cinitVariables, cai.cinitActivation, cai.staticInit,
                    cai.openedNamespaces,
                    namespace,
                    initScope, pkg, ci, ii,
                    localData, false,
                    cai.classBaseName, cai.extendsOp == null ? "Object" : cai.extendsOp.toString(),
                    cai.extendsOp, cai.implementsOp, cai.iinit,
                    cai.iinitVariables, cai.iinitActivation, cai.traits, class_index
            );
            if (!cai.isDynamic) {
                ii.flags |= InstanceInfo.CLASS_SEALED;
            }
            if (cai.isFinal) {
                ii.flags |= InstanceInfo.CLASS_FINAL;
            }
            if (!cai.isNullable) {
                ii.flags |= InstanceInfo.CLASS_NON_NULLABLE;
            }
            ii.flags |= InstanceInfo.CLASS_PROTECTEDNS;
            ii.protectedNS = abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_PROTECTED, pkg.toRawString().isEmpty() ? cai.classBaseName : pkg.toRawString() + ":" + cai.classBaseName, 0, true);
        }
        if (cls instanceof InterfaceAVM2Item) {
            InterfaceAVM2Item iai = (InterfaceAVM2Item) cls;
            ii.flags |= InstanceInfo.CLASS_INTERFACE;
            ii.flags |= InstanceInfo.CLASS_SEALED;
            if (!iai.isNullable) {
                ii.flags |= InstanceInfo.CLASS_NON_NULLABLE;
            }
            generateClass(iai.importedClasses, new ArrayList<>(), false, new ArrayList<>(),
                    iai.openedNamespaces, namespace, initScope, pkg, ci, ii, localData, true, iai.baseName, null, null, iai.superInterfaces, null, null, false, iai.methods,
                    class_index
            );
        }

        return abcIndex.getSelectedAbc().instance_info.size() - 1;
    }

    /**
     * Gets trait name.
     * @param namespace Namespace
     * @param var Variable
     * @return Multiname index
     */
    public int traitName(int namespace, String var) {
        return abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createQName(false, str(var), namespace), true);
    }

    /**
     * Gets type name.
     * @param localData Local data
     * @param type Type
     * @return Multiname index
     * @throws CompilationException On compilation error
     */
    public int typeName(SourceGeneratorLocalData localData, GraphTargetItem type) throws CompilationException {
        if (type.equals(TypeItem.UNBOUNDED)) {
            return 0;
        }
        if (("" + type).equals("*")) {
            return 0;
        }

        return resolveType(localData, type, abcIndex);
        /*
         TypeItem nameItem = (TypeItem) type;
         baseName = nameItem.fullTypeName;
         if (baseName.contains(".")) {
         pkg = baseName.substring(0, baseName.lastIndexOf('.'));
         baseName = baseName.substring(baseName.lastIndexOf('.') + 1);
         }
         if (!nameItem.subtypes.isEmpty()) { //It's vector => TypeName
         List<Integer> params = new ArrayList<>();
         for (GraphTargetItem p : nameItem.subtypes) {
         params.add(typeName(localData, p));//abc.getLastAbc().constants.getMultinameId(new Multiname(Multiname.QNAME, str(p), namespace(Namespace.KIND_PACKAGE, ppkg), 0, 0, new ArrayList<Integer>()), true));
         }
         int qname = abc.getLastAbc().constants.getMultinameId(new Multiname(Multiname.QNAME, str(baseName), namespace(Namespace.KIND_PACKAGE, pkg), 0, 0, new ArrayList<Integer>()), true);
         return abc.getLastAbc().constants.getMultinameId(new Multiname(Multiname.TYPENAME, 0, 0, 0, qname, params), true);
         } else {
         return abc.getLastAbc().constants.getMultinameId(new Multiname(Multiname.QNAME, str(baseName), namespace(Namespace.KIND_PACKAGE, pkg), 0, 0, new ArrayList<Integer>()), true);
         }*/
    }

    /**
     * Gets identifier from name.
     * @param name Name
     * @return Identifier
     */
    public int ident(GraphTargetItem name) {
        if (name instanceof NameAVM2Item) {
            return str(((NameAVM2Item) name).getVariableName());
        }
        throw new RuntimeException("no ident"); //FIXME
    }

    /**
     * Gets namespace.
     * @param nsKind Namespace kind
     * @param name Name
     * @return Namespace index
     */
    public int namespace(int nsKind, String name) {
        return abcIndex.getSelectedAbc().constants.getNamespaceId(nsKind, str(name), 0, true);
    }

    /**
     * Gets string
     * @param name Name
     * @return String index
     */
    public int str(String name) {
        return abcIndex.getSelectedAbc().constants.getStringId(name, true);
    }

    /**
     * Gets property name.
     * @param name Name
     * @return Multiname index
     */
    public int propertyName(GraphTargetItem name) {
        if (name instanceof NameAVM2Item) {
            NameAVM2Item va  = (NameAVM2Item) name;
            return abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createQName(false, str(va.getVariableName()), namespace(Namespace.KIND_PACKAGE, "")), true);
        }
        throw new RuntimeException("no prop"); //FIXME
    }

    /**
     * Gets free register.
     * @param localData Local data
     * @return Register
     */
    public int getFreeRegister(SourceGeneratorLocalData localData) {
        for (int i = 0;; i++) {
            if (!localData.registerVars.containsValue(i)) {
                localData.registerVars.put("__TEMP__" + i, i);
                return i;
            }
        }
    }

    /**
     * Kills register.
     * @param localData Local data
     * @param i Register
     * @return True if register was killed
     */
    public boolean killRegister(SourceGeneratorLocalData localData, int i) {
        String key = null;
        for (String k : localData.registerVars.keySet()) {
            if (localData.registerVars.get(k) == i) {
                key = k;
                break;
            }
        }
        if (key != null) {
            localData.registerVars.remove(key);
            return true;
        }
        return false;
    }

    public int methodHeader(boolean isStatic, int name_index, boolean subMethod, boolean isInterface, boolean isNative, List<MethodBody> callStack, DottedChain pkg, boolean needsActivation, List<AssignableAVM2Item> subvariables, int initScope, boolean hasRest, int line, String classBaseName, String superType, boolean constructor, SourceGeneratorLocalData localData, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, GraphTargetItem retType) throws CompilationException {
        //Reference<Boolean> hasArgs = new Reference<>(Boolean.FALSE);
        //calcRegisters(localData,needsActivation,paramNames,subvariables,body, hasArgs);
        SourceGeneratorLocalData newlocalData = new SourceGeneratorLocalData(new HashMap<>(), 1, true, 0);
        newlocalData.currentClassBaseName = classBaseName;
        newlocalData.pkg = localData.pkg;
        newlocalData.callStack.addAll(localData.callStack);
        newlocalData.traitUsages = localData.traitUsages;
        newlocalData.currentScript = localData.currentScript;
        newlocalData.scriptIndex = localData.scriptIndex;
        newlocalData.documentClass = localData.documentClass;
        newlocalData.privateNs = localData.privateNs;
        newlocalData.protectedNs = localData.protectedNs;
        newlocalData.isStatic = isStatic;
        newlocalData.subMethod = subMethod;
        newlocalData.numberContext = localData.numberContext;
        newlocalData.returnType = retType;
        localData = newlocalData;

        localData.activationReg = 0;    int[] param_types = new int[paramTypes.size()];
        ValueKind[] optional = new ValueKind[paramValues.size()];
        //int[] param_names = new int[paramNames.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            param_types[i] = typeName(localData, paramTypes.get(i));
            //param_names[i] = str(paramNames.get(i));
        }

        for (int i = 0; i < paramValues.size(); i++) {
            optional[i] = getValueKind(Namespace.KIND_NAMESPACE/*FIXME*/, paramTypes.get(paramTypes.size() - paramValues.size() + i), paramValues.get(i), false);
            if (optional[i] == null) {
                throw new CompilationException("Default value must be compiletime constant", line);
            }
        }

        MethodInfo mi = new MethodInfo(param_types, constructor ? 0 : typeName(localData, retType), name_index, 0, optional, new int[0]/*no param_names*/);
        return abcIndex.getSelectedAbc().addMethodInfo(mi);
    }
    
    /**
     * Gets method.
     * @param replaceMethodIndex Which method info to replace. Null = create new.
     * @param isStatic Is static
     * @param name_index Name index
     * @param subMethod Sub method
     * @param isInterface Is interface
     * @param isNative Is native
     * @param callStack Call stack
     * @param pkg Package
     * @param needsActivation Needs activation
     * @param subvariables Subvariables
     * @param initScope Init scope
     * @param hasRest Has rest
     * @param line Line
     * @param classBaseName Class base name
     * @param superType Super type
     * @param constructor Constructor
     * @param localData Local data
     * @param paramTypes Parameter types
     * @param paramNames Parameter names
     * @param paramValues Parameter values
     * @param body Body
     * @param retType Return type
     * @return Method index
     * @throws CompilationException On compilation error
     */
    public int method(Integer replaceMethodIndex, boolean isStatic, int name_index, boolean subMethod, boolean isInterface, boolean isNative, List<MethodBody> callStack, DottedChain pkg, boolean needsActivation, List<AssignableAVM2Item> subvariables, int initScope, boolean hasRest, int line, String classBaseName, String superType, boolean constructor, SourceGeneratorLocalData localData, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, GraphTargetItem retType) throws CompilationException {
        //Reference<Boolean> hasArgs = new Reference<>(Boolean.FALSE);
        //calcRegisters(localData,needsActivation,paramNames,subvariables,body, hasArgs);
        SourceGeneratorLocalData newlocalData = new SourceGeneratorLocalData(new HashMap<>(), 1, true, 0);
        newlocalData.currentClassBaseName = classBaseName;
        newlocalData.pkg = localData.pkg;
        newlocalData.callStack.addAll(localData.callStack);
        newlocalData.traitUsages = localData.traitUsages;
        newlocalData.currentScript = localData.currentScript;
        newlocalData.scriptIndex = localData.scriptIndex;
        newlocalData.documentClass = localData.documentClass;
        newlocalData.privateNs = localData.privateNs;
        newlocalData.protectedNs = localData.protectedNs;
        newlocalData.isStatic = isStatic;
        newlocalData.subMethod = subMethod;
        newlocalData.numberContext = localData.numberContext;
        newlocalData.returnType = retType;
        localData = newlocalData;

        localData.activationReg = 0;

        for (int i = 0; i < subvariables.size(); i++) {
            AssignableAVM2Item an = subvariables.get(i);
            if (an instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item n = (UnresolvedAVM2Item) an;
                if (n.resolved == null) {
                    String fullClass = localData.getFullClass();
                    GraphTargetItem res = n.resolve(localData, fullClass, new TypeItem(fullClass), paramTypes, paramNames, abcIndex, callStack, subvariables);
                    if (res instanceof AssignableAVM2Item) {
                        subvariables.set(i, (AssignableAVM2Item) res);
                    } else {
                        subvariables.remove(i);
                        i--;
                    }
                }
            }
        }

        for (int t = 0; t < paramTypes.size(); t++) {
            GraphTargetItem an = paramTypes.get(t);
            if (an instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item n = (UnresolvedAVM2Item) an;
                if (n.resolved == null) {
                    String fullClass = localData.getFullClass();
                    GraphTargetItem res = n.resolve(localData, fullClass, new TypeItem(fullClass), paramTypes, paramNames, abcIndex, callStack, subvariables);
                    paramTypes.set(t, res);
                }
            }
        }

        boolean hasArguments = false;
        List<String> slotNames = new ArrayList<>();
        List<GraphTargetItem> slotTypes = new ArrayList<>();
        slotNames.add("--first");
        slotTypes.add(TypeItem.UNBOUNDED);

        int paramLine = 0; //?

        List<String> registerNames = new ArrayList<>();
        List<Integer> registerLines = new ArrayList<>();
        List<GraphTargetItem> registerTypes = new ArrayList<>();
        if (classBaseName != null && !subMethod) {
            String fullClassName = pkg.addWithSuffix(classBaseName).toRawString();
            registerTypes.add(new TypeItem(fullClassName));
            localData.scopeStack.add(new LocalRegAVM2Item(null, null, registerNames.size(), null, new TypeItem(fullClassName)));
            registerNames.add("this");
            registerLines.add(0); //?

        } else {
            registerTypes.add(new TypeItem("global"));
            registerNames.add("this");
            registerLines.add(0); //?
        }
        for (GraphTargetItem t : paramTypes) {
            registerTypes.add(t);
            slotTypes.add(t);
        }
        for (int i = 0; i < paramNames.size(); i++) {
            registerLines.add(paramLine);
        }
        registerNames.addAll(paramNames);
        slotNames.addAll(paramNames);
        if (hasRest) {
            registerTypes.add(TypeItem.ARRAY);
            slotTypes.add(TypeItem.ARRAY);
        }
        localData.registerVars.clear();
        for (AssignableAVM2Item an : subvariables) {
            if (an instanceof NameAVM2Item) {
                NameAVM2Item n = (NameAVM2Item) an;
                if (n.getVariableName().equals("arguments") & !n.isDefinition()) {
                    registerNames.add("arguments");
                    registerTypes.add(new TypeItem("Array"));
                    registerLines.add(0); //?
                    slotNames.add(n.getVariableName());
                    slotTypes.add(new TypeItem("Array"));
                    hasArguments = true;
                    break;
                }
            }
        }
        int paramRegCount = registerNames.size();

        if (needsActivation) {
            registerNames.add("+$activation");
            registerLines.add(0); //?
            localData.activationReg = registerNames.size() - 1;
            registerTypes.add(new TypeItem("Object"));
            localData.scopeStack.add(new LocalRegAVM2Item(null, null, localData.activationReg, null, new TypeItem("Object")));
        }

        String mask = Configuration.registerNameFormat.get();
        String maskRegexp = mask.replace("%d", "([0-9]+)");
        Pattern pat = Pattern.compile(maskRegexp);

        final String UNUSED = "~~unused";

        //Two rounds
        for (int round = 1; round <= 2; round++) {
            for (AssignableAVM2Item an : subvariables) {
                if (an instanceof NameAVM2Item) {
                    NameAVM2Item n = (NameAVM2Item) an;
                    if (n.isDefinition() && !registerNames.contains(n.getVariableName())) {
                        String varName = n.getVariableName();
                        if (!needsActivation) {
                            Matcher m = pat.matcher(varName);
                            boolean addNew = false;
                            //In first round, make all register that match standard loc_xx register                            
                            if ((round == 1) && (m.matches())) {
                                String regIndexStr = m.group(1);
                                int regIndex = Integer.parseInt(regIndexStr);
                                
                                boolean alreadyExistsWithDifferentName = regIndex < registerNames.size() 
                                        && !registerNames.get(regIndex).equals(UNUSED)
                                        && !registerNames.get(regIndex).equals(varName);
                                
                                if (alreadyExistsWithDifferentName) {
                                    addNew = true;
                                } else {
                                    while (registerNames.size() <= regIndex) {
                                        registerNames.add(UNUSED);
                                        registerTypes.add(TypeItem.UNBOUNDED);
                                        registerLines.add(paramLine);
                                    }
                                    registerNames.set(regIndex, varName);
                                    registerTypes.set(regIndex, n.type);
                                    registerLines.set(regIndex, n.line);
                                }
                            } else if (round == 2 && !m.matches()) { //in second round the rest
                                addNew = true;
                            }
                            if (addNew) {                                
                                //search for some unused indices first:
                                int newRegIndex = -1;
                                for (int j = 0; j < registerNames.size(); j++) {
                                    if (UNUSED.equals(registerNames.get(j))) {
                                        newRegIndex = j;
                                        break;
                                    }
                                }
                                if (newRegIndex == -1) {
                                    newRegIndex = registerNames.size();
                                    registerNames.add(UNUSED);
                                    registerTypes.add(TypeItem.UNBOUNDED);
                                    registerLines.add(paramLine);
                                }
                                registerNames.set(newRegIndex, n.getVariableName());
                                registerTypes.set(newRegIndex, n.type);
                                registerLines.set(newRegIndex, n.line);
                            }
                        } else {
                            if (round == 1) {
                                if (!slotNames.contains(varName)) {
                                    slotNames.add(varName);
                                    slotTypes.add(n.type);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int j = registerNames.size() - 1; j >= 0; j--) {
            if (UNUSED.equals(registerNames.get(j))) {
                registerNames.remove(j);
                registerTypes.remove(j);
                registerLines.remove(j);
            }
        }
        for (int j = 0; j < registerNames.size(); j++) {
            if (UNUSED.equals(registerNames.get(j))) {
                String standardName = String.format(mask, j);
                registerNames.set(j, standardName);
            }
        }

        int slotScope = subMethod ? 0 : 1;

        for (AssignableAVM2Item an : subvariables) {
            if (an instanceof NameAVM2Item) {
                NameAVM2Item n = (NameAVM2Item) an;
                String variableName = n.getVariableName();
                if (variableName != null) {
                    boolean isThisOrSuper = variableName.equals("this") || variableName.equals("super");
                    if (!isThisOrSuper && needsActivation) {
                        if (n.getSlotNumber() <= 0) {
                            n.setSlotNumber(slotNames.indexOf(variableName));
                            n.setSlotScope(slotScope);
                        }
                    } else if (isThisOrSuper) {
                        n.setRegNumber(0);
                    } else {
                        n.setRegNumber(registerNames.indexOf(variableName));
                    }
                }
            }
        }

        for (int i = 0; i < registerNames.size(); i++) {
            if (needsActivation && i > localData.activationReg) {
                break;
            }
            localData.registerVars.put(registerNames.get(i), i);
        }
        List<NameAVM2Item> declarations = new ArrayList<>();
        loopn:
        for (AssignableAVM2Item an : subvariables) {
            if (an instanceof NameAVM2Item) {
                NameAVM2Item n = (NameAVM2Item) an;

                if (needsActivation) {
                    if (n.getSlotScope() != slotScope) {
                        continue;
                    } else if (n.getSlotNumber() < paramRegCount) {
                        continue;
                    }
                }
                for (NameAVM2Item d : declarations) {
                    if (n.getVariableName() != null && n.getVariableName().equals(d.getVariableName())) {
                        continue loopn;
                    }
                }

                for (GraphTargetItem it : body) { //search first level of commands
                    if (it instanceof NameAVM2Item) {
                        NameAVM2Item n2 = (NameAVM2Item) it;
                        if (n2.isDefinition() && n2.getAssignedValue() != null && n2.getVariableName().equals(n.getVariableName())) {
                            continue loopn;
                        }
                        if (!n2.isDefinition() && n2.getVariableName() != null && n2.getVariableName().equals(n.getVariableName())) { //used earlier than defined
                            break;
                        }
                    }
                }
                if (n.unresolved) {
                    continue;
                }
                if (n.redirect != null) {
                    continue;
                }
                if (n.getNs() != null) {
                    continue;
                }

                String variableName = n.getVariableName();
                if ("this".equals(variableName) || "super".equals(variableName) || paramNames.contains(variableName) || "arguments".equals(variableName)) {
                    continue;
                }

                NameAVM2Item d = new NameAVM2Item(n.type, n.line, n.isAttribute(), n.getVariableName(), n.getNamespaceSuffix(), null /*NameAVM2Item.getDefaultValue("" + n.type)*/, true, n.openedNamespaces, abcIndex, n.isConst());
                //no index
                if (needsActivation) {
                    if (d.getSlotNumber() <= 0) {
                        d.setSlotNumber(n.getSlotNumber());
                        d.setSlotScope(n.getSlotScope());
                    }
                } else {
                    d.setRegNumber(n.getRegNumber());
                }
                declarations.add(d);
            }
        }

        int[] param_types = new int[paramTypes.size()];
        ValueKind[] optional = new ValueKind[paramValues.size()];
        //int[] param_names = new int[paramNames.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            param_types[i] = typeName(localData, paramTypes.get(i));
            //param_names[i] = str(paramNames.get(i));
        }

        for (int i = 0; i < paramValues.size(); i++) {
            optional[i] = getValueKind(Namespace.KIND_NAMESPACE/*FIXME*/, paramTypes.get(paramTypes.size() - paramValues.size() + i), paramValues.get(i), false);
            if (optional[i] == null) {
                throw new CompilationException("Default value must be compiletime constant", line);
            }
        }

        MethodInfo mi = new MethodInfo(param_types, constructor ? 0 : typeName(localData, retType), name_index, 0, optional, new int[0]/*no param_names*/);
        
        if (replaceMethodIndex != null) {
            mi = abcIndex.getSelectedAbc().method_info.get(replaceMethodIndex);
        }
        
        if (hasArguments) {
            mi.setFlagNeed_Arguments();
        }
        //No param names like in official
        /*
         if (!paramNames.isEmpty()) {
         mi.setFlagHas_paramnames();
         }*/
        if (!paramValues.isEmpty()) {
            mi.setFlagHas_optional();
        }
        if (hasRest) {
            mi.setFlagNeed_rest();
        }
        if (isNative) {
            mi.setFlagNative();
        }

        int mindex;
        if (!isInterface && !isNative) {
            MethodBody mbody = new MethodBody(abcIndex.getSelectedAbc(), new Traits(), new byte[0], new ABCException[0]);

            if (needsActivation) {
                int slotId = 1;
                for (int i = 1; i < slotNames.size(); i++) {
                    TraitSlotConst tsc = new TraitSlotConst();
                    tsc.slot_id = slotId++;
                    tsc.name_index = abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createQName(false, abcIndex.getSelectedAbc().constants.getStringId(slotNames.get(i), true), abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_PACKAGE_INTERNAL, pkg, 0, true)), true);
                    tsc.type_index = typeName(localData, slotTypes.get(i));
                    mbody.traits.traits.add(tsc);
                }
                for (int i = 1; i < paramRegCount; i++) {
                    NameAVM2Item param = new NameAVM2Item(registerTypes.get(i), 0, false, registerNames.get(i), "", null, false, new ArrayList<>(), abcIndex, false);
                    param.setRegNumber(i);
                    NameAVM2Item d = new NameAVM2Item(registerTypes.get(i), 0, false, registerNames.get(i), "", param, true, new ArrayList<>(), abcIndex, false);
                    d.setSlotScope(slotScope);
                    d.setSlotNumber(slotNames.indexOf(registerNames.get(i)));
                    declarations.add(d);
                }
            }
            boolean addRet = false;
            if (body != null) {
                body.addAll(0, declarations);
                if (body.isEmpty() || (!((body.get(body.size() - 1) instanceof ReturnValueAVM2Item)
                        || (body.get(body.size() - 1) instanceof ReturnVoidAVM2Item)))) {
                    addRet = true;
                }
            }

            localData.exceptions = new ArrayList<>();
            localData.callStack.add(mbody);
            List<GraphSourceItem> src = body == null ? new ArrayList<>() : generate(localData, body);

            mbody.method_info = replaceMethodIndex != null ? replaceMethodIndex : abcIndex.getSelectedAbc().addMethodInfo(mi);
            ArrayList<AVM2Instruction> mbodyCode = toInsList(src);
            mbody.setCode(new AVM2Code(mbodyCode));

            if (needsActivation) {
                /*if (localData.traitUsages.containsKey(mbody)) {
                    List<Integer> usages = localData.traitUsages.get(mbody);
                    List<AVM2Instruction> mbodyCodeDecl = new ArrayList<>();
                    for (int i = 0; i < mbody.traits.traits.size(); i++) {
                        if (usages.contains(i)) {
                            TraitSlotConst tsc = (TraitSlotConst) mbody.traits.traits.get(i);
                            GraphTargetItem type = TypeItem.UNBOUNDED;
                            if (tsc.type_index > 0) {
                                type = new TypeItem(abcIndex.getSelectedAbc().constants.getMultiname(tsc.type_index).getNameWithNamespace(abcIndex.getSelectedAbc().constants, true));
                            }
                            NameAVM2Item d = new NameAVM2Item(type, 0, tsc.getName(abcIndex.getSelectedAbc()).getName(abcIndex.getSelectedAbc().constants, null, true, true), NameAVM2Item.getDefaultValue("" + type), true, new ArrayList<>(), abcIndex);
                            d.setSlotNumber(tsc.slot_id);
                            d.setSlotScope(slotScope);
                            mbodyCodeDecl.addAll(toInsList(d.toSourceIgnoreReturnValue(localData, this)));
                        }
                    }
                    mbodyCode.addAll(0, mbodyCodeDecl);
                }*/

                List<AVM2Instruction> acts = new ArrayList<>();
                acts.add(ins(AVM2Instructions.NewActivation));
                acts.add(ins(AVM2Instructions.Dup));
                acts.add(AssignableAVM2Item.generateSetLoc(localData.activationReg));
                acts.add(ins(AVM2Instructions.PushScope));

                mbodyCode.addAll(0, acts);
            }

            if (constructor) {
                /* List<ABC> abcs = new ArrayList<>();
                 abcs.add(abc);
                 abcs.addAll(allABCs);
                 */
                int parentConstMinAC = 0;

                AbcIndexing.ClassIndex ci = abcIndex.findClass(new TypeItem(superType), abcIndex.getSelectedAbc(), localData.scriptIndex);

                if (ci != null) {
                    MethodInfo pmi = ci.abc.method_info.get(ci.abc.instance_info.get(ci.index).iinit_index);
                    parentConstMinAC = pmi.param_types.length;
                    if (pmi.flagHas_optional()) {
                        parentConstMinAC -= pmi.optional.length;
                    }
                }
                int ac = -1;
                for (AVM2Instruction ins : mbodyCode) {
                    if (ins.definition instanceof ConstructSuperIns) {
                        ac = ins.operands[0];
                        if (parentConstMinAC > ac) {
                            throw new CompilationException("Parent constructor call requires different number of arguments", line);
                        }

                    }
                }
                if (ac == -1) {
                    if (parentConstMinAC == 0) {
                        mbodyCode.add(0, new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
                        mbodyCode.add(1, new AVM2Instruction(0, AVM2Instructions.ConstructSuper, new int[]{0}));

                    } else {
                        throw new CompilationException("Parent constructor must be called", line);
                    }
                }
            }
            for (int i = 1; i < registerNames.size(); i++) {
                mbodyCode.add(i - 1, ins(AVM2Instructions.Debug, 1, str(registerNames.get(i)), i - 1, (int) registerLines.get(i)));
            }
            if (!subMethod) {
                mbodyCode.add(0, new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
                mbodyCode.add(1, new AVM2Instruction(0, AVM2Instructions.PushScope, null));
            }
            /*if (!mbodyCode.isEmpty()) {
                InstructionDefinition lastDef = mbodyCode.get(mbodyCode.size() - 1).definition;
                if (!((lastDef instanceof ReturnVoidIns) || (lastDef instanceof ReturnValueIns))) {
                    addRet = true;
                }
            } else {
                addRet = true;
            }*/
            if (addRet) {
                if (retType.toString().equals("*") || retType.toString().equals("void") || constructor) {
                    mbodyCode.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null));
                } else {
                    mbodyCode.add(new AVM2Instruction(0, AVM2Instructions.PushUndefined, null));
                    mbodyCode.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
                }
            }
            mbody.exceptions = localData.exceptions.toArray(new ABCException[localData.exceptions.size()]);
            int offset = 0;
            for (int i = 0; i < mbodyCode.size(); i++) {
                AVM2Instruction ins = mbodyCode.get(i);
                if (ins instanceof ExceptionMarkAVM2Instruction) {
                    ExceptionMarkAVM2Instruction m = (ExceptionMarkAVM2Instruction) ins;
                    switch (m.markType) {
                        case MARK_E_START:
                            mbody.exceptions[m.exceptionId].start = offset;
                            break;
                        case MARK_E_END:
                            mbody.exceptions[m.exceptionId].end = offset;
                            break;
                        case MARK_E_TARGET:
                            mbody.exceptions[m.exceptionId].target = offset;
                            break;
                    }
                    mbodyCode.remove(i);
                    i--;
                    continue;
                }
                offset += ins.getBytesLength();
            }

            mbody.markOffsets();
            mbody.autoFillStats(abcIndex.getSelectedAbc(), initScope, classBaseName != null);
            abcIndex.getSelectedAbc().addMethodBody(mbody);
            mindex = mbody.method_info;
        } else {
            if (replaceMethodIndex != null) {
                mindex = replaceMethodIndex;
            } else {
                mindex = abcIndex.getSelectedAbc().addMethodInfo(mi);
            }
        }

        return mindex;
    }

    /**
     * Gets value kind.
     * @param ns Namespace
     * @param type Type
     * @param val Value
     * @param generatedNs Generated namespace
     * @return Value kind
     */
    public ValueKind getValueKind(int ns, GraphTargetItem type, GraphTargetItem val, boolean generatedNs) {

        if (val instanceof BooleanAVM2Item) {
            BooleanAVM2Item bi = (BooleanAVM2Item) val;
            if (bi.value) {
                return new ValueKind(ValueKind.CONSTANT_True, ValueKind.CONSTANT_True);
            } else {
                return new ValueKind(ValueKind.CONSTANT_False, ValueKind.CONSTANT_False);
            }
        }

        boolean isNs = false;
        if (type instanceof NameAVM2Item) {
            if (((NameAVM2Item) type).getVariableName().equals("namespace")) {
                isNs = true;
            }
        }

        if ((type instanceof TypeItem) && (((TypeItem) type).fullTypeName.equals(DottedChain.NAMESPACE))) {
            isNs = true;
        }

        if (val instanceof StringAVM2Item) {
            StringAVM2Item sval = (StringAVM2Item) val;
            if (isNs) {
                return new ValueKind(namespace(generatedNs ? Namespace.KIND_PACKAGE_INTERNAL : Namespace.KIND_NAMESPACE, sval.getValue()), ValueKind.CONSTANT_Namespace);
            } else {
                return new ValueKind(str(sval.getValue()), ValueKind.CONSTANT_Utf8);
            }
        }
        if (val instanceof IntegerValueAVM2Item) {
            return new ValueKind(abcIndex.getSelectedAbc().constants.getIntId(((IntegerValueAVM2Item) val).value, true), ValueKind.CONSTANT_Int);
        }
        if (val instanceof DoubleValueAVM2Item) {
            return new ValueKind(abcIndex.getSelectedAbc().constants.getDoubleId(((DoubleValueAVM2Item) val).value, true), ValueKind.CONSTANT_Double);
        }
        if (val instanceof DecimalValueAVM2Item) {
            return new ValueKind(abcIndex.getSelectedAbc().constants.getDecimalId(((DecimalValueAVM2Item) val).value, true), ValueKind.CONSTANT_DecimalOrFloat);
        }
        if (val instanceof FloatValueAVM2Item) {
            return new ValueKind(abcIndex.getSelectedAbc().constants.getFloatId(((FloatValueAVM2Item) val).value, true), ValueKind.CONSTANT_DecimalOrFloat);
        }
        if (val instanceof Float4ValueAVM2Item) {
            return new ValueKind(abcIndex.getSelectedAbc().constants.getFloat4Id(((Float4ValueAVM2Item) val).value, true), ValueKind.CONSTANT_Float4);
        }
        if (val instanceof NanAVM2Item) {
            return new ValueKind(abcIndex.getSelectedAbc().constants.getDoubleId(Double.NaN, true), ValueKind.CONSTANT_Double);
        }
        if (val instanceof NullAVM2Item) {
            return new ValueKind(ValueKind.CONSTANT_Null, ValueKind.CONSTANT_Null);
        }
        if (val instanceof UndefinedAVM2Item) {
            return new ValueKind(ValueKind.CONSTANT_Undefined, ValueKind.CONSTANT_Undefined);
        }

        //TODO: the compiler should check whether default value is compatible with its type
        //Flash CS6 does this, example message: "Incompatible default value of type int where String is expected."
        //However a negative integer value on uint type silently loses its negation. Weird.
        return null;
    }

    public int genNs(List<DottedChain> importedClasses, DottedChain pkg, NamespaceItem ns, List<NamespaceItem> openedNamespaces, SourceGeneratorLocalData localData, int line) throws CompilationException {
        ns.resolveCustomNs(abcIndex, importedClasses, pkg, openedNamespaces, localData);
        return ns.getCpoolIndex(abcIndex);
    }

    /**
     * Generates traits phase 2.
     * @param importedClasses Imported classes
     * @param pkg Package
     * @param items Items
     * @param traits Traits
     * @param openedNamespaces Opened namespaces
     * @param localData Local data
     * @throws CompilationException On compilation error
     */
    public void generateTraitsPhase2(List<DottedChain> importedClasses, DottedChain pkg, List<GraphTargetItem> items, Trait[] traits, List<NamespaceItem> openedNamespaces, SourceGeneratorLocalData localData) throws CompilationException {
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                //empty
            } else if (item instanceof InterfaceAVM2Item) {
                traits[k].name_index = traitName(((InterfaceAVM2Item) item).pkg == null ? 0 : ((InterfaceAVM2Item) item).pkg.getCpoolIndex(abcIndex), ((InterfaceAVM2Item) item).baseName);
            } else if (item instanceof ClassAVM2Item) {
                traits[k].name_index = traitName(((ClassAVM2Item) item).pkg == null ? 0 : ((ClassAVM2Item) item).pkg.getCpoolIndex(abcIndex), ((ClassAVM2Item) item).classBaseName);
            } else if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                traits[k].name_index = traitName(genNs(importedClasses, pkg, ((MethodAVM2Item) item).pkg, openedNamespaces, localData, ((MethodAVM2Item) item).line), ((MethodAVM2Item) item).functionName);
            } else if (item instanceof FunctionAVM2Item) {
                traits[k].name_index = traitName(((FunctionAVM2Item) item).pkg == null ? 0 : ((FunctionAVM2Item) item).pkg.getCpoolIndex(abcIndex), ((FunctionAVM2Item) item).functionName);
            } else if (item instanceof ConstAVM2Item) {
                traits[k].name_index = traitName(genNs(importedClasses, pkg, ((ConstAVM2Item) item).pkg, openedNamespaces, localData, ((ConstAVM2Item) item).line), ((ConstAVM2Item) item).var);
            } else if (item instanceof SlotAVM2Item) {
                traits[k].name_index = traitName(genNs(importedClasses, pkg, ((SlotAVM2Item) item).pkg, openedNamespaces, localData, ((SlotAVM2Item) item).line), ((SlotAVM2Item) item).var);
            }
        }

        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }
            if (item instanceof ClassAVM2Item) {
                localData.currentClassBaseName = ((ClassAVM2Item) item).classBaseName;
                InstanceInfo instanceInfo = abcIndex.getSelectedAbc().instance_info.get(((TraitClass) traits[k]).class_info);
                instanceInfo.name_index = abcIndex.getSelectedAbc().constants.getMultinameId(
                        Multiname.createQName(
                                false,
                                abcIndex.getSelectedAbc().constants.getStringId(((ClassAVM2Item) item).classBaseName, true),
                                ((ClassAVM2Item) item).pkg.getCpoolIndex(abcIndex)), true);

                localData.currentClassBaseName = null;
            }
            if (item instanceof InterfaceAVM2Item) {
                localData.currentClassBaseName = ((InterfaceAVM2Item) item).baseName;
                ABC abc = abcIndex.getSelectedAbc();
                AVM2ConstantPool constants = abc.constants;
                InstanceInfo instanceInfo = abc.instance_info.get(((TraitClass) traits[k]).class_info);
                instanceInfo.name_index = constants.getMultinameId(Multiname.createQName(false, constants.getStringId(((InterfaceAVM2Item) item).baseName, true),
                        ((InterfaceAVM2Item) item).pkg.getCpoolIndex(abcIndex)), true);

                localData.currentClassBaseName = null;
            }
        }
    }

    /**
     * Generates traits phase 3.
     * @param items Items
     * @param traits Traits
     * @param localData Local data
     * @throws CompilationException On compilation error
     */
    public void generateTraitsPhase3(List<GraphTargetItem> items, Trait[] traits, SourceGeneratorLocalData localData) throws CompilationException {
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }
            if (item instanceof ClassAVM2Item) {
                int minClassIndex = -1;
                localData.currentClassBaseName = ((ClassAVM2Item) item).classBaseName;
                InstanceInfo instanceInfo = abcIndex.getSelectedAbc().instance_info.get(((TraitClass) traits[k]).class_info);
                ABC abc = abcIndex.getSelectedAbc();
                if (((ClassAVM2Item) item).extendsOp != null) {
                    instanceInfo.super_index = typeName(localData, ((ClassAVM2Item) item).extendsOp);
                } else {
                    instanceInfo.super_index = abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createQName(false, str("Object"), namespace(Namespace.KIND_PACKAGE, "")), true);
                }
                if (instanceInfo.super_index != 0) {
                    int foundClass = abc.findClassByName(abc.constants.getMultiname(instanceInfo.super_index).getNameWithNamespace(abc.constants, true));
                    if (foundClass > -1) {
                        if (foundClass > minClassIndex) {
                            minClassIndex = foundClass;
                        }
                    }
                }
                instanceInfo.interfaces = new int[((ClassAVM2Item) item).implementsOp.size()];
                for (int i = 0; i < ((ClassAVM2Item) item).implementsOp.size(); i++) {
                    instanceInfo.interfaces[i] = superIntName(localData, ((ClassAVM2Item) item).implementsOp.get(i));
                    int foundIface = abc.findClassByName(abc.constants.getMultiname(instanceInfo.interfaces[i]).getNameWithNamespace(abc.constants, true));
                    if (foundIface > -1) {
                        if (foundIface > minClassIndex) {
                            minClassIndex = foundIface;
                        }
                    }
                }
                int oldClassIndex = ((TraitClass) traits[k]).class_info;
                if (oldClassIndex <= minClassIndex) {
                    abc.moveClass(oldClassIndex, minClassIndex + 1);
                }
                localData.currentClassBaseName = null;
            }
            if (item instanceof InterfaceAVM2Item) {
                int minClassIndex = -1;
                localData.currentClassBaseName = ((InterfaceAVM2Item) item).baseName;
                ABC abc = abcIndex.getSelectedAbc();
                InstanceInfo instanceInfo = abc.instance_info.get(((TraitClass) traits[k]).class_info);
                instanceInfo.interfaces = new int[((InterfaceAVM2Item) item).superInterfaces.size()];
                for (int i = 0; i < ((InterfaceAVM2Item) item).superInterfaces.size(); i++) {
                    GraphTargetItem un = ((InterfaceAVM2Item) item).superInterfaces.get(i);
                    instanceInfo.interfaces[i] = superIntName(localData, un);

                    int foundIface = abc.findClassByName(abc.constants.getMultiname(instanceInfo.interfaces[i]).getNameWithNamespace(abc.constants, true));
                    if (foundIface > -1) {
                        if (foundIface > minClassIndex) {
                            minClassIndex = foundIface;
                        }
                    }
                }
                int oldClassIndex = ((TraitClass) traits[k]).class_info;
                if (oldClassIndex <= minClassIndex) {
                    abc.moveClass(oldClassIndex, minClassIndex + 1);
                }
                localData.currentClassBaseName = null;
            }
        }
    }

    /**
     * Gets super interface name.
     * @param localData Local data
     * @param un Unresolved item
     * @return Multiname index
     * @throws CompilationException On compilation error
     */
    public int superIntName(SourceGeneratorLocalData localData, GraphTargetItem un) throws CompilationException {
        if (un instanceof UnresolvedAVM2Item) {
            ((UnresolvedAVM2Item) un).resolve(localData, localData.getFullClass(), null, new ArrayList<>(), new ArrayList<>(), abcIndex, new ArrayList<>(), new ArrayList<>());
            un = ((UnresolvedAVM2Item) un).resolved;
        }
        if (!(un instanceof TypeItem)) { //not applyType
            throw new CompilationException("Invalid type", 0);
        }
        TypeItem sup = (TypeItem) un;
        int propId = resolveType(localData, sup, abcIndex);
        int[] nss = new int[]{abcIndex.getSelectedAbc().constants.getMultiname(propId).namespace_index};
        return abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createMultiname(false, abcIndex.getSelectedAbc().constants.getMultiname(propId).name_index, abcIndex.getSelectedAbc().constants.getNamespaceSetId(nss, true)), true);

    }

    /**
     * Generates metadata
     * @param metadata Metadata
     * @param abc ABC
     * @return Metadata indices
     */
    public int[] generateMetadata(List<Map.Entry<String, Map<String, String>>> metadata, ABC abc) {
        List<Integer> retList = new ArrayList<>();
        for (int i = 0; i < metadata.size(); i++) {
            Map.Entry<String, Map<String, String>> en = metadata.get(i);
            if ("API".equals(en.getKey()) && abc.isApiVersioned()) {
                continue;
            }
            int[] keys = new int[en.getValue().size()];
            int[] values = new int[en.getValue().size()];
            int j = 0;
            for (String key : en.getValue().keySet()) {
                keys[j] = abcIndex.getSelectedAbc().constants.getStringId(key, true);
                values[j] = abcIndex.getSelectedAbc().constants.getStringId(en.getValue().get(key), true);
                j++;
            }
            MetadataInfo mi = new MetadataInfo(abcIndex.getSelectedAbc().constants.getStringId(en.getKey(), true), keys, values);
            retList.add(abcIndex.getSelectedAbc().metadata_info.size());
            abcIndex.getSelectedAbc().metadata_info.add(mi);
        }
        int[] ret = new int[retList.size()];
        for (int i = 0; i < retList.size(); i++) {
            ret[i] = retList.get(i);
        }
        return ret;
    }

    /**
     * Generates traits phase 4.
     * @param importedClasses Imported classes
     * @param openedNamespaces Opened namespaces
     * @param methodInitScope Method init scope
     * @param isInterface Is interface
     * @param className Class name
     * @param superName Super name
     * @param generateStatic Generate static
     * @param localData Local data
     * @param items Items
     * @param ts Traits
     * @param traits Traits
     * @param initScopes Init scopes
     * @param class_index Class index
     * @param isScriptTraits Is script traits
     * @throws AVM2ParseException On parse error
     * @throws CompilationException On compilation error
     */
    public void generateTraitsPhase4(List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, int methodInitScope, boolean isInterface, String className, String superName, boolean generateStatic, SourceGeneratorLocalData localData, List<GraphTargetItem> items, Traits ts, Trait[] traits, Map<Trait, Integer> initScopes, Reference<Integer> class_index, boolean isScriptTraits) throws AVM2ParseException, CompilationException {

        //Note: Names must be generated first before accessed in inner subs
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }

            if ((item instanceof SlotAVM2Item) || (item instanceof ConstAVM2Item)) {
                TraitSlotConst tsc = (TraitSlotConst) traits[k];
                GraphTargetItem val = null;
                GraphTargetItem type = null;
                boolean isNamespace = false;
                int namespace = 0;
                boolean isStatic = false;
                int[] metadata = new int[0];
                if (item instanceof SlotAVM2Item) {
                    SlotAVM2Item sai = (SlotAVM2Item) item;
                    if (!isScriptTraits && sai.isStatic() != generateStatic) {
                        continue;
                    }
                    val = sai.value;
                    type = sai.type;
                    isStatic = sai.isStatic();
                    if (sai.pkg != null) {
                        sai.pkg.resolveCustomNs(abcIndex, importedClasses, localData.pkg, openedNamespaces, localData);
                    }
                    namespace = sai.pkg == null ? 0 : sai.pkg.getCpoolIndex(abcIndex);
                }
                boolean generatedNs = false;
                if (item instanceof ConstAVM2Item) {
                    ConstAVM2Item cai = (ConstAVM2Item) item;
                    generatedNs = cai.generatedNs;
                    if (!isScriptTraits && cai.isStatic() != generateStatic) {
                        continue;
                    }
                    val = cai.value;
                    type = cai.type;
                    if (cai.pkg != null) {
                        cai.pkg.resolveCustomNs(abcIndex, importedClasses, localData.pkg, openedNamespaces, localData);
                    }
                    namespace = cai.pkg == null ? 0 : cai.pkg.getCpoolIndex(abcIndex);
                    isNamespace = type.toString().equals("Namespace");
                    isStatic = cai.isStatic();
                }
                tsc.type_index = isNamespace ? 0 : (type == null ? 0 : typeName(localData, type));

                ValueKind vk = getValueKind(namespace, type, val, generatedNs);
                if (vk == null) {
                    tsc.value_index = ValueKind.CONSTANT_Undefined;
                    tsc.value_kind = ValueKind.CONSTANT_Undefined;
                } else {
                    tsc.value_kind = vk.value_kind;
                    tsc.value_index = vk.value_index;
                }
            }

            if (item instanceof InterfaceAVM2Item) {
                ABC abc = abcIndex.getSelectedAbc();
                TraitClass trait = (TraitClass) traits[k];
                InterfaceAVM2Item iitem = (InterfaceAVM2Item) item;
                generateClass(iitem.pkg.getCpoolIndex(abcIndex), abc.class_info.get(trait.class_info), abc.instance_info.get(trait.class_info), initScopes.get(trait), iitem.pkg.name, localData, iitem, class_index);
            }

            if (item instanceof ClassAVM2Item) {
                ABC abc = abcIndex.getSelectedAbc();
                TraitClass trait = (TraitClass) traits[k];
                ClassAVM2Item citem = (ClassAVM2Item) item;
                generateClass(citem.pkg.getCpoolIndex(abcIndex), abc.class_info.get(trait.class_info), abc.instance_info.get(trait.class_info), initScopes.get(trait), citem.pkg.name, localData, citem, class_index);
            }
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (!isScriptTraits && mai.isStatic() != generateStatic) {
                    continue;
                }
                for (List<NamespaceItem> ln : mai.allOpenedNamespaces) {
                    for (NamespaceItem n : ln) {
                        n.resolveCustomNs(abcIndex, importedClasses, localData.pkg, ln, localData);
                    }
                }
                String suffix = null;
                if (item instanceof GetterAVM2Item) {
                    suffix = "get";
                }
                if (item instanceof SetterAVM2Item) {
                    suffix = "set";
                }

                ((TraitMethodGetterSetter) traits[k]).method_info = methodHeader(mai.isStatic(), methodName(mai.outsidePackage, localData.pkg, mai.functionName, mai.pkg, className, mai.customNamespace, suffix), false, isInterface, mai.isNative, new ArrayList<>(), localData.pkg, mai.needsActivation, mai.subvariables, methodInitScope + (mai.isStatic() ? 0 : 1), mai.hasRest, mai.line, className, superName, false, localData, mai.paramTypes, mai.paramNames, mai.paramValues, mai.body, mai.retType);
            } else if (item instanceof FunctionAVM2Item) {
                FunctionAVM2Item fai = (FunctionAVM2Item) item;
                ((TraitFunction) traits[k]).method_info = methodHeader(false, methodName(false/*?*/, localData.pkg, fai.functionName, fai.pkg, null, null, ""), false, isInterface, fai.isNative, new ArrayList<>(), localData.pkg, fai.needsActivation, fai.subvariables, methodInitScope, fai.hasRest, fai.line, className, superName, false, localData, fai.paramTypes, fai.paramNames, fai.paramValues, fai.body, fai.retType);
            }
        }
        
        abcIndex.refreshSelected();
        
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }          
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (!isScriptTraits && mai.isStatic() != generateStatic) {
                    continue;
                }
                for (List<NamespaceItem> ln : mai.allOpenedNamespaces) {
                    for (NamespaceItem n : ln) {
                        n.resolveCustomNs(abcIndex, importedClasses, localData.pkg, ln, localData);
                    }
                }
                String suffix = null;
                if (item instanceof GetterAVM2Item) {
                    suffix = "get";
                }
                if (item instanceof SetterAVM2Item) {
                    suffix = "set";
                }

                ((TraitMethodGetterSetter) traits[k]).method_info = method(((TraitMethodGetterSetter) traits[k]).method_info, mai.isStatic(), methodName(mai.outsidePackage, localData.pkg, mai.functionName, mai.pkg, className, mai.customNamespace, suffix), false, isInterface, mai.isNative, new ArrayList<>(), localData.pkg, mai.needsActivation, mai.subvariables, methodInitScope + (mai.isStatic() ? 0 : 1), mai.hasRest, mai.line, className, superName, false, localData, mai.paramTypes, mai.paramNames, mai.paramValues, mai.body, mai.retType);
            } else if (item instanceof FunctionAVM2Item) {
                FunctionAVM2Item fai = (FunctionAVM2Item) item;
                ((TraitFunction) traits[k]).method_info = method(((TraitFunction) traits[k]).method_info, false, methodName(false/*?*/, localData.pkg, fai.functionName, fai.pkg, null, null, ""), false, isInterface, fai.isNative, new ArrayList<>(), localData.pkg, fai.needsActivation, fai.subvariables, methodInitScope, fai.hasRest, fai.line, className, superName, false, localData, fai.paramTypes, fai.paramNames, fai.paramValues, fai.body, fai.retType);
            }
        }
    }

    private int methodName(boolean outsidePkg, DottedChain pkg, String methodName, NamespaceItem ns, String className, String customNs, String typeSuffix) {
        StringBuilder sb = new StringBuilder();
        /*if (ns != null) {
         sb.append(ns.baseName.toRawString());
         }*/
        if (className != null) {
            if (pkg != null && !pkg.isEmpty() && !pkg.isTopLevel()) {
                sb.append(pkg.toRawString());
                sb.append(":");
            }
            sb.append(className);
        }
        if (customNs != null) {
            sb.append(customNs);
        } else if (ns != null) {
            switch (ns.kind) {
                case Namespace.KIND_PACKAGE_INTERNAL:
                    sb.append(pkg == null ? "" /*?*/ : pkg.toRawString());
                    break;
                case Namespace.KIND_PRIVATE:

                    if (!outsidePkg) {
                        sb.append("/private");
                    }
                    break;
                case Namespace.KIND_PROTECTED:
                case Namespace.KIND_STATIC_PROTECTED:
                    sb.append("/protected");
                    break;
            }
        }
        sb.append("/");
        sb.append(methodName);
        if (typeSuffix != null && !typeSuffix.isEmpty()) {
            sb.append("/");
            sb.append(typeSuffix);
        }
        return abcIndex.getSelectedAbc().constants.getStringId(sb.toString(), true);
    }

    /**
     * Generates traits phase 1.
     * @param importedClasses Imported classes
     * @param openedNamespaces Opened namespaces
     * @param className Class name
     * @param superName Super name
     * @param generateStatic Generate static
     * @param localData Local data
     * @param items Items
     * @param ts Traits
     * @param classIndex Class index
     * @param isScriptTraits Is script traits
     * @return Traits
     * @throws AVM2ParseException On parse error
     * @throws CompilationException On compilation error
     */
    public Trait[] generateTraitsPhase1(List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, String className, String superName, boolean generateStatic, SourceGeneratorLocalData localData, List<GraphTargetItem> items, Traits ts, Reference<Integer> classIndex, boolean isScriptTraits) throws AVM2ParseException, CompilationException {
        Trait[] traits = new Trait[items.size()];
        int slot_id = 1;
        int disp_id = 3; //1 and 2 are for constructor
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (item instanceof InterfaceAVM2Item) {
                TraitClass tc = new TraitClass();
                ClassInfo ci = new ClassInfo();
                InstanceInfo ii = new InstanceInfo();
                /*abc.class_info.add(ci);
                 abc.instance_info.add(ii);*/
                tc.class_info = classIndex.getVal();
                abcIndex.getSelectedAbc().addClass(ci, ii, classIndex.getVal());
                classIndex.setVal(classIndex.getVal() + 1);
                ii.flags |= InstanceInfo.CLASS_INTERFACE;
                //ii.name_index = traitName(((InterfaceAVM2Item) item).namespace, ((InterfaceAVM2Item) item).baseName);
                //tc.class_info = abc.instance_info.size() - 1;
                tc.kindType = Trait.TRAIT_CLASS;
                //tc.name_index = traitName(((InterfaceAVM2Item) item).namespace, ((InterfaceAVM2Item) item).baseName);
                tc.slot_id = 0; //?
                ts.traits.add(tc);
                traits[k] = tc;
                traits[k].metadata = generateMetadata(((InterfaceAVM2Item) item).metadata, abcIndex.getSelectedAbc());
                if (traits[k].metadata.length > 0) {
                    traits[k].kindFlags |= Trait.ATTR_Metadata;
                }
            }

            if (item instanceof ClassAVM2Item) {
                TraitClass tc = new TraitClass();
                ClassInfo ci = new ClassInfo();
                InstanceInfo ii = new InstanceInfo();
                //ii.name_index = traitName(((ClassAVM2Item) item).namespace, ((ClassAVM2Item) item).classBaseName);
                /*abc.class_info.add(ci);
                 abc.instance_info.add(instanceInfo);*/
                tc.class_info = classIndex.getVal();
                abcIndex.getSelectedAbc().addClass(ci, ii, classIndex.getVal());
                classIndex.setVal(classIndex.getVal() + 1);
                tc.kindType = Trait.TRAIT_CLASS;
                // tc.name_index = traitName(((ClassAVM2Item) item).namespace, ((ClassAVM2Item) item).classBaseName);
                tc.slot_id = slot_id++;
                ts.traits.add(tc);
                traits[k] = tc;
                traits[k].metadata = generateMetadata(((ClassAVM2Item) item).metadata, abcIndex.getSelectedAbc());
                if (traits[k].metadata.length > 0) {
                    traits[k].kindFlags |= Trait.ATTR_Metadata;
                }
            }
            if ((item instanceof SlotAVM2Item) || (item instanceof ConstAVM2Item)) {
                TraitSlotConst tsc = new TraitSlotConst();
                tsc.kindType = (item instanceof SlotAVM2Item) ? Trait.TRAIT_SLOT : Trait.TRAIT_CONST;
                String var = null;
                //GraphTargetItem val = null;
                GraphTargetItem type = null;
                boolean isNamespace = false;
                int namespace = 0;
                boolean isStatic = false;
                int[] metadata = new int[0];
                if (item instanceof SlotAVM2Item) {
                    SlotAVM2Item sai = (SlotAVM2Item) item;
                    if (!isScriptTraits && sai.isStatic() != generateStatic) {
                        continue;
                    }
                    var = sai.var;
                    //val = sai.value;
                    type = sai.type;
                    isStatic = sai.isStatic();
                    if (sai.pkg != null) {
                        sai.pkg.resolveCustomNs(abcIndex, importedClasses, localData.pkg, openedNamespaces, localData);
                    }
                    namespace = sai.pkg == null ? 0 : sai.pkg.getCpoolIndex(abcIndex);
                    metadata = generateMetadata(((SlotAVM2Item) item).metadata, abcIndex.getSelectedAbc());
                }
                boolean generatedNs = false;
                if (item instanceof ConstAVM2Item) {
                    ConstAVM2Item cai = (ConstAVM2Item) item;
                    generatedNs = cai.generatedNs;
                    if (!isScriptTraits && cai.isStatic() != generateStatic) {
                        continue;
                    }
                    var = cai.var;
                    //val = cai.value;
                    type = cai.type;
                    if (cai.pkg != null) {
                        cai.pkg.resolveCustomNs(abcIndex, importedClasses, localData.pkg, openedNamespaces, localData);
                    }
                    namespace = cai.pkg == null ? 0 : cai.pkg.getCpoolIndex(abcIndex);
                    isNamespace = type.toString().equals("Namespace");
                    isStatic = cai.isStatic();
                    metadata = generateMetadata(((ConstAVM2Item) item).metadata, abcIndex.getSelectedAbc());
                }
                if (isNamespace) {
                    tsc.name_index = traitName(namespace, var);
                }
                //tsc.type_index = isNamespace ? 0 : (type == null ? 0 : typeName(localData, type));

                /*ValueKind vk = getValueKind(namespace, type, val, generatedNs);
                if (vk == null) {
                    tsc.value_index = ValueKind.CONSTANT_Undefined;
                    tsc.value_kind = ValueKind.CONSTANT_Undefined;
                } else {
                    tsc.value_kind = vk.value_kind;
                    tsc.value_index = vk.value_index;
                }*/
                tsc.slot_id = !isScriptTraits && isStatic ? slot_id++ : 0;
                ts.traits.add(tsc);
                traits[k] = tsc;
                traits[k].metadata = metadata;
                if (traits[k].metadata.length > 0) {
                    traits[k].kindFlags |= Trait.ATTR_Metadata;
                }
            }
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (!isScriptTraits && mai.isStatic() != generateStatic) {
                    continue;
                }
                TraitMethodGetterSetter tmgs = new TraitMethodGetterSetter();
                tmgs.kindType = (item instanceof GetterAVM2Item) ? Trait.TRAIT_GETTER : ((item instanceof SetterAVM2Item) ? Trait.TRAIT_SETTER : Trait.TRAIT_METHOD);
                tmgs.disp_id = mai.isStatic() ? disp_id++ : 0; //For a reason, there is disp_id only for static methods (or not?)
                if (mai.isFinal() || (className != null && mai.isStatic())) {
                    tmgs.kindFlags |= Trait.ATTR_Final;
                }
                if (mai.isOverride()) {
                    tmgs.kindFlags |= Trait.ATTR_Override;
                }
                ts.traits.add(tmgs);

                traits[k] = tmgs;
                traits[k].metadata = generateMetadata(((MethodAVM2Item) item).metadata, abcIndex.getSelectedAbc());
                if (traits[k].metadata.length > 0) {
                    traits[k].kindFlags |= Trait.ATTR_Metadata;
                }
            }
            /*else if (item instanceof FunctionAVM2Item) {
             TraitFunction tf = new TraitFunction();
             tf.slot_id = slot_id++;
             tf.kindType = Trait.TRAIT_FUNCTION;
             //tf.name_index = traitName(((FunctionAVM2Item) item).namespace, ((FunctionAVM2Item) item).functionName);
             ts.traits.add(tf);
             traits[k] = tf;
             traits[k].metadata = generateMetadata(((FunctionAVM2Item) item).metadata);
             }*/

        }

        return traits;
    }

    /**
     * Generates script info.
     * @param sinitVariables Script initializer variables
     * @param sinitNeedsActivation Script initializer needs activation
     * @param importedClasses Imported classes
     * @param openedNamespaces Opened namespaces
     * @param scriptIndex Script index
     * @param scriptInfo Script info
     * @param allOpenedNamespaces All opened namespaces
     * @param localData Local data
     * @param commands Commands
     * @param classPos Class position
     * @throws AVM2ParseException On parse error
     * @throws CompilationException On compilation error
     */
    public void generateScriptInfo(List<AssignableAVM2Item> sinitVariables, boolean sinitNeedsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, int scriptIndex, ScriptInfo scriptInfo, List<List<NamespaceItem>> allOpenedNamespaces, SourceGeneratorLocalData localData, List<GraphTargetItem> commands, int classPos) throws AVM2ParseException, CompilationException {
        List<GraphTargetItem> traitsList = new ArrayList<>();
        for (GraphTargetItem it : commands) {
            if ((it instanceof SlotAVM2Item) 
                    || (it instanceof ConstAVM2Item)
                    || (it instanceof ClassAVM2Item)
                    || (it instanceof InterfaceAVM2Item)
                    || (it instanceof MethodAVM2Item)
                    || (it instanceof GetterAVM2Item)
                    || (it instanceof SetterAVM2Item)
                    ) {
                traitsList.add(it);
            }
        }
        Reference<Integer> class_index = new Reference<>(classPos);
        localData.currentScript = scriptInfo;
        localData.scriptIndex = scriptIndex;
        Trait[] traitArr = generateTraitsPhase1(new ArrayList<>(), new ArrayList<>(), null, null, true, localData, traitsList, scriptInfo.traits, class_index, true);
        generateTraitsPhase2(new ArrayList<>(), null/*FIXME*/, traitsList, traitArr, new ArrayList<>(), localData);
        abcIndex.refreshSelected();
        generateTraitsPhase3(traitsList, traitArr, localData);
        abcIndex.refreshSelected();

        ABC abc = abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abc.constants;
        MethodInfo mi = new MethodInfo(new int[0], 0, constants.getStringId("", true), 0, new ValueKind[0], new int[0]);
        MethodBody mb;
        //= new MethodBody(abc, new Traits(), new byte[0], new ABCException[0]);
        //mb.method_info = abc.addMethodInfo(mi);
  //      mb.setCode(new AVM2Code());
        
        localData.importedClasses = importedClasses;
        localData.openedNamespaces = openedNamespaces;
        int sinit_index = method(null, true, str(""), false, false, false, new ArrayList<>(), null, sinitNeedsActivation, sinitVariables, 0, false, 0, null, null, false, localData, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), commands, TypeItem.UNBOUNDED);
        mb = abcIndex.getSelectedAbc().findBody(sinit_index);

        
        List<AVM2Instruction> mbCode = mb.getCode().code;
        /*mbCode.add(ins(AVM2Instructions.GetLocal0));
        mbCode.add(ins(AVM2Instructions.PushScope));
*/
        int traitScope = 1;

        String documentClassStr = localData.documentClass;
        DottedChain documentClass = null;
        if (documentClassStr != null && !documentClassStr.isEmpty()) {
            documentClass = DottedChain.parseNoSuffix(documentClassStr);
        }

        Map<Trait, Integer> initScopes = new HashMap<>();

        List<AVM2Instruction> sinitcode = new ArrayList<>();
        for (Trait t : scriptInfo.traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                DottedChain className = tc.getName(abc).getNameWithNamespace(abc.constants, true);

                List<Integer> parents = new ArrayList<>();
                if (documentClass != null && documentClass.equals(className)) {
                    sinitcode.add(ins(AVM2Instructions.GetScopeObject, 0));
                } else {
                    int[] nsset = new int[]{constants.getMultiname(tc.name_index).namespace_index};
                    sinitcode.add(ins(AVM2Instructions.FindPropertyStrict, constants.getMultinameId(Multiname.createMultiname(false, constants.getMultiname(tc.name_index).name_index, constants.getNamespaceSetId(nsset, true)), true)));
                }
                traitScope++;
                if (abc.instance_info.get(tc.class_info).isInterface()) {
                    sinitcode.add(ins(AVM2Instructions.PushNull));
                } else {

                    AbcIndexing.ClassIndex ci = abcIndex.findClass(AbcIndexing.multinameToType(abc.instance_info.get(tc.class_info).name_index, constants), abc, scriptIndex);
                    while (ci != null && ci.parent != null) {
                        ci = ci.parent;
                        Multiname origM = ci.abc.constants.getMultiname(ci.abc.instance_info.get(ci.index).name_index);
                        Namespace origNs = ci.abc.constants.getNamespace(origM.namespace_index);
                        if (origM.kind == Multiname.QNAME || origM.kind == Multiname.QNAMEA) {
                            parents.add(constants.getMultinameId(
                                    Multiname.createQName(origM.kind == Multiname.QNAMEA,
                                            constants.getStringId(ci.abc.constants.getString(origM.name_index), true),
                                            constants.getNamespaceId(origNs.kind,
                                                    ci.abc.constants.getString(origNs.name_index), 0, true)), true));
                        }
                    }

                    //add all parent objects to scopestack
                    for (int i = parents.size() - 1; i >= 0; i--) {
                        sinitcode.add(ins(AVM2Instructions.GetLex, parents.get(i)));
                        sinitcode.add(ins(AVM2Instructions.PushScope));
                        traitScope++;
                    }
                    //direct parent class to new_class instruction
                    if (!parents.isEmpty()) { //NON EXISTING PARENT CLASS - TODO: handle as error!
                        sinitcode.add(ins(AVM2Instructions.GetLex, parents.get(0)));
                    }
                }
                sinitcode.add(ins(AVM2Instructions.NewClass, tc.class_info));
                for (int i = 0; i < parents.size(); i++) {
                    sinitcode.add(ins(AVM2Instructions.PopScope));
                }

                sinitcode.add(ins(AVM2Instructions.InitProperty, tc.name_index));
                initScopes.put(t, traitScope);
                traitScope = 1;
            }
        }

        mbCode.addAll(2, sinitcode); //after getlocal0 pushscope
        
        //abc.addMethodBody(mb);
        scriptInfo.init_index = mb.method_info;
        localData.pkg = DottedChain.EMPTY;
        //localData.registerVars.put("this", 0);
        generateTraitsPhase4(new ArrayList<>(), new ArrayList<>(), 1/*??*/, false, null, null, true, localData, traitsList, scriptInfo.traits, traitArr, initScopes, class_index, true);              
        
        mb.markOffsets();
        mb.autoFillStats(abc, 1, false);

    }

    /**
     * Parent names add names.
     * @param abc ABC
     * @param name_index Name index
     * @param indices Indices
     * @param names Names
     * @param namespaces Namespaces
     */
    public static void parentNamesAddNames(AbcIndexing abc, int scriptIndex, int name_index, List<Integer> indices, List<String> names, List<String> namespaces) {
        List<Integer> cindices = new ArrayList<>();

        List<ABC> outABCs = new ArrayList<>();
        parentNames(abc, scriptIndex, name_index, cindices, names, namespaces, outABCs);
        for (int i = 0; i < cindices.size(); i++) {
            ABC a = outABCs.get(i);
            int m = cindices.get(i);
            if (a == abc.getSelectedAbc()) {
                indices.add(m);
                continue;
            }
            Multiname superName = a.constants.getMultiname(m);
            indices.add(
                    abc.getSelectedAbc().constants.getMultinameId(
                            Multiname.createQName(false,
                                    abc.getSelectedAbc().constants.getStringId(superName.getName(a.constants, null, true, true /*FIXME!!! ???*/), true),
                                    abc.getSelectedAbc().constants.getNamespaceId(superName.getNamespace(a.constants).kind, superName.getNamespace(a.constants).getName(a.constants), 0, true)), true)
            );
        }
    }

    /**
     * Gets trait return type.
     * @param abc ABC
     * @param t Trait
     * @return Trait return type
     */
    public static GraphTargetItem getTraitReturnType(AbcIndexing abc, Trait t) {
        if (t instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) t;
            if (tsc.type_index == 0) {
                return TypeItem.UNBOUNDED;
            }
            return AbcIndexing.multinameToType(tsc.type_index, abc.getSelectedAbc().constants);
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
            if (tmgs.kindType == Trait.TRAIT_GETTER) {
                return AbcIndexing.multinameToType(abc.getSelectedAbc().method_info.get(tmgs.method_info).ret_type, abc.getSelectedAbc().constants);
            }
            if (tmgs.kindType == Trait.TRAIT_SETTER) {
                if (abc.getSelectedAbc().method_info.get(tmgs.method_info).param_types.length > 0) {
                    return AbcIndexing.multinameToType(abc.getSelectedAbc().method_info.get(tmgs.method_info).param_types[0], abc.getSelectedAbc().constants);
                } else {
                    return TypeItem.UNBOUNDED;
                }
            }
        }
        if (t instanceof TraitFunction) {
            return new TypeItem(DottedChain.FUNCTION);
        }
        return TypeItem.UNBOUNDED;
    }

    /**
     * Searches prototype chain.
     * @param namespaceSuffix Namespace suffix
     * @param otherNs Other namespaces
     * @param privateNs Private namespace
     * @param protectedNs Protected namespace
     * @param instanceOnly Instance only
     * @param abc ABC
     * @param pkg Package
     * @param obj Object
     * @param propertyName Property name
     * @param outName Out name
     * @param outNs Out namespace
     * @param outPropNs Out property namespace
     * @param outPropNsKind Out property namespace kind
     * @param outPropNsIndex Out property namespace index
     * @param outPropType Out property type
     * @param outPropValue Out property value
     * @param outPropValueAbc Out property value ABC
     * @param isType Is type
     * @return True if found
     */
    public static boolean searchPrototypeChain(Integer namespaceSuffix, List<Integer> otherNs, int privateNs, int protectedNs, boolean instanceOnly, AbcIndexing abc, DottedChain pkg, String obj, String propertyName, Reference<String> outName, Reference<DottedChain> outNs, Reference<DottedChain> outPropNs, Reference<Integer> outPropNsKind, Reference<Integer> outPropNsIndex, Reference<GraphTargetItem> outPropType, Reference<ValueKind> outPropValue, Reference<ABC> outPropValueAbc, Reference<Boolean> isType, Reference<Trait> outPropTrait) {
        // private and protected namespaces first so we find overriding functions before overridden functions
        if (namespaceSuffix != null) {
            if (searchPrototypeChain(namespaceSuffix, instanceOnly, abc, pkg, obj, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait)) {
                return true;
            }
        }
        if (searchPrototypeChain(privateNs, instanceOnly, abc, pkg, obj, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait)) {
            return true;
        }
        if (searchPrototypeChain(protectedNs, instanceOnly, abc, pkg, obj, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait)) {
            return true;
        }

        for (int ns : otherNs) {
            if (searchPrototypeChain(ns, instanceOnly, abc, pkg, obj, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait)) {
                return true;
            }
        }

        return searchPrototypeChain(0, instanceOnly, abc, pkg, obj, propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc, isType, outPropTrait);
    }

    private static boolean searchPrototypeChain(int selectedNs, boolean instanceOnly, AbcIndexing abc, DottedChain pkg, String obj, String propertyName, Reference<String> outName, Reference<DottedChain> outNs, Reference<DottedChain> outPropNs, Reference<Integer> outPropNsKind, Reference<Integer> outPropNsIndex, Reference<GraphTargetItem> outPropType, Reference<ValueKind> outPropValue, Reference<ABC> outPropValueAbc, Reference<Boolean> isType, Reference<Trait> outPropTrait) {
        isType.setVal(false);
        AbcIndexing.TraitIndex sp = abc.findScriptProperty(pkg.addWithSuffix(propertyName));
        if (sp == null) {
            sp = abc.findProperty(new AbcIndexing.PropertyDef(propertyName, new TypeItem(pkg.addWithSuffix(obj)), abc.getSelectedAbc(), selectedNs), !instanceOnly, true, true);
        }
        if (sp != null) {
            if (sp.trait instanceof TraitClass) {
                isType.setVal(true);
            }
            if (sp.objType instanceof TypeItem) {
                outName.setVal(((TypeItem) sp.objType).fullTypeName.getLast());
                outNs.setVal(((TypeItem) sp.objType).fullTypeName.getWithoutLast());
            } else {
                //FIXME? Vector?
            }
            outPropNs.setVal(sp.trait.getName(sp.abc).getNamespace(sp.abc.constants).getName(sp.abc.constants));
            outPropNsKind.setVal(sp.trait.getName(sp.abc).getNamespace(sp.abc.constants).kind);
            int nsi = sp.trait.getName(sp.abc).namespace_index;
            outPropNsIndex.setVal(sp.abc == abc.getSelectedAbc() ? sp.abc.constants.getNamespaceSubIndex(nsi) : 0);
            outPropType.setVal(sp.returnType);
            outPropValue.setVal(sp.value);
            outPropValueAbc.setVal(sp.abc);
            outPropTrait.setVal(sp.trait);
            return true;
        }
        return false;
    }

    /**
     * Parent names.
     * @param abc ABC
     * @param name_index Name index
     * @param indices Indices
     * @param names Names
     * @param namespaces Namespaces
     * @param outABCs Out ABCs
     */
    public static void parentNames(AbcIndexing abc, int scriptIndex, int name_index, List<Integer> indices, List<String> names, List<String> namespaces, List<ABC> outABCs) {
        AbcIndexing.ClassIndex ci = abc.findClass(new TypeItem(abc.getSelectedAbc().constants.getMultiname(name_index).getNameWithNamespace(abc.getSelectedAbc().constants, true /*FIXME!!*/)), abc.getSelectedAbc(), scriptIndex);
        while (ci != null) {
            int ni = ci.abc.instance_info.get(ci.index).name_index;
            indices.add(ni);
            outABCs.add(ci.abc);
            names.add(ci.abc.constants.getMultiname(ni).getName(ci.abc.constants, null, true, true/*FIXME!!*/));
            namespaces.add(ci.abc.constants.getMultiname(ni).getNamespace(ci.abc.constants).getName(ci.abc.constants).toRawString());
            ci = ci.parent;
        }
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, FalseItem item) throws CompilationException {
        return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.PushFalse));
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TrueItem item) throws CompilationException {
        return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.PushTrue));
    }

    /**
     * Generates GetDescendants.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, GetDescendantsAVM2Item item) throws CompilationException {

        AVM2ConstantPool constants = abcIndex.getSelectedAbc().constants;
        int[] nssa = new int[item.openedNamespaces.size()];
        for (int i = 0; i < item.openedNamespaces.size(); i++) {
            nssa[i] = item.openedNamespaces.get(i).getCpoolIndex(abcIndex);
        }

        int nsset = constants.getNamespaceSetId(nssa, true);

        return GraphTargetItem.toSourceMerge(localData, this,
                item.object,
                ins(AVM2Instructions.GetDescendants, constants.getMultinameId(Multiname.createMultiname(false, constants.getStringId(item.nameStr, true), nsset), true))
        );
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, AndItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        ret.add(ins(AVM2Instructions.Dup));
        if (!("" + item.leftSide.returnType()).equals("Boolean")) {
            ret.add(ins(AVM2Instructions.ConvertB));
        }
        List<AVM2Instruction> andExpr = generateToActionList(localData, item.rightSide);
        andExpr.add(0, ins(AVM2Instructions.Pop));
        int andExprLen = insToBytes(andExpr).length;
        ret.add(ins(AVM2Instructions.IfFalse, andExprLen));
        ret.addAll(andExpr);
        return ret;

    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, OrItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        ret.add(ins(AVM2Instructions.Dup));
        if (!("" + item.leftSide.returnType()).equals("Boolean")) {
            ret.add(ins(AVM2Instructions.ConvertB));
        }
        List<AVM2Instruction> orExpr = generateToActionList(localData, item.rightSide);
        orExpr.add(0, ins(AVM2Instructions.Pop));
        int orExprLen = insToBytes(orExpr).length;
        ret.add(ins(AVM2Instructions.IfTrue, orExprLen));
        ret.addAll(orExpr);
        return ret;
    }

    /**
     * Generates XMLFilter.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, XMLFilterAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        final Reference<Integer> counterReg = new Reference<>(0);
        final Reference<Integer> collectionReg = new Reference<>(0);
        final Reference<Integer> xmlListReg = new Reference<>(0);
        List<GraphSourceItem> xmlListSetTemp = AssignableAVM2Item.setTemp(localData, this, xmlListReg);
        AVM2ConstantPool constants = abcIndex.getSelectedAbc().constants;
        ret.addAll(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.PushByte, 0),
                AssignableAVM2Item.setTemp(localData, this, counterReg),
                item.object,
                ins(AVM2Instructions.CheckFilter),
                NameAVM2Item.generateCoerce(localData, this, TypeItem.UNBOUNDED),
                AssignableAVM2Item.setTemp(localData, this, collectionReg),
                ins(AVM2Instructions.GetLex, constants.getMultinameId(Multiname.createQName(false, constants.getStringId("XMLList", true), constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true)), true)),
                ins(AVM2Instructions.PushString, constants.getStringId("", true)),
                ins(AVM2Instructions.Construct, 1),
                xmlListSetTemp
        ));
        final Reference<Integer> tempVal1 = new Reference<>(0);
        final Reference<Integer> tempVal2 = new Reference<>(0);

        List<AVM2Instruction> forBody = toInsList(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.Label),
                AssignableAVM2Item.getTemp(localData, this, collectionReg),
                AssignableAVM2Item.getTemp(localData, this, counterReg),
                ins(AVM2Instructions.NextValue),
                AssignableAVM2Item.dupSetTemp(localData, this, tempVal1),
                AssignableAVM2Item.dupSetTemp(localData, this, tempVal2),
                ins(AVM2Instructions.PushWith)
        ));
        localData.scopeStack.add(new LocalRegAVM2Item(null, null, tempVal2.getVal(), null, new TypeItem("Object") /*?*/));
        forBody.addAll(toInsList(item.value.toSource(localData, this)));
        List<AVM2Instruction> trueBody = new ArrayList<>();
        trueBody.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, xmlListReg)));
        trueBody.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, counterReg)));
        trueBody.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, tempVal1)));
        trueBody.add(ins(AVM2Instructions.SetProperty, constants.getMultinameId(Multiname.createMultinameL(false, NamespaceItem.getCpoolSetIndex(abcIndex, item.openedNamespaces)), true)));
        forBody.add(ins(AVM2Instructions.IfFalse, insToBytes(trueBody).length));
        forBody.addAll(trueBody);
        forBody.add(ins(AVM2Instructions.PopScope));
        localData.scopeStack.remove(localData.scopeStack.size() - 1);
        forBody.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempVal2, tempVal1))));

        int forBodyLen = insToBytes(forBody).length;
        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, forBodyLen);
        ret.add(forwardJump);

        List<AVM2Instruction> expr = new ArrayList<>();
        expr.add(ins(AVM2Instructions.HasNext2, collectionReg.getVal(), counterReg.getVal()));
        AVM2Instruction backIf = ins(AVM2Instructions.IfTrue, 0);
        expr.add(backIf);

        int exprLen = insToBytes(expr).length;
        backIf.operands[0] = -(exprLen + forBodyLen);

        ret.addAll(forBody);
        ret.addAll(expr);
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(collectionReg, counterReg)));
        ret.addAll(AssignableAVM2Item.getTemp(localData, this, xmlListReg));
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(xmlListReg)));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) throws CompilationException {
        return generateIf(localData, item.expression, item.onTrue, item.onFalse, false);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TernarOpItem item) throws CompilationException {
        List<GraphTargetItem> onTrue = new ArrayList<>();
        onTrue.add(item.onTrue);
        List<GraphTargetItem> onFalse = new ArrayList<>();
        onFalse.add(item.onFalse);
        return generateIf(localData, item.expression, onTrue, onFalse, true);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WhileItem item) throws CompilationException {
        localData.openedLoops.add(item.loop.id);
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {
            lastItem = ex.remove(ex.size() - 1);
            while (lastItem instanceof CommaExpressionItem) {
                CommaExpressionItem cei = (CommaExpressionItem) lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            whileExpr.addAll(generateToInsList(localData, ex));
        }
        List<AVM2Instruction> whileBody = generateToInsList(localData, item.commands);
        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, 0);
        ret.add(forwardJump);
        whileBody.add(0, ins(AVM2Instructions.Label));
        ret.addAll(whileBody);
        int whileBodyLen = insToBytes(whileBody).length;
        forwardJump.operands[0] = whileBodyLen;
        whileExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int whileExprLen = insToBytes(whileExpr).length;
        whileExpr.get(whileExpr.size() - 1).operands[0] = -(whileExprLen + whileBodyLen); //Assuming last is if instruction
        ret.addAll(whileExpr);
        fixLoop(whileBody, whileBodyLen + whileExprLen, whileBodyLen, item.loop.id);
        return ret;
    }

    /**
     * Generates for each in loop.
     * @param localData Local data
     * @param item Loop item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForEachInAVM2Item item) throws CompilationException {
        return generateForIn(localData, item.loop, item.expression.collection, (AssignableAVM2Item) item.expression.object, item.commands, true);
    }

    /**
     * Generates for in loop.
     * @param localData Local data
     * @param item Loop item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForInAVM2Item item) throws CompilationException {
        return generateForIn(localData, item.loop, item.expression.collection, (AssignableAVM2Item) item.expression.object, item.commands, false);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) throws CompilationException {
        localData.openedLoops.add(item.loop.id);
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {
            lastItem = ex.remove(ex.size() - 1);
            while (lastItem instanceof CommaExpressionItem) {
                CommaExpressionItem cei = (CommaExpressionItem) lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            whileExpr.addAll(generateToInsList(localData, ex));
        }
        List<AVM2Instruction> dowhileBody = generateToInsList(localData, item.commands);
        List<AVM2Instruction> labelBody = new ArrayList<>();
        labelBody.add(ins(AVM2Instructions.Label));
        int labelBodyLen = insToBytes(labelBody).length;

        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, labelBodyLen);
        ret.add(forwardJump);
        ret.addAll(labelBody);
        ret.addAll(dowhileBody);
        int dowhileBodyLen = insToBytes(dowhileBody).length;
        whileExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int dowhileExprLen = insToBytes(whileExpr).length;
        whileExpr.get(whileExpr.size() - 1).operands[0] = -(dowhileExprLen + dowhileBodyLen + labelBodyLen); //Assuming last is if instruction
        ret.addAll(whileExpr);
        fixLoop(dowhileBody, dowhileBodyLen + dowhileExprLen, dowhileBodyLen, item.loop.id);
        return ret;
    }

    /**
     * Generates with statement.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WithAVM2Item item) throws CompilationException {

        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.scope.toSource(localData, this));
        Reference<Integer> tempReg = new Reference<>(0);
        ret.addAll(AssignableAVM2Item.dupSetTemp(localData, this, tempReg));
        localData.scopeStack.add(new WithObjectAVM2Item(null, null, new LocalRegAVM2Item(null, null, tempReg.getVal(), null, new TypeItem("Object") /*?*/)));
        ret.add(ins(AVM2Instructions.PushWith));
        ret.addAll(generate(localData, item.items));
        ret.add(ins(AVM2Instructions.PopScope));
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg)));
        localData.scopeStack.remove(localData.scopeStack.size() - 1);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) throws CompilationException {
        localData.openedLoops.add(item.loop.id);
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> forExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>();
        if (item.expression != null) {
            ex.add(item.expression);
        } else {
            ex.add(new BooleanAVM2Item(null, null, true));
        }
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {
            lastItem = ex.remove(ex.size() - 1);
            while (lastItem instanceof CommaExpressionItem) {
                CommaExpressionItem cei = (CommaExpressionItem) lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            forExpr.addAll(generateToInsList(localData, ex));
        }
        List<AVM2Instruction> forBody = generateToInsList(localData, item.commands);
        List<AVM2Instruction> forFinalCommands = generateToInsList(localData, item.finalCommands);

        ret.addAll(generateToInsList(localData, item.firstCommands));

        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, 0);
        ret.add(forwardJump);
        forBody.add(0, ins(AVM2Instructions.Label));
        ret.addAll(forBody);
        ret.addAll(forFinalCommands);
        int forBodyLen = insToBytes(forBody).length;
        int forFinalCLen = insToBytes(forFinalCommands).length;
        forwardJump.operands[0] = forBodyLen + forFinalCLen;
        forExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int forExprLen = insToBytes(forExpr).length;
        forExpr.get(forExpr.size() - 1).operands[0] = -(forExprLen + forBodyLen + forFinalCLen); //Assuming last is if instruction
        ret.addAll(forExpr);
        fixLoop(forBody, forBodyLen + forFinalCLen + forExprLen, forBodyLen, item.loop.id);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwitchItem item) throws CompilationException {
        localData.openedLoops.add(item.loop.id);
        List<GraphSourceItem> ret = new ArrayList<>();
        Reference<Integer> switchedReg = new Reference<>(0);
        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, 0);
        ret.add(forwardJump);

        int defIndex = item.caseValues.size();

        for (int i = item.caseValues.size() - 1; i >= 0; i--) {
            if (item.caseValues.get(i) instanceof DefaultItem) {
                defIndex = i;
                break;
            }
        }

        List<AVM2Instruction> cases = new ArrayList<>();
        cases.addAll(toInsList(new IntegerValueAVM2Item(null, null, defIndex).toSource(localData, this)));
        int cLen = insToBytes(cases).length;
        List<AVM2Instruction> caseLast = new ArrayList<>();
        caseLast.add(0, ins(AVM2Instructions.Jump, cLen));
        caseLast.addAll(0, toInsList(new IntegerValueAVM2Item(null, null, defIndex).toSource(localData, this)));
        int cLastLen = insToBytes(caseLast).length;
        caseLast.add(0, ins(AVM2Instructions.Jump, cLastLen));
        cases.addAll(0, caseLast);

        List<AVM2Instruction> preCases = new ArrayList<>();
        preCases.addAll(toInsList(item.switchedObject.toSource(localData, this)));
        preCases.addAll(toInsList(AssignableAVM2Item.setTemp(localData, this, switchedReg)));

        for (int i = item.caseValues.size() - 1; i >= 0; i--) {
            if (item.caseValues.get(i) instanceof DefaultItem) {
                continue;
            }
            List<AVM2Instruction> sub = new ArrayList<>();
            sub.addAll(toInsList(new IntegerValueAVM2Item(null, null, i).toSource(localData, this)));
            sub.add(ins(AVM2Instructions.Jump, insToBytes(cases).length));
            int subLen = insToBytes(sub).length;

            cases.addAll(0, sub);
            cases.add(0, ins(AVM2Instructions.IfStrictNe, subLen));
            cases.addAll(0, toInsList(AssignableAVM2Item.getTemp(localData, this, switchedReg)));
            cases.addAll(0, toInsList(item.caseValues.get(i).toSource(localData, this)));
        }
        cases.addAll(0, preCases);

        AVM2Instruction lookupOp = new AVM2Instruction(0, AVM2Instructions.LookupSwitch, new int[1 + 1 + item.caseValues.size() + 1]);
        cases.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(switchedReg))));
        List<AVM2Instruction> bodies = new ArrayList<>();
        List<Integer> bodiesOffsets = new ArrayList<>();
        int defOffset;
        int casesLen = insToBytes(cases).length;
        bodies.add(0, ins(AVM2Instructions.Label));
        bodies.add(ins(new BreakJumpIns(item.loop.id), 0));  //There could be two breaks when default clause ends with break, but official compiler does this too, so who cares...
        defOffset = -(insToBytes(bodies).length + casesLen);
        for (int i = item.caseCommands.size() - 1; i >= 0; i--) {
            bodies.addAll(0, generateToInsList(localData, item.caseCommands.get(i)));
            bodies.add(0, ins(AVM2Instructions.Label));
            bodiesOffsets.add(0, -(insToBytes(bodies).length + casesLen));
        }
        lookupOp.operands[0] = defOffset;
        lookupOp.operands[1] = item.valuesMapping.size();
        // as per avm2 spec: "There are case_count+1 case offsets"
        for (int i = 0; i < item.valuesMapping.size(); i++) {
            lookupOp.operands[2 + i] = bodiesOffsets.get(item.valuesMapping.get(i));
        }
        lookupOp.operands[2 + item.valuesMapping.size()] = defOffset;

        forwardJump.operands[0] = insToBytes(bodies).length;
        ret.addAll(bodies);
        ret.addAll(cases);
        ret.add(lookupOp);        
        fixSwitch(toInsList(ret), insToBytes(toInsList(ret)).length, item.loop.id);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, NotItem item) throws CompilationException {
        /*if (item.getOriginal() instanceof Inverted) {
         GraphTargetItem norig = ((Inverted) item).invert();
         return norig.toSource(localData, this);
         }*/
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.getOriginal().toSource(localData, this));
        ret.add(ins(AVM2Instructions.Not));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DuplicateItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(ins(AVM2Instructions.Dup));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, BreakItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        if (!localData.finallyCatches.isEmpty()) {
            for (int i = localData.finallyCatches.size() - 1; i >= 0; i--) {
                if (localData.finallyOpenedLoops.get(i).contains(item.loopId)) {
                    if (i < localData.finallyCatches.size() - 1) {
                        ret.add(ins(AVM2Instructions.Label));
                    }
                    int clauseId = localData.finallyCatches.get(i);
                    Integer cnt = localData.finallyCounter.get(clauseId);
                    if (cnt == null) {
                        cnt = -1;
                    }
                    cnt++;
                    localData.finallyCounter.put(clauseId, cnt);
                    ret.addAll(new IntegerValueAVM2Item(null, null, cnt).toSource(localData, this));
                    ret.add(ins(new FinallyJumpIns(clauseId), 0));
                    ret.add(ins(AVM2Instructions.Label));
                    ret.add(ins(AVM2Instructions.Pop));
                }
            }
            ret.add(ins(AVM2Instructions.Label));
        }

        for (int i = 0; i < localData.catchesOpenedLoops.size(); i++) {
            List<Long> openedLoops = localData.catchesOpenedLoops.get(i);

            if (openedLoops.contains(item.loopId)) {
                ret.add(ins(new PopScopeIns()));
            }
            ret.add(ins(new KillIns(), localData.catchesTempRegs.get(i)));
        }

        AVM2Instruction abreak = ins(new BreakJumpIns(item.loopId), 0);
        ret.add(abreak);
        return ret;
    }

    /**
     * Generates function.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, FunctionAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        int scope = 0;
        if (!item.functionName.isEmpty()) {
            ret.add(ins(AVM2Instructions.NewObject, 0));
            ret.add(ins(AVM2Instructions.PushWith));
            scope = localData.scopeStack.size();
            localData.scopeStack.add(new PropertyAVM2Item(null, false, item.functionName, "" /*??*/, abcIndex, new ArrayList<>(), localData.callStack));
        }
        AVM2ConstantPool constants = abcIndex.getSelectedAbc().constants;
        ret.add(ins(AVM2Instructions.NewFunction, method(null, false, constants.getStringId(item.functionName, true), true, false, false, localData.callStack, localData.pkg, item.needsActivation, item.subvariables, 0 /*Set later*/, item.hasRest, item.line, localData.currentClassBaseName, null, false, localData, item.paramTypes, item.paramNames, item.paramValues, item.body, item.retType)));
        if (!item.functionName.isEmpty()) {
            ret.add(ins(AVM2Instructions.Dup));
            ret.add(ins(AVM2Instructions.GetScopeObject, scope));
            ret.add(ins(AVM2Instructions.Swap));
            ret.add(ins(AVM2Instructions.SetProperty, constants.getMultinameId(Multiname.createQName(false, constants.getStringId(item.functionName, true), constants.getNamespaceId(Namespace.KIND_PACKAGE, localData.pkg, 0, true)), true)));
            ret.add(ins(AVM2Instructions.PopScope));
            localData.scopeStack.remove(localData.scopeStack.size() - 1);
        }
        return ret;
    }

    /**
     * Generates try-catch-finally.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TryAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();

        boolean newFinallyReg = false;
        List<ABCException> newex = new ArrayList<>();
        int aloneFinallyEx = -1;
        int finallyEx = -1;
        for (NameAVM2Item e : item.catchExceptions2) {
            ABCException aex = new ABCException();
            aex.name_index = abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createQName(false, abcIndex.getSelectedAbc().constants.getStringId(e.getVariableName(), true), abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_PACKAGE, "", 0, true)), true);
            aex.type_index = typeName(localData, e.type);
            newex.add(aex);
        }
        int finId = 0;
        if (item.finallyCommands != null) {
            if (item.catchExceptions2.isEmpty()) {
                ABCException aex = new ABCException();
                aex.name_index = 0;
                aex.type_index = 0;
                newex.add(aex);
                aloneFinallyEx = newex.size() - 1;
            }
            ABCException aex = new ABCException();
            aex.name_index = 0;
            aex.type_index = 0;
            newex.add(aex);
            finallyEx = newex.size() - 1;
            if (localData.finallyRegister == -1) {
                localData.finallyRegister = getFreeRegister(localData);
                killRegister(localData, localData.finallyRegister); //reuse for catches
                newFinallyReg = true;
            }
            finId = finId();
        }

        if (finallyEx > -1) {
            localData.finallyCatches.add(finId);
            localData.finallyOpenedLoops.add(new ArrayList<>(localData.openedLoops));
        }
        List<AVM2Instruction> tryCmds = generateToInsList(localData, item.tryCommands);

        //int i = firstId + item.catchCommands.size() - 1;
        List<AVM2Instruction> catches = new ArrayList<>();
        Reference<Integer> tempReg = new Reference<>(0);

        List<Integer> currentExceptionIds = new ArrayList<>();
        List<List<AVM2Instruction>> catchCmds = new ArrayList<>();
        for (int c = 0; c < item.catchCommands.size(); c++) {
            int i = localData.exceptions.size();
            localData.exceptions.add(newex.get(c));

            currentExceptionIds.add(i);

            //Reference<Integer> tempReg=new Reference<>(0);
            List<AVM2Instruction> catchCmd = new ArrayList<>();
            catchCmd.add(ins(AVM2Instructions.NewCatch, i));
            catchCmd.addAll(toInsList(AssignableAVM2Item.dupSetTemp(localData, this, tempReg)));
            catchCmd.add(ins(AVM2Instructions.Dup));
            catchCmd.add(ins(AVM2Instructions.PushScope));
            catchCmd.add(ins(AVM2Instructions.Swap));
            catchCmd.add(ins(AVM2Instructions.SetSlot, 1));

            for (AssignableAVM2Item a : item.catchVariables.get(c)) {
                GraphTargetItem r = a;
                if (r instanceof UnresolvedAVM2Item) {
                    r = ((UnresolvedAVM2Item) r).resolvedRoot;
                }
                if (r instanceof NameAVM2Item) {
                    NameAVM2Item n = (NameAVM2Item) r;
                    if (item.catchExceptions2.get(c).getVariableName().equals(n.getVariableName())) {
                        n.setSlotScope(localData.scopeStack.size());
                    }
                }
            }
            localData.catchesTempRegs.add(tempReg.getVal());
            localData.scopeStack.add(new LocalRegAVM2Item(null, null, tempReg.getVal(), null, new TypeItem("Object") /*?*/));
            localData.catchesOpenedLoops.add(new ArrayList<>(localData.openedLoops));
            catchCmd.addAll(generateToInsList(localData, item.catchCommands.get(c)));
            localData.catchesOpenedLoops.remove(localData.catchesOpenedLoops.size() - 1);
            localData.scopeStack.remove(localData.scopeStack.size() - 1);
            localData.catchesTempRegs.remove(localData.catchesTempRegs.size() - 1);
            if (!item.catchCommands.get(c).isEmpty()
                    && ((item.catchCommands.get(c).get(item.catchCommands.get(c).size() - 1) instanceof ContinueItem)
                    || (item.catchCommands.get(c).get(item.catchCommands.get(c).size() - 1) instanceof BreakItem))) {
                AssignableAVM2Item.killRegister(localData, this, tempReg.getVal());
            } else {
                catchCmd.add(ins(AVM2Instructions.PopScope));
                catchCmd.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg))));
            }
            catchCmds.add(catchCmd);
        }
        for (int c = item.catchCommands.size() - 1; c >= 0; c--) {
            List<AVM2Instruction> preCatches = new ArrayList<>();
            /*preCatches.add(ins(AVM2Instructions.GetLocal0));
             preCatches.add(ins(AVM2Instructions.PushScope));
             preCatches.add(AssignableAVM2Item.generateGetLoc(localData.activationReg));
             preCatches.add(ins(AVM2Instructions.PushScope));*/
            for (GraphTargetItem s : localData.scopeStack) {
                preCatches.addAll(toInsList(s.toSource(localData, this)));
                if (s instanceof WithObjectAVM2Item) {
                    preCatches.add(ins(AVM2Instructions.PushWith));
                } else {
                    preCatches.add(ins(AVM2Instructions.PushScope));
                }
            }

            //catchCmds.add(catchCmd);
            preCatches.addAll(catchCmds.get(c));
            catches.addAll(0, preCatches);
            catches.add(0, new ExceptionMarkAVM2Instruction(currentExceptionIds.get(c), MARK_E_TARGET));
            catches.add(0, ins(AVM2Instructions.Jump, insToBytes(catches).length));
        }

        if (aloneFinallyEx > -1) {
            localData.exceptions.add(newex.get(aloneFinallyEx));
            aloneFinallyEx = localData.exceptions.size() - 1;

        }
        if (finallyEx > -1) {
            localData.exceptions.add(newex.get(finallyEx));
            finallyEx = localData.exceptions.size() - 1;
        }

        for (int i : currentExceptionIds) {
            ret.add(new ExceptionMarkAVM2Instruction(i, MARK_E_START));
        }
        if (aloneFinallyEx > -1) {
            ret.add(new ExceptionMarkAVM2Instruction(aloneFinallyEx, MARK_E_START));
        }
        if (finallyEx > -1) {
            ret.add(new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_START));
        }

        ret.addAll(tryCmds);

        for (int i : currentExceptionIds) {
            ret.add(new ExceptionMarkAVM2Instruction(i, MARK_E_END));
        }
        if (aloneFinallyEx > -1) {
            ret.add(new ExceptionMarkAVM2Instruction(aloneFinallyEx, MARK_E_END));
        }

        if (aloneFinallyEx > -1) {
            List<AVM2Instruction> preCatches = new ArrayList<>();
            for (GraphTargetItem s : localData.scopeStack) {
                preCatches.addAll(toInsList(s.toSource(localData, this)));
                if (s instanceof WithObjectAVM2Item) {
                    preCatches.add(ins(AVM2Instructions.PushWith));
                } else {
                    preCatches.add(ins(AVM2Instructions.PushScope));
                }
            }
            preCatches.add(ins(AVM2Instructions.NewCatch, aloneFinallyEx));
            preCatches.addAll(toInsList(AssignableAVM2Item.dupSetTemp(localData, this, tempReg)));
            preCatches.add(ins(AVM2Instructions.PushScope));
            preCatches.add(ins(AVM2Instructions.Throw));
            preCatches.add(ins(AVM2Instructions.PopScope));
            preCatches.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg))));
            catches.add(ins(AVM2Instructions.Jump, insToBytes(preCatches).length));
            catches.add(new ExceptionMarkAVM2Instruction(aloneFinallyEx, MARK_E_TARGET));
            catches.addAll(preCatches);
        }
        AVM2Instruction finSwitch = null;
        AVM2Instruction pushDefIns = ins(AVM2Instructions.PushByte, 0);

        int defPos = 0;
        if (finallyEx > -1) {
            List<AVM2Instruction> preCatches = new ArrayList<>();
            preCatches.add(0, new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_TARGET));
            for (GraphTargetItem s : localData.scopeStack) {
                preCatches.addAll(toInsList(s.toSource(localData, this)));
                if (s instanceof WithObjectAVM2Item) {
                    preCatches.add(ins(AVM2Instructions.PushWith));
                } else {
                    preCatches.add(ins(AVM2Instructions.PushScope));
                }
            }
            preCatches.add(ins(AVM2Instructions.NewCatch, finallyEx));
            preCatches.addAll(toInsList(AssignableAVM2Item.dupSetTemp(localData, this, tempReg)));
            preCatches.add(ins(AVM2Instructions.PushScope));
            preCatches.add(ins(AVM2Instructions.PopScope));
            Reference<Integer> tempReg2 = new Reference<>(0);
            preCatches.add(ins(AVM2Instructions.Kill, tempReg.getVal()));
            preCatches.add(ins(AVM2Instructions.CoerceA));
            preCatches.addAll(toInsList(AssignableAVM2Item.setTemp(localData, this, tempReg2)));
            preCatches.add(pushDefIns);

            List<AVM2Instruction> finallySwitchCmds = new ArrayList<>();

            finSwitch = new AVM2Instruction(0, AVM2Instructions.LookupSwitch, new int[1 + 1 + 1]);
            finSwitch.operands[0] = finSwitch.getBytesLength();
            finSwitch.operands[1] = 0; //switch cnt

            List<AVM2Instruction> preFinallySwitch = new ArrayList<>();
            preFinallySwitch.add(ins(AVM2Instructions.Label));
            preFinallySwitch.add(ins(AVM2Instructions.Pop));
            int preFinallySwitchLen = insToBytes(preFinallySwitch).length;

            finallySwitchCmds.add(ins(AVM2Instructions.Label));
            finallySwitchCmds.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, tempReg2)));
            finallySwitchCmds.add(ins(AVM2Instructions.Kill, tempReg2.getVal()));
            finallySwitchCmds.add(ins(AVM2Instructions.Throw));

            int finSwitchLen = insToBytes(finallySwitchCmds).length;

            preCatches.add(ins(AVM2Instructions.Jump, preFinallySwitchLen + finSwitchLen));
            AVM2Instruction fjump = ins(AVM2Instructions.Jump, 0);
            fjump.operands[0] = insToBytes(preCatches).length + preFinallySwitchLen + finSwitchLen;

            preCatches.add(0, fjump);
            preCatches.add(0, new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_END));
            preCatches.add(0, ins(AVM2Instructions.PushByte, -1));

            finallySwitchCmds.add(new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_FINALLYPART));

            int oldReg = localData.finallyRegister;
            localData.finallyRegister = getFreeRegister(localData);
            Integer cnt = localData.finallyCounter.get(finId);
            if (cnt == null) {
                cnt = -1;
            }
            defPos = cnt;
            cnt++; //Skip default clause (throw)
            localData.finallyCounter.put(finId, cnt);
            finallySwitchCmds.addAll(generateToInsList(localData, item.finallyCommands));
            killRegister(localData, localData.finallyRegister);
            localData.finallyRegister = oldReg;
            finSwitchLen = insToBytes(finallySwitchCmds).length;

            finSwitch.operands[2] = -finSwitchLen;
            preCatches.addAll(preFinallySwitch);
            preCatches.addAll(finallySwitchCmds);
            preCatches.add(finSwitch);

            catches.addAll(preCatches);
            AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg, tempReg2));
        }

        ret.addAll(catches);
        //localData.exceptions.addAll(newex);

        if (finallyEx > -1) {
            localData.finallyCatches.remove(localData.finallyCatches.size() - 1);
            localData.finallyOpenedLoops.remove(localData.finallyOpenedLoops.size() - 1);
        }
        if (newFinallyReg) {
            localData.finallyRegister = -1;
            killRegister(localData, localData.finallyRegister);
        }
        int pos = 0;
        int finallyPos = 0;
        int switchPos = 0;
        for (int s = 0; s < ret.size(); s++) {
            GraphSourceItem src = ret.get(s);
            if (src == finSwitch) {
                switchPos = pos;
            }
            if (src instanceof AVM2Instruction) {
                AVM2Instruction ins = (AVM2Instruction) src;
                if (ins instanceof ExceptionMarkAVM2Instruction) {
                    ExceptionMarkAVM2Instruction em = (ExceptionMarkAVM2Instruction) ins;
                    if (em.exceptionId == finallyEx && em.markType == MARK_E_FINALLYPART) {
                        finallyPos = pos;
                        ret.remove(s);
                        s--;
                        continue;
                    }
                }
                pos += ins.getBytesLength();
            }

        }

        if (finSwitch != null) {
            pos = 0;
            int defLoc = finSwitch.operands[2];
            List<Integer> switchLoc = new ArrayList<>();
            boolean wasDef = false;
            for (int s = 0; s < ret.size(); s++) {
                GraphSourceItem src = ret.get(s);
                if (src instanceof AVM2Instruction) {
                    AVM2Instruction ins = (AVM2Instruction) src;
                    if (ins.definition instanceof FinallyJumpIns) {
                        FinallyJumpIns fji = (FinallyJumpIns) ins.definition;
                        if (fji.getClauseId() == finId) {
                            List<AVM2Instruction> bet = new ArrayList<>();
                            bet.add(ins(AVM2Instructions.Label));
                            bet.add(ins(AVM2Instructions.Pop));
                            int betLen = insToBytes(bet).length;
                            if (wasDef) {
                                ins.operands[0] = 0;
                            } else {
                                ins.operands[0] = finallyPos - (pos + ins.getBytesLength());
                            }
                            ins.definition = AVM2Code.instructionSet[AVM2Instructions.Jump];
                            switchLoc.add(pos + ins.getBytesLength() + betLen - switchPos);
                        }
                    }
                    pos += ins.getBytesLength();
                }
                if (defPos == switchLoc.size() - 1) {
                    switchLoc.add(defLoc);
                    wasDef = true;
                }
            }
            finSwitch.operands = new int[1 + 1 + switchLoc.size()];
            pushDefIns.operands[0] = defPos + 1;
            int afterLoc = finSwitch.getBytesLength();
            finSwitch.operands[0] = afterLoc;
            finSwitch.operands[1] = switchLoc.size() - 1;
            for (int j = 0; j < switchLoc.size(); j++) {
                finSwitch.operands[2 + j] = switchLoc.get(j);
            }
        }

        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();

        if (!localData.finallyCatches.isEmpty()) {
            for (int i = localData.finallyCatches.size() - 1; i >= 0; i--) {
                if (localData.finallyOpenedLoops.get(i).contains(item.loopId)) {
                    if (i < localData.finallyCatches.size() - 1) {
                        ret.add(ins(AVM2Instructions.Label));
                    }
                    int clauseId = localData.finallyCatches.get(i);
                    Integer cnt = localData.finallyCounter.get(clauseId);
                    if (cnt == null) {
                        cnt = -1;
                    }
                    cnt++;
                    localData.finallyCounter.put(clauseId, cnt);
                    ret.addAll(new IntegerValueAVM2Item(null, null, cnt).toSource(localData, this));
                    ret.add(ins(new FinallyJumpIns(clauseId), 0));
                    ret.add(ins(AVM2Instructions.Label));
                    ret.add(ins(AVM2Instructions.Pop));
                }
            }
            ret.add(ins(AVM2Instructions.Label));
        }

        for (int i = 0; i < localData.catchesOpenedLoops.size(); i++) {
            List<Long> openedLoops = localData.catchesOpenedLoops.get(i);

            if (openedLoops.contains(item.loopId)) {
                ret.add(ins(new PopScopeIns()));
            }
            ret.add(ins(new KillIns(), localData.catchesTempRegs.get(i)));
        }

        AVM2Instruction acontinue = ins(new ContinueJumpIns(item.loopId), 0);
        ret.add(acontinue);
        return ret;
    }

    /**
     * Generates return value.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ReturnValueAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        GraphTargetItem value = item.value;
        value = handleAndOrCoerce(value, localData.returnType);
        ret.addAll(value.toSource(localData, this));
        if (!localData.finallyCatches.isEmpty()) {
            ret.add(ins(AVM2Instructions.CoerceA));
            ret.add(AssignableAVM2Item.generateSetLoc(localData.finallyRegister));
            for (int i = localData.finallyCatches.size() - 1; i >= 0; i--) {
                if (i < localData.finallyCatches.size() - 1) {
                    ret.add(ins(AVM2Instructions.Label));
                }
                int clauseId = localData.finallyCatches.get(i);
                Integer cnt = localData.finallyCounter.get(clauseId);
                if (cnt == null) {
                    cnt = -1;
                }
                cnt++;
                localData.finallyCounter.put(clauseId, cnt);
                ret.addAll(new IntegerValueAVM2Item(null, null, cnt).toSource(localData, this));
                ret.add(ins(new FinallyJumpIns(clauseId), 0));
                ret.add(ins(AVM2Instructions.Label));
                ret.add(ins(AVM2Instructions.Pop));
            }
            ret.add(ins(AVM2Instructions.Label));
            ret.add(AssignableAVM2Item.generateGetLoc(localData.finallyRegister));
            ret.add(ins(AVM2Instructions.Kill, localData.finallyRegister));
        }
        ret.add(ins(AVM2Instructions.ReturnValue));
        return ret;
    }

    /**
     * Generates return void.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ReturnVoidAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        if (!localData.finallyCatches.isEmpty()) {

            for (int i = 0; i < localData.finallyCatches.size(); i++) {
                if (i > 0) {
                    ret.add(ins(AVM2Instructions.Label));
                }
                int clauseId = localData.finallyCatches.get(i);
                Integer cnt = localData.finallyCounter.get(clauseId);
                if (cnt == null) {
                    cnt = -1;
                }
                cnt++;
                localData.finallyCounter.put(clauseId, cnt);
                ret.addAll(new IntegerValueAVM2Item(null, null, cnt).toSource(localData, this));
                ret.add(ins(new FinallyJumpIns(clauseId), 0));
                ret.add(ins(AVM2Instructions.Label));
                ret.add(ins(AVM2Instructions.Pop));
            }
            ret.add(ins(AVM2Instructions.Label));
        }
        ret.add(ins(AVM2Instructions.ReturnVoid));
        return ret;
    }

    /**
     * Generates throw.
     * @param localData Local data
     * @param item Item
     * @return Source items
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ThrowAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.value.toSource(localData, this));
        ret.add(ins(AVM2Instructions.Throw));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (GraphTargetItem item : commands) {
            ret.addAll(item.toSourceIgnoreReturnValue(localData, this));
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, CommaExpressionItem item, boolean withReturnValue) throws CompilationException {
        if (item.commands.isEmpty()) {
            return new ArrayList<>();
        }

        //We need to handle commands and last expression separately, otherwise last expression result will be popped
        List<GraphTargetItem> cmds = new ArrayList<>(item.commands);
        GraphTargetItem lastExpr = cmds.remove(cmds.size() - 1);
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generate(localData, cmds));
        ret.addAll(withReturnValue ? lastExpr.toSource(localData, this) : lastExpr.toSourceIgnoreReturnValue(localData, this));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TypeItem item) throws CompilationException {
        String currentFullClassName = localData.getFullClass();

        int globalSlotId = 0;
        if (item.fullTypeName.size() == 1 && item.toString().equals(currentFullClassName)) {
            int c = abcIndex.getSelectedAbc().findClassByName(currentFullClassName);
            for (Trait t : localData.currentScript.traits.traits) {
                if (t instanceof TraitClass) {
                    TraitClass tc = (TraitClass) t;
                    if (tc.class_info == c) {
                        globalSlotId = tc.slot_id;
                        break;
                    }
                }
            }
        }
        if (localData.isStatic && item.toString().equals(currentFullClassName)) {
            return Arrays.asList(ins(AVM2Instructions.GetLocal0));
        } else if (globalSlotId > 0) {
            return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.GetGlobalScope), ins(AVM2Instructions.GetSlot, globalSlotId));
        } else {
            return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.GetLex, resolveType(localData, item, abcIndex)));
        }
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, PushItem item) throws CompilationException {
        return item.value.toSource(localData, this);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, PopItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        return ret;
    }
    
    /**
     * For some strange reasons, &amp;&amp; and || operators must have coerced both sides
     * when there is specific type expected.
     * @param item Item to check. If it's &amp;&amp; or ||, it will be handled, otherwise ignored
     * @param type Coerced type
     * @return Original value or coerced item
     */
    public static GraphTargetItem handleAndOrCoerce(GraphTargetItem item, GraphTargetItem type) {
        if (type == null) {
            return item;
        }
        if ((item instanceof OrItem)||(item instanceof AndItem)) {
            BinaryOpItem bo = (BinaryOpItem) item;
            if ((bo.leftSide instanceof OrItem)||(bo.leftSide instanceof AndItem)) {
                bo.leftSide = handleAndOrCoerce(bo.leftSide, type);
            } else {
                bo.leftSide = new CoerceAVM2Item(null, null, bo.leftSide, type);
            }
            if ((bo.rightSide instanceof OrItem)||(bo.rightSide instanceof AndItem)) {
                bo.rightSide = handleAndOrCoerce(bo.rightSide, type);
            } else {
                bo.rightSide = new CoerceAVM2Item(null, null, bo.rightSide, type);
            }
        }
        return item;
    }
}
