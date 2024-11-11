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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Local data for decompilation.
 *
 * @author JPEXS
 */
public class LocalData {

    /**
     * Empty local data
     */
    public static LocalData empty = new LocalData();

    /**
     * Constant pool
     */
    public ConstantPool constants;

    /**
     * AVM2 constant pool
     */
    public AVM2ConstantPool constantsAvm2;

    /**
     * ABC indexing
     */
    public AbcIndexing abcIndex;

    /**
     * Call stack
     */
    public List<MethodBody> callStack;

    /**
     * Local register names
     */
    public HashMap<Integer, String> localRegNames;

    /**
     * Fully qualified names
     */
    public List<DottedChain> fullyQualifiedNames;

    /**
     * Seen methods
     */
    public Set<Integer> seenMethods = new HashSet<>();

    /**
     * ABC
     */
    public ABC abc;
    
    /**
     * Script export mode
     */
    public ScriptExportMode exportMode;

    /**
     * SWF version
     */
    public int swfVersion;
    
    /**
     * Creates a new local data
     *
     * @param constants Constant pool
     * @return Local data
     */
    public static LocalData create(ConstantPool constants) {
        LocalData localData = new LocalData();
        localData.constants = constants;
        return localData;
    }

    /**
     * Creates a new local data
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param abc ABC
     * @param localRegNames Local register names
     * @param fullyQualifiedNames Fully qualified names
     * @param seenMethods Seen methods
     * @param exportMode Export mode 
     * @param swfVersion SWF version
     * @return Local data
     */
    public static LocalData create(List<MethodBody> callStack, AbcIndexing abcIndex, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, Set<Integer> seenMethods, ScriptExportMode exportMode, int swfVersion) {
        LocalData localData = new LocalData();
        localData.abc = abc;
        localData.constantsAvm2 = abc.constants;
        localData.localRegNames = localRegNames;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        localData.seenMethods = seenMethods;
        localData.abcIndex = abcIndex;
        localData.callStack = callStack;
        localData.exportMode = exportMode;                
        localData.swfVersion = swfVersion;
        return localData;
    }
}
