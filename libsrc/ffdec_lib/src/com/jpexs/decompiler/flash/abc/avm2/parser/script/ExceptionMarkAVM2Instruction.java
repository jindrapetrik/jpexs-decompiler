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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;

/**
 *
 * @author JPEXS
 */
public class ExceptionMarkAVM2Instruction extends AVM2Instruction {

    public int markType;

    public int exceptionId;

    public ExceptionMarkAVM2Instruction(int exceptionId, int markType) {
        super(0, null, null);
        this.markType = markType;
        this.exceptionId = exceptionId;
        this.definition = new InstructionDefinition(0, "--mark", new int[0], false /*?*/);
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
