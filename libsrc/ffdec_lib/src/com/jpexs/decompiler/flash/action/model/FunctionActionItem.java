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
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BranchStackResistant;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Function.
 *
 * @author JPEXS
 */
public class FunctionActionItem extends ActionItem implements BranchStackResistant {

    /**
     * Decompile get/set functions
     */
    public static final boolean DECOMPILE_GET_SET = true;

    /**
     * Is getter
     */
    public boolean isGetter = false;

    /**
     * Is setter
     */
    public boolean isSetter = false;

    /**
     * Actions
     */
    public List<GraphTargetItem> actions;

    /**
     * Constants
     */
    public List<String> constants;

    /**
     * Function name
     */
    public String functionName;

    /**
     * Parameter names
     */
    public List<String> paramNames;

    /**
     * Register names
     */
    public Map<Integer, String> regNames;

    /**
     * Calculated function name
     */
    public GraphTargetItem calculatedFunctionName;

    /**
     * Has eval
     */
    public boolean hasEval = false;

    /**
     * Register start
     */
    private int regStart;

    /**
     * Variables
     */
    private List<VariableActionItem> variables;

    /**
     * Inner functions
     */
    private List<FunctionActionItem> innerFunctions;

    /**
     * Register - this
     */
    public static final int REGISTER_THIS = 1;

    /**
     * Register - arguments
     */
    public static final int REGISTER_ARGUMENTS = 2;

    /**
     * Register - super
     */
    public static final int REGISTER_SUPER = 3;

    /**
     * Register - root
     */
    public static final int REGISTER_ROOT = 4;

    /**
     * Register - parent
     */
    public static final int REGISTER_PARENT = 5;

    /**
     * Register - global
     */
    public static final int REGISTER_GLOBAL = 6;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visitAll(actions);
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {

    }

    /**
     * Adds variable.
     * @param variable Variable
     */
    public void addVariable(VariableActionItem variable) {
        variables.add(variable);
    }

