/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import java.util.List;

public class GetLocal2Ins extends GetLocalTypeIns {

    public GetLocal2Ins() {
        super(0xd2, "getlocal_2", new int[]{});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List<Object> arguments) {
        lda.operandStack.push(lda.localRegisters.get(2));
    }

    @Override
    public int getRegisterId(AVM2Instruction par0) {
        return 2;
    }
}
