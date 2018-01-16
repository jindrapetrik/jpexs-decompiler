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
package com.jpexs.decompiler.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Loop implements Serializable {

    public GraphPart loopContinue;

    public GraphPart loopBreak;

    public GraphPart loopPreContinue;

    public List<GraphPart> breakCandidates = new ArrayList<>();

    public List<Integer> breakCandidatesLevels = new ArrayList<>();

    public final long id;

    public int leadsToMark;

    public int reachableMark;

    public int phase;

    public int breakCandidatesLocked = 0;

    public Loop(long id, GraphPart loopContinue, GraphPart loopBreak) {
        this.loopContinue = loopContinue;
        this.loopBreak = loopBreak;
        this.id = id;
    }

    @Override
    public String toString() {
        return "loop(id:" + id + (loopPreContinue != null ? ",precontinue:" + loopPreContinue : "") + ",continue:" + loopContinue + ", break:" + loopBreak + ", phase:" + phase + ")";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

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
