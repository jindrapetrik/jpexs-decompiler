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

package com.jpexs.asdec.action.flashlite;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.FSCommand2TreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionFSCommand2 extends Action {

    public ActionFSCommand2() {
        super(0x2D, 0);
    }

    @Override
    public String toString() {
        return "FSCommand2";
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        long numArgs = popLong(stack);
        TreeItem command = stack.pop();
        List<TreeItem> args = new ArrayList<TreeItem>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }        
        stack.push(new FSCommand2TreeItem(this, command, args));
    }
}
