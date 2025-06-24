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

/**
 * Offset updater interface. Used to update offsets in instructions and
 * operands.
 *
 * @author JPEXS
 */
public interface OffsetUpdater {

    /**
     * Updates instruction offset.
     *
     * @param addr Address of the instruction
     * @return New address of the instruction
     */
    public long updateInstructionOffset(long addr);

    /**
     * Updates operand offset.
     *
     * @param jumpAddr Address of the jump instruction
     * @param targetAddress Address of the target instruction
     * @param offset Operand (offset) of the jump instruction
     * @return New operand offset
     */
    public int updateOperandOffset(long jumpAddr, long targetAddress, int offset);
}
