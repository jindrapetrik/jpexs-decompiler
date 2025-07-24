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
import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.BranchStackResistant;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.IntegerValueItem;
import com.jpexs.decompiler.graph.model.IntegerValueTypeItem;
import com.jpexs.decompiler.graph.model.LabelItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.UniversalLoopItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.decompiler.graph.precontinues.GraphPrecontinueDetector;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Graph class. This is the main class where decompilation process is done. It
 * translates GraphSourceItems to GraphTargetItems.
 * <p>
 * Subclasses of Graph are used for different types of decompilation.
 *
 * @author JPEXS
 */
public class Graph {

    /**
     * Graph entry points
     */
    public List<GraphPart> heads;

    /**
     * Graph target dialect
     */
    protected GraphTargetDialect dialect;

    /**
     * Graph source code
     */
    protected GraphSource code;

    /**
     * Exceptions in the graph
     */
    private final List<GraphException> exceptions;

    /**
     * Graph startIp
     */
    protected int startIp = 0;

    /**
     * Debug flag to print all parts
     */
    private final boolean debugPrintAllParts = false;
    /**
     * Debug flag to print loop list
     */
    private final boolean debugPrintLoopList = false;
    /**
     * Debug flag to print getLoops
     */
    private final boolean debugGetLoops = false;
    /**
     * Debug flag to print decompilation progress (printGraph method)
     */
    private final boolean debugPrintGraph = false;
    /**
     * Debug flag to not process Ifs
     */
    protected boolean debugDoNotProcess = false;

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(Graph.class.getName());

    /**
     * Gets the graphSource
     *
     * @return GraphSource
     */
    public GraphSource getGraphCode() {
        return code;
    }

    /**
     * Gets sub-graphs
     *
     * @return Sub-graphs
     */
    public LinkedHashMap<String, Graph> getSubGraphs() {
        return new LinkedHashMap<>();
    }

    /**
     * Constructs a new Graph.
     *
     * @param dialect Dialect
     * @param code Graph source
     * @param exceptions Exceptions in the graph
     * @param startIp Start ip
     */
    public Graph(GraphTargetDialect dialect, GraphSource code, List<GraphException> exceptions, int startIp) {
        this.dialect = dialect;
        this.code = code;
        this.exceptions = exceptions;
        this.startIp = startIp;
    }

    /**
     * Initializes the graph.
     *
     * @param localData Local data
     * @throws InterruptedException On interrupt
     */
    public void init(BaseLocalData localData) throws InterruptedException {
        if (heads != null) {
            return;
        }
        heads = makeGraph(code, new ArrayList<>(), exceptions);
        int time = 1;
        List<GraphPart> ordered = new ArrayList<>();
        List<GraphPart> visited = new ArrayList<>();
        for (GraphPart head : heads) {
            time = head.setTime(time, ordered, visited);
            head.setNumblocks(1);
        }
    }

    /**
     * Calculates time of closing the node. The node is closed when all its
     * input edges are already visited (not counting back edges), then all its
     * output edges are processed.
     * <p>
     * This time is useful when sorting nodes according their occurrence in
     * getMostCommonPart method - used for switch detection
     *
     * @param loops Already calculated loops to get backedges from.
     */
    private void calculateClosedTime(List<Loop> loops) {
        ArrayDeque<GraphPart> openedNodes = new ArrayDeque<>();
        Set<GraphPart> closedNodes = new HashSet<>();
        Set<LevelMapEdge> visitedEdges = new HashSet<>();
        for (GraphPart h : heads) {
            for (GraphPart r : h.refs) {
                visitedEdges.add(new LevelMapEdge(r, h));
            }
        }
        for (Loop el : loops) {
            for (GraphPart be : el.backEdges) {
                visitedEdges.add(new LevelMapEdge(be, el.loopContinue));
            }
        }

        int closedTime = 1;

        for (GraphPart h : heads) {
            openedNodes.add(h);

            loopopened:
            while (!openedNodes.isEmpty()) {
                GraphPart part = openedNodes.remove();
                if (closedNodes.contains(part)) {
                    continue;
                }
                for (GraphPart r : part.refs) {
                    if (!visitedEdges.contains(new LevelMapEdge(r, part))) {
                        continue loopopened;
                    }
                }
                for (GraphPart n : part.nextParts) {
                    openedNodes.add(n);
                    visitedEdges.add(new LevelMapEdge(part, n));
                }
                closedNodes.add(part);
                part.closedTime = closedTime++;
                //System.err.println("part " + part + " closedTime: " + part.closedTime);
            }
        }

    }

    /**
     * Edge for calculating closed time.
     */
    private class LevelMapEdge {

        /**
         * Source part
         */
        public GraphPart from;

        /**
         * Target part
         */
        public GraphPart to;

        /**
         * Constructs a new LevelMapEdge
         *
         * @param from Source part
         * @param to Target part
         */
        public LevelMapEdge(GraphPart from, GraphPart to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Hash code
         *
         * @return Hash code
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(this.from);
            hash = 31 * hash + Objects.hashCode(this.to);
            return hash;
        }

        /**
         * Equals
         *
         * @param obj Object
         * @return True if equals
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LevelMapEdge other = (LevelMapEdge) obj;
            //use == comparison, not equals, as some parts may be equal
            // (the refs to throw targets have -1,-1 as start/end)
            if (this.from != other.from) {
                return false;
            }
            return this.to == other.to;
        }
    }

    /**
     * Gets graph exceptions.
     *
     * @return List of exceptions
     */
    public List<GraphException> getExceptions() {
        return exceptions;
    }

    /**
     * Populates all parts available from the part.
     *
     * @param part Source part
     * @param allParts Result
     */
    protected static void populateParts(GraphPart part, Set<GraphPart> allParts) {
        if (allParts.contains(part)) {
            return;
        }
        allParts.add(part);
        for (GraphPart p : part.nextParts) {
            populateParts(p, allParts);
        }
    }

    /**
     * Deep copies GraphPart.
     *
     * @param part Source part
     * @return Deep copy of the part
     */
    public GraphPart deepCopy(GraphPart part) {
        return deepCopy(part, new HashMap<>());
    }

    /**
     * Deep copies GraphPart.
     *
     * @param part Source part
     * @param copies Already copied parts
     * @return Deep copy of the part
     */
    private GraphPart deepCopy(GraphPart part, Map<GraphPart, GraphPart> copies) {
        GraphPart copy = copies.get(part);
        if (copy != null) {
            return copy;
        }

        copy = new GraphPart(part.start, part.end);
        copy.path = part.path;
        copies.put(part, copy);
        copy.nextParts = new ArrayList<>();
        for (int i = 0; i < part.nextParts.size(); i++) {
            copy.nextParts.add(deepCopy(part.nextParts.get(i), copies));
        }

        for (int i = 0; i < part.refs.size(); i++) {
            copy.refs.add(deepCopy(part.refs.get(i), copies));
        }

        return copy;
    }

    /**
     * Resets the graph.
     *
     * @param part Part to reset
     * @param visited Visited parts
     */
    public void resetGraph(GraphPart part, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }

        visited.add(part);
        int pos = 0;
        for (GraphPart p : part.nextParts) {
            if (!visited.contains(p)) {
                p.path = part.path.sub(pos, p.end);
            }

            resetGraph(p, visited);
            pos++;
        }
    }

    /**
     * Gets reachable parts from the part.
     *
     * @param localData Local data
     * @param part Source part
     * @param ret Result
     * @param loops Loops
     * @param throwStates Throw states
     */
    protected void getReachableParts(BaseLocalData localData, GraphPart part, LinkedHashSet<GraphPart> ret, List<Loop> loops, List<ThrowState> throwStates) {
        // use LinkedHashSet to preserve order
        getReachableParts(localData, part, ret, loops, throwStates, true);
    }

    /**
     * Gets reachable parts from the part.
     *
     * @param localData Local data
     * @param part Source part
     * @param ret Result
     * @param loops Loops
     * @param throwStates Throw states
     * @param first First call
     */
    private void getReachableParts(BaseLocalData localData, GraphPart part, LinkedHashSet<GraphPart> ret, List<Loop> loops, List<ThrowState> throwStates, boolean first) {
        // todo: honfika: why call with first = true parameter always?
        Stack<GraphPartQueue> stack = new Stack<>();
        GraphPartQueue queue = new GraphPartQueue();
        queue.add(part);
        stack.add(queue);
        stacknext:
        while (!stack.isEmpty()) {

            queue = stack.peek();
            if (!queue.isEmpty()) {
                part = queue.remove();
            } else if (queue.currentLoop != null) {
                Loop cLoop = queue.currentLoop;
                part = cLoop.loopBreak;
                queue.currentLoop = null;
                if (ret.contains(part)) {
                    continue;
                }

                ret.add(part);
                cLoop.reachableMark = 2;
            } else {
                stack.pop();
                continue;
            }

            for (Loop l : loops) {
                l.reachableMark = 0;
            }

            Loop currentLoop = null;
            for (Loop l : loops) {
                if ((l.phase == 1) || (l.reachableMark == 1)) {
                    if (l.loopContinue == part) {
                        continue stacknext;
                    }
                    if (l.loopBreak == part) {
                        continue stacknext;
                    }
                    if (l.loopPreContinue == part) {
                        continue stacknext;
                    }
                }
                if (l.reachableMark == 0) {
                    if (l.loopContinue == part) {
                        l.reachableMark = 1;
                        currentLoop = l;
                    }
                }
            }

            GraphPartQueue newParts = new GraphPartQueue();
            List<GraphPart> nextParts = new ArrayList<>(getNextParts(localData, part));

            for (ThrowState ts : throwStates) {
                if (ts.state != 1) {
                    if (ts.throwingParts.contains(part)) {
                        newParts.add(ts.targetPart);
                    }
                }
            }
            loopnext:
            for (GraphPart nextRaw : nextParts) {

                GraphPart next = checkPart(null, localData, part, nextRaw, null);
                if (next == null) {
                    continue;
                }
                if (next.start >= code.size()) {
                    continue;
                }

                for (Loop l : loops) {
                    if ((l.phase == 1) || (l.reachableMark == 1)) {
                        if (l.loopContinue == next) {
                            continue loopnext;
                        }
                        if (l.loopBreak == next) {
                            continue loopnext;
                        }
                        if (l.loopPreContinue == next) {
                            continue loopnext;
                        }
                    }

                }
                if (!ret.contains(next)) {
                    newParts.add(next);
                }
            }

            ret.addAll(newParts);
            if (currentLoop != null && currentLoop.loopBreak != null) {
                newParts.currentLoop = currentLoop;
            }

            if (!newParts.isEmpty() || newParts.currentLoop != null) {
                stack.add(newParts);
            }
        }
    }

    /**
     * Gets common successor of the next parts of the part.
     *
     * @param localData Local data
     * @param part Part
     * @param loops Loops
     * @param throwStates Throw states
     * @return Common successor
     * @throws InterruptedException On interrupt
     */
    public GraphPart getNextCommonPart(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        return getCommonPart(localData, part, getNextParts(localData, part), loops, throwStates);
    }

