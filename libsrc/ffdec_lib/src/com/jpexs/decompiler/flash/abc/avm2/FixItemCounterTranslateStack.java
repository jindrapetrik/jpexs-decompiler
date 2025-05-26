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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;

/**
 * Translate stack that counts the number of items pushed and not popped.
 *
 * @author JPEXS
 */
public class FixItemCounterTranslateStack extends TranslateStack {

    /**
     *
     */
    private int fixItemCount = Integer.MAX_VALUE;

    /**
     * Constructs a new FixItemCounterTranslateStack
     *
     * @param path Path
     */
    public FixItemCounterTranslateStack(String path) {
        super(null); //null path => do not add PushItems
    }

    /**
     * Pops an item from the stack
     *
     * @return The popped item
     */
    @Override
    public GraphTargetItem pop() {
        GraphTargetItem result = super.pop();
        int itemCount = size();
        if (itemCount < fixItemCount) {
            fixItemCount = itemCount;
        }
        return result;
    }

    /**
     * Removes the element at the specified index
     *
     * @param index the index of the element to be removed
     * @return The removed element
     */
    @Override
    public synchronized GraphTargetItem remove(int index) {
        if (index < fixItemCount) {
            fixItemCount = index;
        }
        return super.remove(index);
    }

    /**
     * All items were never popped.
     *
     * @return True if all items were never popped
     */
    public boolean allItemsFixed() {
        return size() <= fixItemCount;
    }

    /**
     * Gets the number of items that were never popped
     *
     * @return The number of items that were never popped
     */
    public int getFixItemCount() {
        return fixItemCount;
    }
}
