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
package com.jpexs.decompiler.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A loop in a graph.
 *
 * @author JPEXS
 */
public class Loop implements Serializable {

    /**
     * Continue part of the loop
     */
    public GraphPart loopContinue;

    /**
     * Break part of the loop
     */
    public GraphPart loopBreak;

    /**
     * Precontinue part of the loop. A precontinue is a part of the loop that is
     * executed before the continue part. Example of this is a for loop with
     * continue statement.
     */
    public GraphPart loopPreContinue;

    /**
     * Back edges of the loop
     */
    public Set<GraphPart> backEdges = new HashSet<>();

    /**
     * Break candidates of the loop
     */
    public List<GraphPart> breakCandidates = new ArrayList<>();

    /**
     * Levels of the break candidates
     */
    public List<Integer> breakCandidatesLevels = new ArrayList<>();

    /**
     * Unique id of the loop
     */
    public final long id;

    /**
     * Mark for leads to method
     */
    public int leadsToMark;

    /**
     * Mark for reachable method
     */
    public int reachableMark;

    /**
     * Phase of the loop. The decompiler marks here whether the loop is already
     * processed or not.
     */
    public int phase;

    /**
     * Break candidates are locked
     */
    public int breakCandidatesLocked = 0;
    
    /**
     * Stop parts before entering the loop
     */
    public List<GraphPart> stopParts = new ArrayList<>();

    /**
     * Constructs a loop
     *
     * @param id Unique id of the loop
     * @param loopContinue Continue part of the loop
     * @param loopBreak Break part of the loop
     */
    public Loop(long id, GraphPart loopContinue, GraphPart loopBreak) {
        this.loopContinue = loopContinue;
        this.loopBreak = loopBreak;
        this.id = id;
    }

    /**
     * To string method
     *
     * @return String representation of the loop
     */
    @Override
    public String toString() {
        Set<String> edgesAsStr = new HashSet<>();
        for (GraphPart p : backEdges) {
            edgesAsStr.add(p.toString());
        }
        Set<String> bcAsStr = new LinkedHashSet<>();
        for (int i = 0; i < breakCandidates.size(); i++) {
            bcAsStr.add(breakCandidates.get(i) + " - level " + breakCandidatesLevels.get(i) + " - numblocks " + breakCandidates.get(i).numBlocks);
        }

        return "loop(id:" + id + (loopPreContinue != null ? ",precontinue:" + loopPreContinue : "") + ",continue:" + loopContinue + ", break:" + loopBreak + ", phase:" + phase + ", backedges: " + String.join(",", edgesAsStr) + ", breakCandidates: " + String.join(",", bcAsStr) + ")";
    }

    /**
     * Hash code
     *
     * @return Hash code of the loop
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    /**
     * Equals
     *
     * @param obj Object to compare
     * @return True if the object is equal to this loop
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Loop other = (Loop) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
