/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public interface SetTypeIns {

    public static void handleResult(GraphTargetItem value, TranslateStack stack, List<GraphTargetItem> output, AVM2LocalData localData, GraphTargetItem result, int regId) {
        GraphTargetItem notCoercedValue = value;
        if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
            notCoercedValue = value.value;
        }

        if (notCoercedValue instanceof DuplicateItem) {
            GraphTargetItem insideDup = notCoercedValue.value;
            if (!AVM2Item.mustStayIntact1(insideDup.getNotCoercedNoDup())) {
                if (!stack.isEmpty() && stack.peek() == insideDup) {
                    stack.pop();

                    if ((insideDup instanceof DuplicateItem) && regId > -1) {
                        int numDups = 1;
                        while ((insideDup instanceof DuplicateItem) && !stack.isEmpty() && stack.peek() == insideDup.value) {
                            insideDup = insideDup.value;
                            stack.pop();
                            numDups++;
                        }
                        if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                            value.value = insideDup;
                        } else {
                            value = insideDup;
                        }
                        result.value = value;
                        output.add(result);
                        for (int i = 0; i < numDups; i++) {
                            stack.push(new LocalRegAVM2Item(null, localData.lineStartInstruction, regId, value));
                        }
                        return;
                    } else {

                        if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                            value.value = insideDup;
                        } else {
                            value = insideDup;
                        }

                        result.value = value;

                        if (regId > -1 && AVM2Item.mustStayIntact2(insideDup.getNotCoerced())) { //hack
                            output.add(result);
                            stack.push(new LocalRegAVM2Item(null, localData.lineStartInstruction, regId, value));
                            return;
                        }

                        stack.push(result);
                        return;
                    }
                }
            }
        }
        output.add(result);
    }
}
