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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.HasTempIndex;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
import java.util.List;

/**
 * DeobfuscatePop instruction. Special pop pused for deobfuscation purposes.
 *
 * @author JPEXS
 */
public class DeobfuscatePopIns extends PopIns {

    /**
     * Instruction name
     */
    public static final String NAME = "ffdec_deobfuscatepop";

    /**
     * Singleton instance
     */
    private static final DeobfuscatePopIns instance = new DeobfuscatePopIns();

    /**
     * Returns singleton instance
     *
     * @return Singleton instance
     */
    public static final DeobfuscatePopIns getInstance() {
        return instance;
    }

    /**
     * Constructs new instance
     */
    private DeobfuscatePopIns() {
        instructionName = NAME;
    }

    /**
     * Translates instruction to high level code.
     *
     * @param localData Local data area
     * @param stack Translate stack
     * @param ins Instruction
     * @param output Output
     * @param path Path
     */
    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        GraphTargetItem item = stack.pop();
        AbstractGraphTargetVisitor visitor = new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem subItem) {
                if ((subItem instanceof DuplicateSourceItem) || (subItem instanceof DuplicateItem)) {
                    int tempIndex = ((HasTempIndex) subItem).getTempIndex();
                    if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                        if (st.tempIndex == tempIndex) {                                                        
                            st.refCount--;
                            if (st.refCount <= 0) {
                                output.remove(output.size() - 1);
                                stack.moveToStack(output);
                            } else if (st.refCount == 1) {
                                for (int i = 0; i < stack.size(); i++) {
                                    if (stack.get(i) instanceof HasTempIndex) {
                                        HasTempIndex ht = (HasTempIndex) stack.get(i);
                                        if (ht.getTempIndex() == tempIndex) {
                                            stack.set(i, st.value);
                                            st.refCount--;
                                            output.remove(output.size() - 1);
                                            stack.moveToStack(output);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
                return true;
            }            
        };
        visitor.visit(item);
        item.visitRecursively(visitor);
    }

    /**
     * Gets number of pops from stack.
     *
     * @param ins Instruction
     * @param abc ABC
     * @return Number of pops from stack
     */
    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}
