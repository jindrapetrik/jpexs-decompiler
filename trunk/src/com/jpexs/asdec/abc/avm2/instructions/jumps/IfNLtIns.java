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

package com.jpexs.asdec.abc.avm2.instructions.jumps;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.IfTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.GeTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.LtTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class IfNLtIns extends InstructionDefinition implements IfTypeIns {

    public IfNLtIns() {
        super(0x0c, "ifnlt", new int[]{AVM2Code.DAT_OFFSET});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        TreeItem v2 = (TreeItem) stack.pop();
        TreeItem v1 = (TreeItem) stack.pop();
        stack.push(new GeTreeItem(ins, v1, v2));
    }

    public void translateInverted(java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, AVM2Instruction ins) {
        TreeItem v2 = (TreeItem) stack.pop();
        TreeItem v1 = (TreeItem) stack.pop();
        stack.push(new LtTreeItem(ins, v1, v2));
    }
}
