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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;

/**
 * Finally jump instruction.
 *
 * @author JPEXS
 */
public class FinallyJumpIns extends JumpIns {

    /**
     * Finally clause id
     */
    private final long finallyClauseId;

    /**
     * Constructor.
     * @param finallyClauseId Finally clause id
     */
    public FinallyJumpIns(long finallyClauseId) {
        this.finallyClauseId = finallyClauseId;
    }

    /**
     * Gets clause id.
     * @return Clause id
     */
    public long getClauseId() {
        return finallyClauseId;
    }
}