    /**
     * Gets common successor of the parts.
     * <p>
     * TODO: Make this faster!
     *
     * @param localData Local data
     * @param prev Previous part
     * @param parts Parts
     * @param loops Loops
     * @param throwStates Throw states
     * @return Common successor
     * @throws InterruptedException On interrupt
     */
    public GraphPart getCommonPart(BaseLocalData localData, GraphPart prev, List<GraphPart> parts, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        if (parts.isEmpty()) {
            return null;
        }

        List<GraphPart> loopContinues = new ArrayList<>();
        List<GraphPart> loopBreaks = new ArrayList<>();
        for (Loop l : loops) {
            if (l.phase == 1) {
                loopContinues.add(l.loopContinue);
                if (l.loopPreContinue != null) {
                    loopContinues.add(l.loopPreContinue);
                }
                if (l.loopBreak != null) {
                    loopBreaks.add(l.loopBreak);
                }
            }
        }

        for (GraphPart p : parts) {
            if (loopContinues.contains(p)) {
                break;
            }
            if (loopBreaks.contains(p)) {
                break;
            }
            boolean common = true;
            for (GraphPart q : parts) {
                if (q == p) {
                    continue;
                }
                if (!q.leadsTo(localData, this, code, p, loops, throwStates, false /*!!THROW*/)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }
        List<Set<GraphPart>> reachable = new ArrayList<>();
        for (GraphPart p : parts) {
            LinkedHashSet<GraphPart> r1 = new LinkedHashSet<>();
            getReachableParts(localData, p, r1, loops, throwStates);
            r1.add(p);
            reachable.add(r1);
        }
        Set<GraphPart> first = reachable.get(0);
        for (GraphPart p : first) {
            boolean common = true;
            for (Set<GraphPart> r : reachable) {
                if (!r.contains(p)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                if (loopContinues.contains(p)) {
                    return null;
                }
                if (loopBreaks.contains(p)) {
                    return null;
                }
                return p;
            }
        }
        return null;
    }

    /**
     * Gets common successor of most of the nextparts of the part.
     * <p>
     * This is used mostly in switch detection.
     *
     * @param localData Local data
     * @param parts Parts
     * @param loops Loops
     * @param throwStates Throw states
     * @param stopPart Stop part
     * @return Most common successor
     * @throws InterruptedException On interrupt
     */
    public GraphPart getMostCommonPart(BaseLocalData localData, List<GraphPart> parts, List<Loop> loops, List<ThrowState> throwStates, List<GraphPart> stopPart) throws InterruptedException {
        if (parts.isEmpty()) {
            return null;
        }

        Set<GraphPart> s = new HashSet<>(parts); //unique
        parts = new ArrayList<>(s); //make local copy

        List<GraphPart> loopContinues = new ArrayList<>();
        for (Loop l : loops) {
            if (l.phase == 1) {
                loopContinues.add(l.loopContinue);
                loopContinues.add(l.loopPreContinue);
            }
        }

        Map<GraphPart, Set<GraphPart>> reachable = new HashMap<>();
        Set<GraphPart> allReachable = new LinkedHashSet<>();
        for (GraphPart p : parts) {
            LinkedHashSet<GraphPart> r1 = new LinkedHashSet<>();
            getReachableParts(localData, p, r1, loops, throwStates);
            Set<GraphPart> r2 = new LinkedHashSet<>();
            r2.add(p);
            r2.addAll(r1);
            reachable.put(p, r2);
            allReachable.add(p);
            allReachable.addAll(r1);
        }
        Comparator<PartCommon> comparator = new Comparator<PartCommon>() {
            @Override
            public int compare(PartCommon o1, PartCommon o2) {
                int levelCompare = o2.level - o1.level;
                if (levelCompare == 0) {
                    try {
                        if (o1.part.leadsTo(localData, Graph.this, code, o2.part, loops, throwStates, false)) {
                            return -1;
                        }
                        if (o2.part.leadsTo(localData, Graph.this, code, o1.part, loops, throwStates, false)) {
                            return 1;
                        }
                        return 0;
                    } catch (InterruptedException ex) {
                        //ignore
                        return 0;
                    }
                    //return o1.part.discoveredTime - o2.part.discoveredTime;
                } else {
                    return levelCompare;
                }
            }
        };
        Set<PartCommon> commonSet = new TreeSet<>();

        for (GraphPart r : allReachable) {
            if (loopContinues.contains(r)) {
                continue;
            }
            boolean common = true;
            int commonLevel = 0;
            for (GraphPart p : parts) {
                if (p == r) {
                    commonLevel++;
                    continue;
                }
                if (!reachable.get(p).contains(r)) {
                    common = false;
                } else {
                    commonLevel++;
                }
            }
            if (common) {
                Stack<GraphPart> toProcess = new Stack<>();
                Set<GraphPart> visited = new HashSet<>();
                toProcess.addAll(parts);

                loopprocess:
                while (!toProcess.isEmpty()) {
                    GraphPart p = toProcess.pop();
                    if (p == r) {
                        continue;
                    }

                    if (loopContinues.contains(p)) {
                        continue;
                    }
                    if (visited.contains(p)) {
                        continue;
                    }
                    visited.add(p);
                    for (GraphPart n : p.nextParts) {
                        if (n == r) {
                            continue;
                        }

                        if (loopContinues.contains(n)) {
                            continue;
                        }
                        if (visited.contains(n)) {
                            continue;
                        }
                        if (!n.leadsTo(localData, this, code, r, loops, throwStates, false)) {
                            common = false;
                            break loopprocess;
                        }
                    }
                    toProcess.addAll(p.nextParts);
                }

                if (common) {
                    if (debugPrintLoopList) {
                        System.err.println("all time common: " + r);
                    }
                    return r;
                }
            }
            /*if (commonLevel > maxCommonLevel) {                
                maxCommonPart = r;
                maxCommonLevel = commonLevel;
                commonSet.add(e)
            }*/
            commonSet.add(new PartCommon(r, commonLevel));
        }

        /*Set<GraphPart> partsLeadingToStopPart = new LinkedHashSet<>();
        if (stopPart != null) {
            for (GraphPart p : parts) {
                for (GraphPart sp : stopPart) {
                    if (sp == p || p.leadsTo(localData, this, code, sp, new ArrayList<Loop>(), throwStates, false)) {
                        partsLeadingToStopPart.add(p);
                    }
                }
            }
        }
 
        if (debugPrintLoopList) {
            System.err.println("commonset:");
            for (PartCommon pc : commonSet) {
                System.err.println("- " + pc);
            }

            System.err.println("parts:");
            for (GraphPart p : parts) {
                System.err.println("- " + p);
            }

            if (stopPart != null) {
                System.err.println("stopparts:");
                for (GraphPart p : stopPart) {
                    System.err.println("- " + p);
                }
            }

            System.err.println("partsLeadingToStopPart:");
            for (GraphPart p : partsLeadingToStopPart) {
                System.err.println("- " + p);
            }           
        }*/
        loopc:
        for (PartCommon pc : commonSet) {
            /*for (GraphPart p : partsLeadingToStopPart) {
                if (p != pc.part && !p.leadsTo(localData, this, code, pc.part, loops, throwStates, false)) {
                    if (debugPrintLoopList) {
                        System.err.println("ignoring " + pc.part + ", " + p + " does not lead to it");
                    }
                    continue loopc;
                }
            }*/
            if (pc.level <= 1) {
                return null;
            }
            if (debugPrintLoopList) {
                StringBuilder sb = new StringBuilder();
                for (GraphPart p : parts) {
                    sb.append(" ");
                    sb.append(p.toString());
                }
                System.err.println("most common part of" + sb.toString() + " is " + pc.part);
            }
            return pc.part;
        }

        return null;
    }

    /**
     * Common part
     */
    private class PartCommon implements Comparable<PartCommon> {

        /**
         * Part
         */
        public GraphPart part;
        /**
         * Level - how many parts are common
         */
        public int level;

        /**
         * Constructs a new PartCommon
         *
         * @param part Part
         * @param level Level
         */
        public PartCommon(GraphPart part, int level) {
            this.part = part;
            this.level = level;
        }

        /**
         * Compares to another PartCommon
         *
         * @param o Other PartCommon
         * @return Comparison result
         */
        @Override
        public int compareTo(PartCommon o) {
            int ret = o.level - level;
            if (ret == 0) {
                ret = part.closedTime - o.part.closedTime;

                if (ret == 0) { //some nodes may be split in half and thus have same closedTime - like in try..catch
                    return part.start - o.part.start;
                }
            }
            return ret;
        }

        /**
         * To string
         *
         * @return String representation
         */
        @Override
        public String toString() {
            return "" + part.toString() + " (level=" + level + ")";
        }

        /**
         * Hash code
         *
         * @return Hash code
         */
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + Objects.hashCode(this.part);
            hash = 71 * hash + this.level;
            return hash;
        }

        /**
         * Equals
         *
         * @param obj Object
         * @return True if equals
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PartCommon other = (PartCommon) obj;
            if (this.level != other.level) {
                return false;
            }
            return Objects.equals(this.part, other.part);
        }

    }

    /**
     * Method called after populating all parts.
     *
     * @param allParts All parts
     */
    protected void afterPopulateAllParts(Set<GraphPart> allParts) {

    }

    /**
     * Gets throw states. Override this method to get throw states.
     *
     * @param localData Local data
     * @param allParts All parts
     * @return List of ThrowStates
     */
    protected List<ThrowState> getThrowStates(BaseLocalData localData, Set<GraphPart> allParts) {
        return new ArrayList<>();
    }

    /**
     * Translates the graph - decompiles.
     *
     * @param localData Local data
     * @param staticOperation Unused
     * @param path Path
     * @return List of GraphTargetItems
     * @throws InterruptedException On interrupt
     */
    public List<GraphTargetItem> translate(BaseLocalData localData, int staticOperation, String path) throws InterruptedException {

        Set<GraphPart> allParts = new HashSet<>();
        for (GraphPart head : heads) {
            populateParts(head, allParts);
        }
        afterPopulateAllParts(allParts);
        if (debugPrintAllParts) {
            System.err.println("parts:");
            for (GraphPart p : allParts) {
                System.err.print(p);
                if (!getNextParts(localData, p).isEmpty()) {
                    System.err.print(", next: ");
                }
                boolean first = true;
                for (GraphPart n : getNextParts(localData, p)) {
                    if (!first) {
                        System.err.print(",");
                    }
                    System.err.print(n);
                    first = false;
                }
                System.err.println("");
            }
            System.err.println("/parts");
        }
        TranslateStack stack = new TranslateStack(path);
        List<ThrowState> throwStates = getThrowStates(localData, allParts);
        beforeGetLoops(localData, path, allParts, throwStates);
        List<Loop> loops = new ArrayList<>();

        getLoops(localData, heads.get(0), loops, throwStates, null);

        afterGetLoops(localData, path, allParts);

        //TODO: Make getPrecontinues faster
        getBackEdges(localData, loops, throwStates);
        calculateClosedTime(loops);

        new GraphPrecontinueDetector().detectPrecontinues(heads, allParts, loops, throwStates);
        if (debugPrintLoopList) {
            System.err.println("<loops>");
            for (Loop el : loops) {
                System.err.println(el);
            }
            System.err.println("</loops>");
        }
        /*System.err.println("<loopspre>");
         for (Loop el : loops) {
         System.err.println(el);
         }
         System.err.println("</loopspre>");//*/
        List<GotoItem> gotos = new ArrayList<>();

        List<GraphTargetItem> ret = printGraph(gotos, new HashMap<>(), new HashMap<>(), new HashSet<>(), localData, stack, allParts, null, heads.get(0), null, null, loops, throwStates, staticOperation, path);

        if (localData.secondPassData == null) {
            SecondPassData secondPassData = prepareSecondPass(ret);
            if (secondPassData == null) {
                if (!localData.allSwitchParts.isEmpty()) {
                    secondPassData = new SecondPassData();
                    secondPassData.allSwitchParts = localData.allSwitchParts;
                    throw new SecondPassException(secondPassData);
                }
            } else {
                if (secondPassData.getClass() == SecondPassData.class && localData.allSwitchParts.isEmpty()) {
                    //nothing
                } else {
                    secondPassData.allSwitchParts = localData.allSwitchParts;
                    throw new SecondPassException(secondPassData);
                }
            }
        }

        processIfGotos2(new ArrayList<>(), gotos, ret, ret);
        processIfGotos(gotos, ret, ret);
        processScriptEnd(ret);

        Map<String, Integer> usages = new HashMap<>();
        Map<String, GotoItem> lastUsage = new HashMap<>();
        for (GotoItem gi : gotos) {
            if (!usages.containsKey(gi.labelName)) {
                usages.put(gi.labelName, 0);
            }
            usages.put(gi.labelName, usages.get(gi.labelName) + 1);
            lastUsage.put(gi.labelName, gi);
        }
        for (String labelName : usages.keySet()) {
            logger.log(Level.FINE, "usage - {0}: {1}", new Object[]{labelName, usages.get(labelName)});
            if (usages.get(labelName) == 1) {
                lastUsage.get(labelName).labelName = null;
            }
        }
        expandGotos(ret);
        processIfs(ret);
        propagateBreaks(ret);
        finalProcessStack(stack, ret, path);
        makeAllCommands(ret, stack);
        finalProcessAll(null, ret, 0, getFinalData(localData, loops, throwStates), path);
        //fixSwitchEnds(ret);
        return ret;
    }

    /*
    private void fixSwitchEnds(List<GraphTargetItem> items) {
        for (GraphTargetItem item : items) {
            if (item instanceof SwitchItem) {
                fixSwitchEnd((SwitchItem) item);
            }
            if (item instanceof Block) {
                Block blk = (Block) item;
                for (List<GraphTargetItem> sub : blk.getSubs()) {
                    fixSwitchEnds(sub);
                }
            }
        }
    }
    */
    
    /**
     * This is needed to avoid loop/switch identifiers in AS1/2. AS3 supports
     * them, but AS1/2 not.
     *
     * <pre>
     * loop1: switch(a) { //has loop identifier
     *          case 1:
     *              trace("1");
     *              break;
     *          case 2: //last case
     *              trace("2");
     *              switch(b) { //last command is switch case 3:
     *                  case 4:
     *                      trace("4");
     *                      break loop1; //breaks parent loop
     *                  case 5:
     *                      trace("5");
     *                      break loop1;
     *                  case 6:
     *                      trace("6");
     *              }
     *          }
     *
     * ==>
     *
     * switch(a) {
     *      case 1:
     *          trace("1");
     *          break;
     *      case 2:
     *          trace("2");
     *          switch(b) {
     *              case 3:
     *              case 4:
     *                  trace("4");
     *                  break;
     *              case 5:
     *                  trace("5");
     *                  break;
     *              case 6:
     *                  trace("6");
     *          }
     * }
     *
     * </pre> It also does similar thing to loops and continues.
     *
     *
     *
     *
     * @param list Items
     */
    protected void propagateBreaks(List<GraphTargetItem> list) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                Block bl = (Block) item;
                for (List<GraphTargetItem> subList : bl.getSubs()) {
                    propagateBreaks(subList);
                }
            }
            if (item instanceof SwitchItem) {
                SwitchItem sw = (SwitchItem) item;
                if (sw.caseCommands.isEmpty()) {
                    continue;
                }
                List<GraphTargetItem> lastCase = sw.caseCommands.get(sw.caseCommands.size() - 1);
                if (lastCase.isEmpty()) {
                    continue;
                }

                //Replace breaks in lastCommands loops
                for (int h = 0; h < sw.caseCommands.size(); h++) {
                    List<GraphTargetItem> com = sw.caseCommands.get(h);
                    List<GraphTargetItem> com2 = com;
                    if (com.isEmpty()) {
                        continue;
                    }
                    boolean isLastCase = h == sw.caseCommands.size() - 1;
                    int last = com.size() - 1;
                    boolean hasBreak = ((com.get(last) instanceof BreakItem) && (((BreakItem) com.get(last)).loopId == sw.loop.id));

                    if (!isLastCase && !hasBreak) {
                        continue;
                    }
                    com2 = new ArrayList<>(com);
                    if (hasBreak) {
                        com2.remove(com2.size() - 1);
                    }

                    List<List<GraphTargetItem>> todos = new ArrayList<>();
                    todos.add(com2);

                    for (int j = 0; j < todos.size(); j++) {
                        List<GraphTargetItem> currentList = todos.get(j);
                        if (currentList.isEmpty()) {
                            continue;
                        }
                        GraphTargetItem lastCommand = currentList.get(currentList.size() - 1);
                        if (lastCommand instanceof LoopItem) {
                            if (lastCommand instanceof SwitchItem) {
                                SwitchItem innerSwitch = (SwitchItem) lastCommand;
                                List<List<GraphTargetItem>> subs = innerSwitch.getSubs();
                                for (int k = 0; k < subs.size(); k++) {
                                    List<GraphTargetItem> caseCommands = subs.get(k);
                                    if (caseCommands.isEmpty()) {
                                        continue;
                                    }
                                    lastCommand = caseCommands.get(caseCommands.size() - 1);
                                    if ((lastCommand instanceof BreakItem) || (lastCommand instanceof ContinueItem) || (lastCommand instanceof ExitItem)) {
                                        changeBreakToBreak(caseCommands, sw.loop.id, innerSwitch.loop.id);
                                    } else if (k == subs.size() - 1) {
                                        changeBreakToBreak(caseCommands, sw.loop.id, innerSwitch.loop.id);
                                    }
                                }
                                fixSwitchEnd(innerSwitch);
                            } else {
                                LoopItem innerLoop = (LoopItem) lastCommand;
                                changeBreakToBreak(innerLoop.getBaseBodyCommands(), sw.loop.id, innerLoop.loop.id);
                                //Detect While
                                if (innerLoop instanceof UniversalLoopItem) {
                                    List<GraphTargetItem> body = innerLoop.getBaseBodyCommands();
                                    if (!body.isEmpty()) {
                                        if (body.get(0) instanceof IfItem) {
                                            IfItem ifi = (IfItem) body.get(0);
                                            if (ifi.onFalse.isEmpty() && ifi.onTrue.size() == 1) {
                                                if (ifi.onTrue.get(0) instanceof BreakItem) {
                                                    BreakItem br = (BreakItem) ifi.onTrue.get(0);
                                                    if (br.loopId == innerLoop.loop.id) {
                                                        List<GraphTargetItem> expr = new ArrayList<>();
                                                        expr.add(ifi.expression.invert(null));
                                                        body.remove(0);
                                                        WhileItem wh = new WhileItem(dialect, innerLoop.getSrc(), innerLoop.getLineStartItem(), innerLoop.loop, expr, body);
                                                        if (currentList == com2) {
                                                            com.set(com.size() - 1 - (hasBreak ? 1 : 0), wh);
                                                        } else {
                                                            currentList.set(currentList.size() - 1, wh);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (lastCommand instanceof Block) {
                            Block blk = (Block) lastCommand;
                            List<List<GraphTargetItem>> newTodos = new ArrayList<>(blk.getSubs());
                            if (!newTodos.isEmpty() && lastCommand instanceof SwitchItem) {
                                List<List<GraphTargetItem>> newTodos2 = new ArrayList<>();
                                newTodos2.add(newTodos.get(newTodos.size() - 1));
                                newTodos = newTodos2;
                            }
                            todos.addAll(newTodos);
                        }
                    }
                }

                // Remove breaks of switch in last commands of last case:
                List<List<GraphTargetItem>> todos = new ArrayList<>();
                todos.add(lastCase);

                for (int j = 0; j < todos.size(); j++) {
                    List<GraphTargetItem> currentList = todos.get(j);
                    if (currentList.isEmpty()) {
                        continue;
                    }
                    GraphTargetItem lastCommand = currentList.get(currentList.size() - 1);
                    if (lastCommand instanceof BreakItem) {
                        BreakItem bi = (BreakItem) lastCommand;
                        if (bi.loopId == sw.loop.id) {
                            currentList.remove(currentList.size() - 1);
                        }
                    } else if (lastCommand instanceof LoopItem) {
                        //ignore
                    } else if (lastCommand instanceof Block) {
                        Block blk = (Block) lastCommand;
                        List<List<GraphTargetItem>> newTodos = new ArrayList<>(blk.getSubs());
                        if (!newTodos.isEmpty() && lastCommand instanceof SwitchItem) {
                            //empty
                        } else {
                            todos.addAll(newTodos);
                        }
                    }
                }
                //-----------------------

                if (lastCase.isEmpty() || !(lastCase.get(lastCase.size() - 1) instanceof SwitchItem)) {
                    continue;
                }
                SwitchItem swInner = (SwitchItem) lastCase.get(lastCase.size() - 1);

                List<BreakItem> breaks = swInner.getBreaks();
                for (BreakItem br : breaks) {
                    if (br.loopId == sw.loop.id) {
                        br.loopId = swInner.loop.id;
                    }
                }
            }
            if (item instanceof LoopItem) {
                LoopItem li = (LoopItem) item;
                if (li.hasBaseBody()) {
                    List<GraphTargetItem> body = li.getBaseBodyCommands();
                    if (!body.isEmpty()) {

                        List<List<GraphTargetItem>> todos = new ArrayList<>();
                        todos.add(body);

                        for (int j = 0; j < todos.size(); j++) {
                            List<GraphTargetItem> currentList = todos.get(j);
                            if (currentList.isEmpty()) {
                                continue;
                            }
                            GraphTargetItem lastCommand = currentList.get(currentList.size() - 1);
                            if (lastCommand instanceof LoopItem) {
                                LoopItem innerLoop = (LoopItem) lastCommand;
                                Block blk = (Block) lastCommand;
                                changeContinueToBreak(blk, li.loop.id, innerLoop.loop.id);
                            } else if (lastCommand instanceof Block) {
                                Block blk = (Block) lastCommand;
                                List<List<GraphTargetItem>> newTodos = new ArrayList<>(blk.getSubs());
                                if (!newTodos.isEmpty() && lastCommand instanceof SwitchItem) {
                                    List<List<GraphTargetItem>> newTodos2 = new ArrayList<>();
                                    newTodos2.add(newTodos.get(newTodos.size() - 1));
                                    newTodos = newTodos2;
                                }
                                todos.addAll(newTodos);
                            }
                        }
                    }
                    if (li instanceof ForItem) {
                        ForItem fi = (ForItem) li;
                        List<ContinueItem> continues = fi.getContinues();
                        boolean hasContinue = false;
                        for (ContinueItem ci : continues) {
                            if (ci.loopId == fi.loop.id) {
                                hasContinue = true;
                                break;
                            }
                        }
                        
                        //No continue, change for back to while
                        if (!hasContinue) {
                            List<GraphTargetItem> expr = new ArrayList<>();
                            expr.add(fi.expression);
                            WhileItem wh = new WhileItem(dialect, fi.getSrc(), fi.getLineStartItem(), fi.loop, expr, fi.commands);
                            wh.commands.addAll(fi.finalCommands);
                            list.set(i, wh);
                            for (int j = 0; j < fi.firstCommands.size(); j++) {
                                list.add(i, fi.firstCommands.get(j));
                                i++;
                            }                            
                        }
                    }
                }
            }
        }
    }

    private void changeContinueToBreak(Block blk, long continueLoopId, long breakLoopId) {
        for (List<GraphTargetItem> subItems : blk.getSubs()) {
            changeContinueToBreak(subItems, continueLoopId, breakLoopId);
        }
        if (blk instanceof SwitchItem) {
            fixSwitchEnd((SwitchItem) blk);
        }
    }

    private void changeContinueToBreak(List<GraphTargetItem> items, long continueLoopId, long breakLoopId) {
        for (int i = 0; i < items.size(); i++) {
            GraphTargetItem item = items.get(i);
            if (item instanceof Block) {
                Block blk = (Block) item;
                changeContinueToBreak(blk, continueLoopId, breakLoopId);
            }
            if (item instanceof ContinueItem) {
                ContinueItem ci = (ContinueItem) item;
                if (ci.loopId == continueLoopId) {
                    items.set(i, new BreakItem(dialect, ci.getSrc(), ci.getLineStartItem(), breakLoopId));
                }
            }
        }
    }

    private void changeBreakToBreak(List<GraphTargetItem> items, long breakLoopIdFrom, long breakLoopIdTo) {
        for (int i = 0; i < items.size(); i++) {
            GraphTargetItem item = items.get(i);
            if (item instanceof Block) {
                Block blk = (Block) item;
                for (List<GraphTargetItem> subItems : blk.getSubs()) {
                    changeBreakToBreak(subItems, breakLoopIdFrom, breakLoopIdTo);
                }
            }
            if (item instanceof BreakItem) {
                BreakItem ci = (BreakItem) item;
                if (ci.loopId == breakLoopIdFrom) {
                    ci.loopId = breakLoopIdTo;
                }
            }
        }
    }

    /**
     * Prepares second pass data. Can return null when no second pass will
     * happen. Override this method to prepare second pass data.
     *
     * @param list List of GraphTargetItems
     * @return Second pass data or null
     */
    protected SecondPassData prepareSecondPass(List<GraphTargetItem> list) {
        return null;
    }

    /**
     * Process various items. Override this method to process items.
     *
     * @param list List of GraphTargetItems
     * @param lastLoopId Last loop id
     */
    protected void processOther(List<GraphTargetItem> list, long lastLoopId) {

    }

    /**
     * Process switches.
     *
     * @param list List of GraphTargetItems
     */
    protected final void processSwitches(List<GraphTargetItem> list) {
        processSwitches(list, -1);
    }

    /*
    
    while(something){
        switch(xx){
            case 1:
                trace("1");
                continue;
            case 2:
                trace("2");
                continue;
            case 3:
                break;
            default:
                continue;
        }
        item
    }
    
    =>
    
    while(something){
        switch(xx){
            case 1:
                trace("1");
                break;
            case 2:
                trace("2");
                break;
            case 3:
                item
                break;
            default:
                break;
        }        
    }
    
    This will fix precontinue handler which detects multiple continues
     */
    /**
     * Process switches.
     *
     * @param list List of GraphTargetItems
     * @param lastLoopId Last loop id
     */
    protected void processSwitches(List<GraphTargetItem> list, long lastLoopId) {
        loopi:
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof SwitchItem) {
                SwitchItem swi = (SwitchItem) item;

                Set<GraphTargetItem> allItems = swi.getAllSubItemsRecursively();
                int breakCount = 0;
                for (GraphTargetItem it : allItems) {
                    if (it instanceof BreakItem) {
                        BreakItem br = (BreakItem) it;
                        if (br.loopId == swi.loop.id) {
                            breakCount++;
                            if (breakCount > 2) {
                                continue loopi;
                            }
                        }
                    }
                }
                if (!swi.caseCommands.isEmpty()) {
                    List<GraphTargetItem> lastCommands = swi.caseCommands.get(swi.caseCommands.size() - 1);
                    if (lastCommands.isEmpty() && breakCount > 0) {
                        continue loopi;
                    }

                    if (breakCount > 0 && !(lastCommands.get(lastCommands.size() - 1) instanceof ContinueItem)
                            && !(lastCommands.get(lastCommands.size() - 1) instanceof ExitItem)) {
                        continue loopi;
                    }
                }

                int breakCaseIndex = -1;
                for (int c = 0; c < swi.caseCommands.size(); c++) {
                    List<GraphTargetItem> commands = swi.caseCommands.get(c);
                    if (!commands.isEmpty()) {
                        if (commands.get(commands.size() - 1) instanceof BreakItem) {
                            if (commands.size() == 1) {
                                BreakItem br = (BreakItem) commands.get(commands.size() - 1);
                                if (br.loopId == swi.loop.id) {
                                    breakCaseIndex = c;
                                    break;
                                }
                            }
                        }
                    }
                    if (c == swi.caseCommands.size() - 1) {
                        if (commands.isEmpty()) {
                            breakCaseIndex = c;
                            break;
                        }
                        if (!(commands.get(commands.size() - 1) instanceof ContinueItem)
                                && !(commands.get(commands.size() - 1) instanceof ExitItem)) {
                            breakCaseIndex = c;
                            break;
                        }
                    }
                }

                if (breakCount == 2) {
                    if (breakCaseIndex <= 0) {
                        continue loopi;
                    }
                    if (swi.caseCommands.get(breakCaseIndex - 1).isEmpty()) {
                        continue loopi;
                    }
                    GraphTargetItem ti = swi.caseCommands.get(breakCaseIndex - 1).get(swi.caseCommands.get(breakCaseIndex - 1).size() - 1);
                    if (!(ti instanceof BreakItem)) {
                        continue loopi;
                    }
                    BreakItem br = (BreakItem) ti;
                    if (br.loopId != swi.loop.id) {
                        continue loopi;
                    }
                    swi.caseCommands.get(breakCaseIndex - 1).remove(swi.caseCommands.get(breakCaseIndex - 1).size() - 1);
                }

                boolean hasContinues = false;
                for (int c = 0; c < swi.caseCommands.size(); c++) {
                    List<GraphTargetItem> commands = swi.caseCommands.get(c);
                    if (!commands.isEmpty()) {
                        if (commands.get(commands.size() - 1) instanceof ContinueItem) {
                            ContinueItem cnt = (ContinueItem) commands.get(commands.size() - 1);
                            if (cnt.loopId == lastLoopId) {
                                hasContinues = true;
                                commands.set(commands.size() - 1, new BreakItem(dialect, null, null, swi.loop.id));
                            }
                        }
                    }
                }
                if (hasContinues && breakCaseIndex > -1 && i + 1 < list.size()) {
                    List<GraphTargetItem> toAdd = new ArrayList<>();
                    boolean continueOnEnd = list.get(list.size() - 1) instanceof ContinueItem;
                    for (int j = i + 1; j < list.size() - (continueOnEnd ? 1 : 0); j++) {
                        toAdd.add(list.remove(i + 1));
                    }
                    List<GraphTargetItem> targetCommands = swi.caseCommands.get(breakCaseIndex);
                    if (!targetCommands.isEmpty() && (targetCommands.get(targetCommands.size() - 1) instanceof BreakItem)) {
                        targetCommands.remove(targetCommands.size() - 1);
                    }
                    targetCommands.addAll(toAdd);
                    if (toAdd.isEmpty() || (!((toAdd.get(toAdd.size() - 1) instanceof ExitItem) || (toAdd.get(toAdd.size() - 1) instanceof BreakItem)))) {
                        targetCommands.add(new BreakItem(dialect, null, null, swi.loop.id));
                    }
                }
                fixSwitchEnd(swi);
            } else if (item instanceof IfItem) {
                processSwitches(((IfItem) item).onTrue, lastLoopId);
                processSwitches(((IfItem) item).onFalse, lastLoopId);
            }
        }
    }

    /**
     * Gets data for final process. Override this method to provide data for
     * final process.
     *
     * @param localData Local data
     * @param loops Loops
     * @param throwStates Throw states
     * @return Final process local data
     */
    protected FinalProcessLocalData getFinalData(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) {
        return new FinalProcessLocalData(loops);
    }

    /**
     * Method called before getting loops. Override this method to provide
     * custom behavior.
     *
     * @param localData Local data
     * @param path Path
     * @param allParts All parts
     * @param throwStates Throw states
     * @throws InterruptedException On interrupt
     */
    protected void beforeGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts, List<ThrowState> throwStates) throws InterruptedException {

    }

    /**
     * Method called after getting loops. Override this method to provide custom
     * behavior.
     *
     * @param localData Local data
     * @param path Path
     * @param allParts All parts
     * @throws InterruptedException On interrupt
     */
    protected void afterGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts) throws InterruptedException {

    }

    /**
     * Checks whether part is empty. Override this method to provide custom
     * behavior.
     *
     * @param part Part
     * @return True if part is empty
     */
    protected boolean isPartEmpty(GraphPart part) {
        return false;
    }

    /**
     * Converts path to string
     *
     * @param list Collection of objects
     * @return String representation of the path
     */
    private String pathToString(Collection<? extends Object> list) {
        List<String> strs = new ArrayList<>();
        for (Object p : list) {
            strs.add(p.toString());
        }
        return "[" + String.join(", ", strs) + "]";
    }

    /**
     * Gets unique part list.
     *
     * @param list List of parts
     * @return Unique list of parts
     */
    private List<GraphPart> getUniquePartList(List<GraphPart> list) {
        List<GraphPart> result = new ArrayList<>();
        for (GraphPart p : list) {
            if (!result.contains(p)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Gets list of unique references of part without going through throw edges.
     *
     * @param part Part
     * @param throwEdges Throw edges
     * @return List of unique references
     */
    private List<GraphPart> getUniqueRefsNoThrow(GraphPart part, Set<GraphPartEdge> throwEdges) {
        List<GraphPart> result = new ArrayList<>();
        for (GraphPart r : part.refs) {
            GraphPartEdge edge = new GraphPartEdge(r, part);
            if (!throwEdges.contains(edge)) {
                result.add(r);
            }
        }

        return getUniquePartList(result);
    }

    /**
     * Gets of loop backedges
     *
     * @param localData Local data
     * @param loops Loops
     * @param throwStates Throw states
     * @throws InterruptedException On interrupt
     */
    private void getBackEdges(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        clearLoops(loops);
        for (Loop el : loops) {
            el.backEdges.clear();
            Set<GraphPart> uniqueRefs = new HashSet<>(el.loopContinue.refs);
            loopr:
            for (GraphPart r : uniqueRefs) {
                for (Loop el2 : loops) {
                    if (el2.phase == 1) {
                        if (el2.loopContinue == r) {
                            continue loopr;
                        }
                    }
                }
                if (el.loopContinue.leadsTo(localData, this, code, r, loops, throwStates, true)) {
                    el.backEdges.add(r);
                }
            }
            el.phase = 1;
        }
        clearLoops(loops);
    }

    /**
     * Final process stack. Override this method to provide custom behavior.
     *
     * @param stack Translate stack
     * @param output Output
     * @param path Path
     */
    public void finalProcessStack(TranslateStack stack, List<GraphTargetItem> output, String path) {
    }

    /**
     * Final process all.
     *
     * @param parent Parent item
     * @param list List of GraphTargetItems
     * @param level Level
     * @param localData Local data
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    private void finalProcessAll(GraphTargetItem parent, List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {
        if (debugDoNotProcess) {
            return;
        }
        finalProcess(parent, list, level, localData, path);
        for (GraphTargetItem item : list) {
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    finalProcessAll(item, sub, level + 1, localData, path);
                }
            }
        }
        finalProcessAfter(list, level, localData, path);
    }

    /**
     * Processes sub blocks. TODO: make this clearer what it does
     *
     * @param b Block
     * @param replacement If not null, then if last item of block is PushItem,
     * then it will be replaced with this item
     * @return If all blocks ends with PushItem
     */
    private boolean processSubBlk(Block b, GraphTargetItem replacement) {
        boolean allSubPush = true;
        boolean atleastOne = false;
        for (List<GraphTargetItem> sub : b.getSubs()) {
            if (!sub.isEmpty()) {
                int lastPos = sub.size() - 1;

                GraphTargetItem last = sub.get(sub.size() - 1);
                GraphTargetItem br = null;

                if ((last instanceof BreakItem) && (sub.size() >= 2)) {
                    br = last;
                    lastPos--;
                    last = sub.get(lastPos);
                }
                if (last instanceof Block) {
                    if (!processSubBlk((Block) last, replacement)) {
                        allSubPush = false;
                    } else {
                        atleastOne = true;
                    }
                } else if (last instanceof PushItem) {
                    if (replacement != null) {
                        GraphTargetItem e2 = (((GraphTargetItem) replacement).clone());
                        e2.value = last.value;
                        sub.set(lastPos, e2);
                        if (br != null) {
                            sub.remove(sub.size() - 1);
                        }
                    }
                    atleastOne = true;
                } else if (!(last instanceof ExitItem)) {
                    allSubPush = false;
                }
            }
        }
        return allSubPush && atleastOne;
    }

    /**
     * Final process after. Override this method to provide custom behavior.
     *
     * @param list List of GraphTargetItems
     * @param level Level
     * @param localData Local data
     * @param path Path
     */
    protected void finalProcessAfter(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) {
        if (list.size() >= 2) {
            if (list.get(list.size() - 1) instanceof ExitItem) {
                ExitItem e = (ExitItem) list.get(list.size() - 1);
                if (list.get(list.size() - 1).value instanceof PopItem) {
                    if (list.get(list.size() - 2) instanceof Block) {
                        Block b = (Block) list.get(list.size() - 2);
                        if (processSubBlk(b, (GraphTargetItem) e)) {
                            list.remove(list.size() - 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Final process. Override this method to provide custom behavior.
     *
     * @param parent Paren item
     * @param list List of GraphTargetItems
     * @param level Level
     * @param localData Local data
     * @param path Path
     * @throws InterruptedException On interrupt
     */
    protected void finalProcess(GraphTargetItem parent, List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {

        //For detection based on debug line information
        boolean[] toDelete = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }

            GraphTargetItem itemI = list.get(i);
            if (itemI instanceof ForItem) {
                ForItem fori = (ForItem) itemI;
                int exprLine = fori.getLine();
                if (exprLine > 0) {
                    List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                    for (int j = i - 1; j >= 0; j--) {
                        if (list.get(j).getLine() == exprLine && !(list.get(j) instanceof LoopItem /*to avoid recursion and StackOverflow*/)) {
                            forFirstCommands.add(0, list.get(j));
                            toDelete[j] = true;
                            break; //do not allow more than 1
                        } else {
                            break;
                        }
                    }
                    fori.firstCommands.addAll(0, forFirstCommands);
                }
            }

            if (itemI instanceof WhileItem) {
                WhileItem whi = (WhileItem) itemI;
                int whileExprLine = whi.getLine();
                if (whileExprLine > 0) {
                    List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                    List<GraphTargetItem> forFinalCommands = new ArrayList<>();

                    for (int j = i - 1; j >= 0; j--) {
                        GraphTargetItem itemJ = list.get(j);
                        if (itemJ.getLine() == whileExprLine && !(itemJ instanceof LoopItem /*to avoid recursion and StackOverflow*/)) {
                            forFirstCommands.add(0, itemJ);
                            toDelete[j] = true;
                        } else {
                            break;
                        }
                    }
                    for (int j = whi.commands.size() - 1; j >= 0; j--) {
                        if (whi.commands.get(j).getLine() == whileExprLine && !(whi.commands.get(j) instanceof LoopItem /*to avoid recursion and StackOverflow*/)) {
                            forFinalCommands.add(0, whi.commands.remove(j));
                        } else {
                            break;
                        }
                    }
                    if (!forFirstCommands.isEmpty() || !forFinalCommands.isEmpty()) {
                        //Do not allow more than 1 first/final command, since it can be obfuscated
                        if (forFirstCommands.size() > 1 || forFinalCommands.size() > 1) {
                            //put it back
                            for (int k = 0; k < forFirstCommands.size(); k++) {
                                toDelete[i - 1 - k] = false;
                            }
                            whi.commands.addAll(forFinalCommands); //put it back
                        } else if (whi.commands.isEmpty() && forFirstCommands.isEmpty()) {
                            //it would be for(;expr;commands) {}  which looks better as while(expr){commands}
                            whi.commands.addAll(forFinalCommands); //put it back
                        } else {
                            GraphTargetItem lastExpr = whi.expression.remove(whi.expression.size() - 1);
                            forFirstCommands.addAll(whi.expression);
                            list.set(i, new ForItem(dialect, whi.getSrc(), whi.getLineStartItem(), whi.loop, forFirstCommands, lastExpr, forFinalCommands, whi.commands));
                        }
                    }
                }
            }
        }

        for (int i = toDelete.length - 1; i >= 0; i--) {
            if (toDelete[i]) {
                list.remove(i);
            }
        }
    }

    /**
     * Expands gotos.
     *
     * @param list List of GraphTargetItems
     */
    private void expandGotos(List<GraphTargetItem> list) {
        if (!list.isEmpty() && (list.get(list.size() - 1) instanceof GotoItem)) {
            GotoItem gi = (GotoItem) list.get(list.size() - 1);
            if (gi.targetCommands != null) {
                list.remove(gi);
                if (gi.labelName != null) {
                    list.add(new LabelItem(dialect, null, gi.lineStartItem, gi.labelName));
                }
                list.addAll(gi.targetCommands);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    expandGotos(sub);
                }
            }
        }
    }

    /**
     * Processes if gotos.
     *
     * @param alreadyProcessedBlocks Already processed blocks
     * @param allGotos All gotos
     * @param list List of GraphTargetItems
     * @param rootList Root list
     */
    private void processIfGotos2(List<List<GraphTargetItem>> alreadyProcessedBlocks, List<GotoItem> allGotos, List<GraphTargetItem> list, List<GraphTargetItem> rootList) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfGotos2(alreadyProcessedBlocks, allGotos, sub, rootList);
                }
            }
            if (item instanceof GotoItem) {
                GotoItem gi = (GotoItem) item;
                loopblk:
                for (List<GraphTargetItem> blk : alreadyProcessedBlocks) {
                    for (int j = 0; j < blk.size(); j++) {
                        GraphTargetItem ti = blk.get(j);
                        if (ti instanceof LabelItem) {
                            LabelItem label = (LabelItem) ti;
                            if (label.labelName.equals(gi.labelName)) {
                                if (blk.get(blk.size() - 1) instanceof ExitItem) {
                                    int siz = blk.size();
                                    for (int k = 0; k < siz - j; k++) {
                                        list.add(i + 1 + k, blk.remove(j));
                                    }
                                    blk.add(j, list.remove(i));
                                }
                                break loopblk;
                            }
                        }
                    }
                }
            }
        }
        alreadyProcessedBlocks.add(list);
    }

    /**
     * Processes if gotos.
     * <p>
     * if (xxx) { y ; goto a } else { z ; goto a }
     * <p>
     * =>
     * <p>
     * if (xxx) { y } else { z } goto a
     *
     * @param allGotos All gotos
     * @param list List of GraphTargetItems
     * @param rootList Root list
     */
    private void processIfGotos(List<GotoItem> allGotos, List<GraphTargetItem> list, List<GraphTargetItem> rootList) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfGotos(allGotos, sub, rootList);
                }
            }
            if (item instanceof IfItem) {
                IfItem ii = (IfItem) item;
                if (!ii.onTrue.isEmpty() && !ii.onFalse.isEmpty()) {
                    if (ii.onTrue.get(ii.onTrue.size() - 1) instanceof GotoItem) {
                        if (ii.onFalse.get(ii.onFalse.size() - 1) instanceof GotoItem) {
                            for (int j = i + 1; j < list.size(); j++) {
                                list.remove(i + 1);
                            }
                        }
                    }
                }
                if (!ii.onTrue.isEmpty() && !ii.onFalse.isEmpty()) {
                    if (ii.onTrue.get(ii.onTrue.size() - 1) instanceof GotoItem) {
                        if (ii.onFalse.get(ii.onFalse.size() - 1) instanceof GotoItem) {
                            GotoItem gotoOnTrue = (GotoItem) ii.onTrue.get(ii.onTrue.size() - 1);
                            GotoItem gotoOnFalse = (GotoItem) ii.onFalse.get(ii.onFalse.size() - 1);
                            if (gotoOnTrue.labelName.equals(gotoOnFalse.labelName)) {
                                String labelOnTrue = gotoOnTrue.labelName;
                                String labelOnFalse = gotoOnFalse.labelName;
                                if (labelOnTrue != null && labelOnFalse != null) {
                                    if (labelOnTrue.equals(labelOnFalse)) {
                                        GotoItem gotoMerged;
                                        GotoItem gotoRemoved;
                                        if (gotoOnTrue.targetCommands != null) {
                                            gotoMerged = gotoOnTrue;
                                            gotoRemoved = gotoOnFalse;
                                        } else {
                                            gotoMerged = gotoOnFalse;
                                            gotoRemoved = gotoOnTrue;
                                        }
                                        ii.onTrue.remove(ii.onTrue.size() - 1);
                                        ii.onFalse.remove(ii.onFalse.size() - 1);
                                        list.add(i + 1, gotoMerged);
                                        allGotos.remove(gotoRemoved);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ii.onTrue.isEmpty() && ii.onFalse.isEmpty()) {
                    if (ii.onTrue.get(ii.onTrue.size() - 1) instanceof GotoItem) {
                        GotoItem g1 = (GotoItem) ii.onTrue.get(ii.onTrue.size() - 1);
                        if (i + 1 < list.size()) {
                            if (list.get(i + 1) instanceof GotoItem) {

                                GotoItem g2 = (GotoItem) list.get(i + 1);
                                if (g1.labelName.equals(g2.labelName)) {
                                    ii.onTrue.remove(ii.onTrue.size() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processScriptEnd(List<GraphTargetItem> ret) {
        if (!ret.isEmpty()) {
            if (ret.get(ret.size() - 1) instanceof ScriptEndItem) {
                ret.remove(ret.size() - 1);
                processScriptEnd(ret);
                return;
            }
            if (ret.get(ret.size() - 1) instanceof Block) {
                Block blk = (Block) ret.get(ret.size() - 1);
                if (blk instanceof SwitchItem) {
                    return;
                }

                for (List<GraphTargetItem> sub : blk.getSubs()) {
                    processScriptEnd(sub);
                }
            }
        }
    }

    /**
     * Processes ifs.
     *
     * @param list List of GraphTargetItems
     */
    protected final void processIfs(List<GraphTargetItem> list) {
        if (debugDoNotProcess) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if ((item instanceof LoopItem) && (item instanceof Block)) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfs(sub);
                    checkContinueAtTheEnd(sub, ((LoopItem) item).loop);
                }
            } else if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfs(sub);
                }
            }
            if (item instanceof IfItem) {
                IfItem ifi = (IfItem) item;
                List<GraphTargetItem> onTrue = ifi.onTrue;
                List<GraphTargetItem> onFalse = ifi.onFalse;
                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                        if (onFalse.get(onFalse.size() - 1) instanceof ContinueItem) {
                            if (((ContinueItem) onTrue.get(onTrue.size() - 1)).loopId == ((ContinueItem) onFalse.get(onFalse.size() - 1)).loopId) {
                                onTrue.remove(onTrue.size() - 1);
                                list.add(i + 1, onFalse.remove(onFalse.size() - 1));
                            }
                        }
                    }
                }

                if (i < list.size() - 1) {
                    if (list.get(i + 1) instanceof ContinueItem) {
                        if ((!onTrue.isEmpty()) && (onFalse.isEmpty())) {
                            if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                                if (((ContinueItem) onTrue.get(onTrue.size() - 1)).loopId == ((ContinueItem) list.get(i + 1)).loopId) {
                                    onTrue.remove(onTrue.size() - 1);
                                }
                            }
                        }
                    }

                }

                /*
                if (xx) {
                    A;
                    continue
                } else {
                    B;
                    break/exit/continue;
                }
                
                =>
                
                if (xx) {
                    A;
                } else {
                    B;
                    break/exit/continue;
                }
                continue;
                
                 */
                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    if ((onFalse.get(onFalse.size() - 1) instanceof ExitItem) || (onFalse.get(onFalse.size() - 1) instanceof BreakItem)) {
                        if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                            list.add(i + 1, onTrue.remove(onTrue.size() - 1));
                        }
                    }
                }

                /*
                if (xx) {
                    A;
                    break/exit/continue;
                } else {
                    B;                    
                }
                
                =>
                
                if (xx) {
                    A;
                    break/exit/continue;
                }
                B;
                
                 */
                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    GraphTargetItem last = onTrue.get(onTrue.size() - 1);
                    if ((last instanceof ExitItem) || (last instanceof ContinueItem) || (last instanceof BreakItem)) {
                        list.addAll(i + 1, onFalse);
                        onFalse.clear();
                    }
                }

                //Prefer continue/return/throw/break in onTrue rather than onFalse
                if (!onFalse.isEmpty()
                        && ((onFalse.get(onFalse.size() - 1) instanceof BreakItem)
                        || (onFalse.get(onFalse.size() - 1) instanceof ExitItem)
                        || (onFalse.get(onFalse.size() - 1) instanceof ContinueItem))
                        && !(onFalse.get(onFalse.size() - 1) instanceof ScriptEndItem)
                        && (onTrue.isEmpty() || !((onTrue.get(onTrue.size() - 1) instanceof BreakItem)
                        || (onTrue.get(onTrue.size() - 1) instanceof ExitItem)
                        || (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)))) {
                    ifi.expression = ifi.expression.invert(null);
                    ifi.onTrue = onFalse;
                    ifi.onFalse = new ArrayList<>();
                    list.addAll(i + 1, onTrue);
                    onFalse = ifi.onFalse;
                    onTrue = ifi.onTrue;
                }

                if (i < list.size() - 1) {
                    if ((list.get(i + 1) instanceof BreakItem) && onFalse.isEmpty()) {
                        if (!onTrue.isEmpty() && (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)) {
                            ifi.expression = ifi.expression.invert(null);
                            list.addAll(i + 2, ifi.onTrue);
                            ifi.onTrue.clear();
                            ifi.onTrue.add(list.remove(i + 1));
                        }
                    }
                }

                //Switch break onFalse and continue onTrue
                if (i < list.size() - 1) {
                    if ((list.get(list.size() - 1) instanceof ExitItem) || (list.get(list.size() - 1) instanceof BreakItem)) {
                        if (onFalse.isEmpty() && !onTrue.isEmpty() && (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)) {
                            ifi.expression = ifi.expression.invert(null);
                            List<GraphTargetItem> onTrueItems = new ArrayList<>();
                            for (int j = i; j < list.size(); j++) {
                                onTrueItems.add(list.remove(i + 1));
                            }
                            list.addAll(i + 1, ifi.onTrue);
                            ifi.onTrue.clear();
                            ifi.onTrue.addAll(onTrueItems);
                        }
                    }
                }

                /*
                if (xx) {
                    A;
                    break/continue x;
                }
                break/continue x;
                
                =>
                
                if (xx) {
                    A;
                }
                break/continue x;
                
                 */
                if (i + 1 < list.size()) {
                    GraphTargetItem nextItem = list.get(i + 1);
                    if (onFalse.isEmpty() && !onTrue.isEmpty()) {
                        if ((onTrue.get(onTrue.size() - 1) instanceof ContinueItem) && (nextItem instanceof ContinueItem)) {
                            ContinueItem cntOnTrue = (ContinueItem) onTrue.get(onTrue.size() - 1);
                            ContinueItem cntNext = (ContinueItem) nextItem;
                            if (cntOnTrue.loopId == cntNext.loopId) {
                                onTrue.remove(onTrue.size() - 1);
                            }
                        }
                        if ((onTrue.get(onTrue.size() - 1) instanceof BreakItem) && (nextItem instanceof BreakItem)) {
                            BreakItem brkOnTrue = (BreakItem) onTrue.get(onTrue.size() - 1);
                            BreakItem brkNext = (BreakItem) nextItem;
                            if (brkOnTrue.loopId == brkNext.loopId) {
                                onTrue.remove(onTrue.size() - 1);
                            }
                        }
                    }
                }
            }
        }

        //Same continues in onTrue and onFalse gets continue on parent level
    }

    /**
     * Checks continue at the end of block and remove it when its from nearest
     * loop.
     *
     * @param commands List of GraphTargetItems
     * @param loop Loop
     */
    private void checkContinueAtTheEnd(List<GraphTargetItem> commands, Loop loop) {
        if (!commands.isEmpty()) {
            int i = commands.size() - 1;
            for (; i >= 0; i--) {
                if (commands.get(i) instanceof ContinueItem) {
                    continue;
                }
                if (commands.get(i) instanceof BreakItem) {
                    continue;
                }
                break;
            }
            if (i < commands.size() - 1) {
                for (int k = i + 2; k < commands.size(); k++) {
                    commands.remove(k);
                }
            }
            if (commands.get(commands.size() - 1) instanceof ContinueItem) {
                if (((ContinueItem) commands.get(commands.size() - 1)).loopId == loop.id) {
                    commands.remove(commands.size() - 1);
                }
            }
        }
    }

    /**
     * Checks whether list of items is empty.
     *
     * @param output List of GraphTargetItems
     * @return True if list of items is empty
     */
    protected final boolean isEmpty(List<GraphTargetItem> output) {
        if (output.isEmpty()) {
            return true;
        }
        if (output.size() == 1) {
            if (output.get(0) instanceof MarkItem) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check before decompiling next section. Override this method to provide
     * custom behavior.
     *
     * @param currentRet Current return
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param code Code
     * @param localData Local data
     * @param allParts All parts
     * @param stack Stack
     * @param parent Parent part
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param output Output
     * @param currentLoop Current loop
     * @param staticOperation Unused
     * @param path Path
     * @return List of GraphTargetItems to replace current output and stop
     * further processing. Null to continue.
     * @throws InterruptedException On interrupt
     */
    protected List<GraphTargetItem> check(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
        return null;
    }

    /**
     * Check of Part passing output. Allows you to switch part for another. If
     * not overridden, then it calls checkPart.
     *
     * @param output List of GraphTargetItems
     * @param stack Translate stack
     * @param localData Local data
     * @param prev Previous part
     * @param part Part
     * @param allParts All parts
     * @return Return same part to continue processing or return another part to
     * continue to other part. Or return null to stop.
     */
    protected GraphPart checkPartWithOutput(List<GraphTargetItem> output, TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        return checkPart(stack, localData, prev, part, allParts);
    }

    /**
     * Check of part. Allows you to switch part for another.
     *
     * @param stack Translate stack
     * @param localData Local data
     * @param prev Previous part
     * @param part Part
     * @param allParts All parts
     * @return Return same part to continue processing or return another part to
     * continue to other part. Or return null to stop.
     */
    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        return part;
    }

    /**
     * Translates part and get its stack.
     *
     * @param localData Local data
     * @param part Part
     * @param stack Translate stack
     * @param staticOperation Unused
     * @return Top of the stack
     * @throws InterruptedException On interrupt
     * @throws GraphPartChangeException On graph part change
     */
    //@SuppressWarnings("unchecked")
    protected final GraphTargetItem translatePartGetStack(BaseLocalData localData, GraphPart part, TranslateStack stack, int staticOperation) throws InterruptedException, GraphPartChangeException {
        stack = (TranslateStack) stack.clone();
        translatePart(new ArrayList<>(), localData, part, stack, staticOperation, null);
        return stack.pop();
    }
    
    /**
     * Translates part and get its stack with output
     *
     * @param localData Local data
     * @param part Part
     * @param stack Translate stack
     * @param staticOperation Unused
     * @return Top of the stack
     * @throws InterruptedException On interrupt
     * @throws GraphPartChangeException On graph part change
     */
    //@SuppressWarnings("unchecked")
    protected final GraphTargetItem translatePartGetStack(BaseLocalData localData, GraphPart part, TranslateStack stack, int staticOperation, List<GraphTargetItem> output) throws InterruptedException, GraphPartChangeException {
        stack = (TranslateStack) stack.clone();
        output.clear();
        translatePart(output, localData, part, stack, staticOperation, null);
        return stack.pop();
    }

    /**
     * Translates part.
     *
     * @param output Output
     * @param localData Local data
     * @param part Part
     * @param stack Translate stack
     * @param staticOperation Unused
     * @param path Path
     * @return List of GraphTargetItems
     * @throws InterruptedException On interrupt
     * @throws GraphPartChangeException On graph part change
     */
    protected final void translatePart(List<GraphTargetItem> output, BaseLocalData localData, GraphPart part, TranslateStack stack, int staticOperation, String path) throws InterruptedException, GraphPartChangeException {
        List<GraphPart> sub = part.getSubParts();
        int end;
        for (GraphPart p : sub) {
            if (p.end == -1) {
                p.end = code.size() - 1;
            }
            if (p.start == code.size()) {
                continue;
            } else if (p.end == code.size()) {
                p.end--;
            }
            end = p.end;
            int start = p.start;
            code.translatePart(output, this, part, localData, stack, start, end, staticOperation, path);
        }
    }

    /**
     * Checks for Continue and Break items at current part.
     *
     * @param output List of GraphTargetItems
     * @param part Part
     * @param stopPart Stop part
     * @param loops Loops
     * @param throwStates Throw states
     * @return Continue or Break item or null
     */
    protected final GraphTargetItem checkLoop(List<GraphTargetItem> output, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<ThrowState> throwStates) {
        if (stopPart.contains(part)) {
            return null;
        }

        GraphSourceItem firstIns = null;
        if (part != null) {
            if (part.start >= 0 && part.start < code.size()) {
                firstIns = code.get(part.start);
            }
        }

        for (Loop l : loops) {
            if (l.phase == 2) {
                continue;
            }
            if (l.loopContinue == part) {
                return (new ContinueItem(dialect, null, firstIns, l.id));
            }
            if (l.loopBreak == part) {
                return (new BreakItem(dialect, null, firstIns, l.id));
            }
        }
        return null;
    }

    /**
     * Check loop. Can be overridden to provide custom behavior.
     *
     * @param output List of GraphTargetItems
     * @param loopItem Loop item
     * @param localData Local data
     * @param loops Loops
     * @param throwStates Throw states
     * @param stack Translate stack
     * @return Return loopItem to replace current loop. Return null to continue.
     */
    protected GraphTargetItem checkLoop(List<GraphTargetItem> output, LoopItem loopItem, BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates, TranslateStack stack) {
        return loopItem;
    }

    /**
     * Sets loop phase to 0 on loops.
     *
     * @param loops List of loops
     */
    private void clearLoops(List<Loop> loops) {
        for (Loop l : loops) {
            l.phase = 0;
        }
    }

    /**
     * Sets state to 0 on throw states.
     *
     * @param throwStates List of throw states
     */
    private void clearThrowStates(List<ThrowState> throwStates) {
        for (ThrowState ts : throwStates) {
            ts.state = 0;
        }
    }

    /**
     * Loop detection.
     *
     * @param localData Local data
     * @param part Part
     * @param loops List of loops
     * @param throwStates List of throw states
     * @param stopPart Stop part
     * @throws InterruptedException On interrupt
     */
    private void getLoops(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates, List<GraphPart> stopPart) throws InterruptedException {
        clearLoops(loops);
        clearThrowStates(throwStates);
        getLoopsWalk(localData, part, loops, throwStates, stopPart, true, new ArrayList<>(), 0);
        clearLoops(loops);
        clearThrowStates(throwStates);
    }

    /**
     * Checks whether a part can be a break candidate. Can be overridden to
     * provide custom behavior.
     *
     * @param localData Local data
     * @param part Part
     * @param throwStates List of throw states
     * @return True if part can be a break candidate
     */
    protected boolean canBeBreakCandidate(BaseLocalData localData, GraphPart part, List<ThrowState> throwStates) {
        return true;
    }

    /**
     * Check part in get loops walk. Can be overridden to provide custom
     * behavior.
     *
     * @param part Graph part
     */
    protected void checkGetLoopsPart(GraphPart part) {

    }

    /**
     * Finds parts outside try statement
     *
     * @param ts Throw state
     * @param part Graph part
     * @param ret List of Graph parts
     * @param visited Set of Graph parts
     */
    private void findPartsOutsideTry(ThrowState ts, GraphPart part, List<GraphPart> ret, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        if (!ts.throwingParts.contains(part)) {
            ret.add(part);
            return;
        }
        for (GraphPart n : part.nextParts) {
            findPartsOutsideTry(ts, n, ret, visited);
        }
    }

    /**
     * Walks parts to detect loops.
     *
     * @param localData Local data
     * @param part Graph part
     * @param loops List of loops
     * @param throwStates List of throw states
     * @param stopPart Stop part
     * @param first First
     * @param visited Set of Graph parts
     * @param level Level
     * @throws InterruptedException On interrupt
     */
    private void getLoopsWalk(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates, List<GraphPart> stopPart, boolean first, List<GraphPart> visited, int level) throws InterruptedException {

        loopwalk:
        while (true) {
            if (part == null) {
                return;
            }

            part = checkPart(null, localData, null, part, null);
            if (part == null) {
                return;
            }
            if (!visited.contains(part)) {
                visited.add(part);
            }
            checkGetLoopsPart(part);

            if (debugGetLoops) {
                System.err.println("getloops: " + part);
            }
            //List<GraphPart> loopContinues = getLoopsContinues(loops);
            Loop lastP1 = null;
            for (Loop el : loops) {
                if ((el.phase == 1) && el.loopBreak == null) { //break not found yet
                    if (el.loopContinue != part) {
                        lastP1 = el;

                    } else {
                        lastP1 = null;
                    }

                }
            }

            boolean canBeCandidate = true;
            if (lastP1 != null) {
                for (ThrowState ts : throwStates) {
                    if (!ts.catchParts.contains(lastP1.loopContinue) && ts.catchParts.contains(part)) {
                        canBeCandidate = false;
                        break;
                    }
                }
            }

            boolean isLoop = false;
            Loop currentLoop = null;
            try {
                if (lastP1 != null && canBeCandidate && canBeBreakCandidate(localData, part, throwStates)) {
                    if (lastP1.breakCandidates.contains(part)) {
                        lastP1.breakCandidates.add(part);
                        lastP1.breakCandidatesLevels.add(level);
                        return;
                    } else {
                        List<Loop> loops2 = new ArrayList<>(loops);
                        loops2.remove(lastP1);

                        for (ThrowState ts : throwStates) {
                            if (ts.throwingParts.contains(part)) {
                                ts.state *= -1;
                            }
                        }

                        try {
                            if (!part.leadsTo(localData, this, code, lastP1.loopContinue, loops2, throwStates, true)) {
                                if (lastP1.breakCandidatesLocked == 0) {
                                    if (debugGetLoops) {
                                        System.err.println("added breakCandidate " + part + " to " + lastP1);
                                    }

                                    lastP1.breakCandidates.add(part);
                                    lastP1.breakCandidatesLevels.add(level);
                                    return;
                                }
                            }
                        } finally {
                            for (ThrowState ts : throwStates) {
                                if (ts.throwingParts.contains(part)) {
                                    ts.state *= -1;
                                }
                            }

                        }
                    }
                }

                for (Loop el : loops) {
                    if (el.loopContinue == part) {
                        return;
                    }
                }

                if (stopPart != null && stopPart.contains(part)) {
                    return;
                }
                part.level = level;

                isLoop = part.leadsTo(localData, this, code, part, loops, throwStates, true);
                currentLoop = null;
                if (isLoop) {
                    currentLoop = new Loop(loops.size(), part, null);
                    currentLoop.phase = 1;
                    loops.add(currentLoop);
                }

            } finally {
                for (ThrowState ts : throwStates) {
                    if (ts.throwingParts.contains(part)) {
                        ts.state = 1;
                    }
                }
            }

            for (ThrowState ts : throwStates) {
                if (ts.throwingParts.contains(part)) {
                    GraphPart t = ts.targetPart;
                    if (!visited.contains(t)) {
                        getLoopsWalk(localData, t, loops, throwStates, stopPart, false, visited, level);
                    }
                }
            }

            if (part.nextParts.size() == 2 && !partIsSwitch(part)) {

                List<GraphPart> nps;

                nps = part.nextParts;
                GraphPart next = getCommonPart(localData, part, nps, loops, throwStates);
                List<GraphPart> stopPart2 = stopPart == null ? new ArrayList<>() : new ArrayList<>(stopPart);
                if (next != null) {
                    stopPart2.add(next);
                }
                if (next != nps.get(0)) {
                    getLoopsWalk(localData, nps.get(0), loops, throwStates, stopPart2, false, visited, level + 1);
                }
                if (next != nps.get(1)) {
                    getLoopsWalk(localData, nps.get(1), loops, throwStates, stopPart2, false, visited, level + 1);
                }
                if (next != null) {
                    getLoopsWalk(localData, next, loops, throwStates, stopPart, false, visited, level);
                }
            } else if (part.nextParts.size() > 2 || partIsSwitch(part)) {
                GraphPart next = getMostCommonPart(localData, part.nextParts, loops, throwStates, stopPart);

                for (GraphPart p : part.nextParts) {
                    List<GraphPart> stopPart2 = stopPart == null ? new ArrayList<>() : new ArrayList<>(stopPart);
                    if (next != null) {
                        stopPart2.add(next);
                    }
                    for (GraphPart p2 : part.nextParts) {
                        if (p2 == p) {
                            continue;
                        }
                        if (!stopPart2.contains(p2)) {
                            stopPart2.add(p2);
                        }
                    }
                    if (next != p) {
                        getLoopsWalk(localData, p, loops, throwStates, stopPart2, false, visited, level + 1);
                    }
                }
                if (next != null) {
                    getLoopsWalk(localData, next, loops, throwStates, stopPart, false, visited, level);
                }
            } else if (part.nextParts.size() == 1) {

                if (!isLoop || currentLoop == null) {
                    part = part.nextParts.get(0);
                    first = false;
                    continue loopwalk; //to avoid recursion
                } else {
                    getLoopsWalk(localData, part.nextParts.get(0), loops, throwStates, stopPart, false, visited, level);
                }
            }

            if (isLoop && currentLoop != null) {
                GraphPart found;

                for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                    GraphPart ch = checkPart(null, localData, null, currentLoop.breakCandidates.get(i), null);
                    if (ch == null) {
                        currentLoop.breakCandidates.remove(i);
                        currentLoop.breakCandidatesLevels.remove(i);
                        i--;
                    }
                }
                if (debugGetLoops) {
                    System.err.println("loop " + currentLoop + " break candidates:");
                    for (GraphPart cand : currentLoop.breakCandidates) {
                        System.err.println("- " + cand);
                    }
                }

                List<Integer> contThrowStates = new ArrayList<>();

                for (ThrowState ts : throwStates) {
                    if (ts.throwingParts.contains(currentLoop.loopContinue)) {
                        contThrowStates.add(ts.exceptionId);
                    }
                }

                Map<GraphPart, Integer> removed = new HashMap<>();

                loopcand:
                for (int c = 0; c < currentLoop.breakCandidates.size(); c++) {
                    GraphPart cand = currentLoop.breakCandidates.get(c);
                    List<Integer> candThrowStates = new ArrayList<>();
                    for (ThrowState ts : throwStates) {
                        if (ts.throwingParts.contains(cand) && ts.startPart != cand) {
                            if (contThrowStates.equals(candThrowStates)) {
                                //adding new ts
                                //this means breakcandidate is in nested try
                                if (debugGetLoops) {
                                    System.err.println("candidate " + cand + " is in inner try, getting outside parts");
                                }
                                List<GraphPart> outsideTry = new ArrayList<>();
                                findPartsOutsideTry(ts, cand, outsideTry, new HashSet<>());

                                for (int j = outsideTry.size() - 1; j >= 0; j--) {
                                    if (!canBeBreakCandidate(localData, outsideTry.get(j), throwStates)) {
                                        outsideTry.remove(j);
                                    }
                                }
                                if (debugGetLoops) {
                                    for (GraphPart op : outsideTry) {
                                        System.err.println("- outsidepart " + op);
                                    }
                                }
                                int bcLevel = currentLoop.breakCandidatesLevels.get(c);
                                currentLoop.breakCandidates.remove(c);

                                currentLoop.breakCandidates.addAll(c, outsideTry);
                                currentLoop.breakCandidatesLevels.remove(c);

                                removed.put(cand, bcLevel);
                                for (int j = 0; j < outsideTry.size(); j++) {
                                    currentLoop.breakCandidatesLevels.add(c, bcLevel);
                                }

                                c--;
                                continue loopcand;
                            }
                            candThrowStates.add(ts.exceptionId);
                        }
                    }

                }

                do {
                    found = null;

                    loopcand:
                    for (int c1 = 0; c1 < currentLoop.breakCandidates.size(); c1++) {
                        GraphPart cand = currentLoop.breakCandidates.get(c1);
                        for (int c2 = 0; c2 < currentLoop.breakCandidates.size(); c2++) {
                            GraphPart cand2 = currentLoop.breakCandidates.get(c2);
                            if (cand == cand2) {
                                continue;
                            }
                            if (cand.leadsTo(localData, this, code, cand2, loops, throwStates, true)) {

                                int curLevl = currentLoop.breakCandidatesLevels.get(c1);
                                int curLev2 = currentLoop.breakCandidatesLevels.get(c2);
                                /*
                            found = cand;
                            int lev2 = currentLoop.breakCandidatesLevels.get(c2);
                            currentLoop.breakCandidates.set(c1, cand2);
                            currentLoop.breakCandidatesLevels.set(c1, lev2);*/
                                int lev1 = Integer.MAX_VALUE;
                                int lev2 = Integer.MAX_VALUE;
                                for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                                    if (currentLoop.breakCandidates.get(i) == cand) {
                                        if (currentLoop.breakCandidatesLevels.get(i) < lev1) {
                                            lev1 = currentLoop.breakCandidatesLevels.get(i);
                                        }
                                    }
                                    if (currentLoop.breakCandidates.get(i) == cand2) {
                                        if (currentLoop.breakCandidatesLevels.get(i) < lev2) {
                                            lev2 = currentLoop.breakCandidatesLevels.get(i);
                                        }
                                    }
                                }
                                //
                                GraphPart other;
                                int curLev;
                                if (lev1 <= lev2) {
                                    found = cand2;
                                    other = cand;
                                    curLev = curLevl;
                                } else {
                                    found = cand;
                                    other = cand2;
                                    curLev = curLev2;
                                }

                                currentLoop.breakCandidates.add(other);
                                currentLoop.breakCandidatesLevels.add(curLev);

                                break loopcand;
                            }
                        }
                    }
                    if (found != null) {
                        int maxlevel = 0;
                        while (currentLoop.breakCandidates.contains(found)) {
                            int ind = currentLoop.breakCandidates.indexOf(found);
                            currentLoop.breakCandidates.remove(ind);
                            int lev = currentLoop.breakCandidatesLevels.remove(ind);
                            if (lev > maxlevel) {
                                maxlevel = lev;
                            }
                        }
                        if (removed.containsKey(found)) {
                            if (removed.get(found) > maxlevel) {
                                maxlevel = removed.get(found);
                            }
                        }
                        removed.put(found, maxlevel);
                    }
                } while ((found != null) && (currentLoop.breakCandidates.size() > 1));

                Map<GraphPart, Integer> count = new HashMap<>();
                GraphPart winner = null;
                int winnerCount = 0;
                int winnerNumBlock = Integer.MAX_VALUE;

                Set<GraphPart> bannedCandidates = new HashSet<>();
                if (localData.secondPassData != null) {
                    bannedCandidates = localData.secondPassData.allSwitchParts;
                }

                if (debugPrintLoopList) {
                    System.err.println("bannedCandidates:");
                    for (GraphPart p : bannedCandidates) {
                        System.err.println("- " + p);
                    }
                }

                for (GraphPart cand : currentLoop.breakCandidates) {
                    if (bannedCandidates.contains(cand)) {
                        if (debugPrintLoopList) {
                            System.err.println("cand " + cand + " is banned");
                        }
                        continue;
                    }
                    if (!count.containsKey(cand)) {
                        count.put(cand, 0);
                    }
                    count.put(cand, count.get(cand) + 1);
                    boolean otherBreakCandidate = false;
                    for (Loop el : loops) {
                        if (el == currentLoop) {
                            continue;
                        }
                        if (el.breakCandidates.contains(cand)) {
                            otherBreakCandidate = true;
                            break;
                        }
                    }
                    if (otherBreakCandidate) {
                        //empty
                    } else if (count.get(cand) > winnerCount) {
                        winnerCount = count.get(cand);
                        winner = cand;
                    } else if (count.get(cand) == winnerCount && winner != null) {
                        if (cand.path.length() < winner.path.length()) {
                            winner = cand;
                        }
                    }
                }
                for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                    GraphPart cand = currentLoop.breakCandidates.get(i);
                    if (cand != winner) {
                        int lev = currentLoop.breakCandidatesLevels.get(i);
                        if (removed.containsKey(cand)) {
                            if (removed.get(cand) > lev) {
                                lev = removed.get(cand);
                            }
                        }
                        removed.put(cand, lev);
                    }
                }
                currentLoop.loopBreak = winner;
                currentLoop.breakCandidates.clear();
                currentLoop.phase = 2;
                boolean start = false;
                for (int l = 0; l < loops.size(); l++) {
                    Loop el = loops.get(l);
                    if (start) {
                        el.phase = 1;
                    }
                    if (el == currentLoop) {
                        start = true;
                    }
                }
                List<GraphPart> removedVisited = new ArrayList<>();
                for (GraphPart r : removed.keySet()) {
                    if (removedVisited.contains(r)) {
                        continue;
                    }
                    getLoopsWalk(localData, r, loops, throwStates, stopPart, false, visited, removed.get(r));
                    removedVisited.add(r);
                }
                start = false;
                for (int l = 0; l < loops.size(); l++) {
                    Loop el = loops.get(l);
                    if (el == currentLoop) {
                        start = true;
                    }
                    if (start) {
                        el.phase = 2;
                    }
                }
                getLoopsWalk(localData, currentLoop.loopBreak, loops, throwStates, stopPart, false, visited, level);
            }
            break;
        }
    }

    /**
     * Gets all Continue commands in sub blocks.
     *
     * @param commands List of GraphTargetItems
     * @param result Result
     * @param loopId Loop id
     */
    private void getContinuesCommands(List<GraphTargetItem> commands, List<List<GraphTargetItem>> result, long loopId) {
        for (GraphTargetItem ti : commands) {
            if (ti instanceof ContinueItem) {
                ContinueItem ci = (ContinueItem) ti;
                if (ci.loopId == loopId) {
                    result.add(commands);
                }
            }
            if (ti instanceof Block) {
                Block bl = (Block) ti;
                for (List<GraphTargetItem> subCommands : bl.getSubs()) {
                    getContinuesCommands(subCommands, result, loopId);
                }
            }
        }
    }

    /**
     * Get next parts of a part. Can be overridden to provide custom behavior.
     *
     * @param localData Local data
     * @param part Part
     * @return List of GraphParts
     */
    protected List<GraphPart> getNextParts(BaseLocalData localData, GraphPart part) {
        return part.nextParts;
    }

    /**
     * Check before processing with output.
     *
     * @param currentRet Current return
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param code Code
     * @param localData Local data
     * @param allParts All parts
     * @param stack Stack
     * @param parent Parent part
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param currentLoop Current loop
     * @param staticOperation Unused
     * @param path Path
     * @param recursionLevel Recursion level
     * @return True to stop processing. False to continue.
     * @throws InterruptedException On interrupt
     */
    protected boolean checkPartOutput(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, Loop currentLoop, int staticOperation, String path, int recursionLevel) throws InterruptedException {
        return false;
    }

    /**
     * Checks whether part is loop continue, break or precontinue.
     *
     * @param part Graph part
     * @param loops List of loops
     * @param throwStates List of throw states
     * @return True if part is loop continue, break or precontinue
     */
    protected boolean partIsLoopContBrePre(GraphPart part, List<Loop> loops, List<ThrowState> throwStates) {
        for (Loop el : loops) {
            if (el.phase == 1) {
                if (el.loopBreak == part) {
                    return true;
                }
                if (el.loopContinue == part) {
                    return true;
                }
                if (el.loopPreContinue == part) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether part can be continue of a loop. Defaults to true. Override
     * this method to provide custom behavior.
     *
     * @param localData Local data
     * @param part Graph part
     * @param loops List of loops
     * @param throwStates List of throw states
     * @return True if part can be continue of a loop
     */
    protected boolean canHandleLoop(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates) {
        return true;
    }

    /**
     * Checks whether part can be checked over visited parts list. Defaults to
     * true. Can be overridden to provide custom behavior.
     *
     * @param localData Local data
     * @param part Graph part
     * @return True if part can be checked over visited parts list
     */
    protected boolean canHandleVisited(BaseLocalData localData, GraphPart part) {
        return true;
    }

    /**
     * Checks whether the list of items can be converted to comma separated
     * list.
     *
     * @param list List of GraphTargetItems
     * @return True if the list of items can be converted to comma separated
     * list
     */
    protected final boolean canBeCommaised(List<GraphTargetItem> list) {
        for (GraphTargetItem item : list) {
            if (item instanceof Block) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets if expression from stack. Can be overridden for custom handling
     *
     * @param localData Local data
     * @param stack Stack
     * @param output Output
     * @return Expression
     */
    protected GraphTargetItem getIfExpression(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output) {
        return stack.pop();
    }

    /**
     * Walks graph parts and converts them to target items.
     *
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param localData Local data
     * @param stack Translate stack
     * @param allParts All parts
     * @param parent Parent part
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param staticOperation Unused
     * @param path Path
     * @return List of GraphTargetItems
     * @throws InterruptedException On interrupt
     */
    protected final List<GraphTargetItem> printGraph(List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, BaseLocalData localData, TranslateStack stack, Set<GraphPart> allParts, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, int staticOperation, String path) throws InterruptedException {
        return printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, parent, part, stopPart, stopPartKind, loops, throwStates, null, staticOperation, path, 0);
    }

    /**
     * Walks graph parts and converts them to target items.
     *
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param localData Local data
     * @param stack Translate stack
     * @param allParts All parts
     * @param parent Parent part
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param ret Return
     * @param staticOperation Unused
     * @param path Path
     * @param recursionLevel Recursion level
     * @return List of GraphTargetItems
     * @throws InterruptedException On interrupt
     */
    protected final List<GraphTargetItem> printGraph(List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, BaseLocalData localData, TranslateStack stack, Set<GraphPart> allParts, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> ret, int staticOperation, String path, int recursionLevel) throws InterruptedException {
        loopPrintGraph:
        while (true) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            if (stopPart == null) {
                stopPart = new ArrayList<>();
            }
            if (stopPartKind == null) {
                stopPartKind = new ArrayList<>();
            }
            if (recursionLevel > allParts.size() + 1) {
                throw new TranslateException("printGraph max recursion level reached.");
            }

            if (ret == null) {
                ret = new GraphPartMarkedArrayList<>();
            }

            //try {
            if (debugPrintGraph) {
                System.err.println("PART " + part + " nextsize:" + getNextParts(localData, part).size());
            }
            if (part == null) {
                return ret;
            }
            part = checkPartWithOutput(ret, stack, localData, parent, part, allParts);
            if (part == null) {
                return ret;
            }

            //List<GraphPart> loopContinues = getLoopsContinues(loops);
            boolean isLoop = false;
            Loop currentLoop = null;
            List<GraphTargetItem> precontinueCommands = new ArrayList<>();

            boolean vCanHandleLoop = canHandleLoop(localData, part, loops, throwStates);
            Loop ignoredLoop = null;
            for (Loop el : loops) {
                if ((el.loopContinue == part) && (el.phase == 0)) {
                    if (vCanHandleLoop) {
                        currentLoop = el;
                        currentLoop.phase = 1;
                        isLoop = true;
                    } else {
                        ignoredLoop = el;
                    }

                    break;
                }
            }

            if (isLoop) {
                makeAllCommands(ret, stack);
            }
            if (debugPrintGraph) {
                System.err.println("loopsize:" + loops.size());
            }
            for (int l = loops.size() - 1; l >= 0; l--) {
                Loop el = loops.get(l);
                if (el == ignoredLoop) {
                    if (debugPrintGraph) {
                        System.err.println("ignoring to be loop " + el);
                    }
                    continue;
                }
                if (el == currentLoop) {
                    if (debugPrintGraph) {
                        System.err.println("ignoring current loop " + el);
                    }
                    continue;
                }
                //Special case: for example in AS3 - try in while loop.
                if (el.phase == 4) {
                    el.phase = 1;
                    continue;
                }
                if (el.phase != 1) {
                    if (debugPrintGraph) {
                        System.err.println("ignoring loop " + el);
                    }
                    continue;
                }
                if (el.loopBreak == part) {
                    if (currentLoop != null) {
                        currentLoop.phase = 0;
                    }
                    if (debugPrintGraph) {
                        System.err.println("Adding break");
                    }
                    makeAllCommands(ret, stack);
                    ret.add(new BreakItem(dialect, null, localData.lineStartInstruction, el.id));
                    return ret;
                }
                if (el.loopPreContinue == part) {
                    if (currentLoop != null) {
                        currentLoop.phase = 0;
                    }
                    if (debugPrintGraph) {
                        System.err.println("Adding precontinue");
                    }
                    makeAllCommands(ret, stack);                    
                    ret.add(new ContinueItem(dialect, null, localData.lineStartInstruction, el.id));
                    return ret;
                }
                if (el.loopContinue == part) {
                    if (currentLoop != null) {
                        currentLoop.phase = 0;
                    }
                    if (debugPrintGraph) {
                        System.err.println("Adding continue");
                    }
                    makeAllCommands(ret, stack);                    
                    ret.add(new ContinueItem(dialect, null, localData.lineStartInstruction, el.id));
                    return ret;
                }
            }

            if (debugPrintGraph) {
                System.err.println("stopParts: " + pathToString(stopPart));
            }

            if (stopPart.contains(part)) {

                boolean isRealStopPart = false;
                //this weird stuff handles some goto problems:
                /*loopi:
                for (int i = 0; i < stopPartKind.size(); i++) {
                    if (stopPart.get(i) == part) {
                        for (int j = i + 1; j < stopPartKind.size(); j++) {
                            if (stopPart.get(j) != part) {
                                if (stopPartKind.get(j) == StopPartKind.BLOCK_CLOSE) {
                                    hasBlockClosesAfter = true;
                                    break loopi;
                                }
                            }
                        }
                    }
                }*/

                
                //This weird stuff also tries to handle some goto problems
                for (int i = stopPartKind.size() - 1; i >= 0; i--) {                    
                    if (stopPartKind.get(i) == StopPartKind.OTHER && stopPart.get(i) == part) {
                        isRealStopPart = true;
                        break;
                    }
                    if (stopPartKind.get(i) == StopPartKind.BLOCK_CLOSE) {
                        if (stopPart.get(i) == part) {
                            isRealStopPart = true;
                        }
                        break;
                    }
                }
                
                if (isRealStopPart) {
                    if (currentLoop != null) {
                        currentLoop.phase = 0;
                    }
                    if (debugPrintGraph) {
                        System.err.println("Stopped on part " + part);
                    }
                    return ret;
                }
            }

            if (code.size() <= part.start) {                
                if (!(!ret.isEmpty() && ret.get(ret.size() - 1) instanceof ExitItem)) {
                    ret.add(new ScriptEndItem(dialect));
                }
                return ret;
            }

            boolean vCanHandleVisited = canHandleVisited(localData, part);

            if (vCanHandleVisited) {
                if (visited.contains(part)) {
                    String labelName = "addr" + Helper.formatAddress(code.pos2adr(part.start, true)); // + "_ip" + part.start;
                    List<GraphTargetItem> firstCode = partCodes.get(part);
                    int firstCodePos = partCodePos.get(part);
                    if (firstCodePos > firstCode.size()) {
                        firstCodePos = firstCode.size();
                    }
                    if (firstCode instanceof GraphPartMarkedArrayList) {
                        GraphPartMarkedArrayList<GraphTargetItem> markedFirstCode = (GraphPartMarkedArrayList<GraphTargetItem>) firstCode;
                        firstCodePos = markedFirstCode.indexOfPart(part);
                        if (firstCodePos == -1) {
                            firstCodePos = firstCode.size();
                        }
                        ((GraphPartMarkedArrayList<GraphTargetItem>) firstCode).clearCurrentParts();
                        ((GraphPartMarkedArrayList<GraphTargetItem>) firstCode).startPart(part);
                    }

                    if (firstCode.size() > firstCodePos && (firstCode.get(firstCodePos) instanceof LabelItem)) {
                        labelName = ((LabelItem) firstCode.get(firstCodePos)).labelName;
                    } else {
                        firstCode.add(firstCodePos, new LabelItem(dialect, null, localData.lineStartInstruction, labelName));
                    }
                    ret.add(new GotoItem(dialect, null, localData.lineStartInstruction, labelName));
                    return ret;
                } else {
                    visited.add(part);
                    partCodes.put(part, ret);
                    partCodePos.put(part, ret.size());
                }
            }
            List<GraphTargetItem> currentRet = ret;
            UniversalLoopItem loopItem = null;
            TranslateStack sPreLoop = stack;
            LoopItem li = null;
            boolean loopTypeFound = false;
            boolean doWhileCandidate = false;
            if (isLoop) {
                //makeAllCommands(currentRet, stack);
                stack = (TranslateStack) stack.clone();

                //hack for as1/2 for..in to get enumeration through
                GraphTargetItem topBsr = !stack.isEmpty() && (stack.peek() instanceof BranchStackResistant) ? stack.peek() : null;
                stack.clear();
                if (topBsr != null) {
                    stack.push(topBsr);
                }
                loopItem = new UniversalLoopItem(dialect, null, localData.lineStartInstruction, currentLoop, new ArrayList<>());
                //loopItem.commands=printGraph(visited, localData, stack, allParts, parent, part, stopPart, loops);
                currentRet.add(loopItem);
                currentRet = loopItem.commands;
                li = loopItem;

                if (currentLoop.loopPreContinue != null) {
                    GraphPart backup = currentLoop.loopPreContinue;
                    currentLoop.loopPreContinue = null;
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(currentLoop.loopContinue);
                    List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);
                    stopPartKind2.add(StopPartKind.OTHER);
                    Set<GraphPart> subVisited = new HashSet<>();

                    /*
                     * Save loop phases to be able to walk precontinue block again.
                     */
                    List<Integer> loopPhases = new ArrayList<>();
                    for (Loop el : loops) {
                        loopPhases.add(el.phase);
                    }
                    precontinueCommands = printGraph(foundGotos, partCodes, partCodePos, subVisited, localData, new TranslateStack(path), allParts, null, backup, stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                    currentLoop.loopPreContinue = backup;
                    checkContinueAtTheEnd(precontinueCommands, currentLoop);

                    if (!precontinueCommands.isEmpty() && precontinueCommands.get(precontinueCommands.size() - 1) instanceof IfItem) {
                        IfItem ifi = (IfItem) precontinueCommands.get(precontinueCommands.size() - 1);
                        boolean ok = false;
                        boolean invert = false;
                        if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem) && (((BreakItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                && ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                            ok = true;
                            invert = true;
                        }
                        if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                && ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem) && (((BreakItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                            ok = true;
                        }
                        if (ok) {
                            doWhileCandidate = true;
                        }
                    }

                    if (!doWhileCandidate) {
                        for (GraphTargetItem item : precontinueCommands) {
                            if (item instanceof Block) {
                                currentLoop.loopPreContinue = null;
                                break;
                            }
                        }
                        if (currentLoop.loopPreContinue == null) {
                            precontinueCommands.clear();

                            /*
                             * Restore loop phases
                             */
                            for (int i = 0; i < loopPhases.size(); i++) {
                                loops.get(i).phase = loopPhases.get(i);
                            }
                        }
                    }
                }
                //return ret;
            }

            boolean parseNext = true;

            //****************************DECOMPILING PART*************
            if (stack.isEmpty() && (currentRet instanceof GraphPartMarkedArrayList)) {
                ((GraphPartMarkedArrayList) currentRet).clearCurrentParts();
            }

            GraphPartMarkedArrayList<GraphTargetItem> output = new GraphPartMarkedArrayList<>();
            output.startPart(part);
            if (currentRet instanceof GraphPartMarkedArrayList) {
                ((GraphPartMarkedArrayList) currentRet).startPart(part);
            }
            stack.setConnectedOutput(0, currentRet);
            if (checkPartOutput(currentRet, foundGotos, partCodes, partCodePos, visited, code, localData, allParts, stack, parent, part, stopPart, stopPartKind, loops, throwStates, currentLoop, staticOperation, path, recursionLevel)) {
                parseNext = false;
            } else {
                boolean exHappened = false;
                int ipStart = part.start;
                do {
                    exHappened = false;
                    try {
                        stack.setConnectedOutput(currentRet.size(), output);
                        code.translatePart(output, this, part, localData, stack, ipStart, part.end, staticOperation, path);
                    } catch (GraphPartChangeException ex) { //Special case for ifFrameLoaded when it's over multiple parts
                        //output.addAll(ex.getOutput());
                        for (GraphPart p : allParts) {
                            if (p.containsIP(ex.getIp())) {
                                if (ex.getIp() == p.start) {
                                    currentRet.addAll(output);
                                    //to check for stopparts,etc. we need to call printGraph again
                                    part = p;
                                    //return printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, parent, part, stopPart, stopPartKind, loops, throwStates, ret, staticOperation, path, recursionLevel);
                                    continue loopPrintGraph;
                                }
                                exHappened = true;
                                ipStart = ex.getIp();
                                part = p;
                                break;
                            }
                        }
                    }
                } while (exHappened);
                if ((part.end >= code.size() - 1) && getNextParts(localData, part).isEmpty()) {
                    if (!(!output.isEmpty() && output.get(output.size() - 1) instanceof ExitItem)) {
                        output.add(new ScriptEndItem(dialect));
                    }
                }
            }

            if (parseNext) {
                List<GraphTargetItem> retCheck = check(currentRet, foundGotos, partCodes, partCodePos, visited, code, localData, allParts, stack, parent, part, stopPart, stopPartKind, loops, throwStates, output, currentLoop, staticOperation, path);
                if (retCheck != null) {
                    if (!retCheck.isEmpty()) {
                        currentRet.addAll(retCheck);
                    }
                    parseNext = false;
                    //return ret;
                } else {
                    currentRet.addAll(output);
                }
            }
            //********************************END PART DECOMPILING
            if (parseNext) {

                if (getNextParts(localData, part).size() > 2 || partIsSwitch(part)) {
                    GraphTargetItem originalSwitchedItem = stack.pop();
                    makeAllCommands(currentRet, stack);
                    GraphTargetItem switchedItem = originalSwitchedItem;
                    if ((switchedItem instanceof PopItem) && !currentRet.isEmpty() && (currentRet.get(currentRet.size() - 1) instanceof IfItem)) {
                        switchedItem = currentRet.get(currentRet.size() - 1);
                    }

                    List<GraphTargetItem> caseValues = new ArrayList<>();
                    List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
                    List<Integer> valueMappings = new ArrayList<>();
                    boolean first = false;
                    int pos;

                    Map<Integer, GraphTargetItem> caseExpressions = new HashMap<>();
                    Map<Integer, GraphTargetItem> caseExpressionLeftSides = new HashMap<>();
                    Map<Integer, GraphTargetItem> caseExpressionRightSides = new HashMap<>();

                    GraphTargetItem it = switchedItem;
                    int defaultBranch = 0;
                    boolean hasExpr = false;
                    List<GraphTargetItem> commaCommands = new ArrayList<>();
                    Map<Integer, List<GraphTargetItem>> caseCommaCommands = new HashMap<>();

                    while ((it instanceof TernarOpItem) || (it instanceof IfItem)) {

                        if (it instanceof IfItem) {
                            IfItem ii = (IfItem) it;
                            List<GraphTargetItem> iiOnTrue = ii.onTrue;
                            List<GraphTargetItem> iiOnFalse = ii.onFalse;
                            if ((ii.expression instanceof EqualsTypeItem) || (ii.expression instanceof NotEqualsTypeItem)) {

                                if (ii.expression instanceof NotEqualsTypeItem) {
                                    iiOnTrue = ii.onFalse;
                                    iiOnFalse = ii.onTrue;
                                }

                                if (!iiOnFalse.isEmpty() && !iiOnTrue.isEmpty()
                                        && iiOnTrue.get(iiOnTrue.size() - 1) instanceof PushItem
                                        && iiOnTrue.get(iiOnTrue.size() - 1).value instanceof IntegerValueTypeItem) {
                                    int cpos = ((IntegerValueTypeItem) iiOnTrue.get(iiOnTrue.size() - 1).value).intValue();
                                    caseCommaCommands.put(cpos, commaCommands);
                                    caseExpressionLeftSides.put(cpos, ((BinaryOpItem) ii.expression).getLeftSide());
                                    caseExpressionRightSides.put(cpos, ((BinaryOpItem) ii.expression).getRightSide());
                                    commaCommands = new ArrayList<>();
                                    for (int f = 0; f < iiOnFalse.size() - 1; f++) {
                                        commaCommands.add(iiOnFalse.get(f));
                                    }
                                    it = iiOnFalse.get(iiOnFalse.size() - 1);
                                    if (it instanceof PushItem) {
                                        it = it.value;
                                    }
                                } else {
                                    break;
                                }
                            } else if (ii.expression instanceof FalseItem && !ii.onFalse.isEmpty()) {
                                it = ii.onFalse.get(ii.onFalse.size() - 1);
                            } else if (ii.expression instanceof TrueItem && !ii.onTrue.isEmpty()) {
                                it = ii.onTrue.get(ii.onTrue.size() - 1);
                            } else {
                                break;
                            }
                        } else if (it instanceof TernarOpItem) {
                            TernarOpItem to = (TernarOpItem) it;
                            GraphTargetItem toOnTrue = to.onTrue;
                            GraphTargetItem toOnFalse = to.onFalse;
                            if ((to.expression instanceof EqualsTypeItem) || (to.expression instanceof NotEqualsTypeItem)) {
                                if (to.expression instanceof NotEqualsTypeItem) {
                                    toOnTrue = to.onFalse;
                                    toOnFalse = to.onTrue;
                                }

                                if (toOnTrue instanceof IntegerValueTypeItem) {
                                    int cpos = ((IntegerValueTypeItem) toOnTrue).intValue();
                                    caseExpressionLeftSides.put(cpos, ((BinaryOpItem) to.expression).getLeftSide());
                                    caseExpressionRightSides.put(cpos, ((BinaryOpItem) to.expression).getRightSide());
                                    caseCommaCommands.put(cpos, commaCommands);
                                    commaCommands = new ArrayList<>();
                                    it = toOnFalse;
                                    if (it instanceof CommaExpressionItem) {
                                        commaCommands = new ArrayList<>();
                                        CommaExpressionItem ce = (CommaExpressionItem) it;
                                        for (int f = 0; f < ce.commands.size() - 1; f++) {
                                            commaCommands.add(ce.commands.get(f));
                                        }
                                        it = ce.commands.get(ce.commands.size() - 1);
                                    }
                                } else {
                                    break;
                                }
                            } else if (to.expression instanceof FalseItem) {
                                it = to.onFalse;
                            } else if (to.expression instanceof TrueItem) {
                                it = to.onTrue;
                            } else {
                                break;
                            }
                        }
                    }

                    if (switchedItem != originalSwitchedItem && !caseExpressionRightSides.isEmpty()) {
                        currentRet.remove(currentRet.size() - 1);
                    } else {
                        switchedItem = originalSwitchedItem;
                    }

                    //int ignoredBranch = -1;
                    if ((it instanceof IntegerValueTypeItem) && !(switchedItem instanceof IntegerValueTypeItem)) {
                        defaultBranch = ((IntegerValueTypeItem) it).intValue();
                    }

                    Map<Integer, GraphTargetItem> caseExpressionOtherSides = caseExpressions;
                    if (!caseExpressionRightSides.isEmpty()) {
                        GraphTargetItem firstItem;
                        firstItem = (GraphTargetItem) caseExpressionRightSides.values().toArray()[0];
                        boolean sameRight = true;
                        for (GraphTargetItem cit : caseExpressionRightSides.values()) {
                            if (!cit.equals(firstItem)) {
                                sameRight = false;
                                break;
                            }
                        }

                        if (sameRight) {
                            caseExpressions = caseExpressionLeftSides;
                            caseExpressionOtherSides = caseExpressionRightSides;
                            switchedItem = firstItem;
                            hasExpr = true;
                        } else {
                            firstItem = (GraphTargetItem) caseExpressionLeftSides.values().toArray()[0];

                            boolean sameLeft = true;
                            for (GraphTargetItem cit : caseExpressionLeftSides.values()) {
                                if (!cit.equals(firstItem)) {
                                    sameLeft = false;
                                    break;
                                }
                            }
                            if (sameLeft) {
                                caseExpressions = caseExpressionRightSides;
                                caseExpressionOtherSides = caseExpressionLeftSides;
                                switchedItem = firstItem;
                                hasExpr = true;
                            }
                        }
                    }

                    first = true;
                    pos = 0;
                    //This is tied to AS3 switch implementation which has nextparts switched from index 1. TODO: Make more universal

                    GraphPart defaultPart = hasExpr ? getNextParts(localData, part).get(1 + defaultBranch) : getNextParts(localData, part).get(0);
                    List<GraphPart> caseBodyParts = new ArrayList<>();
                    for (int i = 1; i < getNextParts(localData, part).size(); i++) {
                        if (!hasExpr) {
                            //This if probably should be removed, but it fails some tests...
                            if (getNextParts(localData, part).get(i) == defaultPart) {
                                pos++;
                                continue;
                            }
                            caseValues.add(new IntegerValueItem(dialect, null, localData.lineStartInstruction, pos));
                        } else if (caseExpressions.containsKey(pos)) {
                            GraphTargetItem expr = caseExpressions.get(pos);
                            if (caseCommaCommands.get(pos).size() > 0) {
                                List<GraphTargetItem> exprCommaCommands = new ArrayList<>(caseCommaCommands.get(pos));
                                exprCommaCommands.add(expr);
                                expr = new CommaExpressionItem(dialect, null, expr.lineStartItem, exprCommaCommands);
                            }
                            caseValues.add(expr);
                        } else {
                            pos++;
                            continue;
                        }
                        caseBodyParts.add(getNextParts(localData, part).get(i));
                        pos++;
                    }
                    Reference<GraphPart> nextRef = new Reference<>(null);
                    Reference<GraphTargetItem> tiRef = new Reference<>(null);
                    makeAllCommands(currentRet, stack);
                    SwitchItem sw = handleSwitch(switchedItem, originalSwitchedItem.getSrc(), foundGotos, partCodes, partCodePos, visited, allParts, stack, stopPart, stopPartKind, loops, throwStates, localData, staticOperation, path,
                            caseValues, defaultPart, caseBodyParts, nextRef, tiRef);
                    GraphPart next = nextRef.getVal();
                    checkSwitch(localData, sw, caseExpressionOtherSides.values(), currentRet);
                    currentRet.add(sw);
                    if (next != null) {
                        if (tiRef.getVal() != null) {
                            ret.add(tiRef.getVal());
                        } else {
                            printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, part, next, stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel + 1);
                        }
                    }
                    pos++;
                } //else
                GraphPart nextOnePart = null;
                if (getNextParts(localData, part).size() == 2 && !partIsSwitch(part)) {
                    GraphTargetItem expr = getIfExpression(localData, stack, currentRet);

                    if (nextOnePart == null) {

                        List<GraphPart> nps;
                        nps = getNextParts(localData, part);
                        boolean isEmpty = nps.get(0) == nps.get(1);

                        GraphPart next = getCommonPart(localData, part, nps, loops, throwStates);
                        TranslateStack trueStack = (TranslateStack) stack.clone();
                        TranslateStack falseStack = (TranslateStack) stack.clone();

                        //hack for as1/2 for..in to get enumeration through
                        GraphTargetItem topBsr = !stack.isEmpty() && (stack.peek() instanceof BranchStackResistant) ? stack.peek() : null;
                        trueStack.clear();
                        falseStack.clear();
                        if (topBsr != null) {
                            trueStack.add(topBsr);
                            falseStack.add(topBsr);
                        }
                        if (isEmpty) {
                            next = nps.get(0);
                        }
                        boolean hasOnTrue = nps.get(1) != next;
                        boolean hasOnFalse = nps.get(0) != next;
                        
                        if (nps.get(1).start >= code.size()) {
                            next = null;
                            hasOnTrue = true;
                            hasOnFalse = true;
                        } else if (nps.get(0).start >= code.size()) {
                            next = null;
                            hasOnTrue = true;
                            hasOnFalse = true;
                        }

                        
                        
                        List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                        List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);                       
                        
                        if ((!isEmpty) && (next != null)) {                                                                               
                            stopPart2.add(next);
                            stopPartKind2.add(StopPartKind.BLOCK_CLOSE);                            
                        }

                        List<GraphTargetItem> onTrue = new ArrayList<>();
                        if (!isEmpty && hasOnTrue) {
                            onTrue = printGraph(foundGotos, partCodes, partCodePos, visited, prepareBranchLocalData(localData), trueStack, allParts, part, nps.get(1), stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                        }
                        List<GraphTargetItem> onFalse = new ArrayList<>();

                        if (!isEmpty && hasOnFalse) {
                            onFalse = printGraph(foundGotos, partCodes, partCodePos, visited, prepareBranchLocalData(localData), falseStack, allParts, part, nps.get(0), stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                        }
                        //List<GraphTargetItem> out2 = new ArrayList<>();
                        //makeAllCommands(out2, stack);
                        makeAllCommands(onTrue, trueStack);
                        makeAllCommands(onFalse, falseStack);
                        GraphTargetItem addAfterIf = null;
                        if (!onTrue.isEmpty() && !onFalse.isEmpty()
                                && (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)
                                && (onFalse.get(onFalse.size() - 1) instanceof ContinueItem)) {
                            ContinueItem contTrue = (ContinueItem) onTrue.get(onTrue.size() - 1);
                            ContinueItem contFalse = (ContinueItem) onFalse.get(onFalse.size() - 1);
                            if (contTrue.loopId == contFalse.loopId) {
                                onTrue.remove(onTrue.size() - 1);
                                onFalse.remove(onFalse.size() - 1);
                                addAfterIf = contTrue;
                            }
                        }

                        List<GraphTargetItem> filteredOnTrue = onTrue;
                        List<GraphTargetItem> filteredOnFalse = onFalse;

                        if (!isEmpty(filteredOnTrue) && !isEmpty(filteredOnFalse)
                                && (filteredOnTrue.get(filteredOnTrue.size() - 1) instanceof PushItem)
                                && (filteredOnFalse.get(filteredOnFalse.size() - 1) instanceof PushItem)
                                && canBeCommaised(filteredOnTrue) && canBeCommaised(filteredOnFalse)) {
                            GraphTargetItem ternarOnTrue;
                            if (filteredOnTrue.size() > 1) {
                                filteredOnTrue.set(filteredOnTrue.size() - 1, filteredOnTrue.get(filteredOnTrue.size() - 1).value); // replace Pushitem with its value
                                ternarOnTrue = new CommaExpressionItem(dialect, null, null, filteredOnTrue);
                            } else {
                                ternarOnTrue = filteredOnTrue.get(0).value;
                            }
                            GraphTargetItem ternarOnFalse;
                            if (filteredOnFalse.size() > 1) {
                                filteredOnFalse.set(filteredOnFalse.size() - 1, filteredOnFalse.get(filteredOnFalse.size() - 1).value); // replace Pushitem with its value                            
                                ternarOnFalse = new CommaExpressionItem(dialect, null, null, filteredOnFalse);
                            } else {
                                ternarOnFalse = filteredOnFalse.get(0).value;
                            }
                            TernarOpItem top = new TernarOpItem(dialect, null, localData.lineStartInstruction, expr.invert(null), ternarOnTrue, ternarOnFalse);
                            stack.push(handleTernar(top, localData));
                        } else {
                            boolean isIf = true;
                            //If the ontrue is empty, switch ontrue and onfalse
                            if (filteredOnTrue.isEmpty() && !filteredOnFalse.isEmpty()) {
                                expr = expr.invert(null);
                                List<GraphTargetItem> tmp = onTrue;
                                onTrue = onFalse;
                                onFalse = tmp;
                                //tmp = filteredOnTrue;
                                filteredOnTrue = filteredOnFalse;
                                //filteredOnFalse = tmp;
                            }
                            if (!stack.isEmpty() && ((filteredOnTrue.size() == 1 && (filteredOnTrue.get(0) instanceof PopItem)) || ((filteredOnTrue.size() >= 2) && (filteredOnTrue.get(0) instanceof PopItem) && (filteredOnTrue.get(filteredOnTrue.size() - 1) instanceof PushItem)))) {
                                if (filteredOnTrue.size() > 1) {
                                    GraphTargetItem rightSide = ((PushItem) filteredOnTrue.get(filteredOnTrue.size() - 1)).value;
                                    GraphTargetItem prevExpr = stack.pop();
                                    GraphTargetItem leftSide = expr.getNotCoercedNoDup();

                                    boolean hideEmptyTrueFalse = true;

                                    if (leftSide instanceof DuplicateItem) {
                                        isIf = false;
                                        if (hideEmptyTrueFalse && prevExpr.getNotCoercedNoDup() instanceof FalseItem) {
                                            stack.push(rightSide);
                                        } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof FalseItem) {
                                            stack.push(prevExpr);
                                        } else {
                                            stack.push(new OrItem(dialect, null, localData.lineStartInstruction, prevExpr, rightSide));
                                        }
                                    } else if (leftSide.invert(null).getNotCoercedNoDup() instanceof DuplicateItem) {
                                        isIf = false;
                                        if (hideEmptyTrueFalse && prevExpr.getNotCoercedNoDup() instanceof TrueItem) {
                                            stack.push(rightSide);
                                        } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof TrueItem) {
                                            stack.push(prevExpr);
                                        } else {
                                            stack.push(new AndItem(dialect, null, localData.lineStartInstruction, prevExpr, rightSide));
                                        }
                                    } else if (prevExpr instanceof FalseItem) {
                                        isIf = false;
                                        leftSide = leftSide.invert(null);

                                        if (hideEmptyTrueFalse && leftSide.getNotCoercedNoDup() instanceof TrueItem) {
                                            stack.push(rightSide);
                                        } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof TrueItem) {
                                            stack.push(leftSide);
                                        } else {
                                            stack.push(new AndItem(dialect, null, localData.lineStartInstruction, leftSide, rightSide));
                                        }
                                    } else if (prevExpr instanceof TrueItem) {
                                        isIf = false;
                                        if (hideEmptyTrueFalse && leftSide.getNotCoercedNoDup() instanceof FalseItem) {
                                            stack.push(rightSide);
                                        } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof FalseItem) {
                                            stack.push(leftSide);
                                        } else {
                                            stack.push(new OrItem(dialect, null, localData.lineStartInstruction, leftSide, rightSide));
                                        }
                                    } else {
                                        stack.push(prevExpr); //push it back
                                        //Still unstructured
                                    }
                                } else {
                                    isIf = false;
                                }
                            }

                            if (isIf) {
                                makeAllCommands(currentRet, stack);
                                IfItem b = new IfItem(dialect, null, localData.lineStartInstruction, expr.invert(null), onTrue, onFalse);
                                b.decisionPart = part;
                                b.onTruePart = nps.get(0);
                                b.onFalsePart = nps.get(1);
                                currentRet.add(b);
                                if (processSubBlk(b, null)) {
                                    stack.push(new PopItem(dialect, null, localData.lineStartInstruction));
                                }
                            }
                        }
                        if (addAfterIf != null) {
                            currentRet.add(addAfterIf);
                        }
                        //currentRet.addAll(out2);
                        if (next != null) {
                            printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, part, next, stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel + 1);
                            //currentRet.addAll();
                        }
                    }
                }  //else
                if (getNextParts(localData, part).size() == 1) {
                    nextOnePart = getNextParts(localData, part).get(0);
                }

                if (getNextParts(localData, part).isEmpty()) {
                    makeAllCommands(currentRet, stack);
                }

                if (nextOnePart != null) {
                    printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, part, getNextParts(localData, part).get(0), stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel + 1);
                }

            }
            if (isLoop && loopItem != null && currentLoop != null) {

                processIfs(loopItem.commands);
                processSwitches(loopItem.commands, currentLoop.id);
                processOther(loopItem.commands, currentLoop.id);

                checkContinueAtTheEnd(loopItem.commands, currentLoop);

                //DoWhile based on precontinue
                if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                    List<List<GraphTargetItem>> continueCommands1 = new ArrayList<>();
                    getContinuesCommands(loopItem.commands, continueCommands1, currentLoop.id);
                    if (!continueCommands1.isEmpty() && doWhileCandidate) {
                        int index = ret.indexOf(loopItem);
                        ret.remove(index);
                        IfItem ifi = (IfItem) precontinueCommands.remove(precontinueCommands.size() - 1);
                        List<GraphTargetItem> exprList = new ArrayList<>(precontinueCommands);
                        boolean invert = false;
                        if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem) && (((BreakItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                && ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                            invert = true;
                        }

                        GraphTargetItem expr = ifi.expression;
                        if (invert) {
                            expr = expr.invert(null);
                        }
                        exprList.add(expr);
                        ret.add(index, li = new DoWhileItem(dialect, null, expr.getLineStartItem(), currentLoop, loopItem.commands, exprList));
                        loopTypeFound = true;
                    }
                }

                //Loop with condition at the beginning (While)
                if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                    if (loopItem.commands.get(0) instanceof IfItem) {
                        IfItem ifi = (IfItem) loopItem.commands.get(0);

                        List<GraphTargetItem> bodyBranch = null;
                        boolean inverted = false;
                        boolean breakpos2 = false;
                        BreakItem addBreakItem = null;
                        ContinueItem addContinueItem = null;
                        if ((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem)) {
                            BreakItem bi = (BreakItem) ifi.onTrue.get(0);
                            if (bi.loopId == currentLoop.id) {
                                bodyBranch = ifi.onFalse;
                                inverted = true;
                            }
                        } else if ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem)) {
                            BreakItem bi = (BreakItem) ifi.onFalse.get(0);
                            if (bi.loopId == currentLoop.id) {
                                bodyBranch = ifi.onTrue;
                            }
                        } else if (loopItem.commands.size() == 2 && (loopItem.commands.get(1) instanceof BreakItem)) {
                            BreakItem bi = (BreakItem) loopItem.commands.get(1);
                            if (ifi.onTrue.isEmpty()) {
                                inverted = true;
                            }
                            bodyBranch = inverted ? ifi.onFalse : ifi.onTrue;
                            breakpos2 = true;
                            if (bi.loopId != currentLoop.id) { //it's break of another parent loop
                                addBreakItem = bi; //we must add it after the loop
                            }
                        } else if ((ifi.onTrue.size() == 1)
                                && (ifi.onTrue.get(0) instanceof ContinueItem)
                                && (((ContinueItem) ifi.onTrue.get(0)).loopId != currentLoop.id)) {
                            addContinueItem = (ContinueItem) ifi.onTrue.get(0);
                            bodyBranch = ifi.onFalse;
                            inverted = true;
                        } else if ((ifi.onFalse.size() == 1)
                                && (ifi.onFalse.get(0) instanceof ContinueItem)
                                && (((ContinueItem) ifi.onFalse.get(0)).loopId != currentLoop.id)) {
                            addContinueItem = (ContinueItem) ifi.onFalse.get(0);
                            bodyBranch = ifi.onTrue;
                        } else if (loopItem.commands.size() == 2
                                && (loopItem.commands.get(1) instanceof ContinueItem)
                                && (((ContinueItem) loopItem.commands.get(1)).loopId != currentLoop.id)) {
                            addContinueItem = (ContinueItem) loopItem.commands.get(1);
                            if (ifi.onTrue.isEmpty()) {
                                inverted = true;
                            }
                            bodyBranch = inverted ? ifi.onFalse : ifi.onTrue;
                            breakpos2 = true;
                        }
                        if (bodyBranch != null) {
                            int index = ret.indexOf(loopItem);
                            ret.remove(index);
                            List<GraphTargetItem> exprList = new ArrayList<>();
                            GraphTargetItem expr = ifi.expression;
                            if (inverted) {
                                if (expr instanceof LogicalOpItem) {
                                    expr = ((LogicalOpItem) expr).invert(null);
                                } else {
                                    expr = new NotItem(dialect, null, expr.getLineStartItem(), expr);
                                }
                            }
                            exprList.add(expr);
                            List<GraphTargetItem> commands = new ArrayList<>();
                            commands.addAll(bodyBranch);
                            loopItem.commands.remove(0);
                            if (breakpos2) {
                                loopItem.commands.remove(0); //remove that break too
                            }
                            commands.addAll(loopItem.commands);
                            checkContinueAtTheEnd(commands, currentLoop);
                            List<GraphTargetItem> finalComm = new ArrayList<>();

                            //findGotoTargets - comment this out:
                            if (!precontinueCommands.isEmpty()) {

                                List<List<GraphTargetItem>> continueCommands = new ArrayList<>();
                                getContinuesCommands(commands, continueCommands, currentLoop.id);

                                if (continueCommands.isEmpty()) {
                                    commands.addAll(precontinueCommands);
                                    precontinueCommands = new ArrayList<>();

                                    //Single continue and there is break/continue/return/throw at end of the commands
                                } else if (!commands.isEmpty() && continueCommands.size() == 1) {
                                    GraphTargetItem lastItem = commands.get(commands.size() - 1);
                                    if ((lastItem instanceof BreakItem) || (lastItem instanceof ContinueItem) || (lastItem instanceof ExitItem)) {
                                        continueCommands.get(0).addAll(continueCommands.get(0).size() - 1, precontinueCommands);
                                        precontinueCommands = new ArrayList<>();
                                    }
                                }

                                finalComm.addAll(precontinueCommands);
                            }
                            if (!finalComm.isEmpty()) {
                                ret.add(index, li = new ForItem(dialect, expr.getSrc(), expr.getLineStartItem(), currentLoop, new ArrayList<>(), exprList.get(exprList.size() - 1), finalComm, commands));
                            } else {
                                ret.add(index, li = new WhileItem(dialect, expr.getSrc(), expr.getLineStartItem(), currentLoop, exprList, commands));
                            }
                            if (addBreakItem != null) {
                                ret.add(index + 1, addBreakItem);
                            }
                            if (addContinueItem != null) {
                                ret.add(index + 1, addContinueItem);
                            }

                            loopTypeFound = true;
                        }
                    }
                }

                if (!loopTypeFound && !precontinueCommands.isEmpty()) {
                    loopItem.commands.addAll(precontinueCommands);
                }

                //Loop with condition at the end (Do..While)
                if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                    if (loopItem.commands.get(loopItem.commands.size() - 1) instanceof IfItem) {
                        IfItem ifi = (IfItem) loopItem.commands.get(loopItem.commands.size() - 1);
                        List<GraphTargetItem> bodyBranch = null;
                        boolean inverted = false;
                        if ((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem)) {
                            BreakItem bi = (BreakItem) ifi.onTrue.get(0);
                            if (bi.loopId == currentLoop.id) {
                                bodyBranch = ifi.onFalse;
                                inverted = true;
                            }
                        } else if ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem)) {
                            BreakItem bi = (BreakItem) ifi.onFalse.get(0);
                            if (bi.loopId == currentLoop.id) {
                                bodyBranch = ifi.onTrue;
                            }
                        }
                        if (bodyBranch != null) {
                            //Condition at the beginning
                            int index = ret.indexOf(loopItem);
                            ret.remove(index);
                            List<GraphTargetItem> exprList = new ArrayList<>();
                            GraphTargetItem expr = ifi.expression;
                            if (inverted) {
                                expr = expr.invert(null);
                            }

                            checkContinueAtTheEnd(bodyBranch, currentLoop);

                            List<GraphTargetItem> commands = new ArrayList<>();

                            if (!bodyBranch.isEmpty()) {
                                ret.add(index, loopItem);
                            } else {
                                loopItem.commands.remove(loopItem.commands.size() - 1);
                                commands.addAll(loopItem.commands);
                                commands.addAll(bodyBranch);
                                exprList.add(expr);
                                checkContinueAtTheEnd(commands, currentLoop);
                                ret.add(index, li = new DoWhileItem(dialect, null, exprList.get(0).getLineStartItem(), currentLoop, commands, exprList));
                            }

                            loopTypeFound = true;
                        }
                    }
                }

                if (!loopTypeFound) {
                    checkContinueAtTheEnd(loopItem.commands, currentLoop);
                }
                currentLoop.phase = 2;

                GraphTargetItem replaced = checkLoop(ret, li, localData, loops, throwStates, sPreLoop);
                if (replaced != li) {
                    int index = ret.indexOf(li);
                    ret.remove(index);
                    if (replaced != null) {
                        ret.add(index, replaced);
                    }
                }

                if (currentLoop.loopBreak != null) {
                    printGraph(foundGotos, partCodes, partCodePos, visited, localData, sPreLoop, allParts, part, currentLoop.loopBreak, stopPart, stopPartKind, loops, throwStates, ret, staticOperation, path, recursionLevel + 1);
                }
            }
            break;
        }
        return ret;
    }

    /**
     * Checks switch statement. Override this method to add custom switch
     * handling.
     *
     * @param localData Local data
     * @param switchItem Switch item
     * @param otherSides Other sides
     * @param output Output
     */
    protected void checkSwitch(BaseLocalData localData, SwitchItem switchItem, Collection<? extends GraphTargetItem> otherSides, List<GraphTargetItem> output) {

    }

    /**
     * Checks all parts of the graph after they are populated. Override this
     * method to add custom checks.
     *
     * @param allBlocks All blocks
     */
    protected void checkGraph(List<GraphPart> allBlocks) {
    }

    /**
     * Checks IP and allows to modify it. Override this method to add custom IP
     * checks.
     *
     * @param ip Current IP
     * @return New IP
     */
    protected int checkIp(int ip) {
        return ip;
    }

    /**
     * Searches for part by IP.
     *
     * @param ip IP
     * @param allParts All parts
     * @return Part
     */
    public GraphPart searchPart(int ip, Collection<? extends GraphPart> allParts) {
        if (ip < 0) {
            return null;
        }
        for (GraphPart p : allParts) {
            if (ip >= p.start && ip <= p.end) {
                return p;
            }
        }
        return null;
    }

    private void flattenJumps(List<GraphPart> heads, List<GraphPart> allBlocks) {
        boolean modified;
        do {
            modified = false;
            for (int i = 0; i < allBlocks.size(); i++) {
                GraphPart part = allBlocks.get(i);
                if (heads.contains(part)) {
                    continue;
                }
                if (part.getHeight() != 1) {
                    continue;
                }
                if (part.start < 0) {
                    continue;
                }
                if (part.start >= code.size()) {
                    continue;
                }
                if (!code.get(part.start).isJump()) {
                    continue;
                }

                if (part.nextParts.size() != 1) {
                    continue;
                }

                GraphPart nextPart = part.nextParts.get(0);
                if (nextPart == part) {
                    continue;
                }

                for (GraphPart r : part.refs) {
                    while (true) {
                        int ind = r.nextParts.indexOf(part);
                        if (ind == -1) {
                            break;
                        }
                        r.nextParts.set(ind, nextPart);
                    }

                }

                nextPart.refs.remove(part);
                nextPart.refs.addAll(part.refs);
                allBlocks.remove(i);
                modified = true;
            }
        } while (modified);
    }

    /**
     * Makes connected set of GraphParts from GraphSource.
     *
     * @param code Graph source
     * @param allBlocks All blocks to populate parts into.
     * @param exceptions Exceptions
     * @return List of entry points
     * @throws InterruptedException On interrupt
     */
    public List<GraphPart> makeGraph(GraphSource code, List<GraphPart> allBlocks, List<GraphException> exceptions) throws InterruptedException {
        List<Integer> alternateEntries = new ArrayList<>();
        for (GraphException ex : exceptions) {
            alternateEntries.add(ex.start);
            //alternateEntries.add(ex.end);
            alternateEntries.add(ex.target);
        }
        HashMap<Integer, List<Integer>> refs = code.visitCode(alternateEntries);
        List<GraphPart> ret = new ArrayList<>();
        boolean[] visited = new boolean[code.size()];
        ret.add(makeGraph(null, new GraphPath(), code, startIp, 0, allBlocks, refs, visited));
        for (int pos : alternateEntries) {
            GraphPart e1 = new GraphPart(-1, -1);
            e1.path = new GraphPath("e");
            ret.add(makeGraph(e1, new GraphPath("e"), code, pos, pos, allBlocks, refs, visited));
        }
        flattenJumps(ret, allBlocks);
        checkGraph(allBlocks);
        return ret;
    }

    /**
     * Makes connected set of GraphParts from GraphSource.
     *
     * @param parent Parent part
     * @param path Path
     * @param code Graph source
     * @param startIp Start IP
     * @param lastIp Last IP
     * @param allBlocks All blocks
     * @param refs References
     * @param visited Visited
     * @return Entry point
     * @throws InterruptedException On interrupt
     */
    private GraphPart makeGraph(GraphPart parent, GraphPath path, GraphSource code, int startIp, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean[] visited) throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }

        int ip = startIp;
        GraphPart existingPart = searchPart(ip, allBlocks);
        if (existingPart != null) {
            if (parent != null) {
                existingPart.refs.add(parent);
                parent.nextParts.add(existingPart);
            }
            return existingPart;
        }
        GraphPart ret = new GraphPart(ip, -1);
        ret.path = path;
        GraphPart part = ret;
        if (parent != null) {
            ret.refs.add(parent);
            parent.nextParts.add(ret);
        }
        while (ip < code.size()) {
            int aip = checkIp(ip);
            if (ip >= code.size()) {
                break;
            }
            if (aip >= code.size()) {
                ip = aip;
                break;
            }
            if (visited[ip] || ((ip != startIp) && (refs.get(ip).size() > 1))) {
                part.end = lastIp;
                GraphPart found = searchPart(aip, allBlocks);

                allBlocks.add(part);

                if (found != null) {
                    part.nextParts.add(found);
                    found.refs.add(part);
                    break;
                } else {
                    GraphPart nextPart = new GraphPart(aip, -1);
                    nextPart.path = path;
                    part.nextParts.add(nextPart);
                    nextPart.refs.add(part);
                    part = nextPart;
                }
            }
            visited[ip] = true;
            ip = aip;
            lastIp = ip;
            GraphSourceItem ins = code.get(ip);
            if (ins.isIgnored()) {
                ip++;
                continue;
            }
            if (ins instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) ins;
                if (ins instanceof Action) { //TODO: Remove dependency of AVM1
                    long endAddr = ((Action) ins).getAddress() + cnt.getHeaderSize();
                    for (long size : cnt.getContainerSizes()) {
                        endAddr += size;
                    }
                    ip = code.adr2pos(endAddr);

                    if ((ins instanceof ActionDefineFunction) || (ins instanceof ActionDefineFunction2)) {
                        part.end = lastIp;
                        allBlocks.add(part);
                        GraphPart nextGraphPart = new GraphPart(ip, -1);
                        nextGraphPart.path = path;
                        part.nextParts.add(nextGraphPart);
                        nextGraphPart.refs.add(part);
                        part = nextGraphPart;
                    }
                }

                continue;
            } else if (ins.isExit()) {
                part.end = ip;
                allBlocks.add(part);
                break;
            } else if (ins.isJump()) {
                part.end = ip;
                allBlocks.add(part);
                ip = ins.getBranches(code).get(0);
                ip = checkIp(ip);
                makeGraph(part, path, code, ip, lastIp, allBlocks, refs, visited);
                lastIp = -1;
                break;
            } else if (ins.isBranch()) {
                part.end = ip;

                allBlocks.add(part);
                List<Integer> branches = ins.getBranches(code);
                for (int i = 0; i < branches.size(); i++) {
                    makeGraph(part, path.sub(i, ip), code, checkIp(branches.get(i)), ip, allBlocks, refs, visited);
                }
                break;
            }
            ip++;
        }
        if ((part.end == -1) && (ip >= code.size())) {
            if (part.start == code.size()) {
                part.end = code.size();
                allBlocks.add(part);
            } else {
                part.end = ip - 1;
                for (GraphPart p : allBlocks) {
                    if (p.start == ip) {
                        p.refs.add(part);
                        part.nextParts.add(p);
                        allBlocks.add(part);
                        return ret;
                    }
                }
                GraphPart gp = new GraphPart(ip, ip);
                allBlocks.add(gp);
                gp.refs.add(part);
                part.nextParts.add(gp);
                allBlocks.add(part);
            }
        }
        return ret;
    }

    /**
     * Converts list of TreeItems to string.
     *
     * @param tree List of TreeItem
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public static GraphTextWriter graphToString(List<GraphTargetItem> tree, GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean lastNewLine = true;
        int tsize = tree.size();
        if (!tree.isEmpty() && (tree.get(tree.size() - 1) instanceof ScriptEndItem)) {
            tsize--;
        }
        for (int i = 0; i < tsize; i++) {
            GraphTargetItem ti = tree.get(i);
            if (!ti.isEmpty()) {
                if (ti.hasSingleNewLineAround() && !lastNewLine) {
                    writer.newLine();
                }
                ti.toStringSemicoloned(writer, localData);
                if (!ti.handlesNewLine()) {
                    writer.newLine();
                }
                lastNewLine = false;
                if (ti.hasSingleNewLineAround() && (i < tsize - 1)) {
                    writer.newLine();
                    lastNewLine = true;
                }
            }
        }
        return writer;
    }

    /**
     * Prepares local data for branch. Override this method to add custom branch
     * handling.
     *
     * @param localData Local data
     * @return Local data for a branch
     */
    public BaseLocalData prepareBranchLocalData(BaseLocalData localData) {
        return localData;
    }

    /**
     * Get source items for a part
     *
     * @param part Part
     * @return List of source items
     */
    protected final List<GraphSourceItem> getPartItems(GraphPart part) {
        List<GraphSourceItem> ret = new ArrayList<>();
        do {
            for (int i = 0; i < part.getHeight(); i++) {
                if (part.getPosAt(i) < code.size()) {
                    if (part.getPosAt(i) < 0) {
                        continue;
                    }
                    GraphSourceItem s = code.get(part.getPosAt(i));
                    if (!s.isJump()) {
                        ret.add(s);
                    }
                }
            }
            if (part.nextParts.size() == 1 && part.nextParts.get(0).refs.size() == 1) {
                part = part.nextParts.get(0);
            } else {
                part = null;
            }
        } while (part != null);
        return ret;
    }

    /**
     * Moves all pushitems from commands to stack.
     *
     * @param commands Commands
     * @param stack Stack
     */
    protected static void makeAllStack(List<GraphTargetItem> commands, TranslateStack stack) {
        int pcnt = 0;
        for (int i = commands.size() - 1; i >= 0; i--) {
            if (commands.get(i) instanceof PushItem) {
                pcnt++;
            } else {
                break;
            }
        }
        for (int i = commands.size() - pcnt; i < commands.size(); i++) {
            stack.push(commands.remove(i).value);
            i--;
        }
    }

    /**
     * Moves all stack items to commands. (If it's not a branch stack resistant
     * or other special case)
     *
     * @param commands Commands
     * @param stack Stack
     */
    public void makeAllCommands(List<GraphTargetItem> commands, TranslateStack stack) {
        int clen = commands.size();
        boolean isExit = false;
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof ScriptEndItem) {
                clen--;
                isExit = true;
            }
        }
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof ExitItem) {
                isExit = true;
                clen--;
            }
        }
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof BreakItem) {
                clen--;
            }
        }
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof ContinueItem) {
                clen--;
            }
        }
        for (int i = stack.size() - 1; i >= 0; i--) {
            GraphTargetItem p = stack.get(i);
            if (p instanceof BranchStackResistant) {
                continue;
            }
            stack.remove(i);
            if (!(p instanceof PopItem)) {
                if (isExit) {
                    //ASC2 leaves some function calls unpopped on stack before returning from a method
                    commands.add(clen, p);
                } else {
                    int pos = 0;
                    if (p.outputPos < commands.size()) {
                        commands.add(p.outputPos, new PushItem(p));
                    } else {
                        commands.add(clen + pos, new PushItem(p));
                    }
                }
            }
        }
    }

    /**
     * Handles switch statement.
     *
     * @param switchedObject Switched object
     * @param switchStartItem Switch start item
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code positions
     * @param visited Visited
     * @param allParts All parts
     * @param stack Stack
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param localData Local data
     * @param staticOperation Unused
     * @param path Path
     * @param caseValuesMap Case values map
     * @param defaultPart Default part
     * @param caseBodyParts Case body parts
     * @param nextRef Next reference
     * @param tiRef Target item reference
     * @return Switch item
     * @throws InterruptedException On interrupt
     */
    protected SwitchItem handleSwitch(GraphTargetItem switchedObject,
            GraphSourceItem switchStartItem, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, Set<GraphPart> allParts, TranslateStack stack, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, BaseLocalData localData, int staticOperation, String path,
            List<GraphTargetItem> caseValuesMap, GraphPart defaultPart, List<GraphPart> caseBodyParts, Reference<GraphPart> nextRef, Reference<GraphTargetItem> tiRef) throws InterruptedException {

        boolean hasDefault = false;
        /*
                case 4:
                case 5:
                default: 
                    trace("5 & def");
                    ...
                case 6:
                
         */
        //must go backwards to hit case 5, not case 4
        for (int i = caseBodyParts.size() - 1; i >= 0; i--) {
            if (caseBodyParts.get(i) == defaultPart) {
                DefaultItem di = new DefaultItem(dialect);
                caseValuesMap.add(i + 1, di);
                caseBodyParts.add(i + 1, defaultPart);
                hasDefault = true;
                break;
            }
        }

        if (!hasDefault) {
            /*
                    case 1:
                        trace("1");
                    case 2:
                        trace("2"); //no break
                    default:
                        trace("def");
                        ...
                    case 3:  
             */
            //must go backwards to hit case 2, not case 1
            for (int i = caseBodyParts.size() - 1; i >= 0; i--) {
                if (caseBodyParts.get(i).leadsTo(localData, this, code, defaultPart, loops, throwStates, false)) {
                    DefaultItem di = new DefaultItem(dialect);
                    caseValuesMap.add(i + 1, di);
                    caseBodyParts.add(i + 1, defaultPart);
                    hasDefault = true;
                    break;
                }
            }
        }

        if (!hasDefault) {
            /*
                    case 1:
                        trace("1");
                        break;
                    default:
                        trace("def"); //no break
                    case 2:
                        trace("2");                    
             */
            for (int i = 0; i < caseBodyParts.size(); i++) {
                if (defaultPart.leadsTo(localData, this, code, caseBodyParts.get(i), loops, throwStates, false)) {
                    DefaultItem di = new DefaultItem(dialect);
                    caseValuesMap.add(i, di);
                    caseBodyParts.add(i, defaultPart);
                    hasDefault = true;
                    break;
                }
            }
        }

        if (!hasDefault) {
            /*
                        case 1:
                        ...
                        case 2:
                        ...
                        default:
                            trace("def");                        
             */
            caseValuesMap.add(new DefaultItem(dialect));
            caseBodyParts.add(defaultPart);
        }

        //gotoTargets
        GraphPart breakPart = getMostCommonPart(localData, caseBodyParts, loops, throwStates, stopPart);
        //removeEdgeToFromList(gotoTargets, breakPart);

        List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
        GraphPart next = breakPart;

        //create switch as new loop break command detection to work
        Loop currentLoop = new Loop(loops.size(), null, next);
        currentLoop.phase = 1;
        loops.add(currentLoop);
        List<Integer> valuesMapping = new ArrayList<>();
        List<GraphPart> caseBodies = new ArrayList<>();

        for (int i = 0; i < caseValuesMap.size(); i++) {
            GraphPart cur = caseBodyParts.get(i);
            if (!caseBodies.contains(cur)) {
                caseBodies.add(cur);
            }
        }

        //Sort bodies by leadsto to proper handle clauses without a break statement
        loopi:
        for (int i = 0; i < caseBodies.size(); i++) {
            GraphPart b = caseBodies.get(i);
            for (int j = i + 1; j < caseBodies.size(); j++) {
                GraphPart b2 = caseBodies.get(j);
                if (b2.leadsTo(localData, this, code, b, loops, throwStates, false)) {
                    if (b.leadsTo(localData, this, code, b2, loops, throwStates, false)) { //unstructured code
                        continue;
                    }
                    caseBodies.remove(j);
                    caseBodies.add(i, b2);
                    i--;
                    continue loopi;
                } else if (j > i + 1) {
                    if (b.leadsTo(localData, this, code, b2, loops, throwStates, false)) {
                        caseBodies.remove(j);
                        caseBodies.add(i + 1, b2);
                        continue loopi;
                    }
                }
            }
        }

        for (int i = 0; i < caseValuesMap.size(); i++) {
            GraphPart cur = caseBodyParts.get(i);
            valuesMapping.add(caseBodies.indexOf(cur));
        }

        for (int i = 0; i < caseBodies.size(); i++) {
            List<GraphTargetItem> currentCaseCommands = new ArrayList<>();
            boolean willHaveBreak = false;
            if (i < caseBodies.size() - 1) {
                if (!caseBodies.get(i).leadsTo(localData, this, code, caseBodies.get(i + 1), loops, throwStates, false)) {
                    willHaveBreak = true;
                }
            }

            localData.allSwitchParts.add(caseBodies.get(i));

            List<GraphPart> stopPart2x = new ArrayList<>(stopPart);
            List<StopPartKind> stopPartKind2x = new ArrayList<>(stopPartKind);
            for (GraphPart b : caseBodies) {
                if (b != caseBodies.get(i)) {
                    stopPart2x.add(b);
                    stopPartKind2x.add(StopPartKind.OTHER);
                }
            }
            if (breakPart != null) {
                stopPart2x.add(breakPart);
                stopPartKind2x.add(StopPartKind.OTHER);
            }
            TranslateStack subStack = (TranslateStack) stack.clone();
            currentCaseCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData, subStack, allParts, null, caseBodies.get(i), stopPart2x, stopPartKind2x, loops, throwStates, staticOperation, path);
            if (willHaveBreak) {
                if (!currentCaseCommands.isEmpty()) {
                    GraphTargetItem last = currentCaseCommands.get(currentCaseCommands.size() - 1);
                    if (!(last instanceof ContinueItem) && !(last instanceof BreakItem) && !(last instanceof GotoItem) && !(last instanceof ExitItem) && !(last instanceof ScriptEndItem)) {
                        currentCaseCommands.add(new BreakItem(dialect, null, localData.lineStartInstruction, currentLoop.id));
                    }
                }
            }
            caseCommands.add(currentCaseCommands);            
        }

        /*
        switch(a)
        {
        case 0:
        case 1:
            break;
        case 2:
            break;        
        }
        
        =>
        
        switch(a)
        {
        case 0:
        case 1:
        case 2:
            break;        
        }
        
         */
        for (int i = 0; i < caseCommands.size(); i++) {
            if (caseCommands.get(i).size() == 1
                    && (caseCommands.get(i).get(0) instanceof BreakItem)
                    && (((BreakItem) caseCommands.get(i).get(0)).loopId == currentLoop.id)) {
                for (int j = i + 1; j < caseCommands.size(); j++) {
                    if (caseCommands.get(j).size() == 1
                            && (caseCommands.get(j).get(0) instanceof BreakItem)
                            && (((BreakItem) caseCommands.get(j).get(0)).loopId == currentLoop.id)) {
                        caseCommands.get(j - 1).remove(0);
                    } else {
                        break;
                    }
                }
            }
        }

        nextRef.setVal(next);

        currentLoop.phase = 2;
        GraphTargetItem ti = checkLoop(new ArrayList<>() /*??*/, next, stopPart, loops, throwStates);
        tiRef.setVal(ti);

        SwitchItem ret = new SwitchItem(dialect, null, switchStartItem, currentLoop, switchedObject, caseValuesMap, caseCommands, valuesMapping);
        fixSwitchEnd(ret);
        return ret;
    }

    protected void fixSwitchEnd(SwitchItem sw) {

        List<GraphTargetItem> caseValuesMap = sw.caseValues;
        List<Integer> valuesMapping = sw.valuesMapping;
        List<List<GraphTargetItem>> caseCommands = sw.caseCommands;
        long loopId = sw.loop.id;

        //Remove empty default clauses:
        loopj:
        for (int j = 0; j < caseValuesMap.size(); j++) {
            if (caseValuesMap.get(j) instanceof DefaultItem) {
                int m = valuesMapping.get(j);
                List<GraphTargetItem> cc = caseCommands.get(m);
                if (cc.size() == 1) {
                    if (cc.get(0) instanceof BreakItem) {
                        BreakItem br = (BreakItem) cc.get(0);
                        if (br.loopId == loopId) {
                            caseValuesMap.remove(j);
                            valuesMapping.remove(j);
                            boolean hasOther = false;
                            for (int k = 0; k < valuesMapping.size(); k++) {
                                if (valuesMapping.get(k) == m) {
                                    hasOther = true;
                                    break;
                                }
                            }
                            if (!hasOther) {
                                caseCommands.remove(m);
                                for (int k = 0; k < valuesMapping.size(); k++) {
                                    int vm = valuesMapping.get(k);
                                    if (vm > m) {
                                        valuesMapping.set(k, vm - 1);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        //If the lastone is default empty and alone, remove it                       
        if (!caseCommands.isEmpty()) {
            List<GraphTargetItem> lastc = caseCommands.get(caseCommands.size() - 1);
            if (!lastc.isEmpty() && (lastc.get(lastc.size() - 1) instanceof BreakItem)) {
                BreakItem bi = (BreakItem) lastc.get(lastc.size() - 1);
                if (bi.loopId == loopId) {
                    lastc.remove(lastc.size() - 1);
                }
            }
            if (lastc.isEmpty()) {
                int cnt2 = 0;
                int defaultIndex = -1;
                for (int i = 0; i < valuesMapping.size(); i++) {
                    if (valuesMapping.get(i) == caseCommands.size() - 1) {
                        if (caseValuesMap.get(i) instanceof DefaultItem) {
                            defaultIndex = i;
                            break;
                        }
                    }
                }
                if (defaultIndex > -1) {
                    for (int i = valuesMapping.size() - 1; i >= 0; i--) {
                        if (valuesMapping.get(i) == caseCommands.size() - 1) {
                            cnt2++;
                        }
                    }

                    caseValuesMap.remove(defaultIndex);
                    valuesMapping.remove(defaultIndex);
                    if (cnt2 == 1) {
                        caseCommands.remove(caseCommands.size() - 1);
                    }
                }
            }
        }
        
        
        //remove last break from last section                
        if (!caseCommands.isEmpty()) {
            List<GraphTargetItem> lastc = caseCommands.get(caseCommands.size() - 1);
            if (!lastc.isEmpty() && (lastc.get(lastc.size() - 1) instanceof BreakItem)) {
                BreakItem bi = (BreakItem) lastc.get(lastc.size() - 1);
                if (bi.loopId == loopId) {
                    lastc.remove(lastc.size() - 1);
                }
            }
        }
    }

    /**
     * Checks if part is a switch. Defaults to false. Override this method to
     * add custom switch handling.
     *
     * @param part Part
     * @return True if part is a switch
     */
    protected boolean partIsSwitch(GraphPart part) {
        return false;
    }

    /**
     * Replaces ternar with custom value
     *
     * @param ternar Ternar
     * @param localData Local data
     * @return Custom item
     */
    protected GraphTargetItem handleTernar(TernarOpItem ternar, BaseLocalData localData) {
        return ternar;
    }
}
