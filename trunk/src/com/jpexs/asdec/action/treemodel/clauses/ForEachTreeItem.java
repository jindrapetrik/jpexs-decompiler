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

package com.jpexs.asdec.action.treemodel.clauses;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.ContinueTreeItem;
import com.jpexs.asdec.action.treemodel.EachTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.util.ArrayList;
import java.util.List;


public class ForEachTreeItem extends LoopTreeItem implements Block {

    public EachTreeItem expression;
    public List<TreeItem> commands;

    public ForEachTreeItem(Action instruction, int loopBreak, int loopContinue, EachTreeItem expression, List<TreeItem> commands) {
        super(instruction, loopBreak, loopContinue);
        this.expression = expression;
        this.commands = commands;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret += "loop" + loopBreak + ":\r\n";
        ret += hilight("for ") + expression.toString(constants) + "\r\n{\r\n";
        for (TreeItem ti : commands) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}") + "\r\n";
        ret += ":loop" + loopBreak;
        return ret;
    }

    public List<ContinueTreeItem> getContinues() {
        List<ContinueTreeItem> ret = new ArrayList<ContinueTreeItem>();
        for (TreeItem ti : commands) {
            if (ti instanceof ContinueTreeItem) {
                ret.add((ContinueTreeItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }
}
