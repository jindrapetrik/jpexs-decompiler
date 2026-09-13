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

/** Differential regression for postdominator membership and candidate order. */
public class LoopDetectorPostDominatorTest {

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
