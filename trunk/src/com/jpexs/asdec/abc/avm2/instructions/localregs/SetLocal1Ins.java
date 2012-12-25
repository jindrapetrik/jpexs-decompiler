/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.abc.avm2.instructions.localregs;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.treemodel.FindPropertyTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SetLocal1Ins extends SetLocalTypeIns {

   public SetLocal1Ins() {
      super(0xd5, "setlocal_1", new int[]{});
   }

   public int getRegisterId(AVM2Instruction ins) {
      return 1;
   }

}
