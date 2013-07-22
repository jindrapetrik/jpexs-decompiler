/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class FunctionActionItem extends ActionItem {

    public List<GraphTargetItem> actions;
    public List<String> constants;
    public String functionName;
    public List<String> paramNames;
    public GraphTargetItem calculatedFunctionName;
    private int regStart;
    public static final int REGISTER_THIS = 1;
    public static final int REGISTER_ARGUMENTS = 2;
    public static final int REGISTER_SUPER = 3;
    public static final int REGISTER_ROOT = 4;
    public static final int REGISTER_PARENT = 5;
    public static final int REGISTER_GLOBAL = 6;

    public FunctionActionItem() {
        super(null, PRECEDENCE_PRIMARY);
    }

    public FunctionActionItem(GraphSourceItem instruction, String functionName, List<String> paramNames, List<GraphTargetItem> actions, List<String> constants, int regStart) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.actions = actions;
        this.constants = constants;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.regStart = regStart;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (true) {
            //return "<func>";
        }
        String ret = hilight("function");
        if (calculatedFunctionName != null) {
            ret += " " + calculatedFunctionName.toStringNoQuotes(constants);
        } else if (!functionName.equals("")) {
            ret += " " + functionName;
        }
        ret += hilight("(");
        for (int p = 0; p < paramNames.size(); p++) {
            if (p > 0) {
                ret += hilight(", ");
            }
            String pname = paramNames.get(p);
            if (pname == null || pname.equals("")) {
                pname = "register" + (regStart + p);
            }
            ret += hilight(pname);
        }
        ret += hilight(")") + "\r\n{\r\n" + Graph.graphToString(actions, constants) + "}";
        return ret;
    }

    @Override
    public List<com.jpexs.decompiler.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.graph.GraphSourceItemPos> ret = super.getNeededSources();
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
    public boolean isCompileTime() {
        for (GraphTargetItem a : actions) {
            if (!a.isCompileTime()) {
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
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        List<Integer> paramRegs = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object> localDataCopy = (List<Object>) Helper.deepCopy(localData);
        HashMap<String, Integer> registerVars = asGenerator.getRegisterVars(localDataCopy);
        registerVars.put("_parent", REGISTER_PARENT);
        registerVars.put("_root", REGISTER_ROOT);
        registerVars.put("super", REGISTER_SUPER);
        registerVars.put("arguments", REGISTER_ARGUMENTS);
        registerVars.put("this", REGISTER_THIS);
        registerVars.put("_global", REGISTER_GLOBAL);
        for (int i = 0; i < paramNames.size(); i++) {
            registerVars.put(paramNames.get(i), (7 + i)); //(paramNames.size() - i)));
        }
        boolean preloadParentFlag = false;
        boolean preloadRootFlag = false;
        boolean preloadSuperFlag = false;
        boolean preloadArgumentsFlag = false;
        boolean preloadThisFlag = false;
        boolean preloadGlobalFlag = false;

        boolean suppressParentFlag = false;
        boolean suppressArgumentsFlag = false;
        boolean suppressThisFlag = false;
        TreeSet<Integer> usedRegisters = new TreeSet<>();
        if (actions != null && !actions.isEmpty()) {
            asGenerator.setInFunction(localDataCopy, true);
            List<Action> body = asGenerator.toActionList(asGenerator.generate(localDataCopy, actions));
            for (Action a : body) {
                if (a instanceof ActionStoreRegister) {
                    usedRegisters.add(((ActionStoreRegister) a).registerNumber);
                }
                if (a instanceof ActionPush) {
                    ActionPush ap = (ActionPush) a;
                    for (Object o : ap.values) {
                        if (o instanceof RegisterNumber) {
                            usedRegisters.add(((RegisterNumber) o).number);
                        }
                    }
                }
            }
            if (usedRegisters.contains(REGISTER_PARENT)) {
                preloadParentFlag = true;
            } else {
                suppressParentFlag = true;
            }
            if (usedRegisters.contains(REGISTER_ROOT)) {
                preloadRootFlag = true;
            }
            if (usedRegisters.contains(REGISTER_SUPER)) {
                preloadSuperFlag = true;
            }
            if (usedRegisters.contains(REGISTER_ARGUMENTS)) {
                preloadArgumentsFlag = true;
            } else {
                suppressArgumentsFlag = true;
            }
            if (usedRegisters.contains(REGISTER_THIS)) {
                preloadThisFlag = true;
            } else {
                suppressThisFlag = true;
            }
            if (usedRegisters.contains(REGISTER_GLOBAL)) {
                preloadGlobalFlag = true;
            }

            int newpos = 1;
            HashMap<Integer, Integer> registerMap = new HashMap<>();
            if (preloadThisFlag) {
                registerMap.put(REGISTER_THIS, newpos);
                newpos++;
            }
            if (preloadArgumentsFlag) {
                registerMap.put(REGISTER_ARGUMENTS, newpos);
                newpos++;
            }
            if (preloadSuperFlag) {
                registerMap.put(REGISTER_SUPER, newpos);
                newpos++;
            }
            if (preloadRootFlag) {
                registerMap.put(REGISTER_ROOT, newpos);
                newpos++;
            }
            if (preloadParentFlag) {
                registerMap.put(REGISTER_PARENT, newpos);
                newpos++;
            }
            if (preloadGlobalFlag) {
                registerMap.put(REGISTER_GLOBAL, newpos);
                newpos++;
            }
            if (newpos < 1) {
                newpos = 1;
            }
            for (int i = 0; i < 256; i++) {
                if (usedRegisters.contains(7 + i)) {
                    registerMap.put(7 + i, newpos);
                    if (i < paramNames.size()) {
                        paramRegs.add(newpos);
                    }
                    newpos++;
                } else {
                    if (i < paramNames.size()) {
                        paramRegs.add(0);
                    }
                }
            }

            TreeSet<Integer> usedRegisters2 = new TreeSet<>();
            for (int i : usedRegisters) {
                if (registerMap.get(i) == null) {
                    usedRegisters2.add(i);
                } else {
                    usedRegisters2.add(registerMap.get(i));
                }
            }
            usedRegisters = usedRegisters2;

            for (Action a : body) {
                if (a instanceof ActionStoreRegister) {
                    if (registerMap.containsKey(((ActionStoreRegister) a).registerNumber)) {
                        ((ActionStoreRegister) a).registerNumber = registerMap.get(((ActionStoreRegister) a).registerNumber);
                    }
                }
                if (a instanceof ActionPush) {
                    ActionPush ap = (ActionPush) a;
                    for (Object o : ap.values) {
                        if (o instanceof RegisterNumber) {
                            if (registerMap.containsKey(((RegisterNumber) o).number)) {
                                ((RegisterNumber) o).number = registerMap.get(((RegisterNumber) o).number);
                            }
                        }
                    }
                }
            }
            ret.addAll(body);
        } else {
            for (int i = 0; i < paramNames.size(); i++) {
                paramRegs.add(1 + i);
            }
        }
        int len = Action.actionsToBytes(asGenerator.toActionList(ret), false, SWF.DEFAULT_VERSION).length;
        if ((!preloadParentFlag)
                && (!preloadRootFlag)
                && (!preloadSuperFlag)
                && (!preloadArgumentsFlag)
                && (!preloadThisFlag)
                && (!preloadGlobalFlag)
                && (suppressArgumentsFlag)
                && (suppressThisFlag)
                && (suppressParentFlag)
                && usedRegisters.isEmpty()) {
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
                    usedRegisters.isEmpty() ? 0 : (usedRegisters.last() + 1), len, SWF.DEFAULT_VERSION, paramNames, paramRegs));
        }
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false; //function actually returns itself, but here is false for generator to not add Pop
    }
}