    /**
     * Constructor.
     */
    public FunctionActionItem() {
        super(null, null, PRECEDENCE_PRIMARY);
    }

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param functionName Function name
     * @param paramNames Parameter names
     * @param regNames Register names
     * @param actions Actions
     * @param constants Constants
     * @param regStart Register start
     * @param variables Variables
     * @param innerFunctions Inner functions
     * @param hasEval Has eval
     */
    public FunctionActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, String functionName, List<String> paramNames, Map<Integer, String> regNames, List<GraphTargetItem> actions, List<String> constants, int regStart, List<VariableActionItem> variables, List<FunctionActionItem> innerFunctions, boolean hasEval) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.actions = actions;
        this.constants = constants;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.regNames = regNames;
        this.regStart = regStart;
        this.variables = variables;
        this.innerFunctions = innerFunctions;
        this.hasEval = hasEval;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String n = calculatedFunctionName != null ? calculatedFunctionName.toStringNoQuotes(localData) : functionName;
        writer.startFunction(n);
        HighlightData srcData = getSrcData();
        if (n != null) {
            srcData.localName = n;
            srcData.declaration = true;
        }

        writer.append("function");
        if (DECOMPILE_GET_SET) {
            if (isGetter) {
                writer.append(" get");
            }
            if (isSetter) {
                writer.append(" set");
            }
        }
        if (calculatedFunctionName != null) {
            writer.append(" ");
            String fname = calculatedFunctionName.toStringNoQuotes(localData);
            if (DECOMPILE_GET_SET) {
                if (isGetter && fname.startsWith("__get__")) {
                    fname = fname.substring(7);
                }
                if (isSetter && fname.startsWith("__set__")) {
                    fname = fname.substring(7);
                }
            }

            if (!IdentifiersDeobfuscation.isValidName(false, fname)) {
                IdentifiersDeobfuscation.appendObfuscatedIdentifier(fname, writer);
            } else {
                writer.append(fname);
                //calculatedFunctionName.appendToNoQuotes(writer, localData);
            }
        } else if (!functionName.isEmpty()) {
            String fname = functionName;
            if (DECOMPILE_GET_SET) {
                if (isGetter && fname.startsWith("__get__")) {
                    fname = fname.substring(7);
                }
                if (isSetter && fname.startsWith("__set__")) {
                    fname = fname.substring(7);
                }
            }
            writer.append(" ");
            if (!IdentifiersDeobfuscation.isValidName(false, fname)) {
                IdentifiersDeobfuscation.appendObfuscatedIdentifier(fname, writer);
            } else {
                writer.append(fname);
            }
        }
        writer.spaceBeforeCallParenthesis(paramNames.size());
        writer.append("(");

        Map<String, Integer> n2r = new HashMap<>();
        for (int r : regNames.keySet()) {
            n2r.put(regNames.get(r), r);
        }

        for (int p = 0; p < paramNames.size(); p++) {
            if (p > 0) {
                writer.append(", ");
            }
            String pname = paramNames.get(p);
            if (pname == null || pname.isEmpty()) {
                pname = new RegisterNumber(regStart + p).translate();
            }
            HighlightData d = getSrcData();
            d.localName = pname;
            if (n2r.containsKey(pname)) {
                d.regIndex = n2r.get(pname);
            }
            d.declaration = true;

            if (!IdentifiersDeobfuscation.isValidName(false, pname)) {
                IdentifiersDeobfuscation.appendObfuscatedIdentifier(pname, writer);
            } else {
                writer.append(pname);
            }
        }
        writer.append(")").startBlock();

        Graph.graphToString(actions, writer, localData);

        writer.endBlock();
        writer.endMethod();
        return writer;
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

    /*
    NOT COMPILE TIME! This causes problems in simplifyExpressions, etc.
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
    }*/
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

    private Set<String> getDefinedVariableNames(List<VariableActionItem> variables) {
        Set<String> ret = new HashSet<>();
        for (VariableActionItem v : variables) {
            if (v.isDefinition()) {
                ret.add(v.getVariableName());
            }
        }
        return ret;
    }

    private void getDeeplyUsedVariableNames(Set<String> topLevelDefinedVariableNames, FunctionActionItem fun, Set<String> deeplyUsedVariableNames) {

        Set<String> definedVarNamesInFunc = getDefinedVariableNames(fun.variables);
        for (VariableActionItem v : fun.variables) {
            if (!v.isDefinition() && !definedVarNamesInFunc.contains(v.getVariableName()) && topLevelDefinedVariableNames.contains(v.getVariableName())) {
                deeplyUsedVariableNames.add(v.getVariableName());
            }
        }

        for (FunctionActionItem innerFun : fun.innerFunctions) {
            getDeeplyUsedVariableNames(topLevelDefinedVariableNames, innerFun, deeplyUsedVariableNames);
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();

        Set<String> usedNames = new HashSet<>();
        for (VariableActionItem v : variables) {
            usedNames.add(v.getVariableName());
        }

        List<GraphSourceItem> ret = new ArrayList<>();
        List<Integer> paramRegs = new ArrayList<>();
        SourceGeneratorLocalData localDataCopy = Helper.deepCopy(localData);
        localDataCopy.inFunction++;
        boolean preloadParentFlag = false;
        boolean preloadRootFlag = false;
        boolean preloadSuperFlag = false;
        boolean preloadArgumentsFlag = false;
        boolean preloadThisFlag = false;
        boolean preloadGlobalFlag = false;

        boolean suppressSuperFlag = false;
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
        } else {
            suppressSuperFlag = true;
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
            //needsFun2 = true;
        }
        if (localData.inFunction > 1) {
            needsFun2 = true;
        }

        //If the function parameter or local variable is used in inner function, 
        //it must not be stored in a local register.
        Set<String> topLevelVariableNames = new HashSet<>();
        for (VariableActionItem v : variables) {
            if (v.isDefinition()) {
                topLevelVariableNames.add(v.getVariableName());
            }
        }
        for (String pn : paramNames) {
            topLevelVariableNames.add(pn);
        }
        Set<String> deeplyUsedVariableNames = new HashSet<>();
        for (FunctionActionItem fun : innerFunctions) {
            getDeeplyUsedVariableNames(topLevelVariableNames, fun, deeplyUsedVariableNames);
        }

        if (needsFun2) {
            for (int i = 0; i < paramNames.size(); i++) {
                if (deeplyUsedVariableNames.contains(paramNames.get(i))) {
                    paramRegs.add(0); //this will be variable, no register
                } else {
                    paramRegs.add(registerNames.size());
                    registerNames.add(paramNames.get(i));
                }
            }
        }

        int regCount = 0;
        if (actions != null && !actions.isEmpty()) {
            localDataCopy.inFunction++;

            for (VariableActionItem v : variables) {
                String varName = v.getVariableName();
                GraphTargetItem stored = v.getStoreValue();
                if (needsFun2) {
                    if (v.isDefinition() && !registerNames.contains(varName) && !deeplyUsedVariableNames.contains(varName)
                            && !hasEval) {
                        registerNames.add(varName);
                    }
                }

                if (registerNames.contains(varName)) {
                    if (stored != null) {
                        v.setBoxedValue(new StoreRegisterActionItem(null, null, new RegisterNumber(registerNames.indexOf(varName), varName), stored, false));
                    } else {
                        v.setBoxedValue(new DirectValueActionItem(new RegisterNumber(registerNames.indexOf(varName), varName)));
                    }
                } else if (v.isDefinition()) {
                    v.setBoxedValue(new DefineLocalActionItem(null, null, ((ActionSourceGenerator) generator).pushConstTargetItem(varName), stored));
                } else if (stored != null) {
                    v.setBoxedValue(new SetVariableActionItem(null, null, ((ActionSourceGenerator) generator).pushConstTargetItem(varName), stored));
                } else {
                    v.setBoxedValue(new GetVariableActionItem(null, null, ((ActionSourceGenerator) generator).pushConstTargetItem(varName)));
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
            ret.add(0, new ActionDefineFunction(functionName, paramNames, len, SWF.DEFAULT_VERSION, charset));
        } else {
            ret.add(0, new ActionDefineFunction2(functionName,
                    preloadParentFlag,
                    preloadRootFlag,
                    suppressSuperFlag,
                    preloadSuperFlag,
                    suppressArgumentsFlag,
                    preloadArgumentsFlag,
                    suppressThisFlag,
                    preloadThisFlag,
                    preloadGlobalFlag,
                    regCount, len, SWF.DEFAULT_VERSION, paramNames, paramRegs, charset));
        }

        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false; //function actually returns itself, but here is false for generator to not add Pop
    }

    @Override
    public boolean hasSideEffect() {
        return true; //??
    }
    //What about hashcode and equals? Probably not.
}
