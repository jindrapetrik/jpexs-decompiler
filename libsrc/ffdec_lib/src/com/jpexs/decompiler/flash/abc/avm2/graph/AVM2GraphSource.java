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
package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConvertOutput;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * AVM2 graph source.
 *
 * @author JPEXS
 */
public class AVM2GraphSource extends GraphSource {

    /**
     * AVM2 code
     */
    private final AVM2Code code;

    /**
     * Is static
     */
    boolean isStatic;

    /**
     * Class index
     */
    int classIndex;

    /**
     * Script index
     */
    int scriptIndex;

    /**
     * Local registers - map of register index to value item
     */
    HashMap<Integer, GraphTargetItem> localRegs;

    /**
     * ABC
     */
    ABC abc;

    /**
     * Method body
     */
    MethodBody body;

    /**
     * Local register names - map of register index to name
     */
    HashMap<Integer, String> localRegNames;

    /**
     * Fully qualified names
     */
    List<DottedChain> fullyQualifiedNames;

    /**
     * Local register assignment IPs - map of register index to IP
     */
    HashMap<Integer, Integer> localRegAssignmentIps;

    /**
     * Get AVM2 code
     *
     * @return AVM2 code
     */
    public AVM2Code getCode() {
        return code;
    }

    /**
     * Constructs a new AVM2 graph source
     *
     * @param code AVM2 code
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param localRegs Local registers
     * @param abc ABC
     * @param body Method body
     * @param localRegNames Local register names
     * @param fullyQualifiedNames Fully qualified names
     * @param localRegAssignmentIp Local register assignment IPs
     */
    public AVM2GraphSource(AVM2Code code, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ABC abc, MethodBody body, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, HashMap<Integer, Integer> localRegAssignmentIp) {
        this.code = code;
        this.isStatic = isStatic;
        this.classIndex = classIndex;
        this.localRegs = localRegs;
        this.abc = abc;
        this.body = body;
        this.localRegNames = localRegNames;
        this.fullyQualifiedNames = fullyQualifiedNames;
        this.scriptIndex = scriptIndex;
        this.localRegAssignmentIps = localRegAssignmentIp;
        code.calculateDebugFileLine(abc);
    }

    /**
     * Gets important addresses
     *
     * @return Important addresses
     */
    @Override
    public Set<Long> getImportantAddresses() {
        return code.getImportantOffsets(body, false);
    }

    /**
     * Converts instruction at the specified position to string
     *
     * @param pos Position of the instruction
     * @return Instruction as string
     */
    @Override
    public String insToString(int pos) {
        if (pos < code.code.size()) {
            return code.code.get(pos).toStringNoAddress(abc.constants, fullyQualifiedNames);
        }
        return "";
    }

    /**
     * Gets the size of the graph source
     *
     * @return The size of the graph source
     */
    @Override
    public int size() {
        return code.code.size();
    }

    /**
     * Gets the graph source item at the specified position
     *
     * @param pos Position of the graph source item
     * @return The graph source item at the specified position
     */
    @Override
    public AVM2Instruction get(int pos) {
        return code.code.get(pos);
    }

    /**
     * Checks if the graph source is empty
     *
     * @return True if the graph source is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return code.code.isEmpty();
    }

    /**
     * Translates the part of the graph source
     *
     * @param graph Graph
     * @param part Graph part
     * @param localData Local data
     * @param stack Translate stack
     * @param start Start position
     * @param end End position
     * @param staticOperation Unused
     * @param path Path
     * @return List of graph target items
     * @throws InterruptedException On interrupt
     */
    @Override
    public List<GraphTargetItem> translatePart(Graph graph, GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        Reference<GraphSourceItem> lineStartItem = new Reference<>(localData.lineStartInstruction);
        ConvertOutput co = code.toSourceOutput(localData.allSwitchParts, ((AVM2LocalData) localData).callStack, ((AVM2LocalData) localData).abcIndex, ((AVM2LocalData) localData).setLocalPosToGetLocalPos, ((AVM2LocalData) localData).thisHasDefaultToPrimitive, lineStartItem, path, part, false, isStatic, scriptIndex, classIndex, localRegs, stack, ((AVM2LocalData) localData).scopeStack, ((AVM2LocalData) localData).localScopeStack, abc, body, start, end, localRegNames, ((AVM2LocalData) localData).localRegTypes, fullyQualifiedNames, new boolean[size()], localRegAssignmentIps, ((AVM2LocalData) localData).bottomSetLocals);
        localData.lineStartInstruction = lineStartItem.getVal();
        ret.addAll(co.output);
        return ret;
    }

    /**
     * Converts address to position
     *
     * @param adr Address
     * @return Position
     */
    @Override
    public int adr2pos(long adr) {
        return code.adr2pos(adr);
    }

    /**
     * Converts address to position
     *
     * @param adr Address
     * @param nearest Nearest
     * @return Position
     */
    @Override
    public int adr2pos(long adr, boolean nearest) {
        return code.adr2pos(adr, true);
    }

    /**
     * Converts position to address
     *
     * @param pos Position
     * @return Address
     */
    @Override
    public long pos2adr(int pos) {
        return code.pos2adr(pos);
    }
}
