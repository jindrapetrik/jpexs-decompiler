/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.action.deobfuscation;

import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;

/**
 *
 * @author JPEXS
 */
public class FixItemCounterTranslateStack extends TranslateStack {

    private int fixItemCount = Integer.MAX_VALUE;

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
