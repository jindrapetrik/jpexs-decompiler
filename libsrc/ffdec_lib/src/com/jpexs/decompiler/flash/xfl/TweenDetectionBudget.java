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
package com.jpexs.decompiler.flash.xfl;

import java.util.AbstractList;
import java.util.List;

/** Deterministic search limits for one exported layer. */
final class TweenDetectionBudget {

    private final int maximumFrameSpan;
    private long remaining;

    TweenDetectionBudget(int quality, int sampleCount) {
        maximumFrameSpan = 3 + quality * quality / 10;
        remaining = 4096L + (long) sampleCount * (32 + quality * quality);
    }

    static final class Exhausted extends RuntimeException {

        Exhausted() {
            super(null, null, false, false);
        }
    }

    void consume(long work) {
        if (work > remaining) {
            remaining = 0;
            throw new Exhausted();
        }
        remaining -= work;
    }

    boolean includes(int firstFrame, long lastFrame) {
        return lastFrame - firstFrame <= maximumFrameSpan;
    }

    int lastCandidate(List<ClassicTweenDetector.Sample> samples, int first, int last) {
        int firstFrame = samples.get(first).index;
        while (first < last) {
            int middle = first + (last - first + 1) / 2;
            if (includes(firstFrame, samples.get(middle).index)) {
                first = middle;
            } else {
                last = middle - 1;
            }
        }
        return first;
    }

    List<ClassicTweenDetector.Sample> track(List<ClassicTweenDetector.Sample> samples) {
        return new AbstractList<ClassicTweenDetector.Sample>() {
            @Override
            public ClassicTweenDetector.Sample get(int index) {
                consume(1);
                return samples.get(index);
            }

            @Override
            public int size() {
                return samples.size();
            }
        };
    }
}
