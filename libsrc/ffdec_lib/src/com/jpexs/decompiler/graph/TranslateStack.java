/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stack for translation.
 *
 * @author JPEXS
 */
public class TranslateStack extends Stack<GraphTargetItem> {

    /**
     * Pop item
     */
    private PopItem pop;

    /**
     * Path
     */
    private final String path;

    private Map<String, GraphTargetItem> marks = new HashMap<>();

    
    /**
     * Sets mark.
     * @param name Name
     * @param value Value
     */
    public void setMark(String name, GraphTargetItem value) {
        marks.put(name, value);
    }
    
    /**
     * Gets mark.
     * @param name Name
     * @return Value
     */
    public GraphTargetItem getMark(String name) {
        return marks.get(name);
    }
    
    /**
     * Simplifies all items in the stack.
     */
    public void simplify() {
        for (int i = 0; i < size(); i++) {
            set(i, get(i).simplify(""));
        }
    }

    /**
     * Constructs new TranslateStack with the specified path.
     *
     * @param path The path.
     */
    public TranslateStack(String path) {
        this.path = path;
    }

    /**
     * Gets the path.
     *
     * @return The path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the pop item.
     *
     * @return The pop item.
     */
    private PopItem getPop() {
        if (pop == null) {
            pop = new PopItem(null, null); //TODO: linestart?
        }

        return pop;
    }

    /**
     * Gets the item at the specified index.
     *
     * @param index index of the element to return
     * @return The element at the specified position in this list
     */
    @Override
    public synchronized GraphTargetItem get(int index) {
        if (path != null) {
            if (index >= this.size() || index < 0) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Get item outside of bounds of stack", path);
                return getPop();
            }
        }
        return super.get(index);
    }

    /**
     * Gets the item at the top of the stack.
     *
     * @return The item at the top of the stack.
     */
    @Override
    public synchronized GraphTargetItem peek() {
        if (path != null) {
            if (this.isEmpty()) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Peek empty stack", path);
                return getPop();
            }
        }
        return super.peek();
    }

    /**
     * Gets the item at the specified index from the top of the stack.
     *
     * @param index The index.
     * @return The item at the specified index from the top of the stack.
     */
    public synchronized GraphTargetItem peek(int index) {
        if (path != null) {
            if (index > this.size()) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Peek item from stack", path);
                return getPop();
            }
        }
        return super.get(size() - index);
    }

    /**
     * Pop the item at the top of the stack.
     *
     * @return The item at the top of the stack.
     */
    @Override
    public synchronized GraphTargetItem pop() {
        if (path != null) {
            if (this.isEmpty()) {
                PopItem oldpop = getPop();
                pop = null;
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Pop empty stack", path);
                return oldpop;
            }
        }
        return super.pop();
    }
}
