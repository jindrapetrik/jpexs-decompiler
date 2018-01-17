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

import com.jpexs.decompiler.flash.BaseLocalData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPart implements Serializable {

    public static final int TYPE_NONE = 0;

    public static final int TYPE_LOOP_HEADER = 1;

    public static final int TYPE_PRELOOP = 3;

    public static final int TYPE_REENTRY = 2;

    public boolean traversed = false;

    public int DFSP_pos = 0;

    public GraphPart iloop_header;

    public int type = TYPE_NONE;

    public boolean irreducible = false;

    public int start = 0;

    public int end = 0;

    public int instanceCount = 0;

    public List<GraphPart> nextParts = new ArrayList<>();

    public int posX = -1;

    public int posY = -1;

    public GraphPath path = new GraphPath();

    public List<GraphPart> refs = new ArrayList<>();

    public boolean ignored = false;

    public List<Object> forContinues = new ArrayList<>();

    public int level;

    public int discoveredTime;

    public int finishedTime;

    public int order;

    public List<GraphPart> throwParts = new ArrayList<>();

    public enum StopPartType {

        NONE, AND_OR, COMMONPART
    }

    //public StopPartType stopPartType = StopPartType.NONE;
    //public TranslateStack andOrStack; // Stores stack when AND_OR stopPart has been reached
    /*public class CommonPartStack { // Stores stack when COMMONPART stopPart has been reached

     boolean isTrueStack;

     TranslateStack trueStack;

     TranslateStack falseStack;
     }/

     //public ArrayList<CommonPartStack> commonPartStacks;

     /*    public void setAndOrStack(TranslateStack stack) {
     andOrStack = stack;
     }

     public void setCommonPartStack(TranslateStack stack) {
     CommonPartStack currentStack = commonPartStacks.get(commonPartStacks.size() - 1);
     if (currentStack.isTrueStack) {
     currentStack.trueStack = stack;
     } else {
     currentStack.falseStack = stack;
     }
     }*/
    public int setTime(int time, List<GraphPart> ordered, List<GraphPart> visited) {
        if (visited.contains(this)) {
            return time;
        }
        discoveredTime = time;
        visited.add(this);
        for (GraphPart next : nextParts) {
            if (!visited.contains(next)) {
                time = next.setTime(time + 1, ordered, visited);
            }
        }
        time++;
        finishedTime = time;
        order = ordered.size();
        ordered.add(this);
        return time;
    }

    private boolean leadsTo(BaseLocalData localData, Graph gr, GraphSource code, GraphPart part, HashSet<GraphPart> visited, List<Loop> loops) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        GraphPart tpart = gr.checkPart(null, localData, this, null);
        if (tpart == null) {
            return false;
        }
        if (tpart != this) {
            return tpart.leadsTo(localData, gr, code, part, visited, loops);
        }
        Loop currentLoop = null;
        for (Loop l : loops) {
            /*if(l.phase==0){
             if(l.loopContinue==this){
             l.leadsToMark = 1;
             next = l.loopBreak;
             currentLoop = l;
             continue;
             }
             }*/
            if (l.phase == 1) {
                if (l.loopContinue == this) {
                    return false;
                }
                if (l.loopPreContinue == this) {
                    return false;
                }
                if (l.loopBreak == this) {
                    //return false;    //?
                }
            }
        }
        if (visited.contains(this)) {
            return false;
        }
        /*if (loops.contains(this)) {
         return false;
         }*/
        visited.add(this);
        if (end < code.size() && code.get(end).isBranch() && (code.get(end).ignoredLoops())) {
            return false;
        }
        for (GraphPart p : nextParts) {
            if (p == part) {
                return true;
            } else if (p.leadsTo(localData, gr, code, part, visited, loops)) {
                return true;
            }
        }
        for (GraphPart p : throwParts) {
            if (p == part) {
                return true;
            } else if (p.leadsTo(localData, gr, code, part, visited, loops)) {
                return true;
            }
        }
        return false;
    }

    public boolean leadsTo(BaseLocalData localData, Graph gr, GraphSource code, GraphPart part, List<Loop> loops) throws InterruptedException {
        for (Loop l : loops) {
            l.leadsToMark = 0;
        }
        return leadsTo(localData, gr, code, part, new HashSet<>(), loops);
    }

    public GraphPart(int start, int end) {
        this.start = start;
        this.end = end;
    }

    private GraphPart getNextPartPath(GraphPart original, GraphPath path, List<GraphPart> visited) {
        if (visited.contains(this) && (this == original)) {
            return null;
        }
        if (visited.contains(this) && (this != original)) {
            return null;
        }
        visited.add(this);
        for (GraphPart p : nextParts) {
            if (p == original) {
                continue;
            }
            if (p.path.equals(path)) {
                return p;
            } else if (p.path.length() >= path.length()) {
                GraphPart gp = p.getNextPartPath(original, path, visited);
                if (gp != null) {
                    return gp;
                }
            }
        }
        return null;
    }

    public GraphPart getNextPartPath(List<GraphPart> ignored) {
        List<GraphPart> visited = new ArrayList<>();
        visited.addAll(ignored);
        if (visited.contains(this)) {
            visited.remove(this);
        }
        return getNextPartPath(this, path, visited);
    }

    public GraphPart getNextSuperPartPath(List<GraphPart> ignored) {
        List<GraphPart> visited = new ArrayList<>();
        visited.addAll(ignored);
        return getNextSuperPartPath(this, path, visited);
    }

    private GraphPart getNextSuperPartPath(GraphPart original, GraphPath path, List<GraphPart> visited) {
        if (visited.contains(this)) {
            return null;
        }
        visited.add(this);
        for (GraphPart p : nextParts) {
            if (p == original) {
                continue;
            }
            if (p.path.length() < path.length()) {
                return p;
            } else {
                GraphPart gp = p.getNextSuperPartPath(original, path, visited);
                if (gp != null) {
                    return gp;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        if (end < start) {
            return "<-> " + (start + 1) + "-" + (end + 1);
        }
        return "" + (start + 1) + "-" + (end + 1) + (instanceCount > 1 ? "(" + instanceCount + " links)" : "");// + "  p" + path;
    }

    public boolean containsIP(int ip) {
        return (ip >= start) && (ip <= end);
    }

    private boolean containsPart(GraphPart part, GraphPart what, List<GraphPart> used) {
        if (used.contains(part)) {
            return false;
        }
        used.add(part);
        for (GraphPart subpart : part.nextParts) {
            if (subpart == what) {
                return true;
            }
            if (containsPart(subpart, what, used)) {
                return true;
            }
        }
        return false;
    }

    public int getHeight() {
        return end - start + 1;
    }

    public int getPosAt(int offset) {
        return start + offset;
    }

    public boolean containsPart(GraphPart what) {
        return containsPart(this, what, new ArrayList<>());
    }

    public List<GraphPart> getSubParts() {
        List<GraphPart> ret = new ArrayList<>();
        ret.add(this);
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.start;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GraphPart)) {
            return false;
        }
        final GraphPart other = (GraphPart) obj;
        if (start != other.start) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        return true;
    }
}
