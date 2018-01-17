/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscateJump extends ActionJump {

    public ActionDeobfuscateJump(int offset) {
        super(2);
    }

    public ActionDeobfuscateJump(FlasmLexer lexer) throws IOException, ActionParseException {
        super(lexer);
    }

    @Override
    public String toString() {
        return "FFDec_DeobfuscateJump " + getJumpOffset();
    }
}
