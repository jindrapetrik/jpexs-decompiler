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
package com.jpexs.decompiler.flash.action.as2;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.ExtendsActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.ImplementsOpActionItem;
import com.jpexs.decompiler.flash.action.model.NewMethodActionItem;
import com.jpexs.decompiler.flash.action.model.NewObjectActionItem;
import com.jpexs.decompiler.flash.action.model.ReturnActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.TemporaryRegisterMark;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Detects AS2 classes inside DoInitAction tags
 *
 * @author JPEXS
 */
public class ActionScript2ClassDetector {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(ActionScript2ClassDetector.class.getName());


    /**
     * Constructor.
     */
    public ActionScript2ClassDetector() {
    }

    /**
     * Assert exception
     */
    private class AssertException extends Exception {

        /**
         * Condition
         */
        private final String condition;

        /**
         * Constructs a new AssertException
         *
         * @param condition Condition
         */
        public AssertException(String condition) {
            super(condition);
            this.condition = condition;
        }

        /**
         * Gets condition
         *
         * @return Condition
         */
        public String getCondition() {
            return condition;
        }

    }

    /**
     * Checks whether an item is direct submember of path. a.b.c.d is submember
     * of a.b.c, x.y.z is not submember of x.
     *
     * @param item Item
     * @param objectPath Path
     * @param newPathItem New submember name
     * @return True if item is submember of path
     */
    private boolean isMemberOfPath(GraphTargetItem item, List<String> objectPath, Reference<String> newPathItem) {
        List<String> path = getMembersPath(item);
        if (path == null) {
            return false;
        }
        if (path.size() != objectPath.size() + 1) {
            return false;
        }
        for (int i = 0; i < objectPath.size(); i++) {
            if (!path.get(i).equals(objectPath.get(i))) {
                return false;
            }
        }
        newPathItem.setVal(path.get(path.size() - 1));
        return true;
    }

