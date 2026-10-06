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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.graph.SecondPassData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates the two ActionScript 1/2 decompilation passes across all
 * nested action containers.
 *
 * @author JPEXS
 */
final class ActionSecondPassContext implements AutoCloseable {

    private static final ThreadLocal<ActionSecondPassContext> CURRENT = new ThreadLocal<>();

    private final Map<GraphKey, SecondPassData> secondPassData = new HashMap<>();

    private boolean collecting = true;

    private ActionSecondPassContext() {
    }

    public static ActionSecondPassContext open() {
        ActionSecondPassContext context = new ActionSecondPassContext();
        CURRENT.set(context);
        return context;
    }

    public static ActionSecondPassContext current() {
        return CURRENT.get();
    }

    public boolean isCollecting() {
        return collecting;
    }

    public void startCollecting() {
        collecting = true;
    }

    public void startRendering() {
        collecting = false;
    }

    public GraphKey getKey(List<Action> code, int startIp, String path) {
        Action firstAction = code.isEmpty() ? null : code.get(0);
        Action startAction = startIp >= 0 && startIp < code.size() ? code.get(startIp) : null;
        Action lastAction = code.isEmpty() ? null : code.get(code.size() - 1);
        return new GraphKey(firstAction, startAction, lastAction, code.size(), startIp, path);
    }

    public void put(GraphKey key, SecondPassData data) {
        secondPassData.put(key, data);
    }

    public SecondPassData get(GraphKey key) {
        return secondPassData.get(key);
    }

    @Override
    public void close() {
        CURRENT.remove();
    }

    static final class GraphKey {

        private final Action firstAction;

        private final Action startAction;

        private final Action lastAction;

        private final int size;

        private final int startIp;

        private final String path;

        private GraphKey(Action firstAction, Action startAction, Action lastAction, int size, int startIp, String path) {
            this.firstAction = firstAction;
            this.startAction = startAction;
            this.lastAction = lastAction;
            this.size = size;
            this.startIp = startIp;
            this.path = path;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + System.identityHashCode(firstAction);
            hash = 79 * hash + System.identityHashCode(startAction);
            hash = 79 * hash + System.identityHashCode(lastAction);
            hash = 79 * hash + size;
            hash = 79 * hash + startIp;
            hash = 79 * hash + Objects.hashCode(path);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof GraphKey)) {
                return false;
            }
            GraphKey other = (GraphKey) obj;
            return firstAction == other.firstAction
                    && startAction == other.startAction
                    && lastAction == other.lastAction
                    && size == other.size
                    && startIp == other.startIp
                    && Objects.equals(path, other.path);
        }
    }
}
