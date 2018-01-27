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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.ExtendsActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.ImplementsOpActionItem;
import com.jpexs.decompiler.flash.action.model.NewMethodActionItem;
import com.jpexs.decompiler.flash.action.model.NewObjectActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionScript2ClassDetector {

    private static final Logger logger = Logger.getLogger(ActionScript2ClassDetector.class.getName());

    private class AssertException extends Exception {

        private final String condition;

        public AssertException(String condition) {
            super(condition);
            this.condition = condition;
        }

        public String getCondition() {
            return condition;
        }

    }

    /**
     * Checks whether an item is direct submember of path. a.b.c.d is submember
     * of a.b.c, x.y.z is not submember of x,
     *
     * @param item
     * @param objectPath
     * @param newPathItem New submember name
     * @return
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
     * Gets path of variable and its getMembers: a.b.c.d => [a,b,c,d]
     *
     * @param item
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

    private GraphTargetItem setMemberToGetMember(GraphTargetItem item) {
        if (item instanceof SetMemberActionItem) {
            return new GetMemberActionItem(null, null, ((SetMemberActionItem) item).object, ((SetMemberActionItem) item).objectName);
        } else if (item instanceof SetVariableActionItem) {
            return new GetVariableActionItem(null, null, ((SetVariableActionItem) item).name);
        }
        return null;
    }

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
     * Get register id or -1 if not found
     *
     * @param item
     * @return
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

    /*private boolean isInstanceRegister(GraphTargetItem item, Reference<Integer> instanceReg, Reference<Integer> classReg,Reference<GraphTargetItem> extracted, String assertName) throws AssertException {
        if (item instanceof DirectValueActionItem) {
            DirectValueActionItem dv = (DirectValueActionItem) item;
            if (dv.value instanceof RegisterNumber) {
                RegisterNumber rn = (RegisterNumber) dv.value;
                if (rn.number == instanceReg.getVal()) {
                    return true;
                } else if (rn.number == classReg.getVal()) {
                    return false;
                }else{
                    throw new AssertException(assertName + " - unknown register");
                }
            }
        }
        if (item instanceof TemporaryRegister) {
            TemporaryRegister tr = (TemporaryRegister) item;
            if (!"prototype".equals(getAsString(gm.memberName, "memberName"))) {
                                                throw new AssertException("memberName not \"prototype\"");
                                            }
            return tr.getRegId();
        }
    }
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

    private boolean checkClassContent(List<GraphTargetItem> parts, int partsPos, int commandsStartPos, int commandsEndPos, List<GraphTargetItem> commands, List<String> classNamePath, String scriptPath) {

        try {
            GraphTargetItem item = parts.get(partsPos);
            GraphTargetItem extendsOp = null;
            List<GraphTargetItem> implementsOp = new ArrayList<>();
            if (item instanceof ExtendsActionItem) {
                ExtendsActionItem et = (ExtendsActionItem) parts.get(partsPos);
                extendsOp = getWithoutGlobal(et.superclass);
                partsPos++;
                item = parts.get(partsPos);
            }
            int instanceReg = -1;
            int classReg = -1;
            GraphTargetItem classNameTargetPath = null;
            GraphTargetItem constructor = null;
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
                        if (!classNamePath.equals(memPath)) {
                            throw new AssertException("Invalid path of setmember:" + String.join(".", memPath));
                        }
                        classNameTargetPath = pathSource;
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
                                throw new AssertException("getter does not match property name");
                            }
                            //TODO: handle getter HERE                                                                                                       

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
                                throw new AssertException("setter does not match property name");
                            }
                            //TODO: handle setter HERE
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
                } else {
                    throw new AssertException("unknown item - " + item.getClass().getSimpleName());
                }
            }

            if (constructor != null) { //constructor should be there always, but just in calse
                //add constructor as trait                                
                traitsStatic.add(0, false);
                DirectValueActionItem classBaseName = new DirectValueActionItem(classNamePath.get(classNamePath.size() - 1));
                ((FunctionActionItem) constructor).calculatedFunctionName = classBaseName;
                traits.add(0, new MyEntry<>(classBaseName, constructor));
            } else {
                throw new AssertException("No constructor found");
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

    private boolean checkIfVariants(List<GraphTargetItem> commands, int pos, String scriptPath) {


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
                        if (this.checkClassContent(ifItem.onTrue, 0, pos, checkPos, commands, classPath, scriptPath)) {
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
                if (checkClassContent(parts, checkPos, pos, pos, commands, memPath, scriptPath)) {
                    return true;
                }
            }
        }//check_variant2

        return false;
    }

    public void checkClass(List<GraphTargetItem> commands, String scriptPath) {
        for (int pos = 0; pos < commands.size(); pos++) {
            checkIfVariants(commands, pos, scriptPath);
        }
    }
}
