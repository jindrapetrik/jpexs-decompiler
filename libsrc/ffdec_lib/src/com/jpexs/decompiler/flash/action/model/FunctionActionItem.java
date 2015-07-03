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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionActionItem extends ActionItem {

    public List<GraphTargetItem> actions;

    public List<String> constants;

    public String functionName;

    public List<String> paramNames;

    public GraphTargetItem calculatedFunctionName;

    private int regStart;

    private List<VariableActionItem> variables;

    public static final int REGISTER_THIS = 1;

    public static final int REGISTER_ARGUMENTS = 2;

    public static final int REGISTER_SUPER = 3;

    public static final int REGISTER_ROOT = 4;

    public static final int REGISTER_PARENT = 5;

    public static final int REGISTER_GLOBAL = 6;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.addAll(actions);
        return ret;
    }

    public FunctionActionItem() {
        super(null, PRECEDENCE_PRIMARY);
    }

    public FunctionActionItem(GraphSourceItem instruction, String functionName, List<String> paramNames, List<GraphTargetItem> actions, List<String> constants, int regStart, List<VariableActionItem> variables) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.actions = actions;
        this.constants = constants;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.regStart = regStart;
        this.variables = variables;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("function");
        if (calculatedFunctionName != null) {
            writer.append(" ");
            String fname = calculatedFunctionName.toStringNoQuotes(localData);
            if (!IdentifiersDeobfuscation.isValidName(false, fname)) {
                writer.append(IdentifiersDeobfuscation.makeObfuscatedIdentifier(fname));
            } else {
                calculatedFunctionName.appendToNoQuotes(writer, localData);
            }
        } else if (!functionName.isEmpty()) {
            writer.append(" ");
            if (!IdentifiersDeobfuscation.isValidName(false, functionName)) {
                writer.append(IdentifiersDeobfuscation.makeObfuscatedIdentifier(functionName));
            } else {
                writer.append(functionName);
            }
        }
        writer.spaceBeforeCallParenthesies(paramNames.size());
        writer.append("(");

        for (int p = 0; p < paramNames.size(); p++) {
            if (p > 0) {
                writer.append(", ");
            }
            String pname = paramNames.get(p);
            if (pname == null || pname.isEmpty()) {
                pname = new RegisterNumber(regStart + p).translate();
            }
            if (!IdentifiersDeobfuscation.isValidName(false, pname)) {
                writer.append(IdentifiersDeobfuscation.makeObfuscatedIdentifier(pname));
            }
            writer.append(pname);
        }
        writer.append(")").startBlock();

        Graph.graphToString(actions, writer, localData);

        return writer.endBlock();
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        for (GraphTargetItem ti : actions) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        for (GraphTargetItem a : actions) {
            if (dependencies.contains(a)) {
                return false;
            }
            dependencies.add(a);
            if (!a.isCompileTime(dependencies)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getResult() {
        if (!actions.isEmpty()) {
            if (actions.get(actions.size() - 1) instanceof ReturnActionItem) {
                ReturnActionItem r = (ReturnActionItem) actions.get(actions.size() - 1);
                return r.value.getResult();
            }
        }
        return 0;
    }

    @Override
    public boolean needsNewLine() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        Set<String> usedNames = new HashSet<>();
        for (VariableActionItem v : variables) {
            usedNames.add(v.getVariableName());
        }

        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        List<Integer> paramRegs = new ArrayList<>();
        SourceGeneratorLocalData localDataCopy = Helper.deepCopy(localData);
        localDataCopy.inFunction++;
        boolean preloadParentFlag = false;
        boolean preloadRootFlag = false;
        boolean preloadSuperFlag = false;
        boolean preloadArgumentsFlag = false;
        boolean preloadThisFlag = false;
        boolean preloadGlobalFlag = false;

        boolean suppressParentFlag = false;
        boolean suppressArgumentsFlag = false;
        boolean suppressThisFlag = false;

        boolean needsFun2 = false;

        List<String> registerNames = new ArrayList<>();
        registerNames.add("***** ZERO *****");
        if (usedNames.contains("this")) {
            needsFun2 = true;
            preloadThisFlag = true;
            registerNames.add("this");
        } else {
            suppressThisFlag = true;
        }
        if (usedNames.contains("arguments")) {
            preloadArgumentsFlag = true;
            needsFun2 = true;
            registerNames.add("arguments");
        } else {
            suppressArgumentsFlag = true;
        }
        if (usedNames.contains("super")) {
            preloadSuperFlag = true;
            needsFun2 = true;
            registerNames.add("super");
        }
        if (usedNames.contains("_root")) {
            preloadRootFlag = true;
            needsFun2 = true;
            registerNames.add("_root");
        }
        if (usedNames.contains("_parent")) {
            preloadParentFlag = true;
            needsFun2 = true;
            registerNames.add("_parent");
        } else {
            suppressParentFlag = true;
        }
        if (usedNames.contains("_global")) {
            needsFun2 = true;
            preloadGlobalFlag = true;
            registerNames.add("_global");
        }

        int preloadedNumber = registerNames.size();
        if (!paramNames.isEmpty()) {
            needsFun2 = true;
        }
        if (localData.inMethod) {
            needsFun2 = true;
        }
        if (localData.inFunction > 1) {
            needsFun2 = true;
        }
        if (needsFun2) {
            for (int i = 0; i < paramNames.size(); i++) {
                paramRegs.add(registerNames.size());
                registerNames.add(paramNames.get(i));
            }
        }

        int regCount = 0;
        if (actions != null && !actions.isEmpty()) {
            localDataCopy.inFunction++;

            for (VariableActionItem v : variables) {
                String varName = v.getVariableName();
                GraphTargetItem stored = v.getStoreValue();
                if (needsFun2) {
                    if (v.isDefinition() && !registerNames.contains(varName)) {
                        registerNames.add(varName);
                    }
                }

                if (registerNames.contains(varName)) {
                    if (stored != null) {
                        v.setBoxedValue(new StoreRegisterActionItem(null, new RegisterNumber(registerNames.indexOf(varName), varName), stored, false));
                    } else {
                        v.setBoxedValue(new DirectValueActionItem(new RegisterNumber(registerNames.indexOf(varName), varName)));
                    }
                } else {
                    if (v.isDefinition()) {
                        v.setBoxedValue(new DefineLocalActionItem(null, ((ActionSourceGenerator) generator).pushConstTargetItem(varName), stored));
                    } else {
                        if (stored != null) {
                            v.setBoxedValue(new SetVariableActionItem(null, ((ActionSourceGenerator) generator).pushConstTargetItem(varName), stored));
                        } else {
                            v.setBoxedValue(new GetVariableActionItem(null, ((ActionSourceGenerator) generator).pushConstTargetItem(varName)));
                        }
                    }
                }

            }
            for (int i = 1 /* zero is not preloaded*/; i < registerNames.size(); i++) {
                localDataCopy.registerVars.put(registerNames.get(i), i);
            }

            ret.addAll(asGenerator.toActionList(asGenerator.generate(localDataCopy, actions)));

            regCount = registerNames.size();

            //some temporary registers can exceed variable+param count
            for (GraphSourceItem a : ret) {
                if (a instanceof ActionPush) {
                    ActionPush apu = (ActionPush) a;
                    for (Object o : apu.values) {
                        if (o instanceof RegisterNumber) {
                            RegisterNumber rn = (RegisterNumber) o;
                            if (rn.number >= regCount) {
                                regCount++;
                            }
                        }
                    }
                }
            }
        }
        int len = Action.actionsToBytes(asGenerator.toActionList(ret), false, SWF.DEFAULT_VERSION).length;
        if (!needsFun2 && paramNames.isEmpty()) {
            ret.add(0, new ActionDefineFunction(functionName, paramNames, len, SWF.DEFAULT_VERSION));
        } else {
            ret.add(0, new ActionDefineFunction2(functionName,
                    preloadParentFlag,
                    preloadRootFlag,
                    suppressParentFlag,
                    preloadSuperFlag,
                    suppressArgumentsFlag,
                    preloadArgumentsFlag,
                    suppressThisFlag,
                    preloadThisFlag,
                    preloadGlobalFlag,
                    regCount, len, SWF.DEFAULT_VERSION, paramNames, paramRegs));
        }

        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false; //function actually returns itself, but here is false for generator to not add Pop
    }
}
