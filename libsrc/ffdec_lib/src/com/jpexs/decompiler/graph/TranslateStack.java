/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.graph.model.PopItem;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class TranslateStack extends Stack<GraphTargetItem> {

    private static PopItem pop;

    private final String path;

    public TranslateStack(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    private PopItem getPop() {
        if (pop == null) {
            pop = new PopItem(null);
        }

        return pop;
    }

    @Override
    public synchronized GraphTargetItem get(int index) {
        if (path != null) {
            if (index >= this.size() || index < 0) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attemp to Get item outside of bounds of stack", path);
                return getPop();
            }
        }
        return super.get(index);
    }

    @Override
    public synchronized GraphTargetItem peek() {
        if (path != null) {
            if (this.isEmpty()) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attemp to Peek empty stack", path);
                return getPop();
            }
        }
        return super.peek();
    }

    public synchronized GraphTargetItem peek(int index) {
        if (path != null) {
            if (index > this.size()) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attemp to Peek item from stack", path);
                return getPop();
            }
        }
        return super.get(size() - index);
    }

    @Override
    public synchronized GraphTargetItem pop() {
        if (path != null) {
            if (this.isEmpty()) {
                PopItem oldpop = getPop();
                pop = null;
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attemp to Pop empty stack", path);
                return oldpop;
            }
        }
        return super.pop();
    }
}
