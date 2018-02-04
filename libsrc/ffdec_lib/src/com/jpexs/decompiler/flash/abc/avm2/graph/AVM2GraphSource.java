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
package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConvertOutput;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class AVM2GraphSource extends GraphSource {

    private final AVM2Code code;

    boolean isStatic;

    int classIndex;

    int scriptIndex;

    HashMap<Integer, GraphTargetItem> localRegs;

    ScopeStack scopeStack;

    ABC abc;

    MethodBody body;

    HashMap<Integer, String> localRegNames;

    List<DottedChain> fullyQualifiedNames;

    HashMap<Integer, Integer> localRegAssigmentIps;

    HashMap<Integer, List<Integer>> refs;

    public AVM2Code getCode() {
        return code;
    }

    public AVM2GraphSource(AVM2Code code, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ScopeStack scopeStack, ABC abc, MethodBody body, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, HashMap<Integer, Integer> localRegAssigmentIp, HashMap<Integer, List<Integer>> refs) {
        this.code = code;
        this.isStatic = isStatic;
        this.classIndex = classIndex;
        this.localRegs = localRegs;
        this.scopeStack = scopeStack;
        this.abc = abc;
        this.body = body;
        this.localRegNames = localRegNames;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.scriptIndex = scriptIndex;
        this.localRegAssigmentIps = localRegAssigmentIp;
        this.refs = refs;
        code.calculateDebugFileLine(abc);
    }

    @Override
    public Set<Long> getImportantAddresses() {
        return code.getImportantOffsets(body, false);
    }

    @Override
    public String insToString(int pos) {
        if (pos < code.code.size()) {
            return code.code.get(pos).toStringNoAddress(abc.constants, fullyQualifiedNames);
        }
        return "";
    }

    @Override
    public int size() {
        return code.code.size();
    }

    @Override
    public AVM2Instruction get(int pos) {
        return code.code.get(pos);
    }

    @Override
    public boolean isEmpty() {
        return code.code.isEmpty();
    }

    @Override
    public List<GraphTargetItem> translatePart(GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        ScopeStack newstack = ((AVM2LocalData) localData).scopeStack;
        Reference<GraphSourceItem> lineStartItem = new Reference<>(localData.lineStartInstruction);
        ConvertOutput co = code.toSourceOutput(((AVM2LocalData) localData).thisHasDefaultToPrimitive, lineStartItem, path, part, false, isStatic, scriptIndex, classIndex, localRegs, stack, newstack, abc, body, start, end, localRegNames, fullyQualifiedNames, new boolean[size()], localRegAssigmentIps, refs);
        localData.lineStartInstruction = lineStartItem.getVal();
        ret.addAll(co.output);
        return ret;
    }

    @Override
    public int adr2pos(long adr) {
        return code.adr2pos(adr);
    }

    @Override
    public long pos2adr(int pos) {
        return code.pos2adr(pos);
    }
}
