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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.LinkedIdentityHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AVM2 local data.
 *
 * @author JPEXS
 */
public class AVM2LocalData extends BaseLocalData {

    /**
     * Whether the method is static
     */
    public Boolean isStatic;

    /**
     * Current class index
     */
    public Integer classIndex;

    /**
     * Local registers values
     */
    public HashMap<Integer, GraphTargetItem> localRegs;

    /**
     * Scope stack
     */
    public ScopeStack scopeStack;

    /**
     * Local scope stack
     */
    public ScopeStack localScopeStack;

    /**
     * Current method body
     */
    public MethodBody methodBody;

    /**
     * Current call stack
     */
    public List<MethodBody> callStack;

    /**
     * Current ABC file
     */
    public ABC abc;

    /**
     * ABCIndexing
     */
    public AbcIndexing abcIndex;

    /**
     * Local register names
     */
    public HashMap<Integer, String> localRegNames;

    /**
     * Local register types
     */
    public HashMap<Integer, GraphTargetItem> localRegTypes;

    /**
     * Fully qualified names
     */
    public List<DottedChain> fullyQualifiedNames;

    /**
     * Parsed exceptions
     */
    public List<ABCException> parsedExceptions = new ArrayList<>();

    /**
     * Parsed exception ids
     */
    public List<Integer> parsedExceptionIds = new ArrayList<>();

    /**
     * Mapped jumps from pushbyte xx part to appropriate lookupswitch branch
     */
    public Map<GraphPart, GraphPart> finallyJumps = new HashMap<>();

    /**
     * Mapping from source part (as in finallyJumps) to finally exception index
     */
    public Map<GraphPart, Integer> finallyJumpsToFinallyIndex = new HashMap<>();

    /**
     * Mapping from finally exception index to default part
     */
    public Map<Integer, GraphPart> finallyIndexToDefaultGraphPart = new HashMap<>();

    /**
     * Mapping from exception index to switch part
     */
    public Map<Integer, GraphPart> ignoredSwitches = new HashMap<>();

    /**
     * Mapping from exception index to switch defaultPart
     */
    public Map<Integer, GraphPart> defaultParts = new HashMap<>();

    /**
     * Mapping from finally index to register index
     */
    public Map<Integer, Integer> switchedRegs = new HashMap<>();

    /**
     * Mapping from finally index push default part
     */
    public Map<Integer, GraphPart> pushDefaultPart = new HashMap<>();

    /**
     * Mapping from finally index to finally kind See AVM2Graph.FINALLY_KIND_*
     * constants.
     */
    public Map<Integer, Integer> finallyKinds = new HashMap<>();

    /**
     * Mapping from finally exception index to switch throw part
     */
    public Map<Integer, GraphPart> finallyThrowParts = new HashMap<>();

    /**
     * Mapping from finally exception index to target part
     */
    public Map<Integer, GraphPart> finallyTargetParts = new HashMap<>();

    /**
     * Mapping from switchedPart to index of nextpart
     */
    public Map<GraphPart, Integer> defaultWays = new HashMap<>();

    /**
     * Current script index
     */
    public Integer scriptIndex;

    /**
     * Local registers assignment ips. Maps register index to ip where it was
     * assigned.
     */
    public HashMap<Integer, Integer> localRegAssignmentIps;

    /**
     * Current instruction pointer
     */
    public Integer ip;

    /**
     * AVM2 code
     */
    public AVM2Code code;

    /**
     * Whether this has default to primitive
     */
    public boolean thisHasDefaultToPrimitive;

    /**
     * Mapping from setLocal position to getLocal positions
     */
    public Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = new HashMap<>();

    /**
     * Code stats
     */
    public CodeStats codeStats;

    /**
     * Indices of finally exceptions with double push
     */
    public Set<Integer> finallyIndicesWithDoublePush = new HashSet<>();

    /**
     * Whether we are in get loops
     */
    public boolean inGetLoops = false;

    /**
     * Set of seen methods
     */
    public Set<Integer> seenMethods = new HashSet<>();

    
    /**
     * Bottom set locals
     */
    public LinkedIdentityHashSet<SetLocalAVM2Item> bottomSetLocals = new LinkedIdentityHashSet<>();    
    
    /**
     * Constructs a new AVM2LocalData
     */
    public AVM2LocalData() {

    }

    /**
     * Returns set of getLocal positions for given setLocal position
     *
     * @param setLocalPos SetLocal position
     * @return Set of getLocal positions
     */
    public Set<Integer> getSetLocalUsages(int setLocalPos) {
        if (setLocalPosToGetLocalPos == null) {
            return new HashSet<>();
        }
        if (!setLocalPosToGetLocalPos.containsKey(setLocalPos)) {
            return new HashSet<>();
        }
        return setLocalPosToGetLocalPos.get(setLocalPos);
    }

    /**
     * Constructs a new AVM2LocalData from another AVM2LocalData
     *
     * @param localData Another AVM2LocalData
     */
    public AVM2LocalData(AVM2LocalData localData) {
        allSwitchParts = localData.allSwitchParts;
        isStatic = localData.isStatic;
        classIndex = localData.classIndex;
        localRegs = localData.localRegs;
        scopeStack = localData.scopeStack;
        localScopeStack = localData.localScopeStack;
        methodBody = localData.methodBody;
        callStack = localData.callStack;
        abc = localData.abc;
        abcIndex = localData.abcIndex;
        localRegNames = localData.localRegNames;
        localRegTypes = localData.localRegTypes;
        fullyQualifiedNames = localData.fullyQualifiedNames;
        parsedExceptions = localData.parsedExceptions;
        finallyJumps = localData.finallyJumps;
        ignoredSwitches = localData.ignoredSwitches;
        //ignoredSwitches2 = localData.ignoredSwitches2;
        scriptIndex = localData.scriptIndex;
        localRegAssignmentIps = localData.localRegAssignmentIps;
        ip = localData.ip;
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
        bottomSetLocals = localData.bottomSetLocals;
    }

    /**
     * Returns constant pool
     *
     * @return Constant pool
     */
    public AVM2ConstantPool getConstants() {
        return abc.constants;
    }

    /**
     * Returns ABC method infos
     *
     * @return List of MethodInfo
     */
    public List<MethodInfo> getMethodInfo() {
        return abc.method_info;
    }

    /**
     * Returns ABC instance infos
     *
     * @return List of InstanceInfo
     */
    public List<InstanceInfo> getInstanceInfo() {
        return abc.instance_info;
    }

    /**
     * Returns ABC script infos
     *
     * @return List of ScriptInfo
     */
    public List<ScriptInfo> getScriptInfo() {
        return abc.script_info;
    }
}
