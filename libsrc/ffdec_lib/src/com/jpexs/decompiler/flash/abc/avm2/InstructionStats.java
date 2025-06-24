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

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;

/**
 * Instruction statistics.
 *
 * @author JPEXS
 */
public class InstructionStats {

    /**
     * Whether the instruction has been seen while walking the instruction list
     */
    public boolean seen = false;

    /**
     * Stack position before the instruction
     */
    public int stackpos = 0;

    /**
     * Scope position before the instruction
     */
    public int scopepos = 0;

    /**
     * Stack position after the instruction
     */
    public int stackpos_after = 0;

    /**
     * Scope position after the instruction
     */
    public int scopepos_after = 0;

    /**
     * Instruction
     */
    public AVM2Instruction ins;

    /**
     * Constructs a new InstructionStats object
     *
     * @param ins Instruction
     */
    public InstructionStats(AVM2Instruction ins) {
        this.ins = ins;
    }
}