    /**
     * Gets path of variable and its getMembers: a.b.c.d => [a,b,c,d].
     *
     * @param item Item
     * @return List of path or null if not members path
     */
    private List<String> getMembersPath(GraphTargetItem item) {
        List<String> ret = new ArrayList<>();
        while (item instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) item;
            if (!(mem.memberName instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem dv = ((DirectValueActionItem) mem.memberName);
            if (!dv.isString()) {
                return null;
            }
            ret.add(0, dv.getAsString());
            item = mem.object;
        }
        if (!(item instanceof GetVariableActionItem)) {
            return null;
        }
        GetVariableActionItem gv = (GetVariableActionItem) item;
        if (!(gv.name instanceof DirectValueActionItem)) {
            return null;
        }
        DirectValueActionItem dv = ((DirectValueActionItem) gv.name);
        if (!dv.isString()) {
            return null;
        }
        String varName = dv.getAsString();
        ret.add(0, varName);
        return ret;
    }

    /**
     * Converts SetMemberActionItem to GetMemberActionItem,
     * SetVariableActionItem to GetVariableActionItem.
     *
     * @param item Item
     * @return Converted item
     */
    private GraphTargetItem setMemberToGetMember(GraphTargetItem item) {
        if (item instanceof SetMemberActionItem) {
            return new GetMemberActionItem(null, null, ((SetMemberActionItem) item).object, ((SetMemberActionItem) item).objectName);
        } else if (item instanceof SetVariableActionItem) {
            return new GetVariableActionItem(null, null, ((SetVariableActionItem) item).name);
        }
        return null;
    }

    /**
     * Converts NewMethodActionItem or NewObjectActionItem to
     * GetMemberActionItem or GetVariableActionItem.
     *
     * @param nobj Item
     * @return Converted item
     * @throws AssertException If item is not NewMethod or NewObject
     */
    private GraphTargetItem newToGetMember(GraphTargetItem nobj) throws AssertException {
        if (nobj instanceof NewMethodActionItem) {
            NewMethodActionItem nm = (NewMethodActionItem) nobj;
            return new GetMemberActionItem(nobj.getSrc(), nobj.getLineStartItem(), nm.scriptObject, nm.methodName);
        } else if (nobj instanceof NewObjectActionItem) {
            NewObjectActionItem no = (NewObjectActionItem) nobj;
            return new GetVariableActionItem(nobj.getSrc(), nobj.getLineStartItem(), no.objectName);
        }
        throw new AssertException("NewMethod or NewObject expected");
    }

    /**
     * Gets path of setmembers: a.b.c.d => [a,b,c,d].
     *
     * @param item Item
     * @return List of path or null if not members path
     */
    private List<String> getSetMembersPath(GraphTargetItem item) {
        if (item instanceof SetVariableActionItem) {
            SetVariableActionItem sv = (SetVariableActionItem) item;
            if (!(sv.name instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem nDv = (DirectValueActionItem) sv.name;
            if (!nDv.isString()) {
                return null;
            }
            List<String> ret = new ArrayList<>();
            ret.add(nDv.getAsString());
            return ret;
        } else if (item instanceof SetMemberActionItem) {
            SetMemberActionItem sm = (SetMemberActionItem) item;
            if (!(sm.objectName instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem onDv = (DirectValueActionItem) sm.objectName;
            if (!onDv.isString()) {
                return null;
            }
            String currentMemberName = onDv.getAsString();
            List<String> path = getMembersPath(sm.object);
            if (path == null) {
                return null;
            }
            path.add(currentMemberName);
            return path;
        } else {
            return null;
        }
    }

    /**
     * Get register id or -1 if not found.
     *
     * @param item Item
     * @return Register id
     */
    private int getAsRegisterNum(GraphTargetItem item, String assertName) throws AssertException {
        if (item instanceof DirectValueActionItem) {
            DirectValueActionItem dv = (DirectValueActionItem) item;
            if (dv.value instanceof RegisterNumber) {
                RegisterNumber rn = (RegisterNumber) dv.value;
                return rn.number;
            }
        }
        if (item instanceof TemporaryRegister) {
            TemporaryRegister tr = (TemporaryRegister) item;
            return tr.getRegId();
        }
        throw new AssertException("not a register - " + assertName);
    }

    /**
     * Gets item without global prefix.
     *
     * @param ti Item
     * @return Item without global prefix
     */
    private static GraphTargetItem getWithoutGlobal(GraphTargetItem ti) {
        GraphTargetItem t = ti;
        if (!(t instanceof GetMemberActionItem)) {
            return ti;
        }
        GetMemberActionItem lastMember = null;
        while (((GetMemberActionItem) t).object instanceof GetMemberActionItem) {
            lastMember = (GetMemberActionItem) t;
            t = ((GetMemberActionItem) t).object;
        }
        if (((GetMemberActionItem) t).object instanceof GetVariableActionItem) {
            GetVariableActionItem v = (GetVariableActionItem) ((GetMemberActionItem) t).object;
            if (v.name instanceof DirectValueActionItem) {
                if (((DirectValueActionItem) v.name).value instanceof String) {
                    if (((DirectValueActionItem) v.name).value.equals("_global")) {
                        GetVariableActionItem gvt = new GetVariableActionItem(null, null, ((GetMemberActionItem) t).memberName);
                        if (lastMember == null) {
                            return gvt;
                        } else {
                            lastMember.object = gvt;
                        }
                    }
                }
            }
        }
        return ti;
    }

    /**
     * Converts item to string.
     *
     * @param item Item
     * @param itemName Item name for exception
     * @return String
     * @throws AssertException If item is not DirectValue or not string
     */
    private String getAsString(GraphTargetItem item, String itemName) throws AssertException {
        if (!(item instanceof DirectValueActionItem)) {
            throw new AssertException(itemName + " not DirectValue");
        }
        DirectValueActionItem mnDv = (DirectValueActionItem) item;
        if (!mnDv.isString()) {
            throw new AssertException(itemName + " not string");
        }
        return mnDv.getAsString();
    }

    /**
     * Detects classes in AS2 script.
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param parts Parts
     * @param variables Variables
     * @param partsPos Parts position
     * @param commandsStartPos Commands start position
     * @param commandsEndPos Commands end position
     * @param commands Commands
     * @param classNamePath Class name path
     * @param scriptPath Script path
     * @return True if class was detected
     */
    private boolean checkClassContent(Map<String, Map<String, Trait>> uninitializedClassTraits, List<GraphTargetItem> parts, HashMap<String, GraphTargetItem> variables, int partsPos, int commandsStartPos, int commandsEndPos, List<GraphTargetItem> commands, List<String> classNamePath, String scriptPath) {

        try {

            GraphTargetItem extendsOp = null;
            List<GraphTargetItem> implementsOp = new ArrayList<>();
            GraphTargetItem item = null;
            int instanceReg = -1;
            int classReg = -1;
            GraphTargetItem classNameTargetPath = null;
            GraphTargetItem constructor = null;

            Pattern regPattern = Pattern.compile("__register([0-9]+)");

            Set<Integer> definedRegisters = new HashSet<>();
            for (int i = partsPos; i < parts.size(); i++) {
                item = parts.get(i);
                if (item instanceof StoreRegisterActionItem) {
                    StoreRegisterActionItem sr = (StoreRegisterActionItem) item;
                    definedRegisters.add(sr.register.number);
                }
            }

            if (parts.size() > partsPos) {
                item = parts.get(partsPos);

                if (item instanceof TemporaryRegisterMark) {
                    if (partsPos + 1 < parts.size()) {
                        partsPos++;
                        item = parts.get(partsPos);
                    }
                }
                
                if (item instanceof SetMemberActionItem) {
                    List<String> memPath = getSetMembersPath((SetMemberActionItem) item);
                    if (memPath != null) {
                        if (memPath.get(0).equals("_global")) {
                            memPath.remove(0);
                        }
                        if (memPath.equals(classNamePath)) {
                            if (item.value instanceof StoreRegisterActionItem) {
                                constructor = item.value.value;
                                partsPos++;
                                if (parts.size() > partsPos) {
                                    item = parts.get(partsPos);
                                } else {
                                    item = null;
                                }
                            }

                        }
                    }
                }

                if (item instanceof ExtendsActionItem) {
                    ExtendsActionItem et = (ExtendsActionItem) parts.get(partsPos);
                    extendsOp = getWithoutGlobal(et.superclass);
                    partsPos++;
                    if (parts.size() > partsPos) {
                        item = parts.get(partsPos);
                    } else {
                        item = null;
                    }
                }

                if (item instanceof SetMemberActionItem) {
                    SetMemberActionItem sm = (SetMemberActionItem) item;
                    List<String> protoPath = new ArrayList<>(classNamePath);
                    protoPath.add("prototype");
                    List<String> smPath = getSetMembersPath(sm);
                    if (smPath != null) { //null = can start with TempRegister for example
                        if (smPath.get(0).equals("_global")) {
                            smPath.remove(0);
                        }
                        if (smPath.equals(protoPath)) {
                            if (sm.value instanceof StoreRegisterActionItem) {
                                partsPos++;
                                if (parts.size() > partsPos) {
                                    item = parts.get(partsPos);
                                } else {
                                    item = null;
                                }
                            }
                        }
                    }
                }

                if (item instanceof StoreRegisterActionItem) {
                    StoreRegisterActionItem sr = (StoreRegisterActionItem) item;
                    instanceReg = sr.register.number;
                    if (sr.value instanceof GetMemberActionItem) {
                        GetMemberActionItem gm = (GetMemberActionItem) sr.value;
                        if (gm.object instanceof TemporaryRegister) {
                            TemporaryRegister treg = (TemporaryRegister) gm.object;
                            classReg = treg.getRegId();
                            if (!"prototype".equals(getAsString(gm.memberName, "memberName"))) {
                                throw new AssertException("memberName not \"prototype\"");
                            }
                            if ((treg.value instanceof SetMemberActionItem) || (treg.value instanceof SetVariableActionItem)) {
                                List<String> path = getSetMembersPath(treg.value);
                                if (path == null || path.isEmpty()) {
                                    throw new AssertException("Cannot detect class - tempreg value is not a path");
                                }
                                //remove _global if it's there - happens for classes in global package
                                if ("_global".equals(path.get(0))) {
                                    path.remove(0);
                                }
                                if (classNamePath.equals(path)) {
                                    //can start with _global for classes on top level
                                    classNameTargetPath = getWithoutGlobal(setMemberToGetMember(treg.value));

                                    //treg.value.value is the value being set - treg.value is setmember ot setvariable
                                    if (!(treg.value.value instanceof StoreRegisterActionItem)) {
                                        throw new AssertException("Constructor expected to be in storeregister");
                                    }
                                    if (!(treg.value.value.value instanceof FunctionActionItem)) {
                                        throw new AssertException("Constructor expected as functionitem");
                                    }
                                    constructor = treg.value.value.value;
                                } else {
                                    throw new AssertException("temporaryreg value does not match class path");
                                }
                            } else {
                                throw new AssertException("temporaryreg value not setmember/setvariable");
                            }
                        } else {
                            throw new AssertException("Getmember does not have TemporaryRegister as object");
                        }
                    } else {
                        throw new AssertException("Not Getmember in StoreRegister");
                    }
                    partsPos++;
                }
            }
            classNameTargetPath = new GetVariableActionItem(null, null, new DirectValueActionItem(classNamePath.get(0)));
            for (int i = 1; i < classNamePath.size(); i++) {
                classNameTargetPath = new GetMemberActionItem(null, null, classNameTargetPath, new DirectValueActionItem(classNamePath.get(i)));
            }
            List<MyEntry<GraphTargetItem, GraphTargetItem>> traits = new ArrayList<>();
            List<Boolean> traitsStatic = new ArrayList<>();
            loopsetmembers:
            for (; partsPos < parts.size(); partsPos++) {
                item = parts.get(partsPos);
                if (item instanceof PushItem) { //push is optional
                    PushItem pi = (PushItem) item;
                    item = pi.value;
                }
                if (item instanceof SetMemberActionItem) {
                    SetMemberActionItem sm = (SetMemberActionItem) item;
                    GraphTargetItem regValue;
                    int currentRegId = -1;
                    if (sm.object instanceof TemporaryRegister) {
                        TemporaryRegister tempReg = (TemporaryRegister) sm.object;
                        currentRegId = tempReg.getRegId();
                        regValue = tempReg.value;
                    } else if ((sm.object instanceof DirectValueActionItem) && (((DirectValueActionItem) sm.object).value instanceof RegisterNumber)) {
                        DirectValueActionItem dv = (DirectValueActionItem) sm.object;
                        RegisterNumber rn = ((RegisterNumber) dv.value);
                        currentRegId = rn.number;
                        regValue = dv.computedRegValue;
                    } else {
                        //might be an interface
                        List<String> path = getSetMembersPath(item);
                        if (path == null || path.isEmpty()) {
                            throw new AssertException("invalid setmember");
                        }
                        //remove _global if it's there - happens for classes in global package
                        if ("_global".equals(path.get(0))) {
                            path.remove(0);
                        }
                        if (!path.equals(classNamePath)) {
                            throw new AssertException("wrong path in setmember");
                        }
                        GraphTargetItem interfaceClass = getWithoutGlobal(setMemberToGetMember(item));
                        if (!(sm.value instanceof FunctionActionItem)) {
                            throw new AssertException("not a function in setmember");
                        }
                        FunctionActionItem f = (FunctionActionItem) sm.value;
                        if (!"".equals(f.functionName)) {
                            throw new AssertException("not unnamed func in setmember");
                        }
                        if (!f.actions.isEmpty()) {
                            throw new AssertException("not empty function in setmember");
                        }
                        if (!f.paramNames.isEmpty()) {
                            throw new AssertException("not empty params for function in setmember");
                        }
                        partsPos++;
                        for (; partsPos < parts.size(); partsPos++) {
                            item = parts.get(partsPos);
                            if (item instanceof ImplementsOpActionItem) {
                                if (!implementsOp.isEmpty()) {
                                    throw new AssertException("multiple implementsAction");
                                }
                                ImplementsOpActionItem io = (ImplementsOpActionItem) item;
                                implementsOp = io.superclasses;
                            } else {
                                throw new AssertException("unknown iface item: " + item.getClass().getSimpleName());
                            }
                        }

                        InterfaceActionItem ifsItem = new InterfaceActionItem(interfaceClass, implementsOp);
                        for (int k = commandsStartPos; k <= commandsEndPos; k++) {
                            commands.remove(commandsStartPos);
                        }
                        commands.add(commandsStartPos, ifsItem);

                        //remove §§pop after, if it's there
                        if (commandsStartPos + 1 < commands.size()) {
                            if (commands.get(commandsStartPos + 1) instanceof PopItem) {
                                commands.remove(commandsStartPos + 1);
                            }
                        }

                        // goto next line and check next classes
                        return true;
                    }
                    //it was register .. continue class detection
                    if (currentRegId != instanceReg && currentRegId != classReg) {
                        if (!(regValue instanceof SetMemberActionItem)) {
                            throw new AssertException("temp register do not contain setmember");
                        }
                        SetMemberActionItem sm2 = (SetMemberActionItem) regValue;
                        GraphTargetItem pathSource;
                        boolean isPrototype;
                        if ("prototype".equals(getAsString(sm2.objectName, "objectName"))) {
                            pathSource = sm2.object;
                            isPrototype = true;
                        } else {
                            pathSource = setMemberToGetMember(sm2);
                            isPrototype = false;
                        }
                        List<String> memPath = getMembersPath(pathSource);
                        if (memPath == null) {
                            throw new AssertException("Invalid pathsource");
                        }
                        //remove _global if it's there - happens for classes in global package
                        if (memPath.size() > 0 && "_global".equals(memPath.get(0))) {
                            memPath.remove(0);
                        }
                        if (!classNamePath.equals(memPath)) {
                            throw new AssertException("Invalid path of setmember:" + String.join(".", memPath));
                        }
                        //classNameTargetPath = pathSource;
                        if (!(sm2.value instanceof StoreRegisterActionItem)) {
                            throw new AssertException("Not storeregister");
                        }
                        StoreRegisterActionItem sr = (StoreRegisterActionItem) sm2.value;
                        if (sr.register.number != currentRegId) {
                            throw new AssertException("Invalid storeregister");
                        }
                        if (isPrototype && ((sr.value instanceof NewMethodActionItem) || (sr.value instanceof NewObjectActionItem))) {
                            extendsOp = newToGetMember(sr.value);
                            instanceReg = currentRegId;
                        } else if (!isPrototype && (sr.value instanceof FunctionActionItem)) { //constructor
                            constructor = sr.value;
                            classReg = currentRegId;
                        } else {
                            throw new AssertException("invalid storeregister value: " + sr.value.getClass().getSimpleName());
                        }
                    }

                    MyEntry<GraphTargetItem, GraphTargetItem> trait = new MyEntry<>(sm.objectName, sm.value);
                    if (sm.value instanceof FunctionActionItem) {
                        FunctionActionItem f = (FunctionActionItem) sm.value;
                        f.calculatedFunctionName = sm.objectName;
                    }
                    traits.add(trait);
                    if (currentRegId == instanceReg) {
                        traitsStatic.add(false);
                    } else if (currentRegId == classReg) {
                        traitsStatic.add(true);
                    }
                } else if (item instanceof ImplementsOpActionItem) {
                    ImplementsOpActionItem iot = (ImplementsOpActionItem) item;
                    implementsOp = iot.superclasses;
                } else if (item instanceof CallMethodActionItem) {
                    CallMethodActionItem cm = (CallMethodActionItem) item;
                    String pushMethodName = getAsString(cm.methodName, "push methodName");
                    if ("addProperty".equals(pushMethodName)) {
                        int rnumObject = getAsRegisterNum(cm.scriptObject, "addProperty not on register");
                        if ((rnumObject != instanceReg) && (rnumObject != classReg)) {
                            throw new AssertException("unexpected addProperty object register " + rnumObject);
                        }

                        if (cm.arguments.size() != 3) {
                            throw new AssertException("invalid number of arguments to addProperty: " + cm.arguments.size());
                        }
                        GraphTargetItem propertyName = cm.arguments.get(0);
                        GraphTargetItem propertyGetter = cm.arguments.get(1);
                        GraphTargetItem propertySetter = cm.arguments.get(2);
                        String propertyNameStr = getAsString(propertyName, "propertyName");
                        if (propertyGetter instanceof GetMemberActionItem) {
                            int regId = getAsRegisterNum(((GetMemberActionItem) propertyGetter).object, "getter member not register");
                            if (rnumObject != regId) {
                                throw new AssertException("getter register does not match property register " + regId + " <=> " + rnumObject);
                            }
                            String getterNameStr = getAsString(((GetMemberActionItem) propertyGetter).memberName, "getter memberName");
                            if (!(getterNameStr.equals("__get__" + propertyNameStr))) {
                                //throw new AssertException("getter does not match property name");
                                Logger.getLogger(ActionScript2ClassDetector.class.getName()).warning(scriptPath + ": getter " + IdentifiersDeobfuscation.printIdentifier(false, getterNameStr) + " does not match property name " + IdentifiersDeobfuscation.printIdentifier(false, propertyNameStr));
                                continue;
                            }

                            for (MyEntry<GraphTargetItem, GraphTargetItem> trait : traits) {
                                if (trait.getKey() instanceof DirectValueActionItem) {
                                    if (((DirectValueActionItem) trait.getKey()).isString()) {
                                        if (((DirectValueActionItem) trait.getKey()).toString().equals(getterNameStr)) {
                                            if (trait.getValue() instanceof FunctionActionItem) {
                                                FunctionActionItem func = (FunctionActionItem) trait.getValue();
                                                func.isGetter = true;
                                            }
                                        }
                                    }
                                }
                            }

                        } else if (propertyGetter instanceof FunctionActionItem) {
                            FunctionActionItem getterFunc = (FunctionActionItem) propertyGetter;
                            if (!(getterFunc.actions.isEmpty() && getterFunc.functionName.isEmpty() && ((FunctionActionItem) propertyGetter).paramNames.isEmpty())) {
                                throw new AssertException("unexpected getter value for property " + propertyNameStr);
                            }
                            //we got empty getter
                        } else {
                            throw new AssertException("unexpected getter value for property " + propertyNameStr + ": " + propertyGetter.getClass().getSimpleName());
                        }

                        if (propertySetter instanceof GetMemberActionItem) {
                            int regId = getAsRegisterNum(((GetMemberActionItem) propertySetter).object, "setter member");
                            if (rnumObject != regId) {
                                throw new AssertException("setter register does not match property register " + regId + " <=> " + rnumObject);
                            }
                            String setterNameStr = getAsString(((GetMemberActionItem) propertySetter).memberName, "setter memberNAme");
                            if (!(setterNameStr.equals("__set__" + propertyNameStr))) {
                                Logger.getLogger(ActionScript2ClassDetector.class.getName()).warning(scriptPath + ": setter " + IdentifiersDeobfuscation.printIdentifier(false, setterNameStr) + " does not match property name " + IdentifiersDeobfuscation.printIdentifier(false, propertyNameStr));
                                continue;
                                //throw new AssertException("setter does not match property name");
                            }

                            for (MyEntry<GraphTargetItem, GraphTargetItem> trait : traits) {
                                if (trait.getKey() instanceof DirectValueActionItem) {
                                    if (((DirectValueActionItem) trait.getKey()).isString()) {
                                        if (((DirectValueActionItem) trait.getKey()).toString().equals(setterNameStr)) {
                                            if (trait.getValue() instanceof FunctionActionItem) {
                                                FunctionActionItem func = (FunctionActionItem) trait.getValue();
                                                func.isSetter = true;

                                                if (FunctionActionItem.DECOMPILE_GET_SET) {

                                                    AbstractGraphTargetVisitor visitor = new AbstractGraphTargetVisitor() {
                                                        @Override
                                                        public boolean visit(GraphTargetItem item) {
                                                            if (item instanceof ReturnActionItem) {
                                                                ReturnActionItem ret = (ReturnActionItem) item;
                                                                if (ret.value instanceof DirectValueActionItem) {
                                                                    DirectValueActionItem dv = (DirectValueActionItem) ret.value;
                                                                    if (dv.value instanceof Undefined) {
                                                                        ret.value = null;
                                                                    }
                                                                }
                                                            }
                                                            return true;
                                                        }
                                                    };
                                                    for (GraphTargetItem ti : func.actions) {
                                                        ti.visitRecursively(visitor);
                                                    }

                                                    //There is return getter added at the end of every setter, gotta remove it, since it won't compile
                                                    //as setter must not return a value
                                                    if (!func.actions.isEmpty()) {
                                                        int pos = func.actions.size() - 1;
                                                        if (func.actions.get(pos) instanceof ScriptEndItem) {
                                                            pos--;
                                                        }
                                                        if (pos >= 0 && func.actions.get(pos) instanceof ReturnActionItem) {
                                                            GraphTargetItem val = func.actions.get(pos);
                                                            if (val.value instanceof CallMethodActionItem) {
                                                                if (((CallMethodActionItem) val.value).methodName instanceof DirectValueActionItem) {
                                                                    if (((CallMethodActionItem) val.value).methodName.toString().startsWith("__get__")) {
                                                                        func.actions.remove(pos);
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
                        } else if (propertySetter instanceof FunctionActionItem) {
                            FunctionActionItem setterFunc = (FunctionActionItem) propertySetter;
                            if (!(setterFunc.actions.isEmpty() && setterFunc.functionName.isEmpty() && ((FunctionActionItem) propertySetter).paramNames.isEmpty())) {
                                throw new AssertException("unexpected getter value for property " + propertyNameStr);
                            }
                            //we got empty setter
                        } else {
                            throw new AssertException("unexpected setter value for property " + propertyNameStr + ": " + propertySetter.getClass().getSimpleName());
                        }

                    } else {
                        throw new AssertException("unknown push method name: " + pushMethodName);
                    }
                } else if (item instanceof CallFunctionActionItem) {
                    CallFunctionActionItem cf = (CallFunctionActionItem) item;
                    String funName = getAsString(cf.functionName, "pushitem function name");
                    if (funName.equals("ASSetPropFlags")) {
                        //it should be ASSetPropFlags(a.b.c.D.prototype,null,1) as it sets prototype to hidden
                        //see http://www.ryanjuckett.com/programming/how-to-use-assetpropflags-in-actionscript-2-0/
                        if (cf.arguments.size() != 3) {
                            throw new AssertException("Invalid number of arguments to ASSetPropFlags:" + cf.arguments.size() + ", 3 expected");
                        }
                        GraphTargetItem obj = cf.arguments.get(0);
                        GraphTargetItem props = cf.arguments.get(1);
                        GraphTargetItem flags = cf.arguments.get(2);
                        if ((obj instanceof DirectValueActionItem) && (((DirectValueActionItem) obj).value instanceof RegisterNumber)) {
                            RegisterNumber rn = (RegisterNumber) ((DirectValueActionItem) obj).value;
                            if (rn.number != instanceReg) {
                                throw new AssertException("ASSetPropFlags not on instanceReg");
                            }
                        } else {
                            List<String> path = getMembersPath(obj);
                            if (path != null && !path.isEmpty() && "_global".equals(path.get(0))) { //For classes in toplevel package, there's _global in path
                                path.remove(0); //remove that _global
                            }
                            List<String> classPathWithPrototype = new ArrayList<>();
                            classPathWithPrototype.addAll(classNamePath);
                            classPathWithPrototype.add("prototype");
                            if (!classPathWithPrototype.equals(path)) {
                                throw new AssertException("ASSetPropFlags not on prototype");
                            }
                        }
                        if (!((props instanceof DirectValueActionItem) && (((DirectValueActionItem) props).value == Null.INSTANCE))) {
                            throw new AssertException("ASSetPropFlags properties param not null");
                        }
                        if (!((flags instanceof DirectValueActionItem) && (((DirectValueActionItem) flags).value == (Long) 1L))) {
                            throw new AssertException("ASSetPropFlags flags not set to 1");
                        }
                    } else {
                        throw new AssertException("unknown pushitem function call " + funName);
                    }
                } else if (item instanceof DirectValueActionItem) {
                    //ignore such values
                    //TODO: maybe somehow display in the class ?
                } else if (item instanceof ScriptEndItem) {
                    //ignore
                } else if (item instanceof TemporaryRegisterMark) {
                    //ignore
                } else {
                    throw new AssertException("unknown item - " + item.getClass().getSimpleName());
                }
            }

            if (constructor != null) { //constructor should be there always, but just in case
                //add constructor as trait                                
                traitsStatic.add(0, false);
                DirectValueActionItem classBaseName = new DirectValueActionItem(classNamePath.get(classNamePath.size() - 1));
                ((FunctionActionItem) constructor).calculatedFunctionName = classBaseName;
                traits.add(0, new MyEntry<>(classBaseName, constructor));

                AbstractGraphTargetVisitor visitor = new AbstractGraphTargetVisitor() {
                    @Override
                    public boolean visit(GraphTargetItem item) {
                        if (item instanceof ReturnActionItem) {
                            ReturnActionItem ret = (ReturnActionItem) item;
                            if (ret.value instanceof DirectValueActionItem) {
                                DirectValueActionItem dv = (DirectValueActionItem) ret.value;
                                if (dv.value instanceof Undefined) {
                                    ret.value = null;
                                }
                            }
                        }
                        return true;
                    }
                };
                for (GraphTargetItem ti : ((FunctionActionItem) constructor).actions) {
                    ti.visitRecursively(visitor);
                }
            } else {
                //throw new AssertException("No constructor found");
            }

            String fullClassName = String.join(".", getMembersPath(classNameTargetPath));
            if (uninitializedClassTraits.containsKey(fullClassName)) {
                int t = 0;
                for (String traitName : uninitializedClassTraits.get(fullClassName).keySet()) {
                    Trait trait = uninitializedClassTraits.get(fullClassName).get(traitName);
                    traitsStatic.add(t, trait.isStatic());
                    traits.add(t, new MyEntry<>(new DirectValueActionItem(trait.getName()), null));
                    t++;
                }
            }
            ClassActionItem clsItem = new ClassActionItem(classNameTargetPath, extendsOp, implementsOp, traits, traitsStatic);
            for (int k = commandsStartPos; k <= commandsEndPos; k++) {
                commands.remove(commandsStartPos);
            }
            commands.add(commandsStartPos, clsItem);

            //remove §§pop after, if it's there
            if (commandsStartPos + 1 < commands.size()) {
                if (commands.get(commandsStartPos + 1) instanceof PopItem) {
                    commands.remove(commandsStartPos + 1);
                }
            }

            // goto next line and check next classes
            return true;
        } catch (AssertException ex) {
            logger.log(Level.WARNING, "{0}: Cannot detect class - {1}", new Object[]{scriptPath, ex.getCondition()});
        }
        return false;
    }

    /**
     * In some weird cases, ifs are detected as ternars, this method expands
     * ternars to ifs.
     *
     * @param commands Commands
     */
    private void expandTernars(List<GraphTargetItem> commands) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i) instanceof TernarOpItem) {
                TernarOpItem ter = (TernarOpItem) commands.get(i);
                List<GraphTargetItem> onTrue = new ArrayList<>();
                if (ter.onTrue instanceof CommaExpressionItem) {
                    CommaExpressionItem ce = (CommaExpressionItem) ter.onTrue;
                    onTrue = ce.commands;
                } else {
                    onTrue.add(ter.onTrue);
                }
                List<GraphTargetItem> onFalse = new ArrayList<>();
                if (ter.onFalse instanceof CommaExpressionItem) {
                    CommaExpressionItem ce = (CommaExpressionItem) ter.onFalse;
                    onFalse = ce.commands;
                } else {
                    onFalse.add(ter.onFalse);
                }
                commands.set(i, new IfItem(null, null, ter.expression, onTrue, onFalse));
            }
        }
    }

    /**
     * Checks if variants.
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param commands Commands
     * @param variables Variables
     * @param pos Position
     * @param scriptPath Script path
     * @return True if some of the variants was detected
     */
    private boolean checkIfVariants(Map<String, Map<String, Trait>> uninitializedClassTraits, List<GraphTargetItem> commands, HashMap<String, GraphTargetItem> variables, int pos, String scriptPath) {

        expandTernars(commands);

        /*
            Variant 1:        
        
            if(!_global.a)
            {
               _global.a = new Object();
            }
            §§pop();
            if(!_global.a.b)
            {
               _global.a.b = new Object();
            }
            §§pop();
            if(!_global.a.b.c)
            {
               _global.a.b.c = new Object();
            }
            §§pop();
            if(!_global.a.b.c.D)
            {
                ..class_content...            
            }
            §§pop();
         */
        List<String> pathToSearchVariant1 = new ArrayList<>();
        pathToSearchVariant1.add("_global");

        check_variant1:
        for (int checkPos = pos; checkPos < commands.size(); checkPos++) {
            GraphTargetItem t = commands.get(checkPos);
            if (t instanceof IfItem) {
                IfItem ifItem = (IfItem) t;
                if (ifItem.expression instanceof NotItem) {
                    NotItem nti = (NotItem) ifItem.expression;
                    GraphTargetItem condType = nti.value;
                    Reference<String> newMemberNameRef = new Reference<>("");

                    if (isMemberOfPath(condType, pathToSearchVariant1, newMemberNameRef)) {
                        pathToSearchVariant1.add(newMemberNameRef.getVal());

                        //_global.a.b.c = new Object();  
                        if ((ifItem.onTrue.size() == 1) && (ifItem.onTrue.get(0) instanceof SetMemberActionItem) && (((SetMemberActionItem) ifItem.onTrue.get(0)).value instanceof NewObjectActionItem)) {
                            //skip §§pop item if its there right after if
                            if (checkPos + 1 < commands.size()) {
                                GraphTargetItem tnext = commands.get(checkPos + 1);
                                if (tnext instanceof PopItem) {
                                    checkPos++;
                                }
                            }
                            continue check_variant1;
                        }
                        List<String> classPath = pathToSearchVariant1;
                        classPath.remove(0); //remove _global
                        if (ifItem.onTrue.isEmpty()) { //if can have zero offset as the code is larger than bytes limit. TODO: make this check also for variant 2 (?)
                            if (this.checkClassContent(uninitializedClassTraits, commands, variables, checkPos + 1, pos, commands.size() - 1, commands, classPath, scriptPath)) {
                                return true;
                            } else {
                                break check_variant1;
                            }
                        } else if (this.checkClassContent(uninitializedClassTraits, ifItem.onTrue, variables, 0, pos, checkPos, commands, classPath, scriptPath)) {
                            return true;
                        } else {
                            break check_variant1;
                        }
                    } else {
                        break check_variant1;
                    }
                } else {
                    break check_variant1; //not an if !
                }
            } else {
                break check_variant1; //not an if
            }
        } //check_variant1

        /*
            
            Variant 2:
        
            if(!a.b.c.D)
            {
               if(!a)
               {
                  _global.a = new Object();
               }
               if(!a.b)
               {
                  _global.a.b = new Object();
               }
               if(!a.b.c)
               {
                  _global.a.b.c = new Object();
               }
               ..class_content.. 
            }  
         */
        List<String> variant2CurrentPath = new ArrayList<>();
        check_variant2:
        if (commands.get(pos) instanceof IfItem) {
            IfItem ifItem = (IfItem) commands.get(pos);
            if (ifItem.expression instanceof NotItem) {
                NotItem nti = (NotItem) ifItem.expression;
                List<String> memPath = getMembersPath(nti.value);
                if (memPath == null) {
                    break check_variant2;
                }
                if (ifItem.onTrue.size() < memPath.size()) {
                    break check_variant2;
                }
                variant2CurrentPath.clear();
                List<GraphTargetItem> parts = ifItem.onTrue;
                int checkPos = 0;
                for (; checkPos < memPath.size() - 1; checkPos++) {
                    variant2CurrentPath.add(memPath.get(checkPos));
                    if (!(parts.get(checkPos) instanceof IfItem)) {
                        break check_variant2;
                    }
                    IfItem ifItem2 = (IfItem) parts.get(checkPos);
                    if (ifItem2.expression instanceof NotItem) {
                        NotItem nti2 = (NotItem) ifItem2.expression;
                        List<String> if2Path = getMembersPath(nti2.value);
                        if (!variant2CurrentPath.equals(if2Path)) {
                            break check_variant2;
                        }
                    }
                }
                if (checkClassContent(uninitializedClassTraits, parts, variables, checkPos, pos, pos, commands, memPath, scriptPath)) {
                    return true;
                }
            }
        } //check_variant2

        return false;
    }

    /**
     * Checks class.
     *
     * @param uninitializedClassTraits Uninitialized class traits
     * @param commands Commands
     * @param variables Variables
     * @param scriptPath Script path
     */
    public void checkClass(Map<String, Map<String, Trait>> uninitializedClassTraits, List<GraphTargetItem> commands, HashMap<String, GraphTargetItem> variables, String scriptPath) {
        List<GraphTargetItem> localCommands = new ArrayList<>(commands);
        boolean changed = false;
        for (int pos = 0; pos < localCommands.size(); pos++) {
            if (checkIfVariants(uninitializedClassTraits, localCommands, variables, pos, scriptPath)) {
                changed = true;
            }
        }
        if (changed) {
            commands.clear();
            commands.addAll(localCommands);
        }
    }
}
