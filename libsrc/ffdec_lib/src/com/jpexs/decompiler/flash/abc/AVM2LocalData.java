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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class AVM2LocalData extends BaseLocalData {

    public Boolean isStatic;

    public Integer classIndex;

    public HashMap<Integer, GraphTargetItem> localRegs;

    public ScopeStack scopeStack;

    public MethodBody methodBody;

    public ABC abc;

    public HashMap<Integer, String> localRegNames;

    public List<DottedChain> fullyQualifiedNames;

    public ArrayList<ABCException> parsedExceptions;

    public Map<Integer, List<Integer>> finallyJumps;

    public Map<Integer, Integer> ignoredSwitches;

    public List<Integer> ignoredSwitches2;

    public Integer scriptIndex;

    public HashMap<Integer, Integer> localRegAssignmentIps;

    public Integer ip;

    public HashMap<Integer, List<Integer>> refs;

    public AVM2Code code;

    public boolean thisHasDefaultToPrimitive;

    public AVM2LocalData() {

    }

    public AVM2LocalData(AVM2LocalData localData) {
        isStatic = localData.isStatic;
        classIndex = localData.classIndex;
        localRegs = localData.localRegs;
        scopeStack = localData.scopeStack;
        methodBody = localData.methodBody;
        abc = localData.abc;
        localRegNames = localData.localRegNames;
        fullyQualifiedNames = localData.fullyQualifiedNames;
        parsedExceptions = localData.parsedExceptions;
        finallyJumps = localData.finallyJumps;
        ignoredSwitches = localData.ignoredSwitches;
        ignoredSwitches2 = localData.ignoredSwitches2;
        scriptIndex = localData.scriptIndex;
        localRegAssignmentIps = localData.localRegAssignmentIps;
        ip = localData.ip;
        refs = localData.refs;
        code = localData.code;
        thisHasDefaultToPrimitive = localData.thisHasDefaultToPrimitive;
    }

    public AVM2ConstantPool getConstants() {
        return abc.constants;
    }

    public List<MethodInfo> getMethodInfo() {
        return abc.method_info;
    }

    public List<InstanceInfo> getInstanceInfo() {
        return abc.instance_info;
    }

    public List<ScriptInfo> getScriptInfo() {
        return abc.script_info;
    }
}
