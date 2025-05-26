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
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
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
        stack.pop(); //Just ignore the value
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
