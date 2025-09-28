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
package com.jpexs.decompiler.flash.abc.avm2.instructions.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.Set;

/**
 * Coerce or convert type instruction interface.
 *
 * @author JPEXS
 */
public interface CoerceOrConvertTypeIns {

    /**
     * Get target type of coercing or converting.
     *
     * @param usedDeobfuscations Used deobfuscations
     * @param abc ABC
     * @param constants Constants
     * @param ins Instruction
     * @return Target type
     */
    public GraphTargetItem getTargetType(Set<String> usedDeobfuscations, ABC abc, AVM2ConstantPool constants, AVM2Instruction ins);
}
