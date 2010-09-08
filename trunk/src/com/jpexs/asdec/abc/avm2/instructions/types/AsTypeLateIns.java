/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.avm2.instructions.types;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.AsTypeTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class AsTypeLateIns extends InstructionDefinition {

    public AsTypeLateIns() {
        super(0x87, "astypelate", new int[]{});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        Object objClass = lda.operandStack.pop();
        Object obj = lda.operandStack.pop();
        //if obj.class=objClass
        lda.operandStack.push(obj);
        //else push null
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        TreeItem cls = (TreeItem) stack.pop();
        TreeItem val = (TreeItem) stack.pop();
        stack.push(new AsTypeTreeItem(ins, val, cls));
    }
}
