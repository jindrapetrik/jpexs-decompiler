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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AVM2LocalData extends BaseLocalData {

    public Boolean isStatic;
    public Integer classIndex;
    public HashMap<Integer, GraphTargetItem> localRegs;
    public ScopeStack scopeStack;
    public AVM2ConstantPool constants;
    public List<MethodInfo> methodInfo;
    public MethodBody methodBody;
    public ABC abc;
    public HashMap<Integer, String> localRegNames;
    public List<String> fullyQualifiedNames;
    public ArrayList<ABCException> parsedExceptions;
    public ArrayList<Integer> finallyJumps;
    public ArrayList<Integer> ignoredSwitches;
    public Integer scriptIndex;
    public HashMap<Integer, Integer> localRegAssignmentIps;
    public Integer ip;
    public HashMap<Integer, List<Integer>> refs;
    public AVM2Code code;

    public AVM2LocalData() {

    }

    public AVM2LocalData(AVM2LocalData localData) {
        isStatic = localData.isStatic;
        classIndex = localData.classIndex;
        localRegs = localData.localRegs;
        scopeStack = localData.scopeStack;
        constants = localData.constants;
        methodInfo = localData.methodInfo;
        methodBody = localData.methodBody;
        abc = localData.abc;
        localRegNames = localData.localRegNames;
        fullyQualifiedNames = localData.fullyQualifiedNames;
        parsedExceptions = localData.parsedExceptions;
        finallyJumps = localData.finallyJumps;
        ignoredSwitches = localData.ignoredSwitches;
        scriptIndex = localData.scriptIndex;
        localRegAssignmentIps = localData.localRegAssignmentIps;
        ip = localData.ip;
        refs = localData.refs;
        code = localData.code;
    }
}
