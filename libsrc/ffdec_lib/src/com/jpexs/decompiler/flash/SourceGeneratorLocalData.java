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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class SourceGeneratorLocalData implements Serializable {

    public HashMap<String, Integer> registerVars;

    public Integer inFunction;

    public Boolean inMethod;

    public Integer forInLevel;

    // TODO: handle AVM2 separately
    public List<ABCException> exceptions = new ArrayList<>();

    public List<Integer> finallyCatches = new ArrayList<>();

    public Map<Integer, Integer> finallyCounter = new HashMap<>();

    public int finallyRegister = -1;

    public String currentClass; //FIXME! Suffixed or not?

    public String superClass = null;

    public DottedChain superPkg = null;

    public int activationReg = 0;

    public List<MethodBody> callStack = new ArrayList<>();

    public Map<MethodBody, List<Integer>> traitUsages = new HashMap<>();

    public DottedChain pkg = DottedChain.TOPLEVEL;

    public List<GraphTargetItem> scopeStack = new ArrayList<>();

    public boolean documentClass;

    public ScriptInfo currentScript;

    public boolean subMethod = false;

    public int privateNs = 0;

    public int protectedNs = 0;

    public boolean isStatic = false;

    public String getFullClass() {
        return pkg == null ? currentClass : pkg.addWithSuffix(currentClass).toRawString();
    }

    public SourceGeneratorLocalData(HashMap<String, Integer> registerVars, Integer inFunction, Boolean inMethod, Integer forInLevel) {
        this.registerVars = registerVars;
        this.inFunction = inFunction;
        this.inMethod = inMethod;
        this.forInLevel = forInLevel;
    }
}
