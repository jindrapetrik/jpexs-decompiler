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
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionCallMethod;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionNewObject;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf7.ActionExtends;
import com.jpexs.decompiler.flash.action.swf7.ActionImplementsOp;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * ActionScript 1/2 bytecode generator.
 *
 * @author JPEXS
 */
public class ActionSourceGenerator implements SourceGenerator {

    private final List<String> constantPool;
    
    private int constantPoolLength;

    private final int swfVersion;

    private final String charset;

    private long uniqLast = 0;

    /**
     * Constructor.
     *
     * @param swfVersion SWF version
     * @param constantPool Constant pool
     * @param constantPoolLength Constant pool length
     * @param charset Charset
     */
    public ActionSourceGenerator(int swfVersion, List<String> constantPool, int constantPoolLength, String charset) {
        this.constantPool = constantPool;
        this.swfVersion = swfVersion;
        this.constantPoolLength = constantPoolLength;
        this.charset = charset;
    }

    /**
     * Generates unique ID.
     *
     * @return Unique ID
     */
    public String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    /**
     * Gets charset.
     *
     * @return Charset
     */
    public String getCharset() {
        return charset;
    }

    private List<Action> generateToActionList(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException {
        return toActionList(generate(localData, commands));
    }

    private List<Action> generateToActionList(SourceGeneratorLocalData localData, GraphTargetItem command) throws CompilationException {
        return toActionList(command.toSource(localData, this));
    }

    /**
     * Converts list of GraphSourceItem to list of Action.
     *
     * @param items List of GraphSourceItem
     * @return List of Action
     */
    public List<Action> toActionList(List<GraphSourceItem> items) {
        items = groupPushes(items);
        List<Action> ret = new ArrayList<>();
        for (GraphSourceItem s : items) {
            if (s instanceof Action) {
                ret.add((Action) s);
            }
        }
        return ret;
    }

    private List<GraphSourceItem> groupPushes(List<GraphSourceItem> items) {

        if (swfVersion <= 4) {
            return items;
        }
        

        return items;
        
        //TODO: This should take into account important offsets (jumps)
        //And not group Pushes over different parts of code
        /*Like:
           If locA
             Push "A"
             Push "B"
           locA:
           Push "C"
        
           Should not be grouped to Push "A","B","C"
         
        
        //commented out for now...
        Map<GraphSourceItem, Long> addresses = new LinkedHashMap<>();
        List<Long> referencedAddresses = new ArrayList<>();
        long addr = 0;
        for (GraphSourceItem item : items) {
            addresses.put(item, addr);
            if (item instanceof ActionIf) {
                ActionIf aIf = (ActionIf) item;
                referencedAddresses.add(addr + 5 + aIf.getJumpOffset());
            }
            if (item instanceof ActionJump) {
                ActionJump aJump = (ActionJump) item;
                referencedAddresses.add(addr + 5 + aJump.getJumpOffset());
            }
            addr += item.getBytesLength();            
        }
        
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionPush prevPush = null;
        for (GraphSourceItem s : items) {            
            if (s instanceof ActionPush) {
                if (prevPush == null) {
                    prevPush = (ActionPush) s;
                    ret.add(prevPush);                    
                } else {
                    long itemAddr = addresses.get(s);
                    if (referencedAddresses.contains(itemAddr)) {
                        prevPush = (ActionPush) s;
                        ret.add(prevPush);
                    } else {
                        prevPush.values.addAll(((ActionPush) s).values);
                        ((ActionPush) s).values.clear();
                    }
                }
            } else {
                ret.add(s);
                prevPush = null;
            }                      
        }
        return ret;   */                          
    }

    private List<Action> nonempty(List<Action> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private List<GraphSourceItem> generateIf(SourceGeneratorLocalData localData, GraphTargetItem expression, List<GraphTargetItem> onTrueCmds, List<GraphTargetItem> onFalseCmds, boolean ternar, int errorLine, String errorTitle) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        if (expression instanceof NotItem) {
            ret.addAll(expression.value.toSource(localData, this));
        } else {
            ret.addAll(expression.toSource(localData, this));
            ret.add(new ActionNot());
        }
        List<Action> onTrue;
        List<Action> onFalse = null;
        if (ternar) {
            onTrue = toActionList(onTrueCmds.get(0).toSource(localData, this));
        } else {
            onTrue = generateToActionList(localData, onTrueCmds);
        }

        if (onFalseCmds != null && !onFalseCmds.isEmpty()) {
            if (ternar) {
                onFalse = toActionList(onFalseCmds.get(0).toSource(localData, this));
            } else {
                onFalse = generateToActionList(localData, onFalseCmds);
            }
        }
        byte[] onTrueBytes = Action.actionsToBytes(onTrue, false, SWF.DEFAULT_VERSION);
        int onTrueLen = onTrueBytes.length;
        
        if (onTrueLen > 32767) {
            throw new CompilationException("Generated offset for onTrue part of " + errorTitle + " is larger than maximum allowed for SI16.", errorLine);
        }

        ActionIf ifaif = new ActionIf(0, charset);
        ret.add(ifaif);
        ret.addAll(onTrue);
        ifaif.setJumpOffset(onTrueLen);
        ActionJump ajmp = null;
        if (onFalse != null) {
            if (onTrueCmds.isEmpty()
                    || !((onTrueCmds.get(onTrueCmds.size() - 1) instanceof ContinueItem)
                    || (onTrueCmds.get(onTrueCmds.size() - 1) instanceof BreakItem))) {
                ajmp = new ActionJump(0, charset);
                ret.add(ajmp);
                onTrueLen += ajmp.getTotalActionLength();
            }
            if (onTrueLen > 32767) {
                throw new CompilationException("Generated offset for onTrue part of " + errorTitle + " is larger than maximum allowed for SI16.", errorLine);
            }
            ifaif.setJumpOffset(onTrueLen);
            byte[] onFalseBytes = Action.actionsToBytes(onFalse, false, SWF.DEFAULT_VERSION);
            int onFalseLen = onFalseBytes.length;
            if (ajmp != null) {
                ajmp.setJumpOffset(onFalseLen);
            }
            if (onTrueLen > 32767) {
                throw new CompilationException("Generated offset for onFalse part of " + errorTitle + " is larger than maximum allowed for SI16.", errorLine);
            }
            ret.addAll(onFalse);
        }
        return ret;
    }

    private void fixLoop(List<Action> code, int breakOffset, GraphTargetItem item) throws CompilationException {
        fixLoop(code, breakOffset, Integer.MAX_VALUE, item);
    }

    private void fixLoop(List<Action> code, int breakOffset, int continueOffset, GraphTargetItem item) throws CompilationException {
        int pos = 0;
        for (Action a : code) {
            pos += a.getBytesLength();
            if (a instanceof ActionJump) {
                ActionJump aj = (ActionJump) a;
                if (aj.isContinue && (continueOffset != Integer.MAX_VALUE)) {
                    int nOffset = -pos + continueOffset;
                    if (nOffset < -32768 || nOffset > 32767) {
                        throw new CompilationException("Generated offset for Continue is outside bounds of SI16.", item.line);
                    } 
                    
                    aj.setJumpOffset(nOffset);
                    aj.isContinue = false;
                }
                if (aj.isBreak) {
                    int nOffset = -pos + breakOffset;
                    if (nOffset < -32768 || nOffset > 32767) {
                        throw new CompilationException("Generated offset for Break is outside bounds of SI16.", item.line);
                    } 
                    aj.setJumpOffset(nOffset);
                    aj.isBreak = false;
                }
            }
        }
    }

    /**
     * Gets register variables.
     *
     * @param localData Local data
     * @return Register variables
     */
    public HashMap<String, Integer> getRegisterVars(SourceGeneratorLocalData localData) {
        return localData.registerVars;
    }

    /**
     * Sets register variables.
     *
     * @param localData Local data
     * @param value Register variables
     */
    public void setRegisterVars(SourceGeneratorLocalData localData, HashMap<String, Integer> value) {
        localData.registerVars = value;
    }

    /**
     * Sets in function.
     *
     * @param localData Local data
     * @param value Value
     */
    public void setInFunction(SourceGeneratorLocalData localData, int value) {
        localData.inFunction = value;
    }

    /**
     * Gets in function.
     *
     * @param localData Local data
     * @return Value
     */
    public int isInFunction(SourceGeneratorLocalData localData) {
        return localData.inFunction;
    }

    /**
     * Checks if in method.
     *
     * @param localData Local data
     * @return True if in method
     */
    public boolean isInMethod(SourceGeneratorLocalData localData) {
        return localData.inMethod;
    }

    /**
     * Sets in method.
     *
     * @param localData Local data
     * @param value Value
     */
    public void setInMethod(SourceGeneratorLocalData localData, boolean value) {
        localData.inMethod = value;
    }

    /**
     * Gets for in level.
     *
     * @param localData Local data
     * @return For in level
     */
    public int getForInLevel(SourceGeneratorLocalData localData) {
        return localData.forInLevel;
    }

    /**
     * Sets for in level.
     *
     * @param localData Local data
     * @param value Value
     */
    public void setForInLevel(SourceGeneratorLocalData localData, int value) {
        localData.forInLevel = value;
    }

    /**
     * Gets temp register.
     *
     * @param localData Local data
     * @return Temp register
     */
    public int getTempRegister(SourceGeneratorLocalData localData) {
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        for (int tmpReg = 0; tmpReg < 256; tmpReg++) {
            if (!registerVars.containsValue(tmpReg)) {
                registerVars.put("__temp" + tmpReg, tmpReg);
                return tmpReg;
            }
        }
        return 0; //?
    }

    /**
     * Releases temp register.
     *
     * @param localData Local data
     * @param tmp Temp register
     */
    public void releaseTempRegister(SourceGeneratorLocalData localData, int tmp) {
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        registerVars.remove("__temp" + tmp);
    }

    private String getName(GraphTargetItem item) {
        if (item instanceof VariableActionItem) {
            return ((VariableActionItem) item).getVariableName();
        }
        if (item instanceof DirectValueActionItem) {
            DirectValueActionItem dv = (DirectValueActionItem) item;
            return (String) dv.getResult();
        }
        if (item instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) item;
            return getName(gv.name);
        }
        if (item instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) item;
            return getName(mem.memberName);
        }
        return null;
    }

    private List<String> getVarParts(GraphTargetItem item) {
        List<String> ret = new ArrayList<>();
        do {
            if (item instanceof GetMemberActionItem) {
                GetMemberActionItem mem = (GetMemberActionItem) item;
                ret.add(0, getName(mem));
                item = mem.object;
            }
        } while (item instanceof GetMemberActionItem);
        String f = getName(item);
        if (f != null) {
            ret.add(0, f);
        }
        return ret;
    }

    private int getVarLength(GraphTargetItem item) {
        int len = 1;
        do {
            if (item instanceof GetMemberActionItem) {
                GetMemberActionItem mem = (GetMemberActionItem) item;
                item = mem.object;
                len++;
            }
        } while (item instanceof GetMemberActionItem);
        return len;
    }

    private GraphTargetItem removeVarLast(GraphTargetItem item, int cnt) {
        item = Helper.deepCopy(item);

        for (int i = 0; i < cnt; i++) {
            if (item instanceof GetMemberActionItem) {
                GetMemberActionItem mem = (GetMemberActionItem) item;
                item = mem.object;
            }
        }
        return item;
    }

    private GraphTargetItem addGlobalPrefix(GraphTargetItem item) {
        item = Helper.deepCopy(item);
        GraphTargetItem first = item;
        GetMemberActionItem mem = null;
        do {
            if (item instanceof GetMemberActionItem) {
                mem = (GetMemberActionItem) item;
                item = mem.object;
            }
        } while (item instanceof GetMemberActionItem);
        if (item instanceof GetVariableActionItem) {
            GetVariableActionItem v = (GetVariableActionItem) item;
            item = new GetMemberActionItem(null, null, new GetVariableActionItem(null, null, new DirectValueActionItem(null, null, 0, "_global", new ArrayList<>())), v.name);
            if (mem != null) {
                mem.object = item;
            }
        }
        return first;
    }

    private List<Action> typeToActions(List<String> type, List<Action> value) {
        List<Action> ret = new ArrayList<>();
        if (type.isEmpty()) {
            return ret;
        }
        ret.add(pushConst(type.get(0)));
        if (type.size() == 1 && (value != null)) {
            ret.addAll(value);
            ret.add(new ActionSetVariable());
        } else {
            ret.add(new ActionGetVariable());
        }
        for (int i = 1; i < type.size(); i++) {
            ret.add(pushConst(type.get(i)));
            if ((i == type.size() - 1) && (value != null)) {
                ret.addAll(value);
                ret.add(new ActionSetMember());
            } else {
                ret.add(new ActionGetMember());
            }
        }
        return ret;
    }

    /**
     * Gets SWF version.
     *
     * @return SWF version
     */
    public int getSwfVersion() {
        return swfVersion;
    }

    /**
     * Gets constant pool.
     *
     * @return Constant pool
     */
    public List<String> getConstantPool() {
        return constantPool;
    }

    /**
     * Gets Push constant item.
     *
     * @param s Constant
     * @return Push constant item
     */
    public DirectValueActionItem pushConstTargetItem(String s) {
        
        //ActionConstantPool was introduced in SWF 5
        if (swfVersion < 5) {
            return new DirectValueActionItem(null, null, 0, s, constantPool);
        }

        int index = constantPool.indexOf(s);
        if (index == -1) {
            int newItemLen = ActionConstantPool.calculateSize(s, charset);
            if (constantPool.size() < 0xffff 
                    && constantPoolLength + newItemLen <= 0xffff) {
                // constant pool is not full
                constantPool.add(s);
                index = constantPool.indexOf(s);
                constantPoolLength += newItemLen;
            }
        }

        if (index == -1) {
            return new DirectValueActionItem(null, null, 0, s, constantPool);
        }

        return new DirectValueActionItem(null, null, 0, new ConstantIndex(index), constantPool);                
    }

    /**
     * Gets Push constant action.
     *
     * @param s Constant
     * @return Push constant action
     */
    public ActionPush pushConst(String s) {
        
        if (swfVersion < 5) {
            return new ActionPush(s, charset);
        }        
        
        int index = constantPool.indexOf(s);
        if (index == -1) {            
            
            if (constantPool.size() == 0xffff) {
                return new ActionPush(s, charset);
            }                
            
            int newItemLen = ActionConstantPool.calculateSize(s, charset);
            
            //constant pool is full
            if (constantPoolLength + newItemLen > 0xffff) {
                return new ActionPush(s, charset);
            }
            
            constantPool.add(s);
            index = constantPool.indexOf(s);
            constantPoolLength += newItemLen;
        }
        return new ActionPush(new ConstantIndex(index), charset);
    }

    @Override
    public List<GraphSourceItem> generateDiscardValue(SourceGeneratorLocalData localData, GraphTargetItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>(item.toSource(localData, this));
        ret.add(new ActionPop());
        return ret;
    }

    /**
     * Generates traits.
     *
     * @param localData Local data
     * @param isInterface Is interface
     * @param name Name
     * @param extendsVal Extends value
     * @param implementsStr Implements
     * @param traits Traits
     * @param traitsStatic Static traits
     * @return List of GraphSourceItem
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> generateTraits(SourceGeneratorLocalData localData, boolean isInterface, GraphTargetItem name, GraphTargetItem extendsVal, List<GraphTargetItem> implementsStr, List<MyEntry<GraphTargetItem, GraphTargetItem>> traits, List<Boolean> traitsStatic) throws CompilationException {
        List<String> extendsStr = getVarParts(extendsVal);
        List<GraphSourceItem> ret = new ArrayList<>();
        List<String> nameStr = getVarParts(name);
        for (int i = 0; i < nameStr.size() - 1; i++) {
            List<Action> notBody = new ArrayList<>();
            List<String> globalClassTypeStr = new ArrayList<>();
            globalClassTypeStr.add("_global");
            for (int j = 0; j <= i; j++) {
                globalClassTypeStr.add(nameStr.get(j));
            }

            List<Action> val = new ArrayList<>();
            val.add(new ActionPush((Double) 0.0, charset));
            val.add(pushConst("Object"));
            val.add(new ActionNewObject());
            notBody.addAll(typeToActions(globalClassTypeStr, val));
            ret.addAll(typeToActions(globalClassTypeStr, null));
            ret.add(new ActionNot());
            ret.add(new ActionNot());
            ret.add(new ActionIf(Action.actionsToBytes(notBody, false, SWF.DEFAULT_VERSION).length, charset));
            ret.addAll(notBody);
            ret.add(new ActionPop());
        }
        List<Action> ifbody = new ArrayList<>();
        List<String> globalClassTypeStr = new ArrayList<>();
        globalClassTypeStr.add("_global");
        globalClassTypeStr.addAll(nameStr);

        String constructorName = nameStr.get(nameStr.size() - 1); //com.jpexs.MyClass => MyClass
        GraphTargetItem constructor = null;
        int constructorIndex = -1;
        for (int t = 0; t < traits.size(); t++) {
            MyEntry<GraphTargetItem, GraphTargetItem> en = traits.get(t);
            if (en.getValue() instanceof FunctionActionItem) {
                if (constructorName.equals(getName(en.getKey()))) {
                    constructorIndex = t;
                    constructor = en.getValue();
                    break;
                }
            }
        }
        ParsedSymbol s = null;
        List<Action> constr = new ArrayList<>();

        if (constructor == null) {
            List<Action> val = new ArrayList<>();
            val.add(new ActionDefineFunction("", new ArrayList<>(), 0, SWF.DEFAULT_VERSION, charset));
            if (!isInterface) {
                val.add(new ActionStoreRegister(1, charset));
            }
            constr.addAll(typeToActions(globalClassTypeStr, val));
        } else {
            constr.addAll(toActionList(((FunctionActionItem) constructor).toSource(localData, this)));
            constr.add(new ActionStoreRegister(1, charset));
            constr = (typeToActions(globalClassTypeStr, constr));
        }

        Set<String> properties = new LinkedHashSet<>();
        Set<String> setters = new HashSet<>();
        Set<String> getters = new HashSet<>();

        Set<String> staticProperties = new LinkedHashSet<>();
        Set<String> staticSetters = new HashSet<>();
        Set<String> staticGetters = new HashSet<>();

        if (!isInterface) {
            for (int pass = 1; pass <= 2; pass++) { //two passes, methods first, then variables
                for (int t = 0; t < traits.size(); t++) {
                    if (constructorIndex == t) { //constructor already handled
                        continue;
                    }
                    MyEntry<GraphTargetItem, GraphTargetItem> en = traits.get(t);
                    boolean isFunc = (en.getValue() instanceof FunctionActionItem);
                    if (pass == 1 && isFunc) { //Add methods in first pass
                        FunctionActionItem fi = (FunctionActionItem) en.getValue();
                        if (fi.isGetter || fi.isSetter) {
                            (traitsStatic.get(t) ? staticProperties : properties).add(fi.calculatedFunctionName.toString());
                        }

                        if (!FunctionActionItem.DECOMPILE_GET_SET) {
                            if (fi.calculatedFunctionName.toString().startsWith("__get__") || fi.calculatedFunctionName.toString().startsWith("__set__")) {
                                (traitsStatic.get(t) ? staticProperties : properties).add(fi.calculatedFunctionName.toString().substring(7));
                            }
                            if (fi.calculatedFunctionName.toString().startsWith("__get__")) {
                                (traitsStatic.get(t) ? staticGetters : getters).add(fi.calculatedFunctionName.toString().substring(7));
                            }
                            if (fi.calculatedFunctionName.toString().startsWith("__set__")) {
                                (traitsStatic.get(t) ? staticSetters : setters).add(fi.calculatedFunctionName.toString().substring(7));
                            }
                        }

                        String prefix = "";
                        if (fi.isGetter) { //really has "get" keyword
                            (traitsStatic.get(t) ? staticGetters : getters).add(fi.calculatedFunctionName.toString());
                            prefix = "__get__";
                        }
                        if (fi.isSetter) { //really has "set" keyword
                            (traitsStatic.get(t) ? staticSetters : setters).add(fi.calculatedFunctionName.toString());
                            prefix = "__set__";
                        }
                        ifbody.add(new ActionPush(new RegisterNumber(traitsStatic.get(t) ? 1 : 2), charset));
                        ifbody.add(pushConst(prefix + getName(en.getKey())));
                        ifbody.addAll(toActionList(fi.toSource(localData, this)));
                        ifbody.add(new ActionSetMember());
                    } else if (pass == 2 && !isFunc) { //add variables in second pass
                        ifbody.add(new ActionPush(new RegisterNumber(traitsStatic.get(t) ? 1 : 2), charset));
                        ifbody.add(pushConst(getName(en.getKey())));
                        ifbody.addAll(toActionList(en.getValue().toSource(localData, this)));
                        ifbody.add(new ActionSetMember());
                    }
                }
            }
        }

        for (String prop : staticProperties) {
            if (staticSetters.contains(prop)) {
                ifbody.add(new ActionPush(new Object[]{new RegisterNumber(1), "__set__" + prop}, charset));
                ifbody.add(new ActionGetMember());
            } else {
                ifbody.add(new ActionDefineFunction("", new ArrayList<String>(), 0, swfVersion, charset));
            }
            if (staticGetters.contains(prop)) {
                ifbody.add(new ActionPush(new Object[]{new RegisterNumber(1), "__get__" + prop}, charset));
                ifbody.add(new ActionGetMember());
            } else {
                ifbody.add(new ActionDefineFunction("", new ArrayList<String>(), 0, swfVersion, charset));
            }
            ifbody.add(new ActionPush(new Object[]{prop, 3, new RegisterNumber(1), "addProperty"}, charset));
            ifbody.add(new ActionCallMethod());
        }

        for (String prop : properties) {
            if (setters.contains(prop)) {
                ifbody.add(new ActionPush(new Object[]{new RegisterNumber(2), "__set__" + prop}, charset));
                ifbody.add(new ActionGetMember());
            } else {
                ifbody.add(new ActionDefineFunction("", new ArrayList<String>(), 0, swfVersion, charset));
            }
            if (getters.contains(prop)) {
                ifbody.add(new ActionPush(new Object[]{new RegisterNumber(2), "__get__" + prop}, charset));
                ifbody.add(new ActionGetMember());
            } else {
                ifbody.add(new ActionDefineFunction("", new ArrayList<String>(), 0, swfVersion, charset));
            }
            ifbody.add(new ActionPush(new Object[]{prop, 3, new RegisterNumber(2), "addProperty"}, charset));
            ifbody.add(new ActionCallMethod());
        }

        if (!isInterface) {
            ifbody.add(new ActionPush((Long) 1L, charset));
            ifbody.add(new ActionPush(Null.INSTANCE, charset));
            ifbody.addAll(typeToActions(globalClassTypeStr, null));
            ifbody.add(pushConst("prototype"));
            ifbody.add(new ActionGetMember());
            ifbody.add(new ActionPush((Long) 3L, charset));
            ifbody.add(pushConst("ASSetPropFlags"));
            ifbody.add(new ActionCallFunction());
        }

        if (constr.isEmpty()) {
            List<Action> val = new ArrayList<>();
            val.add(new ActionDefineFunction("", new ArrayList<>(), 0, SWF.DEFAULT_VERSION, charset));
            if (!isInterface) {
                val.add(new ActionStoreRegister(1, charset));
            }
            constr.addAll(typeToActions(globalClassTypeStr, val));
        }
        if (!extendsStr.isEmpty()) {
            constr.addAll(typeToActions(globalClassTypeStr, null));
            constr.addAll(typeToActions(extendsStr, null));
            constr.add(new ActionExtends(charset));
        }
        if (!isInterface) {
            constr.add(new ActionPush(new RegisterNumber(1), charset));
            constr.add(pushConst("prototype"));
            constr.add(new ActionGetMember());
            constr.add(new ActionStoreRegister(2, charset));
            constr.add(new ActionPop());
        }

        if (!implementsStr.isEmpty()) {
            for (GraphTargetItem imp : implementsStr) {
                List<String> impList = getVarParts(imp);
                List<String> globImp = new ArrayList<>();
                globImp.add("_global");
                globImp.addAll(impList);
                constr.addAll(typeToActions(globImp, null));
            }
            constr.add(new ActionPush((long) implementsStr.size(), charset));
            constr.addAll(typeToActions(globalClassTypeStr, null));
            constr.add(new ActionImplementsOp(charset));
        }
        ifbody.addAll(0, constr);

        ret.addAll(typeToActions(globalClassTypeStr, null));
        ret.add(new ActionNot());
        ret.add(new ActionNot());
        int ifOffset = Action.actionsToBytes(ifbody, false, SWF.DEFAULT_VERSION).length;
        if (ifOffset > 0x7fff) {
            if (!localData.secondRun) { //to avoid printing the warning twice
                Logger.getLogger(ActionSourceGenerator.class.getName()).warning("The class is long. If offset for its header would be longer than max value for SI16, 0 was stored instead. This should not have any negative impact.");
            }
            ifOffset = 0;
        }
        ret.add(new ActionIf(ifOffset, charset));
        ret.addAll(ifbody);
        ret.add(new ActionPop());
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, FalseItem item) throws CompilationException {
        return GraphTargetItem.toSourceMerge(localData, this, new ActionPush(Boolean.FALSE, charset));
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TrueItem item) throws CompilationException {
        return GraphTargetItem.toSourceMerge(localData, this, new ActionPush(Boolean.TRUE, charset));
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, AndItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        ret.add(new ActionPushDuplicate());
        ret.add(new ActionNot());
        List<Action> andExpr = generateToActionList(localData, item.rightSide);
        andExpr.add(0, new ActionPop());
        int andExprLen = Action.actionsToBytes(andExpr, false, SWF.DEFAULT_VERSION).length;
        ret.add(new ActionIf(andExprLen, charset));
        ret.addAll(andExpr);
        return ret;

    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, OrItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        ret.add(new ActionPushDuplicate());
        List<Action> orExpr = generateToActionList(localData, item.rightSide);
        orExpr.add(0, new ActionPop());
        int orExprLen = Action.actionsToBytes(orExpr, false, SWF.DEFAULT_VERSION).length;
        ret.add(new ActionIf(orExprLen, charset));
        ret.addAll(orExpr);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) throws CompilationException {
        return generateIf(localData, item.expression, item.onTrue, item.onFalse, false, item.line, "If");
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TernarOpItem item) throws CompilationException {
        List<GraphTargetItem> onTrue = new ArrayList<>();
        onTrue.add(item.onTrue);
        List<GraphTargetItem> onFalse = new ArrayList<>();
        onFalse.add(item.onFalse);
        return generateIf(localData, item.expression, onTrue, onFalse, true, item.line, "Ternar");
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WhileItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<Action> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        if (!ex.isEmpty()) {
            GraphTargetItem lastItem = ex.remove(ex.size() - 1);
            whileExpr.addAll(generateToActionList(localData, ex));
            whileExpr.addAll(toActionList(lastItem.toSource(localData, this))); //Want result
        }

        List<Action> whileBody = generateToActionList(localData, item.commands);
        whileExpr.add(new ActionNot());
        ActionIf whileaif = new ActionIf(0, charset);
        whileExpr.add(whileaif);
        ActionJump whileajmp = new ActionJump(0, charset);
        whileBody.add(whileajmp);
        int whileExprLen = Action.actionsToBytes(whileExpr, false, SWF.DEFAULT_VERSION).length;
        int whileBodyLen = Action.actionsToBytes(whileBody, false, SWF.DEFAULT_VERSION).length;
        
        if (whileBodyLen > 32767) {
            throw new CompilationException("Generated offset for While is larger than maximum allowed for SI16.", item.line);
        }
        
        int whileJumpOffset = -(whileExprLen + whileBodyLen);
        if (whileJumpOffset < -32768) {
            throw new CompilationException("Generated offset for While is lower than mininum allowed for SI16.", item.line);
        }        
        whileajmp.setJumpOffset(whileJumpOffset);
        whileaif.setJumpOffset(whileBodyLen);
        ret.addAll(whileExpr);
        fixLoop(whileBody, whileBodyLen, -whileExprLen, item);
        ret.addAll(whileBody);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<Action> doExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        if (!ex.isEmpty()) {
            GraphTargetItem lastItem = ex.remove(ex.size() - 1);
            doExpr.addAll(generateToActionList(localData, ex));
            doExpr.addAll(toActionList(lastItem.toSource(localData, this))); //Want result
        }
        List<Action> doBody = generateToActionList(localData, item.commands);

        int doBodyLen = Action.actionsToBytes(doBody, false, SWF.DEFAULT_VERSION).length;
        int doExprLen = Action.actionsToBytes(doExpr, false, SWF.DEFAULT_VERSION).length;

        ret.addAll(doBody);
        ret.addAll(doExpr);
        ActionIf doif = new ActionIf(0, charset);
        ret.add(doif);
        int offset = doBodyLen + doExprLen + doif.getTotalActionLength();       
        if (-offset < -32768) {
            throw new CompilationException("Generated offset for DoWhile is lower than mininum allowed for SI16.", item.line);
        }  
        
        doif.setJumpOffset(-offset);
        fixLoop(doBody, offset, doBodyLen, item);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<Action> forExpr = generateToActionList(localData, item.expression);
        List<Action> forBody = generateToActionList(localData, item.commands);
        List<Action> forFinalCommands = generateToActionList(localData, item.finalCommands);

        forExpr.add(new ActionNot());
        ActionIf foraif = new ActionIf(0, charset);
        forExpr.add(foraif);
        ActionJump forajmp = new ActionJump(0, charset);
        int forajmpLen = forajmp.getTotalActionLength();
        int forExprLen = Action.actionsToBytes(forExpr, false, SWF.DEFAULT_VERSION).length;
        int forBodyLen = Action.actionsToBytes(forBody, false, SWF.DEFAULT_VERSION).length;
        int forFinalLen = Action.actionsToBytes(forFinalCommands, false, SWF.DEFAULT_VERSION).length;
        int jmpOffset = -(forExprLen + forBodyLen + forFinalLen + forajmpLen);
        int ifOffset = forBodyLen + forFinalLen + forajmpLen;
        if (jmpOffset < -32768) {
            throw new CompilationException("Generated offset for For is lower than mininum allowed for SI16.", item.line);
        }
        if (ifOffset > 32767) {
            throw new CompilationException("Generated offset for For is larger than maximum allowed for SI16.", item.line);
        }
        
        forajmp.setJumpOffset(jmpOffset);
        foraif.setJumpOffset(ifOffset);
        ret.addAll(generateToActionList(localData, item.firstCommands));
        ret.addAll(forExpr);
        ret.addAll(forBody);
        ret.addAll(forFinalCommands);
        ret.add(forajmp);
        fixLoop(forBody, forBodyLen + forFinalLen + forajmpLen, forBodyLen, item);
        return ret;
    }        
    
    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwitchItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        int exprReg = 0;
        for (int i = 0; i < 256; i++) {
            if (!registerVars.containsValue(i)) {
                registerVars.put("__switch" + uniqId(), i);
                exprReg = i;
                break;
            }
        }

        ret.addAll(toActionList(item.switchedObject.toSource(localData, this)));

        boolean firstCase = true;
        List<List<ActionIf>> caseIfs = new ArrayList<>();
        List<List<Action>> caseCmds = new ArrayList<>();
        List<List<List<Action>>> caseExprsAll = new ArrayList<>();

        int defaultPos = -1;

        loopm:
        for (int m = 0; m < item.caseValues.size(); m++) {
            List<List<Action>> caseExprs = new ArrayList<>();
            List<ActionIf> caseIfsOne = new ArrayList<>();
            int mapping = item.valuesMapping.get(m);

            for (; m < item.caseValues.size(); m++) {
                int newmapping = item.valuesMapping.get(m);
                if (newmapping != mapping) {
                    m--;
                    break;
                }

                if (item.caseValues.get(m) instanceof DefaultItem) {
                    defaultPos = caseIfs.size();
                } else {
                    List<Action> curCaseExpr = generateToActionList(localData, item.caseValues.get(m));
                    caseExprs.add(curCaseExpr);
                    if (firstCase) {
                        curCaseExpr.add(0, new ActionStoreRegister(exprReg, charset));
                    } else {
                        curCaseExpr.add(0, new ActionPush(new RegisterNumber(exprReg), charset));
                    }
                    curCaseExpr.add(new ActionStrictEquals());
                    ActionIf aif = new ActionIf(0, charset);
                    caseIfsOne.add(aif);
                    curCaseExpr.add(aif);
                    ret.addAll(curCaseExpr);
                }
                firstCase = false;
            }
            caseExprsAll.add(caseExprs);
            caseIfs.add(caseIfsOne);
            List<Action> caseCmd = generateToActionList(localData, item.caseCommands.get(mapping));
            caseCmds.add(caseCmd);
        }

        ActionJump defJump = new ActionJump(0, charset);
        ret.add(defJump);
        for (List<Action> caseCmd : caseCmds) {
            ret.addAll(caseCmd);
        }

        List<List<Integer>> exprLengths = new ArrayList<>();
        for (List<List<Action>> caseExprs : caseExprsAll) {
            List<Integer> lengths = new ArrayList<>();
            for (List<Action> caseExpr : caseExprs) {
                lengths.add(Action.actionsToBytes(caseExpr, false, SWF.DEFAULT_VERSION).length);
            }
            exprLengths.add(lengths);
        }
        List<Integer> caseLengths = new ArrayList<>();
        for (List<Action> caseCmd : caseCmds) {
            caseLengths.add(Action.actionsToBytes(caseCmd, false, SWF.DEFAULT_VERSION).length);
        }

        for (int i = 0; i < caseIfs.size(); i++) {
            for (int c = 0; c < caseIfs.get(i).size(); c++) {
                int jmpPos = 0;
                for (int j = c + 1; j < caseIfs.get(i).size(); j++) {
                    jmpPos += exprLengths.get(i).get(j);
                }
                for (int k = i + 1; k < caseIfs.size(); k++) {
                    for (int m = 0; m < caseIfs.get(k).size(); m++) {
                        jmpPos += exprLengths.get(k).get(m);
                    }
                }
                jmpPos += defJump.getTotalActionLength();
                for (int n = 0; n < i; n++) {
                    jmpPos += caseLengths.get(n);
                }
                checkOffsetBounds(jmpPos, "Switch", item.line);
                caseIfs.get(i).get(c).setJumpOffset(jmpPos);
            }
        }
        int defJmpPos = 0;
        for (int i = 0; i < caseIfs.size(); i++) {
            if (defaultPos == -1 || i < defaultPos) {
                defJmpPos += caseLengths.get(i);
            }
        }

        checkOffsetBounds(defJmpPos, "Switch", item.line);
                
        defJump.setJumpOffset(defJmpPos);
        List<Action> caseCmdsAll = new ArrayList<>();
        int breakOffset = 0;
        for (int i = 0; i < caseCmds.size(); i++) {
            caseCmdsAll.addAll(caseCmds.get(i));
            breakOffset += caseLengths.get(i);
        }
        fixLoop(caseCmdsAll, breakOffset, item);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, NotItem item) throws CompilationException {
        /*if (item.value instanceof NotItem) {
            return item.value.value.toSource(localData, this);
        }*/
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.getOriginal().toSource(localData, this));
        ret.add(new ActionNot());
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DuplicateItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(new ActionPushDuplicate());
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, BreakItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionJump abreak = new ActionJump(0, charset);
        abreak.isBreak = true;
        ret.add(abreak);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionJump acontinue = new ActionJump(0, charset);
        acontinue.isContinue = true;
        ret.add(acontinue);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (GraphTargetItem item : commands) {
            ret.addAll(item.toSourceIgnoreReturnValue(localData, this));
        }
        ret = groupPushes(ret);
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
        //Unsupported in AS1/2
        return new ArrayList<>();
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
    
    private int checkOffsetBounds(int offset, String errorItem, int errorLine) throws CompilationException {
        if (offset < -32768) {
            throw new CompilationException("Generated offset for " + errorItem + " is lower than mininum allowed for SI16.", errorLine);
        }
        if (offset > 32767) {
            throw new CompilationException("Generated offset for " + errorItem + " is larger than maximum allowed for SI16.", errorLine);
        }
        return offset;
    }
}
