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

package com.jpexs.asdec.abc.avm2.instructions.localregs;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.treemodel.FindPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class SetLocal2Ins extends InstructionDefinition implements SetTypeIns, SetLocalTypeIns {

    public SetLocal2Ins() {
        super(0xd6, "setlocal_2", new int[]{});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        TreeItem val = (TreeItem) stack.pop();
        localRegs.put(2, val);
        if (val instanceof NewActivationTreeItem) return;
        if (val instanceof FindPropertyTreeItem) return;
        //if(val.startsWith("catchscope ")) return;
        //if(val.startsWith("newactivation()")) return;
        output.add(new SetLocalTreeItem(ins, 2, val));
    }

    public String getObject(Stack<TreeItem> stack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body) {
        return localRegName(2);
    }

    public int getRegisterId(AVM2Instruction ins) {
        return 2;
    }
}
