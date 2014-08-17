/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.action.parser.ParseException;
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

    public ActionDeobfuscateJump(FlasmLexer lexer) throws IOException, ParseException {
        super(lexer);
    }

    @Override
    public String toString() {
        return "FFDec_DeobfuscateJump " + getJumpOffset();
    }

}
