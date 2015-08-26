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
package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscatePop extends ActionPop {

    public ActionDeobfuscatePop() {
    }

    @Override
    public String toString() {
        return "FFDec_DeobfuscatePop";
    }

    @Override
    public void translate(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output, int staticOperation, String path) {
        if (stack.isEmpty()) {
            return;
        }

        stack.pop(); //Just ignore the value
    }

    @Override
    public boolean isDeobfuscatePop() {
        return true;
    }
}
