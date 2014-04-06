/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        super(0, null, new int[0], new byte[0]);
        this.markType = markType;
        this.exceptionId = exceptionId;
        this.definition = new InstructionDefinition(0, "--mark", new int[0]);
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

}
