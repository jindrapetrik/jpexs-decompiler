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

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for loop exit selection and postdominator ordering. */
public class LoopDetectorPostDominatorTest {

    @Test
    public void testShortCircuitInnerConditionJoinsAtOuterPrecontinue() throws InterruptedException {
        GraphPart entry = new GraphPart(0, 0);
        GraphPart outerCondition = new GraphPart(1, 1);
        GraphPart innerInit = new GraphPart(2, 2);
        GraphPart innerCondition = new GraphPart(3, 3);
        GraphPart secondCondition = new GraphPart(4, 4);
        GraphPart conditionResult = new GraphPart(5, 5);
        GraphPart innerBody = new GraphPart(6, 6);
        GraphPart earlyExit = new GraphPart(7, 7);
        GraphPart commonExit = new GraphPart(8, 8);
        GraphPart firstTail = new GraphPart(9, 9);
        GraphPart secondTail = new GraphPart(10, 10);
        GraphPart outerIncrement = new GraphPart(11, 11);
        GraphPart done = new GraphPart(12, 12);
        connect(entry, outerCondition);
        connect(outerCondition, innerInit, done);
        connect(innerInit, innerCondition);
        connect(innerCondition, secondCondition, conditionResult);
        connect(secondCondition, conditionResult);
        connect(conditionResult, innerBody, commonExit);
        connect(innerBody, innerCondition, earlyExit);
        connect(earlyExit, commonExit);
        connect(commonExit, firstTail, secondTail);
        connect(firstTail, outerIncrement);
        connect(secondTail, outerIncrement);
        connect(outerIncrement, outerCondition);
        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(entry, outerCondition, innerInit, innerCondition,
                secondCondition, conditionResult, innerBody, earlyExit, commonExit, firstTail, secondTail, outerIncrement, done));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(entry), loops, Collections.emptyList(),
                parts, Collections.emptyList(), Collections.emptyList(), true);
        Assert.assertEquals(loops.size(), 2);
        Assert.assertSame(loops.get(0).loopPreContinue, commonExit);
        Assert.assertSame(loops.get(1).loopContinue, innerCondition);
        Assert.assertSame(loops.get(1).loopBreak, commonExit);
    }

    @Test
    public void testInnerLoopExitsJoinBeforeOuterContinue() throws InterruptedException {
        assertInnerLoopExit(true);
    }

    @Test
    public void testInnerLoopExitsJoinAtBranchingOuterPrecontinue() throws InterruptedException {
        assertInnerLoopExit(false);
    }

    private void assertInnerLoopExit(boolean canSkipInnerLoop) throws InterruptedException {
        GraphPart entry = new GraphPart(0, 0);
        GraphPart outerCondition = new GraphPart(1, 1);
        GraphPart innerInit = new GraphPart(2, 2);
        GraphPart innerCondition = new GraphPart(3, 3);
        GraphPart innerBody = new GraphPart(4, 4);
        GraphPart innerIncrement = new GraphPart(5, 5);
        GraphPart earlyExit = new GraphPart(6, 6);
        GraphPart naturalExit = new GraphPart(7, 7);
        GraphPart commonExit = new GraphPart(8, 8);
        GraphPart reportMissing = new GraphPart(9, 9);
        GraphPart outerIncrement = new GraphPart(10, 10);
        GraphPart done = new GraphPart(11, 11);

        connect(entry, outerCondition);
        connect(outerCondition, innerInit, done);
        connect(innerInit, innerCondition);
        if (canSkipInnerLoop) {
            connect(innerInit, outerIncrement);
        }
        connect(innerCondition, innerBody, naturalExit);
        connect(innerBody, innerIncrement, earlyExit);
        connect(innerIncrement, innerCondition);
        connect(earlyExit, commonExit);
        connect(naturalExit, commonExit);
        connect(commonExit, outerIncrement, reportMissing);
        connect(reportMissing, outerIncrement);
        connect(outerIncrement, outerCondition);

        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(entry, outerCondition, innerInit,
                innerCondition, innerBody, innerIncrement, earlyExit, naturalExit, commonExit,
                reportMissing, outerIncrement, done));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(entry), loops, Collections.emptyList(),
                parts, Collections.emptyList(), Collections.emptyList(), true);

        Assert.assertEquals(loops.size(), 2);
        Assert.assertSame(loops.get(1).loopContinue, innerCondition);
        if (!canSkipInnerLoop) {
            Assert.assertSame(loops.get(0).loopPreContinue, commonExit);
        }
        Assert.assertSame(loops.get(1).loopBreak, commonExit);
    }

    private void connect(GraphPart from, GraphPart... successors) {
        for (GraphPart to : successors) {
            from.nextParts.add(to);
            to.refs.add(from);
        }
    }

    @Test
    public void testSwitchesInExitingBranchesDoNotEncloseLoop() throws InterruptedException {
        GraphPart entry = new GraphPart(0, 0);
        GraphPart condition = new GraphPart(1, 1);
        GraphPart firstCheck = new GraphPart(2, 2);
        GraphPart firstSwitch = new GraphPart(3, 3);
        GraphPart secondCheck = new GraphPart(4, 4);
        GraphPart firstCase = new GraphPart(5, 5);
        GraphPart secondSwitch = new GraphPart(6, 6);
        GraphPart firstDefault = new GraphPart(7, 7);
        GraphPart firstJoin = new GraphPart(8, 8);
        GraphPart secondCase = new GraphPart(9, 9);
        GraphPart done = new GraphPart(10, 10);
        GraphPart secondDefault = new GraphPart(11, 11);
        connect(entry, condition);
        connect(condition, firstCheck, done);
        connect(firstCheck, firstSwitch, secondCheck);
        connect(secondCheck, secondSwitch, condition);
        connect(firstSwitch, firstCase, firstDefault);
        connect(firstCase, firstJoin);
        connect(firstDefault, firstJoin);
        connect(firstJoin, done);
        connect(secondSwitch, secondCase, secondDefault);
        connect(secondCase, done);
        connect(secondDefault, done);
        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(entry, condition, firstCheck,
                firstSwitch, secondCheck, firstCase, secondSwitch, firstDefault, firstJoin,
                secondCase, done, secondDefault));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(entry), loops, Collections.emptyList(),
                parts, Arrays.asList(Arrays.asList(firstCase, firstDefault), Arrays.asList(secondCase, secondDefault)),
                Arrays.asList(firstJoin, done), true);
        Assert.assertEquals(loops.size(), 1);
        Assert.assertSame(loops.get(0).loopContinue, condition);
        Assert.assertSame(loops.get(0).loopBreak, done);
    }

    @Test
    public void testEnclosingSwitchKeepsEarlyExitSeparate() throws InterruptedException {
        GraphPart dispatch = new GraphPart(0, 0);
        GraphPart firstCase = new GraphPart(1, 1);
        GraphPart secondCase = new GraphPart(2, 2);
        GraphPart condition = new GraphPart(3, 3);
        GraphPart body = new GraphPart(4, 4);
        GraphPart afterLoop = new GraphPart(5, 5);
        GraphPart earlyExit = new GraphPart(6, 6);
        GraphPart following = new GraphPart(7, 7);
        GraphPart done = new GraphPart(8, 8);
        connect(dispatch, firstCase, secondCase);
        connect(firstCase, condition);
        connect(condition, body, afterLoop);
        connect(body, condition, earlyExit);
        connect(afterLoop, following);
        connect(following, done);
        connect(earlyExit, done);
        connect(secondCase, done);
        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(dispatch, firstCase, secondCase,
                condition, body, afterLoop, earlyExit, following, done));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(dispatch), loops, Collections.emptyList(),
                parts, Collections.singletonList(Arrays.asList(firstCase, secondCase)),
                Collections.singletonList(done), true);
        Assert.assertEquals(loops.size(), 1);
        Assert.assertSame(loops.get(0).loopBreak, afterLoop);
    }

    @Test
    public void testSwitchFallthroughKeepsExitOfLoopInsideCase() throws InterruptedException {
        GraphPart dispatch = new GraphPart(0, 0);
        GraphPart emptyCase = new GraphPart(1, 1);
        GraphPart loopCase = new GraphPart(2, 2);
        GraphPart condition = new GraphPart(3, 3);
        GraphPart body = new GraphPart(4, 4);
        GraphPart done = new GraphPart(5, 5);
        connect(dispatch, emptyCase, loopCase, done);
        connect(emptyCase, done);
        connect(loopCase, condition);
        connect(condition, body, done);
        connect(body, condition);
        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(dispatch, emptyCase, loopCase, condition, body, done));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(dispatch), loops, Collections.emptyList(),
                parts, Collections.singletonList(Arrays.asList(emptyCase, loopCase, done)),
                Collections.singletonList(done), true);
        Assert.assertEquals(loops.size(), 1);
        Assert.assertSame(loops.get(0).loopContinue, condition);
        Assert.assertSame(loops.get(0).loopBreak, done);
    }

    @Test
    public void testGroupedSwitchCasesKeepEveryLoopExit() throws InterruptedException {
        GraphPart entry = new GraphPart(0, 0);
        GraphPart dispatch = new GraphPart(1, 1);
        GraphPart firstExit = new GraphPart(2, 2);
        GraphPart body = new GraphPart(3, 3);
        GraphPart lastExit = new GraphPart(4, 4);
        GraphPart done = new GraphPart(5, 5);
        connect(entry, dispatch);
        connect(dispatch, firstExit, firstExit, body, body, lastExit);
        connect(body, dispatch);
        connect(firstExit, done);
        connect(lastExit, done);
        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(entry, dispatch, firstExit, body, lastExit, done));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(entry), loops, Collections.emptyList(), parts,
                Collections.singletonList(Arrays.asList(firstExit, firstExit, body, body, lastExit)),
                Collections.singletonList(done), true);
        Assert.assertEquals(loops.size(), 1);
        Assert.assertSame(loops.get(0).loopBreak, done);
    }

    @Test
    public void testLoopExitFallbackPrefersTailSharedByMorePaths() throws InterruptedException {
        GraphPart condition = new GraphPart(0, 0);
        GraphPart firstCheck = new GraphPart(1, 1);
        GraphPart secondCheck = new GraphPart(2, 2);
        GraphPart body = new GraphPart(3, 3);
        GraphPart prefix = new GraphPart(4, 4);
        GraphPart alternate = new GraphPart(5, 5);
        GraphPart shared = new GraphPart(6, 6);
        GraphPart otherFirst = new GraphPart(7, 7);
        GraphPart otherSecond = new GraphPart(8, 8);
        GraphPart otherShared = new GraphPart(9, 9);
        connect(condition, firstCheck, secondCheck, alternate, shared, otherFirst, otherSecond);
        connect(firstCheck, body, prefix);
        connect(secondCheck, body, prefix);
        connect(body, condition);
        connect(prefix, shared);
        connect(alternate, shared);
        connect(otherFirst, otherShared);
        connect(otherSecond, otherShared);
        Set<GraphPart> parts = new LinkedHashSet<>(Arrays.asList(condition, firstCheck, secondCheck, body,
                prefix, alternate, shared, otherFirst, otherSecond, otherShared));
        List<Loop> loops = new ArrayList<>();
        new LoopDetector().detectLoops(Collections.singletonList(condition), loops, Collections.emptyList(), parts,
                Collections.emptyList(), Collections.emptyList(), true);
        Assert.assertEquals(loops.size(), 1);
        Assert.assertSame(loops.get(0).loopBreak, shared);
    }

    @Test
    public void testAgainstOrderedSetAlgorithm() throws Exception {
        Method method = LoopDetector.class.getDeclaredMethod("findNearestCommonPostDominator",
                Set.class, Set.class, Set.class, PrevNextWalker.class, Set.class, Set.class, Set.class);
        method.setAccessible(true);
        Random random = new Random(2750);
        PrevNextWalker walker = new PrevNextWalker() {
            @Override
            public List<GraphPart> getNext(GraphPart part) {
                return part.nextParts;
            }

            @Override
            public List<GraphPart> getPrev(GraphPart part) {
                return part.refs;
            }
        };
        for (int run = 0; run < 1000; run++) {
            List<GraphPart> parts = new ArrayList<>();
            int count = run % 20 == 0 ? 65 + random.nextInt(80) : 2 + random.nextInt(20);
            for (int i = 0; i < count; i++) {
                GraphPart part = new GraphPart(i, i);
                part.closedTime = random.nextInt(3); // deliberately create ties
                parts.add(part);
            }
            Set<GraphPartEdge> backEdges = new LinkedHashSet<>();
            Set<GraphPartEdge> ignoredEdges = new LinkedHashSet<>();
            for (GraphPart part : parts) {
                for (GraphPart next : parts) {
                    if (random.nextInt(6) == 0) {
                        part.nextParts.add(next);
                        next.refs.add(part);
                        if (random.nextInt(10) == 0) {
                            backEdges.add(new GraphPartEdge(part, next));
                        }
                        if (random.nextInt(10) == 0) {
                            ignoredEdges.add(new GraphPartEdge(part, next));
                        }
                    }
                }
            }
            Set<GraphPart> starts = new LinkedHashSet<>();
            Set<GraphPart> stops = new LinkedHashSet<>();
            Set<GraphPart> excluded = new LinkedHashSet<>();
            Set<GraphPart> direct = new LinkedHashSet<>();
            for (GraphPart part : parts) {
                if (random.nextBoolean()) {
                    starts.add(part);
                }
                if (random.nextInt(5) == 0) {
                    stops.add(part);
                }
                if (random.nextInt(5) == 0) {
                    excluded.add(part);
                }
                if (random.nextBoolean()) {
                    direct.add(part);
                }
            }
            GraphPart expected = referencePostDominator(starts, backEdges, stops, walker, ignoredEdges, excluded, direct);
            GraphPart actual = (GraphPart) method.invoke(new LoopDetector(), starts, backEdges, stops,
                    walker, ignoredEdges, excluded, direct);
            Assert.assertSame(actual, expected, "Graph " + run);
        }
    }

    // Original ordered-set fixed point algorithm, retained as an independent
    // oracle for cycles, multiple exits, excluded edges and exact tie-breaking.
    private GraphPart referencePostDominator(
            Set<GraphPart> startParts,
            Set<GraphPartEdge> backEdges,
            Set<GraphPart> stopParts,
            PrevNextWalker pnw,
            Set<GraphPartEdge> ignoredEdges,
            Set<GraphPart> excludedCandidates,
            Set<GraphPart> directOutParts
    ) {
        if (startParts.isEmpty()) {
            return null;
        }

        Map<GraphPart, List<GraphPart>> successors = new LinkedHashMap<>();
        Queue<GraphPart> queue = new ArrayDeque<>();
        queue.addAll(startParts);

        while (!queue.isEmpty()) {
            GraphPart part = queue.poll();
            if (successors.containsKey(part)) {
                continue;
            }

            List<GraphPart> nextParts = new ArrayList<>();
            successors.put(part, nextParts);
            if (stopParts.contains(part)) {
                continue;
            }

            for (GraphPart next : pnw.getNext(part)) {
                GraphPartEdge edge = new GraphPartEdge(part, next);
                if (ignoredEdges.contains(edge) || backEdges.contains(edge)) {
                    continue;
                }
                nextParts.add(next);
                queue.offer(next);
            }
        }

        Set<GraphPart> allParts = new LinkedHashSet<>(successors.keySet());
        Map<GraphPart, Set<GraphPart>> postDominators = new LinkedHashMap<>();
        for (Map.Entry<GraphPart, List<GraphPart>> entry : successors.entrySet()) {
            Set<GraphPart> initial = new LinkedHashSet<>();
            if (entry.getValue().isEmpty()) {
                initial.add(entry.getKey());
            } else {
                initial.addAll(allParts);
            }
            postDominators.put(entry.getKey(), initial);
        }

        boolean changed;
        do {
            changed = false;
            for (Map.Entry<GraphPart, List<GraphPart>> entry : successors.entrySet()) {
                GraphPart part = entry.getKey();
                List<GraphPart> nextParts = entry.getValue();
                Set<GraphPart> updated = new LinkedHashSet<>();
                updated.add(part);

                if (!nextParts.isEmpty()) {
                    Set<GraphPart> common = new LinkedHashSet<>(postDominators.get(nextParts.get(0)));
                    for (int i = 1; i < nextParts.size(); i++) {
                        common.retainAll(postDominators.get(nextParts.get(i)));
                    }
                    updated.addAll(common);
                }

                if (!updated.equals(postDominators.get(part))) {
                    postDominators.put(part, updated);
                    changed = true;
                }
            }
        } while (changed);

        Set<GraphPart> commonPostDominators = null;
        for (GraphPart startPart : startParts) {
            Set<GraphPart> partPostDominators = postDominators.get(startPart);
            if (partPostDominators == null) {
                return null;
            }
            if (commonPostDominators == null) {
                commonPostDominators = new LinkedHashSet<>(partPostDominators);
            } else {
                commonPostDominators.retainAll(partPostDominators);
            }
        }

        if (commonPostDominators == null) {
            return null;
        }
        commonPostDominators.removeAll(excludedCandidates);
        if (commonPostDominators.isEmpty()) {
            return null;
        }

        Map<GraphPart, Long> distanceSums = new LinkedHashMap<>();
        for (GraphPart candidate : commonPostDominators) {
            distanceSums.put(candidate, 0L);
        }

        for (GraphPart startPart : startParts) {
            Map<GraphPart, Integer> distances = new LinkedHashMap<>();
            Queue<GraphPart> distanceQueue = new ArrayDeque<>();
            distances.put(startPart, 0);
            distanceQueue.offer(startPart);

            while (!distanceQueue.isEmpty()) {
                GraphPart part = distanceQueue.poll();
                int nextDistance = distances.get(part) + 1;
                for (GraphPart next : successors.get(part)) {
                    if (!distances.containsKey(next)) {
                        distances.put(next, nextDistance);
                        distanceQueue.offer(next);
                    }
                }
            }

            for (GraphPart candidate : commonPostDominators) {
                Integer distance = distances.get(candidate);
                if (distance == null) {
                    return null;
                }
                distanceSums.put(candidate, distanceSums.get(candidate) + distance);
            }
        }

        GraphPart result = null;
        for (GraphPart candidate : commonPostDominators) {
            if (result == null) {
                result = candidate;
                continue;
            }

            int distanceCompare = Long.compare(distanceSums.get(candidate), distanceSums.get(result));
            if (distanceCompare < 0
                    || (distanceCompare == 0
                    && directOutParts.contains(candidate)
                    && !directOutParts.contains(result))
                    || (distanceCompare == 0
                    && directOutParts.contains(candidate) == directOutParts.contains(result)
                    && candidate.closedTime < result.closedTime)) {
                result = candidate;
            }
        }

        Logger.getLogger(LoopDetector.class.getName()).log(
                Level.FINE,
                "nearest common local exit postdominator: {0}",
                result
        );
        return result;
    }

}
