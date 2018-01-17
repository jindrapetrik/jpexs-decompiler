/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;

/**
 *
 * @author JPEXS
 */
public class FixItemCounterTranslateStack extends TranslateStack {

    private int fixItemCount = Integer.MAX_VALUE;

    public FixItemCounterTranslateStack(String path) {
        super(null); //null path => do not add PushItems
    }

    @Override
    public GraphTargetItem pop() {
        GraphTargetItem result = super.pop();
        int itemCount = size();
        if (itemCount < fixItemCount) {
            fixItemCount = itemCount;
        }
        return result;
    }

    @Override
    public synchronized GraphTargetItem remove(int index) {
        if (index < fixItemCount) {
            fixItemCount = index;
        }
        return super.remove(index);
    }

    public boolean allItemsFixed() {
        return size() <= fixItemCount;
    }

    public int getFixItemCount() {
        return fixItemCount;
    }
}
