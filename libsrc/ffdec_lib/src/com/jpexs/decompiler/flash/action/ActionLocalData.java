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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Local data for ActionScript decompilation.
 *
 * @author JPEXS
 */
public class ActionLocalData extends BaseLocalData {

    /**
     * Uninitialized class traits - map of class name to map of trait name to
     * trait
     */
    public final Map<String, Map<String, Trait>> uninitializedClassTraits;

    /**
     * Register names - map of register number to register name
     */
    public final HashMap<Integer, String> regNames;

    /**
     * Variables - map of variable name to variable target item
     */
    public final HashMap<String, GraphTargetItem> variables;

    /**
     * Functions - map of function name to function target item
     */
    public final HashMap<String, GraphTargetItem> functions;

    /**
     * Line start action
     */
    public GraphSourceItem lineStartAction;

    /**
     * Is inside doInitAction
     */
    public boolean insideDoInitAction;

    /**
     * Constructs new ActionLocalData
     *
     * @param secondPassData Second pass data
     * @param insideDoInitAction Is inside doInitAction
     * @param uninitializedClassTraits Uninitialized class traits
     */
    public ActionLocalData(SecondPassData secondPassData, boolean insideDoInitAction, Map<String, Map<String, Trait>> uninitializedClassTraits, Set<String> usedDeobfuscations) {
        this.secondPassData = secondPassData;
        regNames = new HashMap<>();
        variables = new HashMap<>();
        functions = new HashMap<>();
        this.insideDoInitAction = insideDoInitAction;
        this.uninitializedClassTraits = uninitializedClassTraits;
        this.usedDeobfuscations = usedDeobfuscations;
    }

    /**
     * Constructs new ActionLocalData
     *
     * @param secondPassData Second pass data
     * @param insideDoInitAction Is inside doInitAction
     * @param regNames Register names
     * @param uninitializedClassTraits Uninitialized class traits
     */
    public ActionLocalData(SecondPassData secondPassData, boolean insideDoInitAction, HashMap<Integer, String> regNames, Map<String, Map<String, Trait>> uninitializedClassTraits, Set<String> usedDeobfuscations) {
        this.regNames = regNames;
        this.secondPassData = secondPassData;
        variables = new HashMap<>();
        functions = new HashMap<>();
        this.insideDoInitAction = insideDoInitAction;
        this.uninitializedClassTraits = uninitializedClassTraits;
        this.usedDeobfuscations = usedDeobfuscations;
    }

    /**
     * Constructs new ActionLocalData
     *
     * @param switchParts Switch parts
     * @param secondPassData Second pass data
     * @param insideDoInitAction Is inside doInitAction
     * @param regNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param uninitializedClassTraits Uninitialized class traits
     */
    public ActionLocalData(Set<GraphPart> switchParts, SecondPassData secondPassData, boolean insideDoInitAction, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, Map<String, Map<String, Trait>> uninitializedClassTraits, Set<String> usedDeobfuscations) {
        this.allSwitchParts = switchParts;
        this.regNames = regNames;
        this.variables = variables;
        this.functions = functions;
        this.insideDoInitAction = insideDoInitAction;
        this.secondPassData = secondPassData;
        this.uninitializedClassTraits = uninitializedClassTraits;
        this.usedDeobfuscations = usedDeobfuscations;
    }
}
