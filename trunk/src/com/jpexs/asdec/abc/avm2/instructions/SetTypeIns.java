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
package com.jpexs.asdec.abc.avm2.instructions;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public interface SetTypeIns {

   public abstract String getObject(Stack<TreeItem> stack, ABC abc, AVM2Instruction ins, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, HashMap<Integer, String> localRegNames);
}
