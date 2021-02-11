/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<ABCException> parsedExceptions = new ArrayList<>();
    public List<Integer> parsedExceptionIds = new ArrayList<>();

    //public Map<Integer, List<Integer>> finallyJumps;
    /**
     * Mapped jumps from pushbyte xx part to apropriate lookupswitch branch
     */
    public Map<GraphPart, GraphPart> finallyJumps = new HashMap<>();

    /**
     * Mapping from source part (as in finallyJumps) to finally exception index
     */
    public Map<GraphPart, Integer> finallyJumpsToFinallyIndex = new HashMap<>();

    public Map<Integer, GraphPart> finallyIndexToDefaultGraphPart = new HashMap<>();

    //exception index => switchPart
    public Map<Integer, GraphPart> ignoredSwitches = new HashMap<>();

    /**
     * exception index -> switch defaultPart
     */
    public Map<Integer, GraphPart> defaultParts = new HashMap<>();

    public Map<Integer, Integer> switchedRegs = new HashMap<>();
    
    public Map<Integer, GraphPart> pushDefaultPart = new HashMap<>();

    public Map<Integer, Integer> finallyKinds = new HashMap<>();
    
    /**
     * exception index -> switch throw part
     */
    public Map<Integer, GraphPart> finallyThrowParts = new HashMap<>();

    public Map<Integer, GraphPart> finallyTargetParts = new HashMap<>();

    //switchedPart -> index of nextpart
    public Map<GraphPart, Integer> defaultWays = new HashMap<>();

    public Integer scriptIndex;

    public HashMap<Integer, Integer> localRegAssignmentIps;

    public Integer ip;

    public HashMap<Integer, List<Integer>> refs;

    public AVM2Code code;

    public boolean thisHasDefaultToPrimitive;

    public Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = new HashMap<>();

    public CodeStats codeStats;

    public Set<Integer> finallyIndicesWithDoublePush = new HashSet<>();

    public boolean inGetLoops = false;

    public Set<Integer> seenMethods = new HashSet<>();

    public AVM2LocalData() {

    }

    public Set<Integer> getSetLocalUsages(int setLocalPos) {
        if (setLocalPosToGetLocalPos == null) {
            return new HashSet<>();
        }
        if (!setLocalPosToGetLocalPos.containsKey(setLocalPos)) {
            return new HashSet<>();
        }
        return setLocalPosToGetLocalPos.get(setLocalPos);
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
        //ignoredSwitches2 = localData.ignoredSwitches2;
        scriptIndex = localData.scriptIndex;
        localRegAssignmentIps = localData.localRegAssignmentIps;
        ip = localData.ip;
        refs = localData.refs;
        code = localData.code;
        thisHasDefaultToPrimitive = localData.thisHasDefaultToPrimitive;
        setLocalPosToGetLocalPos = localData.setLocalPosToGetLocalPos;
        codeStats = localData.codeStats;
        defaultWays = localData.defaultWays;
        switchedRegs = localData.switchedRegs;
        finallyIndicesWithDoublePush = localData.finallyIndicesWithDoublePush;
        finallyJumpsToFinallyIndex = localData.finallyJumpsToFinallyIndex;
        finallyIndexToDefaultGraphPart = localData.finallyIndexToDefaultGraphPart;
        defaultParts = localData.defaultParts;
        finallyThrowParts = localData.finallyThrowParts;
        inGetLoops = localData.inGetLoops;
        parsedExceptionIds = localData.parsedExceptionIds;
        finallyTargetParts = localData.finallyTargetParts;
        pushDefaultPart = localData.pushDefaultPart;
        finallyKinds = localData.finallyKinds;
        seenMethods = localData.seenMethods;
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
