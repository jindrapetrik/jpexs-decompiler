/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.IntUnaryOperator;

/** Layer extents and blank keys that the SWF display list cannot describe on its own. */
final class TimelineBlankFrames {

    private final Map<Integer, Integer> ends = new HashMap<>();
    private final Map<Integer, NavigableSet<Integer>> keys = new HashMap<>();

    int end(int depth) {
        return ends.getOrDefault(depth, -1);
    }

    int lastFrame() {
        return ends.values().stream().mapToInt(Integer::intValue).max().orElse(-1);
    }

    boolean hasKey(int depth, int frame) {
        NavigableSet<Integer> layerKeys = keys.get(depth);
        return layerKeys != null && layerKeys.contains(frame);
    }

    NavigableSet<Integer> keys(int depth) {
        NavigableSet<Integer> layerKeys = keys.get(depth);
        return layerKeys == null ? Collections.emptyNavigableSet() : Collections.unmodifiableNavigableSet(layerKeys);
    }

    void addKey(int depth, int frame) {
        keys.computeIfAbsent(depth, ignored -> new TreeSet<>()).add(frame);
        extend(depth, frame);
    }

    void clearKey(int depth, int frame) {
        NavigableSet<Integer> layerKeys = keys.get(depth);
        if (layerKeys != null && layerKeys.remove(frame) && layerKeys.isEmpty()) {
            keys.remove(depth);
        }
    }

    void extend(int depth, int frame) {
        if (frame > end(depth)) {
            ends.put(depth, frame);
        }
    }

    void insert(int depth, int first, int count) {
        if (end(depth) < first) {
            return;
        }
        shiftKeys(depth, first, count);
        ends.put(depth, end(depth) + count);
    }

    void remove(int depth, int frame) {
        if (end(depth) < frame) {
            return;
        }
        clearKey(depth, frame);
        shiftKeys(depth, frame + 1, -1);
        if (end(depth) == 0) {
            ends.remove(depth);
        } else {
            ends.put(depth, end(depth) - 1);
        }
    }

    void limit(int depth, int last) {
        if (end(depth) <= last) {
            return;
        }
        NavigableSet<Integer> layerKeys = keys.get(depth);
        if (layerKeys != null) {
            layerKeys.tailSet(last, false).clear();
            if (layerKeys.isEmpty()) {
                keys.remove(depth);
            }
        }
        ends.put(depth, last);
    }

    private void shiftKeys(int depth, int first, int count) {
        NavigableSet<Integer> layerKeys = keys.get(depth);
        if (layerKeys != null) {
            NavigableSet<Integer> tail = layerKeys.tailSet(first, true);
            NavigableSet<Integer> shifted = new TreeSet<>();
            for (int key : tail) {
                shifted.add(key + count);
            }
            tail.clear();
            layerKeys.addAll(shifted);
        }
    }

    TimelineBlankFrames copy() {
        return remap(depth -> depth);
    }

    TimelineBlankFrames remap(IntUnaryOperator mapDepth) {
        TimelineBlankFrames copy = new TimelineBlankFrames();
        for (Map.Entry<Integer, Integer> layer : ends.entrySet()) {
            int depth = mapDepth.applyAsInt(layer.getKey());
            if (depth > 0) {
                copy.ends.put(depth, layer.getValue());
                NavigableSet<Integer> layerKeys = keys.get(layer.getKey());
                if (layerKeys != null) {
                    copy.keys.put(depth, new TreeSet<>(layerKeys));
                }
            }
        }
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TimelineBlankFrames)) {
            return false;
        }
        TimelineBlankFrames frames = (TimelineBlankFrames) other;
        return ends.equals(frames.ends) && keys.equals(frames.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ends, keys);
    }
}
