/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntUnaryOperator;

/** Selection of layer rows, separate from any selected frames or artwork. */
final class TimelineLayerSelection {

    private static final State EMPTY = new State(Collections.emptySet(), 0);
    private final List<Runnable> listeners = new ArrayList<>();
    private State state = EMPTY;

    State get() {
        return state;
    }

    Set<Integer> contextDepths(int depth) {
        return state.depths.contains(depth) ? state.depths : Collections.singleton(depth);
    }

    void set(State state) {
        this.state = state;
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    void clear() {
        if (!state.depths.isEmpty()) {
            set(EMPTY);
        }
    }

    void addListener(Runnable listener) {
        listeners.add(listener);
    }

    void click(int depth, boolean toggle, boolean extend) {
        Set<Integer> next = new LinkedHashSet<>();
        if (toggle || extend) {
            next.addAll(state.depths);
        }
        int anchor = depth;
        if (extend && state.anchor > 0) {
            anchor = state.anchor;
            for (int row = Math.min(anchor, depth); row <= Math.max(anchor, depth); row++) {
                next.add(row);
            }
        } else if (!toggle || !next.remove(depth)) {
            next.add(depth);
        }
        set(next.isEmpty() ? EMPTY : new State(next, anchor));
    }

    static final class State {

        final Set<Integer> depths;
        final int anchor;

        private State(Set<Integer> depths, int anchor) {
            this.depths = Collections.unmodifiableSet(new LinkedHashSet<>(depths));
            this.anchor = anchor;
        }

        static State single(int depth) {
            return new State(Collections.singleton(depth), depth);
        }

        State remap(IntUnaryOperator map) {
            Set<Integer> mapped = new LinkedHashSet<>();
            for (int depth : depths) {
                int destination = map.applyAsInt(depth);
                if (destination > 0) {
                    mapped.add(destination);
                }
            }
            if (mapped.isEmpty()) {
                return EMPTY;
            }
            int mappedAnchor = map.applyAsInt(anchor);
            return new State(mapped, mappedAnchor > 0 ? mappedAnchor : mapped.iterator().next());
        }
    }
}
