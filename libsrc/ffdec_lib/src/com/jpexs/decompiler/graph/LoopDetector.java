package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.graph.precontinues.GraphPrecontinueDetector;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detector of loops.
 *
 * @author JPEXS
 */
public class LoopDetector {

    /**
     * Detects loops.
     *
     * @param heads
     * @param loops
     * @param throwStates
     * @param allParts
     * @param switchParts
     * @throws InterruptedException
     */
    public void detectLoops(List<GraphPart> heads, List<Loop> loops, List<ThrowState> throwStates, Set<GraphPart> allParts, List<List<GraphPart>> switchParts, List<GraphPart> switchBreaks, boolean detectBreaks) throws InterruptedException {
        PrevNextWalker pnw = new ThrowPrevNextWalker(throwStates);

        List<GraphPart> loopHeaders = new ArrayList<>();
        List<GraphPart> loopTails = new ArrayList<>();
        List<Set<GraphPart>> loopBodys = new ArrayList<>();

        Set<GraphPartEdge> backEdges = getBackEdges(heads.get(0), throwStates);

        for (GraphPartEdge edge : backEdges) {
            loopBodys.add(buildNaturalLoop(edge.from, edge.to, pnw));
            loopHeaders.add(edge.to);
            loopTails.add(edge.from);
        }

        //Join loops with the same header
        Map<GraphPart, Loop> loopByHeader = new LinkedHashMap<>();

        for (int i = 0; i < loopHeaders.size(); i++) {
            GraphPart header = loopHeaders.get(i);
            GraphPart tail = loopTails.get(i);
            Set<GraphPart> body = loopBodys.get(i);

            if (loopByHeader.containsKey(header)) {
                Loop loop = loopByHeader.get(header);
                loop.backEdges.add(tail);
                loop.loopBody.addAll(body);
            } else {
                Loop loop = new Loop(loops.size(), header, null);
                loop.orderPart = header;
                loop.loopBody.addAll(body);
                loop.backEdges.add(tail);

                loopByHeader.put(header, loop);
                loops.add(loop);
            }
        }

        computeParentLoops(loops);

        //Calculate closed time
        calculateClosedTime(heads, loops);

        //Find loops without header
        //findLoopsWithoutHeader(loops, backEdges, allParts);
        loops.sort(new LoopDetector.LoopComparator());

        for (int i = 0; i < loops.size(); i++) {
            loops.get(i).id = i;
        }

        if (!detectBreaks) {
            return;
        }

        Set<GraphPartEdge> ignoredEdges = new LinkedHashSet<>();

        Set<GraphPart> multipleSameCase = new LinkedHashSet<>();
        
        for (List<GraphPart> switchCases : switchParts) {
            for (int i = 1; i < switchCases.size(); i++) {
                GraphPart case1 = switchCases.get(i - 1);
                GraphPart case2 = switchCases.get(i);
                Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "checking case {0} and {1}", new Object[]{case1, case2});
                if (case1 == case2) {
                    multipleSameCase.add(case1);
                    Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "duplicate case {0} detected", new Object[]{case1});
                    continue;
                }
                GraphPart leadsToPrev = partLeadsToGetPrev(case1, case2, backEdges, pnw);
                if (leadsToPrev != null) {
                    Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "- leadsto, ignoring edge {0} => {1}", new Object[]{leadsToPrev, case2});
                    ignoredEdges.add(new GraphPartEdge(leadsToPrev, case2));
                }
            }
        }

        for (GraphPart part : multipleSameCase) {
            List<? extends GraphPart> prev = pnw.getPrev(part);
            for (int r = 0; r < prev.size() - 1; r++) {
                Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "ignoring edge {0}->{1} detected", new Object[]{part.refs.get(r), part});
                ignoredEdges.add(new GraphPartEdge(prev.get(r), part));
            }
        }

        new GraphPrecontinueDetector().detectPrecontinues(heads, allParts, loops, throwStates);

        findBreaks(loops, backEdges, throwStates, ignoredEdges, switchBreaks);

        loops.sort(new LoopDetector.LoopComparator());

        for (int i = 0; i < loops.size(); i++) {
            loops.get(i).id = i;
        }

        new GraphPrecontinueDetector().detectPrecontinues(heads, allParts, loops, throwStates);
    }

    public void computeParentLoops(List<Loop> loops) {
        int n = loops.size();

        List<Set<GraphPart>> sets = new ArrayList<>(n);
        for (Loop loop : loops) {
            sets.add(loop.loopBody);
        }

        for (int i = 0; i < n; i++) {
            Set<GraphPart> child = sets.get(i);

            int bestParentIndex = -1;
            int bestParentSize = Integer.MAX_VALUE;

            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }

                Set<GraphPart> candidate = sets.get(j);

                if (candidate.size() > child.size() && candidate.containsAll(child)) {
                    if (candidate.size() < bestParentSize) {
                        bestParentSize = candidate.size();
                        bestParentIndex = j;
                    }
                }
            }
            if (bestParentIndex > -1) {
                loops.get(i).parentLoop = loops.get(bestParentIndex);
            }
        }
    }

    /**
     * Calculates time of closing the node. The node is closed when all its
     * input edges are already visited (including back edges), then all its
     * output edges are processed.
     * <p>
     * This time is useful when sorting nodes according their occurrence in
     * getMostCommonPart method - used for switch detection
     *
     * @param heads Heads
     * @param loops Already calculated loops to get backedges from.
     */
    public void calculateClosedTime(List<GraphPart> heads, List<Loop> loops) {
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
                //boolean canClose = true;
                for (GraphPart r : part.refs) {
                    LevelMapEdge e = new LevelMapEdge(r, part);
                    /*if (backEdges.contains(e) && !visitedEdges.contains(e)) {
                        canClose = false;
                        continue;
                    }*/
                    if (!visitedEdges.contains(e)) {
                        continue loopopened;
                    }
                }
                for (GraphPart n : part.nextParts) {
                    openedNodes.add(n);
                    visitedEdges.add(new LevelMapEdge(part, n));
                }
                //if (canClose) {
                closedNodes.add(part);
                part.closedTime = closedTime++;
                //}
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

    /*
    private void findLoopsWithoutHeader(List<Loop> loops, Set<GraphPartEdge> backEdges, Set<GraphPart> allParts) {
        List<GraphPart> sortedParts = new ArrayList<>(allParts);
        sortedParts.sort(new Comparator<GraphPart>() {
            @Override
            public int compare(GraphPart o1, GraphPart o2) {
                return o1.closedTime - o2.closedTime;
            }
        });

        PrevNextWalker pnw = new BasicPrevNextWalker();

        Set<GraphPart> ignored = new HashSet<>();

        Loop currentLoop = null;

        for (GraphPart ifPart : sortedParts) {
            List<? extends GraphPart> branches = pnw.getNext(ifPart);

            for (Loop loop : loops) {
                if (loop.loopContinue != null && loop.loopContinue == ifPart) {
                    currentLoop = loop;
                }
            }

            if (branches.size() > 2) {
                GraphPart breakPart = getMostCommonPart(new HashSet<>(branches), backEdges, pnw, false);
                if (breakPart != null) {
                    Loop loop = new Loop(loops.size(), null, breakPart);
                    loop.orderPart = ifPart;
                    loops.add(loop);
                    ignored.add(breakPart);
                }
                continue;
            }

            if (branches.size() != 2) {
                continue;
            }
            GraphPart ifCommonPart = getCommonPart(new HashSet<>(branches), backEdges, pnw);
            //System.err.println("IF at " + ifPart + ", commonPart " + ifCommonPart);            
            if (ifCommonPart == null) {
                //System.err.println("- ignored");
                continue;
            }
            Set<GraphPart> visited = new HashSet<>();

            Stack<GraphPart> stack = new Stack<>();
            stack.push(ifCommonPart);
            loopstack:
            while (!stack.isEmpty()) {
                GraphPart part = stack.pop();
                if (part == ifPart) {
                    continue;
                }
                visited.add(part);
                //System.err.println("popped " + part);
                for (GraphPart r : pnw.getPrev(part)) {
                    //System.err.println("checking ref " + r);
                    if (r == part) {
                        //System.err.println("- is part");
                        continue;
                    }
                    if (backEdges.contains(new GraphPartEdge(r, part))) {
                        //System.err.println("- is backedge");
                        continue;
                    }
                    if (!partLeadsTo(ifPart, r, backEdges, pnw)) {
                        //System.err.println("- ifPart does not lead to it");
                        continue;
                    }
                    if (ignored.contains(r)) {
                        //System.err.println("- is ignored");
                        continue;
                    }
                    if (visited.contains(r)) {
                        //System.err.println("- already visited");
                        continue;
                    }
                    //System.err.println("used ref " + r);
                    for (GraphPart n : pnw.getNext(r)) {
                        //System.err.println("- checking n = " + n);
                        if (visited.contains(n)) {
                            //System.err.println("- visited contains");
                            continue;
                        }
                        if (!partLeadsTo(n, ifCommonPart, backEdges, pnw)) {
                            GraphPart breakPart = getCommonPart(new HashSet<>(Arrays.asList(n, ifCommonPart)), backEdges, pnw);
                            //System.err.println("next " +n + ", breakPart " + breakPart);
                            if (breakPart != null && !ignored.contains(breakPart)) {                                
                                Loop loop = new Loop(loops.size(), ifPart, breakPart);
                                loop.orderPart = ifPart;
                                loops.add(loop);
                                currentLoop = loop;
                                //}
                                ignored.add(breakPart);
                                ignored.add(currentLoop.loopContinue);
                                if (!ignored.contains(ifCommonPart)) {
                                    stack.clear();
                                    stack.push(ifCommonPart);
                                    visited.clear();
                                    continue loopstack;
                                }
                            }
                        } else {
                            //System.err.println("- n leads to commonpart, skip");
                        }
                    }
                    stack.push(r);
                }
            }
        }
    }
     */
    /**
     * Find loop breaks. Make sure you call Graph.calculateClosedTime to loops
     * before calling this method.
     *
     * @param loops Loops
     */
    private void findBreaks(List<Loop> loops, Set<GraphPartEdge> backEdges, List<ThrowState> throwStates, Set<GraphPartEdge> ignoredEdges, List<GraphPart> switchBreaks) {
        PrevNextWalker pnw = new BasicPrevNextWalker();
        //PrevNextWalker tpnw = new ThrowPrevNextWalker(throwStates); //new BasicPrevNextWalker();                        

        Set<GraphPartEdge> throwEdges = new LinkedHashSet<>();

        for (ThrowState ts : throwStates) {
            for (GraphPart tp : ts.throwingParts) {
                throwEdges.add(new GraphPartEdge(tp, ts.targetPart));
            }
        }

        Map<Loop, PrevNextWalker> loopWalkers = new LinkedHashMap<>();
        Map<Loop, List<ThrowState>> loopThrowStates = new LinkedHashMap<>();

        //Find outside edges
        for (Loop loop : loops) {

            List<ThrowState> subThrowStates = new ArrayList<>();

            for (ThrowState ts : throwStates) {
                boolean contains = false;
                if (ts.throwingParts.contains(loop.loopContinue)) {
                    contains = true;
                }
                if (ts.catchParts.contains(loop.loopContinue)) {
                    //contains = true;
                }
                if (!contains) {
                    subThrowStates.add(ts);
                }
            }
            loopThrowStates.put(loop, subThrowStates);

            PrevNextWalker walker = new ThrowPrevNextWalker(subThrowStates);
            loopWalkers.put(loop, walker);

            Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "Body of loop with continue {0}:", loop.loopContinue);
            for (GraphPart part : loop.loopBody) {
                Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "- {0}", part);
                for (GraphPart next : walker.getNext(part)) {
                    GraphPartEdge edge = new GraphPartEdge(part, next);
                    Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "- checking next {0}", next);
                    if (!loop.loopBody.contains(next)) {
                        Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "- is outside");
                        loop.edgesOutside.add(new GraphPartEdge(part, next));
                    }
                }
            }
        }

        int loopIndex = -1;
        //Detect breaks
        for (Loop loop : loops) {
            loopIndex++;

            if (loop.loopBreak != null) {
                continue;
            }

            Set<GraphPart> parentContinues = new LinkedHashSet<>();
            Set<GraphPart> parentBreaks = new LinkedHashSet<>();
            Loop parentLoop = loop.parentLoop;
            while (parentLoop != null) {
                if (parentLoop.loopBreak != null) {
                    parentBreaks.add(parentLoop.loopBreak);
                }
                if (parentLoop.loopContinue != null) {
                    parentContinues.add(parentLoop.loopContinue);
                }
                if (parentLoop.loopPreContinue != null) {
                    parentContinues.add(parentLoop.loopPreContinue);
                }
                parentLoop = parentLoop.parentLoop;
            }
            
            parentBreaks.addAll(switchBreaks); //these are not real parent, but should work(?)

            PrevNextWalker walker = loopWalkers.get(loop);
            List<ThrowState> subThrowStates = loopThrowStates.get(loop);

            Set<GraphPart> allOutReachable = new LinkedHashSet<>();
            List<Set<GraphPart>> allReachable = new ArrayList<>();
            Logger.getLogger(LoopDetector.class.getName()).log(Level.FINE, "loop {0} outside:", loopIndex);
            Set<GraphPart> outParts = new LinkedHashSet<>();
            for (GraphPartEdge edge : loop.edgesOutside) {
                if (ignoredEdges.contains(edge)) {
                    Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "Ignored outside edge {0}->{1}", new Object[]{edge.from, edge.to});
                    continue;
                }
                GraphPart outPart = edge.to;
                outParts.add(outPart);
                Set<GraphPart> reachable = getReachable(outPart, backEdges, parentBreaks, walker, ignoredEdges);

                if (parentContinues.contains(outPart)) {
                    reachable.clear();
                    reachable.add(outPart);
                }

                Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "Reachables of {0}:", outPart);
                for (GraphPart r : reachable) {
                    Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "- {0}:", r);
                }
                allReachable.add(reachable);
                allOutReachable.addAll(reachable);
            }

            for (ThrowState ts : subThrowStates) {
                Set<GraphPart> catchParts = ts.catchParts;
                if (catchParts.isEmpty()) {
                    catchParts = new HashSet<>();
                    catchParts.add(ts.targetPart);
                    //This handles only first part of the catch. We should somehow better detect the actual catchParts,
                    //but for now, it's better than nothing.
                    //The catchparts are empty usually in swftools scripts
                }

                catchParts = new HashSet<>(catchParts);

                if (catchParts.contains(loop.loopContinue)) {
                    continue;
                }

                catchParts.removeAll(parentContinues);
                catchParts.removeAll(parentBreaks);

                Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "removing catchparts:");
                for (GraphPart part : catchParts) {
                    Logger.getLogger(LoopDetector.class.getName()).log(Level.FINEST, "- {0}", part);
                }
                allOutReachable.removeAll(catchParts);
            }

            if (allOutReachable.isEmpty()) {
                continue;
            }

            
            //outParts.removeAll(allSwitchCases);
            
            List<CandidateResult> candidateList = new ArrayList<>();
            for (GraphPart part : allOutReachable) {
                int numBranches = 0;
                for (Set<GraphPart> reachable : allReachable) {                
                    if (reachable.contains(part)) {                        
                        numBranches++;
                    }
                }
                candidateList.add(new CandidateResult(part, numBranches, outParts.contains(part), parentBreaks.contains(part)));
            }

            candidateList.sort(new Comparator<CandidateResult>() {
                @Override
                public int compare(CandidateResult o1, CandidateResult o2) {
                    boolean b1 = parentBreaks.contains(o1.part);
                    boolean b2 = parentBreaks.contains(o2.part);

                    boolean op1 = outParts.contains(o1.part);
                    boolean op2 = outParts.contains(o2.part);

                    if (b1 != b2) {
                        if (b1) {
                            return 1;
                        }
                        return -1;
                    }

                    boolean mb1 = o1.numBranches > 1;
                    boolean mb2 = o2.numBranches > 1;

                    if (mb1 != mb2) {
                        if (mb1) {
                            return -1;
                        }
                        return 1;
                    }

                    if (op1 != op2) {
                        if (op1) {
                            return -1;
                        }
                        return 1;
                    }

                    return o1.part.closedTime - o2.part.closedTime;
                }
            });
            Logger.getLogger(LoopDetector.class.getName()).log(Level.FINE, "candidates:");
            for (CandidateResult cand : candidateList) {
                Logger.getLogger(LoopDetector.class.getName()).log(Level.FINE, "- {0}", cand);
            }
            loop.loopBreak = candidateList.get(0).part;
        }
    }

    private Set<GraphPart> buildNaturalLoop(GraphPart tail, GraphPart header, PrevNextWalker pnw) {
        Set<GraphPart> loop = new LinkedHashSet<>();
        Deque<GraphPart> stack = new ArrayDeque<>();

        if (tail == header) {
            loop.add(header);
            return loop;
        }

        loop.add(header);
        loop.add(tail);
        stack.push(tail);

        while (!stack.isEmpty()) {
            GraphPart x = stack.pop();
            for (GraphPart p : pnw.getPrev(x)) {
                if (!loop.contains(p)) {
                    loop.add(p);
                    stack.push(p);
                }
            }
        }
        return loop;
    }

    private static class CandidateResult {

        GraphPart part;
        int numBranches;
        boolean inOutParts;
        boolean inBreaks;

        public CandidateResult(GraphPart part, int numBranches, boolean inOutParts, boolean inBreaks) {
            this.part = part;
            this.numBranches = numBranches;
            this.inOutParts = inOutParts;
            this.inBreaks = inBreaks;
        }

        @Override
        public String toString() {
            return "" + part + " (numBranches: " + numBranches + ", closedTime: " + part.closedTime + ", inOutParts: " + inOutParts + ", inBreaks: " + inBreaks + ")";
        }
    }

    /*
    private GraphPart getMostCommonPart(Set<GraphPart> parts, Set<GraphPartEdge> backEdges, PrevNextWalker pnw, boolean includeTheseParts) {

        if (parts.isEmpty()) {
            return null;
        }
        if (parts.size() == 1) {
            return parts.iterator().next();
        }

        List<Set<GraphPart>> reachables = new ArrayList<>();
        List<GraphPart> allPartsReachable = new ArrayList<>();

        for (GraphPart part : parts) {
            Set<GraphPart> reachable = getReachable(part, backEdges, new HashSet<>(), pnw);            
            reachables.add(reachable);
            allPartsReachable.addAll(reachable);
        }

        if (!includeTheseParts) {
            allPartsReachable.removeAll(parts);
        }

        allPartsReachable.sort(new Comparator<GraphPart>() {
            @Override
            public int compare(GraphPart o1, GraphPart o2) {
                return o1.closedTime - o2.closedTime;
            }
        });

        List<CandidateResult> candidates = new ArrayList<>();
        for (GraphPart part : allPartsReachable) {
            int numBranches = 0;
            for (Set<GraphPart> reachable : reachables) {
                if (reachable.contains(part)) {
                    numBranches++;
                }
            }
            candidates.add(new CandidateResult(part, numBranches, 0));
        }

        candidates.sort(new Comparator<CandidateResult>() {
            @Override
            public int compare(CandidateResult o1, CandidateResult o2) {
                int ret = o2.numBranches - o1.numBranches;
                if (ret != 0) {
                    return ret;
                }
                return o1.part.closedTime - o2.part.closedTime;
            }
        });

        return candidates.get(0).part;
    }

    private GraphPart getCommonPart(Set<GraphPart> parts, Set<GraphPartEdge> backEdges, PrevNextWalker pnw) {
        List<Set<GraphPart>> reachables = new ArrayList<>();
        List<GraphPart> allPartsReachable = new ArrayList<>();

        for (GraphPart part : parts) {
            Set<GraphPart> reachable = getReachable(part, backEdges, new HashSet<>(), pnw);
            reachables.add(reachable);
            allPartsReachable.addAll(reachable);
        }

        allPartsReachable.sort(new Comparator<GraphPart>() {
            @Override
            public int compare(GraphPart o1, GraphPart o2) {
                return o1.closedTime - o2.closedTime;
            }
        });

        for (GraphPart part : allPartsReachable) {
            boolean allContains = true;
            for (Set<GraphPart> reachable : reachables) {
                if (!reachable.contains(part)) {
                    allContains = false;
                    break;
                }
            }
            if (allContains) {
                return part;
            }
        }
        return null;
    }*/
    private Set<GraphPart> getReachable(GraphPart startPart, Set<GraphPartEdge> backEdges, Set<GraphPart> ignoredParts, PrevNextWalker pnw, Set<GraphPartEdge> ignoredEdges) {
        Set<GraphPart> visited = new LinkedHashSet<>();

        Queue<GraphPart> q = new ArrayDeque<>();
        q.offer(startPart);
        while (!q.isEmpty()) {
            GraphPart part = q.poll();
            if (visited.contains(part)) {
                continue;
            }            
            visited.add(part);

            for (GraphPart next : pnw.getNext(part)) {
                GraphPartEdge edge = new GraphPartEdge(part, next);

                if (ignoredEdges.contains(edge)) {
                    continue;
                }
                if (ignoredParts.contains(part)) {
                    continue;
                }
                if (backEdges.contains(edge)) {
                    visited.add(edge.to);
                    continue;
                }
                q.offer(next);
            }
        }
        return visited;
    }

    private GraphPart partLeadsToGetPrev(GraphPart part1, GraphPart part2, Set<GraphPartEdge> backEdges, PrevNextWalker pnw) {
        if (part1 == part2) {
            return null;
        }
        Stack<GraphPart> stack = new Stack<>();
        stack.push(part1);
        Set<GraphPart> visited = new HashSet<>();
        while (!stack.isEmpty()) {
            GraphPart part = stack.pop();
            if (visited.contains(part)) {
                continue;
            }
            visited.add(part);

            for (GraphPart next : pnw.getNext(part)) {
                GraphPartEdge edge = new GraphPartEdge(part, next);
                if (backEdges.contains(edge)) {
                    continue;
                }

                if (next == part2) {
                    return part;
                }

                stack.push(next);
            }
        }
        return null;
    }

    /*
    private boolean partLeadsTo(GraphPart part1, GraphPart part2, Set<GraphPartEdge> backEdges, PrevNextWalker pnw) {
        if (part1 == part2) {
            return false;
        }
        Stack<GraphPart> stack = new Stack<>();
        stack.push(part1);
        Set<GraphPart> visited = new HashSet<>();
        while (!stack.isEmpty()) {
            GraphPart part = stack.pop();
            if (visited.contains(part)) {
                continue;
            }
            visited.add(part);

            if (part == part2) {
                return true;
            }

            for (GraphPart next : pnw.getNext(part)) {
                GraphPartEdge edge = new GraphPartEdge(part, next);
                if (backEdges.contains(edge)) {
                    continue;
                }
                stack.push(next);
            }
        }
        return false;
    }
     */
    private Set<GraphPartEdge> getBackEdges(GraphPart firstPart, List<ThrowState> throwStates) throws InterruptedException {
        Stack<GraphPartEdge> stack = new Stack<>();
        stack.push(new GraphPartEdge(null, firstPart));
        Stack<List<GraphPart>> pathStack = new Stack<>();
        pathStack.push(new ArrayList<>());
        Set<GraphPart> visited = new HashSet<>();

        Set<GraphPartEdge> backEdges = new LinkedHashSet<>();

        while (!stack.isEmpty()) {
            GraphPartEdge edge = stack.pop();
            List<GraphPart> path = pathStack.pop();
            if (path.contains(edge.to)) {
                backEdges.add(edge);
            }
            GraphPart part = edge.to;
            if (visited.contains(part)) {
                continue;
            }
            visited.add(part);
            List<GraphPart> subPath = new ArrayList<>(path);
            subPath.add(part);
            for (GraphPart next : part.nextParts) {
                stack.push(new GraphPartEdge(part, next));
                pathStack.push(subPath);
            }
            for (ThrowState ts : throwStates) {
                if (ts.throwingParts.contains(part)) {
                    stack.push(new GraphPartEdge(part, ts.targetPart));
                    pathStack.push(subPath);
                }
            }
        }

        return backEdges;
    }

    public static class LoopComparator implements Comparator<Loop> {

        public int compare(Loop o1, Loop o2) {
            GraphPart order1 = o1.orderPart;
            if (order1 == null) {
                order1 = o1.loopContinue;
            }
            GraphPart order2 = o2.orderPart;
            if (order2 == null) {
                order2 = o2.loopContinue;
            }

            int ret = order1.closedTime - order2.closedTime;
            if (ret != 0) {
                return ret;
            }
            ret = (o1.loopBreak == null ? 1 : 0) - (o2.loopBreak == null ? 1 : 0);
            if (ret != 0) {
                return ret;
            }
            if (o1.loopBreak == null) {
                return 0;
            }
            return o2.loopBreak.closedTime - o1.loopBreak.closedTime;
        }

    }
}
