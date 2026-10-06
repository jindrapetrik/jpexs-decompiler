/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import java.util.BitSet;
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
     * Closed time. The node is closed when all its input edges are already
     * visited. Calculated in Graph.calculateClosedTime.
     */
    public int closedTime;

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
     * @param firstCanBeLoopContinue Can entry point be loop continue?
     * @return True if this part leads to the other part
     * @throws InterruptedException On interrupt
     */
    private boolean hasPathTo(BaseLocalData localData, Graph gr, GraphSource code, GraphPart prev,
            GraphPart part, VisitedGraphParts visited, BitSet reachableParts, List<Loop> loops,
            List<ThrowState> throwStates, boolean firstCanBeLoopContinue) throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }

        Stack<GraphPart> todo = new Stack<>();
        todo.push(this);
        boolean first = firstCanBeLoopContinue; //This is big HACK of how to get always-break working

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
                        if (!first) {
                            continue looptodo;
                        }
                    }
                    if (l.loopPreContinue == thisPart) {
                        if (!first) {
                            continue looptodo;
                        }
                    }
                    if (l.loopBreak == thisPart) {
                        //return false;    //?
                    }
                }
            }
            first = false;
            if (visited.contains(thisPart)) {
                continue;
            }
            visited.add(thisPart);
            for (GraphPart p : thisPart.nextParts) {
                if (reachableParts != null) {
                    reachableParts.set(gr.getReachabilityPartIndex(p));
                } else if (p == part) {
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

                        if (reachableParts != null) {
                            reachableParts.set(gr.getReachabilityPartIndex(p));
                        } else if (p == part) {
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

    private BitSet getReachableParts(BaseLocalData localData, Graph gr, GraphSource code,
            List<Loop> loops, List<ThrowState> throwStates, boolean firstCanBeLoopContinue) throws InterruptedException {
        BitSet reachableParts = new BitSet();
        hasPathTo(localData, gr, code, null /*???*/, null, new VisitedGraphParts(code.size()),
                reachableParts, loops, throwStates, firstCanBeLoopContinue);
        return reachableParts;
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
     * @param firstCanBeLoopContinue Can entry point be loop continue?
     * @return True if this part leads to the other part
     * @throws InterruptedException On interrupt
     */
    public boolean leadsTo(BaseLocalData localData, Graph gr, GraphSource code, GraphPart part, List<Loop> loops, List<ThrowState> throwStates, boolean firstCanBeLoopContinue) throws InterruptedException {
        for (Loop l : loops) {
            l.leadsToMark = 0;
        }
        BitSet cachedResult = gr.getCachedReachability(localData, this, loops, throwStates, firstCanBeLoopContinue);
        int partIndex = gr.getReachabilityPartIndex(part);
        if (cachedResult != null) {
            return cachedResult.get(partIndex);
        }
        if (gr.shouldCacheReachability(localData, this, loops, throwStates, firstCanBeLoopContinue)) {
            BitSet reachableParts = getReachableParts(localData, gr, code, loops, throwStates, firstCanBeLoopContinue);
            gr.cacheReachability(localData, this, loops, throwStates, firstCanBeLoopContinue, reachableParts);
            return reachableParts.get(partIndex);
        }
        return hasPathTo(localData, gr, code, null /*???*/, part, new VisitedGraphParts(code.size()),
                null, loops, throwStates, firstCanBeLoopContinue);
        //return gr.partLeadsTo(localData, this, part, code, loops, throwStates, firstCanBeLoopContinue);
    }

    /**
     * Visited set optimized for regular graph parts, whose start is an
     * instruction index. Unusual synthetic parts and duplicate starts retain
     * full GraphPart equality through fallback sets.
     */
    private static final class VisitedGraphParts {

        private static final int MAX_INDEXED_STARTS = 1 << 16;

        private final int[] endByStart;
        private HashSet<Long> duplicateStarts;
        private HashSet<GraphPart> syntheticParts;

        VisitedGraphParts(int codeSize) {
            endByStart = new int[Math.min(codeSize, MAX_INDEXED_STARTS)];
        }

        boolean contains(GraphPart part) {
            if (isRegular(part)) {
                int storedEnd = endByStart[part.start];
                if (storedEnd == part.end + 1) {
                    return true;
                }
                return storedEnd != 0 && duplicateStarts != null && duplicateStarts.contains(key(part));
            }
            return syntheticParts != null && syntheticParts.contains(part);
        }

        void add(GraphPart part) {
            if (isRegular(part)) {
                if (endByStart[part.start] == 0) {
                    endByStart[part.start] = part.end + 1;
                } else if (endByStart[part.start] != part.end + 1) {
                    if (duplicateStarts == null) {
                        duplicateStarts = new HashSet<>();
                    }
                    duplicateStarts.add(key(part));
                }
            } else {
                if (syntheticParts == null) {
                    syntheticParts = new HashSet<>();
                }
                syntheticParts.add(part);
            }
        }

        private boolean isRegular(GraphPart part) {
            return part.start >= 0 && part.start < endByStart.length && part.end >= 0 && part.end < Integer.MAX_VALUE;
        }

        private static long key(GraphPart part) {
            return ((long) part.start << 32) ^ (part.end & 0xffffffffL);
        }
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
