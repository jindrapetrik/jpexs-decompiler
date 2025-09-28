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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.avm2.parser.script.NamespaceItem;
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
 * Local data for source generator.
 *
 * @author JPEXS
 */
public class SourceGeneratorLocalData implements Serializable {

    /**
     * Map of variable name to register number.
     */
    public HashMap<String, Integer> registerVars;

    /**
     * In function level.
     */
    public Integer inFunction;

    /**
     * In method.
     */
    public Boolean inMethod;

    /**
     * For in level.
     */
    public Integer forInLevel;

    //TODO: handle AVM2 separately
    /**
     * List of AVM2 exceptions.
     */
    public List<ABCException> exceptions = new ArrayList<>();

    /**
     * List of finally Ids
     */
    public List<Integer> finallyCatches = new ArrayList<>();

    /**
     * List of finally opened loops
     */
    public List<List<Long>> finallyOpenedLoops = new ArrayList<>();

    /**
     * Counter of finally uses
     */
    public Map<Integer, Integer> finallyCounter = new HashMap<>();

    /**
     * Register number for finally block
     */
    public int finallyRegister = -1;

    /**
     * Current class base name
     */
    public String currentClassBaseName; //FIXME! Suffixed or not?

    /**
     * Super class name
     */
    public String superClass = null;

    /**
     * Super package name
     */
    public DottedChain superPkg = null;

    /**
     * Activation register number
     */
    public int activationReg = 0;

    /**
     * Call stack
     */
    public List<MethodBody> callStack = new ArrayList<>();

    /**
     * MethodBody trait usages. Map of MethodBody to list of used trait indexes.
     */
    public Map<MethodBody, List<Integer>> traitUsages = new HashMap<>();

    /**
     * Current package
     */
    public DottedChain pkg = DottedChain.TOPLEVEL;

    /**
     * Scope stack
     */
    public List<GraphTargetItem> scopeStack = new ArrayList<>();

    /**
     * Current script
     */
    public ScriptInfo currentScript;

    /**
     * Current script index
     */
    public Integer scriptIndex = null;

    /**
     * Is in sub method
     */
    public boolean subMethod = false;

    /**
     * Current private namespace id
     */
    public int privateNs = 0;

    /**
     * Current protected namespace id
     */
    public int protectedNs = 0;

    /**
     * Is in static method
     */
    public boolean isStatic = false;

    /**
     * Opened loop ids
     */
    public List<Long> openedLoops = new ArrayList<>();

    /**
     * Opened loop ids for catch blocks
     */
    public List<List<Long>> catchesOpenedLoops = new ArrayList<>();

    /**
     * Temp registers used in catch blocks
     */
    public List<Integer> catchesTempRegs = new ArrayList<>();

    /**
     * Document class
     */
    public String documentClass;

    /**
     * Is second run
     */
    public boolean secondRun = false;
    
    /**
     * Number context
     */
    public Integer numberContext = null;
    
    /**
     * Imported classes
     */
    public List<DottedChain> importedClasses = new ArrayList<>();
    
    /**
     * Opened namespaces
     */
    public List<NamespaceItem> openedNamespaces = new ArrayList<>();
    
    /**
     * Current method return type
     */
    public GraphTargetItem returnType = null;

    /**
     * Gets full class name.
     *
     * @return Full class name
     */
    public String getFullClass() {
        return pkg == null ? currentClassBaseName : pkg.addWithSuffix(currentClassBaseName).toRawString();
    }

    /**
     * Constructs new SourceGeneratorLocalData.
     *
     * @param registerVars Map of variable name to register number
     * @param inFunction In function level
     * @param inMethod In method
     * @param forInLevel For in level
     */
    public SourceGeneratorLocalData(HashMap<String, Integer> registerVars, Integer inFunction, Boolean inMethod, Integer forInLevel) {
        this.registerVars = registerVars;
        this.inFunction = inFunction;
        this.inMethod = inMethod;
        this.forInLevel = forInLevel;
    }
}
