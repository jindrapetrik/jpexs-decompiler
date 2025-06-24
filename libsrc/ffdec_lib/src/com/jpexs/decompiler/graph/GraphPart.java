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

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.helpers.CancellableWorker;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

/**
 * Represents a part of a graph. Block of instructions which are executed in
 * sequence. No jumps or branches are allowed inside a GraphPart.
 *
 * @author JPEXS
 */
public class GraphPart implements Serializable {

    /**
     * Start IP
     */
    public int start = 0;

    /**
     * End IP
     */
    public int end = 0;

    /**
     * Next parts
     */
    public List<GraphPart> nextParts = new ArrayList<>();

    /**
     * Path
     */
    public GraphPath path = new GraphPath();

    /**
     * Previous parts
     */
    public List<GraphPart> refs = new ArrayList<>();

    /**
     * Level in the graph
     */
    public int level;

    /**
     * Discovered time in DFS
     */
    public int discoveredTime;

    /**
     * Finished time in DFS Calculated in setTime.
     */
    public int finishedTime;

    /**
     * Closed time. The node is closed when all its input edges are already
     * visited. Calculated in Graph.calculateClosedTime.
     */
    public int closedTime;

    /**
     * Order in DFS. Calculated in setTime.
     */
    public int order;

    /**
     * Number of parts following this part. Calculated in setNumblocks.
     */
    public int numBlocks = Integer.MAX_VALUE;

    /**
     * Sets the time of this part in DFS.
     *
     * @param time Time
     * @param ordered Ordered parts
     * @param visited Visited parts
     * @return Time
     */
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

    /**
     * Sets the number of blocks following this part.
     *
     * @param numBlocks Number of blocks
     */
    public void setNumblocks(int numBlocks) {
        this.numBlocks = numBlocks;
        numBlocks++;
        for (GraphPart next : nextParts) {
            if (next.numBlocks > numBlocks) {
                next.setNumblocks(numBlocks);
            }
        }
    }

    /**
     * Checks if this part leads to another part.
     *
     * @param localData Local data
     * @param gr Graph
     * @param code Code
     * @param prev Previous part
     * @param part Part to check
     * @param visited Visited parts
     * @param loops Loops
     * @param throwStates Throw states
     * @param useThrow Use throw
     * @return True if this part leads to the other part
     * @throws InterruptedException On interrupt
     */
    private boolean leadsTo(BaseLocalData localData, Graph gr, GraphSource code, GraphPart prev, GraphPart part, HashSet<GraphPart> visited, List<Loop> loops, List<ThrowState> throwStates, boolean useThrow) throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }

        Stack<GraphPart> todo = new Stack<>();
        todo.push(this);

        looptodo:
        while (!todo.isEmpty()) {
            GraphPart thisPart = todo.pop();

            GraphPart tpart = gr.checkPart(null, localData, prev, thisPart, null);
            if (tpart == null) {
                continue;
            }
            if (tpart != thisPart) {
                todo.push(tpart);
                continue;
            }
            for (Loop l : loops) {
                if (l.phase == 1) {
                    if (l.loopContinue == thisPart) {
                        continue looptodo;
                    }
                    if (l.loopPreContinue == thisPart) {
                        continue looptodo;
                    }
                    if (l.loopBreak == thisPart) {
                        //return false;    //?
                    }
                }
            }
            if (visited.contains(thisPart)) {
                continue;
            }
            visited.add(thisPart);
            if (thisPart.end < code.size() && code.get(thisPart.end).isBranch() && (code.get(thisPart.end).ignoredLoops())) {
                continue;
            }
            for (GraphPart p : thisPart.nextParts) {
                if (p == part) {
                    return true;
                }
                if (visited.contains(p)) {
                    continue;
                }
                todo.push(p);
            }
            for (ThrowState ts : throwStates) {
                if (ts.state != 1) {
                    if (ts.throwingParts.contains(thisPart)) {
                        GraphPart p = ts.targetPart;

                        if (p == part) {
                            return true;
                        }
                        if (visited.contains(p)) {
                            continue;
                        }

                        todo.push(p);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if this part leads to another part.
     *
     * @param localData Local data
     * @param gr Graph
     * @param code Code
     * @param part Part to check
     * @param loops Loops
     * @param throwStates Throw states
     * @param useThrow Use throw
     * @return True if this part leads to the other part
     * @throws InterruptedException On interrupt
     */
    public boolean leadsTo(BaseLocalData localData, Graph gr, GraphSource code, GraphPart part, List<Loop> loops, List<ThrowState> throwStates, boolean useThrow) throws InterruptedException {
        for (Loop l : loops) {
            l.leadsToMark = 0;
        }
        return leadsTo(localData, gr, code, null /*???*/, part, new HashSet<>(), loops, throwStates, useThrow);
    }

    /**
     * Constructs a new GraphPart.
     *
     * @param start Start IP
     * @param end End IP
     */
    public GraphPart(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * To string.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        if (end < start) {
            return "<-> " + (start + 1) + "-" + (end + 1);
        }
        int printStart = start + 1;
        int printEnd = end + 1;

        return "" + (printStart < 0 ? "(" : "") + printStart + (printStart < 0 ? ")" : "")
                + "-" + (printEnd < 0 ? "(" : "") + printEnd + (printEnd < 0 ? ")" : "");
    }

    /**
     * Checks if this part contains an IP.
     *
     * @param ip IP
     * @return True if this part contains the IP
     */
    public boolean containsIP(int ip) {
        return (ip >= start) && (ip <= end);
    }

    /**
     * Gets the height of this part - number of instructions in this part.
     *
     * @return Height
     */
    public int getHeight() {
        return end - start + 1;
    }

    /**
     * Gets IP at offset from start.
     *
     * @param offset Offset
     * @return IP
     */
    public int getPosAt(int offset) {
        return start + offset;
    }

    /**
     * Gets sub parts. Currently only self is allowed.
     */
    public List<GraphPart> getSubParts() {
        List<GraphPart> ret = new ArrayList<>();
        ret.add(this);
        return ret;
    }

    /**
     * Hash code.
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.start;
        return hash;
    }

    /**
     * Equals.
     *
     * @param obj Object
     * @return True if equals
     */
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
